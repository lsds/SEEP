package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

import uk.ac.imperial.lsds.seep.operator.compose.window.IMicroIncrementalComputation;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;

import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.GPUSelection;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.GPUMicroAggregation.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.GPUMicroAggregationCompiler;

import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;

import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

import uk.ac.imperial.lsds.streamsql.op.gpu.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.amd.aparapi.Range;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.ProfileInfo;
import com.amd.aparapi.opencl.OpenCL;
import com.amd.aparapi.device.Device;
import com.amd.aparapi.device.OpenCLDevice;

public class GPUMicroAggregation 
	implements 
	IStreamSQLOperator, 
	IMicroOperatorCode, 
	IMicroIncrementalComputation, 
	IStatefulMicroOperator {

	private int [] groupByAttributes;
	private PrimitiveType [] typesGroupByAttributes = null;
	
	private int aggregationAttribute;
	private PrimitiveType aggregationAttributeType;
			
	private AggregationType aggregationType;
	
	private GPUSelection havingSel;
	
	interface GPUMicroAggregationInterface extends OpenCL<GPUMicroAggregationInterface> {}
	GPUMicroAggregationInterface operator;
	private String sourceCode;
	private Device dev;
	private OpenCLDevice device;
	private GPUDevice gpu;
	private List<GPUKernel> kernels;
	private GPUInvocationHandler handler;
	
	private int taskid;
	
	private Range __plq_range;
	private Range __wlq_range;
	
	private int __panes, __max_keys, __panes_per_window, __max_tuples_per_pane;
	private static final int MAX_SEGMENTS = 100;

    private int      __local_size;
    private int   [] __local_keys;
    private int   [] __local_values;
    private int   [] __local_offsets;
    private int   [] __local_count;

    private int __plq_threads;
    private int __plq_threads_per_group;
    private int __plq_groups;
    private int __plq_outputs;

    private int __wlq_threads;
    private int __wlq_threads_per_group;
    private int __wlq_groups;
    private int __wlq_outputs;
	
	private int [] __S;
	private int [] __N;
	private int [] __F;
	
	public enum AggregationType 
	{
		MAX, MIN, COUNT, SUM, AVG;
		
		public String asString (int s) { return this.toString() + "(" + s + ")"; }
	}
	
	public GPUMicroAggregation (AggregationType aggregationType, int aggregationAttribute) {
		this (aggregationType, aggregationAttribute, new int[0], new PrimitiveType[0], null);
	}
	
	public GPUMicroAggregation (AggregationType aggregationType, int aggregationAttribute, int[] groupByAttributes, PrimitiveType[] typesGroupByAttributes) {
		this(aggregationType, aggregationAttribute, groupByAttributes, typesGroupByAttributes, null);
	}
	
	public GPUMicroAggregation (AggregationType aggregationType, int aggregationAttribute, int[] groupByAttributes, PrimitiveType[] typesGroupByAttributes, GPUSelection havingSel) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.typesGroupByAttributes = typesGroupByAttributes;
		this.havingSel = havingSel;
		aggregationAttributeType = new IntegerType(0);
		
		dev = Device.best();
		if (! (dev instanceof OpenCLDevice)) {
		}
		device = (OpenCLDevice) dev;
		System.out.println(device);
		gpu = new GPUDevice (device);
		kernels = this.generateKernels ();
		operator = gpu.bind(GPUMicroAggregationInterface.class, kernels);
		handler = (GPUInvocationHandler) Proxy.getInvocationHandler(operator);
		
		taskid = 0;
		__panes = 600;
        __max_keys = 200;
        __panes_per_window = 300;
        __max_tuples_per_pane = 2000;
		
        __plq_groups = __panes;
        __plq_threads_per_group = 256;
        __plq_threads = set__plq_range (__plq_groups, __plq_threads_per_group);
        __plq_outputs = __panes * __max_keys;
		
        __wlq_groups = __panes;
        __wlq_threads_per_group = __max_keys / 4; /* Due to vectorisation. */
        __wlq_threads = set__wlq_range (__wlq_groups, __wlq_threads_per_group);
        __wlq_outputs = __plq_outputs;
		
        __local_size = __panes * __max_tuples_per_pane;
        __local_keys = new int [__local_size];
        __local_values = new int [__local_size];
        __local_offsets = new int [__panes];
        __local_count = new int [__panes];
		
		__S = new int   [__plq_outputs];
		__N = new int   [__plq_outputs];
		__F = new int   [__wlq_outputs];
	}

	public int set__plq_range (int groups, int groupsize) {
        int threads;
        threads = groups * groupsize;
        __plq_range = Range.create(threads, groupsize);
        return threads;
    }
	
    public int set__wlq_range (int groups, int groupsize) {
        int threads;
        threads = groups * groupsize;
        __wlq_range = Range.create(threads, groupsize);
        return threads;
    }
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute) + " ");
		return sb.toString();
	}
	
	public List<GPUKernel> generateKernels () {
		String keyType, valueType;
		/* Since we are using an object hash, key is always an int. */
		keyType = "int";
		if (aggregationAttributeType instanceof IntegerType) valueType =   "int"; else
		if (aggregationAttributeType instanceof   FloatType) valueType = "float";
		else return null;
		
		List<GPUKernel> kernels = new ArrayList<GPUKernel>();
		GPUKernel kernel;
		
		switch (aggregationType) {
			case COUNT:
				break; /* `COUNT` and `SUM` are simply (sub)variants of `AVG`. */
			case SUM:
				break;
			case AVG:
				kernels.add(GPUMicroAggregationCompiler.PLQ(keyType, valueType));
				kernels.add(GPUMicroAggregationCompiler.WLQ(valueType, havingSel));
			case MAX:
				break;
			case MIN:
				break;
			default:
				break;
		}
		
		return kernels;
	}
	
	private int getGroupByKey(MultiOpTuple tuple) {
		int result = 0;
		for (int i = 0; i < this.groupByAttributes.length; i++)
			result = 37 * result + tuple.values[this.groupByAttributes[i]].hashCode();
		
		return result;
	}
	
	private int hash (int highway, int direction, int segment) {
        return (segment + (MAX_SEGMENTS * direction)) * (highway + 1);
    }

    private int getHighway () {
        return 0;
    }

    private int getDirection (int key) {
        return ((key >= MAX_SEGMENTS) ? 1 : 0);
    }

    private int getSegment (int key) {
        return (key - (MAX_SEGMENTS * getDirection(key)));
    }
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}	
	
	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches, IWindowAPI api) {
		System.out.println("[GPU task]");
		/* startTime = System.currentTimeMillis(); */
		IWindowBatch batch = windowBatches.values().iterator().next();
		/* Transform data */
		int [] startIndex = batch.getWindowStartPointers();
		int []   endIndex = batch.getWindowEndPointers();
		int batchSize = startIndex.length;
		int start = startIndex[0];
		int end   =   endIndex[batchSize - 1];
		int totalTuples = end - start + 1;
		/* #panes */
		int panes = 2 * batchSize;
		int   [] offsets = new int   [panes];
		int   [] count   = new int   [panes];
		int   [] keys    = new int   [totalTuples];
		int   [] values  = new int   [totalTuples];
		Arrays.fill(offsets, -1);
		Arrays.fill(count, -1);
		Arrays.fill(keys, -1);
		Arrays.fill(values, -1);
		int segment, highway, direction;
		MultiOpTuple tuple;
		int p = 0; /* Current pane */
		int tpp = 1; /* Tuples per pane */
		offsets[p] = 0;
		long  t, _t = batch.getStartTimestamp();
		for (int i = 0; i < totalTuples; i++) {
			tuple = batch.get(i + start);
			t = tuple.timestamp;
			highway = 0;
			direction = ((IntegerType) tuple.values[3]).value;
			segment = ((IntegerType) tuple.values[4]).value; // / 5280;
			keys[i] = hash(highway, direction, segment);
			values[i] = (int) ((FloatType) tuple.values[1]).value;
			/* Populate offsets and count */
			if (t > _t) {
				/* Set current pane & move to next one */
				count[p] = tpp;
				p += 1;
				tpp = 1;
				offsets[p] = i;
			} else {
				/* Inc. current pane count */
				tpp += 1;
			}
			_t = t;
		}
        /* Populate last pane */
        count[p] = tpp;

        /* Print pane offsets and count
		 * for (int i = 0; i < panes; i++) {
		 * GPUUtils.out(String.format("pane %3d offset %8d count %8d", i, offsets[i], count[i]));
		 * }
		 * */
		Arrays.fill(__S, 0);
        Arrays.fill(__N, 0);
        Arrays.fill(__F, 0);
		
		/* Fill in input data */
        Arrays.fill(__local_keys,    0);
        Arrays.fill(__local_values,  0);
        Arrays.fill(__local_offsets, 0);
        Arrays.fill(__local_count,   0);
		
		System.arraycopy(keys,    0, __local_keys,    0,    keys.length);
        System.arraycopy(values,  0, __local_values,  0,  values.length);
        System.arraycopy(offsets, 0, __local_offsets, 0, offsets.length);
        System.arraycopy(count,   0, __local_count,   0,   count.length);
		
		/* gpu.aggregate(keys, values, offsets, count, gpuResults); */
		Object [] plqArgs = new Object[8 +1];
		Object [] wlqArgs = new Object[6 +1];
		
		plqArgs[0] = __plq_range;
		plqArgs[1] = __local_keys.length; 
		plqArgs[2] = __max_keys;
		plqArgs[3] = __local_keys;
		plqArgs[4] = __local_values;
		plqArgs[5] = __local_offsets;
		plqArgs[6] = __local_count;
		plqArgs[7] = __S;
		plqArgs[8] = __N;
		
		handler.call("plq", plqArgs);
		
		wlqArgs[0] = __wlq_range;
		wlqArgs[1] = __plq_outputs;
		wlqArgs[2] = __max_keys;
		wlqArgs[3] = __panes_per_window;
		wlqArgs[4] = __S;
		wlqArgs[5] = __N;
		wlqArgs[6] = __F;
		
		handler.call("wlq", wlqArgs);
	
		
		String dbg = String.format(
		"task %3d %3d windows start @%6d end @%6d %8d %8d total %8d %4d panes",
		++taskid,
		batch.getWindowStartPointers().length,
		batch.getStartTimestamp(),
		batch.getEndTimestamp(),
		start,
		end,
		totalTuples,
		p + 1
		);
		System.out.println(dbg);
	}
	
	private void processDataPerWindow(Map<Integer, IWindowBatch> windowBatches, IWindowAPI api) {
	}

	@Override
	public void enteredWindow(MultiOpTuple tuple) {
	}

	@Override
	public void exitedWindow(MultiOpTuple tuple) {
	}

	@Override
	public void evaluateWindow(IWindowAPI api) {
	}

	@Override
	public IMicroOperatorCode getNewInstance() {
		return new GPUMicroAggregation(this.aggregationType, this.aggregationAttribute, this.groupByAttributes, this.typesGroupByAttributes, this.havingSel);
	}
}

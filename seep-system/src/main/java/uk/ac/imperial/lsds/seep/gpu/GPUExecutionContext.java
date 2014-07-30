package uk.ac.imperial.lsds.seep.gpu;
//
//import com.amd.aparapi.Range;
//import com.amd.aparapi.Kernel;
//import com.amd.aparapi.ProfileInfo;
//
//import com.amd.aparapi.device.Device;
//import com.amd.aparapi.device.OpenCLDevice;
//
//import com.amd.aparapi.opencl.OpenCL;
//import com.amd.aparapi.opencl.OpenCL.Resource;

import java.lang.UnsupportedOperationException;

public class GPUExecutionContext {
//	
//	@Resource("CongestedSegRel.cl") interface QueryOperator 
//		extends OpenCL<QueryOperator> {
//	
//		public QueryOperator PLQ (
//			Range range,
//			@Arg ("size")              int       size,
//            @GlobalReadOnly ("input")  int  []  input,
//            @GlobalWriteOnly("output") int  [] output);
//		
//		public QueryOperator WLQ (
//			Range range,
//			@Arg ("size")              int       size,
//            @GlobalReadOnly ("input")  int  []  input,
//            @GlobalWriteOnly("output") int  [] output);
//		
//		public QueryOperator AGG (
//			Range range,
//			@Arg ("size")              int       size,
//            @GlobalReadOnly ("input")  int  []  input,
//            @GlobalWriteOnly("output") int  [] output);
//	}
//	
//	private final Device dev;
//	private final OpenCLDevice device;
//	private final QueryOperator q;
//	
//	/* Per function call state */
//	private int __plq_runs;
//	private int __wlq_runs;
//	private int __agg_runs;
//	
//	private Range __plq_range;
//	private Range __wlq_range;
//	private Range __agg_range;
	
	public GPUExecutionContext() {
//		this.__plq_runs = 0;
//		this.__wlq_runs = 0;
//		this.__agg_runs = 0;
//		
//		/* Get device */
//		dev = Device.best();
//		if (! (dev instanceof OpenCLDevice))
//			throw new UnsupportedOperationException
//			("OpenCL device not found.");
//		
//		device = (OpenCLDevice) dev;
//		System.out.println(device);
//		q = device.bind(QueryOperator.class);
	}
	
//	public void set__plq_range (int groups, int groupsize) {
//		int threads;
//		threads = groups * groupsize;
//		__plq_range = Range.create(threads, groupsize);
//	}
//	
//	public void set__wlq_range (int groups, int groupsize) {
//		int threads;
//		threads = groups * groupsize;
//		__wlq_range = Range.create(threads, groupsize);
//	}
//	
//	public void set__agg_range (int groups, int groupsize) {
//		int threads;
//		threads = groups * groupsize;
//		__agg_range = Range.create(threads, groupsize);
//	}
//	
//	public int plq (int [] inputs, int [] outputs) {
//		
//		return ++__plq_runs;
//	}
//	
//	public int wlq (int [] inputs, int [] outputs) {
//		
//		return ++__wlq_runs;
//	}
	
	/* Execute both PLQ and WLQ level queries */
	public int aggregate () { 
		return 1;
//		
//		return ++__agg_runs;
	}
}

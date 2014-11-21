package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import uk.ac.imperial.lsds.streamsql.expressions.eint.IValueExpression;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.GPUSelection;
import uk.ac.imperial.lsds.streamsql.predicates.ComparisonPredicate;

import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.*;
import uk.ac.imperial.lsds.streamsql.op.gpu.*;

public class GPUMicroAggregationCompiler {
	
	public static GPUKernel PLQ (String keyType, String valueType) {
		
		StringBuilder s = new StringBuilder();
		
		s.append("__kernel void plq (\n");
		s.append("\tconst int inputs,\n");
		s.append("\tconst int max_key,\n");
		s.append(String.format("\t__global const %s* keys,\n", keyType));
		s.append(String.format("\t__global const %s* values,\n", valueType));
		s.append("\t__global const int* offsets,\n");
		s.append("\t__global const int* counts,\n");
		s.append(String.format("\t__global %s* sum,\n", valueType));
		s.append("\t__global int* N\n");
		s.append(") {\n");
		
		/* Main body */
		s.append("\tint tid = (int) get_global_id(0);\n");
		s.append("\tint gid = (int) get_group_id(0);\n");
		s.append("\tint group_size = (int) get_local_size(0);\n");
		s.append("\tint lid = (int) get_local_id(0);\n");
		s.append("\n");
		s.append("\tint start = offsets[gid];\n");
		s.append("\tint  pane_size = counts[gid];\n");
		s.append("\tint _pane_size = pane_size + (pane_size - (pane_size % group_size));\n");
		/* Split work between work-group threads */
		s.append("\tint tuples_per_thread = (pane_size < group_size) ? 1 : (_pane_size / group_size);\n");
		s.append(String.format("\t%s k;\n", keyType));
		s.append(String.format("\t%s v;\n", valueType));
		s.append("\tint in, out;\n"); /* input and output indices */
		s.append("\tint idx = lid;\n");
		s.append("\t#pragma unroll\n");
		s.append("\tfor (int i = 0; i < tuples_per_thread; i++) {\n");
		s.append("\t\tif (idx >= pane_size)\n");
		s.append("\t\t\treturn;\n");
		s.append("\t\tin = start + idx;\n");
		s.append("\t\tk = keys[in];\n");
		s.append("\t\tv = values[in];\n");
		s.append("\t\tout = k + max_key * gid;\n");
		s.append("\t\tatom_add(&sum[out], v);\n");
		s.append("\t\tatom_inc(&N[out]);\n");
		s.append("\t\tidx = idx + group_size;\n");
		s.append("\t}\n");
		
		s.append("\treturn ;\n");
		s.append("}\n");
		
		GPUKernel kernel = new GPUKernel("plq");
		kernel.setSource(s.toString());
		
		GPUKernelAttribute __a_inputs = new GPUKernelAttribute("inputs", int.class);
		__a_inputs.addAnnotation(new GPUArg("inputs"));
		kernel.addAttribute(__a_inputs);
		
		GPUKernelAttribute __a_max_key = new GPUKernelAttribute("max_key", int.class);
		__a_max_key.addAnnotation(new GPUArg("max_key"));
		kernel.addAttribute(__a_max_key);
		
		GPUKernelAttribute __a_keys = new GPUKernelAttribute("keys", int [].class);
		__a_keys.addAnnotation(new GPUGlobalReadOnly("keys"));
		kernel.addAttribute(__a_keys);
		
		GPUKernelAttribute __a_values = new GPUKernelAttribute("values", int [].class);
		__a_values.addAnnotation(new GPUGlobalReadOnly("values"));
		kernel.addAttribute(__a_values);
		
		GPUKernelAttribute __a_offsets = new GPUKernelAttribute("offsets", int [].class);
		__a_offsets.addAnnotation(new GPUGlobalReadOnly("offsets"));
		kernel.addAttribute(__a_offsets);
		
		GPUKernelAttribute __a_counts = new GPUKernelAttribute("counts", int [].class);
		__a_counts.addAnnotation(new GPUGlobalReadOnly("counts"));
		kernel.addAttribute(__a_counts);
		
		GPUKernelAttribute __a_sum = new GPUKernelAttribute("sum", int [].class);
		__a_sum.addAnnotation(new GPUGlobalWriteOnly("sum"));
		kernel.addAttribute(__a_sum);
		
		GPUKernelAttribute __a_N = new GPUKernelAttribute("N", int [].class);
		__a_N.addAnnotation(new GPUGlobalWriteOnly("N"));
		kernel.addAttribute(__a_N);
		
		return kernel;
	}
	
	public static GPUKernel WLQ (String valueType, GPUSelection selection) {
		
		ComparisonPredicate predicate;
		StringBuilder s = new StringBuilder();
		
		s.append("__kernel void wlq (\n");
		s.append("\tconst int size,\n");
		s.append("\tconst int groups,\n");
		s.append("\tconst int panes,\n");
		s.append(String.format("\t__global const %s* S,\n", valueType));
		s.append("\t__global const int* N,\n");
		if (selection != null)
			s.append("\t__global int* F\n"); /* The result, if having, is a binary vector. */
		else
			s.append("\t__global float* F\n");
		s.append(") {\n");
		s.append("\tint tid = (int) get_global_id(0);\n");
		s.append("\tint group = (int) get_group_id(0);\n");
		s.append("\tint lid = (int) get_local_id(0);\n");
		s.append("\tint local_size = (int) get_local_size(0);\n");
		s.append(String.format("\t%s4 sum = 0;\n", valueType));
		s.append("\tint4 cnt = 0;\n");
		s.append("\tfloat4 avg = 0;\n");
		if (selection != null)
			s.append("\tint4 result;\n");
		s.append("\tint idx;\n");
		s.append("\tint out = lid + group * local_size;\n");
		s.append("\tif (out * 4 >= (size))\n");
		s.append("\t\treturn;\n");
		s.append("\tfor (int i = group; i < group + panes; i++) {\n");
		s.append("\t\tidx = lid + i * local_size;\n");
		s.append("\t\tif (idx * 4 >= (size))\n");
		s.append("\t\t\treturn ;\n");
		s.append("\t\tsum += vload4(idx, S);\n");
		s.append("\t\tcnt += vload4(idx, N);\n");
		s.append("\t}\n");
		s.append("\tavg = convert_float4(sum) / convert_float4(cnt);\n");
		if (selection != null) {
			predicate = (ComparisonPredicate) selection.getPredicate();
			s.append(String.format("\tresult = (avg %s %s);\n", 
				predicate.getComparisonOperator(), predicate.getRight().eval(null).toString()));
			s.append("\tvstore4(result, out, F);\n");
		} else {
			s.append("\tvstore4(avg, out, F);\n");
		}
		s.append("}\n");
		
		GPUKernel kernel = new GPUKernel("wlq");
		kernel.setSource(s.toString());
		
		GPUKernelAttribute __a_size = new GPUKernelAttribute("size", int.class);
		__a_size.addAnnotation(new GPUArg("size"));
		kernel.addAttribute(__a_size);
		
		GPUKernelAttribute __a_groups = new GPUKernelAttribute("groups", int.class);
		__a_groups.addAnnotation(new GPUArg("groups"));
		kernel.addAttribute(__a_groups);
        
		GPUKernelAttribute __a_panes = new GPUKernelAttribute("panes", int.class);
		__a_panes.addAnnotation(new GPUArg("panes"));
		kernel.addAttribute(__a_panes);
		
		GPUKernelAttribute __a_S = new GPUKernelAttribute("S", int [].class);
		__a_S.addAnnotation(new GPUGlobalReadOnly("S"));
		kernel.addAttribute(__a_S);
        
		GPUKernelAttribute __a_N = new GPUKernelAttribute("N", int [].class);
        __a_N.addAnnotation(new GPUGlobalReadOnly("N"));
        kernel.addAttribute(__a_N);
		
		GPUKernelAttribute __a_F = new GPUKernelAttribute("F", int [].class);
		__a_F.addAnnotation(new GPUGlobalWriteOnly("F"));
		kernel.addAttribute(__a_F);
		
        return kernel;
	}
}


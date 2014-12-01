package uk.ac.imperial.lsds.streamsql.op.gpu;

import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.KernelArgument;

public class KernelCodeGenerator {

	public static Kernel getSelection () {
		
		StringBuilder s = new StringBuilder();
		
		/* Preamble */
		
		/* Signature */
		s.append("__kernel void select (\n");
		s.append("\tconst int tuples,\n");
		s.append("\tconst int  bytes,\n");
		s.append("\t__global const uchar*  input,\n");
		s.append("\t__global const uchar* output,\n");
		s.append(") {\n");
		
		/* Main body */
		
		/* Footer */
		s.append("\treturn ;\n");
		s.append("}\n");
		
		Kernel kernel = new Kernel ("select", s.toString());
		
		/* Create arguments */
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		kernel.addAttribute(_tuples);
		
		KernelAttribute _bytes = new KernelAttribute("bytes", int.class);
		_bytes.addAnnotation (new KernelArgument("bytes"));
		kernel.addAttribute(_bytes);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new KernelArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new KernelArgument("output"));
		kernel.addAttribute(_output);
		
		return kernel;
	}
	
	public static Kernel getProjection () {
		
		StringBuilder s = new StringBuilder();
		
		/* Preamble */
		
		/* Signature */
		s.append("__kernel void select (\n");
		s.append("\tconst int tuples,\n");
		s.append("\tconst int  bytes,\n");
		s.append("\t__global const uchar*  input,\n");
		s.append("\t__global const uchar* output,\n");
		s.append(") {\n");
		
		/* Main body */
		
		/* Footer */
		s.append("\treturn ;\n");
		s.append("}\n");
		
		Kernel kernel = new Kernel ("select", s.toString());
		
		/* Create arguments */
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		kernel.addAttribute(_tuples);
		
		KernelAttribute _bytes = new KernelAttribute("bytes", int.class);
		_bytes.addAnnotation (new KernelArgument("bytes"));
		kernel.addAttribute(_bytes);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new KernelArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new KernelArgument("output"));
		kernel.addAttribute(_output);
		
		return kernel;
	}
	
	public static Kernel getMicroAggregation () {
		
		StringBuilder s = new StringBuilder();
		
		/* Preamble */
		
		/* Signature */
		s.append("__kernel void select (\n");
		s.append("\tconst int tuples,\n");
		s.append("\tconst int  bytes,\n");
		s.append("\t__global const uchar*  input,\n");
		s.append("\t__global const uchar* output,\n");
		s.append(") {\n");
		
		/* Main body */
		
		/* Footer */
		s.append("\treturn ;\n");
		s.append("}\n");
		
		Kernel kernel = new Kernel ("select", s.toString());
		
		/* Create arguments */
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		kernel.addAttribute(_tuples);
		
		KernelAttribute _bytes = new KernelAttribute("bytes", int.class);
		_bytes.addAnnotation (new KernelArgument("bytes"));
		kernel.addAttribute(_bytes);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new KernelArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new KernelArgument("output"));
		kernel.addAttribute(_output);
		
		return kernel;
	}
}

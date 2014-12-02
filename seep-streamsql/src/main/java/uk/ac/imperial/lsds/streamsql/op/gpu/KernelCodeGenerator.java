package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.GlobalReadOnlyArgument;
import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.GlobalWriteOnlyArgument;
import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.KernelArgument;
import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.LocalArgument;

public class KernelCodeGenerator {

	public static Kernel getSelection () {
		
		StringBuilder s = new StringBuilder();
		
		/* Preamble */
		
		/* Signature */
		s.append("__kernel void select (\n");
		s.append("\tconst int tuples,\n");
		s.append("\tconst int  bytes,\n");
		s.append("\t__global const uchar*  input,\n");
		s.append("\t__global uchar* output\n");
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
		_input.addAnnotation (new GlobalReadOnlyArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new GlobalWriteOnlyArgument("output"));
		kernel.addAttribute(_output);
		
		return kernel;
	}
	
	public static Kernel getProjection () {
		
		StringBuilder s = new StringBuilder();
		
		/* Preamble */
		
		/* Signature */
		s.append("__kernel void project (\n");
		s.append("\tconst int tuples,\n");
		s.append("\tconst int  bytes,\n");
		s.append("\t__global const uchar*  input,\n");
		s.append("\t__global uchar* output\n");
		s.append(") {\n");
		
		/* Main body */
		
		/* Footer */
		s.append("\treturn ;\n");
		s.append("}\n");
		
		Kernel kernel = new Kernel ("project", s.toString());
		
		/* Create arguments */
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		kernel.addAttribute(_tuples);
		
		KernelAttribute _bytes = new KernelAttribute("bytes", int.class);
		_bytes.addAnnotation (new KernelArgument("bytes"));
		kernel.addAttribute(_bytes);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new GlobalReadOnlyArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new GlobalWriteOnlyArgument("output"));
		kernel.addAttribute(_output);
		
		return kernel;
	}
	
	public static Kernel getMicroAggregation () {
		
		StringBuilder s = new StringBuilder();
		
		/* Preamble */
		
		/* Signature */
		s.append("__kernel void aggregate (\n");
		s.append("\tconst int tuples,\n");
		s.append("\tconst int  bytes,\n");
		s.append("\t__global const uchar* input,\n");
		s.append("\t__global uchar* output\n");
		s.append(") {\n");
		
		/* Main body */
		
		/* Footer */
		s.append("\treturn ;\n");
		s.append("}\n");
		
		Kernel kernel = new Kernel ("aggregate", s.toString());
		
		/* Create arguments */
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		kernel.addAttribute(_tuples);
		
		KernelAttribute _bytes = new KernelAttribute("bytes", int.class);
		_bytes.addAnnotation (new KernelArgument("bytes"));
		kernel.addAttribute(_bytes);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new GlobalReadOnlyArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new GlobalWriteOnlyArgument("output"));
		kernel.addAttribute(_output);
		
		return kernel;
	}

	public static Kernel getProjection (String filename) {
		
		String source = load (filename);
		Kernel kernel = new Kernel ("project", source);
		
		/* Create arguments */
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		kernel.addAttribute(_tuples);
		
		KernelAttribute _bytes = new KernelAttribute("bytes", int.class);
		_bytes.addAnnotation (new KernelArgument("bytes"));
		kernel.addAttribute(_bytes);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new GlobalReadOnlyArgument("input"));
		kernel.addAttribute(_input);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new GlobalWriteOnlyArgument("output"));
		kernel.addAttribute(_output);
		
		KernelAttribute __input = new KernelAttribute("_input", byte [].class);
		__input.addAnnotation (new LocalArgument("_input"));
		kernel.addAttribute(__input);
		
		KernelAttribute __output = new KernelAttribute("_output", byte [].class);
		__output.addAnnotation (new LocalArgument("_output"));
		kernel.addAttribute(__output);
		
		return kernel;
	}
	
	private static String load (String filename) {
		File file = new File(filename);
		try {
			byte [] bytes = Files.readAllBytes(file.toPath());
			return new String (bytes, "UTF8");
		} catch (FileNotFoundException e) {
			System.err.println(String.format("error: file %s not found", filename));
		} catch (IOException e) {
			System.err.println(String.format("error: cannot read file %s", filename));
		}
		return null;
	}
}

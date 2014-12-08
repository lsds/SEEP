package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.GlobalReadOnlyArgument;
import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.GlobalWriteOnlyArgument;
import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.KernelArgument;
import uk.ac.imperial.lsds.streamsql.op.gpu.annotations.LocalArgument;

public class KernelCodeGenerator {

	public static List<Kernel> getSelection () {
		return null;
	}
	
	public static List<Kernel> getSelection (String filename) {
		
		List<Kernel> kernels = new ArrayList<Kernel>();
		
		String source = load (filename);
		Kernel selectKernel = new Kernel ("selectKernel", source);
		/* The `compact` kernel function is already included in `source` */
		Kernel compactKernel = new Kernel ("compactKernel", "");
		
		/* Create arguments for select kernel */
		
		KernelAttribute _size = new KernelAttribute("size", int.class);
		_size.addAnnotation (new KernelArgument("size"));
		selectKernel.addAttribute(_size);
		
		KernelAttribute _tuples = new KernelAttribute("tuples", int.class);
		_tuples.addAnnotation (new KernelArgument("tuples"));
		selectKernel.addAttribute(_tuples);
		
		KernelAttribute __bundle = new KernelAttribute("_bundle", int.class);
		__bundle.addAnnotation (new KernelArgument("_bundle"));
		selectKernel.addAttribute(__bundle);
		
		KernelAttribute _bundles = new KernelAttribute("bundles", int.class);
		_bundles.addAnnotation (new KernelArgument("bundles"));
		selectKernel.addAttribute(_bundles);
		
		KernelAttribute _input = new KernelAttribute("input", byte [].class);
		_input.addAnnotation (new GlobalReadOnlyArgument("input"));
		selectKernel.addAttribute(_input);
		
		KernelAttribute _flags = new KernelAttribute("flags", int [].class);
		_flags.addAnnotation (new GlobalWriteOnlyArgument("flags"));
		selectKernel.addAttribute(_flags);
		
		KernelAttribute _offsets = new KernelAttribute("offsets", int [].class);
		_offsets.addAnnotation (new GlobalWriteOnlyArgument("offsets"));
		selectKernel.addAttribute(_offsets);
		
		KernelAttribute _buffer = new KernelAttribute("buffer", int [].class);
		_buffer.addAnnotation (new LocalArgument("buffer"));
		selectKernel.addAttribute(_buffer);
		
		/* Create arguments for compact kernel */
		
		compactKernel.addAttribute(_size); /* Common attributes */
		compactKernel.addAttribute(_tuples);
		compactKernel.addAttribute(__bundle);
		compactKernel.addAttribute(_bundles);
		compactKernel.addAttribute(_input);
		
		KernelAttribute __flags = new KernelAttribute("flags", int [].class);
		__flags.addAnnotation (new GlobalReadOnlyArgument("flags"));
		compactKernel.addAttribute(__flags);
		
		KernelAttribute __offsets = new KernelAttribute("offsets", int [].class);
		__offsets.addAnnotation (new GlobalReadOnlyArgument("offsets"));
		compactKernel.addAttribute(__offsets);
		
		KernelAttribute _pivots = new KernelAttribute("pivots", int [].class);
		_pivots.addAnnotation (new GlobalReadOnlyArgument("pivots"));
		compactKernel.addAttribute(_pivots);
		
		KernelAttribute _output = new KernelAttribute("output", byte [].class);
		_output.addAnnotation (new GlobalWriteOnlyArgument("output"));
		compactKernel.addAttribute(_output);
		
		kernels.add( selectKernel);
		kernels.add(compactKernel);
		
		return kernels;
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
	
	public static Kernel getMicroAggregation (String filename) {
		return null;
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

package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;

public class KernelCodeGenerator {
	
	public static String getProjection (ITupleSchema input, ITupleSchema output, String filename) {
		StringBuilder b = new StringBuilder ();
		b.append(getHeader (input, output));
		b.append("\n");
		b.append(getProjectionFunctor(input, output));
		b.append("\n");
		b.append(getProjectionKernel(filename));
		b.append("\n");
		return b.toString();
	}
	
	public static String getHeader (ITupleSchema input, ITupleSchema output) {
		StringBuilder b = new StringBuilder ();
		b.append("#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable\n");
		b.append("\n");
		int  _input_vector_size = getVectorSize ( input);
		int _output_vector_size = getVectorSize (output);
		b.append(String.format("#define  INPUT_VECTOR_SIZE %d\n",  _input_vector_size));
		b.append(String.format("#define OUTPUT_VECTOR_SIZE %d\n", _output_vector_size));
		b.append("\n");
		b.append( getInputHeader( input,  _input_vector_size));
		b.append(getOutputHeader(output, _output_vector_size));
		b.append("\n");
		return b.toString();
	}
	
	private static int getVectorSize (ITupleSchema schema) {
		int result;
		int scalar = 16; /* Vector type is uchar{N}   */
		if (schema.getByteSizeOfTuple() % scalar != 0)
			scalar /= 2;
		result = schema.getByteSizeOfTuple() / scalar;
		return result;
	}
	
	private static String getInputHeader (ITupleSchema schema, int vectors) {
		StringBuilder b = new StringBuilder ();
		b.append("typedef struct {\n");
		b.append("\tlong t;\n");
		for (int i = 1; i < schema.getNumberOfAttributes(); i++)
			b.append(String.format("\tint _%d;\n", i));
		if (schema.getDummyContent().length > 0)
			b.append(String.format("\tuchar padding[%d];\n", 
				schema.getDummyContent().length));
		b.append("} input_tuple_t __attribute__((aligned(1)));\n");
		b.append("\n");
		b.append("typedef union {\n");
		b.append("\tinput_tuple_t tuple;\n");
		b.append(String.format("\tuchar16 vectors[%d];\n", vectors));
		b.append("} input_t;\n");
		return b.toString();
	}
	
	private static String getOutputHeader (ITupleSchema schema, int vectors) {
		StringBuilder b = new StringBuilder ();
		b.append("typedef struct {\n");
		b.append("\tlong t;\n");
		for (int i = 1; i < schema.getNumberOfAttributes(); i++)
			b.append(String.format("\tint _%d;\n", i));
		if (schema.getDummyContent().length > 0)
			b.append(String.format("\tuchar padding[%d];\n", 
				schema.getDummyContent().length));
		b.append("} output_tuple_t __attribute__((aligned(1)));\n");
		b.append("\n");
		b.append("typedef union {\n");
		b.append("\toutput_tuple_t tuple;\n");
		b.append(String.format("\tuchar16 vectors[%d];\n", vectors));
		b.append("} output_t;\n");
		return b.toString();
	}
	
	public static String getFooter () {
		return null;
	}
	
	public static String getSelectionFunctor (IPredicate predicate) {
		return null;
	}
	
	public static String getSelectionKernel (String filename) {
		return load (filename);
	}
	
	public static String getProjectionFunctor (ITupleSchema input, ITupleSchema output) {
		StringBuilder b = new StringBuilder ();
		b.append("inline void projectf (__local input_t *p, __local output_t *q) {\n");
		b.append("\tq->tuple.t = p->tuple.t;\n");
		int idx = 0;
		for (int i = 1; i < output.getNumberOfAttributes(); i++) {
			idx += 1;
			if (idx >= input.getNumberOfAttributes())
				idx = 1;
			b.append(String.format("\tq->tuple._%d = p->tuple._%d;\n",
					i, idx));
		}
		b.append("}\n");
		return b.toString();
	}
	
	public static String getProjectionKernel (String filename) {
		return load (filename);
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

	public static String getReduction (ITupleSchema inputSchema,
			ITupleSchema outputSchema, String filename) {
		
		return load (filename);
	}
}

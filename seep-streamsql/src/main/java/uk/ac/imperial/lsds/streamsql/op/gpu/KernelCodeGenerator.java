package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;

public class KernelCodeGenerator {
	
	public static String getProjection (ITupleSchema input, ITupleSchema output, 
			String filename, int depth) {
		StringBuilder b = new StringBuilder ();
		b.append(getHeader (input, output));
		b.append("\n");
		b.append(getProjectionFunctor(input, output, depth));
		b.append("\n");
		b.append(getProjectionKernel(filename));
		b.append("\n");
		return b.toString();
	}
	
	public static String getSelection(ITupleSchema input, ITupleSchema output, 
			IPredicate predicate, String filename, String customFunctor) {
		
		StringBuilder b = new StringBuilder ();
		b.append(getHeader (input, output));
		b.append("\n");
		b.append(getSelectionFunctor(predicate, customFunctor));
		b.append("\n");
		b.append(getSelectionKernel(filename));
		b.append("\n");
		return b.toString();
	}
	
	public static String getReduction (ITupleSchema input, ITupleSchema output, 
			String filename, AggregationType type, FloatColumnReference _the_aggregate) {
		
		StringBuilder b = new StringBuilder ();
		b.append(getHeader (input, output));
		b.append("\n");
		b.append(getReductionFunctors(type, _the_aggregate));
		b.append("\n");
		b.append(getReductionKernel(filename));
		b.append("\n");
		return b.toString();
	}
	
	public static String getAggregation(
			ITupleSchema inputSchema,
			ITupleSchema outputSchema, 
			String filename, 
			AggregationType type,
			FloatColumnReference _the_aggregate,
			Expression [] groupBy,
			String having) {
		
		StringBuilder b = new StringBuilder ();
		b.append(getHeader (inputSchema, outputSchema));
		b.append(getIntermediateStruct (groupBy));
		b.append("\n");
		b.append(getAggregationFunctors(outputSchema, type, _the_aggregate, groupBy));
		b.append("\n");
		if (having != null) {
			b.append("#define HAVING_CLAUSE");
			b.append("\n");
			b.append(getSelectionAggregationFunctor(having));
			b.append("\n");
		}
		b.append(getAggregationKernel(filename));
		b.append("\n");
		return b.toString();
	}
	
	public static String getThetaJoin(String filename) {
		
		return load (filename);
	}
	
	private static String getIntermediateStruct (Expression [] groupBy) {
		
		StringBuilder b = new StringBuilder ();
		
		b.append("typedef struct {\n");
		/* The first attribute is always a timestamp */
		b.append("\tlong t;\n");
		int byteSize = 8;
		for (int i = 1; i <= groupBy.length; i++) {
			if (groupBy[i-1] instanceof IntExpression) { 
				b.append(String.format("\tint key_%d;\n", i));
				byteSize += 4;
			} else
			if (groupBy[i-1] instanceof FloatExpression) { 
				b.append(String.format("\tfloat key_%d;\n", i));
				byteSize += 4;
			} else
			if (groupBy[i-1] instanceof LongExpression) { 
				b.append(String.format("\tlong key_%d;\n", i));
				byteSize += 8;
			}
		}
		b.append("\tint val;\n");
		b.append("\tint cnt;\n");
		byteSize += 8;
		int byteSize_ = byteSize;
		/* Ensure output tuple size is a power of two */
		while ((byteSize_ & (byteSize_ - 1)) != 0) {
			byteSize_ ++;
		}
		if (byteSize_ > byteSize) {
			/* Add padding */
			b.append(String.format("\tuchar padding[%d];\n", (byteSize_ - byteSize)));
		}
		b.append("} intermediate_tuple_t __attribute__((aligned(1)));\n");
		b.append("\n");
		
		b.append("typedef union {\n");
		b.append("\tintermediate_tuple_t tuple;\n");
		b.append(String.format("\tuchar16 vectors[%d];\n", getVectorSize(byteSize_)));
		b.append("} intermediate_t;\n");
		return b.toString();
	}
	
	public static int getIntermediateStructLength (Expression [] groupBy) {
		
		/* The first attribute is always a timestamp */
		int byteSize_, byteSize = 8;
		for (int i = 1; i <= groupBy.length; i++) {
			if (groupBy[i-1] instanceof IntExpression) { 
				byteSize += 4;
			} else
			if (groupBy[i-1] instanceof FloatExpression) { 
				byteSize += 4;
			} else
			if (groupBy[i-1] instanceof LongExpression) { 
				byteSize += 8;
			}
		}
		byteSize += 8;
		byteSize_ = byteSize;
		/* Ensure output tuple size is a power of two */
		while ((byteSize_ & (byteSize_ - 1)) != 0) {
			byteSize_ ++;
		}
		return byteSize_;
	}
	
	public static String getHeader (ITupleSchema input, ITupleSchema output) {
		StringBuilder b = new StringBuilder ();
		b.append("#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable\n");
		b.append("#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable\n");
		b.append("\n");
		b.append("#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable\n");
		b.append("\n");
		b.append("#include \"/Users/akolious/SEEP/seep-system/clib/byteorder.h\"");
		b.append("\n");
		int  _input_vector_size = getVectorSize ( input);
		int _output_vector_size = getVectorSize (output);
		b.append(String.format("#define  INPUT_VECTOR_SIZE %d\n",  _input_vector_size));
		b.append(String.format("#define OUTPUT_VECTOR_SIZE %d\n", _output_vector_size));
		b.append("\n");
		b.append( getInputHeader( input,  _input_vector_size));
		b.append("\n");
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
	
	private static int getVectorSize (int size) {
		int result;
		int scalar = 16; /* Vector type is uchar{N}   */
		if (size % scalar != 0)
			scalar /= 2;
		result = size / scalar;
		return result;
	}
	
	private static String getInputHeader (ITupleSchema schema, int vectors) {
		
		StringBuilder b = new StringBuilder ();
		
		b.append("typedef struct {\n");
		/* The first attribute is always a timestamp */
		b.append("\tlong t;\n");
		for (int i = 1; i < schema.getNumberOfAttributes(); i++) {
			int type = schema.getType(i);
			switch(type) {
			case 1:
				b.append(String.format("\tint _%d;\n", i));
				break;
			case 2:
				b.append(String.format("\tfloat _%d;\n", i));
				break;
			case 3:
				b.append(String.format("\tlong _%d;\n", i));
				break;
			case 0:
				System.err.println("error: failed to auto-generate tuple struct (attribute " + i + " is undefined)");
				System.exit(1);
			}
		}
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
		for (int i = 1; i < schema.getNumberOfAttributes(); i++) {
			int type = schema.getType(i);
			switch(type) {
			case 1:
				b.append(String.format("\tint _%d;\n", i));
				break;
			case 2:
				b.append(String.format("\tfloat _%d;\n", i));
				break;
			case 3:
				b.append(String.format("\tlong _%d;\n", i));
				break;
			case 0:
				System.err.println("error: failed to auto-generate tuple struct");
				System.exit(1);
			}
		}
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
	
	public static String getSelectionFunctor (IPredicate predicate, String customFunctor) {
		
		StringBuilder b = new StringBuilder ();
		b.append("inline int selectf (__global input_t *p) {\n");
		
		if (customFunctor != null) {
			b.append(String.format("\t%s\n", customFunctor));
		} else {
			b.append("\tint value = 0;\n");
			b.append("\tint attr = __bswap32(p->tuple._1);\n");
			b.append("\tvalue = value & ");
			for (int i = 0; i < predicate.getNumPredicates(); i++) {
				if (i == predicate.getNumPredicates() - 1)
					b.append("(attr != 0); \n");
				else
					b.append(String.format("(attr != %d) & ", 
						i - predicate.getNumPredicates() - 1));
			}
			b.append("\treturn value;\n");
		}
		b.append("}\n");
		return b.toString();
	}
	
	public static String getSelectionKernel (String filename) {
		return load (filename);
	}
	
	public static String getProjectionFunctor (ITupleSchema input, ITupleSchema output, int depth) {
		StringBuilder b = new StringBuilder ();
		b.append("inline void projectf (__local input_t *p, __local output_t *q) {\n");
		b.append("\tq->tuple.t = p->tuple.t;\n");
		int idx = 0;
		for (int i = 1; i < output.getNumberOfAttributes(); i++) {
			idx += 1;
			if (idx >= input.getNumberOfAttributes())
				idx = 1;
			if (input.getType(idx) == 2 && depth > 0) {
				/* Floating point expression */
				StringBuilder expr = new StringBuilder ();
				expr.append(String.format("q->tuple._%d = __bswapfp(", idx));
				/* Depth */
				for (int k = 0; k < depth - 1; k++)
					expr.append("3. * ");
				expr.append(String.format("(3. * __bswapfp(p->tuple._%d) / 2.)", idx));
				for (int k = 0; k < depth - 1; k++)
					expr.append(" / 2.");
				expr.append(");\n");
				b.append(String.format("\t%s", expr.toString()));
			} else {
				b.append(String.format("\tq->tuple._%d = p->tuple._%d;\n", i, idx));
			}
		}
		b.append("}\n");
		return b.toString();
	}
	
	public static String getProjectionKernel (String filename) {
		return load (filename);
	}
	
	private static String getReductionFunctors (AggregationType type, FloatColumnReference _the_aggregate) {

		StringBuilder b = new StringBuilder ();

		switch (type) {
		case COUNT:
		case SUM:
		case AVG:
			b.append("#define INITIAL_VALUE 0\n");
			break;
		case MAX:
			b.append("#define INITIAL_VALUE FLT_MIN\n");
			break;
		case MIN:
			b.append("#define INITIAL_VALUE FLT_MAX\n");
			break;
		default:
			break;
		}
		b.append("\n");
		b.append("inline float reducef (float p, float q, int n) {\n");
		switch (type) {
		case COUNT:
			b.append("\treturn (p + 1);\n");
			break;
		case SUM:
			b.append("\treturn (p + q);\n");
			break;
		case AVG:
			b.append("\treturn (n * p + q) / (n + 1);\n");
			break;
		case MAX:
			b.append("\treturn (q > p ? q : p);\n");
			break;
		case MIN:
			b.append("\treturn (q < p ? q : p);\n");
			break;
		default:
			b.append("\treturn -1;\n");
			break;
		}
		b.append ("}\n");
		b.append("\n");
		b.append("inline float mergef (float p, float q, int n) {\n");
		switch (type) {
		case COUNT:
		case SUM:
			b.append("\treturn (p + q);\n");
			break;
		case AVG:
			b.append("\treturn (n * p + q) / (n + 1);\n");
			break;
		case MAX:
			b.append("\treturn (q > p ? q : p);\n");
			break;
		case MIN:
			b.append("\treturn (q < p ? q : p);\n");
			break;
		default:
			b.append("\treturn -1;\n");
			break;
		}
		b.append ("}\n");
		b.append("\n");
		b.append("inline float getAggregateAttribute (__global input_t *p) {\n");
		b.append(String.format("\treturn __bswapfp(p->tuple._%d);\n", _the_aggregate.getColumn()));
		b.append ("}\n");
		b.append("\n");
		return b.toString();
	}
	
	private static String getReductionKernel(String filename) {
		return load (filename);
	}
	
	private static Object getAggregationFunctors(ITupleSchema schema, AggregationType type,
			FloatColumnReference _the_aggregate, Expression [] groupBy) {
		
		StringBuilder b = new StringBuilder ();
		
		b.append("inline int pack_key (__global input_t *p) {\n");
		b.append("\tint key = 1;\n");
		for (int i = 1; i <= groupBy.length; i++) {
			if (groupBy[i-1] instanceof IntExpression) {
				b.append(String.format(String.format("\tkey *= __bswap32(p->tuple._%d);\n", 
					((IntColumnReference) groupBy[i-1]).getColumn())));
			} else
			if (groupBy[i-1] instanceof FloatExpression) { 
				b.append(String.format(String.format("\tkey *= convert_int(__bswapfp(p->tuple._%d));\n", 
					((FloatColumnReference) groupBy[i-1]).getColumn())));
			} else
			if (groupBy[i-1] instanceof LongExpression) { 
				b.append(String.format(String.format("\tkey *= convert_int(__bswap64(p->tuple._%d));\n", 
					((FloatColumnReference) groupBy[i-1]).getColumn())));
			}
			/* How to pack them? */
			b.append("\treturn key;\n");
		}
		b.append ("}\n");
		b.append("\n");
		b.append("inline void storef (__global intermediate_t *out, __global input_t *p) {\n");
		/* Store the timestamp */
		b.append("\tout->tuple.t = p->tuple.t;\n");
		/* Store the (composite) key */
		for (int i = 1; i <= groupBy.length; i++) {
			if (groupBy[i-1] instanceof IntExpression) { 
				b.append(String.format(String.format("\tout->tuple.key_%d = p->tuple._%d;\n", 
					i, ((IntColumnReference) groupBy[i-1]).getColumn())));
			} else
			if (groupBy[i-1] instanceof FloatExpression) { 
				b.append(String.format(String.format("\tout->tuple.key_%d = p->tuple._%d;\n", 
					i, ((FloatColumnReference) groupBy[i-1]).getColumn())));
			} else
			if (groupBy[i-1] instanceof LongExpression) { 
				b.append(String.format(String.format("\tout->tuple.key_%d = p->tuple._%d;\n", 
					i, ((FloatColumnReference) groupBy[i-1]).getColumn())));
			}
		}
		/* Update the value */
		switch (type) {
		case COUNT:
			b.append ("\tatomic_inc ((global int *) &(out->tuple.val));\n");
		case SUM:
		case AVG:
			b.append (String.format("\tatomic_add ((global int *) &(out->tuple.val), convert_int(__bswapfp(p->tuple._%d)));\n",
				_the_aggregate.getColumn()));
			break;
		case MAX:
			b.append (String.format("\tatomic_max ((global int *) &(out->tuple.val), convert_int(__bswapfp(p->tuple._%d)));\n",
				_the_aggregate.getColumn()));
			break;
		case MIN:
			b.append (String.format("\tatomic_min ((global int *) &(out->tuple.val), convert_int(__bswapfp(p->tuple._%d)));\n",
				_the_aggregate.getColumn()));
			break;
		default:
			b.append("\treturn -1;\n");
			break;
		}
		/* Update the count */
		b.append ("\tatomic_inc ((global int *) &(out->tuple.cnt));\n");
		b.append ("}\n");
		b.append("\n");
		b.append("inline void clearf (__global intermediate_t *p) {\n");
		int vectors = getVectorSize(getIntermediateStructLength(groupBy));
		for (int i = 0; i < vectors; i++)
			b.append(String.format("\tp->vectors[%d] = 0;\n", i));
		switch (type) {
		case COUNT:
		case SUM:
		case AVG:
			b.append ("\tp->tuple.val = 0;\n");
			break;
		case MAX:
			b.append ("\tp->tuple.val = FLT_MIN;\n");
			break;
		case MIN:
			b.append ("\tp->tuple.val = FLT_MAX;\n");
			break;
		default:
			b.append("\treturn -1;\n");
			break;
		}
		b.append ("\tp->tuple.cnt = 0;\n");
		b.append ("}\n");
		b.append("\n");
		b.append("inline void copyf (__global intermediate_t *p, __global output_t *q) {\n");
		/* Store the timestamp */
		b.append("\tq->tuple.t = p->tuple.t;\n");
		/* Store the (composite) key; we assume that same id for attribute and key */
		for (int i = 1; i <= groupBy.length; i++) {
			if (groupBy[i-1] instanceof IntExpression) { 
				b.append(String.format(String.format("\tq->tuple._%d = __bswap32(p->tuple.key_%d);\n", i, i)));
			} else
			if (groupBy[i-1] instanceof FloatExpression) { 
				b.append(String.format(String.format("\tq->tuple._%d = __bswapfp(p->tuple.key_%d);\n", i, i)));
			} else
			if (groupBy[i-1] instanceof LongExpression) { 
				b.append(String.format(String.format("\tq->tuple._%d = __bswap64(p->tuple.key_%d);\n", i, i)));
			}
		}
		/* Store the value */
		int valueIndex = groupBy.length + 1;
		switch (type) {
		case COUNT:
		case SUM:
		case MIN:
		case MAX:
			b.append(String.format(String.format("\tq->tuple._%d = __bswapfp(convert_float(p->tuple.val));\n", valueIndex)));
			break;
		case AVG:
			b.append(String.format(String.format("\tq->tuple._%d = __bswapfp(convert_float(p->tuple.val) / convert_float(p->tuple.cnt));\n", valueIndex)));
			break;
		default:
			b.append(String.format(String.format("\tq->tuple._%d = 0;\n", valueIndex)));
			break;
		}
		b.append ("}\n");
		b.append("\n");
		return b.toString();
	}
	
	public static String getSelectionAggregationFunctor (String customFunctor) {
		
		StringBuilder b = new StringBuilder ();
		
		b.append("inline int selectf (__global intermediate_t *p) {\n");
		b.append(String.format("\t%s\n", customFunctor));
		b.append("}\n");
		return b.toString();
	}
	
	private static String getAggregationKernel(String filename) {
		return load (filename);
	}
	
	public static String load (String filename) {
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

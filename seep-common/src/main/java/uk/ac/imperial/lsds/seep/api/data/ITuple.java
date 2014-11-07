package uk.ac.imperial.lsds.seep.api.data;

/**
 * Tuple is a class that contains deserialized data and an accompanying schema. Data will be deserialized only when
 * is explicitly needed, which will reduce overhead in many cases. Other advantage is that all overhead will be 
 * pushed to the application level, and not be part of the system.
 * @author ra
 *
 */

public class ITuple {

	private Schema schema;
	private byte[] data;
	
}
package uk.ac.imperial.lsds.streamsql.util;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class Util {
	
	public static String generateTupleString(DataTuple tuple) {
		return tuple.getPayload().toString();
	}

}

package uk.ac.imperial.lsds.seep.gc14.operator;

import java.util.Iterator;
import java.util.List;

public class QUtils {

	
	public static float medianOfSortedList(List<Float> list) {
		if (list.size() < 1)
			return 0;
		if (list.size() == 1)
			return list.get(0);
		
		float median = ((list.size() % 2) == 0) ? 
				(list.get((int)(list.size()/2f)) + list.get((int)(list.size()/2f)-1))/2f :
				list.get((int)((list.size() - 1)/2f));

		return median;
	}

	public static List<Float> insertIntoSortedFloatList(List<Float> list, float value, int start, int end) {
		if(list.isEmpty()){
			list.add(value);
			return list;
		}
		
		while (start < end - 1) {
			int middle = (int)((start+end)/2f);
			if (list.get(middle) <= value) 
				start = middle;
			if (list.get(middle) >= value) 
				end = middle;
		}
		if (value < list.get(start))
			list.add(start,value);
		else
			list.add(end,value);

		return list;
	}
	
	public static float[] toPrimitiveArray(List<Float> list) {
		float[] result = new float[list.size()];
	    Iterator<Float> iterator = list.iterator();
	    for (int i = 0; i < result.length; i++) {
	        result[i] = iterator.next().intValue();
	    }
	    return result;
	}

}

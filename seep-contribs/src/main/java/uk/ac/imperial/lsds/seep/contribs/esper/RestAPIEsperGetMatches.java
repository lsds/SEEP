package uk.ac.imperial.lsds.seep.contribs.esper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.util.MultiMap;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.infrastructure.api.RestAPIRegistryEntry;

public class RestAPIEsperGetMatches implements RestAPIRegistryEntry {

	private EsperSingleQueryOperator operator;
	
	public RestAPIEsperGetMatches(EsperSingleQueryOperator operator) {
		this.operator = operator;
	}
	
	@Override
	public Object getAnswer(MultiMap<String> reqParameters) {
		List<DataTuple> result = new ArrayList<DataTuple>();
		List<DataTuple> cache = this.operator.getMatchCache();

		/*
		 * Should we keep the matches?
		 */
		boolean keep = false;
		if (reqParameters.containsKey("keep"))
			keep = Boolean.valueOf(reqParameters.getValue("keep",0));

		
		/*
		 * Do we have a time window?
		 */
		if (reqParameters.containsKey("start")
				&& reqParameters.containsKey("stop")) {
			
			long start = Long.valueOf(reqParameters.getValue("start",0));
			long stop  = Long.valueOf(reqParameters.getValue("stop",0));
			
			List<DataTuple> tmpResult = new ArrayList<DataTuple>();
			synchronized(cache) {
				Iterator<DataTuple> iter = cache.iterator();
				while (iter.hasNext()) {
					DataTuple t = iter.next();
					if (start <= t.getPayload().timestamp && t.getPayload().timestamp <= stop) {
						tmpResult.add(t);
						if (!keep) 
							iter.remove();
					}
				}
			}
			/*
			 * Is there a step to consider?
			 */
			long step = -1;
			if (reqParameters.containsKey("step"))
				step = Long.valueOf(reqParameters.getValue("step",0));
			
			if (step != -1) {
				
				long stepEnd = start + step;
				int i = 0;
				do {
					if (i < tmpResult.size()) {
						DataTuple t = tmpResult.get(i);
						if (t.getPayload().timestamp < stepEnd) {
							result.add(t);
							while ((t.getPayload().timestamp < stepEnd) && i < tmpResult.size())
								t = tmpResult.get(i++);
						}
						else {
							result.add(null);
						}
					}
					else 
						result.add(null);
					
					stepEnd += step;
				}
				while (stepEnd <= stop);
			}
			else {
				result = tmpResult; 
			}
		}
		else {
			/*
			 * No time window
			 */
			synchronized(cache) {
				result.addAll(cache);
				if (!keep) 
					cache.clear();
			}		
		}
		
		return result;
	}

}

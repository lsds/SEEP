package uk.ac.imperial.lsds.seep.contribs.esper;

import org.eclipse.jetty.util.MultiMap;

import uk.ac.imperial.lsds.seep.infrastructure.api.RestAPIRegistryEntry;

public class RestAPIEsperGetMatches implements RestAPIRegistryEntry {

	private EsperSingleQueryOperator operator;
	
	public RestAPIEsperGetMatches(EsperSingleQueryOperator operator) {
		this.operator = operator;
	}
	
	@Override
	public Object getAnswer(MultiMap<String> reqParameters) {
		Object result;
		
		/*
		 * Default case:
		 *  - remove matched tuples from cache
		 *  - send full tuples
		 */
		result = this.operator.getAndEmptyCache();
		
		return result;
	}

}

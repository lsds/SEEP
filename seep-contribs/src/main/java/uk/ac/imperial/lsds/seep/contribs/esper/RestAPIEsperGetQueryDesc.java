package uk.ac.imperial.lsds.seep.contribs.esper;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.MultiMap;

import uk.ac.imperial.lsds.seep.infrastructure.api.RestAPIRegistryEntry;

public class RestAPIEsperGetQueryDesc implements RestAPIRegistryEntry {
	
	private Map<String, Object> queryDesc;
	
	public RestAPIEsperGetQueryDesc(EsperSingleQueryOperator operator) {
		this.queryDesc = new HashMap<>();
		
		this.queryDesc.put("typesPerStream", operator.getTypesPerStream());
		this.queryDesc.put("esperEngineURL", operator.getEsperEngineURL());
		this.queryDesc.put("query", operator.getEsperQuery());
		this.queryDesc.put("name", operator.getName());
		this.queryDesc.put("loggingOfResultsEnabled", operator.isEnableLoggingOfMatches());
	}

	@Override
	public Object getAnswer(MultiMap<String> reqParameters) {
		return this.queryDesc;
	}

}

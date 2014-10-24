package uk.ac.imperial.lsds.seep.contribs.esper;

import org.eclipse.jetty.util.MultiMap;

import uk.ac.imperial.lsds.seep.infrastructure.api.RestAPIRegistryEntry;

public class RestAPIEsperPostQueryUpdate implements RestAPIRegistryEntry {

	private EsperSingleQueryOperator operator;

	public RestAPIEsperPostQueryUpdate(EsperSingleQueryOperator operator) {
		this.operator = operator;
	}
	
	@Override
	public Object getAnswer(MultiMap<String> reqParameters) {

		String query = reqParameters.getValue("query", 0);
		this.operator.initWithNewEsperQuery(query);
		return "Loaded query: " + query;
	}
}

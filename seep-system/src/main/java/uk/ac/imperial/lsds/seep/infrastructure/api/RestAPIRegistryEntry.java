package uk.ac.imperial.lsds.seep.infrastructure.api;

import org.eclipse.jetty.util.MultiMap;

public interface RestAPIRegistryEntry {
	
	public Object getAnswer(MultiMap<String> reqParameters);

}

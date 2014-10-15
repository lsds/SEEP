package uk.ac.imperial.lsds.seepmaster.infrastructure.api;

import org.eclipse.jetty.util.MultiMap;

public interface RestAPIRegistryEntry {
	
	public Object getAnswer(MultiMap<String> reqParameters);

}

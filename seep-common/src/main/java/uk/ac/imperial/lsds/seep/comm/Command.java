package uk.ac.imperial.lsds.seep.comm;

import java.util.Set;

public interface Command {
	
	public String commandName();
	public Set<String> getMandatoryArguments();
	public Set<String> getOptArguments();
	public int getTotalNumberArguments();
	
}

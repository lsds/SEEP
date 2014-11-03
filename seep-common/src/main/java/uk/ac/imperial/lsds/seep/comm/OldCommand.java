package uk.ac.imperial.lsds.seep.comm;

import java.util.Set;

public interface OldCommand {
	
	public String commandName();
	public Set<String> getMandatoryArguments();
	public Set<String> getOptArguments();
	public int getTotalNumberArguments();
	
}

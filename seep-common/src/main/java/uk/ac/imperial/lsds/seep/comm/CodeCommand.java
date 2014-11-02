package uk.ac.imperial.lsds.seep.comm;

import java.util.Set;

public class CodeCommand implements Command{

	private static String commandName = "code";
	
	@Override
	public String commandName() {
		return commandName;
	}

	@Override
	public Set<String> getMandatoryArguments() {
		return null;
	}

	@Override
	public Set<String> getOptArguments() {
		return null;
	}

	@Override
	public int getTotalNumberArguments() {
		return 0;
	}

}

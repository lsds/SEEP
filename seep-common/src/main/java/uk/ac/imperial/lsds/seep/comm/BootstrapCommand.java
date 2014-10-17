package uk.ac.imperial.lsds.seep.comm;

import java.util.HashSet;
import java.util.Set;

public class BootstrapCommand implements Command{
	
	private static String commandName = "bootstrap";
	
	public enum Arguments{
		IP("ip"), 
		PORT("port");
		
		private String key;
		
		Arguments(String key){
			this.key = key;
		}
		
		public String argName(){
			return key;
		}
	}
	
	public enum OptArguments{
		MACHINE_NAME("machine_name"), 
		JVM_VERSION("jvm_version");
		
		private String key;
		
		OptArguments(String key){
			this.key = key;
		}
		
		public String argName(){
			return key;
		}
	}

	@Override
	public String commandName() {
		return commandName;
	}

	@Override
	public Set<String> getMandatoryArguments() {
		Set<String> args = new HashSet<>();
		for(Arguments arg : BootstrapCommand.Arguments.values()){
			args.add(arg.argName());
		}
		return args;
	}

	@Override
	public Set<String> getOptArguments() {
		Set<String> args = new HashSet<>();
		for(OptArguments arg : BootstrapCommand.OptArguments.values()){
			args.add(arg.argName());
		}
		return args;
	}

	@Override
	public int getTotalNumberArguments() {
		return BootstrapCommand.Arguments.values().length + BootstrapCommand.OptArguments.values().length;
	}

}

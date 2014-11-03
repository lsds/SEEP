package uk.ac.imperial.lsds.seep.comm;

import java.util.HashMap;
import java.util.Map;

public class MasterWorkerAPI {

	public enum API{
		BOOTSTRAP(new OldBootstrapCommand()),
		CRASH(new OldCrashCommand()),
		CODE(new OldCodeCommand());
		
		private OldCommand c;
		
		API(OldCommand c){
			this.c = c;
		}
		
		public String commandName(){
			return c.commandName();
		}
		
	}
	
	public static API getAPIByName(String name){
		for(API api : MasterWorkerAPI.API.values()){
			if(api.commandName().equals(name)){
				return api;
			}
		}
		return null;
	}
	
	public static boolean validatesCommand(MasterWorkerAPI.API apiCommand, Map<String, String> commandArgs){
		boolean validates = true;
		// argsReceived must contain ALL mandatory arguments
		if (! commandArgs.keySet().containsAll(apiCommand.c.getMandatoryArguments())){
			return false;
		}
		// argsReceive must not contain more arguments than defined
		if(commandArgs.size() > apiCommand.c.getTotalNumberArguments()){
			return false;
		}
		return validates;
	}
	
	//TODO: consider moving this to a util class, does not really belong here
	public static Map<String, String> arrayToMap(String[] commandArguments){
		Map<String, String> toReturn = new HashMap<>();
		for(int i = 1; i < commandArguments.length; i++){
			String[] keyValue = commandArguments[i].split(",");
			toReturn.put(keyValue[0], keyValue[1]);
		}
		return toReturn;
	}
}

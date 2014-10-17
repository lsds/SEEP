package uk.ac.imperial.lsds.seepmaster.comm;

public class MasterWorkerAPI {

	final public static String BOOTSTRAP = "bootstrap";
	final public static String CRASH = "crash";

	public static boolean validatesCommand(String command, String[] commandArgs){
		return false;
	}
}

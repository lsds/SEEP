package uk.ac.imperial.lsds.seepmaster;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.config.Config;
import uk.ac.imperial.lsds.seep.config.ConfigDef;
import uk.ac.imperial.lsds.seep.config.ConfigDef.Importance;
import uk.ac.imperial.lsds.seep.config.ConfigDef.Type;
import uk.ac.imperial.lsds.seep.config.ConfigKey;

public class MasterConfig extends Config {

	private static final ConfigDef config;
	
	public static final String QUERY_FILE = "query.file";
	private static final String QUERY_FILE_DOC = "The file where user queries are specified";
	
	public static final String BASECLASS_NAME = "baseclass.name";
	private static final String BASECLASS_NAME_DOC = "The name of the Base class where the query is composed";
	
	public static final String DEPLOYMENT_TARGET_TYPE = "deployment_target.type";
    private static final String DEPLOYMENT_TARGET_TYPE_DOC = "The target cluster to which the master will submit queries."
    													+ "Physical cluster(0), yarn container(1), lxc, docker, etc";
    public static final String LISTENING_PORT = "master.port";
    private static final String LISTENING_PORT_DOC = "The port in which master will receive commands from workers";
    
    public static final String UI_TYPE = "ui.type";
    private static final String UI_TYPE_DOC = "The type of UI chosen, simpleconsole(0), console(1), web(2), etc";
    
    public static final String PROPERTIES_FILE = "properties.file";
    private static final String PROPERTIES_FILE_DOC = "Optional argument to indicate a properties file";
	
	static{
		config = new ConfigDef().define(QUERY_FILE, Type.STRING, Importance.HIGH, QUERY_FILE_DOC)
				.define(BASECLASS_NAME, Type.STRING, Importance.HIGH, BASECLASS_NAME_DOC) 
				.define(DEPLOYMENT_TARGET_TYPE, Type.INT, 0, Importance.HIGH, DEPLOYMENT_TARGET_TYPE_DOC)
				.define(LISTENING_PORT, Type.INT, 3500, Importance.HIGH, LISTENING_PORT_DOC)
				.define(UI_TYPE, Type.INT, 0, Importance.HIGH, UI_TYPE_DOC)
				.define(PROPERTIES_FILE, Type.STRING, Importance.LOW, PROPERTIES_FILE_DOC);
	}
	
	public MasterConfig(Map<? extends Object, ? extends Object> originals) {
		super(config, originals);
	}
	
	public static ConfigKey getConfigKey(String name){
		return config.getConfigKey(name);
	}
	
	public static List<ConfigKey> getAllConfigKey(){
		return config.getAllConfigKey();
	}
	
	public static void main(String[] args) {
        System.out.println(config.toHtmlTable());
    }

}

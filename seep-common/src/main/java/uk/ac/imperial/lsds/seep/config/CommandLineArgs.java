package uk.ac.imperial.lsds.seep.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import uk.ac.imperial.lsds.seep.config.ConfigDef.Type;

public class CommandLineArgs {

	private OptionSet options;
	
	public CommandLineArgs(String[] args, OptionParser parser, List<ConfigKey> c){
		configureParser(parser, c);
		options = parser.parse(args);
	}
	
	private void configureParser(OptionParser parser, List<ConfigKey> c){
		for(ConfigKey key : c){
			String name = key.name;
			String doc = key.documentation;
			Type type = key.type;
			if(type == Type.BOOLEAN){
				parser.accepts(name, doc).withRequiredArg().ofType(Boolean.class).defaultsTo((boolean)key.defaultValue);
			}
			else if(type == Type.DOUBLE){
				parser.accepts(name, doc).withRequiredArg().ofType(Double.class).defaultsTo((double)key.defaultValue);
			}
			else if(type == Type.INT){
				parser.accepts(name, doc).withRequiredArg().ofType(Integer.class).defaultsTo((int)key.defaultValue);
			}
			else if(type == Type.LONG){
				parser.accepts(name, doc).withRequiredArg().ofType(Long.class).defaultsTo((long)key.defaultValue);
			}
			else if(type == Type.STRING){
				parser.accepts(name, doc).withRequiredArg().ofType(String.class).defaultsTo((String)key.defaultValue);
			}
		}
		parser.accepts("help").forHelp();
	}
	
	public Properties getProperties(){
		return CommandLineArgs.asProperties(options);
	}
	
	private static Properties asProperties(OptionSet options) {
        Properties properties = new Properties();
        for ( Entry<OptionSpec<?>, List<?>> entry : options.asMap().entrySet() ) {
            OptionSpec<?> spec = entry.getKey();
            String key = asPropertyKey(spec);
            String value = asPropertyValue(entry.getValue(), options.has(spec));
            System.out.println("key: "+key+" value: "+value);
            properties.setProperty(key, value);
        }
        return properties;
    }
	
	private static String asPropertyKey(OptionSpec<?> spec) {
        List<String> flags = (List<String>) spec.options();
        for ( String flag : flags )
            if ( 1 < flag.length() )
                return flag;
        throw new IllegalArgumentException( "No usable non-short flag: " + flags );
    }

    private static String asPropertyValue( List<?> values, boolean present ) {
        // Simple flags have no values; treat presence/absence as true/false
    	String value = "";
    	if(values.isEmpty()){
    		return String.valueOf(present);
    	}
    	else{
    		for(int i = 0; i < values.size(); i++){
    			if(i != 0){
    				value.concat(",");
    			}
    			value = value.concat(String.valueOf(values.get(i)));
    		}
    	}
    	return value;
    }
	
}

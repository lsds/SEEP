package uk.ac.imperial.lsds.seep.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.util.Utils;

public class Schema {
	
	private final byte schemaId;
	private final Type[] fields;
	private final String[] names;
	private Map<String, Integer> mapping = new HashMap<String, Integer>();
	
	private Schema(byte schemaId, Type[] fields, String[] names){
		this.schemaId = schemaId;
		this.fields = fields;
		this.names = names;
		for(int i = 0; i < names.length; i++){
			mapping.put(names[i], 0);
		}
	}
	
	public byte schemaId(){
		return schemaId;
	}
	
	public Type[] fields(){
		return fields;
	}
	
	public String[] names(){
		return names;
	}
	
	public Type getField(String name){
		if(!mapping.containsKey(name)){
			System.out.println("ERROR");
			System.exit(0);
		}
		return fields[mapping.get(name)];
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("SchemaID: "+schemaId);
		sb.append(Utils.NL);
		for(int i = 0; i < names.length; i++){
			sb.append("Field: "+names[i]+" : "+fields[i].toString());
			sb.append(Utils.NL);
		}
		return sb.toString();
	}
	
	public static class SchemaBuilder{
		
		// Only one instance is safe as schemaId is handled internally automatically
		private static SchemaBuilder instance = null;
		
		private byte schemaId;
		private List<Type> fields = new ArrayList<Type>();
		private List<String> names = new ArrayList<String>();
		
		private SchemaBuilder(){}
		
		public static SchemaBuilder getInstance(){
			if(instance == null){
				instance = new SchemaBuilder();
			}
			return instance;
		}
		
		public SchemaBuilder newField(Type type, String name){
			// safety checks
			if(names.contains(name)){
				// TODO: throw error
			}
			this.fields.add(type);
			this.names.add(name);
			return this;
		}
		
		public Schema build(){
			// Sanity check
			if(! (fields.size() == names.size())){
				// TODO: throw error
			}
			Type[] f = new Type[fields.size()];
			f = fields.toArray(f);
			String[] s = new String[names.size()];
			s = names.toArray(s);
			Schema toReturn = new Schema(schemaId, f, s);
			schemaId++; // always increasing schemaId to ensure unique id
			this.fields.clear();
			this.names.clear();
			return toReturn; 
		}
	}
	
}

package uk.ac.imperial.lsds.streamsql.op.gpu.deprecated;

import java.lang.annotation.Annotation;

import java.util.List;
import java.util.ArrayList;

public class Kernel {
	
	private String name;
	private String source;
	
	private List<KernelAttribute> attributes;
	
	public Kernel (String name, String source) {
		this.name = name;
		this.source = source;
		attributes = new ArrayList<KernelAttribute>();
	}
	
	public void setName (String name) {
		this.name = name;
	}
	
	public void setSource (String source) { 
		this.source = source; 
	}
	
	public void addAttribute(KernelAttribute attribute) {
		attributes.add(attribute);
	}
	
	public Annotation [][] getAnnotations () {
		Annotation [][] result = null;
		int length = 0;
		if (attributes.isEmpty())
			return result;
		length = attributes.size();
		result = new Annotation [length][];
		for (int i = 0; i < length; i++)
			result[i] = attributes.get(i).getAnnotations();
		return result;
	}
	
	public Class<?> [] getParameterTypes () {
		List<Class<?>> types = null;
		int length = 0;
		if (attributes.isEmpty())
			return null;
		types = new ArrayList<Class<?>>();
		length = attributes.size();
		for (KernelAttribute attribute: attributes)
			types.add(attribute.getClassType());
		return types.toArray(new Class<?>[length]);
	}
	
	public String getSource () {
		return source;
	}
	
	public String getName () {
		return name;
	}
}

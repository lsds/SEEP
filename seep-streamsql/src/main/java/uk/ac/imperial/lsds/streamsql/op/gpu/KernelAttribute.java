package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.lang.annotation.Annotation;

import java.util.List;
import java.util.ArrayList;

public class KernelAttribute {
	
	private String name;
	private Class<?> type;
	private List<Annotation> annotations;
	
	public KernelAttribute (String name, Class<?> type) {
		this.name = name;
		this.type = type;
		annotations = new ArrayList<Annotation>();
	}
	
	public void addAnnotation (Annotation annotation) {
		annotations.add(annotation);
	}
	
	public Annotation [] getAnnotations () {
		return annotations.toArray(new Annotation [annotations.size()]);
	}
	
	public String getName () { 
		return name; 
	}
	
	public Class<?> getClassType () { 
		return type; 
	}
}

package uk.ac.imperial.lsds.streamsql.op.gpu.annotations;

import com.amd.aparapi.opencl.OpenCL.GlobalWriteOnly;

import java.lang.annotation.Annotation;

public class GlobalWriteOnlyArgument implements Annotation, GlobalWriteOnly {
	
	private String name;
	
	public GlobalWriteOnlyArgument (String name) {
		this.name = name;	
	}
	
	public String value() { 
		return name; 
	}
	
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}
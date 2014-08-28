package uk.ac.imperial.lsds.streamsql.op.gpu.annotations;

import com.amd.aparapi.opencl.OpenCL.Arg;

import java.lang.annotation.Annotation;

public class GPUArg implements Annotation, Arg {
	
	private String name;
	
	public GPUArg (String name) {
		this.name = name;	
	}
	
	public String value() { return name; }
	
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}

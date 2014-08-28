package uk.ac.imperial.lsds.streamsql.op.gpu.annotations;

import com.amd.aparapi.opencl.OpenCL.GlobalReadOnly;

import java.lang.annotation.Annotation;

public class GPUGlobalReadOnly implements Annotation, GlobalReadOnly {
	
	private String name;
	
	public GPUGlobalReadOnly (String name) {
		this.name = name;	
	}
	
	public String value() { return name; }
	
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}

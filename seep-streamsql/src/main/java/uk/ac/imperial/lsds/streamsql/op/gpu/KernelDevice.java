package uk.ac.imperial.lsds.streamsql.op.gpu;

import com.amd.aparapi.device.OpenCLDevice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amd.aparapi.internal.opencl.OpenCLArgDescriptor;
import com.amd.aparapi.internal.opencl.OpenCLProgram;
import com.amd.aparapi.internal.opencl.OpenCLKernel;

import com.amd.aparapi.opencl.OpenCL;
import com.amd.aparapi.opencl.OpenCL.Arg;
import com.amd.aparapi.opencl.OpenCL.Local;
import com.amd.aparapi.opencl.OpenCL.Constant;
import com.amd.aparapi.opencl.OpenCL.GlobalReadOnly;
import com.amd.aparapi.opencl.OpenCL.GlobalReadWrite;
import com.amd.aparapi.opencl.OpenCL.GlobalWriteOnly;

public class KernelDevice {
	
	private OpenCLDevice device;
	
	public KernelDevice (OpenCLDevice device) {
		this.device = device;
	}
	
	public OpenCLDevice getDevice () { 
		return device;
	}
	
	@Override
	public String toString() {
		return device.toString();
	}
	
	public List<OpenCLArgDescriptor> parseArgs (Kernel kernel) {
		
		List<OpenCLArgDescriptor> args = new ArrayList<OpenCLArgDescriptor>();
		Annotation [][] annotations = kernel.getAnnotations();
		Class<?>[] types = kernel.getParameterTypes();
		
		long bits;
		int argc;
		String name;
		
		for (argc = 0; argc < types.length; argc++) {
			
			bits = 0L;
			name = null;
			
			for (Annotation a: annotations[argc]) {
				
				if (a instanceof GlobalReadOnly) {
					
					name = ((GlobalReadOnly) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_GLOBAL_BIT;
					bits |= OpenCLArgDescriptor.ARG_READONLY_BIT;
					
				} else
				if (a instanceof GlobalWriteOnly) {
					
					name = ((GlobalWriteOnly) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_GLOBAL_BIT;
					bits |= OpenCLArgDescriptor.ARG_WRITEONLY_BIT;
					
				} else
				if (a instanceof GlobalReadWrite) {
					
					name = ((GlobalReadWrite) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_GLOBAL_BIT;
					bits |= OpenCLArgDescriptor.ARG_READWRITE_BIT;
					
				} else
				if (a instanceof Local) {
					
					name = ((Local) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_LOCAL_BIT;
					
				} else
				if (a instanceof Constant) {
					
					name = ((Constant) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_CONST_BIT;
					bits |= OpenCLArgDescriptor.ARG_READONLY_BIT;
					
				} else
				if (a instanceof Arg) {
					
					name = ((Arg) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_ISARG_BIT;
				}
			}
			
			if (types[argc].isArray()) {
				
				if (types[argc].isAssignableFrom( float [].class)) { bits |= OpenCLArgDescriptor.ARG_FLOAT_BIT;  } else
				if (types[argc].isAssignableFrom(   int [].class)) { bits |= OpenCLArgDescriptor.ARG_INT_BIT;    } else
				if (types[argc].isAssignableFrom(double [].class)) { bits |= OpenCLArgDescriptor.ARG_DOUBLE_BIT; } else
				if (types[argc].isAssignableFrom(  byte [].class)) { bits |= OpenCLArgDescriptor.ARG_BYTE_BIT;   } else
				if (types[argc].isAssignableFrom( short [].class)) { bits |= OpenCLArgDescriptor.ARG_SHORT_BIT;  } else
				if (types[argc].isAssignableFrom(  long [].class)) { bits |= OpenCLArgDescriptor.ARG_LONG_BIT;   }
				
				bits |= OpenCLArgDescriptor.ARG_ARRAY_BIT;
				
			} else 
			if (types[argc].isPrimitive()) {
				
				if (types[argc].isAssignableFrom( float.class)) { bits |= OpenCLArgDescriptor.ARG_FLOAT_BIT;  } else
				if (types[argc].isAssignableFrom(   int.class)) { bits |= OpenCLArgDescriptor.ARG_INT_BIT;    } else
				if (types[argc].isAssignableFrom(double.class)) { bits |= OpenCLArgDescriptor.ARG_DOUBLE_BIT; } else
				if (types[argc].isAssignableFrom(  byte.class)) { bits |= OpenCLArgDescriptor.ARG_BYTE_BIT;   } else
				if (types[argc].isAssignableFrom( short.class)) { bits |= OpenCLArgDescriptor.ARG_SHORT_BIT;  } else
				if (types[argc].isAssignableFrom(  long.class)) { bits |= OpenCLArgDescriptor.ARG_LONG_BIT;   }
				
				bits |= OpenCLArgDescriptor.ARG_PRIMITIVE_BIT;
				
			} else {
				/* Type is neither primitive or array */
				throw new IllegalStateException("error: kernel argument is neither primitive nor array");
			}
			
			if (name == null)
				throw new IllegalStateException("error: kernel argument name is null");
			
			OpenCLArgDescriptor kernelArg = new OpenCLArgDescriptor(name, bits);
			args.add(kernelArg);
		}
		return (args);
	}
	
	public <T extends OpenCL<T>> T bind (Class<T> iface, List<Kernel> kernels) {
		
		Map<String, List<OpenCLArgDescriptor>> kernelArgs = new HashMap<String, List<OpenCLArgDescriptor>>();
		String source = "";
		for (Kernel k: kernels) {
			/* Populate source */
			source += k.getSource();
			source += "\n";
			/* Populate kernelArgs */
			kernelArgs.put(k.getName(), parseArgs(k));
		}
		
		OpenCLProgram program = new OpenCLProgram(device, source).createProgram(device);
		
		Map<String, OpenCLKernel> map = new HashMap<String, OpenCLKernel>();
		for (String name: kernelArgs.keySet()) {
			OpenCLKernel kernel = OpenCLKernel.createKernel(program, name, kernelArgs.get(name));
			if (kernel == null)
				throw new IllegalStateException(String.format("error: kernel %s is null", name));
			map.put(name, kernel);
		}
		
		KernelInvocationHandler<T> handler = new KernelInvocationHandler<T> (program, map);
		@SuppressWarnings("unchecked")
		T instance = (T) Proxy.newProxyInstance (
			OpenCLDevice.class.getClassLoader(),
			new Class [] {
				iface,
				OpenCL.class
			},
			handler
		);
		return instance;
	}
}

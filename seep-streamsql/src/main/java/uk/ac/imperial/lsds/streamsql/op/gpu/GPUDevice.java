package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.lang.IllegalArgumentException;

import com.amd.aparapi.ProfileInfo;

import com.amd.aparapi.device.Device;
import com.amd.aparapi.device.OpenCLDevice;

import java.lang.annotation.Annotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amd.aparapi.Range;

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

public class GPUDevice {
	
	private OpenCLDevice dev;
	
	public GPUDevice (OpenCLDevice dev) {
		this.dev = dev;
	}
	
	public OpenCLDevice getDevice () { return dev; }
	
	public String toString() { return dev.toString(); }
	
	public List<OpenCLArgDescriptor> parseArgs (GPUKernel kernel) {
		
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
					System.out.println(String.format("[DBG] %s is GlobalReadOnly", name));
					
				} else 
				if (a instanceof GlobalWriteOnly) {
					
					name = ((GlobalWriteOnly) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_GLOBAL_BIT;
					bits |= OpenCLArgDescriptor.ARG_WRITEONLY_BIT;
					System.out.println(String.format("[DBG] %s is GlobalWriteOnly", name));
				
				} else 
				if (a instanceof GlobalReadWrite) {
					
					name = ((GlobalReadWrite) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_GLOBAL_BIT;
					bits |= OpenCLArgDescriptor.ARG_READWRITE_BIT;
					System.out.println(String.format("[DBG] %s is GlobalReadWrite", name));
				
				} else 
				if (a instanceof Local) {
					
					name = ((Local) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_LOCAL_BIT;
					System.out.println(String.format("[DBG] %s is Local", name));
				
				} else 
				if (a instanceof Constant) {
					
					name = ((Constant) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_CONST_BIT;
					bits |= OpenCLArgDescriptor.ARG_READONLY_BIT;
					System.out.println(String.format("[DBG] %s is Constant", name));
				
				} else 
				if (a instanceof Arg) {
					
					name = ((Arg) a).value();
					
					bits |= OpenCLArgDescriptor.ARG_ISARG_BIT;
					System.out.println(String.format("[DBG] %s is Arg", name));
				}
			}
			
			if (types[argc].isArray()) {
				System.out.println(String.format("[DBG] %s is array", name));
				
				if (types[argc].isAssignableFrom( float [].class)) { bits |= OpenCLArgDescriptor.ARG_FLOAT_BIT;  } else 
				if (types[argc].isAssignableFrom(   int [].class)) { bits |= OpenCLArgDescriptor.ARG_INT_BIT;    } else 
				if (types[argc].isAssignableFrom(double [].class)) { bits |= OpenCLArgDescriptor.ARG_DOUBLE_BIT; } else 
				if (types[argc].isAssignableFrom(  byte [].class)) { bits |= OpenCLArgDescriptor.ARG_BYTE_BIT;   } else 
				if (types[argc].isAssignableFrom( short [].class)) { bits |= OpenCLArgDescriptor.ARG_SHORT_BIT;  } else 
				if (types[argc].isAssignableFrom(  long [].class)) { bits |= OpenCLArgDescriptor.ARG_LONG_BIT;   }
				
				bits |= OpenCLArgDescriptor.ARG_ARRAY_BIT;
			
			} else if (types[argc].isPrimitive()) {
				
				System.out.println(String.format("[DBG] %s is primitive", name));
				if (types[argc].isAssignableFrom( float.class)) { bits |= OpenCLArgDescriptor.ARG_FLOAT_BIT;  } else 
				if (types[argc].isAssignableFrom(   int.class)) { bits |= OpenCLArgDescriptor.ARG_INT_BIT;    } else 
				if (types[argc].isAssignableFrom(double.class)) { bits |= OpenCLArgDescriptor.ARG_DOUBLE_BIT; } else 
				if (types[argc].isAssignableFrom(  byte.class)) { bits |= OpenCLArgDescriptor.ARG_BYTE_BIT;   } else 
				if (types[argc].isAssignableFrom( short.class)) { bits |= OpenCLArgDescriptor.ARG_SHORT_BIT;  } else 
				if (types[argc].isAssignableFrom(  long.class)) { bits |= OpenCLArgDescriptor.ARG_LONG_BIT;   }
				
				bits |= OpenCLArgDescriptor.ARG_PRIMITIVE_BIT;
			
			} else {
				/* Type is neigher primitive or array */
				throw new IllegalStateException("Argument is neither primitive or array.");
			}
            
			if (name == null)
				throw new IllegalStateException("Argument name is null.");
			
			OpenCLArgDescriptor kernelArg = new OpenCLArgDescriptor(name, bits);
			args.add(kernelArg);
		}
		
		return (args);
	}
	
	public <T extends OpenCL<T>> T bind (Class<T> iface, List<GPUKernel> kernels) {
		
		Map<String, List<OpenCLArgDescriptor>> kernelArgs = 
			new HashMap<String, List<OpenCLArgDescriptor>>();
		
		String source = "";
		for (GPUKernel k: kernels) {
			/* Populate source */
			source += k.getSource();
			source += "\n";
			/* Populate kernelArgs */
			kernelArgs.put(k.getName(), parseArgs(k));
		}
		
		OpenCLProgram program = new OpenCLProgram(dev, source).createProgram(dev);
		
		Map<String, OpenCLKernel> map = new HashMap<String, OpenCLKernel>();
		for (String name: kernelArgs.keySet()) {
			
			OpenCLKernel kernel = OpenCLKernel.createKernel(program, name, kernelArgs.get(name));
			if (kernel == null)
				throw new IllegalStateException(String.format("Kernel %s is null.", name));
			map.put(name, kernel);
		}
		
		GPUInvocationHandler<T> handler = new GPUInvocationHandler<T> (program, map);
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


package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.lang.IllegalArgumentException;

import com.amd.aparapi.ProfileInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.Map;

import com.amd.aparapi.internal.opencl.OpenCLKernel;
import com.amd.aparapi.internal.opencl.OpenCLProgram;

import com.amd.aparapi.opencl.OpenCL;

public class GPUInvocationHandler<T extends OpenCL<T>>
	implements InvocationHandler {
		
		private final Map<String, OpenCLKernel> map;
		private final OpenCLProgram program;
		private boolean disposed = false;
		
		private static boolean isReserved (Method method) {
		String name = method.getName();
		return (   
		name.equals(           "put") || 
		name.equals(           "get") || 
		name.equals(           "end") || 
		name.equals(         "begin") || 
		name.equals(       "dispose") || 
		name.equals("getProfileInfo") );
	}
		
		public GPUInvocationHandler
		(OpenCLProgram program, Map<String, OpenCLKernel> map) {
			
			this.program = program;
			this.map = map;
			this.disposed = false;
		}
		
		public void test (String methodName) {
			System.out.println(methodName);
		}
		
		public void call (String methodName, Object [] args) {
			OpenCLKernel kernel;
			kernel = map.get(methodName);
			if (kernel == null)
				throw new IllegalArgumentException
					(String.format("Method %s does not exist.", methodName));
			kernel.invoke(args);
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object [] args) 
		throws Throwable {
			
			String methodName;
			OpenCLKernel k = null;
			
			if (disposed)
				throw new IllegalStateException("Interface already disposed.");
			
			methodName = method.getName();
			if (isReserved(method)) {
				
				if (methodName.equals(    "put")) { System.out.println("[]"); } else
				if (methodName.equals(    "get")) { System.out.println("[]"); } else
				if (methodName.equals(    "end")) { System.out.println("[]"); } else
				if (methodName.equals(  "begin")) { System.out.println("[]"); } else
				if (methodName.equals("dispose")) {
					
					for (OpenCLKernel kernel: map.values()) 
						kernel.dispose();
					program.dispose();
					map.clear();
					disposed = true;
					
				} else if (methodName.equals("getProfileInfo")) {
					
					proxy = (Object) program.getProfileInfo();
				}
			} else { /* Method is not reserved */
				k = map.get(methodName);
				if (k == null)
					throw new IllegalArgumentException
						(String.format("Method %s does not exist.", methodName));
				k.invoke(args);
			}
			
			return proxy;
		}
	}

package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.amd.aparapi.internal.opencl.OpenCLKernel;
import com.amd.aparapi.internal.opencl.OpenCLProgram;
import com.amd.aparapi.opencl.OpenCL;

public class KernelInvocationHandler <T extends OpenCL<T>> 
	implements InvocationHandler {
	
	private final Map<String, OpenCLKernel> map;
	private final OpenCLProgram program;
	private boolean disposed = false;
	
	OpenCLKernel projectKernel = null;
	
	public KernelInvocationHandler (OpenCLProgram program, Map<String, OpenCLKernel> map) {
		this.program = program;
		this.map = map;
		this.disposed = false;
		
		/* this.projectKernel = map.get("project"); */
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object [] args)
		throws Throwable {
		String name; /* Method name */
		OpenCLKernel k = null;
		if (disposed)
			throw new IllegalStateException("error: OpenCL interface already disposed");
		name = method.getName();
		if (isReserved (method)) {
			if (name.equals("dispose")) {
				for (OpenCLKernel kernel: map.values())
					kernel.dispose();
				program.dispose();
				map.clear();
				disposed = true;
			} else 
			if (name.equals("getProfileInfo")) {
				proxy = program.getProfileInfo();
			} else {
				throw new IllegalArgumentException
				(String.format("error: unsupported method %s", name));
			}
		} else { /* Method is not reserved */
			k = map.get(name);
			if (k == null)
				throw new IllegalArgumentException
				(String.format("error: method %s does not exist.", name));
			k.invoke(args);
		}
		return proxy;
	}
	
	private boolean isReserved (Method method) {
		String name = method.getName();
		return (name.equals("dispose") || name.equals("getProfileInfo"));
	}
	
	public void call (String name, Object [] args) {
		OpenCLKernel kernel = map.get(name);
		if (kernel == null)
		 	throw new IllegalArgumentException(String.format("error: method %s does not exist", name));
		kernel.invoke(args);
    }
	
}

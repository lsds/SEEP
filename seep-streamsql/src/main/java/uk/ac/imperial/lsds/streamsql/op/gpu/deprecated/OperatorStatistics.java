package uk.ac.imperial.lsds.streamsql.op.gpu.deprecated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amd.aparapi.ProfileInfo;

public class OperatorStatistics {
	
	Map<String, KernelStatistics> kernels;
	
	public OperatorStatistics () {
		kernels = new HashMap<String, KernelStatistics> ();
	}
	
	public void add (KernelStatistics kernel) {
		kernels.put(kernel.getName(), kernel);
	}
	
	public void add (String key, int inputSize, int outputSize) {
		KernelStatistics kernel = new KernelStatistics (key, inputSize, outputSize);
		kernels.put(key, kernel);
	}
	
	public void collect (String key, List<ProfileInfo> info) {
		kernels.get(key).collect(info);
	}
	
	public void collect (String key, List<ProfileInfo> info, int inputSize, int outputSize) {
		kernels.get(key).collect(info, inputSize, outputSize);
	}
	
	public void print () {
		for (KernelStatistics kernel: kernels.values())
			kernel.print();
	}
}

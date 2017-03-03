//package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class PBTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			try
			{
				String iconName = "master.png";
				//String nodeId = InetAddress.getLocalHost().getHostName();
				String nodeId = "n2";

				//String iconsDir = GLOBALS.valueFor("iconsDir");
				String iconsDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane/vldb/config";
				String iconPath = iconsDir + "/" + iconName;
				int nodeNumber = nodeId2NodeNumber(nodeId);
				
				System.out.println("Setting node icon for nodeId="+nodeId+", nodeNum="+nodeNumber+" to "+iconPath);
				
				String cmd = "coresendmsg --address 172.16.0.254 node number="+nodeNumber+" icon="+ iconPath;
				ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "'" + cmd + "'");
				Process process = pb.start();

				readResult(process);
				
				cmd = "--address 172.16.0.254 node number="+nodeNumber+" icon="+ iconPath;
				pb = new ProcessBuilder("/usr/sbin/coresendmsg", "--address 172.16.0.254 node number="+nodeNumber+" icon="+ iconPath);
				process = pb.start();
				
				readResult(process);
				
				pb = new ProcessBuilder("/usr/bin/python", "/usr/sbin/coresendmsg", "--address", "172.16.0.254", "node", "number="+nodeNumber, "icon="+iconPath);
				process = pb.start();
				readResult(process);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}
	
	private static int nodeId2NodeNumber(String nodeId)
	{
		return Integer.parseInt(nodeId.substring(1));
	}
	
	private static void readResult(Process process) throws Exception
	{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		String line = "";
		while ((line = reader.readLine()) != null)
		{
			System.out.println(line);
		}
		
        int exitCode = process.waitFor();
        System.out.println("Exit Code : "+exitCode);
	}
}

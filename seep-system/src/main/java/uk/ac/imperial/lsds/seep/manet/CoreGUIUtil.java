package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;

public class CoreGUIUtil {
	private final static Logger logger = LoggerFactory.getLogger(CoreGUIUtil.class);

	
	public static void setSourceIcon()
	{
		setNodeIcon("source.png");
	}
	
	public static void setSinkIcon()
	{
		setNodeIcon("sink.png");
	}
	
	public static void setOpIcon()
	{
		setNodeIcon("op.png");
	}
	
	public static void setMasterIcon()
	{
		setNodeIcon("master.png");
	}
	
	private static void setNodeIcon(String iconName)
	{
		try
		{
			String nodeId = InetAddress.getLocalHost().getHostName();

			String iconsDir = GLOBALS.valueFor("repoDir")+"/seep-system/examples/acita_demo_2015/core-emane/vldb/config";
			String iconPath = iconsDir + "/" + iconName;
			int nodeNumber = nodeId2NodeNumber(nodeId);
			
			logger.info("Setting node icon for nodeId="+nodeId+", nodeNum="+nodeNumber+" to "+iconPath);
			
			//String cmd = "'coresendmsg --address 172.16.0.254 node number="+nodeNumber+" icon="+ iconPath + "'";
			ProcessBuilder pb = new ProcessBuilder("/usr/bin/python", "/usr/sbin/coresendmsg", "--address", "172.16.0.254", "node", "number="+nodeNumber, "icon="+iconPath);
			//ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", cmd);
			Process process = pb.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				logger.info(line);
			}
	        int exitCode = process.waitFor();
	        logger.info("Exit Code : "+exitCode);
	        
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static int nodeId2NodeNumber(String nodeId)
	{
		return Integer.parseInt(nodeId.substring(1));
	}
}

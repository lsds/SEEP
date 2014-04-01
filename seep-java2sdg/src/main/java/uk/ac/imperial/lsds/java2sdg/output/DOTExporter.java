package uk.ac.imperial.lsds.java2sdg.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.OperatorBlock;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.Stream;

public class DOTExporter implements SDGExporter{

	private static DOTExporter instance = null;
	
	private DOTExporter(){
		
	}
	
	public static DOTExporter getInstance(){
		if(instance == null){
			instance = new DOTExporter();
		}
		return instance;
	}
	
	@Override
	public void export(List<OperatorBlock> sdg, String filename) {
		// first write in memory the file content
		List<String> output = new ArrayList<String>();
		output.add("digraph G {\n");
		for(OperatorBlock ob : sdg){
			// Check stateful to paint it differently
			if(ob.getStateId() != -1){
				String stateName = ob.getTE().getOpType().getStateName();
				output.add(""+stateName+" [shape=triangle,color=red,style=bold];\n");
				output.add(stateName+" -> "+ob.getId()+";\n");
				output.add(""+ob.getId()+" [color=green,style=filled];\n");
			}
			// Check downstream to connect it appropiately
			if(ob.getDownstreamSize() > 0){
				for(Stream downstream : ob.getDownstreamOperator()){
					String me = ""+ob.getId()+"";
					String down = ""+downstream.getId()+"";
					output.add(me+" -> "+down+";\n");
				}
			}
			else{
				String me = ""+ob.getId()+"";
				String down = "sink";
				output.add(me+" -> "+down+";\n");
			}
			// Use a different shape for merge ops
			for(TaskElement te : ob.getTEs()){
				if(te.getAnn() != null && te.getAnn().equals(SDGAnnotation.COLLECTION)){
					output.add(""+ob.getId()+" [shape=polygon,sides=5];\n");
				}
			}
		}
		output.add("}\n");
		// then write to file
		this.writeToFile(output, filename);
	}
	
	private void writeToFile(List<String> output, String filename){
		filename = filename+".dot";
		File f = new File(filename);
		BufferedWriter fw = null;
		try {
			fw = new BufferedWriter(new FileWriter(f));
			for(String line : output){
				fw.write(line);
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				fw.flush();
				fw.close();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

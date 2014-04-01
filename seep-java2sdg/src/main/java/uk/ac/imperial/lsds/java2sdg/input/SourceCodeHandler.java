package uk.ac.imperial.lsds.java2sdg.input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import uk.ac.imperial.lsds.java2sdg.Main;
import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;


public class SourceCodeHandler {
	
	private final static Logger log = Logger.getLogger(Main.class.getCanonicalName());
	
	private List<String> lines;
	private Map<Integer, SDGAnnotation> line_sdgAnnotation = new HashMap<Integer, SDGAnnotation>();
	private static final SourceCodeHandler i = null;
	
	public static SourceCodeHandler getInstance(String path){
		if (i == null){
			try {
				return new SourceCodeHandler().loadSourceCode(path);
			} 
			catch (IllegalDriverProgram e) {
				log.severe("Invalid Program: "+e.getMessage());
				System.exit(1);
			}
		}
		else{
			return i;
		}
		return null;
	}
	
	//FIXME: only for debugging
	public void printLineAnnotation(){
		for(Map.Entry<Integer, SDGAnnotation> entry : line_sdgAnnotation.entrySet()){
			System.out.println("Line: "+entry.getKey()+" Value: "+entry.getValue().toString());
		}
	}
	
	public void printCode(){
		
	}
	
	public SDGAnnotation getSDGAnnotationAtLine(int line){
		return line_sdgAnnotation.get(line);
	}
	
	private SourceCodeHandler(){	
	}
	
	public List<String> getChunkOfCode(int firstLine, int lastLine){
		return lines.subList(firstLine, lastLine);
	}
	
	public String getLineAt(int lineNumber){
		return lines.get(lineNumber);
	}
	
	private SourceCodeHandler loadSourceCode(String path) throws IllegalDriverProgram{
		lines = new ArrayList<String>();
		lines.add(null); // position 0 does not correspond to any source code line
		File f = new File(path);
		File out = new File(f.getName());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f+".java"));
			BufferedWriter writer = new BufferedWriter(new FileWriter(out+".java"));
			String line = null;
			int lineNumber = 1;
			while((line = reader.readLine()) != null){
				if(line.contains("@Partitioned")){
					line_sdgAnnotation.put(lineNumber, SDGAnnotation.PARTITIONED);
				}
				else if(line.contains("@PartialState")){
					line_sdgAnnotation.put(lineNumber, SDGAnnotation.PARTIAL_STATE);
				}
				else if(line.contains("@PartialData")){
					if(!line.contains("@Global")){
						throw new IllegalDriverProgram("You need @Global access to @PartialState to get @PartialData. Missing @Global?");
					}
					else{
						line = line.replace("@Global", "");
						line = line.replace("@PartialData", "");
						line_sdgAnnotation.put(lineNumber, SDGAnnotation.GLOBAL_READ);
					}
				}
				else if(line.contains("@Global")){
					if(!line.contains("@PartialData")){
						line = line.replace("@Global", "");
						line_sdgAnnotation.put(lineNumber, SDGAnnotation.GLOBAL_WRITE);
					}
				}
				else if(line.contains("@Collection")){
					line = line.replace("@Collection", "");
					line_sdgAnnotation.put(lineNumber, SDGAnnotation.COLLECTION);
				}
				// Note that this assumes that all merge functions will be named merge (engineering shortcut)
				else if(line.contains("merge")){
					line_sdgAnnotation.put(lineNumber, SDGAnnotation.COLLECTION);
				}
				lines.add(line);
				writer.write(line+"\n");
				lineNumber++;
			}
			reader.close();
			writer.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}
}

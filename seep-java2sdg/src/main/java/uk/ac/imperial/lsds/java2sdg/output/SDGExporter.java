package uk.ac.imperial.lsds.java2sdg.output;

import java.util.List;

import uk.ac.imperial.lsds.java2sdg.bricks.SDG.OperatorBlock;

public interface SDGExporter {
	public void export(List<OperatorBlock> sdg, String filename);
}
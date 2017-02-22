/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.acita15.heatmap;

import java.io.Serializable;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.HashSet;

public class HeatMap implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(HeatMap.class);
	private final double errorMargin = 5.0;
	private final double tileWidth;
	private final double tileHeight;
	private final int xTiles;
	private final int yTiles;
	private int[][] posCounts;
	private final Set<Integer> sourceIds = new HashSet<>();

	public HeatMap(double maxX, double maxY, int xTiles, int yTiles, int sourceId)
	{
		this.xTiles = xTiles;
		this.yTiles = yTiles;
		this.tileWidth = maxX / xTiles;
		this.tileHeight = maxY / yTiles;
		this.posCounts = new int[xTiles][yTiles];
		this.sourceIds.add(sourceId);
	}

	public HeatMap(String serialized)
	{
		String[] parts = serialized.split(";");
		String[] metadata = parts[0].split(",");
		this.tileWidth = Double.parseDouble(metadata[0]);
		this.tileHeight = Double.parseDouble(metadata[1]);
		this.xTiles = Integer.parseInt(metadata[2]);
		this.yTiles = Integer.parseInt(metadata[3]);
		
		
		for (int m = 4; m < metadata.length; m++)
		{
			this.sourceIds.add(Integer.parseInt(metadata[m]));
		}
		
		this.posCounts = new int[xTiles][yTiles];

		for (int pos = 1; pos < parts.length; pos++)
		{
			String[] posCount = parts[pos].split(",");
			if (posCount.length != 3) { throw new RuntimeException("Logic error, invalid pos."); }
			int x = Integer.parseInt(posCount[0]);
			int y = Integer.parseInt(posCount[1]);
			int count = Integer.parseInt(posCount[2]);
			posCounts[x][y] = count;
		}
	}

	public int[][] getPosCounts() { return posCounts; }
	public Set<Integer> getSourceIds() { return Collections.unmodifiableSet(sourceIds); }
	public void reset() { posCounts = new int[xTiles][yTiles]; }

	public String toString()
	{
		String metaData = ""+tileWidth+","+tileHeight+","+xTiles+","+yTiles;

		String occupiedTiles = "";

		for (int x = 0; x < xTiles; x++)
		{
			for (int y = 0; y < yTiles; y++)
			{
				if (posCounts[x][y] > 0)
				{
					String tileCount = "" + x + "," + y + "," + posCounts[x][y];

					if (!occupiedTiles.isEmpty()) { occupiedTiles += ";"; }
					occupiedTiles += tileCount;
				}
			}
		}

		String result = metaData;
		
		for (Integer sourceId : sourceIds)
		{
			result += ","+sourceId;
		}
		
		if (!occupiedTiles.isEmpty())
		{
			result += ";" + occupiedTiles;
		}
		return result;
	}


	public void updatePos(Location currentLoc)
	{
		if (currentLoc.getX() > (xTiles * tileWidth) + errorMargin) 
		{ 
			logger.error("Logic error: current="+currentLoc.getX()+",xTiles="+xTiles+",tw="+tileWidth); 
		}
		if (currentLoc.getY() > (yTiles * tileHeight) + errorMargin) 
		{ 
			logger.error("Logic error: current="+currentLoc.getY()+",yTiles="+yTiles+",th="+tileHeight); 
		}

		//TODO: Fix cast
		int xTile = (int) (currentLoc.getX() / tileWidth);
		int yTile = (int) (currentLoc.getY() / tileHeight);
		//In case on the border.
		if (xTile >= xTiles) { xTile = xTiles-1; }
		if (yTile >= yTiles) { yTile = yTiles-1; }

		posCounts[xTile][yTile]++;
	}

	public void add(HeatMap other)
	{
		if (other.tileWidth != tileWidth || other.tileHeight != tileHeight 
				|| other.xTiles != xTiles || other.yTiles != yTiles)
		{
			throw new RuntimeException("Heat map mismatch.");
		}

		for (int x = 0; x < xTiles; x++)
		{
			for (int y = 0; y < yTiles; y++)
			{
				posCounts[x][y] += other.posCounts[x][y];
			}
		}
		
		sourceIds.addAll(other.sourceIds);
	}
}

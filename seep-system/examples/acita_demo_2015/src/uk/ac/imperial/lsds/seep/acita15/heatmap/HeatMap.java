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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeatMap implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(HeatMap.class);
	private final int tileWidth;
	private final int tileHeight;
	private final int xTiles;
	private final int yTiles;
	private int[][] posCounts;

	public HeatMap(int tileWidth, int tileHeight, int xTiles, int yTiles)
	{
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.xTiles = xTiles;
		this.yTiles = yTiles;
		this.posCounts = new int[xTiles][yTiles];
	}

	public HeatMap(String serialized)
	{
		String[] parts = serialized.split(";");
		String[] metadata = parts[0].split(",");
		this.tileWidth = Integer.parseInt(metadata[0]);
		this.tileHeight = Integer.parseInt(metadata[1]);
		this.xTiles = Integer.parseInt(metadata[2]);
		this.yTiles = Integer.parseInt(metadata[3]);
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
		if (!occupiedTiles.isEmpty())
		{
			result += ";" + occupiedTiles;
		}
		return result;
	}


	public void updatePos(Location currentLoc)
	{
		if (currentLoc.getX() > xTiles * tileWidth) { throw new RuntimeException("Logic error: current="+currentLoc.getX()+",xTiles="+xTiles+",tw="+tileWidth); }
		if (currentLoc.getY() > yTiles * tileHeight) { throw new RuntimeException("Logic error: current="+currentLoc.getY()+",yTiles="+yTiles+",th="+tileHeight); }

		int xTile = currentLoc.getX() / tileWidth;
		int yTile = currentLoc.getY() / tileHeight;
		//In case on the border.
		if (xTile == xTiles) { xTile--; }
		if (yTile == yTiles) { yTile--; }

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
	}
}

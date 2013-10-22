/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

public class KeyBounds {

	private int minBound;
	private int maxBound;
	
	public KeyBounds(){}
	
	public KeyBounds(int minBound, int maxBound) {
		this.minBound = minBound;
		this.maxBound = maxBound;
	}

	public int getMinBound() {
		return minBound;
	}

	public void setMinBound(int minBound) {
		this.minBound = minBound;
	}

	public int getMaxBound() {
		return maxBound;
	}

	public void setMaxBound(int maxBound) {
		this.maxBound = maxBound;
	}
	
}

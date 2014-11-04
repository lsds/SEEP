package uk.ac.imperial.lsds.seep.api;

public enum ConnectionType {
	ONE_AT_A_TIME, 
	BATCH, 
	WINDOW, 
	ORDERED, 
	UPSTREAM_SYNC_BARRIER
}
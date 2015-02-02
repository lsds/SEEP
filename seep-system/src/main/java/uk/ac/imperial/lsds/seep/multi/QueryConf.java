package uk.ac.imperial.lsds.seep.multi;

public class QueryConf {
	
	/* 1KB = 1024
	 * 1MB = 1048576
	 * 1GB = 1073741824
	 */
	public int BATCH = 200;
	public int _BATCH_RECORDS = 1024;
	
	public QueryConf(int batch, int batchRecords) {
		this.BATCH = batch;
		this._BATCH_RECORDS = batchRecords;
	}
	
}

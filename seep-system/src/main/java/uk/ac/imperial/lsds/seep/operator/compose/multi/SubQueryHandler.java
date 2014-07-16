package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;

public class SubQueryHandler implements Runnable {

	private SubQueryTaskSubmitter submitter;
	private SubQueryTaskResultFetcher fetcher;
	
	public SubQueryHandler(ISubQueryConnectable subQueryConnectable, SubQueryTaskCreationScheme creationScheme, ISubQueryTaskResultForwarder resultForwarder) {
		this.submitter = new SubQueryTaskSubmitter(subQueryConnectable, creationScheme);
		this.fetcher = new SubQueryTaskResultFetcher(this.submitter, resultForwarder);
	}

	@Override
	public void run() {
		
		while (true) {
			this.fetcher.run();
			this.submitter.run();
		}
	}
}

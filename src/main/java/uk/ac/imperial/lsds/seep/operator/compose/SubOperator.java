package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.HashMap;
import java.util.Map;

public class SubOperator implements SubOperatorAPI{

	private static final long serialVersionUID = 1L;
	
	private SubOperatorImpl code;
	
	private boolean mostDownstream;
	private boolean mostUpstream;
	private Map<Integer, SubOperator> localDownstream;
	private Map<Integer, SubOperator> localUpstream;
	
	public static SubOperator getSubOperator(SubOperatorImpl code){
		return new SubOperator(code);
	}
	
	private SubOperator(SubOperatorImpl code){
		this.code = code;
		this.mostDownstream = true;
		this.mostUpstream = true;
		this.localDownstream = new HashMap<Integer, SubOperator>();
		this.localUpstream = new HashMap<Integer, SubOperator>();
	}
	
	private void addLocalDownstream(int localStreamId, SubOperator so){
		this.mostDownstream = false;
		if(!localDownstream.containsKey(localStreamId)){
			localDownstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}
	
	private void addLocalUpstream(int localStreamId, SubOperator so){
		this.mostUpstream = false;
		if(!localUpstream.containsKey(localStreamId)){
			localUpstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}
	
	/** Implementation of SubOperatorAPI **/
	
	@Override
	public void connectSubOperatorTo(int localStreamId, SubOperator so){
		this.addLocalDownstream(localStreamId, so);
		so.addLocalUpstream(localStreamId, this);
	}

	@Override
	public boolean isMostLocalDownstream() {
		return mostDownstream;
	}

	@Override
	public boolean isMostLocalUpstream() {
		return mostUpstream;
	}

}
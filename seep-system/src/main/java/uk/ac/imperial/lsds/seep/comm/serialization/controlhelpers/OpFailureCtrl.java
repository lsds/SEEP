package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.io.Serializable;

public class OpFailureCtrl implements Serializable {
	private int opId;
	private FailureCtrl fctrl;
	
	public OpFailureCtrl() {}
	
	public OpFailureCtrl(int opId, FailureCtrl fctrl)
	{
		this.opId = opId;
		this.fctrl = fctrl;
	}
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public FailureCtrl getFctrl() {
		return fctrl;
	}
	public void setFctrl(FailureCtrl fctrl) {
		this.fctrl = fctrl;
	}

	
}

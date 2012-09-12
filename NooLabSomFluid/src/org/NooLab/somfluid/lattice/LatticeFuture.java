package org.NooLab.somfluid.lattice;

import org.NooLab.somfluid.env.communication.LatticeFutureVisorIntf;

public class LatticeFuture {

	String processGuid;
	int nCount = 0;
	long serialID;
	int mode;
	
	LatticeFutureVisorIntf supervisor;
	
	public LatticeFuture(String guid, int ncount) {
		 
		processGuid = guid;
		nCount = ncount;
		mode = 1;
	}

	public LatticeFuture(String guid, long serialId) {
		processGuid = guid;
		nCount = 1;
		serialID = serialId;
		mode = 2;
	}

	public void sendCompletionEvent() {
		supervisor.futureHasCompleted(processGuid) ;
	}

	public void registerCallbackInterests(LatticeFutureVisorIntf visor) {
		supervisor = visor;
		
	}

	public int getnCount() {
		return nCount;
	}

	public void setnCount(int nCount) {
		this.nCount = nCount;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public String getProcessGuid() {
		return processGuid;
	}

	public long getSerialID() {
		return serialID;
	}

	public LatticeFutureVisorIntf getSupervisor() {
		return supervisor;
	}

}

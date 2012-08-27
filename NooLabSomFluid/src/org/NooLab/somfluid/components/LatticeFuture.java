package org.NooLab.somfluid.components;

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

}

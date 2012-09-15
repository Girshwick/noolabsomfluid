package org.NooLab.somfluid.tasks;

import java.util.Observer;

import org.NooLab.structures.infra.SubTaskTypesIntf;
import org.NooLab.utilities.net.GUID;




public class SomFluidSubTask implements SomFluidSubTaskIntf {

	String guidId ="";
	private boolean isCompleted;
	
	
	// ========================================================================
	public SomFluidSubTask(String guid) {
		guidId = guid ;
	}
	// ========================================================================
	
	
	@Override
	public String getGuid() {
		 
		return GUID.randomvalue();
	}


	public Object get(int subtask ) {
		
		Object returnObj = null;
		String guid ;
		SomFluidSubTaskTcpIntf tcpTask;
		
		guid = guidId;
		if (subtask == SubTaskTypesIntf._SUBTASK_TCPBOX){
			if (guid.length()==0){
				guid = GUID.randomvalue();
			}
			tcpTask = new SomFluidSubTaskTcp(guid); 
			returnObj = tcpTask;
		} 
		
		return returnObj;
	}


	@Override
	public void removeObserversAll() {
		 
		
	}


	@Override
	public void removeObserver(Observer obs) {
		 
		
	}


	public boolean isCompleted() {
		return  isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

}

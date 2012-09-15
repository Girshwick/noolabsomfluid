package org.NooLab.somfluid.tasks;

import java.util.ArrayList;
import java.util.Observer;



public class SomFluidSubTaskTcp implements SomFluidSubTaskTcpIntf{


	private String guid;
	private int outPort;
	private int inPort;

	ArrayList<Observer> observer ;
	private int activation;
	
	
	// ========================================================================
	public SomFluidSubTaskTcp( String guid){
		
	}
	// ========================================================================
	
	
	
	@Override
	public String getGuid() {
		
		return guid;
	}

	@Override
	public void setTcpPortOut(int port) {
		outPort = port ;
	}

	@Override
	public int getTcpPortOut() {
		return outPort;
	}

	@Override
	public void setTcpPortIn(int port) {
		inPort = port ;
	}

	@Override
	public int getTcpPortIn() {
		return inPort;
	}

	@Override
	public void setActivation(int activation) {
		this.activation = activation;
	}

	@Override
	public int getActivation() {
		return activation;
	}

	@Override
	public void setObserver(Observer obs) {
		observer = new ArrayList<Observer>();
		observer.add( obs );
	}

	@Override
	public void addObserver(Observer obs) {
		observer.add( obs );
	}

	@Override
	public void removeObserversAll() {
		// TODO first we have to remove them physically!!!
		observer.clear();
	}
	@Override
	public void removeObserver(Observer obs) {
		observer.add( obs );
	}



	@Override
	public Observer getObserver() {
		Observer obs=null;
		if ((observer!=null) && (observer.size()>0)){
			obs = observer.get(0) ;
		}
		return obs;
	}

	
}

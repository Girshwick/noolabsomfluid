package org.NooLab.somfluid.tasks;

import java.util.Observer;



public interface SomFluidSubTaskTcpIntf extends SomFluidSubTaskIntf{

	public static final int _TCP_ACTIVATION_NONE   = 1;
	
	/** // will close down itself after the the first full send receive pair */
	public static final int _TCP_ACTIVATION_SINGLE = 3; 
	
	/** will stay active for limited period of time, then shutdown itself */
	public static final int _TCP_ACTIVATION_TIME   = 5;
	
	/** will close only on shutdown of the application */
	public static final int _TCP_ACTIVATION_PERM   = 7;

	// ----------------------------------------------------
	
	public void setTcpPortOut(int port );
	public int  getTcpPortOut();
	
	public void setTcpPortIn(int port );
	public int  getTcpPortIn();

	public void setActivation(int tcpActivationPerm);
	public int  getActivation();
	
	public void setObserver(Observer obs);
	public void addObserver(Observer obs);
	
	public Observer getObserver();
	
	
	
	
	
}

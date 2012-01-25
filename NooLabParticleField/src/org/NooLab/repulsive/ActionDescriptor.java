package org.NooLab.repulsive;

public class ActionDescriptor {
	
	int actionCode = -1; // RepulsionFieldEventsIntf. ...
	
	int index = -1 ;
	
	double x = -3 ,y=-3 ;
	
	double[] params ;
	
	
	
	public void clear() {
		actionCode = -1;
		index = -1 ;
		x=-3;
		y=-3;
		params = null;
	}

}

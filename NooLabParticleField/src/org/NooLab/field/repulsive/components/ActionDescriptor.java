package org.NooLab.field.repulsive.components;

public class ActionDescriptor {
	
	int actionCode = -1; // RepulsionFieldEventsIntf. ...
	
	int index = -1 ;
	
	double x = -3 ,y=-3 , z=-3;
	
	double[] params ;
	
	public ActionDescriptor(){
		
	}
	
	
	public void clear() {
		actionCode = -1;
		index = -1 ;
		x=-3;
		y=-3;
		params = null;
	}


	public int getActionCode() {
		return actionCode;
	}


	public void setActionCode(int actionCode) {
		this.actionCode = actionCode;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public double getX() {
		return x;
	}


	public void setX(double x) {
		this.x = x;
	}


	public double getY() {
		return y;
	}


	public void setY(double y) {
		this.y = y;
	}


	public double getZ() {
		return z;
	}


	public void setZ(double z) {
		this.z = z;
	}


	public double[] getParams() {
		return params;
	}


	public void setParams(double[] params) {
		this.params = params;
	}

}

package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;



public class SomAssignatesDerivation implements Serializable{
 
	private static final long serialVersionUID = 8185408519370085112L;

	transient SomDataObject somData;
	
	String variableLabel = "";
	public int variableIndex;
	
	String transformGuid = "";
	boolean transformationStackIsAvailable = false;

	
	ArrayList<Integer> succ = new ArrayList<Integer>(); // often just 1, but for some writers we have 2, and if there are several instances of writers, we have many 
	ArrayList<Integer> prev = new ArrayList<Integer>(); // we could have several in case of arithmetic expressions

	
	
	// ========================================================================
	public SomAssignatesDerivation(SomDataObject somdata) {
		somData = somdata;
	}
	// ========================================================================


	public boolean isTransformationStackIsAvailable() {
		return transformationStackIsAvailable;
	}


	public void setTransformationStackIsAvailable(boolean flag) {
		this.transformationStackIsAvailable = flag;
	}


	public String getVariableLabel() {
		return variableLabel;
	}


	public void setVariableLabel(String variableLabel) {
		this.variableLabel = variableLabel;
	}


	public int getVariableIndex() {
		return variableIndex;
	}


	public void setVariableIndex(int variableIndex) {
		this.variableIndex = variableIndex;
	}


	public String getTransformGuid() {
		return transformGuid;
	}


	public void setTransformGuid(String transformGuid) {
		this.transformGuid = transformGuid;
	}


	public ArrayList<Integer> getSucc() {
		return succ;
	}


	public void setSucc(ArrayList<Integer> succ) {
		this.succ = succ;
	}


	public ArrayList<Integer> getPrev() {
		return prev;
	}


	public void setPrev(ArrayList<Integer> prev) {
		this.prev = prev;
	}

 
	
	
	
	
}

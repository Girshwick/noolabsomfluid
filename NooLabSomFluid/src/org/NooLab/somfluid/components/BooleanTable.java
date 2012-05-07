package org.NooLab.somfluid.components;

import java.io.Serializable;
import java.util.ArrayList;



public class BooleanTable implements Serializable{

	ArrayList<ArrayList<FilterCondition>> boolCondition ;
	
	public BooleanTable(){
		
	}
	
	// ------------------------------------------------------------------------
	public void addBooleanCondition( String conditionAsText){
		
	}
	// ------------------------------------------------------------------------
	
	
	public ArrayList<ArrayList<FilterCondition>> getBoolCondition() {
		return boolCondition;
	}

	public void setBoolCondition(ArrayList<ArrayList<FilterCondition>> boolCondition) {
		this.boolCondition = boolCondition;
	}
	
	
}

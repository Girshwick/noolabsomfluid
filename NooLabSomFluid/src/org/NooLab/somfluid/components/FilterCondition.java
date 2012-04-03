package org.NooLab.somfluid.components;

import java.io.Serializable;

public class FilterCondition implements Serializable{

	private static final long serialVersionUID = -2982168703366986633L;


	
	String conditionText="";
	String comparator = "";
	
	String booleanConjunction = "";
	String[] thresholds = new String[0];
	double[] thresholdValues = new double[0];
	
	// ========================================================================
	public FilterCondition(){
		
	}
	// ========================================================================
	
	
	
}

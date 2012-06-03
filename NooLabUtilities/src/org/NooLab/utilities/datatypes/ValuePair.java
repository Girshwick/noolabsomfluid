package org.NooLab.utilities.datatypes;

import java.io.Serializable;



public class ValuePair implements Serializable{

	private static final long serialVersionUID = -2189795455703098200L; 
	
	double value1 = 0.0;
	double value2 = 0.0;
	
	// ------------------------------------------------------------------------
	public ValuePair(){
	}
	
	public ValuePair(double v1, double v2) {
		value1 = v1;
		value2 = v2;
	}

	// ------------------------------------------------------------------------
	

	public double getValue1() {
		return value1;
	}

	public void setValue1(double value1) {
		this.value1 = value1;
	}

	public double getValue2() {
		return value2;
	}

	public void setValue2(double value2) {
		this.value2 = value2;
	}
	
	

}

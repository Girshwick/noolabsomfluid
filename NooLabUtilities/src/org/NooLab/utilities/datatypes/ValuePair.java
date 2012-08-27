package org.NooLab.utilities.datatypes;

import java.io.Serializable;



public class ValuePair <T> implements Serializable{

	private static final long serialVersionUID = -2189795455703098200L; 
	
	T value1 ;
	T value2 ;
	
	// ------------------------------------------------------------------------
	public ValuePair(){
	}
	
	public ValuePair(double v1, double v2) {
		value1 = (T)(Double)v1;
		value2 = (T)(Double)v2;
	}
	public ValuePair(int v1, int v2) {
		value1 = (T)(Integer)v1;
		value2 = (T)(Integer)v2;
	}
	public ValuePair(long v1, long v2) {
		value1 = (T)(Long)v1;
		value2 = (T)(Long)v2;
	}

	// ------------------------------------------------------------------------
	

	public T getValue1() {
		return value1;
	}

	public void setValue1(double value) {
		value1 = (T)(Double)value;
	}

	public T getValue2() {
		return (T)(Double)value2;
	}

	public void setValue2(double value) {
		value2 = (T)(Double)value;
	}
	
	public void setValue1(Long value) {
		value1 = (T)value;
	}
	public void setValue2(Long value) {
		value2 = (T)value;
	}
	

}

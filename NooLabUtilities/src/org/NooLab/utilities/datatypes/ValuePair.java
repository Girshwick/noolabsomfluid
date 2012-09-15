package org.NooLab.utilities.datatypes;

import java.io.Serializable;


@SuppressWarnings("unchecked")
public class ValuePair <T> implements Serializable{

	private static final long serialVersionUID = -2189795455703098200L; 
	
	T value1 ;
	T value2 ;
	
	Object dataObj ;
	
	// ------------------------------------------------------------------------
	public ValuePair(){
	}
	
	public ValuePair(String s1, String s2) {
		value1 = (T)(String)s1;
		value2 = (T)(String)s2;
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
	
	public ValuePair<T> setData(Object obj){
		dataObj = obj ; // need to be a clone, which has to be done outside
		return this;
	}
	
	public Object getData(){
		return dataObj ;
	}

	public T getValue1() {
		return value1;
	}

	public T getValue2() {
		String cn = value2.getClass().getSimpleName().toLowerCase();
		T outv = null;
		
		if (cn.contains("double")){
			outv = (T)(Double)value2;
		}
		if (cn.contains("int")){
			outv = (T)(Integer)value2;
		}
		if (cn.contains("long")){
			outv = (T)(Long)value2;
		}
		if (cn.contains("string")){
			outv = (T)(String)value2;
		}		
		return outv ;
	}


	public ValuePair<T> setValue1(double value) {
		value1 = (T)(Double)value;
		return this;
	}

	public ValuePair<T> setValue2(double value) {
		value2 = (T)(Double)value;
		return this;
	}
	
	public ValuePair<T> setValue1(Long value) {
		value1 = (T)value;
		return this;
	}
	public ValuePair<T> setValue2(Long value) {
		value2 = (T)value;
		return this;
	}
	
	public ValuePair<T> setValue1(Integer value) {
		value1 = (T)value;
		return this;
	}
	public ValuePair<T> setValue2(Integer value) {
		value2 = (T)value;
		return this;
	}

	public ValuePair<T> setValue1(String value) {
		value1 = (T)value;
		return this;
	}
	public ValuePair<T> setValue2(String value) {
		value2 = (T)value;
		return this;
	}
	
}

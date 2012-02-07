package org.NooLab.somfluid.components;

import java.io.Serializable;



/* TODO: reference to global missing value object,
         which can hold many missing value translations
         this implies the usage of a global mv factory
*/

/**
 * 
 * missing values are attached to data objects, usually column or rows
 * 
 */
public class MissingValues implements Serializable{

	private static final long serialVersionUID = 8793216734157914713L;

	
	transient Object parent;
	
	double mvIndicatorValue = -1.0;
	
	int count = 0;
	double averagePosition = -1.0 ; // would be column.size()/2 if evenly distributed,... a simple measure for skewness
	
	
	// ------------------------------------------------------------------------
	public MissingValues( Object obj){
		parent = obj;
		
	}
	// ------------------------------------------------------------------------

	public int incCount() {
		return count++;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getMvIndicatorValue() {
		return mvIndicatorValue;
	}

	public void setMvIndicatorValue(double mvIndicatorValue) {
		this.mvIndicatorValue = mvIndicatorValue;
	}
	
	
	
	
}

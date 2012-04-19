package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

public class AlgorithmParameter implements AlgorithmParameterIntf{

	// TODO: should not be "naked" strings, but (pseudo-)maps with a descriptor,
	String label = "";
	
	String typeLabel = "" ; // as used in instanceOf, class().name
	
	double numValue ;
	double strValue ;

	int[]    intValues = new int[0] ;
	double[] numValues = new double[0] ;
	String[] strValues = new String[0] ;
	
	ArrayList<Object> list = new ArrayList<Object>();

	public Object obj; 
	
	// ========================================================================
	public AlgorithmParameter(){
		
	}
	// ========================================================================
	
	
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the typeLabel
	 */
	public String getTypeLabel() {
		return typeLabel;
	}

	/**
	 * @param typeLabel the typeLabel to set
	 */
	public void setTypeLabel(String typeLabel) {
		this.typeLabel = typeLabel;
	}

	/**
	 * @return the numValue
	 */
	public double getNumValue() {
		return numValue;
	}

	/**
	 * @param numValue the numValue to set
	 */
	public void setNumValue(double numValue) {
		this.numValue = numValue;
	}

	/**
	 * @return the strValue
	 */
	public double getStrValue() {
		return strValue;
	}

	/**
	 * @param strValue the strValue to set
	 */
	public void setStrValue(double strValue) {
		this.strValue = strValue;
	}
	
	
	
}

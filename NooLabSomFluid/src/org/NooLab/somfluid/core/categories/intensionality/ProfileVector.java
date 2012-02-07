package org.NooLab.somfluid.core.categories.intensionality;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.data.Variable;


/**
 * 
 * The weight vector of a SOM node usually contains for all mutable variables (non-ID/TV) 
 * the average of the values in the extensoinal list;
 * 
 * That is, the WeightVector itself does not know anything about TV or index variable...
 * which is solely the business of the Node 
 * 
 * In our case, however, the weight vector also may contain a further section, which is
 * referring to dnaic propertie sof the Node, or the data, e.g. the deviation of the
 * data in the node against a model functoin (such as a correlation)
 * such internal measurements can not be predefined, and they are also not stable input data
 * since they are constantly changing (due to the list of data in the node, the state of other
 * nodes etc.). 
 * 
 * The two sections are stored separately in the WeightVector object, 
 * they will be concatened just on get() (yet they are buffered.)
 * 
 * 
 * 
 */
class ProfileVector implements Serializable, ProfileVectorIntf {

	private static final long serialVersionUID = 9175442377084639661L;

	
	long lastUpdateTime = 0;
	long lastRecalcTime = 0;
	
	// this is important for dealing with compound weight vectors,  
	// composed from external data and internal measurements
	int lastExtDataValueIndex = 0 ;
	
	ArrayList<String>   variablesStr = new ArrayList<String>(); 
	ArrayList<Variable> variables = new ArrayList<Variable>(); 
	ArrayList<Double>   values    = new ArrayList<Double>();
	
	// internal measurements
	
	ArrayList<String>   intSomNodeProperties = new ArrayList<String>(); 
	ArrayList<Double>   intSomNodePropValues = new ArrayList<Double>();
	
	// this is produced by concat of "values" + "intSomNodePropValues", it is visible only for dedicated modes
	ArrayList<Double>   compoundValues       = new ArrayList<Double>();
	
	// statistical description of the WeightVector for faster access
	
	
	// compression.e.g. 1D Wavelet coefficients, FFT-coefficients
	
	
	
	
	// ========================================================================
	public ProfileVector(){
		int z;
		z=0;
	}
	// ========================================================================
	
 
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

 
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}


	public long getLastRecalcTime() {
		return lastRecalcTime;
	}


	public void setLastRecalcTime(long lastRecalcTime) {
		this.lastRecalcTime = lastRecalcTime;
	}


	public ArrayList<Variable> getVariables() {
		return variables;
	}


	public void setVariables(ArrayList<Variable> variables) {
		this.variables = variables;
	}


	public ArrayList<Double> getValues() {
		return values;
	}


	public void setValues(ArrayList<Double> values) {
		this.values = values;
	}




	public int getLastExtDataValueIndex() {
		return lastExtDataValueIndex;
	}


 
	public void setLastExtDataValueIndex(int lastExtDataValueIndex) {
		this.lastExtDataValueIndex = lastExtDataValueIndex;
	}


 
	public ArrayList<String> getVariablesStr() {
		return variablesStr;
	}


 
	public void setVariablesStr(ArrayList<String> variablesStr) {
		this.variablesStr = variablesStr;
	}


 
	public ArrayList<Double> getCompoundValues() {
		return compoundValues;
	}


 
	public void setCompoundValues(ArrayList<Double> compoundValues) {
		this.compoundValues = compoundValues;
	}


 
	public ArrayList<String> getIntSomNodeProperties() {
		return intSomNodeProperties;
	}

 
	public ArrayList<Double> getIntSomNodePropValues() {
		return intSomNodePropValues;
	}
	
	
}

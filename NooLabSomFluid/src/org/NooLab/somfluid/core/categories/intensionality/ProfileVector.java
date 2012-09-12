package org.NooLab.somfluid.core.categories.intensionality;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;


/**
 * 
 * The weight vector of a SOM node usually contains for all mutable variables (non-ID/TV) 
 * the average of the values in the extensional list;
 * 
 * That is, the WeightVector itself does not know anything about TV or index variable...
 * which is solely the business of the Node 
 * 
 * In our case, however, the weight vector also may contain a further section, which is
 * referring to dynamic properties of the Node, or the data, e.g. the deviation of the
 * data in the node against a model function (such as a correlation)
 * such internal measurements can not be predefined, and they are also not stable input data
 * since they are constantly changing (due to the list of data in the node, the state of other
 * nodes etc.). 
 * 
 * The two sections are stored separately in the WeightVector object, 
 * they will be concatenated just on get() (yet they are buffered.)
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
	
	// this is produced by concatenation of "values" + "intSomNodePropValues", it is visible only for dedicated modes
	ArrayList<Double>   compoundValues       = new ArrayList<Double>();
	
	// statistical description of the WeightVector for faster access
	
	
	// compression.e.g. 1D Wavelet coefficients, FFT-coefficients
	
	
	
	
	// ========================================================================
	public ProfileVector(){
		int z;
		z=0;
	}
	public ProfileVector(ProfileVector templateProfile) {
		
		try{
			
			lastExtDataValueIndex = templateProfile.lastExtDataValueIndex ;
			if ( templateProfile.variablesStr!=null) variablesStr.addAll( templateProfile.variablesStr ) ;  
			if (templateProfile.variables!=null) variables.addAll( templateProfile.variables ) ; 
			if (templateProfile.values!=null)values.addAll( templateProfile.values ) ;
			
			if (templateProfile.intSomNodeProperties!=null) intSomNodeProperties.addAll( templateProfile.intSomNodeProperties ) ; 
			if (templateProfile.intSomNodePropValues!=null) intSomNodePropValues.addAll( templateProfile.intSomNodePropValues ) ;
			
			if (templateProfile.compoundValues!=null) compoundValues.addAll( templateProfile.compoundValues ) ;
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public ProfileVector(ProfileVectorIntf templateProfile) {

		ArrayList<Integer> useIndexes ;
		
		
		try{
			
			
			
			lastExtDataValueIndex = templateProfile.getLastExtDataValueIndex()  ;
			if ( templateProfile.getVariablesStr() !=null) variablesStr.addAll( templateProfile.getVariablesStr() ) ;  
			if (templateProfile.getVariables() !=null) variables.addAll( templateProfile.getVariables() ) ; 
			if (templateProfile.getValues() !=null)values.addAll( templateProfile.getValues() ) ;
			
			if (templateProfile.getIntSomNodeProperties() !=null) intSomNodeProperties.addAll( templateProfile.getIntSomNodeProperties() ) ; 
			if (templateProfile.getIntSomNodePropValues() !=null) intSomNodePropValues.addAll( templateProfile.getIntSomNodePropValues() ) ;
			
			if (templateProfile.getCompoundValues() !=null) compoundValues.addAll( templateProfile.getCompoundValues()) ;
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	public void clear() {
		variablesStr.clear();
		variables.clear();
		values.clear();
		
		intSomNodeProperties.clear(); 
		intSomNodePropValues.clear();
		compoundValues.clear();
		
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



	public void setVariables(ArrayList<Variable> variablesList) {
		int n =0;
		if (variables!=null){
			n = variables.size() ;
		}
		variables = variablesList;
		variables.trimToSize() ;
		if ((n!=0) && (n != variables.size())){
			adjustValuesVectorLen();
		}
	}


	private void adjustValuesVectorLen() {
		 
		int n, vn;
		
		vn = variables.size();
		n = values.size() ;
		
		if (vn>n){
			for (int i=0;i<vn-n;i++){
				values.add( 0.0 ) ;
			}
		}
		if (vn<n){
			for (int i=0;i<n-vn;i++){
				values.remove(vn) ;
			}
		}
		
		values.trimToSize() ;
	}
	public ArrayList<Double> getValues() {
		return values;
	}


	


	public void setValues(ArrayList<Double> invalues) {
		this.values = new ArrayList<Double>();
		
		for (int i=0;i<invalues.size();i++){
			values.add( invalues.get(i));
		}
		
		for (int i=0;i<values.size();i++){
			
			if ((values.get(i)>1.0) && (values.get(i)<1.8)){
				values.set(i, 1.0);
			}
			if ((values.get(i)<0.0) && (values.get(i)!=-1.0)){
				values.set(i, 0.0);
			}
		}
		values.trimToSize() ;
	}




	@Override
	public void changeProfile(ArrayList<Double> dataVector, int count, int direction) {
		double v , virtualSum, npv;
		int rc=1;
		boolean hb ;
		
		if (direction<-1)direction=-1;
		if (direction> 1)direction= 1;
		
		
		for (int i=0;i<values.size();i++){
			
			hb = true;
			
			// only if not index variable
			if (hb){
				
				v = values.get(i) ;
				
				if (direction>0){
					rc = (count-1);
					
				}
				if (direction<=0){
					rc = (count+1);
				}
				virtualSum = v * (double)(1.0*rc) ;
				virtualSum = virtualSum + dataVector.get(i) ;
				
				npv = virtualSum/(rc + direction);
				
				values.set(i, npv);
			}
			values.trimToSize();
		} // i-> all positions in vector
		
		
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

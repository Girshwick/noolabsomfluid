package org.NooLab.somfluid.core.categories.intensionality;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;


/**
 * 
 * This is about profiles, weights, usevectors = variable selection 

 * 
 */
public class IntensionalitySurface implements 	Serializable ,
												IntensionalitySurfaceIntf{

	private static final long serialVersionUID = 1349099378078978472L;

	/** a compound object that holds several aspects of a profile vector   */
	ProfileVector profileVector = new ProfileVector() ;

	/**  
	 * the weight vector is NOT the use vector, the weight vector describes the weight of a variable IFF used
	 * the usevector  is also part of similarity class, accessible via" similarity.getUsageIndicationVector()" 
	 */
	ArrayList<Double> useWeights = new ArrayList<Double>();

	private ArrayList<Double> usageIndicationVector;
	
	
	private boolean isQualifiedTarget;

	private int targetVariableIndex = -1 ;
	
	
	// ------------------------------------------------------------------------
	public IntensionalitySurface( ){
		 
	}
	
	public IntensionalitySurface(IntensionalitySurface inIntensions) {
		useWeights.clear() ;
		useWeights.addAll( inIntensions.useWeights );

		usageIndicationVector = new ArrayList<Double>();
		if (inIntensions.usageIndicationVector !=null) usageIndicationVector.addAll( inIntensions.usageIndicationVector ) ;
		
		
		profileVector = new ProfileVector( inIntensions.profileVector );
		
		isQualifiedTarget = inIntensions.isQualifiedTarget ;
		targetVariableIndex = inIntensions.targetVariableIndex ;
	}
	
	// ------------------------------------------------------------------------

	

	public IntensionalitySurface(IntensionalitySurfaceIntf inIntensions) {
		

		usageIndicationVector = new ArrayList<Double>();
		if (inIntensions.getUsageIndicationVector() !=null) usageIndicationVector.addAll( inIntensions.getUsageIndicationVector() ) ;
		
		
		profileVector = new ProfileVector( inIntensions.getProfileVector() );
		
		isQualifiedTarget = inIntensions.isQualifiedTarget() ;
		targetVariableIndex = inIntensions.getTargetVariableIndex() ;
		
		
	}

	@Override
	public ProfileVectorIntf getAbridgedVersion( ArrayList<Integer> usedVarIndexes ) {
											// sth like [6, 10, 17], without TV-index
		ProfileVector pvi ;
		int ix; 
		pvi = new ProfileVector ();
		
		
		pvi.values = new ArrayList<Double>(); 
		pvi.variables = new ArrayList<Variable>();
		pvi.variablesStr = new ArrayList<String>();
		
		for (int i=0;i<usedVarIndexes.size();i++){
			ix = usedVarIndexes.get(i);
			if ((ix>=0) && (ix<profileVector.values.size())){
				pvi.values.add( profileVector.values.get(ix));
				pvi.variables.add( profileVector.variables.get(ix) ); 
				pvi.variablesStr.add( profileVector.variablesStr.get(ix) ); 
			} // within boundaries of list ?
		}
		
		return pvi;
	}
	
	/*
		 
		for (int i=0;i<usageIndicationVector.size();i++){
			double iv = usageIndicationVector.get(i);
			if (iv>0){
				usedVarIndexes.add(i) ;
			}
		}
	 */
	/**   the weight vector is NOT the use vector, the weight vector describes the weight of a variable IFF used  */
	public void prepareWeightVector(){
		useWeights.clear() ;
		int n = profileVector.values.size();
		for (int i=0;i<n;i++){
			useWeights.add(1.0) ;
		}
	}
	
	
	
	// ------------------------------------------------------------------------
	
	@Override
	public void clear(int mode) {
		if (usageIndicationVector!=null)usageIndicationVector.clear(); 
		if (useWeights!=null)useWeights.clear() ;
		if (profileVector!=null) profileVector.clear();
	}

	@Override
	public ProfileVectorIntf getProfileVector(){
		return profileVector;
	}
 
	@Override
	public ArrayList<Double> getWeightsVector() {
		return useWeights;
	}


	@Override
	public void initializeWeightsVector(double defaultValue) {
		prepareWeightVector();
		
	}

	@Override
	public ArrayList<String> getVariablesStr(int usageIndicator) {
		ArrayList<String> usedVariables ;
		
		usedVariables = profileVector.getVariablesStr() ;
		 
		int i=usedVariables.size()-1;
		while (i>=0){
			if (usageIndicationVector.get(i) <= 0.0){
				usedVariables.remove(i) ;
			}
			i--;
		} // i-> all variables
		
		return usedVariables;
	}

	@Override
	public ArrayList<Double> getValues(int usageIndicator) {
		ArrayList<Double> usedVarValues ;
		
		usedVarValues = profileVector.getValues() ;
		
		int i=usedVarValues.size()-1;
		
		while (i>=0){
			if (usageIndicationVector.get(i) <= 0.0){
				usedVarValues.remove(i) ;
			}
			i--;
		} // i-> all variables
		
		return usedVarValues;
	}
	
	@Override
	public void setUsageIndicationVector(ArrayList<Double> usevector) {
		int n = 0;
		if (usageIndicationVector!=null){
			usageIndicationVector.size(); 
		}
		usageIndicationVector = usevector; // it is a copy, not just a reference, as it originates in the similarity part ...
										   // but the active TV is excluded here, since it is used only for application purposes
		if ((n>0) && (n!=usageIndicationVector.size())){
			adjustUseWeightsVectorLen();
		}
	}
 
	private void adjustUseWeightsVectorLen(){
		
		int n, vn;
		
		vn = usageIndicationVector.size();
		n = useWeights.size() ;
		
		if (vn>n){
			for (int i=0;i<vn-n;i++){
				useWeights.add( 1.0 ) ;
			}
		}
		if (vn<n){
			for (int i=0;i<n-vn;i++){
				useWeights.remove(vn) ;
			}
		}
	}
	
	@Override
	public ArrayList<Double> getUsageIndicationVector() {
		 
		return usageIndicationVector;
	}
	@Override
	public void setTargetStatus(boolean istarget) {
		isQualifiedTarget = istarget;
		
	}

	public boolean isQualifiedTarget() {
		return isQualifiedTarget;
	}
	public ArrayList<Double> getUseWeights() {
		return useWeights;
	}

	public void setUseWeights(ArrayList<Double> useWeights) {
		this.useWeights = useWeights;
	}

	public boolean getQualifiedTarget() {
		return isQualifiedTarget;
	}

	public void setQualifiedTarget(boolean isQualifiedTarget) {
		this.isQualifiedTarget = isQualifiedTarget;
	}

	@Override
	public void setTargetVariableIndex(int indexVal) {
		targetVariableIndex = indexVal;
	}

	@Override
	public int getTargetVariableIndex() {
		return targetVariableIndex;
	}
	
}

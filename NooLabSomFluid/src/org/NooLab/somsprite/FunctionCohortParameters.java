package org.NooLab.somsprite;

import java.util.ArrayList;





public class FunctionCohortParameters {

	ArrayList<FunctionCohortParameterSet> cohortParameterSets = new ArrayList<FunctionCohortParameterSet>() ;
	
	
	// ========================================================================
	public FunctionCohortParameters(String varPLabel, double lo, double hi, int steps, String scalingFunc) {
		
		FunctionCohortParameterSet cps = new FunctionCohortParameterSet(varPLabel, lo, hi, steps, scalingFunc);
		cohortParameterSets.add(cps) ;
	}
	// ========================================================================
	
	
	public void addCohortParameter( String varPLabel, double lo, double hi, int steps, String scalingFunc ){
		
		FunctionCohortParameterSet cps = new FunctionCohortParameterSet(varPLabel, lo, hi, steps, scalingFunc);
		cohortParameterSets.add(cps) ;
	}
	
	public FunctionCohortParameterSet getitem( int index){
		return cohortParameterSets.get(index) ;
	}
	
	public ArrayList<FunctionCohortParameterSet> getItems(){
		return cohortParameterSets;
	}
	
	public void remove(int index){
		cohortParameterSets.remove(index) ;
	}
	
	public int size(){
		return cohortParameterSets.size() ;
	}
	
	
	
}

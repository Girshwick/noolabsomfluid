package org.NooLab.somfluid.structures;

import java.util.ArrayList;




public interface VariablesIntf {

	
	public Variable getItem( int index );
	public  ArrayList<Variable> getItems();
	
	public ArrayList<String> getVariableSelection( int[] useindicator) ;

	public ArrayList<String> getVariableSelection( ArrayList<Double> usagevector) ;

		
	public ArrayList<String> getLabelsForVariablesList(Variables vars);
	
	public ArrayList<String> getLabelsForVariablesList(ArrayList<Variable> vars);
	
	public ArrayList<Integer> getIndexesForLabelsList(ArrayList<String> setItems) ;
	
	
	
	/** all variables that are not ID, not TV, not targeted,  not blacklisted */
	public ArrayList<Variable> getActiveVariables();
	
	public ArrayList<String> getActiveVariableLabels();
	
	public Variable getActiveTargetVariable();
	
	public String getActiveTargetVariableLabel() ;
	
	public ArrayList<Variable> getAllTargetedVariables();
	
	public ArrayList<Variable> getAllTargetedVariables(int includingActiveTargetVariable) ;
	
	
	public ArrayList<Variable> getAllIndexVariables();
	
	

	
}

package org.NooLab.somfluid.data;

import java.util.ArrayList;




public interface VariablesIntf {

	/** all variables that are not ID, not TV, not targeted,  not blacklisted */
	public ArrayList<Variable> getActiveVariables();
	
	public ArrayList<String> getActiveVariableLabels();
	
	public Variable getActiveTargetVariable();
	
	public String getActiveTargetVariableLabel() ;
	
	public ArrayList<String> getAllTargetedVariables();
	
	public ArrayList<String> getAllIndexVariables();
	

	
}

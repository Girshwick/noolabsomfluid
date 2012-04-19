package org.NooLab.somfluid.components;

import org.NooLab.somfluid.data.Variable;
import org.NooLab.somscreen.SomQualityData;
 


public class VariableContribution {

	transient VariableContributions parent;
	
	String variableLabel="";
	transient Variable variable;
	
	double scoreDelta;
	
	SomQualityData sqData;
	
	double[] contributionValues  = new double[3] ; // the actual values underlying the type: TP, FP, overall
	int[]    contributionType    = new int[2] ;    // pre-defined type: TP+, FP+, TP-, FP-, biarized into 1,0,-1 
	int contributionClass   = -1;
	
	// ------------------------------------------------------------------------
	public VariableContribution(VariableContributions parent, Variable variable){
		
		this.parent = parent;
		this.variable = variable;
		variableLabel = variable.getLabel() ;
		
	}
	// ------------------------------------------------------------------------

	/**
	 * @return the variableLabel
	 */
	public String getVariableLabel() {
		return variableLabel;
	}

	/**
	 * @param variableLabel the variableLabel to set
	 */
	public void setVariableLabel(String variableLabel) {
		this.variableLabel = variableLabel;
	}

	/**
	 * @return the scoreDelta
	 */
	public double getScoreDelta() {
		return scoreDelta;
	}

	/**
	 * @param scoreDelta the scoreDelta to set
	 */
	public void setScoreDelta(double scoreDelta) {
		this.scoreDelta = scoreDelta;
	}

 

	/**
	 * @return the contributionValues
	 */
	public double[] getContributionValues() {
		return contributionValues;
	}

	/**
	 * @return the contributionType
	 */
	public int[] getContributionType() {
		return contributionType;
	}

	/**
	 * @return the contributionClass
	 */
	public int getContributionClass() {
		return contributionClass;
	}

	/**
	 * @param contributionClass the contributionClass to set
	 */
	public void setContributionClass(int contributionClass) {
		this.contributionClass = contributionClass;
	}

	/**
	 * @return the variable
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * @return the sqData
	 */
	public SomQualityData getSqData() {
		return sqData;
	}
	
	
	
}

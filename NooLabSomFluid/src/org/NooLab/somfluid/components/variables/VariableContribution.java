package org.NooLab.somfluid.components.variables;

import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somscreen.SomQualityData;
 

/**
 * 
 * This class is used to collect the data and objects required for measuring the contribution
 * of individual variables. </br></br>
 * 
 * Basically, two score values are compared: that of the base metric (usually the best available one)
 * and the modified metric, which is the base metric without just the targeted variable.</br></br> 
 * 
 * The score difference is negative if the model gets worse. </br>
 * Since the score's max value is normalized approximately to 100 (which course is dependent on the
 * error cost ratio), this value reflects the change as a "percentage" that expresses predictivity,
 * not however predicability!!! the baseline is given by another model 
 *
 */
public class VariableContribution {

	transient VariableContributions parent;
	
	String variableLabel="";
	transient Variable variable;
	
	double scoreDelta;
	
	private SomQualityData sqData;
	
	double[] contributionValues  = new double[3] ; // the actual values underlying the type: TP, FP, overall
	int[]    contributionType    = new int[2] ;    // pre-defined type: TP+, FP+, TP-, FP-, binarized into 1,0,-1 
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

	public int determineContributionClass() {
		int result = -1;
		if (parent.bestSqData == null){
			return result;
		}
		
		
		try{
			
			
			
			
			
		}catch(Exception e){
			
		}
		
		return result ;
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

	public void setSqData(SomQualityData sqData) {
		this.sqData = sqData;
	}
	
	
	
}

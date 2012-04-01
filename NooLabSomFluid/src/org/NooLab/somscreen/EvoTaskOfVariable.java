package org.NooLab.somscreen;

import java.io.Serializable;

/**
 * This class describes the internal state of a variable.
 * This "state" is a compund built up from 5 different tendencies
 * 
 * - variable is the only one changed (add/removed)
 * - variable is part of large set of changes
 * - variable is changed together with another good one
 * - variable is changed together with a bad one
 * 
 * there is a list containing this object for any of the variables
 * but it is not part of the definition of a variable (the variabe may contain a reference
 * to the interface of this class here)
 *
 */
public class EvoTaskOfVariable  implements Serializable,
                                           EvolutionaryTaskStateIntf{

	private static final long serialVersionUID = -6528086351474098393L;

	String varLabel = "";
	
	
	int meets = 0;
	int singularAdd=0;
	int singularRemoval=0;
	int collinearPlus=0;
	int collinearNeg=0;
	
	double meetsRatio = 0;
	double singularAddRatio =0;
	double singularRemovalRatio =0;
	double collinearPlusRatio =0;
	double collinearNegRatio =0;
	
	
	
	
	int variableCount = 1;
	/**
	 *  the initial weights that control probability of release (and contribution?);
	 */
	double[] initialevotaskWeights = new double[]{ 1.2, 1.0, 1.0, 0.75, 0.32 } ;

	/**
	 *  the initial weights that control probability of release (and contribution?);
	 *  note that these values are dynamic: 
	 *    a particular tasks weight will be reduced to the initial value upon release,
	 *    while all other tasks increase their weight (dependent on the # variables!)
	 *  
	 */
	double[] evotaskWeights = new double[initialevotaskWeights.length] ;
	
	/** 
	 * used for simple selection of tasks as a probabilistic release...
	 */
	double[] cumulatedtaskWeights = new double[evotaskWeights.length] ;
	
	double etwSum ;
	
	// ------------------------------------------------------------------------
	public EvoTaskOfVariable( String vlabel){
		varLabel = vlabel;
		
		for (int i=0;i<initialevotaskWeights.length;i++){
			evotaskWeights[i] = initialevotaskWeights[i] ;
		}
		
		calculateTaskWeightSum();
	}
	// ------------------------------------------------------------------------
	
	
	private void calculateTaskWeightSum(){
		etwSum = 0;
		
		for (int i=0;i<evotaskWeights.length;i++){
			etwSum = etwSum+ evotaskWeights[i] ;
			if (i>0){
				cumulatedtaskWeights[i] = cumulatedtaskWeights[i-1] + evotaskWeights[i] ; 
			}else{
				cumulatedtaskWeights[i] = evotaskWeights[i] ;
			}
		}
	}

	/**
	 * 
	 * @param randomNumber
	 * @param presence <0: select a removal task, >0 select an add task, =0: indifferent
	 * @return
	 */
	public int selectTask( double randomNumber, int presence){
		int selectedTask=-1;
		
		
		
		return selectedTask;
	}
	
	// ========================================================================


	/**
	 * @return the singularAdd
	 */
	public int getSingularAdd() {
		return singularAdd;
	}


	/**
	 * @param singularAdd the singularAdd to set
	 */
	public void setSingularAdd(int singularAdd) {
		this.singularAdd = singularAdd;
	}


	/**
	 * @return the singularRemoval
	 */
	public int getSingularRemoval() {
		return singularRemoval;
	}


	/**
	 * @param singularRemoval the singularRemoval to set
	 */
	public void setSingularRemoval(int singularRemoval) {
		this.singularRemoval = singularRemoval;
	}


	/**
	 * @return the collinearPlus
	 */
	public int getCollinearPlus() {
		return collinearPlus;
	}


	/**
	 * @param collinearPlus the collinearPlus to set
	 */
	public void setCollinearPlus(int collinearPlus) {
		this.collinearPlus = collinearPlus;
	}


	/**
	 * @return the collinearNeg
	 */
	public int getCollinearNeg() {
		return collinearNeg;
	}


	/**
	 * @param collinearNeg the collinearNeg to set
	 */
	public void setCollinearNeg(int collinearNeg) {
		this.collinearNeg = collinearNeg;
	}


	/**
	 * @return the evotaskWeights
	 */
	public double[] getEvotaskWeights() {
		return evotaskWeights;
	}


	/**
	 * @param evotaskWeights the evotaskWeights to set
	 */
	public void setEvotaskWeights(double[] evotaskWeights) {
		this.evotaskWeights = evotaskWeights;
	}


	/**
	 * @return the varLabel
	 */
	public String getVarLabel() {
		return varLabel;
	}


	/**
	 * @param varLabel the varLabel to set
	 */
	public void setVarLabel(String varLabel) {
		this.varLabel = varLabel;
	}


	/**
	 * @return the variableCount
	 */
	public int getVariableCount() {
		return variableCount;
	}


	/**
	 * @param variableCount the variableCount to set
	 */
	public void setVariableCount(int variableCount) {
		this.variableCount = variableCount;
	}


	/**
	 * @return the initialevotaskWeights
	 */
	public double[] getInitialevotaskWeights() {
		return initialevotaskWeights;
	}


	/**
	 * @param initialevotaskWeights the initialevotaskWeights to set
	 */
	public void setInitialevotaskWeights(double[] initialevotaskWeights) {
		this.initialevotaskWeights = initialevotaskWeights;
	}


	/**
	 * @return the cumulatedtaskWeights
	 */
	public double[] getCumulatedtaskWeights() {
		return cumulatedtaskWeights;
	}


	/**
	 * @param cumulatedtaskWeights the cumulatedtaskWeights to set
	 */
	public void setCumulatedtaskWeights(double[] cumulatedtaskWeights) {
		this.cumulatedtaskWeights = cumulatedtaskWeights;
	}


	/**
	 * @return the etwSum
	 */
	public double getEtwSum() {
		return etwSum;
	}


	/**
	 * @param etwSum the etwSum to set
	 */
	public void setEtwSum(double etwSum) {
		this.etwSum = etwSum;
	}
	
}

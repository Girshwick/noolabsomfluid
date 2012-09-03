package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;
import java.util.ArrayList;




public class EcrCompliance  implements Serializable{
 
	private static final long serialVersionUID = 8815859079551980957L;

	String description = "";
	
	int ecrCasesSum = 0;
	int ecrObservationsSum = 0;
	
	double cumulativeRelRisk = 1.0 ;
	
	ArrayList<Integer> ecrNodes = new ArrayList<Integer>();
	
	// ========================================================================
	public EcrCompliance(){
		
	}

	public EcrCompliance(EcrCompliance ecrCompliance) {
		 
		ecrNodes = new ArrayList<Integer>( ecrCompliance.ecrNodes );
		
		ecrCasesSum = ecrCompliance.ecrCasesSum ;
		ecrObservationsSum = ecrCompliance.ecrObservationsSum ;
		cumulativeRelRisk = ecrCompliance.cumulativeRelRisk ;
	}
	// ========================================================================

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the ecrCasesSum
	 */
	public int getEcrCasesSum() {
		return ecrCasesSum;
	}

	/**
	 * @param ecrCasesSum the ecrCasesSum to set
	 */
	public void setEcrCasesSum(int ecrCasesSum) {
		this.ecrCasesSum = ecrCasesSum;
	}

	/**
	 * @return the ecrObservationsSum
	 */
	public int getEcrObservationsSum() {
		return ecrObservationsSum;
	}

	/**
	 * @param ecrObservationsSum the ecrObservationsSum to set
	 */
	public void setEcrObservationsSum(int ecrObservationsSum) {
		this.ecrObservationsSum = ecrObservationsSum;
	}

	/**
	 * @return the cumulativeRelRisk
	 */
	public double getCumulativeRelRisk() {
		return cumulativeRelRisk;
	}

	/**
	 * @param cumulativeRelRisk the cumulativeRelRisk to set
	 */
	public void setCumulativeRelRisk(double cumulativeRelRisk) {
		this.cumulativeRelRisk = cumulativeRelRisk;
	}

	/**
	 * @return the ecrNodes
	 */
	public ArrayList<Integer> getEcrNodes() {
		return ecrNodes;
	}

	/**
	 * @param ecrNodes the ecrNodes to set
	 */
	public void setEcrNodes(ArrayList<Integer> ecrNodes) {
		this.ecrNodes = ecrNodes;
	}
	
	
	
	
}

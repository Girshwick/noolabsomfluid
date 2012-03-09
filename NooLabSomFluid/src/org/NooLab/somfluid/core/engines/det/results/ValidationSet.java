package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;
import java.util.ArrayList;



public class ValidationSet  implements Serializable{

	private static final long serialVersionUID = -5194957520207952879L;
	 
	
	int casesCount;
	int truePositives;
	int trueNegatives;

	int falsePositives;
	int falseNegatives;
	
	RoC roc = new RoC();

	int observationCount;

	// some measures that describe the SOM as far as tohse nodes are concerned which 
	// satisfy the ecr criterion, it is a particular point like the singularity TP @ FP=0
	int ecrTP ;
	int ecrFP  ;
	double ecrRelSize ;
	double ecrRelTP ;
	double ecrRelRisk ;
	
	// related to the ecr measures: the singularity
	double tpSingularity ;
	
	public ArrayList<Integer> ecrNodes;
	
	
	// ------------------------------------------------------------------------
	public ValidationSet(){
		
	}
	// ------------------------------------------------------------------------
	

	public int getCasesCount() {
		return casesCount;
	}

	public void setCasesCount(int casesCount) {
		this.casesCount = casesCount;
	}

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}

	public int getTrueNegatives() {
		return trueNegatives;
	}

	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}

	public int getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}


	public RoC getRoc() {
		return roc;
	}


	public void setRoc(RoC roc) {
		this.roc = roc;
	}


	public int getObservationCount() {
		return observationCount;
	}


	public void setObservationCount(int observationCount) {
		this.observationCount = observationCount;
	}


	public ArrayList<Integer> getEcrNodes() {
		return ecrNodes;
	}


	public void setEcrNodes(ArrayList<Integer> ecrNodes) {
		this.ecrNodes = ecrNodes;
	}


	public int getEcrTP() {
		return ecrTP;
	}


	public void setEcrTP(int ecrTP) {
		this.ecrTP = ecrTP;
	}


	public int getEcrFP() {
		return ecrFP;
	}


	public void setEcrFP(int ecrFP) {
		this.ecrFP = ecrFP;
	}


	public double getEcrRelSize() {
		return ecrRelSize;
	}


	public void setEcrRelSize(double ecrRelSize) {
		this.ecrRelSize = ecrRelSize;
	}


	public double getEcrRelTP() {
		return ecrRelTP;
	}


	public void setEcrRelTP(double ecrRelTP) {
		this.ecrRelTP = ecrRelTP;
	}


	public double getEcrRelRisk() {
		return ecrRelRisk;
	}


	public void setEcrRelRisk(double ecrRelRisk) {
		this.ecrRelRisk = ecrRelRisk;
	}


	public double getTpSingularity() {
		return tpSingularity;
	}


	public void setTpSingularity(double tpSingularity) {
		this.tpSingularity = tpSingularity;
	}

}

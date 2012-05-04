package org.NooLab.somscreen;

import java.io.Serializable;



public class SomQualityData implements Serializable{

	private static final long serialVersionUID = 6688292830863661641L;

	
	int targetMode ;
	double score = 1000.0;

	double ecr ;


	// result parameters from classification
	int tp, fn , fp ,tn,ccases, samplesize;
	double rocAuC, tpSingularity , ecrRelTP;
	
	double tpsqRisk=1.0;
	double tpfpRisk=1.0;
	
	// ------------------------------------------------------------------------
	public SomQualityData(){
		
	}
	
	public SomQualityData(SomQualityData iSqData) {

		if (iSqData==null){
			return;
		}
		targetMode = iSqData.targetMode ;
		score = iSqData.score ;
		
		// result parameters from classification
		tp = iSqData.tp ; 
		fn = iSqData.fn ;
		fp = iSqData.fp ;
		tn = iSqData.tn ;
		ccases = iSqData.ccases ;
		samplesize = iSqData.samplesize ;
		
		rocAuC = iSqData.rocAuC ;
		tpSingularity = iSqData.tpSingularity ;
		ecrRelTP = iSqData.ecrRelTP ;
		
		ecr = iSqData.ecr ;

	}

	// ------------------------------------------------------------------------
	

	/**
	 * @return the targetMode
	 */
	public int getTargetMode() {
		return targetMode;
	}

	/**
	 * @param targetMode the targetMode to set
	 */
	public void setTargetMode(int targetMode) {
		this.targetMode = targetMode;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return the tp
	 */
	public int getTp() {
		return tp;
	}

	/**
	 * @param tp the tp to set
	 */
	public void setTp(int tp) {
		this.tp = tp;
	}

	/**
	 * @return the fn
	 */
	public int getFn() {
		return fn;
	}

	/**
	 * @param fn the fn to set
	 */
	public void setFn(int fn) {
		this.fn = fn;
	}

	/**
	 * @return the fp
	 */
	public int getFp() {
		return fp;
	}

	/**
	 * @param fp the fp to set
	 */
	public void setFp(int fp) {
		this.fp = fp;
	}

	/**
	 * @return the tn
	 */
	public int getTn() {
		return tn;
	}

	/**
	 * @param tn the tn to set
	 */
	public void setTn(int tn) {
		this.tn = tn;
	}

	/**
	 * @return the ccases
	 */
	public int getCcases() {
		return ccases;
	}

	/**
	 * @param ccases the ccases to set
	 */
	public void setCcases(int ccases) {
		this.ccases = ccases;
	}

	/**
	 * @return the samplesize
	 */
	public int getSamplesize() {
		return samplesize;
	}

	/**
	 * @param samplesize the samplesize to set
	 */
	public void setSamplesize(int samplesize) {
		this.samplesize = samplesize;
	}

	/**
	 * @return the rocAuC
	 */
	public double getRocAuC() {
		return rocAuC;
	}

	/**
	 * @param rocAuC the rocAuC to set
	 */
	public void setRocAuC(double rocAuC) {
		this.rocAuC = rocAuC;
	}

	/**
	 * @return the tpSingularity
	 */
	public double getTpSingularity() {
		return tpSingularity;
	}

	/**
	 * @param tpSingularity the tpSingularity to set
	 */
	public void setTpSingularity(double tpSingularity) {
		this.tpSingularity = tpSingularity;
	}

	/**
	 * @return the ecrRelTP
	 */
	public double getEcrRelTP() {
		return ecrRelTP;
	}

	/**
	 * @param ecrRelTP the ecrRelTP to set
	 */
	public void setEcrRelTP(double ecrRelTP) {
		this.ecrRelTP = ecrRelTP;
	}

	/**
	 * @return the ecr
	 */
	public double getEcr() {
		return ecr;
	}

	/**
	 * @param ecr the ecr to set
	 */
	public void setEcr(double ecr) {
		this.ecr = ecr;
	}

	 
	
}

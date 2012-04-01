package org.NooLab.somfluid.properties;

import java.io.Serializable;

public class OptimizerSettings  implements Serializable{

	
	private static final long serialVersionUID = -901599037282834920L;


	
	int maxStepsAbsolute = 17 ;
	
	/** this should be made dependent from number of variables  */
	int atLeastWithoutChange = 500 ; 
	
	double stopAtNormalizedQuality = 1.01 ;
	
	/** 
	 * this creates a sample such that one pass of the optimizer lasts apprx. [n] millis;</br>
	 * the required number of cases is guaranteed though, so it may last longer, if adaptive sampling is not activated
	 */
	boolean activeTimeLimitation = false;
	int timeLimitationPerStep = 8000 ; 
	
	/**   */
	int minimalNumberOfCases = 15 ;
	
	/**  the sample sizes might be different from main settings */
	double samplesRatio = 0.6;
	
	
	// ========================================================================
	public OptimizerSettings(ModelingSettings modelingSettings) {

	}
	// ========================================================================


	/**
	 * @return the maxStepsAbsolute
	 */
	public int getMaxStepsAbsolute() {
		return maxStepsAbsolute;
	}


	/**
	 * @param maxStepsAbsolute the maxStepsAbsolute to set
	 */
	public void setMaxStepsAbsolute(int maxStepsAbsolute) {
		this.maxStepsAbsolute = maxStepsAbsolute;
	}


	/**
	 * @return the atLeastWithoutChange
	 */
	public int getAtLeastWithoutChange() {
		return atLeastWithoutChange;
	}


	/**
	 * @param atLeastWithoutChange the atLeastWithoutChange to set
	 */
	public void setAtLeastWithoutChange(int atLeastWithoutChange) {
		this.atLeastWithoutChange = atLeastWithoutChange;
	}


	/**
	 * @return the stopAtNormalizedQuality
	 */
	public double getStopAtNormalizedQuality() {
		return stopAtNormalizedQuality;
	}


	/**
	 * @param stopAtNormalizedQuality the stopAtNormalizedQuality to set
	 */
	public void setStopAtNormalizedQuality(double stopAtNormalizedQuality) {
		this.stopAtNormalizedQuality = stopAtNormalizedQuality;
	}
	
	
}

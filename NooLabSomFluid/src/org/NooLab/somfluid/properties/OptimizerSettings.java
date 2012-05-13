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



	int maxAvgVariableVisits = 51;

	double durationHours = 25.0;

	boolean shortenedFirstCycleAllowed = true;
	
	
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


	public int getMaxAvgVariableVisits() {
		
		return maxAvgVariableVisits;
	}
	/**
	 * @param maxAvgVariableVisits the maxAvgVariableVisits to set
	 */
	public void setMaxAvgVariableVisits(int maxAvgVariableVisits) {
		this.maxAvgVariableVisits = maxAvgVariableVisits;
	}
	
	public void setDurationHours( double hoursFraction) {
		durationHours = hoursFraction;
	}
	
	
	/**
	 * @return the durationHours
	 */
	public double getDurationHours() {
		return durationHours;
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


	public boolean isShortenedFirstCycleAllowed() {
		return shortenedFirstCycleAllowed;
	}


	/**
	 * @param shortenedFirstCycleAllowed the shortenedFirstCycleAllowed to set
	 */
	public void setShortenedFirstCycleAllowed(boolean flag) {
		shortenedFirstCycleAllowed = flag;
	}


	public boolean isActiveTimeLimitation() {
		return activeTimeLimitation;
	}


	public void setActiveTimeLimitation(boolean activeTimeLimitation) {
		this.activeTimeLimitation = activeTimeLimitation;
	}


	public int getTimeLimitationPerStep() {
		return timeLimitationPerStep;
	}


	public void setTimeLimitationPerStep(int timeLimitationPerStep) {
		this.timeLimitationPerStep = timeLimitationPerStep;
	}


	public int getMinimalNumberOfCases() {
		return minimalNumberOfCases;
	}


	public void setMinimalNumberOfCases(int minimalNumberOfCases) {
		this.minimalNumberOfCases = minimalNumberOfCases;
	}


	public double getSamplesRatio() {
		return samplesRatio;
	}


	public void setSamplesRatio(double samplesRatio) {
		this.samplesRatio = samplesRatio;
	}
	
	
}

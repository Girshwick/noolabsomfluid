package org.NooLab.somtransform.algo.distribution;

import java.io.Serializable;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class DistributionBin implements Serializable{

	private static final long serialVersionUID = -8190329681389337800L;

	
	int index = -1;
	
	int count = 0 ;
	
	double variance = -1;
	double stdev = -1;
	double mean = -1;

	double min = 0.0;
	double max = 0.0 ;

	boolean isActive=true;
	
	// ========================================================================
	public DistributionBin(){
		
	}
	// ========================================================================

	public void importStatsDescription(SummaryStatistics binstats) {
		 
		   
		count = (int) binstats.getN() ;
			
		variance = binstats.getVariance()  ;
		stdev = binstats.getStandardDeviation()  ;
		mean = binstats.getMean()  ;

		min = binstats.getMin()  ;
		max = binstats.getMax()  ;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	public void setObservationCount(int n) {
		count=n;
	}

	

	public double getStdev() {
		return stdev;
	}

	public void setStdev(double stdev) {
		this.stdev = stdev;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	
	
}

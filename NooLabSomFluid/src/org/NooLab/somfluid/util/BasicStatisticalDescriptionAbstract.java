package org.NooLab.somfluid.util;

import java.util.ArrayList;

public abstract class BasicStatisticalDescriptionAbstract {

	int count, mvCount, x_counter;

	double sum, qsum, qqsum, mean, genMeanP2, median, geoMean, adjMean, // adjusted
																		// mean
																		// :
																		// mean
																		// from
																		// (arithmet
																		// +
																		// harmonic)/2
			cov, soval, // sum of values, needed for merging
			sovar, // sum of variances
			invsum, // sum of inverses (kehrwert) for harmonic means
			variance, autocorr, mini, maxi, skewness, kurtosis;

	
	ArrayList<Double> invalues = new ArrayList<Double>();

	protected int removedValues;

	protected int needForRecalc;
	
	// ========================================================================
	public BasicStatisticalDescriptionAbstract() {

	}
	// ========================================================================
	
	
	
	/**
	 * instead of calling all the single methods one after another, we introduce
	 * it here in a single step
	 */
	public void introduceValue(double fieldValue) {

		if (fieldValue == -1.0) { // TODO: not perfectly correct for the general
									// case,
									// in this form, it expects normalized data
									// ;
									// we should use a flag whether data a
									// "raw", and a reference to an instance of
									// an MV object
			mvCount++;
			return;
		}

		count = count + 1;
		invalues.add(fieldValue);

		sum = sum + fieldValue;
		qsum = qsum + (double) (fieldValue * fieldValue);

		qqsum = qqsum + (double) (qsum * qsum); // for kurtosis

		if (fieldValue > 0) {
			invsum = invsum + 1 / (fieldValue);
		}

		mean = sum / count;

		if (invsum > 0) {
			geoMean = count / invsum;
		} else {
			geoMean = 0;
		}

		genMeanP2 = Math.sqrt(qqsum / count);

		if (geoMean > 0) {
			adjMean = (geoMean + mean) / 2.0;
		} else {
			adjMean = 0;
		}

		if (count == 1) {
			mini = fieldValue;
			maxi = fieldValue;
		} else {
			if (mini > fieldValue) {
				mini = fieldValue;
			}
			if (maxi < fieldValue) {
				maxi = fieldValue;
			}
		}

		if (count >= 3) {
			variance = lazyvariance(sum, qsum, count);
		} else {
			variance = -1;
		}

	}

	

	public void removeValue(double fieldValue) {
		
		
		if (fieldValue == -1.0){
			mvCount--;
			return;
		}
		
		count = count -1 ;
		if (count>0){
			mean = sum / count;
		}else{
			mean = -1;
		}
		
		if (fieldValue>0){
			invsum = invsum -1/fieldValue;
		}
		
		if (invsum>0){
			geoMean = count/invsum;
		}else{
			geoMean = 0;
		}
		
		if (geoMean >0){
			adjMean = (geoMean + mean)/2.0 ; 
		}else{
			adjMean = 0;
		}
		
		
		qsum = qsum - (double)(fieldValue * fieldValue);
		
		qqsum = qqsum - (double)(qsum*qsum); // for kurtosis

		
		
		if (count>=3){
			variance = lazyvariance(sum,qsum,count);
		}else{
			variance = -1;
		}
		
		removedValues++;
		
		if ( (removedValues > (count*0.8)) || (mini == fieldValue) || (maxi == fieldValue)){
			needForRecalc++;
		}
		
	}


	
	
	protected double lazyvariance(double sum, double sqsum, int n) {
		return sqsum / n - (sum / n) * (sum / n);
	}

}

package org.NooLab.somfluid.core.categories.extensionality;

import java.util.ArrayList;

import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;

public class BasicSimpleStatisticalDescription implements BasicStatisticalDescriptionIntf{

	String description = "";
	String title = "" ;
	int needForRecalc = 0;
	int removedValues = 0;
	
	
	boolean  initialized = false;
	
	int      count, mvCount, x_counter;

	double   sum, qsum, qqsum, mean, genMeanP2, median, geoMean, 
			 adjMean,  // adjusted mean : mean from (arithmet + harmonic)/2 
			 cov, soval, // sum of values, needed for merging
			 sovar, // sum of variances
			 invsum , // sum of inverses (kehrwert) for harmonic means
			 variance, autocorr, mini, maxi, skewness, kurtosis;

	
	ArrayList<Double> invalues = new ArrayList<Double>();
	
	// ========================================================================
	public BasicSimpleStatisticalDescription(){
		
	}
	// ========================================================================
	
	public void removeValue(double v) {
		// TODO Auto-generated method stub
		
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getNeedForRecalc() {
		return needForRecalc;
	}

	public void setNeedForRecalc(int needForRecalc) {
		this.needForRecalc = needForRecalc;
	}

	public int getRemovedValues() {
		return removedValues;
	}

	public void setRemovedValues(int removedValues) {
		this.removedValues = removedValues;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getMvCount() {
		return mvCount;
	}

	public void setMvCount(int mvCount) {
		this.mvCount = mvCount;
	}

	public int getX_counter() {
		return x_counter;
	}

	public void setX_counter(int x_counter) {
		this.x_counter = x_counter;
	}

	public double getSum() {
		return sum;
	}

	public void setSum(double sum) {
		this.sum = sum;
	}

	public double getQsum() {
		return qsum;
	}

	public void setQsum(double qsum) {
		this.qsum = qsum;
	}

	public double getQqsum() {
		return qqsum;
	}

	public void setQqsum(double qqsum) {
		this.qqsum = qqsum;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getGenMeanP2() {
		return genMeanP2;
	}

	public void setGenMeanP2(double genMeanP2) {
		this.genMeanP2 = genMeanP2;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public double getGeoMean() {
		return geoMean;
	}

	public void setGeoMean(double geoMean) {
		this.geoMean = geoMean;
	}

	public double getAdjMean() {
		return adjMean;
	}

	public void setAdjMean(double adjMean) {
		this.adjMean = adjMean;
	}

	public double getCov() {
		return cov;
	}

	public void setCov(double cov) {
		this.cov = cov;
	}

	public double getSoval() {
		return soval;
	}

	public void setSoval(double soval) {
		this.soval = soval;
	}

	public double getSovar() {
		return sovar;
	}

	public void setSovar(double sovar) {
		this.sovar = sovar;
	}

	public double getInvsum() {
		return invsum;
	}

	public void setInvsum(double invsum) {
		this.invsum = invsum;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getAutocorr() {
		return autocorr;
	}

	public void setAutocorr(double autocorr) {
		this.autocorr = autocorr;
	}

	public double getMini() {
		return mini;
	}

	public void setMini(double mini) {
		this.mini = mini;
	}

	public double getMaxi() {
		return maxi;
	}

	public void setMaxi(double maxi) {
		this.maxi = maxi;
	}

	public double getSkewness() {
		return skewness;
	}

	public void setSkewness(double skewness) {
		this.skewness = skewness;
	}

	public double getKurtosis() {
		return kurtosis;
	}

	public void setKurtosis(double kurtosis) {
		this.kurtosis = kurtosis;
	}

	public ArrayList<Double> getInvalues() {
		return invalues;
	}

	public void setInvalues(ArrayList<Double> invalues) {
		this.invalues = invalues;
	}

	@Override
	public void clear() {
		
		invalues.clear() ;
	}

	@Override
	public void introduceValue(double fieldValue) {
		// 
		invalues.add(fieldValue);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void introduceValues(ArrayList<Double> fieldValues) {
		
		if ((fieldValues!=null) && (fieldValues.size()!=0)){
			invalues.addAll(fieldValues);
		}
	}
	
}

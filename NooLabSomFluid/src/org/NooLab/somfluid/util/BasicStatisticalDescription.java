package org.NooLab.somfluid.util;

import java.io.Serializable;

/**
 * 
 * simply a container for calculated values of an basic entity like a column: NO
 * methods to calculate anything...
 * 
 * it is part of columns
 * 
 */
public class BasicStatisticalDescription implements Serializable {

	private static final long serialVersionUID = -6837594325966248821L;

	BasicStatisticalDescription bsd;
	
	int histogramResolution = 100 ;
	int polynomialFitDegree = 5;
	
	boolean  initialized = false;
	
	int      count, mvCount, x_counter;

	double   sum, qsum, mean, median, geoMean, cov, soval, // sum of values, needed for merging
			 sovar, // sum of variances
			 variance, autocorr, mini, maxi, skewness, kurtosis;
	double[] modalPoints = new double[2] ;

	double[][] histogramValues;

	/** 
	 * this describes the polynomial fit to the histogram;
	 * this is important for the ability to learn what to do on which data
	 * 
	 */
	double[] histogramPolyfitCoefficients ;

	private boolean isTimeSeries = false;
	
	// ========================================================================
	public BasicStatisticalDescription(){
		reset();
		bsd = this;
	}
	// ========================================================================

	public BasicStatisticalDescription( BasicStatisticalDescription inStatsDescr ) {
		try{
		bsd = this;
		
		bsd.count = inStatsDescr.count ;
		bsd.mvCount = inStatsDescr.mvCount  ;
		
	
		bsd.sum= inStatsDescr.sum ;
		bsd.qsum= inStatsDescr.qsum ;
				
		bsd.mean=inStatsDescr.mean ;
		bsd.cov= inStatsDescr.cov ;
		
		bsd.soval = inStatsDescr.soval ;  // sum of values, needed for merging
		bsd.sovar = inStatsDescr.sovar ;  // sum of variances
		bsd.variance= inStatsDescr.variance ;
		bsd.autocorr = inStatsDescr.autocorr ;
		bsd.mini= inStatsDescr.mini ;
		bsd.maxi= inStatsDescr.maxi ;  
		bsd.median= inStatsDescr.median ;
		bsd.skewness = inStatsDescr.skewness ;
		bsd.kurtosis = inStatsDescr.kurtosis ;
		bsd.geoMean = inStatsDescr.geoMean;
		
		if ((inStatsDescr.modalPoints!=null) && (inStatsDescr.modalPoints.length>0)){
			bsd.modalPoints = new double[inStatsDescr.modalPoints.length ] ;
			System.arraycopy( inStatsDescr.modalPoints, 0, bsd.modalPoints, 0, inStatsDescr.modalPoints.length) ;
		}
		// (Object src,int srcPos,Object dest,int destPosint length)

		if ((inStatsDescr.histogramValues!=null) && (inStatsDescr.histogramValues.length>0)){
			bsd.histogramValues = new double[2][inStatsDescr.histogramValues[0].length] ;
			System.arraycopy( inStatsDescr.histogramValues, 0, bsd.histogramValues, 0, inStatsDescr.histogramValues.length) ;
		}
		
		if ((inStatsDescr.histogramPolyfitCoefficients!=null) && (inStatsDescr.histogramPolyfitCoefficients.length>0)){
			bsd.histogramPolyfitCoefficients = new double[inStatsDescr.histogramPolyfitCoefficients.length] ;
			System.arraycopy( inStatsDescr.histogramPolyfitCoefficients, 0, bsd.histogramPolyfitCoefficients, 0, inStatsDescr.histogramPolyfitCoefficients.length) ;
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void reset() {
	
	
		count = 0;
		mvCount = 0 ;
		
	
		sum=0;
		qsum=0;
		
		median=0;
		mean=0;
		cov=0;
		
		soval =0;  // sum of values, needed for merging
		sovar =0;  // sum of variances
		variance=0;
		autocorr =0;
		mini=0;
		maxi=0;   
		
		skewness = 0; 
		kurtosis = 0;
			
		modalPoints = new double[2];
		modalPoints[0] = -1; modalPoints[1] = -1; 
		
		histogramValues = new double[2][100] ;

		histogramPolyfitCoefficients = new double[20] ;
		
		
	}

	public int getHistogramResolution() {
		return histogramResolution;
	}

	public void setHistogramResolution(int histogramResolution) {
		this.histogramResolution = histogramResolution;
	}

	public int getPolynomialFitDegree() {
		return polynomialFitDegree;
	}

	public void setPolynomialFitDegree(int polynomialFitDegree) {
		this.polynomialFitDegree = polynomialFitDegree;
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

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
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

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public void setTimeSeries(boolean isTimeSeries) {
		this.isTimeSeries = isTimeSeries;
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

	public double[] getModalPoints() {
		return modalPoints;
	}

	public void setModalPoints(double[] modalPoints) {
		this.modalPoints = modalPoints;
	}

	public double[][] getHistogramValues() {
		return histogramValues;
	}

	public void setHistogramValues(double[][] histogramValues) {
		this.histogramValues = histogramValues;
	}

	public double[] getHistogramPolyfitCoefficients() {
		return histogramPolyfitCoefficients;
	}

	public void setHistogramPolyfitCoefficients( double[] histogramPolyCoeff) {
		this.histogramPolyfitCoefficients = histogramPolyCoeff;
	}

	public boolean isTimeSeries() {
		 
		return isTimeSeries;
	}
	
	
	
	
	
}


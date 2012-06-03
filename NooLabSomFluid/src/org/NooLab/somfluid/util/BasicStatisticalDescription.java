package org.NooLab.somfluid.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somtransform.algo.distribution.EmpiricDistribution;

/**
 * 
 * simply a container for calculated values of an basic entity like a column: NO
 * methods to calculate anything...
 * 
 * it is part of columns
 * 
 * TODO: recalculation of statistics from within the extensionality container
 * 
 */
public class BasicStatisticalDescription implements Serializable {

	private static final long serialVersionUID = -6837594325966248821L;

	transient BasicStatisticalDescription bsd;
	String description = "";
	String title = "" ;
	int needForRecalc = 0;
	int removedValues = 0;
	
	int histogramResolution = 100 ;
	int polynomialFitDegree = 5;
	
	boolean  initialized = false;
	boolean  includeHistogram = false;
	
	int      count, mvCount, x_counter;

	double   sum, qsum, qqsum, mean, genMeanP2, median, geoMean, 
			 adjMean,  // adjusted mean : mean from (arithmet + harmonic)/2 
			 cov, soval, // sum of values, needed for merging
			 sovar, // sum of variances
			 invsum , // sum of inverses (kehrwert) for harmonic means
			 variance, autocorr, mini, maxi, skewness, kurtosis;
	
	double[] modalPoints = new double[3] ;

	double[][] histogramValues;

	EmpiricDistribution empiricDistribution;
	/** 
	 * this describes the polynomial fit to the histogram;
	 * this is important for the ability to learn what to do on which data
	 * 
	 */
	double[] histogramPolyfitCoefficients ;

	private boolean isTimeSeries = false;
	
	ArrayList<Double> invalues = new ArrayList<Double>();

	
	// ========================================================================
	public BasicStatisticalDescription(boolean includeHisto){
		includeHistogram = includeHisto;
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
		bsd.qsum = inStatsDescr.qsum ;
		bsd.invsum = inStatsDescr.invsum ;
		
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
		bsd.adjMean = inStatsDescr.adjMean;
		
		if ((inStatsDescr.modalPoints!=null) && (inStatsDescr.modalPoints.length>0)){
			bsd.modalPoints = new double[inStatsDescr.modalPoints.length ] ;
			System.arraycopy( inStatsDescr.modalPoints, 0, bsd.modalPoints, 0, inStatsDescr.modalPoints.length) ;
		}
		// (Object src,int srcPos,Object dest,int destPosint length)

		if ((inStatsDescr.histogramValues!=null) && (inStatsDescr.histogramValues.length>0)){
			bsd.histogramValues = new double[3][inStatsDescr.histogramValues[0].length] ;
			System.arraycopy( inStatsDescr.histogramValues, 0, bsd.histogramValues, 0, inStatsDescr.histogramValues.length) ;
		}
		
		if ((inStatsDescr.histogramPolyfitCoefficients!=null) && (inStatsDescr.histogramPolyfitCoefficients.length>0)){
			bsd.histogramPolyfitCoefficients = new double[inStatsDescr.histogramPolyfitCoefficients.length] ;
			System.arraycopy( inStatsDescr.histogramPolyfitCoefficients, 0, bsd.histogramPolyfitCoefficients, 0, inStatsDescr.histogramPolyfitCoefficients.length) ;
		}
		
		if ((bsd!=null) && (bsd.empiricDistribution!=null)){
			empiricDistribution = new EmpiricDistribution (bsd.empiricDistribution);
		}else{
			empiricDistribution = new EmpiricDistribution ();
		}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setData(ArrayList<Double> values) {
		introduceValues(values);
	}
	public void introduceValues(double[] values) {
		
		for (int i=0;i<values.length;i++){
			introduceValue(values[i]);
		}
	}
	
	public void introduceValues(ArrayList<Double> values) {
		
		for (int i=0;i<values.size();i++){
			introduceValue(values.get(i));
		}
		
		Collections.sort(invalues);
		int midpos = (int) (invalues.size()/2.0);
		if (invalues.size()>1){
			median = invalues.get(midpos);
		}
		
	}
	/** 
	 * instead of calling all the single methods one after another,
	 * we introduce it here in a single step 
	 */
	public void introduceValue(double fieldValue) {
		
		if (fieldValue == -1.0){ // TODO: not perfectly correct for the general case, 
								 // in this form, it expects normalized data ;
								 // we should use a flag whether data a "raw", and a reference to an instance of an MV object
			mvCount++;
			return;
		}
		
		count = count + 1;
		invalues.add( fieldValue );
		
		sum = sum + fieldValue;
		qsum = qsum + (double)(fieldValue * fieldValue);
		
		qqsum = qqsum + (double)(qsum*qsum); // for kurtosis
		
		if (fieldValue>0){
			invsum = invsum + 1/(fieldValue);
		}
		
		mean = sum/count;

		if (invsum>0){
			geoMean = count/ invsum;  
		}else{
			geoMean = 0;
		}
		
		genMeanP2 = Math.sqrt( qqsum/count) ;
 		
		if (geoMean >0){
			adjMean = (geoMean + mean)/2.0 ; 
		}else{
			adjMean = 0;
		}

		
		if (count==1){
			mini = fieldValue;
			maxi = fieldValue;
		}else{
			if (mini > fieldValue){
				mini = fieldValue;
			}
			if (maxi < fieldValue){
				maxi = fieldValue;
			}
		}
		
		if (count>=3){
			variance = lazyvariance(sum,qsum,count);
		}else{
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

	private double lazyvariance( double sum, double sqsum, int n){
		return  sqsum /n - (sum/n)*(sum/n) ;
	}
	
	
	public void reset() {
	
		adjMean = 0.0;
		count = 0;
		mvCount = 0 ;
		geoMean = 0.0;
		invsum = 0.0;
		sum=0;
		qsum=0;
		qqsum=0;
		median=0;
		mean=0;
		cov=0;
		
		soval =0;
		sovar =0;  // sum of variances (in case of the comparative statistics of vectors)
		variance=0;
		autocorr =0;
		mini=0;
		maxi=0;   
		
		skewness = 0; 
		kurtosis = 0;
			
		modalPoints = new double[2];
		modalPoints[0] = -1; modalPoints[1] = -1; 
		
		if (includeHistogram ){
			histogramValues = new double[3][100] ;
			histogramPolyfitCoefficients = new double[20] ;
		}
		empiricDistribution = new EmpiricDistribution(); 
		
		invalues.clear();
	}

	public void clear() {
		 
		empiricDistribution.clear();
		reset();
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

	public EmpiricDistribution getEmpiricDistribution() {
		return empiricDistribution;
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

	public void setTimeSeries(boolean isTimeSeries) {
		this.isTimeSeries = isTimeSeries;
	}

	public boolean isTimeSeries() {
		 
		return isTimeSeries;
	}

	public BasicStatisticalDescription getBsd() {
		return bsd;
	}

	public void setBsd(BasicStatisticalDescription bsd) {
		this.bsd = bsd;
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

	public double getQqsum() {
		return qqsum;
	}

	public void setQqsum(double qqsum) {
		this.qqsum = qqsum;
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

	public double getInvsum() {
		return invsum;
	}

	public void setInvsum(double invsum) {
		this.invsum = invsum;
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

	public void setEmpiricDistribution(EmpiricDistribution empiricDistribution) {
		this.empiricDistribution = empiricDistribution;
	}
	
	
	
	
	
}


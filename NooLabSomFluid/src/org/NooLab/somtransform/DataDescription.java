package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;



public class DataDescription implements 
											DataDescriptionIntf, 
											Serializable {

	private static final long serialVersionUID = 2950934875811217706L; 
	
	private boolean isComplete; 
	
	double mean   = -1.0;
	double max    = -1.0;
	double min    = -1.0;
	
	double median   = -1.0;
	
	double variance = -1.0 ;
	double coeffvar = -1.0 ;

	double kurtosis = -1.0 ;
	double skewness = -1.0 ;
	
	ArrayList<Double> quantiles = new ArrayList<Double>();
	double[] quantileSteps = new double[]{ 0.1, 0.5, 1.0, 5.0,10.0, 25.0, 50.0, 75.0, 90.0, 95.0, 99.0, 99.5, 99.9 } ; 
	
	ArrayList<Double> salientMinima = new ArrayList<Double>(); 
	ArrayList<Double> salientMaxima = new ArrayList<Double>();

	

	// histogram
	
	
	
	// ========================================================================
	public DataDescription(){
		
	}
	
	// for cloning
	public DataDescription(DataDescription tmplDescription) {
		
		isComplete = tmplDescription.isComplete  ; 
		
		mean   = tmplDescription.mean  ;
		max    = tmplDescription.max  ;
		min    = tmplDescription.min  ;
		
		median   = tmplDescription.median  ;
		
		variance = tmplDescription.variance  ;
		coeffvar = tmplDescription.coeffvar  ;

		kurtosis = tmplDescription.kurtosis  ;
		skewness = tmplDescription.skewness  ;
		
		quantiles = new ArrayList<Double>(tmplDescription.quantiles);
	}
	
	// ========================================================================

	/**
	 * @return the mean
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * @param mean the mean to set
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @return the median
	 */
	public double getMedian() {
		return median;
	}

	/**
	 * @param median the median to set
	 */
	public void setMedian(double median) {
		this.median = median;
	}

	/**
	 * @return the variance
	 */
	public double getVariance() {
		return variance;
	}

	/**
	 * @param variance the variance to set
	 */
	public void setVariance(double variance) {
		this.variance = variance;
	}

	/**
	 * @return the coeffvar
	 */
	public double getCoeffvar() {
		return coeffvar;
	}

	/**
	 * @param coeffvar the coeffvar to set
	 */
	public void setCoeffvar(double coeffvar) {
		this.coeffvar = coeffvar;
	}

	/**
	 * @return the kurtosis
	 */
	public double getKurtosis() {
		return kurtosis;
	}

	/**
	 * @param kurtosis the kurtosis to set
	 */
	public void setKurtosis(double kurtosis) {
		this.kurtosis = kurtosis;
	}

	/**
	 * @return the skewness
	 */
	public double getSkewness() {
		return skewness;
	}

	/**
	 * @param skewness the skewness to set
	 */
	public void setSkewness(double skewness) {
		this.skewness = skewness;
	}

	/**
	 * @return the quantiles
	 */
	public ArrayList<Double> getQuantiles() {
		return quantiles;
	}

	/**
	 * @param quantiles the quantiles to set
	 */
	public void setQuantiles(ArrayList<Double> quantiles) {
		this.quantiles = quantiles;
	}

	/**
	 * @return the quantileSteps
	 */
	public double[] getQuantileSteps() {
		return quantileSteps;
	}

	/**
	 * @param quantileSteps the quantileSteps to set
	 */
	public void setQuantileSteps(double[] quantileSteps) {
		this.quantileSteps = quantileSteps;
	}

	/**
	 * @return the salientMinima
	 */
	public ArrayList<Double> getSalientMinima() {
		return salientMinima;
	}

	/**
	 * @param salientMinima the salientMinima to set
	 */
	public void setSalientMinima(ArrayList<Double> salientMinima) {
		this.salientMinima = salientMinima;
	}

	/**
	 * @return the salientMaxima
	 */
	public ArrayList<Double> getSalientMaxima() {
		return salientMaxima;
	}

	/**
	 * @param salientMaxima the salientMaxima to set
	 */
	public void setSalientMaxima(ArrayList<Double> salientMaxima) {
		this.salientMaxima = salientMaxima;
	}



	public boolean isComplete() {
		 
		return isComplete;
	}



	/**
	 * @param isComplete the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	
	
	
}

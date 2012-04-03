package org.NooLab.somfluid.util;

import org.NooLab.utilities.strings.StringsUtil;

 





public class DescriptiveStatistics {

	
	double mean=0.0, variance=-1.0, cofv, min=-1.0,max=-1.0, modus=-1.0, skewness=-1.0, kurtosis=-1.0 ;
	
	double[] outliers = new double[0] ;
	
	// 5,25,50,75,95
	double[] quantils = new double[5];
	
	double[] distribution  = new double[0] ;
	int distrsupports = 41 ; 
	
	double missingvalue = -1.0 ;
	boolean excludeMV = true ;
	
	
	StringsUtil strgutil = new StringsUtil () ;
	
	public DescriptiveStatistics(){
		
	}
	
	
	
	public int calcDescriptiveStats( double[] values ){
		int result= -1;
		double sum=0.0, ssq=0.0, v, mi, mx; 
		int i, n, count=0 ;
		
		if (values.length<=0){
			return result;
		}
		
		mi =  9999999999999.0 ; 
		mx = -9999999999999.0 ;
		
		for(i=0;i<values.length;i++){
			
			sum = sum + values[i];
			
			ssq = ssq + (values[i]*values[i]);
			count++;
			
			if (mi>values[i]){
				mi = values[i] ;
			}
			if (mx<values[i]){
				mx = values[i] ;
			}
			
			
		} // i-> all values
		
		if (count > 0) {
			variance = lazyvariance(sum, ssq, count);
			mean = sum / count;

			min = mi;
			max = mx;
			result = 0;
			
		} else{
			result = -3 ;
		}
		return result ;
	}


	private double lazyvariance( double sum,
            					 double sqsum,
            					 int n){
		
		return  sqsum /n - (sum/n)*(sum/n) ;
	}
	
	
	private String convertToString( double value, int fracdigits ){
		
		String vStr = "" , hs1 ;
		
		if (fracdigits>=0){
			
			hs1 = String.format("%." + fracdigits + "f", value);
			 
			hs1 = strgutil.trimtrailingzeroes(hs1);

			vStr = hs1;
		}else{

			vStr = String.valueOf(min) ;
		}
		
		return vStr;
	}
	// ------------------------------------------------------------------------

	public DescriptiveStatistics clone(){
		DescriptiveStatistics ds = new DescriptiveStatistics();
		
		ds.cofv = cofv;
		
		ds.distrsupports = distrsupports;
		ds.excludeMV = excludeMV;
		ds.kurtosis = kurtosis;
		ds.max = max;
		ds.mean = mean;
		ds.min = min;
		ds.missingvalue = missingvalue;
		ds.modus = modus;
		ds.variance =  variance;
		ds.skewness =  skewness;
		
		if (outliers.length>0)
			ds.outliers = outliers.clone();
		if (quantils.length>0)
			ds.quantils = quantils.clone();
		if (distribution.length>0)
			ds.distribution = distribution.clone();
		
		return ds;
	}
	
	
	public double getMean() {
		return mean;
	}



	public void setMean(double mean) {
		this.mean = mean;
	}



	public double getVariance() {
		return variance;
	}

	public String getVariance( int fracdigits ) {
		String  vStr = "";
		
		vStr = convertToString(variance,fracdigits ) ;
	 		
		return vStr;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}



	public double getCofv() {
		return cofv;
	}



	public void setCofv(double cofv) {
		this.cofv = cofv;
	}



	public double getMin() {
		return min;
	}

	public String getMin( int fracdigits ) {
		String  vStr = "";
		
		vStr = convertToString(min,fracdigits ) ;
	 		
		return vStr;
	}
	
	public void setMin(double min) {
		this.min = min;
	}



	public double getMax() {
		return max;
	}

	public String getMax( int fracdigits ) {
		String  vStr = "";
		
		vStr = convertToString(max,fracdigits ) ;
	 		
		return vStr;
	}


	public void setMax(double max) {
		this.max = max;
	}



	public double getModus() {
		return modus;
	}



	public void setModus(double modus) {
		this.modus = modus;
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



	public double[] getQuantils() {
		return quantils;
	}



	public void setQuantils(double[] quantils) {
		this.quantils = quantils;
	}
	
	
	
	
	
}

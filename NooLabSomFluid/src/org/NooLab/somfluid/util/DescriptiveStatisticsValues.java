package org.NooLab.somfluid.util;

import java.io.Serializable;
import java.util.ArrayList;
 
import org.NooLab.utilities.strings.StringsUtil;

  


public class DescriptiveStatisticsValues implements Serializable {

	private static final long serialVersionUID = 5988059698557772886L;
	

	double mean=0.0, variance=-1.0, cofv, min=-1.0,max=-1.0, modus=-1.0, skewness=-1.0, kurtosis=-1.0 ;
	
	int modi[] = new int[0];
	int mindi[] = new int[0];
	
	// portion of bins filled with more than 1 value;
	double variety= -1.0;
	/** variety 95%-quantil : variety 75%-quantil */
	double boxRatio1 = -1.0; 
	/** variety 50%-quantil : variety 75%-quantil */
	double boxRatio2 = -1.0;
	/** absolute size of 95% quantil */
	private double boxRatio3;
	
	double[] outliers = new double[0] ;
	
	// 5,25,50,75,95
	double[] quantils = new double[5];
	
	double[] distribution  = new double[0] ;
	int distrsupports = 41 ; 
	
	double missingvalue = -1.0 ;
	boolean excludeMV = true ;
	
	// a manifold description of the histogram interpreted as a function and its fitting 
	private ArrayList<FDescription> fDescriptions = new ArrayList<FDescription>();
	
	
	// ..........................................
	transient StringsUtil strgutil = new StringsUtil () ;


	int[] sumOnSplit = new int[2];


	double sumosRatio = 0.0;


	
	
	// ========================================================================
	public DescriptiveStatisticsValues(){
		
	}
	// ========================================================================	
	
	
	public void clear() {
		fDescriptions.clear() ;
		
		
	}


	// ------------------------------------------------------------------------
	
	public DescriptiveStatisticsValues clone(){
		DescriptiveStatisticsValues ds = new DescriptiveStatisticsValues();
		
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


	public int calcDescriptiveStats( double[] values ){
		int result= -1;
		double sum=0.0, ssq=0.0,  mi, mx; 
		int i,  count=0 ;
		
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
	
	public FDescription getDescriptionItemByDescriptor(String str, int relativeIndex) {
		FDescription fd = null, fd0;
		int z=0;
		
		for (int i=0;i< fDescriptions.size();i++){
			
			fd0 = fDescriptions.get(i) ;
			if (fd0.getName().contentEquals(str)){
				fd=fd0;
				if (z==relativeIndex){
					break;
				}
				z++;
				
			}
		} // i->
		
		return fd;
	}	
	
	// ------------------------------------------------------------------------

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
	
	public double[] getOutliers() {
		return outliers;
	}



	public void setOutliers(double[] outliers) {
		this.outliers = outliers;
	}



	public double[] getDistribution() {
		return distribution;
	}



	public void setDistribution(double[] distribution) {
		this.distribution = distribution;
	}



	public void setDistribution(int[] values) {
		double[] distribution ;
		
		if (values==null){
			distribution = new double[100];
		}else{
			distribution = new double[values.length];
		}
		
		if (values!=null){
			for (int i = 0; i < distribution.length; i++) {
				distribution[i] = values[i];
			}
		}
		
	}


	public int getDistrsupports() {
		return distrsupports;
	}



	public void setDistrsupports(int distrsupports) {
		this.distrsupports = distrsupports;
	}



	public double getMissingvalue() {
		return missingvalue;
	}



	public void setMissingvalue(double missingvalue) {
		this.missingvalue = missingvalue;
	}



	public boolean isExcludeMV() {
		return excludeMV;
	}



	public void setExcludeMV(boolean excludeMV) {
		this.excludeMV = excludeMV;
	}



	public StringsUtil getStrgutil() {
		return strgutil;
	}



	public void setStrgutil(StringsUtil strgutil) {
		this.strgutil = strgutil;
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


	public void setfDescriptions(ArrayList<FDescription> fDescriptions) {
		this.fDescriptions = fDescriptions;
	}


	public ArrayList<FDescription> getfDescriptions() {
		return fDescriptions;
	}


	public int[] getModi() {
		return modi;
	}


	public void setModi(int[] modis) {
		modi = new int[modis.length] ;
		System.arraycopy(modis, 0, modi, 0, modis.length) ;
	}
	
	public void setMindi(int[] mindis) {
		mindi = new int[mindis.length] ;
		System.arraycopy(mindis, 0, mindi, 0, mindis.length) ;
	}


	public double getVariety() {
		return variety;
	}


	public void setVariety(double variety) {
		this.variety = variety;
	}


	public int[] getSumOnSplit() {
		return sumOnSplit;
	}


	public void setSumOnSplit(int[] sumOnSplit) {
		this.sumOnSplit = sumOnSplit;
	}


	public double getBoxRatio1() {
		return boxRatio1;
	}


	public void setBoxRatio1(double boxRatio1) {
		this.boxRatio1 = boxRatio1;
	}


	public double getBoxRatio2() {
		return boxRatio2;
	}


	public void setBoxRatio2(double boxRatio ) {
		this.boxRatio2 = boxRatio ;
	}


	public double getBoxRatio3() {
		 
		return boxRatio3;
	}


	public void setBoxRatio3(double boxRatio) {
		this.boxRatio3 = boxRatio ;
		
	}


	public double getSumosRatio() {
		return sumosRatio;
	}


	public void setSumosRatio(double sumosRatio) {
		this.sumosRatio = sumosRatio;
	}


	public int[] getMindi() {
		return mindi;
	}


	 
	
}


package org.NooLab.somfluid.util;

import java.util.ArrayList;

import org.NooLab.somfluid.components.MissingValues;
import org.NooLab.somtransform.algo.distribution.EmpiricDistribution;
import org.NooLab.utilities.strings.ArrUtilities;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;

import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction;
import flanagan.analysis.Stat;




public class BasicStatistics {

	ArrayList<Double> values;
	
	BasicStatisticalDescription statisticalDescription;
	
	MissingValues missingValues;
	
	int startIndex = -1, endIndex = -1;

	
	// ========================================================================
	public BasicStatistics( BasicStatisticalDescription statsValuesObj, MissingValues mv, ArrayList<Double> values,int startindex, int endindex){
		this.values  = values;
		
		setBlock( startindex, endindex );
		
		statisticalDescription = statsValuesObj;
		missingValues = mv ;
	}

	public BasicStatistics( BasicStatisticalDescription statsValuesObj, MissingValues mv, ArrayList<Double> values){
		
		this.values  = values;
		startIndex = 0; 
		endIndex = values.size()-1;
		
		statisticalDescription = statsValuesObj;
		missingValues = mv ;
	}
	// ========================================================================
	
	
	public void setData(ArrayList<Double> values){
		this.values  = values;
		startIndex = 0; 
		endIndex = values.size()-1;

	}
	
	// for relational data
	public void setData(ArrayList<Double> values,int startindex, int endindex){
		this.values  = values;
		startIndex = startindex; 
		endIndex = endindex;
		
		if (startIndex<0)startIndex=0;
		if (endIndex > values.size()-1)endIndex = values.size()-1;
	}
	
	public void setBlock( int startindex, int endindex ){
		startIndex = startindex; 
		endIndex = endindex;
		
		if (startIndex<0)startIndex=0;
		if (endIndex > values.size()-1)endIndex = values.size()-1;
	}
	
	public void calculate() {
		
		// flanagan.analysis.Stat for static methods for higher stats
		BasicStatisticalDescription sd;
		int z=0;
		double dv;
		double[] dvs ;
		ArrayList<Double> dvsList = new ArrayList<Double>(); 
		Regression reg ;
		
		sd = statisticalDescription;
		
		sd.sum = 0;
		sd.qsum = 0;
		sd.mini = 9999999999999999.9;
		sd.maxi = -9999999999999999.9;
		
	 
		
		// note that cell values do NEVER contain the header
		for (int i=startIndex; i<=endIndex; i++ ){
			
			dv = values.get(i); 
			if ( isMissingValue( dv )  ){
				sd.mvCount++;
				continue;
			} 

			
			sd.sum = sd.sum + dv; 
			sd.qsum = sd.qsum + (dv*dv) ; 
			
			if (sd.mini>dv) sd.mini=dv;
			if (sd.maxi<dv) sd.maxi=dv;

			dvsList.add( dv );
			z++;
		} // i -> all values
		
		// dvs now contains only value != MV !!!
		dvs = (double[])ArrUtilities.changeArraystyle(dvsList);
			  dvsList.clear();dvsList.trimToSize() ;dvsList=null;
		
		if (z<=0){
			return;
		}
		// shorten dvs if necessary
		
		sd.mean = sd.sum/z;
		sd.variance = lazyvariance( sd.sum, sd.qsum,z);
		
		if (sd.mean!=0){
			sd.cov = sd.variance/sd.mean;
		}else{
			sd.cov = 0;
		}
		// Stat is from flanagan library ...
		sd.geoMean = Stat.harmonicMean(dvs) ;
		sd.median = Stat.median( dvs);
		sd.kurtosis = Stat.kurtosis(dvs) ;
		sd.skewness = Stat.medianSkewness(dvs) ;
		
		double binWidth = (sd.maxi - sd.mini)/sd.histogramResolution;
		if (binWidth >0){
			
			
			// [0] = bins
			// [1] = data as frequs
			// [2] = relative frequs
			
			int n = dvs.length ;
			double[][] _histogramValues = Stat.histogramBins(dvs, binWidth) ;
			
			System.arraycopy(_histogramValues[0], 0, sd.histogramValues[0], 0, sd.histogramValues[0].length);
			System.arraycopy(_histogramValues[1], 0, sd.histogramValues[1], 0, sd.histogramValues[0].length);
			
			// count 0's, values different from zero's , longest sequence of zero's and non-zero's , mean and stdev of that
			int vn = Math.min( _histogramValues[0].length, sd.histogramValues[0].length) ;
			// relative values for histogram for further calculations
			for (int i=0;i<vn;i++){
				sd.histogramValues[2][i] = (_histogramValues[1][i]/((double)(n))) ;
				
			}
			
			
			EmpiricDistribution ed = sd.empiricDistribution ;
			ed.setBinFrequency( sd.histogramValues[1] );
			ed.describeHistogramAsSeries();
			
			ed.identifySalientBins(1); // maxima 
			// ed.setBinFrequency(sd.histogramValues[1]);
			// ed.importStatsDescription( statisticalDescription.);// StatisticalSummary samplestats);
			// ??? StatisticalSummary samplestats
			ed.importParentReference(this.statisticalDescription) ;
			
			ed.describe() ;
			
			// get polynomial fit
			 
			// reg = new Regression(sd.histogramValues); = reg.bestPolynomial();
		}
		
		if (sd.isTimeSeries()){
			// autocorrelation etc.
		}
	}


	private double lazyvariance( double sum, double sqsum, int n){
		return  sqsum /n - (sum/n)*(sum/n) ;
	}
	
	private boolean isMissingValue(double value) {
		boolean rB=false;
		/* TODO
		 * this refers to the local MV, whether the data are already normalized, and
		 * to the global MissingValues object
		 */
		
		if (value==-1.0){
			rB=true;
		}
		return rB;
	}
}




//Class to evaluate the function y = a + b.exp(-c.x) + f^z
class FunctTwo implements RegressionFunction{

  private double b = 0.0D;

  public double function(double[] p, double[] x){
           double y = p[0] + b*Math.exp(-p[1]*x[0]) + Math.pow(p[2], x[1]);
           return y;
  }

  public void setB(double b){
     this.b = b;
  }
}











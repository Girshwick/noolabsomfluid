package org.NooLab.somtransform.algo;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import jsc.curvefitting.*;
import jsc.datastructures.PairedData;
  
import org.NooLab.somfluid.mathstats.FourierTransform;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.optimization.fitting.HarmonicFitter;
import org.apache.commons.math.optimization.fitting.HarmonicFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;


import org.NooLab.math3.stat.descriptive.DescriptiveStatistics;
import org.NooLab.somfluid.util.DescriptiveStatisticsValues;
import org.NooLab.somfluid.util.FDescription;
import org.NooLab.somtransform.algo.distribution.DistributionBin;
import org.NooLab.somtransform.algo.distribution.DistributionBins;
import org.NooLab.somtransform.algo.distribution.Distributions;
import org.NooLab.utilities.ArrUtilities;



/**
 * 
 * a container that describes a serie of values from the perspective of
 * an empirical distribution;
 * 
 * 
 * 
 * 
 */
public class EmpiricDistribution  implements Serializable, EmpiricDistributionIntf{

	private static final long serialVersionUID = 248344256355482790L;
	
	transient Distributions distributions;
	
	DistributionBins bins;

	private int[] frequencies;
	
	// arrays for coefficients of 3 different types of function fittings
	// the idea is to use these coefficients, their statistical description, and the relative error (+relative variance)
	// to create a fingerprint, that can be used to decide on the transformation of the data
	
	// for each type, we have three versions : global, first part up to 50% of data (NOT the quantile), around 50% quantile  
	 
	
	int count = 0 ;
	
	// these values refer to the data!
	double variance = -1;
	double stdev = -1;
	double mean = -1;
	double modus = -1;
	double median = -1;
	
	double min = 0.0;
	double max = 0.0 ;
	
	// ...while these values refer to the values of the histogram == 2nd order description
	// the object is our own container for values, it does not provide any calc method...
	DescriptiveStatisticsValues histoStats = new DescriptiveStatisticsValues ();
	DescriptiveStatistics descriptiveStats = new DescriptiveStatistics();
	
	
	boolean variableIsNominal = false;
	boolean variableIsBinary = false;
	
	// volatile stuff 
	int medianPos=-1;
	int zeroCount = 0;
	int lastZeroBinPosition =-1,lastNonZeroBinPosition=-1;
	
	
	
	ArrayList<Integer> salientBins = new ArrayList<Integer>();
	EmpiricDistribution dis;
	
	ArrUtilities arrutil = new ArrUtilities() ;
	
	// ========================================================================
	public EmpiricDistribution(Distributions distributions){
		dis = this;
		this.distributions = distributions; 
	}
	public EmpiricDistribution(){
		dis = this;
	}

	
	// for cloning
	public EmpiricDistribution(EmpiricDistribution ed) {

		variance = ed.variance  ;
		stdev    = ed.stdev  ;
		mean     = ed.mean  ;
		modus    = ed.modus  ;
		median   = ed.median  ;
		
		min      = ed.min  ;
		max      = ed.max  ;	
	}
	// ========================================================================
	

	public void importStatsDescription(StatisticalSummary samplestats) {
		 
		   
		count = (int) samplestats.getN() ;
			
		variance = samplestats.getVariance()  ;
		stdev = samplestats.getStandardDeviation()  ;
		mean = samplestats.getMean()  ;

		min = samplestats.getMin()  ;
		max = samplestats.getMax()  ;
	}

	/**
	 * this is the powerful core: we will fit three different types of analytic kernels,
	 * and also measure some characteristic points about it (min,max, periodicity, number of zeroes, spatial distribution of zeroes); 
	 * then we give a probablistic estimation about the number scale (binary, ord, real ) and 
	 * the type (=possible actions) of the distribution
	 *
	 * all of that we may use to train a Som that selects a particular transformation for a particular distribution
	 */
	public void describe() {
		int n = this.count ;
		double v;
		boolean hb=false;
		double pquant[] = new double[4];
		
		// [116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 107, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 138, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 106, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117]
		n = salientBins.size() ;
		 
		descriptiveStats.addValues( frequencies );
		
		histoStats.setKurtosis( descriptiveStats.getKurtosis() );
		histoStats.setDistribution( frequencies );
		histoStats.setSkewness( descriptiveStats.getSkewness() ) ;
		
		histoStats.setMean(descriptiveStats.getMean());
		histoStats.setMin(descriptiveStats.getMin());
		histoStats.setMax(descriptiveStats.getMax());
		histoStats.setVariance(descriptiveStats.getVariance());
		
		pquant[0] = descriptiveStats.getPercentile(95);
		pquant[1] = descriptiveStats.getPercentile(75);
		pquant[2] = descriptiveStats.getPercentile(95);
		pquant[3] = descriptiveStats.getPercentile(50);
		
		if ((pquant[0]>0) && (pquant[1]>0)){
			v = pquant[1]/pquant[0] ;
			histoStats.setBoxRatio1(v);
		}
		if ((pquant[2]>0) && (pquant[3]>0)){
			v = pquant[2]/pquant[3] ;
			histoStats.setBoxRatio2(v);
		}
		
		for (int i=0;i<frequencies.length;i++){
			if (frequencies[i] == histoStats.getMax()){
				v = ((double)i)*((max-min)/100.0) ;
				histoStats.setModus(i);
				modus = v;
				break;
			}
		}
		
		MaxPositions mxp = new MaxPositions();
		histoStats.setModi( mxp.setData(frequencies).identifyModi( 0.27, 11 , 21 ) ) ; // parameters: contrast as a fraction of mean, window size, max n
		histoStats.setMindi(mxp.setData(frequencies).identifyMindi(0.27, 11 , 21 ) ) ; // parameters: contrast as a fraction of mean, window size, max n
		
		
		
		int runsum=0; medianPos=0;
		for (int i=0;i<frequencies.length;i++){
			runsum = runsum + frequencies[i];
			if (runsum > ((double)count)*0.5){
				medianPos=i;
				break ;
			}
		}
		if (medianPos>0){
			double v1,v2;
			v1 = ((double)medianPos)*((max-min)/100.0) ;
			v2 = ((double)(medianPos-1))*((max-min)/100.0) ;
			median = (v1+v2)/2.0;
		}
		
		// describe sparse tails on both sides, but for each side separately...
		// params: % of data, %of max.rel.freq per bin, % of #bins that are nearly empty (<70),
		// introduce a subclass that holds these params
		
		
		
		// ............................................
		/* 1. spline
		   2. fitting ... polynomial, exp,  
		   3. derivatives
		   org.apache.commons.math3.analysis.polynomials -> provides derivative 
		*/ 
		
		// creating a list of FDescription-s, which comprise the results of fitting
		calculateFitCoefficients();
		
		hb = variableIsNominal();
		
		// creating a fingerprint from those values for classification
		createDescripriveFingerPrint( hb ) ;
		
		
		variableIsNominal = hb;
		
		
		
	}
	 
	
	
	/**
	 * this method uses msome of the measurements for a judgment whether the 
	 * variable = its frequency distibution could be due a nominal scaled origin
	 * 
	 * - modi: difference between their positions ~ constant ?
	 *         first and last element of frequencies a modus?
	 * -
	 * 
	 * @return
	 */
	private boolean variableIsNominal() {
		boolean isNominal = false;
		int[] modi = histoStats.getModi() ;
		int dp=-1,p,p1,p2,mp;
		int dpi,dpx,dpif,dpxf;
		double dpr=-1;
		int nomScore=0;
		Vector<Integer> dlist = new Vector<Integer> (); 
		
		// check mean of differences in modi positions, is it int, or almost an int
		p=0;
		
		for (int i=0;i<modi.length-1;i++){
			dp = modi[i+1] - modi[i];
			if (dp>2){
				dlist.add(dp);
			}
		} // i->
			
		
		if (dlist.size()>2){
			// if large enough, we determine min, max, 
			// if freq(min)>freq(max) we strip 1 instance of max, else 1 instance of min, before calculating avg distance
			dpi = arrutil.arrayMin(dlist, -1);
			dpx = arrutil.arrayMax(dlist, -1);
			
			dpif = arrutil.valueFrequency(dlist,dpi);
			dpxf = arrutil.valueFrequency(dlist,dpx); 
			p=-1; 
			if (dpif>dpxf){
				p= dlist.indexOf(dpx);
			}else{ 
				p= dlist.indexOf(dpi);
			} 
			if (p>=0)dlist.remove(p) ;
						
			int dpsum= arrutil.arraySum( dlist );
			dpr = (double)dpsum/(double)dlist.size();
			
			double avgd = (dpr - (double)Math.round(dpr));
			
			if (avgd<0.12){	nomScore = nomScore + 5 ;}
			if (avgd<0.04){	nomScore = nomScore + 10 ;}

		}
		
		if (dlist.size()==2){
			// high score
			nomScore = nomScore + 10 ;
		}

		// is the first =[0] and the last item in frequencies a modus ?
		mp = 0 ;
		p1 = Arrays.binarySearch( histoStats.getModi(), mp);
		 
		
		mp = frequencies.length-1 ;
		p2 = Arrays.binarySearch( histoStats.getModi(), mp);
		if ((p1!=p2) && (p1>=0) && (p2>=0))nomScore = nomScore + 10 ;
		
		if ((double)zeroCount>((double)frequencies.length*0.78)){
			if (histoStats.getModi().length>=2){
				nomScore = nomScore + 10 ;
			}
		}
		// checking the power spectrum
		FDescription description = histoStats.getDescriptionItemByDescriptor("osci",0) ;
		double[] cfparams = description.getCfParams();
		double r= cfparams[3] ;
		if (Math.abs(r-1.0)<0.1) nomScore = nomScore + 5 ;
		if (Math.abs(r-1.0)<0.01) nomScore = nomScore + 5 ;
		if (cfparams[1]>100)nomScore = nomScore + 10 ;
		
		if (nomScore>42){
			isNominal = true;
		}
		
		return isNominal;
	}


	private void calculateFitCoefficients() {
		// [116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 107, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 138, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 106, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117]
		
		int mode = 0;
		MultiFit fitting ;
		double v;
		double[] cfParams , devParams, minima;
		FDescription fDescription ;
		
		
		
		// now we collect all minima and all maxima across the various fits per type, the worst fit we will drop
		fitting = new MultiFit( this, "poly", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(1,1);
					   histoStats.getfDescriptions().add(fDescription) ;
					 
		fitting = new MultiFit( this, "poly", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(1,2);
		   			   histoStats.getfDescriptions().add(fDescription) ;
        			
		fitting = new MultiFit( this, "poly", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(1,3);
					   histoStats.getfDescriptions().add(fDescription) ;		 
        			
		// ......................................

		fitting = new MultiFit( this, "expo", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(2,1);
					   histoStats.getfDescriptions().add(fDescription) ;		 

		fitting = new MultiFit( this, "expo", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(2,2);
					   histoStats.getfDescriptions().add(fDescription) ;
		
		fitting = new MultiFit( this, "expo", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(2,3);
					   histoStats.getfDescriptions().add(fDescription) ;
		// ......................................
		
		fitting = new MultiFit( this, "osci", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(3,1);
		   			   histoStats.getfDescriptions().add(fDescription) ;
		/*
		fitting = new MultiFit( this, "osci", frequencies,0, frequencies.length );
		fDescription = fitting.calculate(3,2);
					   histoStats.getfDescriptions().add(fDescription) ;
		*/
	}
	
	
	/**
	 * creating a fingerprint vector from those values for classification purposes
	 * if the variable is nominal, the expo is supressed and only some of the params of poly are taken
	 * 
	 * 
	 * @param isNominal
	 * @return
	 */
	public double[] createDescripriveFingerPrint( boolean isNominal ){
		
		double v;
		double[] fp = new double[0] ;
		FDescription description; 
		
		ArrayList<Double> fpValues = new ArrayList<Double>();
		
		
		// zeroCount
		v = histoStats.getModus();
			insertToFingerPrint(v, fpValues) ;
		
		
		if (isNominal==false){
			description = histoStats.getDescriptionItemByDescriptor("expo",0) ;
		}
			
		return fp;
	}
	
	private void insertToFingerPrint(double v, ArrayList<Double> fingerprint){
		
		if (v!= -1.0){
			fingerprint.add(v);
		}
	}

	public void setBinsCount(int n) {
		frequencies = new int[n] ;
		bins = new DistributionBins(100) ;
	}

	public DistributionBin getBin(int index) {
		 
		return bins.items.get(index);
	}

	public int[] getFrequencies() {

		return frequencies ;
	}

	public void setBinFrequency(int index, long n) {
		// [116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 107, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 138, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 106, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117]		
		frequencies[index] = (int) n;
		getBin(index).setObservationCount((int) n);
		
		if (n==0){
			zeroCount++;

			if ((lastNonZeroBinPosition>=0) && (index - lastZeroBinPosition>1)){
				salientBins.add(index);
			}

			lastZeroBinPosition = index; 
		}else{
			if ((lastZeroBinPosition>=0) && (index - lastZeroBinPosition>1)){
				salientBins.add(index);
			}
			lastNonZeroBinPosition = index;
		}

	}

	public int[] split() {
		int[] splitSupportPositions = new int[0];
		
		
		return splitSupportPositions;
	}
	
	
	public boolean isVariableIsNominal() {
		return variableIsNominal;
	}
	public boolean getVariableIsNominal() {
		return variableIsNominal;
	}
	public void setVariableIsNominal(boolean variableIsNominal) {
		this.variableIsNominal = variableIsNominal;
	}
	
	public boolean isVariableIsBinary() {
		return variableIsBinary;
	}
	public boolean getVariableIsBinary() {
		return variableIsBinary;
	}
	public void setVariableIsBinary(boolean variableIsBinary) {
		this.variableIsBinary = variableIsBinary;
	}

	public double getVariance() {
		return variance;
	}
	public void setVariance(double variance) {
		this.variance = variance;
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
	public double getModus() {
		return modus;
	}
	public void setModus(double modus) {
		this.modus = modus;
	}
	public double getMedian() {
		return median;
	}
	public void setMedian(double median) {
		this.median = median;
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
	public int getMedianPos() {
		return medianPos;
	}
	public void setMedianPos(int medianPos) {
		this.medianPos = medianPos;
	}
	public int getZeroCount() {
		return zeroCount;
	}
	public void setZeroCount(int zeroCount) {
		this.zeroCount = zeroCount;
	}
	public int getLastZeroBinPosition() {
		return lastZeroBinPosition;
	}
	public void setLastZeroBinPosition(int lastZeroBinPosition) {
		this.lastZeroBinPosition = lastZeroBinPosition;
	}
	public int getLastNonZeroBinPosition() {
		return lastNonZeroBinPosition;
	}
	public void setLastNonZeroBinPosition(int lastNonZeroBinPosition) {
		this.lastNonZeroBinPosition = lastNonZeroBinPosition;
	}
	public ArrayList<Integer> getSalientBins() {
		return salientBins;
	}
	public void setSalientBins(ArrayList<Integer> salientBins) {
		this.salientBins = salientBins;
	}
	
	
	
}





class MultiFit{
	
	EmpiricDistribution dis; 
	
	String requestName="";
	
	double[] coefficients, coefficientsd1;
	
	
	int dataLen=0;
	double[] deviations , minima ;
	int[] frequencies;
	
	/** sum of deviation */
	double devSum=0, powerRatio=1.0;
	
	int startPos=0, endPos=0;
	int curveType, sampleType; 
	
	double[][] funcValues   ;
	
	boolean precalcSpline= true;
	
	
	
	// ....................................................
	public MultiFit( EmpiricDistribution d, String name, int[] frequencies, int startPos, int endPos ){
		
		dis = d;
		this.frequencies = frequencies;

		if (precalcSpline){
			
		}
		requestName = name;
		deviations = new double[ dataLen];
		funcValues = new double[2][dataLen] ;
	}
	
	// ....................................................
	
	public FDescription calculate(int curvetype, int sampletype ) {
		curveType  = curvetype ;
		sampleType = sampletype ;
		
		// determines startPos & endPos, dependent on sampleType
		determineDataSegment(sampletype) ;
		
		if ( curvetype == 1){
			//frequencies = getSplinedValues();
			polynomFitForDistribution() ;
			return calcDeviations().setName(requestName);
		}
		if ( curvetype == 2){
			
			expoFitForDistribution() ;
			return calcDeviations().setName(requestName);
		}
		if ( curvetype == 3){
			// frequencies = getSplinedValues();
			oscillFitForDistribution() ;
			return calcDeviations().setName(requestName);
		}
		return null;
		
	}
	
	private void determineDataSegment(int sampletype) {
		int gtotal=0, runsum=0;
		
		if (sampletype==1){
			startPos = 0 ; endPos = frequencies.length ;
		}
		if (sampletype==2){
			startPos=0;
			gtotal = dis.count;
			for (int i=0;i<frequencies.length;i++){
				runsum = runsum + frequencies[i];
				if (runsum > ((double)gtotal)*0.45){
					endPos =i;
					break ;
				}
			}
		}  //   median
		if (sampletype==3){
			int d=dis.medianPos;
			if (d<=0){
				d=frequencies.length/3 ;
			}
			startPos= d-1;
			endPos= dis.medianPos;
			int z=0; gtotal = dis.count ;
			
			while ((z<frequencies.length/1.8) && (startPos>=0) && (endPos<frequencies.length) && (runsum < ((double)gtotal)*0.45)){
				
				if (startPos-z>=0){
					runsum = runsum + frequencies[startPos-z];
				}
				if ((endPos+z>=0) && (runsum < ((double)gtotal)*0.45)){
					runsum = runsum + frequencies[endPos+z];
				}
				if ((runsum <= ((double)gtotal)*0.45)){
					z++;
				}else{
					startPos = startPos - z;
					endPos   = endPos   + z;
				}
			}
		}
		
	}

	
	private int[] getSplinedValues( int[] frequencies) {

		int[] newiFreqdValues = new int[frequencies.length] ;
		
		PolynomialSplineFunction psf;
		double v;
		double[] x = new double[frequencies.length] ;
		double[] y = new double[frequencies.length] ;
		
		
		for (int i=0;i<frequencies.length;i++){
			v = frequencies[i];
			if (i>0){
				v = ((double)(3.0*frequencies[i])+(double)(frequencies[i-1]))/4.0; 
			}else{
				v = ((double)(5.0*frequencies[i])+(double)(2.0*frequencies[i+1]) +(double)(frequencies[i+2]))/8.0;
			}
			if (i>1){
				v = ((double)(5.0*frequencies[i])+(double)(2.0*frequencies[i-1]) +(double)(frequencies[i-2]))/8.0; 
			}else{
				if (i==1){
					v = ((double)(3.0*frequencies[i])+(double)(frequencies[i+1]))/4.0;
				}
			}
			y[i] = 0.001 + v ; 
			x[i] = i/10.0 ;
			
			if (i<frequencies.length-1){
				v = ((double)(3.0*frequencies[i])+(double)(frequencies[i+1]))/4.0;
				frequencies[i+1]=(int)v;
			}

		}// i->
		
		
			SplineInterpolator spline = new SplineInterpolator() ;
			psf = spline.interpolate( x, y) ;
			 
			try {
				int n = psf.getN();
				
				for (int k = 0; k < frequencies.length; k++) {
					newiFreqdValues[k] = (int) psf.value(k / 10.0);
				}
			} catch (ArgumentOutsideDomainException e) {
				e.printStackTrace();
			} 
		
		return newiFreqdValues;
	}
	
	private FDescription calcDeviations(){
		
		double px,py,dy ;
		
		FDescription fDescription = new FDescription();
		
		if ((funcValues.length>0) && (funcValues[0].length>0)){
			
			if (dataLen<=1){
				dataLen = funcValues[0].length;
			}

			deviations = new double[dataLen] ;
			
			for (int i=0;i<dataLen;i++){
				
				dy = funcValues[1][i] - frequencies[i] ;
				
				deviations[i] = dy ;
				dy = dy*dy;
				devSum = devSum+dy;
			} // i->
			
			fDescription.setWeightedDeviation(Math.sqrt(devSum)/((double)(frequencies.length))) ; // actually, it is the sum of squares 
			fDescription.setDeviations(deviations) ;

		}else{
			deviations = new double[0] ;
			// for some analyses we won't get an approximation, e.g. for the power spectrum
			fDescription.setWeightedDeviation(-1);
		}
		
		fDescription.setCfParams(coefficients) ;
        // we also need special points: minima, maxima
		fDescription.setMinima(minima) ;
		
		return fDescription;
	}

	private void polynomFitForDistribution(){
		
		double weight, px, devSum= 0.0, dy ;
		boolean hb;
		double[] coeffd2 ,py;
		ArrayList<Integer> minimaList = new ArrayList<Integer>();

		PolynomialFunction  pf ,pfd1, pfd2;
		PolynomialFitter polyfitter = new PolynomialFitter( 7, new LevenbergMarquardtOptimizer());

		dataLen = endPos - startPos;
		
		for (int i=startPos;i<dataLen;i++){
			weight=1.0 ;
			if (i<=10)weight = 1.5 ; 
			if (i<=3) weight = 2.0 ;
			
			weight=1.0 ;
			if (dis.salientBins.indexOf(i)>=0){
				weight=1.5 ;
			}
			
			if (i<dataLen){
				polyfitter.addObservedPoint( weight, ((double)i)/10.0, (double)frequencies[i]/(double)dis.count);
			}
		}
	
		try {
		
			pf = polyfitter.fit();
			pfd1 = pf.polynomialDerivative() ;
	
			coefficients  = pf.getCoefficients();
			coefficientsd1  = pfd1.getCoefficients();
			
			pfd2 = pfd1.polynomialDerivative() ;
			coeffd2 = pfd2.getCoefficients();
			
			funcValues[0] = new double[dataLen] ;
			funcValues[1] = new double[dataLen] ;
			
			// preparing the measurement the deviation, error
			for (int i=0;i<dataLen;i++){
				px = ((double)i)/10.0;
				funcValues[0][i] = px ;
				funcValues[1][i] = pf.value(px); 
			}
				
			py = new double[3];
			// where is pfd2=0 (!=0 before and after) ? is it a small or a large value?
			for (int i=0;i<dataLen;i++){
				px = ((double)i)/10.0;
				py[0] = py[1]; py[1] = py[2];
				py[2] = pfd1.value(px);
				// check for passage-zero
				hb = (py[0]<0 ) && (py[2]>0) && ( py[1]>py[0]) && ( py[1]<py[2]) ;
				if (hb){
					minimaList.add(i) ;
				}
				hb = (py[0]>0 ) && (py[2]<0) && ( py[1]<py[0]) && ( py[1]>py[2]) ;
				if (hb){
					minimaList.add(i) ;
				}
			}
			
			if (minimaList.size()>0){
				minima = new double[minimaList.size()] ;
				for (int i=0;i<minima.length;i++){
					minima[i] =minimaList.get(i) ;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	}
	private void expoFitForDistribution() {
		PairedData data = null ;
		int p;
		double eps = 0.001, weight, dy ;
		int[] _frequencies ;
		
		int maxf = dis.arrutil.arrayMax( frequencies, 0);
		
		dataLen = endPos - startPos;
		
		_frequencies = new int[frequencies.length];
		System.arraycopy(frequencies, 0, _frequencies, 0, frequencies.length) ;
		
		_frequencies = getSplinedValues(_frequencies);
		_frequencies = getSplinedValues(_frequencies);
		_frequencies = getSplinedValues(_frequencies);
		
		
		int maxf2 = dis.arrutil.arrayMax(_frequencies, 0) ;
		double maxr = (double)maxf/(double)maxf2 ;
		_frequencies = dis.arrutil.arrScale( _frequencies, maxf2);
		// scale it back such that  maxf==maxf2 (then put it to the arrutil...)
		// we need that in order to make the deviances at least roughly comparable across fitting functions
		
		double[] x = new double[_frequencies.length] ;
		double[] y = new double[_frequencies.length] ;
		double[] w = new double[_frequencies.length] ;
	
		for (int i=startPos;i<dataLen;i++){
			weight=1.0 ;
			if (i<=10)weight = 1.5 ; 
			if (i<=3) weight = 2.0 ;
			
			x[i] = ((double)i)/10.0;
			y[i] = (0.001 + (double)_frequencies[i])/1.0 ;//  .../((double)dis.count)+0.01
			w[i] = weight;
		}
		
		double fmin = dis.arrutil.arrayMin( y, 0.00) ;
		while (fmin<0.00001){
			p = dis.arrutil.arrayMinPos( y ) ;
			fmin = dis.arrutil.arrayMin( y, 0.00) ;
			if ((p>=0) &&(fmin<0.000000001)){
				y[p] = 0.001 ;
			}
		}
		
		coefficients = new double[3];
		coefficients[0] = -1;
		coefficients[1] = -1;
		coefficients[2] = -1;

		data = new PairedData( x , y);
		ExponentialFit expoFitter = new ExponentialFit( data, w, eps) ;
		
		// parameters for y = A*exp(-B*x) .... this function provided by jsc is not complete: y = A*exp(-B* (x+c)) would be more correct
		coefficients[0] = expoFitter.getA();
		coefficients[1] = expoFitter.getB();
		// coefficients[2] = expoFitter.getN();
		
		// get the values of the function and the deviances, squared dev's
		
		funcValues[0] = new double[x.length] ;
		funcValues[1] = new double[x.length] ;
		
		
		devSum=0.0 ;
		double a = expoFitter.getA();
		double b = expoFitter.getB();
		// coefficients[2] = expoFitter.;
		
		for (int i=0;i<dataLen;i++){
			// preparing the measurement the deviation, error
			funcValues[0][i] = x[i] ;
			dy = (a * Math.exp(b*x[i])) - y[i];
			funcValues[1][i] = dy;
		}
		dy=0;
	}

	
	
	/**
	 * this method effectively detects whether the values are periodic, and
	 * also whether they are perfectly periodic: they are perfectly periodic
	 * only in case of being a nominal variable;
	 * 
	 * perfectly periodic : 
	 *   - power spectrum of (power spectrum of histo) = power spectrum of histo;
	 *   - the distances between maxima of the 2nd order power spectrum are multiples 
	 *     of the minimal distance betwee any two maxima
	 *      
	 * if these conditions are met, we can determine the support values in the data,
	 * that is, the values that represent the nominals;
	 * 
	 * periodic with strong power also means, that we have different categorical variables in one
	 * 
	 */
	private void oscillFitForDistribution() {
		 
		double weight, px,maxv, v, devSum= 0.0, dy ;
		boolean hb;
		double[] coeffd2 ,py,rValues,tValues ;
		int p,xp,xp1,xp2 ;
		double avgPower = -1; 
		ArrayList<Integer> minimaList = new ArrayList<Integer>();

		HarmonicFunction ofi;
		PolynomialFunction  pfd1, pfd2;
		HarmonicFitter oscifitter = new HarmonicFitter(  new LevenbergMarquardtOptimizer());

		dataLen = endPos - startPos;
		
		if (frequencies[0]!=0){
			// if it is rather large, we abolish it?
			// frequencies[1] = frequencies[0];
			frequencies[0]=0; // 
		}
		// rValues need a length of 2^x+1
		rValues = new double[129];// frequencies.length
		 
		
		for (int i=startPos;i<dataLen;i++){
			v = (double)frequencies[i]/(double)dis.count;
			rValues[i] = v;
		}
 	 
		tValues = new double[rValues.length];
		FourierTransform fft ;
		
		System.arraycopy(rValues, 0, tValues, 0, rValues.length );

		fft = new FourierTransform(rValues);
  
		// note that the power spectrum creates echoes in case of periodic signals:
		// if there is a perfect period of 3 then there are also such of n*3..., but the period-3 will have 
		// the largest power 
		double[][] power0 = fft.powerSpectrum();
		power0 = fft.getpowerSpectrumEstimate();
		// the fft power estimate will always return a vector of 2^n, such
		// the its length (2^n) <= the length of the input vector...
		// if the inputvector was len=100, the power estimate will be of len=64
		// also, the periodicities = main frequencies have to be corrected by the ratio 100/128 
		//       == the position of the max in power indicates the frequency in the original vector 
		//          and that position must be multiplied by the ratio to get the correct one
		 
		powerRatio = getPowerRatio( power0[1],-1 ) ;
		
		xp = determineMainFrequencySupportPoint( power0[1] );
		// we should allow for 2 values ? (the position indexes pointing to the two largest powers...) 
		// TODO: determine ratio !! to minimum before that max / inbetween the pace from that to the one before
		
		
		// main frequency is in xp, and has the power maxv
		// now we correct the position back
		xp1 = (int) Math.round((double)xp*((double)frequencies.length)/((double)power0[1].length)) ;
		// now xp contains the main frequency in the original field
		
		
		// the location of the peaks provide the "frequencies"/ periodicities in the histogram  
		// if we have an ordinal measure, we have periodicity in the power spectrum
		// -> power spectrum second order
		
		// now we should test, if the variable is a nominal variable 
		
		// the power series is now only half of the size of the original, 
		// thus we have to stretch it by a factor of 2
		int pL = power0[1].length;
		tValues = new double[pL] ;
		
		
		for (int i=0;i<pL;i++){
			p = (i*2);
			if (p<tValues.length){
				tValues[p] = power0[1][i] ;
				tValues[p+1] = power0[1][i] ;
			}
		}
		for (int i=0;i<pL-1;i++){
			tValues[i] = (tValues[i]+tValues[i+1])/2.0 ;
		}
		// power0
		
		fft = new FourierTransform(rValues);
		
		double[][] power1 = fft.powerSpectrum();
		power1 = fft.getpowerSpectrumEstimate();
		
		// now searching for maxima beyond the first minimum
		xp  = determineMainFrequencySupportPoint( power0[1] );
		xp2 = (int) Math.round((double)xp *((double)frequencies.length)/((double)power0[1].length)) ;
			
			
		// the first 3 frequencies  
		// coefficients.clone()...
		
		coefficients = new double[4] ;
		coefficients[0] = xp1; 
		coefficients[1] = powerRatio ;
		
		coefficients[2] = xp2;
		coefficients[3] = (double)xp1/(double)xp2;
	}

	private int determineMainFrequencySupportPoint(double[] values) {
		int mainFreqSupportPt=-1;
		double avgPower = 0.0,maxv ;
		int xp=-1,p;
		// a bit of filtering
		if (values.length>0){
			avgPower = dis.arrutil.arraySum( values)/ (values.length);
			for (int i=0;i<values.length;i++){
				values[i] = Math.max( 0 ,values[i]- 2.0*avgPower);
			}
		}
		
		MaxPositions mxp = new MaxPositions();
		int[] mmxs ;
		mmxs = mxp.setData( values ).identifyModi( 0.27, 11 ,10 );
		// [1, 18, 36, 54, 72, 90, 108, 126]
		// now find the maximum VALUE -> check all those positions
		maxv = -1.0; xp=-1;		
		for (int i=0;i<mmxs.length;i++){
			p = mmxs[i];
			if (maxv<values[p]){
				maxv=values[p];
				xp = p;
			}
		}
		mainFreqSupportPt = xp;

		return mainFreqSupportPt;
	}

	public double getPowerRatio(double[] values, int maxpos) {
		double min = 9999999999.09, max=-99.09;
		
		powerRatio=1.0;
		
		for (int i=0;i<values.length;i++){
			if (min>values[i]){
				min=values[i];
			}
			if (max<values[i]){
				max=values[i];
			}
		}
		if (min==0){
			powerRatio = 9999.0;
		}else{
			if (maxpos>=0){
				powerRatio = values[maxpos]/min;
			}else{
				powerRatio = max/min;
			}
		}
		 
		
		return powerRatio;
	}
} // 
// ....................................................



//probably public...
class SparseTails{
	
	public SparseTails(){
		
	}
	
	
	
}


















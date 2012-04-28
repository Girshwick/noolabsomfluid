package org.NooLab.somtransform.algo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;


 
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.results.FrequencyList;
import org.NooLab.somfluid.core.engines.det.results.FrequencyListGeneratorIntf;
import org.NooLab.somfluid.core.engines.det.results.ItemFrequencies;
import org.NooLab.somfluid.core.engines.det.results.ItemFrequency;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somtransform.algo.distribution.DistributionBin;
import org.NooLab.somtransform.algo.distribution.Distributions;
import org.NooLab.somtransform.algo.distribution.EmpiricDistribution;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.math3.distribution.EmpiricalDistribution;



/**
 * 
 * this is also known as binning
 *  
 * there are different approaches, dependent on the purpose 
 *  
 *  - histogram + spline(polynomial fit + derivative(sequence of min,max)
 *  - sorting + clustering + auto-correlation + denoising (fourier-like)
 *  
 * this can be used for identification of groups in the target variable, 
 * but also for finding separations for binning (although ths could be 
 * prepared with knn++ clustering also)
 * 
 * 
 * commons.math, frequency via cumulative ... -> wrapper class
 *               EmpiricalDistribution
 *               
 *        no knowledge : PolynomialFitter        This class implements a curve fitting specialized for polynomials.
		                 WeightedObservedPoint   This class is a simple container for weighted observed point in curve fitting.
          modus somewhere in the middle : HarmonicCoefficientsGuesser
          
          or everything in flanagan: poly, sin exp - fitting, then checking the best match
          http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html

 * org.math.plot.utils.Array.*;
 * jmath  2d , 3d - Histogram
 *                
 *                
	 
	 
	 
 */
public class AdaptiveDiscretization implements FrequencyListGeneratorIntf {


	DSom dSom ;
	SomDataObject somData ;
	
	ModelingSettings modelingSettings ;
	
	ArrayList<Integer> sampleRecordIndexes ;
	Distributions distributions;
	
	ArrayList<Double> nominalSupportValues;
	
	
	// ========================================================================
	public AdaptiveDiscretization(DSom dsom, ArrayList<Integer> recordids) {
		
		if (dsom!=null){
			dSom = dsom ;
			modelingSettings = dSom.getModelingSettings() ;
			distributions = new Distributions( dSom );
		}
		sampleRecordIndexes = recordids ;
		
	}
	
	public AdaptiveDiscretization( SomDataObject somdata, ModelingSettings modset, ArrayList<Integer> recordids) {
		
		somData = somdata;
		modelingSettings = modset;
		sampleRecordIndexes = recordids ;
	}

		
	// ========================================================================
	
	public void hisPolyMinMax( ArrayList<Double> nDataValues , int variableIndex ){
		
		double[] aValues ;
		int j;
		long n; double stdev, mean;
		
		EmpiricDistribution distribution = new EmpiricDistribution( distributions );
		DistributionBin bin ;
		// this is from package org.NooLab.math3.distribution, which is derived from commons.math
		EmpiricalDistribution empiricsDens = new EmpiricalDistribution( 100 );
		
		aValues = new double[sampleRecordIndexes.size()];
		
		for (int i=0;i<aValues.length;i++){ 
			j = sampleRecordIndexes.get(i);
			if (j<nDataValues.size()){
				aValues[i] = nDataValues.get(j);
			}
		}
		
		empiricsDens.load( aValues ) ;
		
		 

		StatisticalSummary  sampleStats = empiricsDens.getSampleStats() ;
		stdev = sampleStats.getStandardDeviation() ; mean = sampleStats.getMean() ;
		List<SummaryStatistics> bstats = empiricsDens.getBinStats();
		
		distribution.setBinsCount(100) ;
		
		distribution.importStatsDescription(sampleStats);

		// for all bins we retrieve the "SummaryStatistics"  
		for (int i=0;i<bstats.size();i++){
			
			bstats.get(i) ;
			n = bstats.get(i).getN();
			
			distribution.setBinFrequency(i,n); // writes to index i of a double[]
			 
			distribution.getBin(i).importStatsDescription(bstats.get(i));
			
			// we also could investigate the location of data record indexes in the distribution in order to 
			// detect non-stationarities in the process, which could be related to the measurement, or its circumstances
			// e.g. seasonal effects, such as time, daytime, day of the week etc
		}
 
		// calculating some stuff, including estimating parameters for fits
		distribution.describe();
		
		
		
		
		Variable var = somData.getVariables().getItem(variableIndex) ;
		
		
		int sn = Variable._VARIABLE_SCALE_REAL ;
		
		if (distribution.getVariableIsNominal() ){
			sn = Variable._VARIABLE_SCALE_NOMINAL ; 
		}
		if (distribution.getVariableIsBinary() ){
			sn = Variable._VARIABLE_SCALE_BINARY ; 
		}
		 
		var.setValueScaleNiveau(sn);
		
		// now that we know whether the variable is nominal or not, we can collect
		// the values in case it is
		
		if (distribution.getVariableIsNominal()){
			
			boolean hb = somData.getVariables().getItem(variableIndex).isTV() ;
			collectNominalValues( aValues, hb);
			
			// if it is NOT the TV, we apply "optimal scaling"
		}
		
		
		// performing the split into groups
		int[] binPositions = distribution.split() ;
		
	}
	
	private void collectNominalValues( double[] values, boolean isTv) {
		
		ArrayList<Double> valueList;
		double v;
		 
		FrequencyList frequencyList  ;
		ItemFrequencies ifr ;
		
		
		
		frequencyList = new FrequencyList( ((FrequencyListGeneratorIntf) this));
		
		valueList = frequencyList.arrutil.changeArrayStyle(values) ;
		
		frequencyList.digestValues(valueList) ;
		
		ifr = frequencyList.getItemFrequencies() ;
		// contains frequency and observed value
		
		int n= ifr.size() ;
		
		if (isTv==false){
			performOptimalScaling(ifr);
		}
		
		nominalSupportValues = new ArrayList<Double>() ;
		
		for (int i=0;i<ifr.size();i++){
			v = ifr.getItems().get(i).getObservedValue() ;
			if (v>=0){
				nominalSupportValues.add(v);
			}
		}
		
	}
	
	
	
	
	private void performOptimalScaling(ItemFrequencies ifr) {
		// TODO Auto-generated method stub
		
	}

	/*
    		what amounts to the Variable Kernel Method with Gaussian smoothing: Digesting the input file

				- Pass the file once to compute min and max.
				- Divide the range from min-max into binCount "bins."
				- Pass the data file again, computing bin counts and univariate statistics (mean, std dev.) for each of the bins
				- Divide the interval (0,1) into subintervals associated with the bins, with the length of a bin's 
				  subinterval proportional to its count.


*/
	public void digestSample(ArrayList<Double> nDataValues) {
		 
		
	}

	public ArrayList<Double> getNominalSupportValues() {
		return nominalSupportValues;
	}

	public void setNominalSupportValues(ArrayList<Double> nominalSupportValues) {
		this.nominalSupportValues = nominalSupportValues;
	}
	
	 
	
}




/**
 * 
 * this can be used for identification of groups in the target variable, but also for finding
 * separations for binning (although ths could be prepared with knn++ clustering also) 
 * 
 * also: creating a histogram, searching for peaks via first derivative in the 5th order spline
 */
class GroupIdentifier 	implements 
 								FrequencyListGeneratorIntf {
	
	FrequencyList frequencyList  ; 
	 
	ClassificationSettings classifySettings;
	
	public GroupIdentifier(){
		
	}
	
	
	public void determineTargetGroups(){
		
		ArrayList<Double> tvValues = null;
		 
		MetaNodeIntf node;
		ArrayList<Double> tvValuesNorm,tvValuesRaw  ;
		  
		
		// get target variable column from raw data and (!) from normalized data... 
		//  - we need to know whether the raw TV is already ordinal, or if we should make it ordinal (by optional option)
		//  - we need the translation from normalized to raw
		
		tvValuesNorm = null;   
		tvValuesRaw  = null;
		
		frequencyList = new FrequencyList( ((FrequencyListGeneratorIntf) this));
		frequencyList.setListIndex(1);
		frequencyList.setSerialID(1);

		// now check this list of values for the target group

		frequencyList.digestValuesForTargets( tvValues,-1, // -1 -> no special ECR applies in this exploration
												classifySettings.getTargetGroupDefinition(),
												classifySettings.getTGlabels());
			 
		 
	}
	
	

	
} // inner class GroupIdentifier 
// ........................................................................
	

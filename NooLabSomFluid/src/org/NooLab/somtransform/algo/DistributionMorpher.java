package org.NooLab.somtransform.algo;

import java.util.ArrayList;


/**
 * 
 * This lcass offers modifications of a distribution, which is given as 
 * an array of double values 
 *  - double[]
 *  - ArrayList&lt;Double&gt; 
 * or as
 *  - interfaced object "EmpiricDistribution" 
 *  
 * results are available as
 *  - as values, either formatted as double[] or as ArrayList&lt;Double&gt; 
 *  - as a string containing a formula
 *     
 *     
 * The transformations are
 * - log transformation, in order to "shift/compress" a distribution to the right,
 *   up to the achieval of a criterion relative to the normal distribution
 * - finding new min max for the values in order to prevent empty classes at the tails
 * -  
 * 
 * 
 */
@SuppressWarnings("unused")
public class DistributionMorpher {

	EmpiricDistribution empiricDistribution, importedEmpirDistribution;
	DistributionMorpher dm;
	
	// ========================================================================
	public DistributionMorpher(){
		
		dm = this;
		
	}
	// ========================================================================

	
	public void importDistribution( EmpiricDistribution empiricdistribution){
		importedEmpirDistribution = new EmpiricDistribution(empiricdistribution);
	}
	
	public void setData( double[] values){
		
	}
	public void setData( ArrayList<Double> values){
		
	}
	
	// ..........................................
	
	public DistributionMorpher normalize(){
		
		return this;
	}
	
	public DistributionMorpher cutTails( int mode, double portion){
		
		return this;
	}

	// ..........................................

	/**
	 * returns the values as ArrayList<Double>
	 */
	public void getTransformedValuesList(){
		
	}
	
	/**
	 * returns the values as double[]
	 */
	public void getTransformedValues(){
		
	}
	
	public void getFormula(){
		
	}
	
	/**
	 * returns the object EmpiricDisitribution that describes the data after morphing
	 */
	public void getDistribution(){
		
	}
	
	/**
	 * returns the frequency histogram that describes the data after morphing
	 */
	public void getDistributionAsHistogram(){
		
	}
	
	
	// ------------------------------------------------------------------------
	
	
	
}

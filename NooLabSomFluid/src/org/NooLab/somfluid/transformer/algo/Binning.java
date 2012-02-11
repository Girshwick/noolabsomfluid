package org.NooLab.somfluid.transformer.algo;

import java.util.ArrayList;

public class Binning implements AlgorithmIntf {

	
	// ========================================================================
	public Binning(){
		
	}
	// ========================================================================
	
	public AlgorithmIntf setValues( ArrayList<Double> values  ){
		
		return this;
	}
	
	
	public AlgorithmIntf setParameters( double ...param){
		
		return this;
	}

	public ArrayList<Double> getDescriptiveResults(){
		
		return null;
	}
	
	public ArrayList<Double> getTransformedValues(){
		
		return null;
	}
	
	public AlgorithmIntf  calculate(){
		
		return this;
	}
}

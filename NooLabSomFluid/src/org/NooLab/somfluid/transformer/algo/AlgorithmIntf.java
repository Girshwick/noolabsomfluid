package org.NooLab.somfluid.transformer.algo;

import java.util.ArrayList;

public interface AlgorithmIntf {

	
	
	public AlgorithmIntf setValues( ArrayList<Double> values  ) ;
	
	public AlgorithmIntf setParameters( double ...param) ;

	public ArrayList<Double> getDescriptiveResults() ;
	
	public ArrayList<Double> getTransformedValues() ;
	
	public AlgorithmIntf  calculate() ;
	
	
}

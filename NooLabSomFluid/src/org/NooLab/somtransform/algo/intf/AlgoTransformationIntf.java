package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

import org.NooLab.somtransform.DataDescription;




public interface AlgoTransformationIntf  extends AlgorithmIntf{

	
	public ArrayList<Double> getTransformedValues() ;

	public void setDatDescription(DataDescription dataDescription);

	public boolean hasParameters() ;
	
	public AlgorithmParameters getParameters() ;
	
	public String getDescription() ;

	double handlingRangeProtection(double value);
		
	public int getInputColumnsCount() ;
	public void setInputColumnsCount( int inColCount ) ;
}

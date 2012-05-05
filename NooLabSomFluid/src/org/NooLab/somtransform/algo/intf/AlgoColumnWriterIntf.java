package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;



public interface AlgoColumnWriterIntf extends AlgorithmIntf{

	public ArrayList<Double> writeTransformedValues( String outColumnLabel , int bufferIndex) ;
	
	public ArrayList<Double> acquireBufferedValues( int stackID, int[] bufferIndexes ) ;

	public AlgorithmParameters getParameters() ;
	
}

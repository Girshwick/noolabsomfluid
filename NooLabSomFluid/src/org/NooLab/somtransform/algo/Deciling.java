package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoColumnWriterAbstract;
import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;


/**
 * 
 * 
 * splitting data from a source column into two new columns
 * specialty: using histogram analysis and kurtosis for automatic decision 
 * 
 */
public class Deciling extends AlgoColumnWriterAbstract {

	
	int typeInfo = AlgorithmIntf._ALGOTYPE_WRITER ;
	
	String versionStr = "1.00.01" ;
	
	// ------------------------------------------------------------------------
	public Deciling(){
		super();
	}
	
	@Override
	public int getType() {
		 
		return typeInfo;
	}

	@Override
	public String getVersion() {
		 
		return versionStr;
	}
	// ------------------------------------------------------------------------
	
	
	 
	@Override
	public int calculate() {
		// TODO Auto-generated method stub
		return -1;
	}
	
	
	
	@Override
	public void setParameters(AlgorithmParameters algorithmParams) {
		
		basicParametersAssimilation(algorithmParams) ;
		
	}

	

	@Override
	public ArrayList<Double> getDescriptiveResults() {
		return null;
	}

	
	@Override
	public ArrayList<Double> writeTransformedValues(String outColumnLabel, int bufferIndex) {

		return null;
	}

	@Override
	public ArrayList<Double> getValues(int part) {
		return null;
	}

	@Override
	public ArrayList<Double> acquireBufferedValues(int stackID, int[] bufferIndexes) {

		return null;
	}

	@Override
	public int getRangeViolationCounter() {
		return 0;
	}

	@Override
	public void setRangeViolationCounter(int rangeViolationCounter) {
		
	}

	@Override
	public String[] showAvailableParameters() {
		// TODO Auto-generated method stub
		return null;
	}
 


}

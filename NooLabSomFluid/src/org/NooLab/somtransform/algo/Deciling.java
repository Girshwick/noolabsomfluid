package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoColumnWriterAbstract;
import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;


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
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void setParameters(ArrayList<Object> params) {
		
		super.setParameters(params) ;
		
	}
	
	
	@Override
	public ArrayList<Double> writeTransformedValues(String outColumnLabel,
			int bufferIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ArrayList<Double> acquireBufferedValues(int stackID,
			int[] bufferIndexes) {
		// TODO Auto-generated method stub
		return null;
	}

	 

	@Override
	public int getRangeViolationCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRangeViolationCounter(int rangeViolationCounter) {
		// TODO Auto-generated method stub
		
	}


 



}

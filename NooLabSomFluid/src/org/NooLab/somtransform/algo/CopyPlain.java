package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoColumnWriterAbstract;
import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;



public class CopyPlain extends AlgoColumnWriterAbstract{

	 
	private static final long serialVersionUID = -6276965194566427330L;
	
	String versionStr = "1.00.01" ;
	
	int typeInfo = AlgorithmIntf._ALGOTYPE_WRITER ;
	
	
	// ------------------------------------------------------------------------
	public CopyPlain(){
		
	}
	// ------------------------------------------------------------------------	
	
	
	@Override
	public int getType() {
		 
		return typeInfo;
	}


	@Override
	public String getVersion() {
		 
		return versionStr;
	}

  
	@Override
	public int calculate() {
		 
		return -1;
	}


	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setParameters(ArrayList<Object> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> writeTransformedValues( String outColumnLabel,
													 int bufferIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> acquireBufferedValues( int stackID,
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


	@Override
	public String[] showAvailableParameters() {
		// TODO Auto-generated method stub
		return null;
	}



}

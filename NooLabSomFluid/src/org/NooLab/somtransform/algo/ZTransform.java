package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;



public class ZTransform extends AlgoTransformationAbstract {

	
	int typeInfo = AlgorithmIntf._ALGOTYPE_VALUE ;
	
	String versionStr = "1.00.01" ;
	
	// ------------------------------------------------------------------------
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
		// 
		
		
		return -1;
	}



	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ArrayList<Double> getTransformedValues() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}
 
 

}

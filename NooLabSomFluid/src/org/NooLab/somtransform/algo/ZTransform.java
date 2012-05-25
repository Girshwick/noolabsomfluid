package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;



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
		 
		return null;
	}



	@Override
	public ArrayList<Double> getTransformedValues() {
		 
		return null;
	}



	@Override
	public ArrayList<Double> getDescriptiveResults() {
		 
		return null;
	}

	@Override
	public String getDescription() {
		 
		return null;
	}
	/*
	@Override
	public void setParameters(AlgorithmParameters algorithmParams) throws Exception {
		if (parametersNullCheck(algorithmparams)==false) return;
		parameters.items.addAll( algorithmparams.getItems() ) ;
		
	}
     */
 

}

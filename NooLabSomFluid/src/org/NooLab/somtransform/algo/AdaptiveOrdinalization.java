package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;

/**
 * 
 * this allows for classification of the data either by denovo clustering methods,
 * or by applying an existing SOM-model !!
 * 
 * 
 *
 */
public class AdaptiveOrdinalization extends AlgoTransformationAbstract{

	private static final long serialVersionUID = -8939558057170713637L;

	int typeInfo = AlgorithmIntf._ALGOTYPE_VALUE ;
	
	String versionStr = "1.00.01" ;
	
	// ------------------------------------------------------------------------
	public AdaptiveOrdinalization(){
		
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
		// TODO Auto-generated method stub
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

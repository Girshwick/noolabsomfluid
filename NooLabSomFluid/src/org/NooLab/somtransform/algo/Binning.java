package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;

public class Binning extends AlgoTransformationAbstract{
	
	private static final long serialVersionUID = -3936810952346465011L;

	
	String versionStr = "1.00.01" ;
	
	
	// ========================================================================
	public Binning(){
		
	}
	// ========================================================================

	@Override
	public String getVersion() {
		 
		return versionStr;
	}
 
	
	public int calculate(){
		
		return -1;
	}
	
	 
	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}
	public AlgorithmIntf setParameters( double ...param){
		
		return this;
	}

	public ArrayList<Double> getDescriptiveResults(){
		
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
 

	
}

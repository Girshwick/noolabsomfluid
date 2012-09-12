package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;




public class ResidualsBySimpleCluster extends AlgoTransformationAbstract{
	
	private static final long serialVersionUID = -6243705674084934037L;
	
	String versionStr = "1.00.01" ;

	public ResidualsBySimpleCluster(){
		
	}

	
	@Override
	public String getDescription() {
		
		autoDescription = "" ;
		return autoDescription;
	}
		
	@Override
	public String getVersion() {
		return versionStr;
	}

	@Override
	public int calculate() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}


}

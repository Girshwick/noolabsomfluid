package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;


/**
 * 
 * this can determine the location by itself, or it takes an interfaced object that describes a NormValueRange; </br> </br>
 * 
 * given the boundaries, two things will be done: </br>
 * 
 * 1. differences within the interval are almost flattened;  </br>
 * 2. increasing deviation from the interval is penalized in a progressive manner,  </br> 
 *    either indicating just the deviation irrespective of the direction, or sensitive to the direction (too small too large)  </br> </br>
 * 
 * this should not be combined with adaotive deciling: actually, adaptive deciling refuses its application, </br> 
 * if the immediate parent level in the stack contains "ResidualsByLocation" !  </br>
 *
 */
public class ResidualsByLocation extends AlgoTransformationAbstract{
	
	private static final long serialVersionUID = -2984759009755175877L; 
	
	
	String versionStr = "1.00.01" ;
	
	
	@Override
	public String getDescription() {
		autoDescription = "";
		return autoDescription;
	}
	 
	@Override
	public String getVersion() {
		 
		return versionStr;
	}

	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int calculate() {
		// TODO Auto-generated method stub
		return 0;
	}

}

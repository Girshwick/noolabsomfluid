package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;



public class MissingValues extends AlgoTransformationAbstract {
 
	private static final long serialVersionUID = -6393438504747140183L;

	String versionStr = "1.00.01" ;
	
	// ....................................................


	
	
	
	// ------------------------------------------------------------------------
	public MissingValues(){
		super();
	}
	// ------------------------------------------------------------------------


	@Override
	public String getVersion() {
		return versionStr;
	}

	

	@Override
	public int calculate() {
		
		double v;
		int result = -1;
		
		outvalues.clear() ;
		
		if (values!=null){
			for (int i=0;i<values.size();i++){
				if (values.get(i)==null){
					v = -1.0 ; // TODO: apply parameters
				}else{
					v = (Double) values.get(i) ;
				}
				outvalues.add(v);
			}
		}
		if (stringvalues!=null){
			for (int i=0;i<stringvalues.size();i++){
				outstringvalues.add( stringvalues.get(i) );
			}
			
		}
		 
		return result;
	}


 

	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}


	
	
}

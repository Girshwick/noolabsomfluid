package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.DataDescription;
import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;


public class LinearNormalization extends AlgoTransformationAbstract {

	private static final long serialVersionUID = -668560046926702153L;
	
	String versionStr = "1.00.01" ;
	
	
	// ------------------------------------------------------------------------
	public LinearNormalization(){
		super();
	}
	// ------------------------------------------------------------------------	
	 

	@Override
	public String getVersion() {
		 
		return versionStr;
	}
	
	
	@Override
	public int calculate() {
		int result = -1;
		double v, vr, _min, _max;
		
		outvalues.clear();
		
		// get parameters 
		_min = dataDescription.getMin() ;
		_max = dataDescription.getMax() ;
		
		if (dataDescription.isComplete()==false){
			return -2;
		}
		
		// perform linear normalization
		try{
			
			outvalues.clear() ;
			
			for (int i=0;i<values.size(); i++){
				
				v = (Double)values.get(i) ;
				
				if ( v!= -1.0 ){
					if (_max - _min == 0.0){
						vr = 0.0 ;
					}else{
						vr = (v- _min)/(_max - _min) ;
					}
				} else{
					vr = -1.0 ;
				}
				
				outvalues.add(vr);
				
			}// -> all values
			
			result=0;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
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



	 
 

}

package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;


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
		boolean hb;
		 
		
		// get parameters 
		_min = dataDescription.getMin() ;
		_max = dataDescription.getMax() ;
		
		if (dataDescription.isComplete()==false){
			return -2;
		}
		
			
		// perform linear normalization
		try{
			if (outvalues==null){
				outvalues = new ArrayList<Double>();
			}
			outvalues.clear() ;
			
			for (int i=0;i<values.size(); i++){
				
				v = (Double)values.get(i) ;
				// NOT here in LinNorm!!! but in any other algorithm... v = handlingRangeProtection(v);
				
				if (( v!= -1.0 ) && (v!=-2.0)){
					
					// the actual calculation ...
					
					if ((_max - _min <= 0.0) ){
						vr = 0.0 ;
					}else{
						vr = (v- _min)/(_max - _min) ;
					}
					if (vr>1.0){
						vr = 1.0;
						// OR: -1.0 , according to the parameters
					}
					
					hb = setCalculationResultValue(i,vr) ;
					// put value to outvalues<>
				} else{
					setCalculationResultValue(i,v) ;
				}
				
			}// -> all values
			
			result=0;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}


 

	@Override
	public void setParameters(ArrayList<Object> params) throws Exception {
		if ((params==null) || (params.size()==0)){
			return;
		}
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

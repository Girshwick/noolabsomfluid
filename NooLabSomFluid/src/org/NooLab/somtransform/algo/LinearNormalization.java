package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;


/**
 * 
 * 
 * TODO: we need a parameter that indicates a conditional normalization: 
 *       if the value distribution is binary or ternary (2 or 3 different values) AND
 *       all values are within [>0 .. <1] then we do NOT transform it
 *       !!! max value should be <=0.9 !!! and min value>=0.1 in order to activate this mechanism 
 * 
 */
public class LinearNormalization extends AlgoTransformationAbstract {

	private static final long serialVersionUID = -668560046926702153L;
	
	String versionStr = "1.00.01" ;
	
	/** if >=1 then [0..1] will be transformed into [0.3,0.7], or even [0.4 .. 0.6],
	 * if scaleForBinary >=2   */
	int scaleForBinary = -1;
	
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
					
					if (scaleForBinary>=1){
						vr = 0.3 + (vr * 0.4);
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
	public void setParameters(AlgorithmParameters algorithmparams)  throws Exception{
		
		AlgorithmParameter ap = algorithmparams.getItem(0);
		String apStr = ap.getStrValue() ;
		if (apStr.length()>0){
			if (apStr.toLowerCase().startsWith("bin")){
				try{
					scaleForBinary = ap.getIntValues()[0] ;
				}catch(Exception e){
					scaleForBinary = -1;
				}
			}
		}
	}
	
	@Override
	public void setParameters(ArrayList<Object> params) throws Exception {
		if ((params==null) || (params.size()==0)){
			return;
		}
		Object p = params.get(0);
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

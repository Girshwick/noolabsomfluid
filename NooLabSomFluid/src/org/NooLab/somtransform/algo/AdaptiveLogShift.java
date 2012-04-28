package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;


/**
 * if data are right-shifted, we apply an integrated inversion first: abs(a-1) ;
 * 
 * particularly semantic-loaden variables are often left shifted;
 * this needs to be corrected for a better discernability
 * 
 *
 * its abstract base class, "AlgoTransformationAbstract", contains an interface to the BaseStatistics which
 * is also in use for variables;
 * 
 * the parameters of the stats description are, however, stored independently !! 
 * 
 * note that the base-base-base- interface = AlgorithmIntf implements Serializable !!
 * 
 */
public class AdaptiveLogShift extends AlgoTransformationAbstract {
 
	private static final long serialVersionUID = 4214942071857241186L;

	
	String versionStr = "1.00.01";
	
	double shiftParam = 0.1;
	boolean produceNormalized = false ;
	
	
	// ------------------------------------------------------------------------
	public AdaptiveLogShift() {

	}
	// ------------------------------------------------------------------------
	
	@Override
	public String getVersion() {
		return versionStr;
	}
	@Override
	public String getDescription() {
		autoDescription= "" ;
		return autoDescription;
	}
	
	
	@Override
	public String[] showAvailableParameters() {
		String[] paramsDescription = new String[0];
		return paramsDescription;
	}

	
	@Override
	public int calculate() {
		//  
		int result = -1;
		double v, vr, _min, _max;
		boolean hb;
		ArrayList<Integer> mvIxesList = new ArrayList<Integer>(); 
		
		outvalues.clear();
		
		// get parameters 
		_min = dataDescription.getMin() ;
		_max = dataDescription.getMax() ;
		
		if (dataDescription.isComplete()==false){
			return -2;
		}
		
			
		// perform linear normalization
		try{
			
			_max = -9999999999999999.09; _min = 999999999999999999999.09 ;
			outvalues.clear() ;
			
			for (int i=0;i<values.size(); i++){
				
				v = (Double)values.get(i) ;
				// v = handlingRangeProtection(v,2);
				
				if ( v!= -1.0 ){
					// the actual calculation ...
					if (v>0.0){
						vr = Math.log( shiftParam + v) ;
						
						if (_min>vr)_min=vr;
						if (_max<vr)_max=vr;
					}else{
						vr=-1.0 ;
					}
					
					hb = setCalculationResultValue(i,vr) ;
					// put value to outvalues<>
				} else{
					setCalculationResultValue(i,-1.0) ;
					mvIxesList.add(i);
				}
				
			}// -> all values
			
			// we normalize in order to avoid negative values outside
			for (int i = 0; i < outvalues.size(); i++) {

				v = outvalues.get(i);
				if (mvIxesList.indexOf(i) < 0) {
					// (v!=-1.0) ... since our results reach into the neg, that's unsafe... 
					vr = (v - _min) / (_max - _min);
					outvalues.set(i, vr);
				}
			}
			
			result=0;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;		
	}

	
	@Override
	public void setParameters(ArrayList<Object> params) throws Exception {
		ArrayList<AlgorithmParameter> items ;
		int n=0;
		
		super.setParameters(params);
		
		items = parameters.getItems();
		n = items.size() ;
		
		if (n>0){
			shiftParam = items.get(0).getNumValue() ;
			if (n>1){
				produceNormalized = items.get(0).getNumValue() > 0.5 ;
			}
		}
	}
	

	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}

}

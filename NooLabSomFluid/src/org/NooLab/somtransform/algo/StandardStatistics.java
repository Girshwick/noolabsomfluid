package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.DataDescription;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementAbstract;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;


/**
 * 
 * 
 * determines min, max, stddev, var, coeff.of var., 90% & 95% quantiles based on median
 * 	          histogram, description of histogram by fitted functions 
 *
 *
 */
public class StandardStatistics extends AlgoMeasurementAbstract {

	private static final long serialVersionUID = -3606370657144611914L;

	
	String versionStr = "1.00.01" ;
	
	DataDescription dataDescription = new DataDescription();
	
	// ------------------------------------------------------------------------
	public StandardStatistics(){
		
	}
	// ------------------------------------------------------------------------	
	
	 
	 
	@Override
	public String getVersion() {
		return versionStr;
	}
	/**
	 * 
	 * creates the data description;
	 * the algo itself does not prepare any return of data, the providing of data is handled by the stack itself; 
	 *  
	 */
	@Override
	public int calculate() {
		int result = -1, n=0;
		double v, min= 9999999999999999999999999999999.0901, max= -9999999999999999999999999999.0901;
		double sum=0, qsum=0, _mean;
		
		
		
		try{
			
			for (int i=0;i<values.size();i++){
				v = values.get(i) ;
				if ( v!= -1.0 ){  // TODO: apply MV parameters for arbitrary MV's
					 
					if (min>v)min=v;
					if (max<v)max=v;
					sum  = sum + v;
					qsum = qsum + v*v;
					n++;
				} // -> all values
			}
			if (n>0){
				_mean = sum/n ;
			}else{
				_mean = -1.0;
				dataDescription.setComplete(false) ; 
			}
			if (_mean>=0){
				dataDescription.setComplete(true);
				
				dataDescription.setMean(_mean);
				dataDescription.setMin(min) ;
				dataDescription.setMax(max) ;
			}
			// histogram description
			
			
			// 
			
			result=0;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}


	@Override
	public DataDescription retrieveDescriptiveResults() {
		
		return dataDescription;
	}


	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}


}

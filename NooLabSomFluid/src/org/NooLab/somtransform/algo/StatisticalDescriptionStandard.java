package org.NooLab.somtransform.algo;

import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somfluid.util.NumUtils;
import org.NooLab.somtransform.DataDescription;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementAbstract;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;


/**
 * 
 * 
 * determines min, max, stddev, var, coeff.of var., 
 *
 *
 */
public class StatisticalDescriptionStandard extends AlgoMeasurementAbstract {

	private static final long serialVersionUID = -3606370657144611914L;

	
	String versionStr = "1.00.01" ;
	
	DataDescription dataDescription = new DataDescription();
	
	// ------------------------------------------------------------------------
	public StatisticalDescriptionStandard(){
		super();
		
		dataDescription = new DataDescription();
	}
	// ------------------------------------------------------------------------	
	
	 
	 
	@Override
	public String getVersion() {
		return versionStr;
	}
	/**
	 * 
	 * creates the data description;
	 * the algorithm itself does not prepare any return of data, the providing of data is handled by the stack itself; 
	 *  
	 */
	@Override
	public int calculate() {
		int result = -1, n=0;
		double v, min= 9999999999999999999999999999999.0901, max= -9999999999999999999999999999.0901;
		double sum=0, qsum=0, _mean, _variance=0.0,_median;
		ArrayList<Double> activeValues = new ArrayList<Double>();
		
		
		try{
			
			if ( parameters.isRecalculationBlocked() ){
				return 0;
			} // recalculationBlocked == false ?
			
			for (int i=0;i<values.size();i++){
				v = values.get(i) ;
				if ( v!= -1.0 ){  // TODO: apply MV parameters for arbitrary MV's
					 
					if (min>v)min=v;
					if (max<v)max=v;
					sum  = sum + v;
					qsum = qsum + v*v;
					n++;
					activeValues.add(v);
				} // -> all values
			}
			if (n>0){
				_mean = sum/(double)n ;
			}else{
				_mean = -1.0;
				dataDescription.setComplete(false) ; 
			}
			if (_mean!=-1.0){
				
				int mp = (int)((double)activeValues.size()/2.0) ;
				if (mp>=activeValues.size())mp = activeValues.size()-1;
				
				Collections.sort( activeValues ) ;
				
				_median = activeValues.get( mp );
				_variance = NumUtils.lazyVariance(sum, qsum, n) ;
				
				
				dataDescription.setComplete(true);
				
				dataDescription.setMean(_mean);
				dataDescription.setMin(min) ;
				dataDescription.setMax(max) ;
				
				dataDescription.setVariance(_variance);
				dataDescription.setMedian(_median) ;
				
				mp = (int)((double)activeValues.size() * 0.2) ;
				if (mp>=activeValues.size())mp = activeValues.size()-1;
				v = activeValues.get( mp );
				dataDescription.getQuantiles().add(v) ;
				
				mp = (int)((double)activeValues.size() * 0.8) ;
				if (mp>=activeValues.size())mp = activeValues.size()-1;
				v = activeValues.get( mp );
				dataDescription.getQuantiles().add(v) ;
				
			}
			// histogram description

			
			// 
			result=0;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 *  all algorithms with particular dynamic parameters that should be transferred to further instances 
	 *  of "SomTransformer" should overwrite this;</br></br>
	 *  the  getParameters() is available through the topmost interface "AlgorithmIntf", that is, independent 
	 *  of the type of the algorithm.</br></br>
	 * 	The structure "AlgorithmParameters" remains the same for all: it is a container that contains an ArrayList[AlgorithmParameter],
	 *  where "AlgorithmParameter" provides various slots; </br></br>
	 *  Since the algorithm infrastructure does not know of anything about those parameters, the algorithm also should
	 *  implement "setParameters(AlgorithmParameters)"
	 * 
	 */
	@Override
	public AlgorithmParameters getParameters() {

		parameters.clear();
		
		AlgorithmParameter algoparam = new AlgorithmParameter();
		
		algoparam.setObj( dataDescription );
		parameters.add(algoparam) ;
		
		return parameters;
	}
	
	/**
	 * the "setParameters()" should match "getParameters", only the implementation of the algorithm itself
	 * knows how to extract the structure into local variables/classes
	 * 
	 */
	@Override
	public void setParameters( AlgorithmParameters algorithmParams) {
		
		Object obj;
		
		parameters.clear();
		
		
		if ((algorithmParams==null) || (algorithmParams.getItems()==null) || 
			(algorithmParams.getItems().size()==0)){
			return;
		}
		obj = algorithmParams.getItem(0).getObj() ;
		
		
		AlgorithmParameter algoparam = new AlgorithmParameter();
		algoparam.setObj(obj) ;
		
		parameters.add(algoparam) ;
		
		dataDescription = (DataDescription)obj;
	}

	
	@Override
	public DataDescription retrieveDescriptiveResults() {
		
		return dataDescription;
	}


	@Override
	public ArrayList<Double> getValues(int part) {
		 
		return null;
	}


	@Override
	public ArrayList<Double> getDescriptiveResults() {
	 
		return null;
	}



	@Override
	public int getRangeViolationCounter() {
		return 0;
	}



	@Override
	public void setRangeViolationCounter(int rangeViolationCounter) {
	}

 
	

}

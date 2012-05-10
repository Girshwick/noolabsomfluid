package org.noolab.algoplug.timeseries;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;


/**
 * 
 * 
 *
 */
public class RunningMean extends AlgoTransformationAbstract {

	private static final long serialVersionUID = 8359446951548360237L;
	
	String versionStr = "1.00.01" ;
	
	// the parameters that are semantically specific for this algorithm
	// the are introduced through the class AlgorithmParameters, which provides semantically neutral data slots.
	int windowLength = 3 ;

	
	// ------------------------------------------------------------------------
	public RunningMean(){
		super();
	}
	// ------------------------------------------------------------------------
	

	@Override
	public String getVersion() {
		 
		return versionStr;
	}

	@Override
	public String getDescription() {
		autoDescription = "Running Mean" ;
		return autoDescription;
	}
	
	@Override
	public int calculate() {
		double v;
		windowLength = 1 ;
		// important !
		outvalues.clear();
		
		/*_ALGO_RANGEVIOLATION_EXCEPTION   = 1;
		public static final int _ALGO_RANGEVIOLATION_DROPVALUE   = 3;
		public static final int _ALGO_RANGEVIOLATION_AUTOCORRECT
		*/
		// perform desired activity
		try{
			
			if (windowLength>values.size()-10)windowLength=values.size();
			if (windowLength<2)windowLength=2;
			
			double windowedValuesSum=0, currentMean=-1.0;
			int currentN=0;
			
			ArrayList<Double> windowedValues = new ArrayList<Double>();
			
			for (int i=0;i<windowLength;i++){
				v = (Double)values.get(i) ;
				
				if ( v!= -1.0 ){
					windowedValuesSum = windowedValuesSum + v;
					windowedValues.add(v);
					currentN++;
				}
				// dependent on option as provided by param : -1.0 or smooth intro with min of 3 values
				outvalues.add(-1.0);
			}
			
			for (int i=windowLength;i<values.size(); i++){
				
				v = (Double)values.get(i) ;
				 
				// applying range violation handling mode, provided by "AlgoTransformationAbstract"
				// the options for that are set during loading, and contracted through SomFluidProperties
				v = handlingRangeProtection(v);
				
					
				if ( v!= -1.0 ){
					
					// the actual calculation ...
					v = v + 2.718182 ;
					
					// storing the result value
					// hb = setCalculationResultValue(i, v);

					windowedValuesSum = windowedValuesSum + v;
					windowedValues.add(v);
					
					if (windowedValues.get(0) != -1.0){
						windowedValuesSum = windowedValuesSum - windowedValues.get(0) ;
						currentMean = windowedValuesSum/currentN; 
					} 
					windowedValues.remove(0);					
					if (i>=windowLength){
						if (i < outvalues.size()) {
							outvalues.set(i, currentMean);
						} else {
							outvalues.add(currentMean);
						}
					} // i>windowLength ?
				}
				
			} // i-> all values
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return 0;
	}


	

	@Override
	public void setParameters(ArrayList<Object> params) throws Exception {
		
		ArrayList<AlgorithmParameter> algoParams ; 
		
		// AlgorithmParameters parameters ;
		// is a list of ArrayList<AlgorithmParameter> items, where "AlgorithmParameter" is a simple object that
		// provides several typed slots for storing values, and a list for untyped (=object) values
		// the meaning is given by the designer and the caller !
		
		if ((params==null) || (params.size()==0)){
			return;
		}
		
		int n1=0, n2=0;
		
		if (parameters!=null){
			n1 = parameters.getItems().size() ;
		}
		
		super.setParameters(params) ; 
		algoParams = parameters.getItems();
		
		n2= algoParams.size() ;
		
		if (n2<=n1){
			// TODO: on option: silent or throwing exception...
			throw(new Exception("assimilating parameters (n="+params.size()+") failed."));
			// return;
		}
		if (algoParams.size()>0){
			windowLength = (int) algoParams.get(0).getNumValue() ;
		}
	}



	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

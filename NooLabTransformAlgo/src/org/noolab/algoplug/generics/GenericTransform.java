package org.noolab.algoplug.generics;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;





public class GenericTransform extends AlgoTransformationAbstract {

	private static final long serialVersionUID = 8359446951548360237L;
	
	String versionStr = "1.00.01" ;
	
	
	public GenericTransform(){
		super();
	}

	

	@Override
	public String getVersion() {
		 
		return versionStr;
	}

	@Override
	public String getDescription() {
		autoDescription = "Generic Transformation" ;
		return autoDescription;
	}
	
	@Override
	public int calculate() {
		double v;
		int windowLength = 1 ;
		// important !
		outvalues.clear();
		
		/*_ALGO_RANGEVIOLATION_EXCEPTION   = 1;
		public static final int _ALGO_RANGEVIOLATION_DROPVALUE   = 3;
		public static final int _ALGO_RANGEVIOLATION_AUTOCORRECT
		*/
		// perform desired activity
		try{
			
			// AlgorithmParameters parameters ;
			// is a list of ArrayList<AlgorithmParameter> items, where "AlgorithmParameter" is a simple object that
			// provides several typed slots for storing values, and a list for untyped (=object) values
			// the meaning is given by the designer and the caller !
			
			windowLength = (int) parameters.getItems().get(0).getNumValue() ;
			
			if (windowLength>values.size()-10)windowLength=values.size();
			if (windowLength<1)windowLength=1;
			
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
			}
			
			for (int i=0;i<values.size(); i++){
				
				v = (Double)values.get(i) ;
				 
				// applying range violation handling mode
				// v = handlingRangeProtection(v);
				
				
					
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
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

import org.NooLab.somtransform.DataDescription;
import org.NooLab.utilities.datatypes.IndexedDistances;



abstract public class AlgoTransformationAbstract implements AlgoTransformationIntf{

	private static final long serialVersionUID = 2971433693993395455L;

	int typeInfo = AlgorithmIntf._ALGOTYPE_VALUE ;
	 
	// ....................................................

	protected DataDescription dataDescription;
	
	protected ArrayList<ArrayList<Double>> valueTable = new ArrayList<ArrayList<Double>>();
	protected ArrayList<Double> values ;
	protected ArrayList<String> stringvalues ;
	
	protected ArrayList<Double> outvalues = new ArrayList<Double>() ;
	protected ArrayList<String> outstringvalues = new ArrayList<String>() ;
	
	protected boolean isStrData ;
	
	protected int rangeViolationHandlingMode = AlgorithmIntf._ALGO_RANGEVIOLATION_AUTOCORRECT ;
	protected int rangeViolationCounter=0;
	
	protected boolean hasParameters = false; // will be true in case of algos like NumValEnum
	
	protected AlgorithmParameters parameters ; 
	
	protected String autoDescription = "" ;
	
	// ------------------------------------------------------------------------
	public AlgoTransformationAbstract(){
		parameters = new AlgorithmParameters( this) ;
	}
	// ------------------------------------------------------------------------
	 
	@Override
	public double handlingRangeProtection( double value){
		
		if ((value<0) || (value>1.0)){
			if (value!=1.0){
				rangeViolationCounter++;
			}
			if (rangeViolationHandlingMode == AlgorithmIntf._ALGO_RANGEVIOLATION_EXCEPTION){
				
			}
			if (rangeViolationHandlingMode == AlgorithmIntf._ALGO_RANGEVIOLATION_DROPVALUE){
				value = -1.0 ;
			}
			if (rangeViolationHandlingMode == AlgorithmIntf._ALGO_RANGEVIOLATION_AUTOCORRECT){
				if ((value<0) || (value!=1.0)){
					value=0.0;
				}
				if (value>1.0){
					value = 1.0 ;
				}
			}
		}
		
		return value;
	}
	
	@Override
	public int getType() {
		return typeInfo;
	}
	
	abstract public String getDescription() ;
	
	
	@Override
	public void setDatDescription(DataDescription datadescription) {
		
		dataDescription = datadescription; 
	}
	
	
	protected boolean setCalculationResultValue(int index, double value) {
		boolean rB=true;
		
		if (index < outvalues.size()) {
			outvalues.set(index , value);
		} else {
			outvalues.add(value);
		}
		
		return rB;
	}
	
	
	@Override
	public int setValues(ArrayList<ArrayList<?>> inValues) {
		
		int result = -1;
		ArrayList list;
		Object obj;
		String cn;
		
		try{
			if ((inValues==null) || (inValues.size()==0)){
				return result -3;
			}
			
			list = inValues.get(0) ;
			if ((list==null) || (list.size()==0)){
				return result -4;
			}
			
			obj = list.get(0) ;
			cn = obj.getClass().getSimpleName() ;
			
			if (cn.toLowerCase().contains("string")){
				stringvalues = (ArrayList<String>) inValues.get(0); 
				isStrData = true;
				result = stringvalues.size() ;
				
			}else{
				isStrData = false;
				
				values = (ArrayList<Double>) inValues.get(0);
				if (values!=null){
					result = values.size() ;
				}
				valueTable.clear();
				for (int i=0;i<inValues.size();i++){
					if ((inValues.get(i)!=null) && (inValues.get(i).size()>0)){
						valueTable.add( (ArrayList<Double>) inValues.get(i)) ;
					}
				}
			} // num
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return result;
	}
	
	
	@Override
	public ArrayList<Double> getValues(int part) {
		 
		return outvalues;
	}
	
	abstract public int calculate() ;
	
	
	@Override
	public ArrayList<Double> getTransformedValues() {
		 
		return outvalues;
	}
 

	/**
	 * @return the hasParameters
	 */
	public boolean hasParameters() {
		return hasParameters;
	}


	@Override
	public AlgorithmParameters getParameters() {
		return parameters;
	}
	
	
	@Override
	public void setParameters(ArrayList<Object> params) throws Exception {
		 
		Object obj ;
		String cn, str;
		AlgorithmParameter algoparam ;
		
		str="";
		if ((params!=null) && (params.size()>0)){
			if (parameters==null){
				parameters = new AlgorithmParameters( this) ;
			}
		}
		
		for (int i=0;i<params.size();i++){
			
			obj = params.get(i);
			cn  = obj.getClass().getSimpleName() ;

			if (cn.toLowerCase().contains("string[]")){
				
				continue;
			}
			if (cn.toLowerCase().contains("string")){
				
				str = (String)obj;
				algoparam = new AlgorithmParameter();
				algoparam.setLabel(str) ;
				parameters.add(algoparam);
				
				continue;
			}
			if (cn.toLowerCase().contains("int[]")){
				continue;
			}
			if (cn.toLowerCase().contains("double[]")){

				double[] numvalues = (double[])(obj);

				algoparam = new AlgorithmParameter();
				algoparam.setNumValues(numvalues) ;
				parameters.add(algoparam);

				continue;
			}
			
			if (cn.toLowerCase().contains("int")){
				
				int numvalue = (int)((Integer)obj);
				
				algoparam = new AlgorithmParameter();
				algoparam.setNumValue( (double)numvalue ) ;
				parameters.add(algoparam);

				continue;
			}
			
			if (cn.toLowerCase().contains("double")){
				
				double numvalue = (double)((Double)obj);
				
				algoparam = new AlgorithmParameter();
				algoparam.setNumValue(numvalue) ;
				parameters.add(algoparam);

				continue;
			}

			if (cn.toLowerCase().contains("arraylist")){
				continue;
			}
			
			
			// IndexedDistances ixds = IndexedDistances.class.cast(obj) ;
			algoparam = new AlgorithmParameter();
			algoparam.obj = obj;
			parameters.add(algoparam);
		}
		
	}
	
	public String[] showAvailableParameters() {
		String[] paramsDescription = new String[0];
		return paramsDescription;
	}	
	

	public int getRangeViolationCounter() {
		return rangeViolationCounter;
	}

	public void setRangeViolationCounter(int rvCounter) {
		this.rangeViolationCounter = rvCounter;
	}
	
}

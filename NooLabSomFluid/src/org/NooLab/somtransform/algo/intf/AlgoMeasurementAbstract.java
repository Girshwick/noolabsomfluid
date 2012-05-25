package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;




abstract public class AlgoMeasurementAbstract implements AlgoMeasurementIntf {
 
	private static final long serialVersionUID = 4931940602875815724L;
	
	protected int typeInfo = AlgorithmIntf._ALGOTYPE_PASSIVE ;
	
	
	// ....................................................

	ArrayList<ArrayList<Double>> valueTable = new ArrayList<ArrayList<Double>>();
	
	protected ArrayList<Double> values = new ArrayList<Double>() ;
	ArrayList<String> stringvalues = new ArrayList<String>() ;
	
	ArrayList<Double> outvalues = new ArrayList<Double>() ;
	ArrayList<String> outstringvalues = new ArrayList<String>() ;
	
	boolean isStrData ;
	
	protected boolean hasParameters = false; // will be true in case of algos like NumValEnum
	
	protected AlgorithmParameters parameters ; 
	
	
	// ------------------------------------------------------------------------
	public AlgoMeasurementAbstract(){
		parameters = new AlgorithmParameters(this) ;
	}
	// ------------------------------------------------------------------------

	@Override
	public int getType() {
		return typeInfo;
	}
	
	
	@Override
	public int setValues( ArrayList<ArrayList<?>> inValues) {
		int result = -1;
		ArrayList list;
		Object obj;
		String cn;
		
		try{
			
			list = inValues.get(0) ;
			obj = list.get(0) ;
			cn = obj.getClass().getSimpleName() ;
			
			if (cn.toLowerCase().contains("string")){
				stringvalues = (ArrayList<String>) inValues.get(0);
				isStrData = true;
				values = new ArrayList<Double>();
			}else{
				isStrData = false;
				
				values = (ArrayList<Double>) inValues.get(0);
				if (values!=null){
					result = values.size() ;
				}
				for (int i=0;i<inValues.size();i++){
					if ((inValues.get(i)!=null) && (inValues.get(i).size()>0)){
						valueTable.add((ArrayList<Double>) inValues.get(i)) ;
					}
				}
			} // num
			
		}catch(Exception e){
		}
		return result;
	}
	
	
	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}
	
	abstract public int calculate() ;
	
	
	@Override
	public String[] showAvailableParameters() {
		
		return null;
	}
	
	
	@Override
	public AlgorithmParameters getParameters() {
		
		return parameters;
	}
	
	
	@Override
	public void setParameters( AlgorithmParameters algorithmParams) {
	}
	
	@Override
	public void setParameters(ArrayList<Object> params) throws Exception {
	}

	
	
}

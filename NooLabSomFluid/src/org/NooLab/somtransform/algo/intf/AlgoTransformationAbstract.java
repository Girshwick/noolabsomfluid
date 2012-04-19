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
	
	protected boolean hasParameters = false; // will be true in case of algos like NumValEnum
	
	protected AlgorithmParameters parameters ; 
	
	// ------------------------------------------------------------------------
	public AlgoTransformationAbstract(){
		
	}
	// ------------------------------------------------------------------------
	 

	@Override
	public int getType() {
		return typeInfo;
	}
	
	
	@Override
	public void setDatDescription(DataDescription datadescription) {
		
		dataDescription = datadescription; 
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
	public void setParameters(ArrayList<Object> params) {
		 
		Object obj ;
		String cn, str;
		AlgorithmParameter algoparam ;
		
		str="";
		
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
				continue;
			}
			if (cn.toLowerCase().contains("double")){
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
	
}

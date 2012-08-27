package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

public class AlgorithmParametersHelper {

	
	public AlgorithmParametersHelper(){
		
	}

	public AlgorithmParameters assimilate( AlgorithmIntf algo,  AlgorithmParameters algorithmparams ) {
		AlgorithmParameters parameters = new AlgorithmParameters(algo);
		
		
		
		
		return parameters;
	}
	
	public AlgorithmParameters assimilateOpenObjectList( AlgorithmIntf algo,  ArrayList<Object> params) {
		
		AlgorithmParameters parameters = new AlgorithmParameters(algo);
		

		Object obj ;
		String cn, str;
		AlgorithmParameter algoparam ;
		 
		
		str="";
		if ((params==null) || (params.size()==0)){
			return parameters;
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
		
		
		
		
		return parameters;
	}
	
	
	
	
}

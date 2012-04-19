package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

 


public abstract class AlgoColumnWriterAbstract implements AlgoColumnWriterIntf{


	private static final long serialVersionUID = -2872072817907944827L;
	

	protected boolean hasParameters = false; // will be true in case of algos like NumValEnum
	
	protected AlgorithmParameters parameters ; 
	
	
	// ========================================================================
	public AlgoColumnWriterAbstract(){
		
	}
	// ========================================================================
	
	@Override
	public int setValues(ArrayList values) {
		 
		return 0;
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
			
		}
		
	}

}

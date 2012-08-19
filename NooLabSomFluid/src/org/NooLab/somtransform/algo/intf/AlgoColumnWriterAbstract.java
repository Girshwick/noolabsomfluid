package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

 


public abstract class AlgoColumnWriterAbstract implements AlgoColumnWriterIntf{


	private static final long serialVersionUID = -2872072817907944827L;
	

	protected boolean hasParameters = false; // will be true in case of algorithms like NumValEnum
	
	protected AlgorithmParameters parameters ; 
	
	
	// ========================================================================
	public AlgoColumnWriterAbstract(){
		
	}
	// ========================================================================
	
	 
	@Override
	public int setValues(ArrayList values) {
		 
		return 0;
	}
	 
	/*
	@Override
	public int setValuesLists(ArrayList<ArrayList<?>> inValues) {
		// TODO Auto-generated method stub
		return 0;
	}
	*/
	

	@Override
	public AlgorithmParameters getParameters() {
		return parameters;
	}
	
	@Override
	public void setParameters(ArrayList<Object> params) {
		
		basicParametersAssimilation(params) ;
	}
	
	
	@Override
	public void setParameters(AlgorithmParameters algorithmParams) {
		
		basicParametersAssimilation(algorithmParams) ;
	}

	
	
	protected void basicParametersAssimilation(AlgorithmParameters algorithmParams){
		
		AlgorithmParametersHelper paramsHelper = new AlgorithmParametersHelper ();
		
		parameters = paramsHelper.assimilate(this, algorithmParams);
	}
	
	protected void basicParametersAssimilation( ArrayList<Object> params ){
		
		AlgorithmParametersHelper paramsHelper = new AlgorithmParametersHelper ();
		
		parameters = paramsHelper.assimilateOpenObjectList(this, params);
		
	}

}

package org.NooLab.somfluid.core.engines.det;

import java.io.Serializable;



public class ResultRequests implements Serializable{

	private static final long serialVersionUID = -9081940836686889937L;

	int identifier = -1 ; 
	double[] parameters = new double[0];
	
	// ------------------------------------------------------------------------
	public ResultRequests(){
		
	}
	// ------------------------------------------------------------------------


	public void setIdentifier(int extResultIdentifier) {
		// 
		identifier = extResultIdentifier;
	}
  

	public void setParameters(double[] params) {
		//  
		if ((params!=null) && (params.length>0)){
			parameters = new double[params.length];
			System.arraycopy(params, 0, parameters, 0, params.length ) ;
		}
		
	}


	public int getIdentifier() {
		return identifier;
	}


	public double[] getParameters() {
		return parameters;
	}
	
	
	
	
	
	
}

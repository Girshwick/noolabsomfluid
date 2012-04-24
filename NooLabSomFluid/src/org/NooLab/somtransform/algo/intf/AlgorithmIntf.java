package org.NooLab.somtransform.algo.intf;

import java.io.Serializable;
import java.util.ArrayList;



public interface AlgorithmIntf extends AlgorithmParameterIntf, Serializable{
	
	// 0= passive, 1=value transform, 2=column writer, determines the interface and thus the handling of data 
	
	public static final int _ALGOTYPE_GENERIC = 0 ; 

	public static final int _ALGOTYPE_PASSIVE = 2 ; 
	public static final int _ALGOTYPE_VALUE   = 3 ; 
	public static final int _ALGOTYPE_WRITER  = 4 ; 
	

	public int getType();
	
	public String getVersion();
	
	/*
	 * TODO:  further properties:  descriptiveLabel, descriptionText  
	 */
		
	//public int setValues( ArrayList< ArrayList<T> > values  ) ;
	
	public int calculate() ;

	public ArrayList<Double> getValues( int part ) ;
	
	public ArrayList<Double> getDescriptiveResults() ;
	
	public AlgorithmParameters getParameters() ;
	
	public void setParameters( ArrayList<Object> params) ;

	public int setValues(ArrayList<ArrayList<?>> inValues);
	

	
}
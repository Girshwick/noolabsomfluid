package org.NooLab.chord.util;

public class ThrdUtilities {

	public ThrdUtilities(){
		
	}
	
	
	public int minimum( int[] values){
		int result = -1;
		int rv = 999999999;
		
		for (int i=0;i<values.length;i++){
			rv = Math.min(rv,values[i]) ;
		}
		
		result = rv ;
		
		return result ;
	}

	public int maximum( int[] values){
		int result = -1;
		int rv = -999999999;
		
		for (int i=0;i<values.length;i++){
			rv = Math.min(rv,values[i]) ;
		}
		
		result = rv ;
		
		return result ;
	}
	

	public double minimum( double[] values){
		double result = -1;
		double rv = 999999999.0;
		
		for (int i=0;i<values.length;i++){
			rv = Math.min(rv,values[i]) ;
		}
		
		result = rv ;
		
		return result ;
	}

	public double maximum( double[] values){
		double result = -1;
		double rv = -999999999.0;
		
		for (int i=0;i<values.length;i++){
			rv = Math.min(rv,values[i]) ;
		}
		
		result = rv ;
		
		return result ;
	}
	
	
}

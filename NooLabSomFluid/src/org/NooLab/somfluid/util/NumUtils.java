package org.NooLab.somfluid.util;

public class NumUtils {

	
	public NumUtils(){
		
	}
	

	public double lazyvariance( double sum,
	                            double sqsum,
	                            int n){
		return  sqsum /n - (sum/n)*(sum/n) ;
	}

	
	
	
}

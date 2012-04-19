package org.NooLab.somfluid.util;

public class NumUtils {

	
	public NumUtils(){
		
	}
	
	public static double lazyVariance( double sum,
            					double sqsum,
            					int n){
		return  sqsum /n - (sum/n)*(sum/n) ;
		
	}
	public double lazyvariance( double sum,
	                            double sqsum,
	                            int n){
		return  lazyVariance(sum,sqsum,n) ;
	}

	
	
	
}

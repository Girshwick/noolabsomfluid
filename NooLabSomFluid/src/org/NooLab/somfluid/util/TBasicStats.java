package org.NooLab.somfluid.util;

public class TBasicStats {
	 
	public int count , mv_count, x_counter;


	public double  sum, qsum,
			mean, cov,
			soval,  // sum of values, needed for merging
			sovar,  // sum of variances
			variance,
			autocorr ,
			mini,
			maxi   ;
	public int[] context_covered_nodes  ;
	 
}

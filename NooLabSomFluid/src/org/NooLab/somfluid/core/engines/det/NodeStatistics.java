package org.NooLab.somfluid.core.engines.det;

 
import java.util.Vector;

import org.NooLab.somfluid.util.TBasicStats;
 



public class NodeStatistics {

	public  Vector<TBasicStats> per_field ;
	
	public NodeStatistics(){
		per_field = new Vector<TBasicStats>();
		
	}
	
    public void resetFieldStatistics( int n){
    	
    	TBasicStats basic_stats_element ;
    	
    	
    	basic_stats_element = per_field.get(n);
    	basic_stats_element.count =0;
    	basic_stats_element.mv_count=0;
    	basic_stats_element.x_counter=0;


    	basic_stats_element.sum=0;
    	basic_stats_element.qsum=0;
    			
    	basic_stats_element.mean=0;
    	basic_stats_element.cov=0;
    	
    	basic_stats_element.soval =0;  // sum of values, needed for merging
    	basic_stats_element.sovar =0;  // sum of variances
    	basic_stats_element.variance=0;
    	basic_stats_element.autocorr =0;
    	basic_stats_element.mini=0;
    	basic_stats_element.maxi=0;      	
    	
    }
}

package org.NooLab.somfluid.core.engines.det;

 
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.util.BasicStatisticalDescription;
 



public class NodeStatistics {

	public  ArrayList<BasicStatisticalDescription> per_field ;
	
	public NodeStatistics(){
		per_field = new ArrayList<BasicStatisticalDescription>();
		
	}
	
    public void resetFieldStatistics( int n){
    	
    	BasicStatisticalDescription basic_stats_element ;
    	
    	
    	basic_stats_element = per_field.get(n);
    	
    	basic_stats_element.reset();
    	  	
    	
    }
}

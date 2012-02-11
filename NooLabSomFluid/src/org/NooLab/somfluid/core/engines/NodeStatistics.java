package org.NooLab.somfluid.core.engines;

 
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
 



public class NodeStatistics {

	ArrayList<BasicStatisticalDescription> fieldValues ;
	
	ArrayList<Variable> variables;
	
	
	// ========================================================================
	public NodeStatistics(){
		fieldValues = new ArrayList<BasicStatisticalDescription>();
		
	}
	// ========================================================================
	
	
	
    public void resetFieldStatistics( int n){
    	
    	BasicStatisticalDescription basic_stats_element ;
    	
    	
    	basic_stats_element = fieldValues.get(n);
    	
    	basic_stats_element.reset();
    }
    
    public void resetFieldStatisticsAll() {
		//  
		for (int i=0;i<fieldValues.size();i++){
			
			resetFieldStatistics(i);
		}
		
	}
    
	public void addRecordData( ArrayList<Double> vector){
    	
    }
    
    public void removeRecordData( ArrayList<Double> vector){
    	BasicStatisticalDescription fieldStats;
    	double v, rv;
    	
    	if (fieldValues.size() == vector.size()){
    	
    		for (int i=0;i<fieldValues.size();i++){
    			
    			v = vector.get(i) ;
    			
    			fieldValues.get(i).removeValue(v) ;
    			    			
    		}// i-> all positions in node structure
    	}
    	
    	
    }
    
    // ------------------------------------------------------------------------

	public ArrayList<BasicStatisticalDescription> getFieldValues() {
		return fieldValues;
	}



	public void setFieldValues(ArrayList<BasicStatisticalDescription> fieldvalues) {
		this.fieldValues = fieldvalues;
	}



	public void setVariables(ArrayList<Variable> variables) {
		this.variables = variables;
	}
    
    
}

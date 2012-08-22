package org.NooLab.somfluid.core.engines;

 
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;
 



public class NodeStatisticsDetailed implements NodeStatisticsIntf{

	ArrayList<BasicStatisticalDescription> fieldValues ;
	
	ArrayList<Variable> variables;
	
	
	// ========================================================================
	public NodeStatisticsDetailed(){
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







	public void setVariables(ArrayList<Variable> variablesList) {
		int n = 0;
		
		if (variables!=null){
			variables.size();
		}
		
		variables = variablesList;
		if ((n!=0) && (n!=variables.size())){
			adjustFieldValuesVectorLen();
		}
	}
    
	private void adjustFieldValuesVectorLen(){
		int n, vn;
		
		vn = variables.size();
		n = fieldValues.size() ;
		
		if (vn>n){
			for (int i=0;i<vn-n;i++){
				fieldValues.add( new BasicStatisticalDescription(false) ) ;
			}
		}
		if (vn<n){
			for (int i=0;i<n-vn;i++){
				fieldValues.remove(vn) ;
			}
		}
		
	}



	@SuppressWarnings("unchecked")
	@Override
 	public void setFieldValues(ArrayList<?> fieldvalues) {
		if (fieldvalues!=null){
			this.fieldValues = (ArrayList<BasicStatisticalDescription>) fieldvalues;
		}
	}


	 
    
}

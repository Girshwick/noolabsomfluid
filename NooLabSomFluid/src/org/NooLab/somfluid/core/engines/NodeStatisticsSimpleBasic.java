package org.NooLab.somfluid.core.engines;

import java.util.ArrayList;

import org.NooLab.somfluid.core.categories.extensionality.BasicSimpleStatisticalDescription;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;



public class NodeStatisticsSimpleBasic implements NodeStatisticsIntf {

	
	ArrayList fieldValues ;
	
	ArrayList<Variable> variables;

	// ========================================================================
	public NodeStatisticsSimpleBasic(){
		fieldValues = new ArrayList<BasicSimpleStatisticalDescription>();
	}
	// ========================================================================
	
	
	@Override
	public void resetFieldStatisticsAll() {
		// 
		for (int i=0;i<fieldValues.size();i++){
			resetFieldStatistics(i);
		}

	}

	private void resetFieldStatistics(int i) {
		// 
		
	}
	
	@Override
	public void setFieldValues(ArrayList<?> arrayList) {
		
		if (fieldValues!=null){
			this.fieldValues = (ArrayList<BasicSimpleStatisticalDescription>) fieldValues;
		}

	}

	@Override
	public ArrayList<?> getFieldValues() {
		 
		return fieldValues;
	}


	private void adjustFieldValuesVectorLen(){
		int n, vn;
		
		vn = variables.size();
		n = fieldValues.size() ;
		
		if (vn>n){
			for (int i=0;i<vn-n;i++){
				BasicSimpleStatisticalDescription a = new BasicSimpleStatisticalDescription();
				fieldValues.add( a ) ;
				
			}
		}
		if (vn<n){
			for (int i=0;i<n-vn;i++){
				fieldValues.remove(vn) ;
			}
		}
		
	}

	
	@Override
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

	@Override

    public void removeRecordData( ArrayList<Double> vector){
    	BasicStatisticalDescriptionIntf fieldStats;
    	double v, rv;
    	
    	if (fieldValues.size() == vector.size()){
    	
    		for (int i=0;i<fieldValues.size();i++){
    			
    			v = vector.get(i) ;
    			
    			((BasicSimpleStatisticalDescription) fieldValues.get(i)).removeValue(v) ;
    			    			
    		}// i-> all positions in node structure
    	}

    }


	@Override
	public void addFieldValues(BasicStatisticalDescription basicStatisticalDescription) {
		// TODO Auto-generated method stub
		
	}


}

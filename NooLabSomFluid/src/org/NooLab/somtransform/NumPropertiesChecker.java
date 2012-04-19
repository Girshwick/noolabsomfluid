package org.NooLab.somtransform;

import java.util.Arrays;

import org.NooLab.somfluid.data.DataTable;





public class NumPropertiesChecker {

	SomTransformer somTransformer ;
	
	DataTable dataTable ;
	int columnIndex = -1;
	
	public NumPropertiesChecker( SomTransformer st, DataTable datatable, int colindex ){
		
		somTransformer = st ;
		dataTable = datatable ;
		columnIndex = colindex; 

		
	}


	public void performChecks(int[] idsOfChecksToPerform) {
		
		int[] tasks;
		tasks = new int[idsOfChecksToPerform.length] ;
		
		System.arraycopy( idsOfChecksToPerform, 0, tasks, 0, idsOfChecksToPerform.length) ;
		Arrays.sort(tasks) ;
		
		
		if (Arrays.binarySearch(tasks, 1)>=0){ // TODO: create constants for those indicators
			boolean sz  = checkForSemanticZeroes() ;	
		}
		
		if (Arrays.binarySearch(tasks, 2)>=0){
			boolean qsp = checkForQuasiSpecies() ; // kurtosis, 1-dimensional k-means, salient points (min,max,zero-regions)
			
		}
		
		if (Arrays.binarySearch(tasks, 3)>=0){
			boolean shp = checkForShapeScaling() ;	
		}

		

		

		
	}
	


	private boolean checkForShapeScaling() {

		return false;
	}


	private boolean checkForQuasiSpecies() {


		return false;
	}


	private boolean checkForSemanticZeroes() {


		return false;
	}

	
}

package org.NooLab.somfluid.core.engines.det;

public class SomMapTable {

	/** profiles across all nodes, dependent on parameters, 
	 *  either with or without (=default) inactive variables */
	public double[][] values = new double[0][0] ; 
	
	/** the variables for improved integration */
	public String[]   variables = new String[0] ;

	public int tvIndex = -1;
	
	
	// ========================================================================
	public SomMapTable(){
		
	}
	// ========================================================================


	public SomMapTable(SomMapTable smt) {
		
		values = new double[smt.values.length][smt.values[0].length] ;
		variables = new String[ smt.variables.length];
		tvIndex = smt.tvIndex ;
		
		values = smt.values.clone();
		variables = smt.variables.clone();
	}
	
	
	
	
	
}

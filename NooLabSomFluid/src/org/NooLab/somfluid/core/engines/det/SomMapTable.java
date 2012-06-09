package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;
import java.util.Arrays;

import org.math.array.DoubleArray;

public class SomMapTable {

	public static final int _TRANSLATE_INTO_INDEX_IN_SMT = 1;
	public static final int _TRANSLATE_INTO_INDEX_IN_EXT = 2;
	
	
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
		
		if (smt.values.length>0){
			values = new double[smt.values.length][smt.values[0].length] ;
			variables = new String[ smt.variables.length];
			values = DoubleArray.copy(smt.values);
			System.arraycopy( smt.variables, 0, variables, 0, variables.length);
		}
		tvIndex = smt.tvIndex ;
		
	}

	/**
	 * 
	 * @param vLabelList
	 * @param direction  there are constants: _TRANSLATE_INTO_INDEX_IN_SMT, _TRANSLATE_INTO_INDEX_IN_EXT
	 * @return
	 */
	public ArrayList<Integer> getTranslatedIndexValues( ArrayList<String> vLabelList ) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		String vlabel;
		
		
		
		//if (direction <= _TRANSLATE_INTO_INDEX_IN_SMT)
		{
			ArrayList<String> smtVarLabels = new ArrayList<String>(Arrays.asList(variables));
			
			for (int i = 0; i < vLabelList.size(); i++) {
				vlabel = vLabelList.get(i);
				int ix = smtVarLabels.indexOf(vlabel);
				indexes.add(ix);
			}
		}
		return indexes;
	}


	public ArrayList<Integer> getTranslatedIndexValues(	ArrayList<String> labelsForVariablesList,
														ArrayList<Integer> varindexes) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		String vlabel;
		
		
		for (int i = 0; i < varindexes.size(); i++) {
			
			int ix = varindexes.get(i) ;
			// vLabelList.indexOf(vlabel);
			vlabel = variables[i];
			ix = labelsForVariablesList.indexOf(vlabel) ;
			indexes.add(ix);
		}

		return indexes;
	}
	
	
	
	
	
}

package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

import org.NooLab.somtransform.DataDescription;




public interface AlgoTransformationIntf  extends AlgorithmIntf{

	
	public ArrayList<Double> getTransformedValues() ;

	public void setDatDescription(DataDescription dataDescription);

	public boolean hasParameters() ;
	
	// ....................................................
	
	/** 
	 * other algorithms do not need the possibility to digest a table, only value transformers, 
	 * such like arithmetic expressions, (cross-)correlation functions, other statistical stuff,
	 * or residual functions (such like those based on clustering)  ;</br>
	 * 
	 * @param inValueCol ArrayList<?>, usually ? is of Double
	 * @return the resulting number of columns in the internal value table
	 */
	public int addValueTableColumn( ArrayList<?> inValueCol );

	public void setValueTableColumn(int index, ArrayList<?> inValueCol);
	
	public void clearValueTable();

	// ....................................................
	
	public String getDescription() ;

	// ....................................................
	
	double handlingRangeProtection(double value);
		
	public int getInputColumnsCount() ;
	public void setInputColumnsCount( int inColCount ) ;

	
}

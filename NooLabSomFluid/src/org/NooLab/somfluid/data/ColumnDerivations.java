package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.NooLab.somfluid.structures.DataTableCol;
import org.NooLab.somfluid.util.Formula;





public class ColumnDerivations implements Serializable {

	private static final long serialVersionUID = 8212635569371820930L;
 

	ArrayList<Formula> formulaStack = new ArrayList<Formula>();
	
	Map<Integer,String> formulaVariables = new HashMap<Integer,String>();
	
	ArrayList<DataTableCol> inColumns  = new ArrayList<DataTableCol>(); 
	ArrayList<DataTableCol> outColumns = new ArrayList<DataTableCol>(); 
	
	ArrayList<Double> outCellValues = new ArrayList<Double>() ;
	
	transient ColumnDerivations cd;
	
	// ========================================================================
	public ColumnDerivations() {
		cd = this;
	}
	public ColumnDerivations(ColumnDerivations inDerivations) {
		cd = this;
		transferContent( inDerivations, cd);
		
		
	}
	// ========================================================================
	
	private void transferContent( ColumnDerivations inDv, ColumnDerivations outDv){
		
		if (inDv==null){
			
			outDv.formulaStack = new ArrayList<Formula>();
			
			
			outDv.formulaVariables = new HashMap<Integer,String>( );
			
			outDv.inColumns = new ArrayList<DataTableCol>(); 
			outDv.outColumns = new ArrayList<DataTableCol>(); 
			
			outDv.outCellValues = new ArrayList<Double>() ;
			return;
		}
		
		outDv.formulaStack = cloneFormulaStack( inDv.formulaStack  );
		
		outDv.formulaVariables = new HashMap<Integer,String>( inDv.formulaVariables );
		
		outDv.inColumns = new ArrayList<DataTableCol>( inDv.inColumns ); 
		outDv.outColumns = new ArrayList<DataTableCol>( inDv.outColumns ); 
		
		outDv.outCellValues = new ArrayList<Double>( inDv.outCellValues ) ;
		
		
	}
	
	private ArrayList<Formula> cloneFormulaStack( ArrayList<Formula> fStack) {

		ArrayList<Formula> formulastack = new ArrayList<Formula>( );
		// alternatively: en./de-code of stringed objects for a complete decoupling
		 
		return formulastack;
	}
	// ------------------------------------------------------------------------
	
	public ArrayList<DataTableCol> getInColumns() {
		return inColumns;
	}

	public ArrayList<DataTableCol> getOutColumns() {
		return outColumns;
	}

	

	@SuppressWarnings("unchecked")
	public void setFormulaStack(ArrayList<Formula> fs){
	
		formulaStack.clear() ;
		formulaStack = (ArrayList<Formula>) fs.clone() ;
	}

	public void addFormulaStack(ArrayList<Formula> fs){
	
		for (int i=0;i<fs.size();i++){
			addFormula( fs.get(i)) ;
		}
		
	}

	public void addFormula(Formula f){
		
		formulaStack.add( f ) ;
	}
	public Map<Integer, String> getFormulaVariables() {
		return formulaVariables;
	}
	public void setFormulaVariables(Map<Integer, String> formulaVariables) {
		this.formulaVariables = formulaVariables;
	}
	public ArrayList<Double> getOutCellValues() {
		return outCellValues;
	}
	public void setOutCellValues(ArrayList<Double> outCellValues) {
		this.outCellValues = outCellValues;
	}
	public ArrayList<Formula> getFormulaStack() {
		return formulaStack;
	}
	public void setInColumns(ArrayList<DataTableCol> inColumns) {
		this.inColumns = inColumns;
	}
	public void setOutColumns(ArrayList<DataTableCol> outColumns) {
		this.outColumns = outColumns;
	}
}

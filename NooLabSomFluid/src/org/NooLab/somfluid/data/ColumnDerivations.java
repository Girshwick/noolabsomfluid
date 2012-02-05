package org.NooLab.somfluid.data;

import java.util.ArrayList;

import org.NooLab.somfluid.util.Formula;


public class ColumnDerivations {

	ArrayList<Formula> formulaStack = new ArrayList<Formula>();
	
	ArrayList<String> inColumns;
	ArrayList<String> outColumns;
	
	
	
	public ColumnDerivations(ColumnDerivations derivations) {
		 
	}

	
	public ArrayList<String> getInColumns() {
		return inColumns;
	}

	public ArrayList<String> getOutColumns() {
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
}

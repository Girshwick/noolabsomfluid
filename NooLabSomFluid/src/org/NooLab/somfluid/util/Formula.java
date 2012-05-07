package org.NooLab.somfluid.util;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;




public class Formula implements Serializable{

 
	private static final long serialVersionUID = 2451594012952643803L;

	
	String formulaTerm;
	
	String variables;
	
	ArrayList<String> targetColumns; 
	
	ArrayList<Point> targetCells; // each entry contains an coordinate
	
	boolean applytoColumns ;
	boolean applytoCells ;
	
	// each variable can be interpreted as a shortcut for another formula
	// the indices must match due to creational procedure !!!
	ArrayList<Formula> nestedFormulas =  new ArrayList<Formula>() ;
	
	
	public Formula(){
		 
	}


	public String getFormulaTerm() {
		return formulaTerm;
	}


	public void setFormulaTerm(String formulaTerm) {
		this.formulaTerm = formulaTerm;
	}


	public String getVariables() {
		return variables;
	}


	public void setVariables(String variables) {
		this.variables = variables;
	}


	public ArrayList<String> getTargetColumns() {
		return targetColumns;
	}


	public void setTargetColumns(ArrayList<String> targetColumns) {
		this.targetColumns = targetColumns;
	}


	public ArrayList<Point> getTargetCells() {
		return targetCells;
	}


	public void setTargetCells(ArrayList<Point> targetCells) {
		this.targetCells = targetCells;
	}


	public boolean isApplytoColumns() {
		return applytoColumns;
	}


	public void setApplytoColumns(boolean applytoColumns) {
		this.applytoColumns = applytoColumns;
	}


	public boolean isApplytoCells() {
		return applytoCells;
	}


	public void setApplytoCells(boolean applytoCells) {
		this.applytoCells = applytoCells;
	}


	public ArrayList<Formula> getNestedFormulas() {
		return nestedFormulas;
	}


	public void setNestedFormulas(ArrayList<Formula> nestedFormulas) {
		this.nestedFormulas = nestedFormulas;
	}
	
	
	
	
}

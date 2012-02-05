package org.NooLab.somfluid.util;

import java.awt.Point;
import java.util.Vector;




public class Formula {

	
	String formulaTerm;
	
	String[] variables;
	
	String[] targetColumns; 
	
	Vector<Point> targetCells; // each entry contains an coordinate
	
	boolean applytoColumns ;
	boolean applytoCells ;
	
	// each variable can be interpreted as a shortcut for another formula
	// the indices must match due to creational procedure !!!
	Vector<Formula> nestedFormulas =  new Vector<Formula>() ;
	
	
	public Formula(){
		 
	}
	
	
	
	
}

package org.NooLab.somfluid.util;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;




public class Formula implements Serializable{

	
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
	
	
	
	
}

package org.NooLab.somfluid.app;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * 
 * 
 * 
 * 
 *
 */
public class SomAppClassifier implements Serializable{

	private static final long serialVersionUID = 1300587404052091239L;
	
	
	ArrayList<SomAppSomObject> somObjects = new ArrayList<SomAppSomObject>() ; ; 
	int somObjectsCount = 0; 
	
	
	// ========================================================================
	public SomAppClassifier(){
		
		
		
	}
	// ========================================================================	
	
	public SomAppSomObject getItem(int index){
		SomAppSomObject soappObj = null;
		
		soappObj = somObjects.get(index) ;
		return soappObj;
	}
	
	
	/*
	 
	 	// how many som instances? check:  <som index="0">
		
		
		// <project>
			// <general>
		
			// <context>
		
			// <content>
		
		// <lattice>
			// description 
		
			// <nodes>
	 */
	
	
	public void classifyRecord(){
		
	}
}

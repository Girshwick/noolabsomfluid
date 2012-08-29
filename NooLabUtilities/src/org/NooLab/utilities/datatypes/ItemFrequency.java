package org.NooLab.utilities.datatypes;

import java.util.Vector;

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;



public class ItemFrequency {
	
	// object references ..............

	
	// main variables / properties ....
	
	public String label = "";
	
	public int docID ;
	public int firstpos = -1;
	
	public Vector<Integer> positions = new Vector<Integer>();
	
	public int frequency ;
	 
	public double poscofv ;
	
	// constants ......................
	
	
	// volatile variables .............
	
	
	// helper objects .................
	StringsUtil strgutil = new StringsUtil();
 	PrintLog out  ;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	public ItemFrequency(){
		
	}

	
	
	public void add( String label, int pos){
		
		
	}
	
	
	public int getPosition( int index){
		int v=-1;
		
		if ((positions != null) && (index < positions.size()) && (index>=0) && (positions.size()>0)){
			v = positions.get(index);
		}
		
		return v;
	}
	
	public Vector<Integer> getPositions(){
		return positions;
	}
	
	
	
	
}

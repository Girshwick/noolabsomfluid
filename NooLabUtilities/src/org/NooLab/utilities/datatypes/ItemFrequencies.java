package org.NooLab.utilities.datatypes;

import java.util.Vector;

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;
 

 

public class ItemFrequencies {

	 
	// =================================

	
	// object references ..............
	
	
	// main variables / properties ....
	
	Vector<ItemFrequency> items = new Vector<ItemFrequency>() ;

	private Vector<String> labels = new Vector<String>() ;
	
	private Vector<Integer> fpos = new Vector<Integer>() ;
	
	// constants ......................
	
	
	// volatile variables .............
	
	
	
	// helper objects .................
	StringsUtil strgutil = new StringsUtil();
	
	PrintLog out  ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	
	public ItemFrequencies(   ){
		 
		
	}
	
	
	public int indexOfStr( String str){
		int ix=-1;
		
		
		try{
			
			ix = labels.indexOf(str) ;
			
		}catch(Exception e){
			
		}
		
		return ix;
	}
	
	
	public int indexOfStr( String str, int doc_ID){
		int ix=-1, p=-1, z=0 ;
		boolean done=false;
		
		try{
			
			while (done==false){
			
				if (p<0){
					ix = labels.indexOf(str) ;
					p=ix+1;
				} else{
					ix = labels.indexOf(str, p) ;
					p=ix+1;
				}
				
				if (ix>=0){
					if (doc_ID>=0){
						if ( doc_ID == items.get(ix).docID ){
							done = true ;
						}
					}
				} else {
					done = true;
				}
				
				// insurance...
				z++;
				if (z>str.length()){
					break ;
				}
			} // done? 
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return ix;
	}
	 
	public int indexOfFirstPos( int _firstpos ){
		int ix=-1;
		
		
		try{
			
			ix = fpos.indexOf(_firstpos) ;
			
			
		}catch(Exception e){
			
		}
		
		return ix;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	public void add( ItemFrequency item ){
		items.add(item) ;
		
		// hidden mirror fields for fast access
		labels.add( item.label );
		fpos.add( item.firstpos );
		 
		
	}
	
	public int size(){
		
		return items.size() ;
	}
	
	
	public ItemFrequency get(int index){
		
		if ((index>=0) && (index<items.size())){
			return items.get(index) ;
		}else{
			return (new ItemFrequency() );
		}
	}	
	
	
	public void remove(int index){
		
	}
	
	
	

	// getters / setters   . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	public Vector<ItemFrequency> getItems() {
		return items;
	}
	
	
	
}

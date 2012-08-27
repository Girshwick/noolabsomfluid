package org.NooLab.somfluid.env.data;

import java.util.ArrayList;




public class NormValueRanges implements NormValueRangesIntf{

	
	ArrayList<NormValueRangeIntf> items = new ArrayList<NormValueRangeIntf>();
	
	
	
	public NormValueRanges(){
		
	}
	
	public int createItem(){
	
		NormValueRange item; 
		
		item = new NormValueRange();
		items.add(item) ;
		return items.size()-1;
	}

	
	public NormValueRangeIntf getItem(int index){
		NormValueRangeIntf item = null;
		item = items.get(index) ;
		return item;
	}
	
	
	
	
	
	
}

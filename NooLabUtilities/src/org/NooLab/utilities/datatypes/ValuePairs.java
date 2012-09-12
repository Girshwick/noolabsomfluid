package org.NooLab.utilities.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;




public class ValuePairs<T> implements Serializable{

	ArrayList<ValuePair> items = new ArrayList<ValuePair>(); 
	
	ArrayList<T> keyList = new ArrayList<T>();
	
	Map<T,T> mappedItems    = new TreeMap<T,T>();
	Map<T,Object> mappedItemData = new TreeMap<T,Object>();
	
	// ========================================================================
	public ValuePairs(){
		
	}
	
	// for cloning
	/**
	 * inVps        : ValuePairs to be copied, 
	 * clonescope   : scope of cloning:  0=all, 1=items, 2=items+keylist , 3=items+maps 
	 */
	public ValuePairs(ValuePairs inVps, int clonescope) {
		
		ValuePair vp;
		for (int i=0;i<inVps.getItems().size();i++){
			vp = inVps.getItem(i);
			items.add(vp);
		}
		
		if (clonescope==2){
			for (int i=0;i<keyList.size();i++){
				keyList.add( (T) inVps.keyList.get(i) );
			}
		}
		
		if (clonescope==0){
			// cloning the mappedItems, mappedItemData
			// ???
			
		}
		
	}
	// ========================================================================
	

	public int size(){
		return items.size();
	}
	
	public void clear() {
		// 
		items.clear() ; 
		keyList.clear(); 
		mappedItems.clear() ;    
		mappedItemData.clear(); 
	}

	public ValuePairs<T> add( ValuePair<?> item){
		
		items.add(item);
		keyList.add( (T)item.value1) ;
		
		// we maintain a map to find quickly the first instance
		if (mappedItems.containsKey(item.value1)==false){
			mappedItems.put((T)item.value1, (T)item.value2) ;
		}
		
		return this;
	}
	
	public ArrayList<ValuePair> getItems(){
		return items;
	}
	
	public ValuePair<T> getItemByKey( T v){
		
		int p;
		ValuePair vp=null;
		
		p = keyList.indexOf(v) ;
		if (p>=0){
			vp = items.get(p) ;
		}
		
		return vp;
	}
	
	
	public ValuePair<T> getItem( int index){
	
		ValuePair vp = items.get(index) ;
		
		return vp;
	}
	
}

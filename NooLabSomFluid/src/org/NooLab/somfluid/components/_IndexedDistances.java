package org.NooLab.somfluid.components;

import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexDistance;


public class _IndexedDistances {

	ArrayList<IndexDistance> items = new ArrayList<IndexDistance> ();
	
	// ========================================================================
	public _IndexedDistances(){
		
	}
	
	
	public _IndexedDistances(ArrayList<IndexDistance> iitems){
		items.addAll(iitems) ;
	}	
	// ========================================================================
	
	public int indexOfIndex(int ix) {
		int pos=-1;
		
		// we should maintain a treemap...
		for (int i=0;i<items.size();i++){
			if (items.get(i).getIndex()==ix){
				pos=i;
				break;
			}
		}
		return pos;
	}
	
	
	public void sort(){
		boolean done = false;
		double ixd2,ixd1 ;
		IndexDistance ixdist0;
		
		while (done==false){
			done=true;
			
			for (int i=0;i<items.size()-1;i++){
				
				ixd1 = items.get(i).getDistance() ;
				ixd2 = items.get(i+1).getDistance() ;
				
				if (ixd2 < ixd1){
					done=false;
					// not changing the list via remove, but just juggling the objects
					ixdist0 = items.get(i);
					items.set(i, items.get(i+1));
					items.set(i+1,ixdist0);
				}
				 
			}// i->
			
		} // done? ->
	}
	
	// ------------------------------------------------------------------------
	
	public ArrayList<IndexDistance> getItems() {
		return items;
	}
	public void setItems(ArrayList<IndexDistance> items) {
		this.items = items;
	}


	
}

package org.NooLab.somseries;

import java.util.ArrayList;



public class MarkovTableCells {

	ArrayList<MarkovTableCell> items = new ArrayList<MarkovTableCell>();

	// represents the sum of a particular Markov path
	
	double pSum = 0.0;
	
	// ========================================================================
	public MarkovTableCells(){
		
	}
	
	public MarkovTableCells( MarkovTableCells sci) {
		items.addAll(sci.items) ;
		calculate() ;
		items.trimToSize() ;
	}
	// ========================================================================


	public void calculate(){
		pSum = 0.0 ;
		
		for (int i=0;i<items.size();i++){
			pSum = pSum + items.get(i).probability;
		}
	
	}

	
	public int size(){
		return items.size() ;
	}
	
	public ArrayList<MarkovTableCell> getItems() {
		return items;
	}
	
	public int getItemIndex(int r, int c) {
		int index = -1 ;
		MarkovTableCell mc;
		
		
		for (int i=0;i<items.size();i++){
			mc = items.get(i) ;
			if ((mc.x==c) && (mc.y==r)){
				index = i;
				break;
			}
		}
		
		return index;
	}


	public void add( MarkovTableCell mc){
		items.add(mc) ;
	}
	
}

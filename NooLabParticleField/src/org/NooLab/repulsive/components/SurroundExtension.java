package org.NooLab.repulsive.components;

import java.util.ArrayList;



public class SurroundExtension {

	ArrayList<Surrex> items = new ArrayList<Surrex>();	
	
	SurroundBuffers parentSbs;
	
	public SurroundExtension( SurroundBuffers sbs){
		
		parentSbs = sbs;
	}
	
	public void add(Surrex sx){
		items.add(sx);
	}
	
	public void clear(){
		items.clear();
	}
	
	public int size(){
		return items.size();	
	}

	public Surrex getItem(int index) {
		 
		return items.get(index);
	}
	
}

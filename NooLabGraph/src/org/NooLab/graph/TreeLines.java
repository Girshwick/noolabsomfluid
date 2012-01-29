package org.NooLab.graph;

import java.io.Serializable;
import java.util.ArrayList;



public class TreeLines implements TreeLinesIntf, Serializable{
	
	  
	private static final long serialVersionUID = -2773806590453871303L;

	 
	
	int index = -1;
	String name ="" ;
	
	ArrayList<PPointXYIntf> items  = new ArrayList<PPointXYIntf>();
	
	
	
	public TreeLines(){
		 
	}

	// -----------------------------------------------------------------------
	
	public int size(){
		return items.size();
	}

	public void clear(){
		items.clear();
	}
	
	public int indexOf( Object obj){
		return items.indexOf( obj );
	}

	public void addItem( PPointXYIntf p){
		items.add(p) ;
	}
	
	public PPointXYIntf getItem( int index){
		return items.get(index);
		 
	}

	public void setItem( int index, PPointXYIntf p ){
		items.set(index, p) ;
	}

	public void removeItem( int index ){
		items.remove(index) ;
	}

	
}


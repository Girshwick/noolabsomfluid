package org.NooLab.repulsive.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.NooLab.repulsive.components.data.IndexDistance;

public class Plane {

	ArrayList<Coordinate2D> coordinates = new ArrayList<Coordinate2D>();
	// these coordinates contain a pair of double x,y

	
	// we use a int,int - map for fast access to the list of coordinates using an index
	// so we create a "virtual" unordered outside-index, which behaves as if it would be ordered
	// 1. X,Y ... -> sorted first according to Y
	Map<Integer,Integer> positionTableMap = new HashMap<Integer,Integer>();
	// this map is defined as <I,I> = <particles index (from dynamic field), coordinates position>
	// particles may be completely unordered, so we need to create a virtual table for fast navigation
	// around neighborhoods
	// 2. external index -> epsilon-column
	// wrapped into an object with additional functionality
	// Map<Integer,Vector<Integer>> position2DTableMap = new HashMap<Integer,Vector<Integer>>();
	VirtualTableMapping  mapping2D = new VirtualTableMapping(); 
	
	double surroundRadius;
	String name;
	Neighborhood neighborhood;
	
	boolean isUpdating = false;
	
	// ========================================================================
	public Plane(Neighborhood nb, String name) {
		neighborhood = nb;
		this.name = name;
		
		
	}
	// ========================================================================
	
	
	public ArrayList<IndexDistance> getSurround( int index, double surroundradius, String guidStr ){
	
		ArrayList<IndexDistance> indexDistances ;
		
		int vtmPosition = 0  ;
		 
		
		Coordinate2D anchor = null;
		Vector<Integer> candidateItems =  new Vector<Integer>();
		
		// double avgDist = neighborhood.averageDistance ;
		if (neighborhood.borderMode == Neighborhood.__BORDER_ALL){
			vtmPosition = positionTableMap.get(index);
			anchor = coordinates.get(vtmPosition);
		}
		if (neighborhood.borderMode == Neighborhood.__BORDER_NONE){
			vtmPosition = positionTableMap.get(index);
			anchor = coordinates.get(vtmPosition);
			
		}
		
		if (anchor==null){
			return null ;
		}
		candidateItems.add(vtmPosition) ;
		
		indexDistances = neighborhood.getAdjustedSurroundSelection((int)anchor.cxValue, (int)anchor.cyValue, surroundradius,guidStr);
		
		return indexDistances;
	}
	
	
	public int size(){
		
		return coordinates.size();
	}
	
	synchronized public void update( Item item, String name) {
		Coordinate2D lastItem;
		int p;
		int index;
		 
		isUpdating = true;
		try{

			index = item.index ;
			
			if (mapContainsIndex(index)==false){
				lastItem = new Coordinate2D( item.x,item.y,index,name);
				add( lastItem  ) ;
			}else{
				set(index,item) ;
				p = positionTableMap.get(index);
				lastItem = coordinates.get(p) ; 
			}
			
			sort(0,lastItem);

		}catch(Exception e){
			e.printStackTrace();
		}
		isUpdating = false;
	}
	 

	synchronized public void set(int index, Item item) {
		 int p;
		 Coordinate2D cp;
		 
		 if (positionTableMap.containsKey(index)){
			p = positionTableMap.get(index);
			cp = coordinates.get(p);

			cp.setXYvalue( new double[]{item.x,item.y});
		}
	}

	synchronized public void add( Coordinate2D c ){
		int n;
		
		n = coordinates.size(); 
		coordinates.add(c);
		positionTableMap.put(c.particleIndex , n);
		 
	}

	

	
	public boolean mapContainsIndex(int index) {
		boolean rB=false;
		int p;
		
		rB= positionTableMap.containsKey( index );
		
		return rB;
	}
	
	synchronized public void sort(int direction, Coordinate2D triggeringItem) {
		int p,index;
		Object tItem;
		Object[] items;
		Coordinate cc;
		 
		tItem = (Object)triggeringItem ;
 
		Collections.sort(coordinates);
		 

		items = coordinates.toArray(); 
		
		p = Arrays.binarySearch( items, tItem);
		
		if (p>=0){
			positionTableMap.put(triggeringItem.particleIndex, p);
			// now, from here on, we have to adjust all entries in the positionMap 
			for (int k=p+1;k<coordinates.size();k++){
				cc = coordinates.get(k) ;
				index = cc.particleIndex ;
				positionTableMap.put(cc.particleIndex, k);
			}// k->
		}
	}


	public boolean isUpdating() {
		return isUpdating;
	}


	public void setUpdating(boolean isUpdating) {
		this.isUpdating = isUpdating;
	}

}
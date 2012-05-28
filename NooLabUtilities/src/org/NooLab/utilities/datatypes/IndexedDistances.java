package org.NooLab.utilities.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



public class IndexedDistances implements Serializable{
 
	private static final long serialVersionUID = 2535904175527972027L;
	
	
	ArrayList<IndexDistance> items = new ArrayList<IndexDistance> ();
	
	// ========================================================================
	public IndexedDistances(){
		
	}
	
	
	public IndexedDistances(ArrayList<IndexDistance> iitems){
		items.addAll(iitems) ;
	}	
	// ========================================================================

	
	public IndexedDistances(IndexedDistances ixds) {
		items.addAll( ixds.getItems() ) ;
	}


	public void clear() {
		 
		items.clear();
	}

	
	public int size(){
		return items.size() ;
	}


	
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
	
	public int getIndexByStr( String checkstr){
		int pos=-1;
		
		// we should maintain a TreeMap...
		for (int i=0;i<items.size();i++){
			String str = items.get(i).guidStr ;
			if (checkstr.contentEquals(str)){
				pos=i;
				break;
			}
		}
		return pos;
	}
	
	/**
	 * 
	 * sorting the list of indexed distances according to the distances
	 * 
	 * @param direction  1=increasing order, -1=decreasing order
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void sort(int direction){
		if (direction < -0.5){
			direction=-1;
		}else{
			if (direction>= 0.5){
				direction= 1;
			}else{
				if ((direction>-0.5) && (direction<0.5))direction = 0;
			}
		}
		
		Collections.sort(items, new ixdComparator(direction));
	}
	
	public void sort(){
		
		// use: 
		
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
	
	/**
	 * criterion : 1=distance, 2=index, 3=secondary index, 4=string
	 * 
	 * not functional yet !!!!!!!!
	 * 
	 */
	public void sort(int criterion, int j) {
		// TODO Auto-generated method stub
		
	}


	public ArrayList<IndexDistance> getItems() {
		return items;
	}
	
	
	public void put(int indexValue, String string, int secIndexValue) {
		IndexDistance item;
		
		item = new IndexDistance(indexValue, secIndexValue, 0.0, string ) ;
		items.add(item) ;
	}


	public void setItems(ArrayList<IndexDistance> items) {
		this.items = items;
	}

	public void add( IndexDistance ixd ){
		items.add(ixd) ;
	}
	public void add( int index, IndexDistance ixd ){
		if (index<0)index=0;
		if (index>items.size()-1){
			items.add(ixd) ;
		}else{
			items.add(index,ixd) ;
		}
	}
	
	public IndexDistance getItem( int index){
		
		IndexDistance item=null;
		if ((index>=0) &&(index<items.size())){
			item =items.get(index);
		}
		
		return item;
	}

	public int[] getIndexItems() {

		int[] vis = new int[items.size()] ;
		
		for (int i=0;i<items.size();i++){
			vis[i] = items.get(i).index ;
		}
		return vis;
	}


	public double[] getDistanceItems() {
		
		double[] vis = new double[items.size()] ;
		
		for (int i=0;i<items.size();i++){
			vis[i] = items.get(i).distance ;
		}
		return vis;
	}


	public String[] getStringItems() {

		String[] vis = new String[items.size()] ;
		
		for (int i=0;i<items.size();i++){
			vis[i] = items.get(i).guidStr ;
		}
		return vis;
		
	}


	public void removeItem( int index){
		items.remove(index);
	}

	
	public void addAll( ArrayList<IndexDistanceIntf> ixdiList ) {
		 
		for (int i=0;i<ixdiList.size();i++){
			items.add( (IndexDistance) ixdiList.get(i)) ;
		}
	}


	public void addAll(IndexedDistances ixds) {
		 
		for (int i=0;i<ixds.size();i++){
			items.add( (IndexDistance) ixds.getItem(i)) ;
		}
	}


	public void addItems(IndexedDistances listOfPutativeTransforms) {
		// TODO Auto-generated method stub
		
	}


	public ArrayList<String> getAllFieldLabels() {
		// TODO Auto-generated method stub
		return null;
	}


	class ixdComparator implements Comparator{

		int direction=0;
		
		public ixdComparator(int dir){
			direction = dir;
		}

		
		@Override
		public int compare(Object obj1, Object obj2) {
			
			int result = 0;
			IndexDistance ixd2,ixd1;
			double v1,v2 ;
			
			ixd1 = (IndexDistance)obj1;
			ixd2 = (IndexDistance)obj2;
			
			v1 = ixd1.getDistance() ;
			v2 = ixd2.getDistance() ;
			
			if (direction>=0){
				if (v1>v2){
					result = 1;
				}else{
					if (v1<v2){
						result = -1 ;
					}
				}
			}else{
				if (v1>v2){
					result = -1;
				}else{
					if (v1<v2){
						result = 1 ;
					}
				}
				
			}
			
			return result;
		}
		
	}

 	 
	
}

package org.NooLab.utilities.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;



public class IndexedDistances implements Serializable{
 
	private static final long serialVersionUID = 2535904175527972027L;
	
	Map<String,Integer> fastIndexMap = new TreeMap<String,Integer>();
	boolean listHasChanged=true;
	
	ArrayList<IndexDistance> items = new ArrayList<IndexDistance> ();
	
	// ========================================================================
	public IndexedDistances(){
		
	}
	
	
	public IndexedDistances(ArrayList<IndexDistance> iitems){
		items.addAll(iitems) ;
	}	
	// ========================================================================

	
	public IndexedDistances(IndexedDistances ixds) {
		
		if ((ixds!=null) && (ixds.size()>0)){
			for (int i=0;i<ixds.size();i++){
				IndexDistance ixd = new IndexDistance(ixds.getItem(i))  ;
			}
		}
	}


	public void clear() {
		fastIndexMap.clear();
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
	
	/**
	 * this lookup is buffered by a TreeMap
	 * 
	 * @param checkstr
	 * @return
	 */
	public int getIndexByStr( String checkstr){
		int pos=-1;
		
		
		if ((fastIndexMap.size()==0) || (listHasChanged)){
			fastIndexMap.clear();
			for (int i=0;i<items.size();i++){
				fastIndexMap.put(items.get(i).guidStr, i) ;
			}
		}
		// 
		
		if (fastIndexMap.containsKey(checkstr)){
			pos = fastIndexMap.get(checkstr);
		}else{

			for (int i = 0; i < items.size(); i++) {
				String str = items.get(i).guidStr;
				if (checkstr.contentEquals(str)) {
					pos = i;
					break;
				}
			}
		}
		
		return pos;
	}
	
	public double getMinimum( int offset) {
		return getMinimum( -999999999.0, offset) ;
	}
	/**
	 * 
	 * @param inf minimum that is larger than the infimum
	 * @param offset positional offset
	 * @return
	 */
	public double getMinimum( double inf, int offset) {
		// 
		double minV= inf - 1.0;
		
		int ix = getIndexOfMinimum(inf,offset);
		
		if (ix>=0){
			minV = items.get(ix).getDistance();
		}else{
			
		}
		return minV;
	}

	public int getIndexOfMinimum(double inf, int offset) {
		int s=0, index= -1;
		
		if (offset<0){
			s=0;
		}else{
			s=offset;
		}
		if (s>=items.size()){
			return -1;
		}
		
		double minv = 9999999999999.9;
		for (int i=s;i<items.size();i++){
			if (minv > items.get(i).distance){
				minv = items.get(i).distance;
				index = i;
			}
		}
		
		return index;
	}
	
	public int getIndexOfMinimum(int offset) {
		// 
		return getIndexOfMinimum(-99999999999.9,offset);
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
		listHasChanged=true;
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * criterion : 1=distance, 2=index, 3=secondary index, 4=string
	 * 
	 * not functional yet !!!!!!!!
	 * 
	 */
	public void sort(int criterion, int direction) {
		// 
		Collections.sort(items, new ixdComparator(direction));
		listHasChanged=true;
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
		listHasChanged=true;
	}
	
	public ArrayList<IndexDistance> getItems() {
		return items;
	}
	
	
	public void put(int indexValue, String string, int secIndexValue) {
		IndexDistance item;
		
		item = new IndexDistance(indexValue, secIndexValue, 0.0, string ) ;
		items.add(item) ;
		listHasChanged=true;
	}


	public void setItems(ArrayList<IndexDistance> items) {
		this.items = items;
		listHasChanged=true;
	}

	public void add( IndexDistance ixd ){
		items.add(ixd) ;
		listHasChanged=true;
	}
	
	public void add( int index, IndexDistance ixd ){
		if (index<0)index=0;
		if (index>items.size()-1){
			items.add(ixd) ;
		}else{
			items.add(index,ixd) ;
		}
		listHasChanged=true;
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


	public ArrayList<String> getAllFieldLabels() {
		// TODO Auto-generated method stub
		return null;
	}


	public void removeItem( int index){
		items.remove(index);
		listHasChanged=true;
	}

	
	public void addAll( ArrayList<IndexDistanceIntf> ixdiList ) {
		 
		for (int i=0;i<ixdiList.size();i++){
			items.add( (IndexDistance) ixdiList.get(i)) ;
		}
		listHasChanged=true;
	}


	public void addAll(IndexedDistances ixds) {
		if (ixds==null)return; 
		for (int i=0;i<ixds.size();i++){
			items.add( (IndexDistance) ixds.getItem(i)) ;
		}
		listHasChanged=true;
	}


	public void addItems(IndexedDistances listOfPutativeTransforms) {
		// 
		listHasChanged=true;
	}


	class ixdComparator implements Comparator{

		int criterion;
		int direction=0;
		
		/** 1 = smallest first, -1 = largest first*/
		public ixdComparator(int dir){
			direction = dir;
			criterion = 1;
		}
		/** criterion : 1=distance, 2=index, 3=secondary index, 4=string */
		public ixdComparator(int criterion, int dir){
			direction = dir;
			this.criterion = criterion;
		}
		
		@Override
		public int compare(Object obj1, Object obj2) {
			
			int result = 0;
			IndexDistance ixd2,ixd1;
			double v1=0,v2=0 ;

			ixd1 = (IndexDistance)obj1;
			ixd2 = (IndexDistance)obj2;

			if (criterion<=1){
				v1 = ixd1.getDistance() ;
				v2 = ixd2.getDistance() ;
			}

			if (criterion==2){
				v1 = ixd1.getIndex() ;
				v2 = ixd2.getIndex() ;
			}
			if (criterion==3){
				v1 = ixd1.getIndex2() ;
				v2 = ixd2.getIndex2() ;
			}
			if (criterion==4){
				// translate string position-wise to number using ascii, bytecode, forming a number string
				
				// translating string to num value
				
				// comparing
			}
			
			
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


	public void renewEnumeration(int indextype, int base) {
		// 
		
		for (int i=0;i<items.size();i++){
			items.get(i).index= (base+i);
		}
		
	}

 	 
	
}

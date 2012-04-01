package org.NooLab.somfluid.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;

public class PowerSetConstraints {


	boolean isActive = false;
	
	private int maximumLength;
	private int minimumLength;
	
	private ArrayList<Integer> excludingItems = new ArrayList<Integer>() ;
	ArrayList<Integer> mandatoryItems = new ArrayList<Integer>() ;

	private int positionalNeighborhoods;
	private boolean allowForPositionalNeighborhoods;
	
	SortedMap<Integer,String> labelPositionMap ;
	SortedMap<String,Integer> positionLabelMap ;


	// ========================================================================
	public PowerSetConstraints(){
		
	}
	// ========================================================================
	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean check(Set<Integer> set) {
		boolean result = true ;
		
		if (isActive){
			
			if (minimumLength>0){
				result = set.size()>= minimumLength;
			}
			if ((result) && (maximumLength>=minimumLength)){
				result = set.size()<= maximumLength;
			}
			
			if ((result) && (excludingItems.size()>0)){
			 
				Collection c = CollectionUtils.intersection(set, excludingItems);
				if (c.size()>0){
					result=false;
				}
			}

			if ((result) && (mandatoryItems.size()>0)){
				 
				Collection c = CollectionUtils.intersection(set, mandatoryItems);
				if (c.size()< mandatoryItems.size()){
					result=false;
				}
			}

			
		} // active ?
		return result;
	}
	public boolean check(Set<Integer> strset, ArrayList<Integer> abandonedPositions) {
		Set<Integer> dset;
		boolean checkOK=false;
		dset = new TreeSet<Integer>();
		
		int i=-1;
		for (Integer ival: strset){
			i++;
			if (abandonedPositions.indexOf(i)<0){
				dset.add(ival) ;
			}
		}
		checkOK = check(dset);
			
		return checkOK;
	}
	
	
	public void setMaps( SortedMap<Integer, String> labelposmap,
						 SortedMap<String, Integer> poslabelmap) {
		labelPositionMap = labelposmap ;
		positionLabelMap = poslabelmap ;
	}
	
	
	
	public void setMaximumLength(int value) {
		maximumLength = value;
	}

	/**
	 * @return the maximumLength
	 */
	public int getMaximumLength() {
		return maximumLength;
	}

	/**
	 * @return the minimumLength
	 */
	public int getMinimumLength() {
		return minimumLength;
	}

	/**
	 * @param minimumLength the minimumLength to set
	 */
	public void setMinimumLength(int value) {
		this.minimumLength = value;
	}

	/**
	 * 
	 * @param size of the neigborhood where a LOCAL permutation of positions is allowed
	 */
	public void setAllowForPositionalNeighborhoods(int nbsize) {
		allowForPositionalNeighborhoods = nbsize>0;
		positionalNeighborhoods = nbsize;
	}

	public void removeItemFromMandatory(String strItem){
		
	}
	public void removeItemFromExcluding(String strItem){
		
	}
	
	public void addMandatoryItem( int item) {
		mandatoryItems.add(item);
	}
	
	public void addMandatoryItems(int[] ivals) {
		
		for (int i=0;i<ivals.length;i++){
			addMandatoryItem( ivals[i]) ;
		}
	}
	
	public void addMandatoryItem( String item) {
		item = item.trim();
		int ival;
		
		// labelPositionMap, 
		if (item.length()>0){
			ival = positionLabelMap.get(item) ;
			mandatoryItems.add(ival);
		}
	}
	public void addMandatoryItems(ArrayList<String> items) {
		
		if ((items!=null) && (items.size()>0)){
			for (String str:items){
				addMandatoryItem(str);	
			}
		}
	}
	public void addMandatoryItems(String[] strings) {
		ArrayList<String> items = new ArrayList<String>(Arrays.asList(strings));
		if (items.size()>0){
			addMandatoryItems(items);
		}
	}
	public void setMandatoryItems(String[] strings) {
		
		ArrayList<String> mItems = new ArrayList<String>(Arrays.asList(strings));
		addMandatoryItems(mItems) ;
	}
	
	
	/**
	 * @param mandatoryItems the mandatoryItems to set
	 */
	public void setMandatoryItems(ArrayList<String> items) {
		
		mandatoryItems.clear();
		if (items.size()>0){
			for (String str:items){
				addMandatoryItem(str);	
			}
		}
	}
	/**
	 * @return the mandatoryItems
	 */
	public ArrayList<String> getMandatoryItemsAsStrings() {
		ArrayList<String> mItems = new ArrayList<String>();
		
		for (int i=0;i<mandatoryItems.size();i++){
			int ival = mandatoryItems.get(i);
			String str = labelPositionMap.get(ival);
			mItems.add(str) ;
		}
		
		return mItems;
	}
	public ArrayList<Integer> getMandatoryItems() {
		return mandatoryItems;
	}

	public void setExcludingItems(String[] strings) {
		excludingItems.clear();
		ArrayList<String> eItems = new ArrayList<String>(Arrays.asList(strings));
		setExcludingItems(eItems);
	}

	/**
	 * @param excludingItems the excludingItems to set
	 */
	public void setExcludingItems(ArrayList<String> items) {
		excludingItems.clear();
		
		if (items.size()>0){
			addExcludingItems(items);
		}

		
	}
	
	public void addExcludingItem( String item) {
		item = item.trim();
	 
		int ival;
		
		// labelPositionMap, 
		if (item.length()>0){
			ival = positionLabelMap.get(item) ;
			excludingItems.add(ival);
		}
	}

	public void addExcludingItem( int item) {
		excludingItems.add(item);
	}
	
	public void addExcludingItems(int[] ivals) {
		
		for (int i=0;i<ivals.length;i++){
			addExcludingItem( ivals[i]) ;
		}
	}


	public void addExcludingItems(String[] strings) {
		
		ArrayList<String> items = new ArrayList<String>(Arrays.asList(strings));
		addExcludingItems(items);
	}
	
	public void addExcludingItems(ArrayList<String> items) {
		
		if ((items!=null) && (items.size()>0)){
			
			if ((items!=null) && (items.size()>0)){
				for (String str:items){
					addExcludingItem(str);	
				}
			}
			
		}
	}
	/**
	 * @return the excludingItems
	 */
	public ArrayList<Integer> getExcludingItems() {
		return excludingItems;  
	}
	
	public ArrayList<String> getExcludingItemsAsStrings() {
		ArrayList<String> mItems = new ArrayList<String>();
		
		for (int i=0;i<excludingItems.size();i++){
			int ival = excludingItems.get(i);
			String str = labelPositionMap.get(ival);
			mItems.add(str) ;
		}
		
		return mItems;
	}

	/**
	 * @return the positionalNeighborhoods
	 */
	public int getPositionalNeighborhoods() {
		return positionalNeighborhoods;
	}

	/**
	 * @param positionalNeighborhoods the positionalNeighborhoods to set
	 */
	public void setPositionalNeighborhoods(int positionalNeighborhoods) {
		this.positionalNeighborhoods = positionalNeighborhoods;
	}

	/**
	 * @param allowForPositionalNeighborhoods the allowForPositionalNeighborhoods to set
	 */
	public void setAllowForPositionalNeighborhoods( boolean allowPosNeighborhoods) {
		this.allowForPositionalNeighborhoods = allowPosNeighborhoods;
	}
	/**
	 * @return the allowForPositionalNeighborhoods
	 */
	public boolean isAllowForPositionalNeighborhoods() {
		return allowForPositionalNeighborhoods;
	}

}

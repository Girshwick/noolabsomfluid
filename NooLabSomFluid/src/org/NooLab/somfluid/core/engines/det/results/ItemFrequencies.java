package org.NooLab.somfluid.core.engines.det.results;

import java.util.ArrayList;




public class ItemFrequencies {

	
	ArrayList<ItemFrequency> items = new ArrayList<ItemFrequency>();
	
	ArrayList<String> itemLabels = new ArrayList<String>(); 
	
	double referenceValue = -1.0 ; // could be used for value assignments 1:n
	
	// ------------------------------------------------------------------------
	public ItemFrequencies(){
		
	}
	public ItemFrequencies( ItemFrequencies _frequencies ) {
		// 
		
		items.addAll( _frequencies.items );
		itemLabels.addAll( _frequencies.itemLabels );
	}
	// ------------------------------------------------------------------------

	
	public double containsValue(double value, double resolution, double mvIndicator) {
		boolean rB=false;
		double dv;
		double itemValue = mvIndicator ; 
		
		for (int i=0;i<items.size();i++){
			dv = Math.abs( items.get(i).observedValue - value);
			if (dv<=resolution){
				itemValue = items.get(i).observedValue ;
				break ;
			}
		}
		
		return itemValue;
	}
	
	
	public void updateValue(double value) {
	
		updateValue(value,"");
	}
	
	
	
	public void updateValue(double value, String groupLabel) {
		 
		boolean rB=false;
		double dv;
		 
		
		for (int i=0;i<items.size();i++){
			dv = Math.abs( items.get(i).observedValue - value);
			
			if (dv<0.00000001){
				items.get(i).increment(1);
				items.get(i).itemLabel = groupLabel ;
				break ;
			}
		}  
	}
	public void introduceValue(double value) {
		 
		introduceValue(value,"");
	}
	
	public void introduceValue(double value, String groupLabel) {
		//  
		ItemFrequency item = new ItemFrequency();
		double rv = 1000000000.0;
		
		item.observedValue = Math.round( value * rv)/rv ;
		item.frequency = 1 ;
		item.itemLabel = groupLabel ;
		items.add(item) ;
		
	}
	
	
	public int countValues( double tvalue) {
		int c=0;
		for (int i=0;i<items.size();i++){
			if ( (Math.abs( tvalue-items.get(i).observedValue )<0.00000000001)) {
				c++;
			}
		}
		return c;
	}

	public int countNegatives() {
		int c=0;
		for (int i=0;i<items.size();i++){
			if (items.get(i).observedValue<0){
				c++;
			}
		}
		return c;
	}
	
	public double countPositives() {
		int c=0;
		for (int i=0;i<items.size();i++){
			if (items.get(i).observedValue>0){
				c++;
			}
		}
		return c;
	}
	public ItemFrequencies selectNonZeroes() {
		ItemFrequencies selitems = new ItemFrequencies ();
		
		int c=0;
		for (int i=0;i<items.size();i++){
			if (items.get(i).observedValue>=0.0){
				c++;
				selitems.add(items.get(i)) ;
			}
		}
		
		return selitems;
	}
	
	
	public void add( ItemFrequency item){
		
		items.add(item) ;
	}
	
	public int indexOfLabel( String str){
		return -1;
	}
	
	public int indexOfLabel(ItemFrequency item){
		return -1;
	}
	
	public int size(){
		return items.size();
	}
	public double getReferenceValue() {
		return referenceValue;
	}
	public void setReferenceValue(double referenceValue) {
		this.referenceValue = referenceValue;
	}
	public ArrayList<ItemFrequency> getItems() {
		return items;
	}
	
}

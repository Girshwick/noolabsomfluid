package org.NooLab.somfluid.core.engines.det.results;

import java.util.ArrayList;




public class ItemFrequencies {

	
	ArrayList<ItemFrequency> items = new ArrayList<ItemFrequency>();
	
	ArrayList<String> itemLabels = new ArrayList<String>(); 
	
	
	
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
		boolean rB=false;
		double dv;
		 
		
		for (int i=0;i<items.size();i++){
			dv = Math.abs( items.get(i).observedValue - value);
			
			if (dv<0.00000001){
				items.get(i).increment(1);
				break ;
			}
		}  
		
	}
	
	
	
	public void introduceValue(double value) {
		//  
		ItemFrequency item = new ItemFrequency();
		double rv = 1000000000.0;
		
		item.observedValue = Math.round( value * rv)/rv ;
		item.frequency = 1 ;
		
		items.add(item) ;
		
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
	
	
	
}

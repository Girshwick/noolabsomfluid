package org.NooLab.somfluid.core.engines.det.results;

public class ItemFrequency {

	
	
	public String itemLabel = "";
	
	public double observedValue = 0.0;
	public int frequency = 0 ;

	 
	
	// ------------------------------------------------------------------------
	public ItemFrequency(){
		
	}
	
	public ItemFrequency(ItemFrequency importItem) {
		
		itemLabel = importItem.itemLabel ;
		observedValue = importItem.observedValue ;
		frequency = importItem.frequency;
	}

	
	// ------------------------------------------------------------------------



	public String getItemLabel() {
		return itemLabel;
	}

	public void setItemLabel(String itemLabel) {
		this.itemLabel = itemLabel;
	}

	public double getObservedValue() {
		return observedValue;
	}

	public void setObservedValue(double observedValue) {
		this.observedValue = observedValue;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}



	public void increment(int i) {
		frequency++;
	}


	
	
	
	
}

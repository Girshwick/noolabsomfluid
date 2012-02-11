package org.NooLab.somfluid.core.engines.det.results;

import java.util.ArrayList;



public class FrequencyList {


	String listLabel = "" ; // for user-based purposes
	int    listIndex = -1 ; // for purposes of reference by the user of this class
	long   serialID  = -1 ; // alternatively...
	
	ItemFrequencies itemFrequencies ;
	
	
	public double resolution = 0.00001; // allows for around 5000 classes....
	
	ItemFrequency majority ;
	
	
	FrequencyListGeneratorIntf freqlistUser;
	double ppv;
	
	int majorityIsActive = -3;
	
	
	// ========================================================================
	public FrequencyList( FrequencyListGeneratorIntf freqlistuser){
		
		itemFrequencies = new ItemFrequencies(); // ArrayList<ItemFrequency>() ;
		
		// for callbacks...
		freqlistUser = freqlistuser ;
		
	}
	// ========================================================================
	
	
	// knowing nothing about the possible values
	public void digestValues(ArrayList<Double> values) {

		double v, fValue ;
		ItemFrequencies _frequencies = new ItemFrequencies() ;
		ItemFrequency itemFreq ;
		
		v=0;
		
		if (values.size()==0){
			majority = new ItemFrequency( );
			ppv = -1.0;
			return;
		}
		try{
			

			for (int i=0;i<values.size();i++){
				
				v = values.get(i) ;
				
				fValue = _frequencies.containsValue(v, resolution, -9.99) ;
				if ( fValue != -9.99 ){
					_frequencies.updateValue( fValue );
				}else{
					_frequencies.introduceValue( v );
				}
				
			} // i -> all values
			itemFrequencies = new ItemFrequencies(_frequencies);
			
			sortFrequenciesItems();
			
			
			majority = new ItemFrequency( itemFrequencies.items.get(0) );
			
			// this we need for the ROC
			ppv = (double)majority.frequency / (double)values.size();
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}
	
	
	
	public void digestValues( ArrayList<Double> tvValues, ArrayList<ValueCode> expectedValueCodes ) {
		// 
		
	}


	private void sortFrequenciesItems() {
		 
		boolean done=false;
		ItemFrequency i1,i2,si;
		
		while (done==false){
			done = true;
			
			for (int i=0;i<itemFrequencies.items.size()-1;i++){
				i1 = itemFrequencies.items.get(i);
				i2 = itemFrequencies.items.get(i+1);
				
				if (i1.frequency<i2.frequency){
					si=i1;
					itemFrequencies.items.remove(i) ;
					itemFrequencies.items.add(i+1,si) ;
					done=false;
				}
			}
		}
		done=true;
	}
	// ------------------------------------------------------------------------

	
	public double getResolution() {
		return resolution;
	}
	

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}


	public String getListLabel() {
		return listLabel;
	}


	public void setListLabel(String listLabel) {
		this.listLabel = listLabel;
	}


	public int getListIndex() {
		return listIndex;
	}


	public void setListIndex(int listIndex) {
		this.listIndex = listIndex;
	}


	public ItemFrequencies getItemFrequencies() {
		return itemFrequencies;
	}


	public void setItemFrequencies(ItemFrequencies itemFreqs) {
		this.itemFrequencies = itemFreqs;
	}


	public long getSerialID() {
		return serialID;
	}


	public void setSerialID(long serialID) {
		this.serialID = serialID;
	}


	public double getPpv() {
		return ppv;
	}


	public void setPpv(double ppv) {
		this.ppv = ppv;
	}


	public int getMajorityIsActive() {
		return majorityIsActive;
	}


	public void setMajorityIsActive(int majorityIsActive) {
		this.majorityIsActive = majorityIsActive;
	}


	public ItemFrequency getMajority() {
		return majority;
	}
	
	
	
	
}

package org.NooLab.somfluid.core.engines.det.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.NooLab.utilities.ArrUtilities;



public class FrequencyList {


	String listLabel = "" ; // for user-based purposes
	int    listIndex = -1 ; // for purposes of reference by the user of this class
	long   serialID  = -1 ; // alternatively...
	
	ItemFrequencies itemFrequencies ;
	
	
	public double resolution = 0.00001; // allows for around 5000 classes....
	
	ItemFrequency majority ;
	
	
	FrequencyListGeneratorIntf freqlistUser;
	
	double ppv, npv;
	// for multi-target case, we have to collect the npv for any of the other groups, that are not identical to the majority class
	Map<Integer,Double> npvs = new HashMap<Integer,Double>() ; 
	
	int majorityIsActive = -3;
	
	transient public ArrUtilities arrutil = new ArrUtilities();
	
	// ========================================================================
	public FrequencyList( FrequencyListGeneratorIntf freqlistuser){
		
		itemFrequencies = new ItemFrequencies(); // ArrayList<ItemFrequency>() ;
		
		// for callbacks...
		freqlistUser = freqlistuser ;
		
	}
	// ========================================================================
	
	
	/** 
	 * knowing nothing about the possible values
	 * 
	 * assumption: the values are on an ordinal niveau, or can be smoothly rendered into an ordinal scale 
	 * 
	 */
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
				
				if (v==-1.0){
					continue;
				}
				
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

	private int valueBelongsToTargetGroup(){
	
		int identifiedTargetGroup = -1;
		
		
		return identifiedTargetGroup;
	}
	
	public void digestValuesForTargets( ArrayList<Double> values, double ecr,
										double[][] targetGroups, String[] tgLabels) {
		// 
		
		int p,targetFreq=0 ,nontargetFreq=0;
		String groupLabel;
		double v, fValue,ttv , nonTgObservedValue, tgObservedValue;
		
		ItemFrequencies _frequencies = new ItemFrequencies() ;
		ItemFrequencies _frequencies_of_TG = new ItemFrequencies() ;
		ItemFrequencies _frequencies_of_nonTG = new ItemFrequencies() ;
		ItemFrequency itemFreq ;
		
		v=0;
		
		if (values.size()==0){
			majority = new ItemFrequency( );
			ppv = -1.0;
			return;
		}
		
		
		try{
			
			v=0;
			for (int i=0;i<values.size();i++){
				
				v = values.get(i) ;
				
				if (v==-1.0){
					continue;
				}
				
				p = arrutil.intervalIndexOf( v, targetGroups,0);
				groupLabel = "" ;
				
				if (p>=0){
					ttv = ( targetGroups[p][0] + targetGroups[p][1])/2.0 ;
					ttv = Math.round(ttv*1000.0)/1000.0 ;
					if (p<tgLabels.length){
						groupLabel = tgLabels[p] ;
					}
					targetFreq++;
					registerValue( _frequencies_of_TG, v, groupLabel);
				} // in some of the target group intervals ?
				else{
					ttv = -3.0 ; 
					groupLabel = "non-TV"; 
					nontargetFreq++;
					registerValue( _frequencies_of_nonTG, v, groupLabel);
				}
				
				registerValue( _frequencies, ttv, groupLabel);
				
			} // i -> all values
			
			
			itemFrequencies = new ItemFrequencies(_frequencies);

			sortFrequenciesItems();
			
			majority = new ItemFrequency( itemFrequencies.items.get(0) );
			
			nonTgObservedValue = getAverageValueFromFreqList( _frequencies_of_nonTG );
			tgObservedValue = getAverageValueFromFreqList( _frequencies_of_TG );
			
			
			// this we need for the ROC
			if (ecr<0){
				ppv = (double)majority.frequency / (double)values.size();
				npv = (double)(values.size() - majority.frequency )/ (double)values.size();
			}else{
				ppv = (double)targetFreq/ (double)values.size();
				npv = (double)nontargetFreq/ (double)values.size();
				//
				if (ecr > (1-ppv)){ // 1-ppv expresses the risk 
					majority.observedValue = tgObservedValue ;
				}else{
					majority.observedValue = nonTgObservedValue ;
				}
			}
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}
	
	private double getAverageValueFromFreqList( ItemFrequencies _freqs  ){
		double avgResult = -1.0, vsum=0.0;
		ItemFrequency item;
		int n=0;
		
		for (int i=0;i<_freqs.size();i++){
			item = _freqs.items.get(i) ;
			vsum = vsum + (item.observedValue * (double)item.frequency) ;
			n = n + item.frequency;
		}
		if (n>0){
			avgResult = vsum/n;
		}
		
		return avgResult;
	}
	
	private void registerValue( ItemFrequencies _frequencies, double v, String groupLabel){
		double fValue;
		
		fValue = _frequencies.containsValue(v, resolution, -9.99) ;
		if ( fValue != -9.99 ){
			_frequencies.updateValue( fValue, groupLabel );
		}else{
			_frequencies.introduceValue( v, groupLabel );
		}

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

	
	public ItemFrequencies getItemFrequencies() {
		return itemFrequencies;
	}


	public void setItemFrequencies(ItemFrequencies itemFreqs) {
		this.itemFrequencies = itemFreqs;
	}

	public int size(){
		return itemFrequencies.items.size() ;
	}
	
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

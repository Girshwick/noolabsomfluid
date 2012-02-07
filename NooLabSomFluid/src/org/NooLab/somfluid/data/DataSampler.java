package org.NooLab.somfluid.data;

import java.util.ArrayList;
import java.util.Random;

import org.NooLab.somfluid.components.SomDataObject;
 
 


/**
 * 
 * 
 * this object collects and translates settings concerning any kind of sampling as 
 * defined by modeling settings.
 * 
 *  it contains lists of record IDs which are allowed, prohibited, etc.
 * 
 * 
 */
public class DataSampler {

	ModelingSettings modelingSettings ;
	
	
	// for identifying this one in a set of data samplers
	String name = "" ;
	
	
	int[] currentPosition = new int[3] ;
	
	ArrayList<Integer> trainingSample   = new ArrayList<Integer>(); 
	ArrayList<Integer> validationSample = new ArrayList<Integer>(); 
	ArrayList<Integer> outofmodelSample = new ArrayList<Integer>(); 
	
	int effectiveRecordCount;
	
	Random _random;

	// ========================================================================
	public DataSampler(){
		
		for(int i=0;i<3;i++){
			currentPosition[i] = -1;
		}
		
		String str = this.toString() ;
		
		_random = new Random();	
	}
	
	public void setModelingSettings( ModelingSettings  modelingsettings){
		
		modelingSettings = modelingsettings;
		
		_random = modelingSettings.getRandom() ;
		_random.nextDouble() ;
	}
	// ========================================================================	
	
	public int getSizeTrainingSet(){
		return trainingSample.size();
	}
	

	public int getSizeValidationSet(){
		return validationSample.size();
	}

	public int getSizeOutofModelSet(){
		return outofmodelSample.size();
	}
	
	
	public ArrayList<Integer> getTrainingSet(){
		return trainingSample ;
	}
	

	public ArrayList<Integer> getValidationSet(){
		return validationSample ;
	}

	public ArrayList<Integer> getOutofModelSet(){
		return outofmodelSample ;
	}
	
	
	public int getNextRecordID( int type ){
		int recID = -1;
		
		if ((type<0) || (type>=3)){
			return -1;
		}
		if (type==0){
			currentPosition[type]++;
			recID = trainingSample.get(currentPosition[type]);
		}
		
		if (type==1){
			currentPosition[type]++;
			recID = validationSample.get(currentPosition[type]);
		}
		
		if (type==2){
			currentPosition[type]++;
			recID = outofmodelSample.get(currentPosition[type]);
		}
		
		return recID;
	}
	
	public int getNextRecordIDoff( int startindex ){
		
		return -1;
	}

	
	public int addRecordID( int type, int recordID ){
		
		int recID = -1;
		
		if ((type<0) || (type>=3)){
			return -1;
		}

		if (type==0){
			currentPosition[type]++;
			trainingSample.add(recordID);
		}
		
		if (type==1){
			currentPosition[type]++;
			validationSample.add(recordID);
		}
		
		if (type==2){
			currentPosition[type]++;
			outofmodelSample.add(recordID);
		}
		
		return recID;
	}
	
	
	public ArrayList<Integer> createEffectiveRecordList( int absrecordcount, int epoch, int steps, int currenteffectivecount ){
		int i, absolute_record_count;
		ArrayList<Integer> row_IDs = new ArrayList<Integer>();
		
		double _scaleFactor = 1.0;
		
		_scaleFactor = Math.log10(currenteffectivecount) * Math.log(currenteffectivecount);
		
		
		
		if (epoch<=1) { if (steps>=3){
			// we drastically reduce the count of records for the first pass
			effectiveRecordCount = (int) Math.round(currenteffectivecount /( _scaleFactor));
			
			if (currenteffectivecount<100){
				if (absrecordcount>100){
					effectiveRecordCount=100 ;
				}
				else{
					effectiveRecordCount = absrecordcount;
				}
			}
			}
		}
		
		double selectionProb = (double)(1.0*effectiveRecordCount) / (double)(1.0*absrecordcount) ;
		boolean _select;
		
		i = 0; 
		while (row_IDs.size() < effectiveRecordCount) {
			// do it randomly, and respect blocks in case of relational/time series data
			_select = false;
			
			
			double rv = _random.nextDouble() ;
			 
			_select = rv <= selectionProb ; 
			
			if (_select) {
				row_IDs.add(i);
				// reflects resized data body;
				// this now means, that all data are referenced;
				// from this we will draw our records by random
			}
			
			i++;
		} // i ->
		
		row_IDs.trimToSize();
		
		return row_IDs;
	}
	
	
	public void createFilteredRecordList( SomDataObject somdata, ModelingSettings modelingsettings){
		Random rnd;
		
		DataTable table;
		DataTableCol indexColumn ;
		
		int i,n, recCount, vi ;
		boolean selected ;
		
		double fraction = 1.0, rd;
		int lowerlimitID, upperlimitID ; 
		
		modelingSettings = modelingsettings;
		
		modelingSettings.setDataSampler(this) ;
		
		try{
			
			rnd = modelingSettings.getRandom() ;
			
			table = somdata.getDataTable() ;
			
			indexColumn = table.getColumn(0) ;
			
			recCount = indexColumn.getSize() ;
			
			//taking the default for block sampling
			lowerlimitID = 0;
			upperlimitID = recCount ;
			
			// modelingSettings.
			
			for (i=0;i<recCount;i++){
				
				// draw a random number
					
				rd = rnd.nextDouble() ;
					
				//
				
				selected = rd < fraction ; 
				
				vi = (int)(Math.round(indexColumn.getValue(i))) ;
				
				if (selected==true){
					trainingSample.add( vi ) ; 
				}else{
					validationSample.add( vi ) ;
				}
				
			} // i ->
			
			
			i=0;
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
	}

	
	
	
	public int getEffectiveRecordCount() {
		return effectiveRecordCount;
	}
	
}

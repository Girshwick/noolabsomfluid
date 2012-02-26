package org.NooLab.somfluid.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.apache.commons.collections.*;


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

	public static final int _SAMPLE_TMP        = 0;
	public static final int _SAMPLE_RAW        = 1;
	public static final int _SAMPLE_TRAINING   = 5;
	public static final int _SAMPLE_VALIDATION = 6;
	public static final int _SAMPLE_OOM_VALID  = 7;
	public static final int _SAMPLE_BAGBASE    = 10;

	
	ModelingSettings modelingSettings ;
	
	
	// for identifying this one in a set of data samplers
	String name = "" ;
	
	ArrayList<Integer> masterSamples = new ArrayList<Integer>() ;
	
	int[] currentPosition = new int[3] ;
	
	ArrayList<Integer> trainingSample   = new ArrayList<Integer>(); 
	ArrayList<Integer> validationSample = new ArrayList<Integer>(); 
	ArrayList<Integer> outofmodelSample = new ArrayList<Integer>(); 
	
	ArrayList<ArrayList<Integer>> somBagSamples = new ArrayList<ArrayList<Integer>>() ;
	
	Map<Integer,Object> samplesMapping = new HashMap<Integer,Object>();
	
	
	int effectiveRecordCount;
	
	/** an absolute limit, out of context, for various purposes */
	int globalLimit = -1;
	
	
	transient Random _random;
	

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
	

	public void createRecordIndexMasterList( int absrecordcount ) {
		//  
		trainingSample.clear() ;
		for (int i=0;i<absrecordcount;i++){
			trainingSample.add(i);
		}
		samplesMapping.put( _SAMPLE_TRAINING, trainingSample); // DO NOT FORGET THAT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	}

	public void createBasicModelingSamples(int absrecordcount, double samplePortion) {
		// 
		ArrayList<Integer> allIndexes = new ArrayList<Integer>();
		
		for (int i=0;i<absrecordcount;i++){
			allIndexes.add(i);
		}
		
		validationSample = this.createEffectiveRecordList( 6, absrecordcount, samplePortion) ;
		trainingSample = getDifferenceSet( allIndexes, validationSample);
		
		samplesMapping.put( _SAMPLE_TRAINING, trainingSample);
		samplesMapping.put( _SAMPLE_VALIDATION, validationSample);
		 
	}
	
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getDifferenceSet( ArrayList<Integer> allIndexes , ArrayList<Integer> removalIndexes ){
		
		ArrayList<Integer> differenceSet = new ArrayList<Integer>();
		
		// also: union, intersection
		
		differenceSet = (ArrayList<Integer>) CollectionUtils.subtract( allIndexes, removalIndexes) ;
		
		return differenceSet ;
	}

	/**
	 * the most simple version of the sampler
	 * 
	 * @param masterSampleID 
	 * @param absrecordcount 
	 * @param samplePortion  a value between [0 .. 1.0], if >1, then it will be interpreted as percentage and normalized by /100
	 * @return
	 */
	public ArrayList<Integer> createEffectiveRecordList( int masterSampleID, int absrecordcount, double samplePortion ){
		
		ArrayList<Integer> excludedItemsIndexes = new ArrayList<Integer>();
		
		return createEffectiveRecordList( masterSampleID, absrecordcount, samplePortion, excludedItemsIndexes);
	}

	
	/**
	 * takes a forbidden sample into account
	 * 
	 * @param masterSampleID
	 * @param absrecordcount
	 * @param samplePortion
	 * @param excludedItemsIndexes
	 * @return
	 */
	public ArrayList<Integer> createEffectiveRecordList( int masterSampleID, int absrecordcount, double samplePortion, ArrayList<Integer> excludedItemsIndexes ){
		
		int i ,rr;
		int absoluteRecordCount = absrecordcount ,effectiveRecordCount;
		boolean _select;
		double selectionProb ;
		
		ArrayList<Integer> row_IDs = new ArrayList<Integer>();
		 
		
		if (masterSamples.size()>1){
			// adjust absoluteRecordCount
		}
		   
		if (samplePortion>1.0){
			samplePortion = samplePortion/100.0;	
		}
		
		selectionProb = samplePortion ;
		effectiveRecordCount = (int) (absrecordcount * samplePortion);
		
		// if selectionProb > 0.6 -> invert the procedure --- remove particles instead of drawing them
		i = 0; 
		if (selectionProb<0.72){
		
			while (row_IDs.size() < effectiveRecordCount) {
				// do it randomly, and respect blocks in case of relational/time series data
				_select = false;
				
				
				double rv = _random.nextDouble() ;
				 
				_select = rv <= selectionProb ; 
				
				if ((_select) || (selectionProb>0.99)) {
					if (row_IDs.indexOf(i)<0){
						row_IDs.add(i);
					}
				}
				
				i++;
				if (i>absrecordcount){
					i=0;
				}
				if ((row_IDs.size() > (double)effectiveRecordCount*1.02)){
					break;
				}
			} // i ->, row_IDs.size() oK ?
		}else{
			for (int k=0;k<effectiveRecordCount;k++){
					row_IDs.add( k) ;
			}
			if (selectionProb<0.999){
				double deselectionProb = 1.0-selectionProb;
				
				while (row_IDs.size() > effectiveRecordCount) {
					// do it randomly, and respect blocks in case of relational/time series data
					_select = false;
					rr=0;
					for (int k=0;k<row_IDs.size();k++){
						
	
						double rv = _random.nextDouble() ;
						 
						_select = rv <= deselectionProb ; 
						
						if ((_select) || (selectionProb>0.99)) {
							if (row_IDs.indexOf(i)<0){
								row_IDs.set(i,-1); 
								rr++;
							}
							// reflects resized data body;
							// this now means, that all data are referenced;
							// from this we will draw our records by random
						}
						if (row_IDs.size()-rr<=effectiveRecordCount){
							break;
						}
					} // k->
					
					int r=row_IDs.size()-1;
					while (r>=0){
						if (row_IDs.get(r)<0){
							row_IDs.remove(r) ;
						}
					}
					
					i++;
					if (i>absrecordcount){
						i=0;
					}
					if ((row_IDs.size() > (double)effectiveRecordCount*1.02)){
						break;
					}
				} // i ->, row_IDs.size() oK ?
				
			}
		}
		
		Collections.sort( row_IDs) ;
		row_IDs.trimToSize();
		
		return row_IDs;
	}

	/**
	 * 
	 * 
	 * 
	 * @param masterSampleID  points to the underlying samle form which we are goingto draw the actual sample...
	 * @param absrecordcount
	 * @param epoch
	 * @param steps
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> createEffectiveRecordList( int masterSampleID, int targetcount, int epoch, int steps ){
		int i , rr;
		int absoluteRecordCount, effectiveRecordCount  ;
		int currenteffectivecount;
		
		ArrayList<Integer> row_IDs = new ArrayList<Integer>();
		
		double _scaleFactor = 1.0;
		
		ArrayList<Integer> baseSet = (ArrayList<Integer>) samplesMapping.get( masterSampleID ) ;
		 
		if ((baseSet==null) || (baseSet.size()==0)){
			return row_IDs;
		}
		
		absoluteRecordCount = baseSet.size() ;
		
		if (absoluteRecordCount> targetcount){
			absoluteRecordCount = targetcount ;
		}
		currenteffectivecount = absoluteRecordCount ;
		effectiveRecordCount = currenteffectivecount ;
			
		if (epoch>=0){ // creates approx the series 7%  16% 32% 100%
			
			// 1+(3- (3*SQRT(epoch/3))) * log10(N)
			_scaleFactor = (1.0+((steps-1.0)- ((steps-1.0)*Math.sqrt(((double)epoch/(steps-1.0))))) * Math.log10((double)currenteffectivecount)) ;
			
			effectiveRecordCount = (int) Math.round(currenteffectivecount /( _scaleFactor));
			
			effectiveRecordCount = Math.min(effectiveRecordCount , absoluteRecordCount) ;
			if (epoch==0){
				effectiveRecordCount = (int) (effectiveRecordCount/3.0) ; 
			}
			if (epoch>=2){
				double p;
				p = (double)((double)effectiveRecordCount/(double)absoluteRecordCount) ;
				if ((p>0.3) && (p<0.65)){
					p = p + ((1.0-p)/2.0);
					effectiveRecordCount = (int)Math.round( p*(double)absoluteRecordCount);
				}
			}
		}
		if (effectiveRecordCount < 100) {
			if (absoluteRecordCount > 100) {
				effectiveRecordCount = 100;
			} else {
				effectiveRecordCount = absoluteRecordCount;
			}
		}
		
		
		double selectionProb = (double)(1.0*effectiveRecordCount) / (double)(1.0*absoluteRecordCount) ;
		boolean _select;
		
		if (selectionProb>0.975){
			selectionProb = 1.0;
		}
		
		// if selectionProb > 0.6 -> invert the procedure --- remove particles instead of drawing them
		i = 0; 
		if (selectionProb<0.72){
		
			while (row_IDs.size() < effectiveRecordCount) {
				// do it randomly, and respect blocks in case of relational/time series data
				_select = false;
				
				
				double rv = _random.nextDouble() ;
				 
				_select = rv <= selectionProb ; 
				
				if ((_select) || (selectionProb>0.99)) {
					if (row_IDs.indexOf(i)<0){
						row_IDs.add(i);
					}
					// reflects resized data body;
					// this now means, that all data are referenced;
					// from this we will draw our records by random
				}
				
				i++;
				if (i>absoluteRecordCount){
					i=0;
				}
				if ((row_IDs.size() > (double)effectiveRecordCount*1.02)){
					break;
				}
			} // i ->, row_IDs.size() oK ?
		}else{
			for (int k=0;k<effectiveRecordCount;k++){
					row_IDs.add( k) ;
			}
			if (selectionProb<0.999){
				double deselectionProb = 1.0-selectionProb;
				
				while (row_IDs.size() > effectiveRecordCount) {
					// do it randomly, and respect blocks in case of relational/time series data
					_select = false;
					rr=0;
					for (int k=0;k<row_IDs.size();k++){
						
	
						double rv = _random.nextDouble() ;
						 
						_select = rv <= selectionProb ; 
						
						if ((_select) || (selectionProb>0.99)) {
							if (row_IDs.indexOf(i)<0){
								row_IDs.set(i,-1); 
								rr++;
							}
							// reflects resized data body;
							// this now means, that all data are referenced;
							// from this we will draw our records by random
						}
						if (row_IDs.size()-rr<=effectiveRecordCount){
							break;
						}
					} // k->
					
					int r=row_IDs.size()-1;
					while (r>=0){
						if (row_IDs.get(r)<0){
							row_IDs.remove(r) ;
						}
					}
					
					i++;
					if (i>absoluteRecordCount){
						i=0;
					}
					if ((row_IDs.size() > (double)absoluteRecordCount*1.002)){
						break;
					}
				} // i ->, row_IDs.size() oK ?
				
			}
		}
		
		Collections.sort( row_IDs) ;
		row_IDs.trimToSize();
		
		return row_IDs;
	}
	
	public ArrayList<Integer> createEffectiveRecordList( int absrecordcount, int epoch, int steps ){
		int i ,maxe,rr;
		int absoluteRecordCount = absrecordcount ;
		int currenteffectivecount;
		
		ArrayList<Integer> row_IDs = new ArrayList<Integer>();
		
		double _scaleFactor = 1.0;
		
		
		currenteffectivecount = absoluteRecordCount ;
		
		if (epoch>=0){ // creates approx the series 7%  16% 32% 100%
			
			// 1+(3- (3*SQRT(epoch/3))) * log10(N)
			_scaleFactor = (1.0+((steps-1.0)- ((steps-1.0)*Math.sqrt(((double)epoch/(steps-1.0))))) * Math.log10((double)currenteffectivecount)) ;
			
			effectiveRecordCount = (int) Math.round(currenteffectivecount /( _scaleFactor));
			
			effectiveRecordCount = Math.min(effectiveRecordCount , absoluteRecordCount) ;
			if (epoch==0){
				effectiveRecordCount = (int) (effectiveRecordCount/3.0) ; 
			}
			if (epoch>=2){
				double p;
				p = (double)((double)effectiveRecordCount/(double)absoluteRecordCount) ;
				if ((p>0.3) && (p<0.65)){
					p = p + ((1.0-p)/2.0);
					effectiveRecordCount = (int)Math.round( p*(double)absoluteRecordCount);
				}
			}
		}
		if (effectiveRecordCount < 100) {
			if (absrecordcount > 100) {
				effectiveRecordCount = 100;
			} else {
				effectiveRecordCount = absoluteRecordCount;
			}
		}
		
		
		
		double selectionProb = (double)(1.0*effectiveRecordCount) / (double)(1.0*absoluteRecordCount) ;
		boolean _select;
		
		if (selectionProb>0.975){
			selectionProb = 1.0;
		}
		
		// if selectionProb > 0.6 -> invert the procedure --- remove particles instead of drawing them
		i = 0; 
		if (selectionProb<0.72){
		
			while (row_IDs.size() < effectiveRecordCount) {
				// do it randomly, and respect blocks in case of relational/time series data
				_select = false;
				
				
				double rv = _random.nextDouble() ;
				 
				_select = rv <= selectionProb ; 
				
				if ((_select) || (selectionProb>0.99)) {
					if (row_IDs.indexOf(i)<0){
						row_IDs.add(i);
					}
					// reflects resized data body;
					// this now means, that all data are referenced;
					// from this we will draw our records by random
				}
				
				i++;
				if (i>absrecordcount){
					i=0;
				}
				if ((row_IDs.size() > (double)effectiveRecordCount*1.02)){
					break;
				}
			} // i ->, row_IDs.size() oK ?
		}else{
			for (int k=0;k<effectiveRecordCount;k++){
					row_IDs.add( k) ;
			}
			if (selectionProb<0.999){
				double deselectionProb = 1.0-selectionProb;
				
				while (row_IDs.size() > effectiveRecordCount) {
					// do it randomly, and respect blocks in case of relational/time series data
					_select = false;
					rr=0;
					for (int k=0;k<row_IDs.size();k++){
						
	
						double rv = _random.nextDouble() ;
						 
						_select = rv <= selectionProb ; 
						
						if ((_select) || (selectionProb>0.99)) {
							if (row_IDs.indexOf(i)<0){
								row_IDs.set(i,-1); 
								rr++;
							}
							// reflects resized data body;
							// this now means, that all data are referenced;
							// from this we will draw our records by random
						}
						if (row_IDs.size()-rr<=effectiveRecordCount){
							break;
						}
					} // k->
					
					int r=row_IDs.size()-1;
					while (r>=0){
						if (row_IDs.get(r)<0){
							row_IDs.remove(r) ;
						}
					}
					
					i++;
					if (i>absrecordcount){
						i=0;
					}
					if ((row_IDs.size() > (double)effectiveRecordCount*1.02)){
						break;
					}
				} // i ->, row_IDs.size() oK ?
				
			}
		}
		
		Collections.sort( row_IDs) ;
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

	// ------------------------------------------------------------------------
	
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

	public Map<Integer, Object> getSampleMapping() {
		return samplesMapping;
	}

	public void setSampleMapping(Map<Integer, Object> samplesmap ) {
		samplesMapping = samplesmap;
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
	
	
	public int getEffectiveRecordCount() {
		return effectiveRecordCount;
	}

	public int getGlobalLimit() {
		 
		return globalLimit;
	}

	public void setGlobalLimit(int globalLimit) {
		this.globalLimit = globalLimit;
	}

	
}

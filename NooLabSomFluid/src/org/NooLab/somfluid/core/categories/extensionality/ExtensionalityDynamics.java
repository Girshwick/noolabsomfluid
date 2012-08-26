package org.NooLab.somfluid.core.categories.extensionality;

import java.util.ArrayList;


import org.NooLab.field.FieldIntf;
import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.components.SomDataObject;

import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.NodeStatisticsFactory;
import org.NooLab.somfluid.core.engines.NodeStatisticsIntf;
import org.NooLab.utilities.datatypes.ValuePair;



/**
 * in-process preparations of dynamic variables that are derived from 
 * the actual list represented by the node, e.g.: 
 * - correlations between vars, 
 * - statistical properties
 * - any other procedural discrimination. like 
 *   > embedded ANN
 *   > embedded tiny SOM
 * 
 * this can be used to evaluate blocks or series of records within the SOM itself,
 * and in a dynamic manner, without the need to return to the transformer layer
 * for that purpose.
 * 
 * One could even think about an embedded capability for transformation; 
 * this does not change the logical distinction between outer loop and inner loop
 * in learning from data, yet.
 * It just provides a more elegant and a much more faster approach for the integration 
 * of the loops.
 * 
 * 
 * 
 * 
 * 
 */
public class ExtensionalityDynamics implements ExtensionalityDynamicsIntf{

	// SomDataObject somData;
	DataSourceIntf somData;
	SomProcessIntf processHost;
	
	ArrayList<Integer> listOfRecords = new ArrayList<Integer>();
	
	// in case of Astor in WebSom, this could be the the Index of the fingerprint entry of the word
	// we store the frequency along with it, additionally, we need an external process that is 
	// unificating multiple entries of the same id-value 
	ArrayList<ValuePair> listOfSecondaryId = new ArrayList<ValuePair>();
	
	/* TODO: needed: 
	 *          - a statistical description, 
	 * 			- indication of method for evaluating the container 
	 *            (most simple: frequency ratio, more advanced: SOM, PCA, Cronbach
	 * 	        - the list of derived features, that would be added to the 
	 *            original feature vector, or which would replace them
	 *            
	 */
	
	//NodeStatisticsIntf statistics ;
	NodeStatisticsIntf statistics ;
	
	/** the positive predictive value of the node */
	double ppv = -1.0, npv = -1.0;
	
	// given a particular threshold for 
	double majorityValueIdentifier = -1.0 ;

	private int ppvRank;

	private int support;
	
	long nodeNumGuid = -1L, serialId= -1L;
	
	// ========================================================================
	public ExtensionalityDynamics( DataSourceIntf somdata , SomProcessIntf processhost, int somType){
								   // SomDataObject
		somData = somdata;
		processHost = processhost ;
		
		statistics = NodeStatisticsFactory.getStatisticsImpl(somType);
		
		
	}
	
	public ExtensionalityDynamics( ExtensionalityDynamics inExtensions, int somType) {
		
		somData = inExtensions.somData;
		processHost = inExtensions.processHost;
		
		statistics = inExtensions.getStatistics()  ; 
		
	}
	
	// ========================================================================
	 

	@Override
	public long getNodeNumGuid() {
		 
		return nodeNumGuid;
	}

	@Override
	public void setNodeNumGuid(long idValue) {
		 
		nodeNumGuid = idValue;
	}

	@Override
	public long getNodeSerial() {
		return serialId;
	}

	@Override
	public void setNodeSerial(long idValue) {
		serialId = idValue;
	}

	@Override
	public void clear() {
		
		try{
			statistics.resetFieldStatisticsAll();
			listOfRecords.clear() ;
			listOfRecords.trimToSize() ;	
		}catch(Exception e){
		
		}
	
	}

	public void setListOfRecordsAsTable( ArrayList<Integer> records){
		listOfRecords = records;
	}

	
	public void addRecordByIndex( int index){
		addRecordByIndex(  index, -1, false) ;
	}
	public void addRecordByIndex( int index, boolean informUpstream){
		addRecordByIndex(  index, 1, informUpstream) ;
	}
	/**
	 * 
	 * @param index
	 * @param returnMode 0=size, 1=the inserted index
	 * @return
	 */
	public int addRecordByIndex( int index, int returnMode ){
		return addRecordByIndex(  index, -1, false) ;
	}
	
	/**
	 * 
	 * @param index value to be inserted, it is the global record index in the data table as defined by the active ID column 
	 * @param returnMode  0=size, 1=the inserted index
	 * @param informUpstream
	 * @return
	 */
	public int addRecordByIndex( int index, int returnMode , boolean informUpstream){
		// take it to the list, but only if it is not there yet
		
		int size = 0, result=-1;
		
		if (listOfRecords.indexOf(index)<0){
			listOfRecords.add(index) ;
			// release an event ... ? 
			size = listOfRecords.size() ;
			
			
			if (returnMode<=0){
				result = size;
			}else{
				result=index;
			}
			if ((informUpstream) && (somData!=null)){
				somData.nodeChangeEvent( (ExtensionalityDynamicsIntf)this, result );
				processHost.nodeChangeEvent( (ExtensionalityDynamicsIntf)this, result );
			}
		}
		
		return result;
	}

	public void removeRecordByIndex( int index){
		
		if (index<0){
			return;
		}
		//  || (index>=listOfRecords.size()
		int p = listOfRecords.indexOf(index) ;
		
		if (p>=0){
			listOfRecords.remove(index) ;
		}
	}
	
	@Override
	public int getRecordItem(int index) {
		  
		return listOfRecords.get(index);
	}

	public ArrayList<Integer> getListOfRecords(){
		return listOfRecords ;
	}

	
	@Override
	public ArrayList<ValuePair> getListOfSecondaryId() {
		return listOfSecondaryId;
	}

	@Override
	public void setListOfSecondaryId(ArrayList<ValuePair> idValuePairs) {
		listOfSecondaryId = idValuePairs;
	}

	public void addSecondaryIndexValue( long idvalue ){
		listOfSecondaryId.add( new ValuePair(idvalue, -1) );
	}
	
	@Override
	public int getCountSecondaryIndex() {
		return 0;
	}
	


	@Override
	public int getCount() {
		 
		return listOfRecords.size() ;
	}

	public NodeStatisticsIntf getStatistics() {
		return statistics;
	}

	@Override
	public void setPPVrank(int rank) {
		ppvRank = rank;
	}

	/**
	 * @return the ppvRank
	 */
	public int getPpvRank() {
		return ppvRank;
	}

	/**
	 * @param ppvRank the ppvRank to set
	 */
	public void setPpvRank(int ppvRank) {
		this.ppvRank = ppvRank;
	}

	@Override
	public void setPPV(double ppvValue) {
		 
		this.ppv = ppvValue;
	}

	public double getPPV() {
		return ppv;
	}

	public double getMajorityValueIdentifier() {
		return majorityValueIdentifier;
	}

	public void setMajorityValueIdentifier(double majorityValueIdentifier) {
		this.majorityValueIdentifier = majorityValueIdentifier;
	}

	public void setStatistics(NodeStatisticsIntf statistics) {
		this.statistics = statistics;
	}

	@Override
	public void setNPV(double npv) {
		this.npv = npv ;
	}

	@Override
	public double getNPV() {
		return npv;
	}

	@Override
	public void setSupport(int n) {
		support = n;
		
	}

	public int getSupport() {
		return support;
	}

	public DataSourceIntf getSomData() {
		return somData;
	}

	public void setSomData(SomDataObject somData) {
		this.somData = somData;
	}

	public double getPpv() {
		return ppv;
	}

	public void setPpv(double ppv) {
		this.ppv = ppv;
	}

	public double getNpv() {
		return npv;
	}

	public void setNpv(double npv) {
		this.npv = npv;
	}

	 
	
	
	
}

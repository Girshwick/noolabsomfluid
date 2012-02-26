package org.NooLab.somfluid.core.categories.extensionality;

import java.util.ArrayList;

import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.NodeStatistics;
import org.NooLab.somfluid.data.DataTable;


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

	SomDataObject somData;
	
	ArrayList<Integer> listOfRecords = new ArrayList<Integer>();
	
	
	/* TODO: needed: 
	 *          - a statistical description, 
	 * 			- indication of method for evaluating the container 
	 *            (most simple: frequency ratio, more advanced: SOM, PCA, Cronbach
	 * 	        - the list of derived features, that would be added to the 
	 *            original feature vector, or which would replace them
	 *            
	 */
	
	NodeStatistics statistics ;

	/** the positive predictive value of the node */
	double ppv;
	double majorityValueIdentifier = -1.0 ;
	
	// ========================================================================
	public ExtensionalityDynamics( SomDataObject somdata){
		
		somData = somdata;
		statistics = new NodeStatistics()  ;
		
	}
	// ========================================================================
	
	public void setListOfRecordsAsTable( ArrayList<Integer> records){
		listOfRecords = records;
	}

	public void addRecordByIndex( int index){
		// take it to the list, but only if it is not there yet
		
		if (listOfRecords.indexOf(index)<0){
			listOfRecords.add(index) ;
		}
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
	public void clear() {
		
		statistics.resetFieldStatisticsAll();
		listOfRecords.clear() ;
		listOfRecords.trimToSize() ;
	}

	@Override
	public int getRecordItem(int index) {
		  
		return listOfRecords.get(index);
	}

	public ArrayList<Integer> getListOfRecords(){
		return listOfRecords ;
	}

	@Override
	public int getCount() {
		 
		return listOfRecords.size() ;
	}

	public NodeStatistics getStatistics() {
		return statistics;
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

	public void setStatistics(NodeStatistics statistics) {
		this.statistics = statistics;
	}

	 
	
	
	
}

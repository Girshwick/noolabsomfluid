package org.NooLab.somfluid.core.categories.extensionality;

import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.NodeStatistics;




public interface ExtensionalityDynamicsIntf {

	
	public int getCount();

	/**
	 * @return contains index values that will be pointing to the data table
	 */
	public ArrayList<Integer> getListOfRecords() ;

	public void addRecordByIndex( int index) ;

	/**
	 * @param iindex pointing to the list of the extension, not to the data table!
	 * @return
	 */
	public int getRecordItem(int iindex );
	
	/**
	 * @param indexPtr is pointing to the data table, =value at the i-th position of the list of record indexes
	 */
	public void removeRecordByIndex( int indexPtr ) ;
	

	public void clear();

	// ....................................................
	
	public NodeStatistics getStatistics();
	
	// ....................................................

	public void setPPV(double ppv) ;
	
	public double getPPV() ;

	public void setNPV(double npv);
	
	public double getNPV() ;

	
	public double getMajorityValueIdentifier() ;

	public void setMajorityValueIdentifier(double majorityValueIdentifier) ;

}

package org.NooLab.somfluid.core.categories.extensionality;

import java.util.ArrayList;

 
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.NodeStatisticsIntf;
import org.NooLab.utilities.datatypes.ValuePair;




public interface ExtensionalityDynamicsIntf {

	
	public long getNodeNumGuid();
	public void setNodeNumGuid(long idValue);
	
	public long getNodeSerial();
	public void setNodeSerial(long idValue);
	
	// public void setProcessHost(SomProcessIntf somProcessParent);
	
	/**
	 * @return number of records in this collection of extensions 
	 */
	public int getCount();

	/**
	 * @return contains index values that will be pointing to the data table
	 */
	public ArrayList<Integer> getListOfRecords() ;

	
	/**
	 * @param index
	 * @param returnMode
	 * @param informUpstream
	 * @return
	 */
	public int addRecordByIndex( int index, Object collectibleColumnInfo, int returnMode , boolean informUpstream) ;
	public void addRecordByIndex(int indexValues, Object collectibleColumnInfo,boolean informUpdatream);
	public void addRecordByIndex( int index,Object collectibleColumnInfo) ;
	public int addRecordByIndex( int index, Object collectibleColumnInfo,int returnMode );
		
	
	 
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
	
	public NodeStatisticsIntf getStatistics();
	
	// ....................................................

	public void setPPV(double ppv) ;
	
	public double getPPV() ;

	public void setNPV(double npv);
	
	public double getNPV() ;

	
	public double getMajorityValueIdentifier() ;

	public void setMajorityValueIdentifier(double majorityValueIdentifier) ;

	public void setPPVrank(int rank);

	public int getPpvRank();

	public void setSupport(int n);

	public int getSupport();
	
	public ArrayList<Object> getListOfSecondaryId();
	
	public void setListOfSecondaryId(ArrayList<Object> collectibleObj);
	
	public int getCountSecondaryIndex();
	

	
}

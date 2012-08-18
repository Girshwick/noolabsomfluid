package org.NooLab.field.repulsive.intf;

import java.util.ArrayList;




public interface DataObjectIntf {

	/** the index value that points to the index of an external data container;  */
	public void setIndexOfDataObject( long index );
	
	/** the data object itself */
	public void registerDataObject( Object dataObject);
	

	public long getIndexOfDataObject();
	public ArrayList<Long> getIndexesOfAllDataObject();
	public void mergeDataObjects( long[] indexes);
	public void mergeDataObjects(ArrayList<Long> indexes );
	public void removeIndexOfDataObject(long index);
	public void clearIndexOfDataObject();
	
	
}

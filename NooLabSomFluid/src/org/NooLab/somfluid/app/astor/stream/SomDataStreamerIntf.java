package org.NooLab.somfluid.app.astor.stream;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.storage.DataTable;




public interface SomDataStreamerIntf {

	

	public void setDataTableOffset( int firstUseRowinTable) ;
	
	public int getDataTableOffset() ;
	
	public void setDataTableReference( DataTable datatable);
	
	public DataTable getDataTableReference();

	public SomDataObject getSomDataObject();

	public void setSomDataObject(SomDataObject sdo);
}

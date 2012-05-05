package org.NooLab.somfluid.env.data;

import org.NooLab.somfluid.data.DataTable;

public interface DataFileReceptorIntf {

	void loadFromFile(String filename) throws Exception;

	String getLoadedFileName();

	public RawFileData getRawFileData();
	
	public DataTable getDataTable() ;

	void loadProfilesFromFile(String filename) throws Exception;
}

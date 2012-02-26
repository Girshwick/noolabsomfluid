package org.NooLab.somfluid.data;



public interface DataHandlingPropertiesIntf {

	public String getDataSrcFilename() ;

	public void addDataSource(int sourceType, String filename) ;
	
	public void setDataSrcFilename(String dataSrcFilename) ;
	
	// ..........................................
	
	public int getDataUptakeControl() ;
	
	public void setDataUptakeControl(int ctrlValue) ;
	
	// ------------------------------------------------------------------------
	
	

}

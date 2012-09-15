package org.NooLab.somfluid.tasks;

import org.NooLab.structures.SomTaskDependProcessIntf;


public interface SomFluidProbTaskIntf  extends SomFluidTaskIntf{

	public void setStartMode(int startingMode);
	
	public int getStartMode();

	public String getGuid();
	
	public int getCounter();

	public boolean activatedDataStreamReceptor();
	
	public void activateDataStreamReceptor(boolean flag);
	
	public void setDependencies( SomTaskDependProcessIntf dependencies );
	
	public SomTaskDependProcessIntf getDependencies();

	public void setApplicationContext(String contextMode);
	public String getApplicationContext();

	
	public void setSourceDatabase(int sourceDbType, String sourceDbName);  // for SOM source
	 
	
	/** the new SOM source  */
	public void setTransferTargetDatabase( int targetDbType, String targetDbName );
	public String getTransferTargetDatabase();
	
	/** the source for creating a new SOM source, could be a purplenodes or astornodes database  */
	public void setTransferSourceDatabase( int targetDbType, String targetDbName, long somId);

	public String getTransferSourceDatabase();
	
	
	public long getTransferSourceSomId();

	 
	public String getDataBaseName();

	public void setDataBaseName(String dbname);

	
	
}

package org.NooLab.somfluid.env.data;

import java.util.ArrayList;

import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.astor.SomDataStreamer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.env.communication.DataglueReceptorIntf;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * This class organizes the uptake of data from the environment;  </br>
 * possible sources are: </br>
 * - external physical files (txt, xls) </br>
 * - databases  </br>
 * - the (internal) GlueClient </br> </br>
 * 
 * 
 * 
 * 
 */
public class DataReceptor implements // 
										DataFileReceptorIntf,
										DataDBReceptorIntf,
										DataglueReceptorIntf {

	 
	SomDataObject somData;
	
	String loadedFile = "";
	String preparedFile = "";
	
	String loadedProfileFile;
	
	RawFileData rawFileData, rawFileProfileData;
	
	DataTable dataTable, profilesTable;
	
	PrintLog out;
	
	
	// ------------------------------------------------------------------------
	public DataReceptor(   SomDataObject somdata) {
		
		 
		somData = somdata;
		
	}

	// ------------------------------------------------------------------------

	
	@Override
	public void loadProfilesFromFile(String filename) throws Exception {
		out = somData.getOut();
		
		loadedProfileFile = filename;
		
		rawFileProfileData = new RawFileData(  somData, out ) ;
		
		profilesTable = rawFileData.getDataTable(); 
		
		rawFileProfileData = null;
	}

	@Override
	public void loadFromFile(String filename) throws Exception {
		
		
		out = somData.getOut();
		
		loadedFile = filename;
		
		rawFileData = new RawFileData(  somData, out ) ;
		
		rawFileData.readRawDatafromFile( filename );
		
		dataTable = rawFileData.getDataTable(); 
		
		// somData.importDataTable( somData.getTransformer(), dataTable );
	
		rawFileData=null;
		// dataTable=null;
	}

	@Override
	public String getLoadedFileName() {
		
		return loadedFile;
	}

	/**
	 * @return the rawFileData
	 */
	public RawFileData getRawFileData() {
		return rawFileData;
	}

	/**
	 * @return the dataTable
	 */
	public DataTable getDataTable() {
		return dataTable;
	}

	public void loadFromDataBase( int recCount) {
		
		SomDataStreamer dataStreamer;
		TexxDataBaseSettingsIntf dbsett;
		
		int k=0;
		
		
		
		try{
			
			dbsett = somData.getDatabaseSettings();
			dataStreamer = somData.getSomDataStreamer();
			
			
			// instead of reading from a file, we issue a query to the database randomwords
			
			
			
			// dealing with the result set
			
			// determining max and min through db queries
			
		}catch(Exception e){
			
		}
		
	}
	
	
	
	
	
}


















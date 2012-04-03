package org.NooLab.somfluid.env.data;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
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

	SomFluidProperties sfProperties;
	SomDataObject somData;
	
	String loadedFile = "";
	String preparedFile = "";
	
	RawFileData rawFileData;
	
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public DataReceptor( SomFluidProperties sfProps, SomDataObject somdata) {
		
		sfProperties = sfProps ;
		somData = somdata;
		
	}

	// ------------------------------------------------------------------------

	


	@Override
	public void loadFromFile(String filename) throws Exception {
		
		DataTable dataTable;
		out = somData.getOut();
		
		loadedFile = filename;
		
		rawFileData = new RawFileData(  somData, out ) ;
		
		rawFileData.readRawDatafromFile( filename );
		
		dataTable = rawFileData.getDataTable(); 
		
		somData.importDataTable( somData.getTransformer(), dataTable );
	
		rawFileData=null;
		// dataTable=null;
	}

	@Override
	public String getLoadedFileName() {
		
		return loadedFile;
	}
	
	
	
	
	
}


















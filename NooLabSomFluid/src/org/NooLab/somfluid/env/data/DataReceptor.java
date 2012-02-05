package org.NooLab.somfluid.env.data;

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
	
	RawFileData rawFileData;
	
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public DataReceptor( SomFluidProperties sfProps, SomDataObject somdata) {
		
		sfProperties = sfProps ;
		somData = somdata;
		
	}

	// ------------------------------------------------------------------------

	


	@Override
	public void loadFromFile(String filename) {
		
		DataTable dataTable;
		out = somData.getOut();
		
		rawFileData = new RawFileData( out ) ;
		
		rawFileData.readRawDatafromFile( filename );
		
		dataTable = rawFileData.getDataTable(); 
		
		somData.importDataTable( dataTable );
	
		
		rawFileData=null;
		dataTable=null;
	}
	
	
	
	
	
}


















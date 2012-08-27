package org.NooLab.somfluid.env.data;

import java.util.ArrayList;
import java.util.Collection;

import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.astor.stream.SomDataStreamer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.VariableSettingsHandlerIntf;
import org.NooLab.somfluid.env.communication.DataglueReceptorIntf;
import org.NooLab.somfluid.env.data.db.DataBaseAccessDefinition;
import org.NooLab.somfluid.env.data.db.XColumn;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;
import org.apache.commons.collections.CollectionUtils;


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

	private SomFluidProperties sfProperties;
	
	StringsUtil strgutil = new StringsUtil(); 
	ArrUtilities arrutil = new ArrUtilities();

	private String sql = "";
	
	
	// ------------------------------------------------------------------------
	public DataReceptor(   SomDataObject somdata) {
		
		 
		somData = somdata;
		
	}

	// ------------------------------------------------------------------------

	
	@Override
	public void loadProfilesFromFile(String filename) throws Exception {
		out = somData.getOut();
		
		loadedProfileFile = filename;
		
		rawFileProfileData = new RawFileData(  somData ) ;
		
		profilesTable = rawFileData.getDataTable(); 
		
		rawFileProfileData = null;
	}

	@Override
	public void loadFromFile(String filename) throws Exception {
		
		
		out = somData.getOut();
		
		loadedFile = filename;
		
		rawFileData = new RawFileData(  somData ) ;
		
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

	@SuppressWarnings("unchecked")
	public void loadFromDataBase( int recCount) {
		
		SomDataStreamer dataStreamer;
		TexxDataBaseSettingsIntf dbsett;
		DataBaseAccessDefinition dbAccess ;
		
		VariableSettingsHandlerIntf variableSettings;
		ArrayList<String> fieldLabels,requestFields, excludeds;
		
		String fieldLabelsStrsql="";
		int k=0;
		
		try{
			
			dbsett = somData.getDatabaseSettings();
			dataStreamer = somData.getSomDataStreamer();
			
			if (somData.isDatabaseConnection()==false){
				return ;
			}
			
			sfProperties = somData.getSfProperties() ;
			
			// instead of reading from a file, we issue a query to the database randomwords
			
			dbAccess = somData.getDbAccessDefinition() ;
			
			fieldLabels = dbAccess.getxColumns().getLabels("contexts",0) ;
			// remove blacklisted labels
			 
			fieldLabels = arrutil.makeItemsUnique( fieldLabels) ;
			 
			
			
			variableSettings = sfProperties.getVariableSettings() ;
			excludeds = variableSettings.getAbsoluteExclusions();  //  larger than fieldLabels
			
			// 1. get all excludeds that are part of the fieldlabels list
			excludeds = (ArrayList<String>) CollectionUtils.intersection( fieldLabels , excludeds) ;
			// 2. remove them
			requestFields = (ArrayList<String>) CollectionUtils.disjunction( fieldLabels , excludeds) ;
			// unfortunately requestFields is now sorted
			requestFields = arrutil.alignStringList( requestFields, fieldLabels,0); // second list provides the sorting
			
			
			dataTable = somData.getSomTexxDb().retrieve("randomwords", requestFields, 1000);
			
			// note, that the docid is = timestamp if the doc is directly digested 
			  
			
			// -> rawFileData + DataTable datatable ;
			// determining max and min through db queries for fields, update the XColumn, which 
			// we will use later in SomTransformer
			
			 
		}catch(Exception e){
			e.printStackTrace();
		}
		k=k+1-1;
		 
	}
	
	
	
	
	
}


















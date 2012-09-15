package org.NooLab.somfluid.env.data;

import java.util.ArrayList;
import java.util.Collection;

import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.itexx.storage.docsom.AstorDocSomDataBase;
import org.NooLab.itexx.storage.docsom.AstorDocSomDataBaseIntf;
import org.NooLab.itexx.storage.somfluid.db.DataBaseAccessDefinition;
import org.NooLab.itexx.storage.somfluid.db.XColumn;
import org.NooLab.itexx.storage.somfluid.db.XColumns;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.astor.stream.SomDataStreamer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.env.communication.DataglueReceptorIntf;
 
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.ArrUtilities;
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
	public void loadFromDataBase( int recCount, int databaseStructureCode) {
		
		SomDataStreamer dataStreamer;
		TexxDataBaseSettingsIntf dbsett;
		DataBaseAccessDefinition dbAccess ;
		
		VariableSettingsHandlerIntf variableSettings;
		ArrayList<String> fieldLabels = null,requestFields=null, excludeds;
		
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
			XColumns xcol ;
			xcol = dbAccess.getxColumns();
			
			if (databaseStructureCode==1){
				
				if (xcol!=null){
					fieldLabels = xcol.getLabels("contexts",0) ;
				}
			}else{
				if (xcol!=null){
					fieldLabels = xcol.getLabels("randomdocuments",0) ;
				}
			}
			// remove blacklisted labels
			 
			if (fieldLabels != null){
				fieldLabels = arrutil.makeItemsUnique( fieldLabels) ;
			}else{
				fieldLabels = new ArrayList<String>(); 
			}
			
			variableSettings = sfProperties.getVariableSettings() ;
			excludeds = variableSettings.getAbsoluteExclusions();  //  larger than fieldLabels
			
			// 1. get all excluded fields that are part of the fieldlabels list
			excludeds = (ArrayList<String>) CollectionUtils.intersection( fieldLabels , excludeds) ;
			// 2. remove them
			requestFields = (ArrayList<String>) CollectionUtils.disjunction( fieldLabels , excludeds) ;
			// unfortunately requestFields is now sorted
			requestFields = arrutil.alignStringList( requestFields, fieldLabels,0); // second list provides the sorting
			
			// TODO: we should not test against the name, but against a flag 
			//       that indicates whether the table contains a {;} list !!!
			//       or against the definition, whether there is a varchar(2000);
			
			if ((databaseStructureCode>=1) && (databaseStructureCode<=3)){
				
				// TODO :  this implies an object holding the definition, and other metadata, 
				// 		   specific for DB type and task type
				int dbsCode = dbAccess.getDatabaseStructureCode() ;
				
				// e.g. TexxDataBaseSettingsIntf._DATABASE_STRUC_CONTEXTS_L0
				if (dbAccess.getDatabaseName().contentEquals("randomwords")){ // contains table "contexts" with column "randomcontext"
					// the "randomwords" DB is a special one, because the actual data vector is contained as a list [;]
					dataTable = somData.getSomTexxDb().retrieve( dbsCode , "randomwords", requestFields, 1000);
					
				}
				// the same is true for "astordocs" which contains a table "randomdocuments" and this a column "randomdoc"
				if (dbAccess.getDatabaseName().contentEquals("astordocs")){ //
					
					// AstorDocSomDataBase astordbi = somData.getSomAstorDb();
					// dataTable = astordbi.retrieve("randomwords", requestFields);
					dataTable = somData.getSomTexxDb().retrieve( dbsCode, "astordocs", requestFields, 25000);
				}
				
			}
			else{
				// here we load normal tables, according to structureCode, without need for post-processing of the loaded fields
				
			}
			
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


















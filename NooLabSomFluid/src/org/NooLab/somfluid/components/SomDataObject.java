package org.NooLab.somfluid.components;

import java.io.*; 
import java.util.*;




import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.itexx.storage.docsom.AstorDocSomDataBase;

import org.NooLab.itexx.storage.somfluid.db.DataBaseAccessDefinition;
import org.NooLab.somfluid.SomDataDescriptor;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.astor.stream.SomDataStreamer;
import org.NooLab.somfluid.clapp.SomAppTransformer;
import org.NooLab.somfluid.components.variables.SomVariableHandling;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;

import org.NooLab.somfluid.data.TableImportSettings;
import org.NooLab.somfluid.env.data.DataFileReceptorIntf;
import org.NooLab.somfluid.env.data.DataReceptor;
import org.NooLab.somfluid.env.data.SomTexxDataBase;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.storage.PersistentAgentIntf;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;
import org.NooLab.somfluid.structures.Variables;


import org.NooLab.somtransform.SomTransformer;
import org.NooLab.somtransform.SomTransformerIntf;
import org.NooLab.somtransform.TransformationModel;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.FileDataSource;
import org.NooLab.utilities.files.WriteFileSimple;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.strings.StringsUtil;
import org.NooLab.utilities.xml.*;




/**
 * 
 * provides access to various stages of data, esp. in reflexive clustering, which needs
 * online measurements of SOM and SOMdata itself
 * 
 * 
 * is able to harvest from various types of sources
 * listen to channel (passive): file, database, port
 * actively check for new data: file, http, ftp
 * 
 *  
 */
public class SomDataObject 	implements      Serializable,
										//	used for read access, e.g. by nodes and other applications
											DataSourceIntf,
										//  other stuff except data source access
											SomDataObjectIntf,
									    //  all objects that need to be storable should implement this interface
											PersistentAgentIntf{

	private static final long serialVersionUID = 3912456095091958187L;
	
	
	public static final String _TEMPDIR_PREFIX = "~noo-sf-" ;
	
	transient DataHandlingPropertiesIntf dataHandlingProperties;
	transient SomTransformerIntf transformer;
	transient SomTransformer profilesTransformer;
	
	
	// object references ..............
	FileDataSource filesource;
	transient XmlFileRead xmlFile ;
	
	transient TexxDataBaseSettingsIntf databaseSettings ;
	transient DataBaseAccessDefinition dbAccessDefinition;
	transient SomDataStreamer somDataStreamer ;
	
	transient SomTexxDataBase somTexxDb ;
	transient AstorDocSomDataBase somAstorDb ;
	
	// main variables / properties ....
	transient SomFluidFactory sfFactory ;
	
	DataTable data=null ;
	DataTable normalizedSomData=null ;
	
	transient DataFileReceptorIntf dataReceptor;
	SomFluidProperties sfProperties;
	
	DataTable profilesTable ; // for simulation mode
	
	boolean openChanges=false;
	
	Variables variables = new Variables() ;
	Variables activeVariables;

	ArrayList<String> variableLabels = null; 

	// needed for dealing with the target variable stuff
	ClassificationSettings classifySettings ;
	ModelingSettings modelingSettings;
	
	MissingValues missingValues;  

	int maxColumnCount ;
	int maxRecordCount = -1 ;
	int inits =-1;
	
	boolean databaseConnection = false;
	boolean dataAvailable=false ;
	
	// read mode = random, serial, block (begin, end) ?
	
	// volatile variables .............
	int dobjsIndex ; // == an identifier in the vector of SomDataObjects, maintained by Spela 
	
	int vectorSize;
	
	
	

	// helper objects .................
	transient public StringsUtil strgutil = new StringsUtil();
	transient public DFutils fileutil = new DFutils () ; 
	transient public ArrUtilities arrutil = new ArrUtilities();
	
	transient PrintLog out = new PrintLog(2,true) ;


	private boolean normalizeData;


	private int streamingRowOffset;
	// transient arrutil

	
		
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	 
	public SomDataObject( DataHandlingPropertiesIntf datahandleProps, SomFluidProperties sfProps) throws Exception{
		
		dataHandlingProperties = datahandleProps;
		
		sfProperties = sfProps;
		missingValues = new MissingValues(this);
		data = new DataTable( this, true ); // true: isnumeric, Som data objects always contain numeric data
		
		if (init()==false){
			throw(new Exception("initializing data base failed while preparing SomDataObject."));
		}
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	 
	
	public void setFactory(SomFluidFactory factory) throws Exception {
		sfFactory = factory;
		sfProperties = sfFactory.getSfProperties() ;
		if (init()==false){
			throw(new Exception("initializing data base failed while preparing SomDataObject."));
		}else{
			databaseConnection = true;
		}
	}
	
	
	private boolean init() throws Exception{
		boolean dbOk = false;
		
		if (inits<0){
			databaseConnection = false;
			inits++;
			modelingSettings = sfProperties.getModelingSettings() ;
			classifySettings = modelingSettings.getClassifySettings() ; 
			
			databaseSettings = sfProperties.getDatabaseSettings() ;
			dbAccessDefinition = sfProperties.getDbAccessDefinition() ;
			
			dbOk = true;
			if (sfProperties.getSourceType()== DataStreamProviderIntf._DSP_SOURCE_DB){
				String dbname = "randomwords" ;
				DataBaseAccessDefinition dbaccess ;
				
				dbaccess = sfProperties.getDbAccessDefinition() ;
				if (dbaccess.getDatabaseName().length()>0){
					dbname = dbaccess.getDatabaseName();
				}
				
				// if (dbaccess.getDatabaseStructureCode() == TexxDataBaseSettingsIntf._DATABASE_STRUC_CONTEXTS_L0)
				{
					somTexxDb = new SomTexxDataBase(this, sfProperties);
					dbOk = somTexxDb.prepareDatabase(dbname );
					// , dbaccess.getDatabaseStructureCode() 
				}
				
			}
		}else{
			dbOk= true;
		}
		
		return dbOk;
	}

	/**
	 * creating utility objects on loading the SDO 
	 */
	public void reestablishObjects() {
		
		strgutil = new StringsUtil();
		fileutil = new DFutils () ; 
		// utils = new ArrUtilities();
		variables.reestablishObjects();
	}


	public void clear() {
		missingValues = null; 
		variables.clear(0) ;
		
		data.clear();
		
		strgutil = null;
		fileutil = null; 
		 
		
	}


	public void prepare() {
	
		String filename="";
		 
		if ((dataHandlingProperties.getDataUptakeControl()>=0)){
			// load data into SomDataObject
			
			if (sfProperties.isITexxContext()==false){
				filename = dataHandlingProperties.getDataSrcFilename() ;
			
				if (fileutil.fileexists(filename)==false){
					return;
				}
			}else{
				// check dbfile
			}
			// now the SomDataObject has a table loaded
			// only for XML output from transformer,etc... readData(filename);
			
		} // getDataUptakeControl >=0 ?
		
	}
	
	/**
	 * this is used in the context of Associative Storage ???
	 * 
	 * @param sfProperties2
	 * @return
	 */
	public static SomDataObject openSomDataSource(SomFluidProperties sfProperties2) {
	
		return null;
	}
	
	/**
	 * 
	 * this imports a .xmd file, which is XML + transformed data
	 * 
	 * @param filsrc
	 * @return
	 */
	public boolean importDataSource( FileDataSource filsrc ){
		boolean rb = false;
		String filename ;
		File fil;
		
		try{
			// data are delivered as an external file
			 
			filename = filsrc.getResourceLocator();
			
			fil = new File(filename) ;
			
			if (fil.exists()){
				
				readData( filename );
				rb = data.isFilled() ;
				
				
			} // fil ?
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		
		return rb;
	}
	

	public void registerDataReceptor(DataFileReceptorIntf datareceptor){
		dataReceptor = datareceptor;
		
	}


	public void readData( String filename ) {
	 
		
		readPreparedData( filename, false);
		
	}
	
	
	/**
	 * 
	 * this reads data, which have been created by the transformation instance;<br/><br/>
	 * - the file format is "compound text", the structure is maintained by tags,
	 *   it is XML, where the data are contained as a pseudo-XML section;<br/>
	 *   this requires a preprocessing, which then alows also for including the data as a zipped string<br/><br/>
	 * - the sections are<br/> 
	 *   > table
	 *   > variables (label, state(raw, derived) type (potential TV, predictive, both) )
	 *   > project
	 *   > session
	 *   
	 * i.e. the data is (most likely) normalized, 
	 * 
	 * @param filename
	 * @param addIndexColumn
	 * @param fillrawdatatable
	 * @return
	 */
	@SuppressWarnings("unused")
	public void readPreparedData( String filename, boolean fillrawdatatable) {
		
		String datasectionContent ,nodelabel, taglabel, temp_dir;
	 
		// exporting the data section into a temp file
		// just as a whole string, which is XML   
		// as section  <tabledata > and child <data content="id ...." />
 		
		xmlFile = new XmlFileRead( filename ) ;
		
		
		
		// get data section
		nodelabel = "data" ;
		taglabel  = "content" ;
		
		datasectionContent = xmlFile.getXmlTagData( "tabledata", nodelabel, taglabel) ;
		
		temp_dir = System.getProperty("java.io.tmpdir");
		filename = temp_dir+"tmp.txt" ;
		
		if (datasectionContent.length()>5){
			datasectionContent = strgutil.replaceAll(datasectionContent, "||", "\n");
			          fileutil.deleteFile(filename);
			          
			WriteFileSimple txtfile = new WriteFileSimple(filename, datasectionContent);
			
			readDataSectionfromTmpFile(filename,true);
			
			
			// TOOD: read administrative & control data from XML and config file !!!
			
			dataAvailable = false ;
			if (data.isFilled()){
				dataAvailable = true;
			}
			
		}
		
		
	}	

	
	@SuppressWarnings("unused")
	private void readDataSectionfromTmpFile( String filename, boolean activateMV){ 
		// later also a version which reads from a stream
		
		int addIndexColumn=0, vectorsize , datavectorsize,cellcount;
		int record_counter = 0, sIDextend = 0, z = 0, j, return_value = -1,n,k , ps;
		long id;
		String cs, separator ="\t", _id_rnum_str = "", cellStr, hs1, hs2;
		 
		 
		String[] cellStrings = null;
		double[] tmp;
		double val;
		
		
		Variable var ;
		
		File file ;
		


		
		return_value = -4;

		file = new File(filename);
		
		if (file.exists()==false){
			return ;
		}
		
		BufferedReader reader = null;
		
		
		return_value = -5;
		
		try {
			
			 
			return_value = -6;
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			return_value = -7;
			
			
			// .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
			
			// reading the first row
			text = reader.readLine();
			// column headers should be read and stored !

		 
			ps = text.indexOf("\t");
			if (ps<0){
				ps = text.indexOf(";");
				if (ps>=0){
					separator = ";" ;
				} else{
					ps = text.indexOf(" ");
					if (ps>=0){
						separator = " " ;
					}
				}
			}
			
			
			if (text.indexOf(separator) > 0) {
				cellStrings = text.split(separator);
			} else {
				if (text.indexOf(" ") > 0) {
					text = text.replace("  ", " ");
					cellStrings = text.split(" ");
				}

			}

			
			return_value = -8;
			
			vectorsize = cellStrings.length;
			
			datavectorsize = vectorsize;
			if ( variables == null ) {
				variables = new Variables() ;

			}

			variables.clear(0) ;
			
			
			for (j = 0; j < vectorsize; j++) {
				if ((cellStrings != null) && (cellStrings[j] != null)) {
					cs = cellStrings[j];
				} else {
					cs = "col" + String.valueOf(j);
				}

				var = new Variable() ;
				var.setLabel(cs) ;
				variables.additem(var) ;

			}

			data.setNumeric(true) ;
			// creating the table and inserting the headers, creates
			// just the appropriate number of columns
			data.opencreateTable( cellStrings );
			
			
			
			if (activateMV==true){
				data.activateMissingValues(-1);
			}
			
			// .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
			// ... now the values ... 
			
			return_value = -10;
			record_counter = 0;
			z = 0;
			// separator = "\t" ;
			while ((text = reader.readLine()) != null) {

				 
				return_value = -11;
				
				ps = text.indexOf("\t");
				if (ps<0){
					ps = text.indexOf(";");
					if (ps>=0){
						separator = ";" ;
					}
				}
				cellStrings = text.split(separator);

				// private Vector<String[]> rawData = new Vector<String[]>();
				n = cellStrings.length;
				if (vectorsize != n) {
					 
					if (vectorsize > n) {
						vectorsize = cellStrings.length;
					}
					;
					if (vectorsize < cellStrings.length) {
						 

					}
					;

				}

				return_value = -12;
				 		
				data.setRow( cellStrings ); 
				 
				id = 0;
				return_value = -14;
				 
			 
				record_counter = record_counter + 1;
				if ((maxRecordCount > 0) && (record_counter >= maxRecordCount - 1)) {
					out.diagnosticMsg.add("reading data interrupted at pre-defined point: record_counter >= maxRecordCount : "+record_counter) ;
					break;
				}
				
			} // while not eofile

			return_value = -20;
			  
		 
			return_value = 0;
			
				n = data.colcount() ;
				k = data.rowcount() ; // includes the header row 
				

				k=0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return_value = -37;
		} catch (IOException e) {
			e.printStackTrace();
			return_value = -38;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return_value = -39;
			}
		}
		return  ;
	}

	
	/**
	 * 
	 *  for simulating surrogated data that match these profiles such
	 *  that the SOM trained with it will be able to recognize these profiles
	 * 
	 *  this will be called from the outside as well as from the inside, since those profiles are "idealizations"
	 *  -> adding some noise to make robust models
	 *
	 */
	public int readProfileDefinitions( String _filename,
            						   boolean simulateData ){
		
		SomFluidProperties sfprops;
		
		try{
			
			
			sfprops = sfFactory.getSfProperties() ;
			
			profilesTransformer = new SomTransformer( this, sfprops );
			 
			DataReceptor dataReceptor = new DataReceptor(  this );
			
			// establishes a "DataTable" from a physical source
			dataReceptor.loadProfilesFromFile( _filename );
		
			// imports the DataTable into the SomDataObject, and uses a SomTransformer instance 
			// in order to provide a basic numeric version of the data by calling SomTransformer.basicTransformToNumericalFormat()
			this.importProfilesTable( dataReceptor, 1 ); 
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return -1;
	}
	
	 
	// ------------------------------------------------------------------------
	
	
	public void importProfilesTable(DataReceptor dataReceptor , int mode) {
		 
		TableImportSettings importSettings = new TableImportSettings() ;
		
		profilesTable.importTable( dataReceptor.getDataTable(), importSettings);
		 
	}
	
	// ------------------------------------------------------------------------

	public static SomDataObject loadSomData( SomFluidProperties sfProperties ) throws Exception{
		
		// DataHandlingPropertiesIntf datahandleProps = sfProperties;
		// 
		SomDataObject sdo=null;
		
		
		// load parts...
		
		String  filepath , vstr="", filename = "";
		PersistenceSettings ps;
		 
		FileOrganizer fileorg = new FileOrganizer(); 
											fileorg.getOut().print(2, "") ;
		fileorg.setPropertiesBase( sfProperties );
		DFutils fileutil = fileorg.getFileutil();
		 
		ps = sfProperties.getPersistenceSettings() ;
		 
		filename = ps.getProjectName()+"-dataobj" + vstr + fileorg.getFileExtension( FileOrganizer._DATAOBJECT ) ;
		filepath = fileutil.createpath( fileorg.getObjectStoreDir("") , filename);
		
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		if (DFutils.fileExists(filepath)==false){
			throw(new Exception("File for stored object not found!"));
		}
		Object obj = storageDevice.loadStoredObject( filepath) ;
		
		if (obj==null){
			throw(new Exception("loading stored object failed!"));
		}
		
		sdo = (SomDataObject)obj ;
		sdo.data.setSomData(sdo);
		sdo.normalizedSomData.setSomData(sdo) ;
											fileorg.getOut().print(2, "") ;
		sdo.data.establishObjects(sdo, true);
		sdo.normalizedSomData.establishObjects(sdo, true);
		
		DFutils.reduceFileFolderList( fileorg.getObjectStoreDir(""),1,20) ;
		
		fileorg=null;
		
		return sdo;
	}
	
	
	public void ensureTransformationsPersistence( int enforceSaveWrite) {
		
		boolean hb;
		// if not saved ...
		
		hb = enforceSaveWrite>=1;
		
		if (hb==false){
			hb = openChanges ;
		}
		
		try{
			
	
			if (hb){
				// transformer.save() ;
				
				transformer.saveXml();
				
				saveSomDataTables();
				
				openChanges = false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}


	@Override
	public int save() {
		
		int result=-1;
		String  filepath , vstr="", filename = "";
		PersistenceSettings ps;
		 
		FileOrganizer fileorg = transformer.getFileorg() ;
		
		ps = fileorg.getPersistenceSettings() ;
		DFutils fileutil = fileorg.getFileutil();
		 
		 
		filename = ps.getProjectName()+"-dataobj" + vstr + fileorg.getFileExtension( FileOrganizer._DATAOBJECT ) ;
		filepath = fileutil.createpath( fileorg.getObjectStoreDir("") , filename);
		
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		fileorg.careForArchive( FileOrganizer._DATAOBJECT, filepath );
		
		storageDevice.storeObject( this, filepath) ; // using a single object is not possible, we have to split it by a dedicated procedure and a dedicated format
		// abc124 
		
		DFutils.reduceFileFolderList( fileorg.getObjectStoreDir(""),1,".sdo",20) ;
		
		if (fileutil.fileexists(filepath)==false){
			result=-3;
		}else{
			result =0;
		}
		return result;
	}


	@Override
	public void saveXml() {
		//
		
	}


	public void saveSomData(){
		
		// all in once
		
	}
	
	
	public void saveSomDataTables() throws Exception{
		
		int r=-1,ms=0;
		String[] str = new String[]{"completed","did NOT complete"};
		
		if ((data != null) && (data.getDataTable().size()>0)){
			r = data.save();  
		}
		if ((normalizedSomData != null) &&(normalizedSomData.getDataTable().size()>0)){
			r = normalizedSomData.save();
			
		}
		if (r==0)ms=0;
		if (r<0)ms=1;
		
		out.print(2, "saving data tables "+str[ms]+" successfully");
	}


	public void loadSomTransformer(){
		
	}

	public void loadSomTransformer( int mode){
		
	}
	public void loadSomTransformer( String filename){
		
	}
 

	public void saveSomTransformer(){
		
		String filename="";
		ContainerStorageDevice storageDevice;
		
		
		storageDevice = new ContainerStorageDevice();
		
		storageDevice.storeObject( transformer, filename);
		
	}

	public void saveSomTransformer( String name, String version){
		
		if (transformer!=null){
			transformer.save();
		}
		
		if (transformer!=null){
			profilesTransformer.save();
		}
		
	}
	public void calculateStatisticalDescription() {
		
		int dataformat ;
		DataTableCol column;
		
		int n=data.getColcount();
											out.print(2, "calculating statistical description for all numerical columns...");
											double f=10.0;
		
		for (int i=0;i<n;i++){
											out.printprc(2, i, n, (int)((double)n/f), "");
			column = data.getColumn(i);
			dataformat = column.getDataFormat() ;
		
			if (dataformat<=8){
				
				column.calculateBasicStatistics();
				
			}// not string, text date?
			if (dataformat>8){
				// we count the different strings, avg. string length
				
			}
			
		}// i->
											out.print(2, "calculating statistical descriptions done.");
	}


	public SomMapTable extractSimpleTable() {
		return extractSimpleTable( 1, -1, true) ;
	}
	public SomMapTable extractSimpleTable( double portion, int maxN, boolean disrespectBlacks) {
		
		int rc,cc, ix,cix = 0;
		String varLabel;
		double vv ;
		SomMapTable smt = new SomMapTable();
		ArrayList<String> smtVar = new ArrayList<String>();
		ArrayList<Integer> indexValues = new ArrayList<Integer>();
		ArrayList<Double> colValues = new ArrayList<Double>();
		
		try{
			

			rc = getNormalizedDataTable().getColumn(0).getCellValues().size() ;
			if (maxN>1){
				rc = (int) Math.min(maxN, rc*portion);
			}
			rc = Math.max(100, rc);
			if (rc>getNormalizedDataTable().getColumn(0).getCellValues().size()){
				rc=getNormalizedDataTable().getColumn(0).getCellValues().size();
			}
			cc = normalizedSomData.getColumnHeaders().size();
			ArrayList<String> targetedVariables = variables.getAllTargetedVariablesStr();
			
			for (int i=0;i<cc;i++){
				varLabel = normalizedSomData.getColumnHeaders().get(i) ;
				Variable v = variables.getItemByLabel(varLabel);
				if ((varLabel.contentEquals( variables.getActiveTargetVariableLabel()) ) || 
						( (variables.getBlacklistLabels().indexOf(varLabel)<0) && 
						  (variables.getAbsoluteFieldExclusions().indexOf(varLabel)<0) &&
						  ( targetedVariables.indexOf(varLabel)<0) &&
						  (v.isTVcandidate()==false) && (v.isIndexcandidate()==false)) 
					){  
					smtVar.add(varLabel);
				}
			}
			cc = smtVar.size() ;
			
			smt.values = new double[rc][cc] ;
			smt.variables = (String[]) ArrUtilities.changeArraystyle(smtVar) ;
			
			// create list of index values that determine the sample
			int tsz = getNormalizedDataTable().getColumn(0).getCellValues().size() ;
			double rr = (double)rc/(double)tsz;
			
			while (indexValues.size()<rc){
				double pp = Math.random();
				double sp = Math.random();
				if (sp<rr){
					ix = (int) (pp*tsz); 
					if (indexValues.indexOf(ix)<0){
						indexValues.add(ix) ;
					}
				}
				
			} // ->
			
			Collections.sort(indexValues) ;
			for (int c=0;c<cc;c++){
				// all columns
				String varlabel = smtVar.get(c);
				
				cix = getNormalizedDataTable().getColumnHeaders().indexOf(varlabel) ;
				colValues = getNormalizedDataTable().getColumn(cix).getCellValues() ;
				if (colValues.size()<indexValues.get(indexValues.size()-1)){
					// continue;
				}
				for (int i=0;i<indexValues.size();i++){
				// all rows
					ix = indexValues.get(i) ;
					vv=-1.0;
					if (ix<colValues.size()){
						vv = colValues.get(ix );
					}
					if ((i<smt.values.length) && (c<smt.values[i].length)){
						smt.values[i][c] = vv;
					}
					
				}// all columns
				
			}// i-> all rows 
			
			
			
		}catch(Exception e){
			rc = colValues.size() ;
			String str = "col: "+cix+", column size: "+ rc;
			out.printErr(2, "problem in extracting smt, "+str) ;
			e.printStackTrace();
			smt.values = new double[0][0] ;
		}
		
		
		return smt;
	}


	public void acquireInitialVariableSelection() {

		String bstr;
		int ix;
		ArrayList<Variable> blackvariables = variables.getBlackList() ;
		ArrayList<String> blacklabels = variables.getBlacklistLabels() ;
		
		ArrayList<String>  blacks = modelingSettings.getBlacklistedVariablesRequest();
		
		for (int i=0;i<blacks.size();i++){
			bstr = blacks.get(i) ;
			if (blacklabels.contains(bstr)==false){
				blacklabels.add(bstr) ;
				ix = variables.getIndexByLabel(bstr) ;
				if (blackvariables.contains(variables.getItem(ix))==false){
					blackvariables.add( variables.getItem(ix) ) ;
				}
			}
		}
		
		variables.setInitialUsageVector( modelingSettings.getInitialVariableSelection() ) ;
		
	}


	public void prepareTransformationsXML(boolean embed){
		
		transformer.extractTransformationsXML(embed);
	}

	
	public SomDataDescriptor getSomDataDescriptor() {
		// TODO Auto-generated method stub, obsolete? undefined....
		return null;
	}


	public DataTable getDataTable(){
		return data ;
	}

	public DataTable getNormalizedDataTable(){
		return normalizedSomData ;
	}
	
	/**
	 * 
	 * sourceType = 1 : default data
	 *            = 2 : normalized data
	 */
	@Override
	public ArrayList<Double> getRecordByIndex( long rIndex , int sourceType) {
		
		ArrayList<Double> recordData = new ArrayList<Double>(); 
		DataTable tdata;
		int rc;
		
		if (sourceType<=1){
			tdata = data ;
		}else{
			tdata = normalizedSomData;
		}
		if (tdata==null){
			return recordData;
		}
		
		rc= tdata.getRowcount() ;
		if ((rIndex<0) || (rIndex>rc)){
			return recordData;
		}
		
		try{
			
			recordData = tdata.getDataTableRow( (int)Math.round(1.0*rIndex)) ;
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return recordData;
	}


	public void importDataTable(DataReceptor datareceptor, int applyExtendedPreparations) {
		dataReceptor = datareceptor;
		
		openChanges = true ;
		
		importDataTable( getTransformer(), datareceptor.getDataTable(), applyExtendedPreparations ); 
		
	}


	public void importDataTable( SomTransformerIntf somTransformerIntf, DataTable datatable, int applyExtendedPreparations ){
		 
		
		if (datatable==null){
			return;
		}
		
		try{

			TableImportSettings importSettings = new TableImportSettings() ;
			 
				
			// TODO
			// check, whether there is a serialization of a dataTable which we derived from the raw file;
			// we use the info about filename, filesize, filedate as stored in the DataTable object itself;
			
			
			// the importTable applies some basic transformations that are implied 
			//     by the format of the columns, e.g. date and NVE (text) 
			// the original format and the if necessary also the parameters of the transformation are 
			//     saved into a description object within the DataTable object;
			// later, this will be used to create a persistent XML description file 
			//    (like the PTS transformer file), which is necessary for applying
			// the result is a purely numerical table, which however is not necessarily normalized.

			// it also checks whether there is a candidate column for an index, and, if
			// there is none, it will insert one as column 0
											out.print(2, "importDataTable() into SomDataObject...");
			data.importTable(datatable, importSettings);
			// == DataTable
											out.print(2, "importDataTable() into SomDataObject done.");
			vectorSize = variables.size() ;
			
			data.createRowOrientedTable() ; // not for raw data ???
			// TODO check against raw data whether string columns are correctly imported
			
			if (modelingSettings.getVariables()==null){
				modelingSettings.setVariables(variables);
			}
			
			// creating variables objects, setting info about raw data format
			// translating wildcards into full names to sets black,treat,group, set treatment, group to blacklist
			actualizeVariables();
			vectorSize = variables.size() ;
			
			//VariableSettingsHandlerIntf vsi;
			 
			variables.setVariableSettings( sfProperties.getVariableSettings() ); 
			// --- transforming data ------------------------------------------
			
			// this creates a clone of the DataTable !
 			somTransformerIntf.setDataTable(data) ;
			
 			// no transformations are applied here, just Transform.Stacks initialized, MV+StdStats prepared, 
 			// but no LinNorm ...  no NVE 
											int outlevel=3;
											if (data.colcount()* data.getDataTable().size()>200000){outlevel=2; }
											out.print(outlevel, "initializing the Transformation Model..."); // msg for large data !!;
 			
 			somTransformerIntf.initializeTransformationModel();
 			                                out.print(outlevel, "going to transform data into numerical form..."); // msg for large data !!;
 			                                  
 			somTransformerIntf.basicTransformToNumericalFormat(); 
 			//
 			
 			if (somTransformerIntf instanceof SomAppTransformer){
 				return;
 			}
			// like the SomSprite, just on raw variables, but based on samples of max 1000 values
 			// transformer.applyAprioriLinkChecking();
			
											out.print(2, "importDataTable(), normalizing data...");
			// normalizing data: only now the data are usable
			// note that index columns and string columns need to be excluded
			// which we can do via the format[] value : use only 1<= f <= 7, exclude otherwise
					out.delay(400);
					
					
			// normalizedSomData = transformer.normalizeData(variables);
					
			// usually, normalizing data is ON by default;
			if (normalizeData){

				somTransformerIntf.normalizeData(); // just adding everywhere LinNorm, caring for output data
					    					out.delay(400);
				    // creating the usable table as an instance of DataTable
				    // creates columns by copying out-data from transformation stack into columns (list of columns = of DataTable)
					    					
			}

			normalizedSomData = somTransformerIntf.writeNormalizedData() ; 
			normalizedSomData.setName("normalized table");

			// where is the request for this ?
			if (applyExtendedPreparations>1){ // should be actually ">0"
			// shifting distributions (kurtosis, skewness), 
 			// splitting (deciling) variables based on histogram splines, outlier compression, NVE (thus quite expensive) 
 			// this also extends the basic transformer model
 			// will draw a copy from numerical columns and add a LinNorm at the end if necessary
			
											out.print(2, "importDataTable(), applying advanced apriori transformations...");
				somTransformerIntf.applyAdvNumericalTransforms(null);

				/*
				//again writing the table containing the normalized data
				normalizedSomData = transformer.writeNormalizedData() ; 
				normalizedSomData.setName("normalized table");
				*/
				
			}
			
			somTransformerIntf.createDataDescriptions();
			
			 
			out.setDisplayMemory(true); 
			out.print(2, "clearing memory ...");
			
			// clearDataFromTransformStack();
			
			// data.getDataTable().clear();
			// data.getDataTableRows().clear() ;
			System.gc();out.delay(200);
			out.print(2, "clearing memory done.");
			
			// unloading raw data into a tmp binary... due to problems with heap space
			// TODO always care for heap settings, especially in production jar!
			
			// OutOfMemoryError for large tables...
			normalizedSomData.createRowOrientedTable( ) ;
			
			
			
			normalizedSomData.createIndexValueMap();
			
			prepareClassificationSettings();
			
			// TODO: saving both the standard imported table, the transformed table and the transformer model
			//       into a dedicated directory... 
			//       this could be change to a zip/jar with add meta information, and the raw data
			
			out.print(2, "importing data to transformer and transforming data completed.");
			
			out.setDisplayMemory(false);			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void extendDataTable( SomTransformerIntf somTransformerIntf, DataTable datatable, int applyExtendedPreparations ){
		 
		
		if (datatable==null){
			return;
		}

		somTransformerIntf.basicTransformToNumericalFormat(streamingRowOffset); 
	}
	
	
	public void clearDataFromTransformStack() {
		 
		TransformationModel tm = transformer.getTransformationModel();
		tm.clearData();
	}


	public void introduceBlackList() {
		ArrayList<String>  blacklist;
		 
		String varLabel;
		
		blacklist = modelingSettings.getBlacklistedVariablesRequest() ;
		
		
		for (int i=0;i<blacklist.size();i++){
			varLabel = blacklist.get(i) ;
			variables.addBlacklistLabel(varLabel) ;
		}
	}


	/** prepares a transposed table for fast access  */
	public void prepareTransposedTable() {
		
		try{
		
			normalizedSomData.createTransposedForm();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	/** 
	 * we have to accomplish several tasks, dependent on target mode
	 * 
	 * SINGLE
	 * -assign a label to the the target group, if provided by the user
	 * 
	 * MULTI
	 * - infer the mapping of raw data values to target groups,
	 * - infer the separation between groups (for node labeling) 
	 * 
	 * 
	 */
	public void prepareClassificationSettings(){
		
		if ( modelingSettings.getTargetedModeling() == false ){
			return;
		}
		
		// cs = sfProperties.getModelingSettings().getClassifySettings() ; 
		// cs.setTargetMode(ClassificationSettings._TARGETMODE_SINGLE) ;
		
		
	}
	
	
	public void inferTargetGroups( ModelingSettings modset ) {

		SomVariableHandling variableHandling;
		
		// String tvLabel = modset.getActiveTvLabel() ;
		
		variableHandling = new SomVariableHandling( this, modset );
		
		variableHandling.getEmpiricTargetGroups(  true ); // flag : enforceRecalc
		  
		
		double[][] tgdefinition = variableHandling.getTargetGroups();
		classifySettings.setTGdefinition(tgdefinition);
		
	}
	// ------------------------------------------------------------------------
	



	// ........................................................................
	
	/**
	 * translating wildcards into full names to sets black,treat,group, set treatment, group to blacklist
	 *  
	 * TODO: also check whether format info is transferred ....
	 * 
	 */
	private void actualizeVariables() throws Exception{
		
		Variable v;
		DataTableCol  column ;
		int _format;
		
		boolean  idcolFound=false ;
		
		if (modelingSettings==null){
			throw(new Exception("settings object (modeling) is null"));
		}
		String tvstr = modelingSettings.getActiveTvLabel();
		
		// if there is then only 1 we set this one as active...
		 
		
		int nh = data.getColumnHeaders().size();
		
		variables.clear(0) ; 
		
		for (int i=0;i<nh;i++){
			column = data.getColumn(i);
			
			v = new Variable();
			v.setLabel( data.getColumnHeader(i)) ;
			
			_format = column.getDataFormat() ;
			v.setRawFormat(_format) ;
			
			if ((column.getDataFormat()==0) || (column.isIndexColumnCandidate())){
				v.setID(true);
				idcolFound=true;
			}
			
			v.setIndexcandidate( column.isIndexColumnCandidate()) ;
			
			v.setIndex(i);
			if ((v.getLabel().contains("_TV")) || 
				(v.getLabel().startsWith("TV")) || 
				(v.getLabel().endsWith("TV"))){
				v.setTV(true);
				
				if (tvstr.contains("*")){
					modelingSettings.setActiveTvLabel( v.getLabel() ) ;
				}
			}
			v.setVariableSerialID(i);
			
			
			variables.additem(v);
		}
		
		if (idcolFound==false){
			// is there some org id?
		}
		
		if (idcolFound==false){
			// set a global flag to add an ID col
			
		}
		
	}


	public void determineActiveVariables() {

		activeVariables = variables;
		// TODO obviously stub...
	}


	public Variables getActiveVariables() {
		 
		if (activeVariables==null){
			activeVariables = variables;
		}
		return activeVariables;
	}
 

	// ........................................................................
	
	public MissingValues getMissingValues() {
		return missingValues;
	}


	public int getMaxColumnCount() {
		return maxColumnCount;
	}


	public void setMaxColumnCount(int maxColumnCount) {
		this.maxColumnCount = maxColumnCount;
	}


	public int getMaxRecordCount() {
		return maxRecordCount;
	}


	public void setMaxRecordCount(int maxRecordCount) {
		this.maxRecordCount = maxRecordCount;
	}


	public boolean isDataAvailable() {
		return dataAvailable;
	}


	public void setDataAvailable(boolean dataAvailable) {
		this.dataAvailable = dataAvailable;
	}


	public PrintLog getOut() {
		return out;
	}


	public void setOut(PrintLog out) {
		this.out = out;
		out.setPrefix("[SomFluid-main]") ;
		out.setShowTimeStamp(true);
	}


	public void setIndex( int indexval){
		dobjsIndex = indexval ;
	}

	public ArrayList<Variable> getVariableItemsReference() {
		return variables.getActiveVariablesReference() ;
	}
	
	public ArrayList<Variable> getVariableItems() {
		return variables.getActiveVariables() ;
	}
	
	public Variable getVariable(String label) {
		Variable variable = null;
		String str ="";
		
		for (int i=0;i<variables.size();i++){
			
			str = variables.getItem(i).getLabel() ;
			
			if ((str.length()>0) && (str.contentEquals(label) )){
				variable = variables.getItem(i) ; 
				break ;
			}
		}
		
		return variable;
	}


	public Variables getVariables() {
		
		int nh = this.data.getHeadersCount(); //  getColumnHeaders()
		
		if (variables.size()< nh){
			
		}
		return variables;
	}
 
	

	public ArrayList<String> getVariablesLabels() {
		
		ArrayList<String> strings ;
		int i,k ;
		String str ;
		
		if (variableLabels != null){
			
			return variableLabels ;
		}
		
		
		k = variables.size();
		strings = new ArrayList<String>();
		
		
		for (i=0;i<k;i++){
			str = variables.getItem(i).getLabel() ;
			strings.add(str) ;
		}
		
		variableLabels = strings;
		return strings;
	}
	
	
	public int getRecordSize(){
		if (vectorSize <= 0){
			vectorSize = data.getHeadersCount();
		}
		return vectorSize;
	}
	
	public int getRecordCount(){
		return data.rowcount();
	}

	public int getColumnCount(){
		return getRecordSize();
	}


	/**
	 * this comes from  ExtensionalityDynamics, which maintains a list of index values
	 * that are being collected by the node (MetaNode in VirtualLattice)
	 *   
	 * @param extensionality
	 * @param result
	 */
	public void nodeChangeEvent( ExtensionalityDynamicsIntf extensionality, long result) {
		//  
		// for registration and handling : fork immediately into container !!
		long nodeID, uuid;
		
		uuid  = extensionality.getNodeNumGuid();
		nodeID = extensionality.getNodeSerial();
		
		/* it is also sent to implementations of SomProcessIntf :
		 * 	  - SomTargetedModeling
		 *    - SomAstor
		 */
	}


	public void activateNormalizationOfInData(boolean normalizedata) {
		//
		normalizeData = normalizedata ;
	}


	public DataTable getData() {
		return data;
	}


	public void setData(DataTable data) {
		this.data = data;
	}


	public DataTable getNormalizedSomData() {
		return normalizedSomData;
	}


	public void setNormalizedSomData(DataTable normalizedSomData) {
		this.normalizedSomData = normalizedSomData;
	}


	public DataTable getProfilesTable() {
		return profilesTable;
	}


	public void setProfilesTable(DataTable profilesTable) {
		this.profilesTable = profilesTable;
	}


	public boolean isOpenChanges() {
		return openChanges;
	}


	public void setOpenChanges(boolean openChanges) {
		this.openChanges = openChanges;
	}


	/**
	 * @return the transformer
	 */
	public SomTransformerIntf getTransformer() {
		return transformer; // SomTransformer@124614c
	}


	/**
	 * @param transformer2 the transformer to set
	 */
	public void setTransformer(SomTransformerIntf transformer ) {
		this.transformer = transformer ;
		
	}


	/**
	 * @return the dataReceptor
	 */
	public DataFileReceptorIntf getDataReceptor() {
		return dataReceptor;
	}


	/**
	 * @param dataReceptor the dataReceptor to set
	 */
	public void setDataReceptor(DataFileReceptorIntf datareceptor) {
		this.dataReceptor = datareceptor;
	}


	public TexxDataBaseSettingsIntf getDatabaseSettings() {
		return databaseSettings;
	}


	public boolean isDatabaseConnection() {
		return databaseConnection;
	}
	public boolean getDatabaseConnection() {
		return databaseConnection;
	}


	public void setDatabaseSettings(TexxDataBaseSettingsIntf texxDbSettings ) {
		this.databaseSettings = texxDbSettings;
	}


	public DataBaseAccessDefinition getDbAccessDefinition() {
		return dbAccessDefinition;
	}


	public void setDbAccessDefinition(DataBaseAccessDefinition dbAccessDefinition) {
		this.dbAccessDefinition = dbAccessDefinition;
	}


	public SomDataStreamer getSomDataStreamer() {
		return somDataStreamer;
	}


	public void setSomDataStreamer(SomDataStreamer somDataStreamer) {
		this.somDataStreamer = somDataStreamer;
	}


	// TODO: for this return we need an abstracting container... 
	public SomTexxDataBase getSomTexxDb() {
		return somTexxDb;
	}
	
	public AstorDocSomDataBase getSomAstorDb() {
		return somAstorDb;
	}


	public Variable addDerivedVariable( int newIndex, 
										String varLabel,
										String newVarLabel ,
										String parentTransformID ) {

		Variable v, newVariable;
		int ix,formind;
		
		
		ix = variables.getIndexByLabel(varLabel) ;
		v = variables.getItem(ix);
		
		newVariable  = new Variable(v) ;
		newVariable.setLabel( newVarLabel ) ;
		newVariable.setDerived( true ) ;
		newVariable.setParentTransformID( parentTransformID ) ;
		newVariable.setIndex(newIndex) ;
		
		formind = newVariable.getRawFormat() ;
		
		if ((formind == DataTable.__FORMAT_ID) || (formind == DataTable.__FORMAT_ORGINT)){
			newVariable.setRawFormat( DataTable.__FORMAT_NUM ) ;
		}
		variables.additem(newVariable) ;
		
		return newVariable;
	}


	public ArrayList<String> getVariableLabels() {
		return variableLabels;
	}


	public void setVariableLabels(ArrayList<String> variableLabels) {
		this.variableLabels = variableLabels;
	}


	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}


	public ClassificationSettings getClassifySettings() {
		return classifySettings;
	}


	public void setClassifySettings(ClassificationSettings classifySettings) {
		this.classifySettings = classifySettings;
	}


	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}


	public void setModelingSettings(ModelingSettings modelingSettings) {
		this.modelingSettings = modelingSettings;
	}


	public int getVectorSize() {
		return vectorSize;
	}


	public void setVectorSize(int vectorSize) {
		this.vectorSize = vectorSize;
	}


	public void setVariables(Variables variables) {
		this.variables = variables;
	}


	public void setActiveVariables(Variables activeVariables) {
		this.activeVariables = activeVariables;
	}


	public void setMissingValues(MissingValues missingValues) {
		this.missingValues = missingValues;
	}


	/**
	 * this method establishes the data linkage to the stream provider 
	 * 
	 */
	public void establishDataLinkage() {
		// 
		
	}

	public void updateVariableLabels() {
		
		this.variableLabels = new ArrayList<String>( variables.getLabelsForVariablesList(variables) );
	}


	public void setStreamingRowOffset(int offset) {

		streamingRowOffset = offset ;
	}





	
	
}




/*


/**
 *  for simulating surrogated data that match these profiles such
 *  that the trained SOM is able to recognize these profiles
 *   
 * 
public int readProfileDefinitions( String _filename,
                                   boolean create_new_SOM,
                                   boolean simulateData ){
	

	int return_value = -1, L0, L1, L2, rawprofileCount;
	String[] MAelements;
	String  hs1, _tmp_str, _profile_Label, _file_name ;
	String text = null;
	double[][] tmp_table = null, _pTable ;
	int record_counter = 0,  eL, i,r,c, err;
	String[] arr, columnHeaders;
	String columnsep = "\t";
	String errmsg="";

	err = 0;
	
	if (SP==null){
		initializeSOM();
		
	}
	else{
		clearcontentSOM();
	}

	
	err = 1;
	
	SD = SP.SOMobject.SOMdata ; // ???????
	
	_filename = _filename.trim(); // e.g.  D:/dev/processing/SOMLib_Tutorial01_1/definitions/TopProfiles.txt
	
	if (show_status_messages){
		System.out.println("File containing profile definitions is about to be loaded... \n"+
						   ""+_filename);
	}
	try {
		err = 2;
		
		File file = new File(_filename);
		BufferedReader reader = null;
		
		err = 3;
		reader = new BufferedReader(new FileReader(file));
		
		// eL = utils.
		
		// we check the number of columns, first column contains labels
		err = 4;
		text = utils.getnextline_from_filereader(reader);
		
		reader.close();
		reader = new BufferedReader(new FileReader(file));
		
		err = 5;
		L0 = text.length();
		_tmp_str = text;
		_tmp_str.replace(";", "");
		L1 = _tmp_str.length();
		_tmp_str = text;
		_tmp_str.replace(columnsep, "");
		L2 = _tmp_str.length();
		
		if ((L0 == L2) && (L1 > 0) && (L1 < L0)) {
			columnsep = ";";
		}
		
		err = 6;
		arr = text.trim().split(columnsep);
		eL = arr.length ;
		
		if ((arr[0].contentEquals("PLabel")==true) ||
			(arr[0].contentEquals("Label")==true)){
			arr[0]="";
			eL = arr.length -1;
			arr = utils.resizeArray(arr.length -1, arr, 1) ;
			
		}
		
		
		
		 
		err = 7;
		columnHeaders = new String[eL];
		for (i=0;i<eL;i++){
			columnHeaders[i] = arr[i];
		}
		

		rawprofileCount=0;
		while ((text = reader.readLine()) != null) {
			
			if ( (utils.isFileComment(text)==false) &&
				 (text.contentEquals(text.trim())==true) &&
				 (text.contains(columnHeaders[0])==false) && 
				 (text.contains(columnHeaders[1])==false)){
				rawprofileCount=rawprofileCount+1;
			}
		}
		
		
		err = 8;	
		reader.close();
		reader = new BufferedReader(new FileReader(file));
		
		record_counter = 0;
		
		
		return_value = -2;
		
		eL = -1;
		MAelements = null;
		
		
		err = 10;
		while ((text = reader.readLine()) != null) {
			return_value = -3;
			
			if (utils.isFileComment(text) == true) {
				continue;
			}
			
			arr = text.split(columnsep);
			
			
			// if first col is empty, we have met the header row
			if ((arr[0].length()==0) || (arr[0].contentEquals("PLabel"))){
				continue;
				
			}
			
			if (eL<0){
				eL = arr.length - 1;
				MAelements = new String[rawprofileCount];  // eL
				
				if (columnHeaders.length != eL){ 
					if (columnHeaders.length < eL){ eL=columnHeaders.length; };
					if (columnHeaders.length > eL){ utils.resizeArray(eL,columnHeaders); };
				}
				tmp_table = new double[rawprofileCount][eL];
			    // this holds the profiles for top-down defined SOMs
			    SP.SOMobject.SOMprofiles = new SOM_profiles(rawprofileCount,eL);

			}
			
			// 	first col is the label of the profile
				_profile_Label = arr[0]; 
				
			
			// further columns contain values
			
			if (eL != arr.length) {
				// this should NOT throw an error !!!
				
			}
			

			if (record_counter < rawprofileCount) {
				MAelements[record_counter] = arr[0];
			}
			
			for (int j = 1; j < eL + 1; j++) {
				_tmp_str = arr[j];
				_tmp_str = _tmp_str.replace(",", ".").trim();
				
				if (tmp_table==null){ return -5;}
				
				_tmp_str = _tmp_str.trim() ;
				
				if (utils.isnumeric( _tmp_str)==true){
					tmp_table[record_counter][j - 1] = Double.parseDouble(_tmp_str);
				}
				else{
					break ;
				}
			}
			
			
			SP.SOMobject.SOMprofiles.profileLabels[record_counter]=_profile_Label ;
			
			record_counter = record_counter + 1;
			if (record_counter >= rawprofileCount) {
				break;
			}
			
		} // while not eofile
		
		return_value = -6;
		err = 20;


		_pTable = SP.SOMobject.SOMprofiles.profilesTable ;
			
		for (r=0;r<rawprofileCount;r++){
			for (c=0;c<eL;c++){
				_pTable[r][c] = tmp_table[r][c];
			}
		}
		for (i=0;i<columnHeaders.length;i++){
			SP.SOMobject.SOMprofiles.profileVariables[i]= columnHeaders[i]  ;
		}
		
		// 
		return_value = 0;
		err = 21;
		
		SD = SP.SOMobject.SOMdata ;
		
		if (simulateData==true){
			err = 22;
			//  right now we also have to create the simulated data
			//  the simulated data will be in the SOMdata object
			r = SP.SOMobject.simulateDatafromProfiles( SP.SOMobject.SOMprofiles,
			                                           "simprofiles.txt",
			                                           sizeOverrideValue,
			                                           scaleTableby);
										     // if there is a filename, we just save it!
			tablerecordcount = SP.SOMobject.getSimulatedRecordCount(); 
			SD.sourcefile = SP.SOMobject.simulatedFile ;
			
		}
		else{
			err = 23;
			// simply read file
			SP.SOMobject.prepareSimulation( SP.SOMobject.SOMprofiles ) ;
			
			SP.SOMobject.SOMdata._SOM_object = SP.SOMobject ;
			_file_name = utils.prepareFilepath("simprofiles.txt",1); 
			SD.sourcefile = _file_name; 
			SP.SOMobject.SOMdata.sourcefile = _file_name;
			SP.SOMobject.simulatedFile= _file_name;  
			
			SP.SOMobject.SOMdata.readData(_file_name,0);
			
			tablerecordcount = SP.SOMobject.getTableRecordCount();
		}
		
		
		err = 25;

		hs1 = SP.SOMobject.simulatedFile ;
		
		
		
		
		if (hs1==null){
			r=-3; 
		}
		else{
			if (hs1.length()==0){r=-3;}
		}
		
		if (r==0){
			int labelcolumn=-1, encodedNumColumn=-1;
			
			SD = SP.SOMobject.SOMdata ; // ???????
			
			SD.TVLabelcolumn = SP.SOMobject.SOMdata.TVLabelcolumn;
			SD.TVcolumn = SP.SOMobject.SOMdata.TVcolumn  ;
			
			labelcolumn = SD.TVLabelcolumn ;
			
			encodedNumColumn = SD.TVcolumn ;
			SP.SOMobject.SOMdata.adaptTargetValues(labelcolumn, encodedNumColumn) ;
		
		}
		return_value = r;

		err = 30;
		
		
	} // try
	catch (FileNotFoundException e) {
		errmsg = e.getMessage()+"\n"+ e.getStackTrace();
		
		if (err==3){
			errmsg = "file not found : "+_filename+ " \n"+
					 errmsg;
		}

		
		System.out.println("\nerrorcode "+err+"\n"+errmsg);
		
		return_value = -3;
	}
	catch (IOException e) {
		e.printStackTrace();
		return_value = -5;
	}
	catch (Exception e) {
		e.printStackTrace();
		return_value = -7;
	}
	finally {
		
	}
	
	return return_value;
			

}
*/

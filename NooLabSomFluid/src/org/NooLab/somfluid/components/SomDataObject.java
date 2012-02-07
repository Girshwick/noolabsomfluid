package org.NooLab.somfluid.components;

import java.io.*; 
import java.util.*;



 
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;

import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.TableImportSettings;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.FileDataSource;
import org.NooLab.utilities.files.WriteFileSimple;
import org.NooLab.utilities.logging.PrintLog;
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
 * TODO:  prepare the interface DataSourceIntf !
 */
public class SomDataObject 	implements  
										//	used for read access, e.g. by nodes
											DataSourceIntf {

	transient DataHandlingPropertiesIntf dataHandlingProperties;
	transient SomFluidFactory sfFactory;
	
	// object references ..............
	transient FileDataSource filesource;
	transient XmlFileRead xmlFile ;
	
	// main variables / properties ....
	
	DataTable data=null, normalizedSomData=null ;
	
	Variables variables = new Variables() ;
	Variables activeVariables;

	ArrayList<String> variableLabels = null; 

	
	MissingValues missingValues;

	int maxColumnCount ;
	int maxRecordCount = -1 ;
	
	boolean dataAvailable=false ;
	
	// read mode = random, serial, block (begin, end) ?
	
	// volatile variables .............
	int dobjsIndex ; // == an identifier in the vector of SomDataObjects, maintained by Spela 
	
	int vectorSize;
	


	// helper objects .................
	transient StringsUtil strgutil = new StringsUtil();
	transient DFutils fileutil = new DFutils () ; 
	transient ArrUtilities utils = new ArrUtilities();
	
	transient PrintLog out  ;
		
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 

	public SomDataObject( DataHandlingPropertiesIntf datahandleProps){
		
		dataHandlingProperties = datahandleProps;
		
		missingValues = new MissingValues(this);
		data = new DataTable( this, true ); // true: isnumeric, Som data objects always contain numeric data
		
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	 
	 
	public void setFactory(SomFluidFactory factory) {
		sfFactory = factory;
	}


	public void prepare() {
	
		String filename="";
		 
		if ((dataHandlingProperties.getDataUptakeControl()>=0)){
			// load data into SomDataObject
			
			filename = dataHandlingProperties.getDataSrcFilename() ;
			if (fileutil.fileexists(filename)==false){
				return;
			}
			
			
			// now the SomDataObject has a table loaded
			// only for XML outut from transformer,etc... readData(filename);
			
		} // getDataUptakeControl >=0 ?
		
		
		
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
	public void readPreparedData( String filename, boolean fillrawdatatable) {
		
		String datasectionContent ,nodelabel, taglabel, temp_dir;
		WriteFileSimple txtfile ;
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
			          
			txtfile = new WriteFileSimple(filename, datasectionContent);
			
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

			variables.clear() ;
			
			
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


	// ------------------------------------------------------------------------
	
	public DataTable getDataTable(){
		return data ;
	}
	
	public void importDataTable( DataTable datatable ){
		SomTransformer  transformer;
		
		if (datatable==null){
			return;
		}
		

		try{

			TableImportSettings importSettings = new TableImportSettings() ;
			transformer = sfFactory.getTransformer();
				
			// TODO
			// check, whether there is a serialization of a dataTable which we derived from the raw file;
			// we use the info about filename, filesize, filedate as stored in the DataTable object itself;
			
			
			// the importTable applies some basic transformations that are implied 
			//     by the format of the columns, e.g. date and NVE (text) 
			// the original format and the if necessary also the params of the transformation are 
			//     saved into a description object within the DataTable object;
			// later, this wil be used to create a persistent XML description file 
			//    (like the PTS transformer file), which is necessary for applying
			// the result is a purely numerical table, which however is not necessarily normalized.

			// it also checks wether ther eis a candidate column for an index, and, if
			// there is none, it will insert one as column 0
			data.importTable(datatable, importSettings);
			
			// TODO check here for buffered transformed data
			
			// creating variables objects
			actualizeVariables();

			// --- transforming data ------------------------------------------
 			transformer.setDataTable(data) ;
			
			// shifting distributions (kurtosis, skewness), 
 			// splitting (deciling) variables based on histogram splines, outlier compression,
			transformer.applyBasicNumericalAdjustments();

			// like the SomSprite, just on raw variables , but based on samples of max 1000 values
			transformer.applyAprioriLinkChecking();
			
			// normalizing data: only now the data are usable
			// note that index columns and string columns need to be excluded
			// which we can do via the format[] value : use onls 1<= f <= 7, exclude otherwise
			normalizedSomData = transformer.normalizeData();
			
			normalizedSomData.createIndexValueMap();
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/** prepares a transposed table for fast access  */
	public void prepareTransposedTable() {
		
		DataTable table;
		
		try{
		
			normalizedSomData.createTransposedForm();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	// ------------------------------------------------------------------------
	
	public void determineActiveVariables() {

		activeVariables = variables;
		// TODO obviously stub...
	}


	public Variables getActiveVariables() {
		 
		return activeVariables;
	}


	
	private void actualizeVariables() {
		Variables vs;
		Variable v;
		DataTableCol  column ;
		
		int nh = data.getColumnHeaders().size();
		
		variables.clear() ; 
		
		for (int i=0;i<nh;i++){
			column = data.getColumn(i);
			
			v = new Variable();
			v.setLabel( data.getColumnHeader(i)) ;
			if ((column.getDataFormat()==0) || (column.isIndexColumnCandidate())){
				v.setID(true);
			}
			
			v.setIndexcandidate( column.isIndexColumnCandidate()) ;
			
			v.setIndex(i);
			if ((v.getLabel().contains("_TV")) || 
				(v.getLabel().startsWith("TV")) || 
				(v.getLabel().endsWith("TV"))){
				v.setTV(true);
			}
			variables.additem(v);
		}
		
	}


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
	}


	public ArrayList<Variable> getVariableItems() {
		return variables.getActiveVariables() ;
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
	
	
	public int getRecordSize(){
		if (vectorSize <= 0){
			vectorSize = data.getHeadersCount();
		}
		return vectorSize;
	}
	
	public int getRecordCount(){
		return data.rowcount();
	}
	
	public void setIndex( int indexval){
		dobjsIndex = indexval ;
	}


	
	
}

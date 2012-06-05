package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

 
import org.NooLab.somfluid.components.MissingValues;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.storage.PersistentAgentSerializableIntf;
import org.NooLab.somfluid.util.Formula;
import org.NooLab.somtransform.StackedTransformation;
import org.NooLab.somtransform.TransformationStack;
import org.NooLab.somtransform.algo.NomValEnum;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.callback.ProcessFeedBackContainer;
import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.callback.ProcessFeedBackIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;



 


 
/**
 * 
 *  The DataTable is the base class for representing tabular data in SomFluid
 *  
 *  
 *  note, that columns do NOT contain headers!
 *  
 *  
 *  TODO / open
 *  
 *  - ColumnDerivations in DataTableCol is not perfectly clones so far
 *  - DataTableCol cloning and importColumn are incomplete
 * 
 * 
 */
public class DataTable implements Serializable, PersistentAgentSerializableIntf{

	private static final long serialVersionUID = 3655282650007767562L;

	public static final String __MV_TEXTUAL   = "-M.V.";
	
	public static final double __MV_RAW       = -999999.090901;
	public static final double __MV_TRANSFORM = -1.0;
	
	public static final int __FORMAT_ID     = 0;
	
	public static final int __FORMAT_NUM    = 1;
	public static final int __FORMAT_INT    = 2;
	public static final int __FORMAT_ORD    = 3;
	public static final int __FORMAT_ORGINT = 4;
	public static final int __FORMAT_BIN    = 5; //   it is basically a value or a string
	
	public static final int __FORMAT_TIME   = 6; //   it is basically a string
	public static final int __FORMAT_DATE   = 7; //     ./.
	public static final int __FORMAT_DATETIME = 8;//    ./.  
	
	public static final int __FORMAT_ORDSTR = 10;
	public static final int __FORMAT_STR    = 11;
	public static final int __FORMAT_TXT    = 12;
	public static final int __FORMAT_BINSTR = 13;

	public static final int __FORMAT_IGNORE = 99;
	
	public static final int _MAX_ORDSTR_VARIABILITY = 50 ;

	
	// not avail: ExecSettings (config for diagnostic printouts etc.) settings ;
	// =================================
	
	// object references ..............

	SomDataObject somData;
	
	transient DataTable dt;

	private transient String tablename="";
	// main variables / properties ....
	
	// our table consists of a list of columns
	ArrayList<DataTableCol> dataTable = new ArrayList<DataTableCol>() ; 
	
	// the transposed table is always a numeric table, where previous column headers are replaced by enumeration 
	ArrayList<DataTableCol> transposedTable = new ArrayList<DataTableCol>() ;
	
	ArrayList< ArrayList<Double> > dataTableRows = new ArrayList< ArrayList<Double>>() ;
	
	ArrayList<String> columnHeaders = new ArrayList<String>() ; 
	
	int[] formats ;         
	
	private Map<Double,Integer> indexValueMap = new TreeMap<Double,Integer>();
										// consider Apache's TreeBidiMap ...
	
	int colcount;
	int rowcount;
	
	int[] columnTypes ;
	
	boolean mvActivated ;
	double mvIndicator = -1 ;
	
	ArrayList<Integer> mvCountperColumn = new ArrayList<Integer>();
	
	/**  if isNumeric, all data are transformed into values upon import of data, 
	 *   incl. NominalValuesEnumeration <br/>
	 *   if not numeric, then all data are hold as String */
	boolean isNumeric ;
	int maxScanRows = 500 ;
	
	// NVE : config: max number of groups
	int maxNveGroupCount = 32 ; // the original is DataTransformationSettings
	
	ArrayList<Integer> derivedColumns = new ArrayList<Integer>();
	
	MissingValues missingValues;
	
	/**
	 * 1=adjust colcount of table to count of header elements
	 * 2=adjust colcount to count of value elements
	 * 3=adjust colcount to max of {header, value}-elements
	 */
	int colcountControlMode = 3; 
	int colcountLimited = 0 ;
	
	boolean headerAvailable ;
	boolean tableHasHeader = false;
	
	// ... environment ...
	

	String sourceFilename = "" ;
	
	
	
	// constants ......................
	
	
	// volatile variables .............
	
	int headersCount;
	int maxColCount ;
	
	
	
	// helper objects .................
	transient StringsUtil strgutil ;
	//transient ArrUtilities arrutil ;
	transient DFutils  fileutil = new DFutils();
	transient PrintLog out ;

	
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	public DataTable( SomDataObject somdata, boolean isnumeric ){
		
		establishObjects( somdata, isnumeric ) ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	public void establishObjects( SomDataObject somdata, boolean isnumeric ){
		
		dt = this;
		isNumeric = isnumeric ;
		
		somData = somdata;
		missingValues = somData.getMissingValues() ;
		
		strgutil = new StringsUtil();
		// arrutil = new ArrUtilities ();
		
		out = somData.getOut() ;
		if (out==null){
			out = new PrintLog(4,false) ;
		}
		
		dt = this;
	}
	
	// creating a copy of the provided data table
	public  DataTable( DataTable inDatatable ) {
	
		dt= this;
		transferContent(inDatatable,dt);
	
	}
	
	@Override
	public int save() {
	
		int result=-1;
		String xstr="", filepath , vstr="", filename = "";
		PersistenceSettings ps;
										    String tname="";
										    if (this.tablename.length()>0){
										    	tname="<"+tablename+"> " ;
										    }
		                                    out.print(2, "saving data table "+tname+"...");
		FileOrganizer fileorg = somData.getTransformer().getFileorg() ;
		
		ps = fileorg.getPersistenceSettings() ;
		DFutils fileutil = fileorg.getFileutil();
		 
		if ((getTablename()!=null) && (getTablename().length()>0) && (getTablename().toLowerCase().contains("norm"))){
			vstr="_n";
		}
		filename = ps.getProjectName()+"-datatable" + vstr + fileorg.getFileExtension( FileOrganizer._TABLEOBJECT ) ;
		filepath = fileutil.createpath( fileorg.getDataDir(1, somData.getTransformer().getDerivationLevel()), filename);
		 
		
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		fileorg.careForArchive( FileOrganizer._TABLEOBJECT, filepath );
		
		storageDevice.storeObject( this, filepath) ;   // ATTENTION: this requires a sufficiently large heap size for the JVM
					// we can't serialize the whole thing into 1 object due to heap space overflow... 
					// we have to create our own storage procedure for tables, where columns and part tab
		if (fileutil.fileexists(filepath)==false){
			result=-3;
		}else{
			result =0;
		}
											out.print(2, "saving data table "+tname+" completed (code="+result+").");
		return result;
	}

	public DataTable clone( ){
		
		DataTable table = new DataTable(somData, true);
		
		transferContent(this, table);
		return table;
	}
	
	public void clear() {
		
		dataTable.clear(); 
		columnHeaders.clear();
		dataTableRows.clear();
		transposedTable.clear();
		indexValueMap.clear();
		
		
		strgutil = null;
		//arrutil = null;
		
	}

	private void transferContent( DataTable inDatatable, DataTable outDaTa){
		
		DataTableCol col;
		// this.dataTable = new ArrayList<DataTableCol>() ;
		
		outDaTa.colcount = inDatatable.colcount;
		outDaTa.rowcount = inDatatable.rowcount;
		
		for (int i=0;i<colcount;i++){
			
			col = new DataTableCol( outDaTa, inDatatable.getColumn(i)) ;
			outDaTa.dataTable.add(col);
			
		} // i-> all columns
		 
		outDaTa.columnHeaders = new ArrayList<String>( inDatatable.getColumnHeaders());
	
		outDaTa.formats = Arrays.copyOf( inDatatable.formats, inDatatable.formats.length) ;
		
		// System.arraycopy
		outDaTa.columnTypes = Arrays.copyOf( inDatatable.formats, inDatatable.columnTypes.length) ;
		outDaTa.mvActivated = inDatatable.mvActivated ; 
		outDaTa.mvIndicator = inDatatable.mvIndicator ;
		
		outDaTa.mvCountperColumn = new ArrayList<Integer>( inDatatable.mvCountperColumn);
		
		outDaTa.isNumeric = inDatatable.isNumeric ;
		outDaTa.maxScanRows = inDatatable.maxScanRows  ;
		
		
		outDaTa.derivedColumns = new ArrayList<Integer>( inDatatable.derivedColumns );
		
		outDaTa.colcountControlMode = 3; 
		outDaTa.colcountLimited = 0 ;
		
		outDaTa.headerAvailable = inDatatable.headerAvailable ;
		outDaTa.tableHasHeader  = inDatatable.tableHasHeader ;
		
		outDaTa.sourceFilename = inDatatable.sourceFilename ;
		outDaTa.headersCount = inDatatable.headersCount ; 
		outDaTa.maxColCount = inDatatable.maxColCount ;
		
	}


	
	/**
	 * 
	 * creates two ArrayLists:  <br/>
	 * - columnHeaders -> String  <br/>
 	 * - dataTable -> DataTableCol  <br/>
	 * 
	 */
	public void opencreateTable( String[] headers){
		int i,n ;
		String str ;
		DataTableCol col ;
		
		n = headers.length ;
		
		headersCount = n ;
		
		if ((colcountControlMode==1) || (colcountControlMode==3)){
			if (maxColCount<n){
				maxColCount=n ;
			}
		}

		if (n>0){
			headerAvailable = true ;
		} else{
			headerAvailable = false ;
		}
		
		// ArrayList<String> columnHeaders = new ArrayList<String>() ; 
		// ArrayList<DataTableCol> dataTable
		
		for (i=0;i<n;i++){
			columnHeaders.add( headers[i]) ;
			
			col = new DataTableCol(dt, i) ;
			col.isNumeric = this.isNumeric ;
			dataTable.add(col) ;
			
		} // i-> n, all headers

		
	}

	public void opencreateTable( ArrayList<String> headers){
		
	}


	public void setSourceFileName(String filename) {
		
		sourceFilename = filename;
	}

	@Override
	public void saveXml() {
		// TODO Auto-generated method stub
		
	}

	public void saveTableToFile(String filename) {
		 String srcfile="", str, tablestr="";
		 
		// dataTable.
		// ArrayList< ArrayList<Double> > dataTableRows = new ArrayList< ArrayList<Double>>() ;
		// ArrayList<String> columnHeaders = new ArrayList<String>() ; 
		 
		if ((dataTableRows!=null) && (dataTableRows.size()>2) && (getTablename().contains("norm"))){
			 
			tablestr = ArrUtilities.arr2Text( columnHeaders, "\t") +"\n";
			
			for (int i=0;i<dataTableRows.size();i++){
				
				ArrayList<Double> datarow = dataTableRows.get(i) ;
				str = ArrUtilities.arr2Text(datarow, 5,"\t") ;
				str = strgutil.replaceAll(str, ".00000", "") ; 
				str = strgutil.replaceAll(str, "000\t", "\t") ;
				
				tablestr = tablestr + str;
				
				if (i<dataTableRows.size()-1){
					 tablestr = tablestr +"\n" ;
				}
			} // i-> all rows
			fileutil.writeFileSimple( filename, tablestr) ;
			
		}else {
			srcfile = somData.getDataReceptor().getLoadedFileName();
			fileutil.copyFile(srcfile, filename) ;
		}
		
		
	}

	public void activateMissingValues( double mv_indicator){

		mvIndicator = mv_indicator;
		mvActivated = true ;
		
		activateMissingValues();
	}
	
	public double activateMissingValues(){
		
		mvActivated = true ;

		if ((mvCountperColumn==null) || (mvCountperColumn.size() < colcount())){
			mvCountperColumn = new ArrayList<Integer>() ;
			for (int i=0;i<colcount;i++){
				mvCountperColumn.add(0) ;
			}
		}

		return mvIndicator ;
	}

	
	/**
	 * 
	 * dependent on settings, we increase or decrease the
	 * colcount in case of mismatch with count of header elements 
	 * 
	 * 
	 * @param proposedColCount
	 * @return
	 */
	private int adjustValuesCount( int proposedColCount){
		int colcount = proposedColCount ;
		
		
		
		return colcount ;
	}
	
	
	/**
	 * 
	 * TODO: operations in importTable() should be multi-threaded, they are mostly independent from each other
	 *  
	 * 
	 * @param importTable
	 * @param nvetions
	 * @param importSettings
	 * @return
	 */
	public int importTable( DataTable importTable, TableImportSettings importSettings){ 
		
		return importTable( importTable, null, importSettings);
	}
	
	/**
	 * 
	 * This imports the data, which comprises the following steps:
	 *  - determining the format of the data in the column, index candidates are recognized automatically
	 *  - simple transformations into numeric values, e.g. yes\no -> 1\0
	 *  - creation of a SOM transformer model, if it does not exist
	 *  - applying nve
	 *  
	 * 
	 * @param importTable
	 * @param nvetions
	 * @param importSettings
	 * @return
	 */
	public int importTable( DataTable importTable, ArrayList<NomValEnum> nvetions, TableImportSettings importSettings){ 
			 
		
		int resultState = -1;
		
		DataTableCol column, col, newColumn ;
		NomValEnum nve;
		
		int n,nn, z, i,err, _dataformat ;
		boolean indexColPresent;
		
		
		ArrayList<Integer> potentialNVEcols  ;  
		ArrayList<String> sdl  ;
		
		// there are problems with empty cells... the resulting column is too short
		
		                               err = 1;
		try{
		
			this.sourceFilename = importTable.sourceFilename ;
			
			n = importTable.colcount() ;
			z = importTable.rowcount() ;
			
			nn = this.colcount() ; 
			
			tableHasHeader = importTable.hasHeader();
			if (headerAvailable){
				// overruling if user knows it better
				tableHasHeader = headerAvailable ;
			}else{
				
				headerAvailable = tableHasHeader;
			}
			boolean hb = this.headerAvailable;
			
			if (nn<=n){
				
				for (i=0;i<n;i++){
					columnHeaders.add( importTable.getColumnHeaders().get(i)) ;
					
					col = new DataTableCol(dt, i) ;
					this.dataTable.add(col) ;
					
				} // i-> n, all headers
				
			}
			 
			newColumn = new DataTableCol(dt,-1) ;
			
			formats = new int[n] ;
			
			// check format of columns
															if (out!=null){ out.print(3,"\ncheck format of columns...");};
															int outlevel=3;
			indexColPresent = false;												
			for (i=0;i<n;i++){  // TODO make this multi-digested if there are many variables (n>80), and many rows, such that even scanrow is large...
															
															if (n>120)outlevel=2;
															if (out!=null){ out.printprc(outlevel, i, n, n/10, "") ;};
				column = importTable.getColumn(i) ;
				column.setMaxScanRows( importTable.getMaxScanRows() );
if (i>=9){
	nn=0; 
}
				// determine format, should take into account __MV_TEXTUAL ("M.V.") as a marker:
				_dataformat = column.determineFormat(tableHasHeader) ;
				/*

					check whether there is a unique ID as integer
					// should not be done here
					NVE : config: max number of groups  
				 */
				
				formats[i] = _dataformat ;  
				
				if (indexColPresent==false){
					indexColPresent = _dataformat==0;
				}
			} // i->n all columns of import table
			 
															if (out!=null){ out.print(outlevel,"\nimporting columns...");};
			// if necessary, apply nve
			i=0;  
			// TODO make this multidigested ...
			
			for (i=0;i<n;i++){ // -> all variables
				
				column = importTable.getColumn(i) ;
				// only if it is num?
				// column = translateMvToNum(column); not necessary, will be accomplished elsewhere ("makeNumeric()")
				column.dataFormat = formats[i]; 
															if (out!=null){ out.printprc(outlevel, i, n, n/5, "") ;};
														
				if (formats[i]== DataTable.__FORMAT_ORGINT){
					// sort the data, and check for "groups" within the organizational codes 
					// could also be a date without dots, like so: 20080923
					/*
					  	later in makenumeric(): 
						some kind of semantic organization indicator, typically not nominal though,
					   	but "grouped", so we could "nominalize" them (1.interpreting as string, 2. nve)

							-> treat as strings, with constrained similarity from the beginning
							-> try to split off first 4 or last 4 digits
					 */
				}
				
				if (formats[i]>=DataTable.__FORMAT_DATE){
					
					// some of the values can be replaced with a true num value, others will be replaced by "-1"
					// new values to -> newColumn
 
					if (formats[i] >= DataTable.__FORMAT_STR){ // simple string ?
						
						
						
						// it could be still boolean, like yes/no, true/false etc...
						sdl = column.getStringsDiffList( tableHasHeader, 500, importTable.getMaxScanRows()); 
						
						if ((sdl.size()>2) && (sdl.size()< _MAX_ORDSTR_VARIABILITY)){
							// apply NVE: recode into integer, save mapping into the table 
							// as item in a list of mapping objects (index, map)
							
							/*  NVE NOT HERE !!!! move down to a stage behind format determination
							NveMapping nveMap = new NveMapping( sdl ) ; // createMap()
							this.nveMaps.add( nveMap );
							column.applyNveRecodeMap( nveMap );
							
							*/
							formats[i] = __FORMAT_ORDSTR ;  
						} else {
							if (sdl.size()==2){
								
								formats[i] = __FORMAT_BINSTR ;
							} 
						}
						
					}
					
					// if date, then serialize to start date
					// date and time will be treated by algorithms as common transformation!!
					if (formats[i] == DataTable.__FORMAT_DATE){  
						// column.serializeDateEntries(tableHasHeader);
						// we create new columns: inverse value = age, month, day of month, week, year
						// if date+time, create a new column for time serializing
					}
					// if time, then serialize to nanos of day
					if (formats[i] == DataTable.__FORMAT_TIME){  
					}
					
					// if boolean, replace with 0 and 1
					if ( (formats[i] == __FORMAT_BINSTR)){  
						// it could be 1,0 yes/no true/false t/f  y/n, ja/nein j/n s/n o/n +/- 
						int fc = column.recodeBinaryEntries(tableHasHeader);
						// if successful, we change from  __FORMAT_BINSTR=13  to __FORMAT_BIN=5
						// if more than 2 entries, we change to ordinal __FORMAT_ORD=3
						if (fc>0){
							formats[i] = fc ;	
						}
						
					}
					
				} // format not num ?
				else{ // is num...
					
					// if boolean, replace with 0 and 1
					if (formats[i] == __FORMAT_BIN ){  
						// it could be 1,0 yes/no true/false t/f  y/n, ja/nein j/n s/n o/n +/- 
						// nothing special ... we met 1,0 and perhaps missing values...
						err=0;
					}
					
				}
				
				
				column.setFormat(formats[i]);
				column.makeNumeric( tableHasHeader ) ;
			
				
				// introduce into this table: structure is created, values copied
				
				// following the various conversions, all columns should contain numerical values
				if (formats[i] <= __FORMAT_BIN){// || (formats[i] <= __FORMAT_ORGINT) 
					column.isNumeric = true ; 
				}
				column.hasHeader = tableHasHeader;
				getColumn(i).importColumn(column, 1) ;
				
				
				
				if (formats[i]==0){
					this.getColumn(i).setAsIndexColumnCandidate(true);
				}
				if ((formats[i]<=2) || (formats[i]==5)){
					getColumn(i).cellValueStr.clear();
				}
				colcount = i;
				 
			} // i-> all formats positions == all columns
			// --------------------------------------------------------------------------------------------------------
			
			// translate
			 
			if (indexColPresent == false){
				DataTableCol synthIndexColumn = new DataTableCol(this,0);
				for (i=0;i<n;i++){
					getColumn(i).index = i+1;
				}
				dataTable.add(0, synthIndexColumn) ; 
				synthIndexColumn.index=0;
				fillColumnAsIndex( synthIndexColumn , getColumn(1).size(), 0 , 1);
				formats = ArrUtilities.resizeArray(formats.length+1, formats);
				
				for (i=n;i>0;i--){
					formats[i] = formats[i-1];  
				}
				formats[0] = 0;

			}
			
			resultState = 0;
			colcount = importTable.colcount();
			z = importTable.rowcount();
			if (rowcount<=0){
				if (colcount>0){
					rowcount = getColumn(0).rowcount;
				}
				if (rowcount<=0){
					rowcount=z;
				}
			}
			
			// determineVarietyOfColumnData();
			
			// createRowOrientedTable(); not for raw data
			
			
			
			
			
		}catch (Exception e){
			resultState = -err ;
			e.printStackTrace() ;
		}
		
		return resultState;
		
	}
	 
	

	private void fillColumnAsIndex(DataTableCol col, int count, int startIxVal, int increment) {
		double value;
		
		col.cellValues.ensureCapacity(count);
		
		for (int i=0;i<count;i++){
			value = startIxVal + (i*increment) ;
			col.addValue(value) ;
		}
		
	}

	/**
	 * clears any previous row perspective and creates a fresh one
	 */
	public void createRowOrientedTable(){
		createRowOrientedTable(0);
	}
	
	public void createRowOrientedTable( int alignedByVariables ){
		
		boolean useCol;
		DataTableCol col ;
		ArrayList<Double> values;
		ArrayList<Double> rowdata;
		int cc=-1,rc = -1, ir=-1,ic=-1 ;
		double dv;
		 
		try{
			if (dataTableRows==null){
				dataTableRows = new ArrayList<ArrayList<Double>>(); 
			}
			dataTableRows.clear() ;  
			// ArrayList<DataTableCol> dataTable = new ArrayList<DataTableCol>() ; dataTableRows
			// ArrayList< ArrayList<Double>>()
if (getTablename().contains("normalized")){
	dv=0.0;
} 			
			rc = dataTable.get(0).getCellValues().size();
			cc = dataTable.size();
			
			System.gc();
			out.delay(50) ;
											out.print(2, "creating row-oriented table, going to arrange "+rc+" normalized records into rows.") ;

										 
            						 
            

			for (int r=0;r<rc;r++){
											int outlevel=3;
											if (rc*cc>80000)outlevel=2;
											out.printprc(outlevel, r, rc, rc/10, "");
				ir=r;
				rowdata = new ArrayList<Double>(); 


				for (int c=0;c<cc;c++){
					ic=c;
					
					useCol=true;
					if (alignedByVariables>=1){
					}

					if (useCol) {

						try {

							col = dataTable.get(c);

							values = col.getCellValues();
							if ((values != null) && (values.size() >= r - 1)) {
								dv = values.get(r);
							} else {
								dv = -1.0;
							}

						} catch (Exception e) {
							dv = -1.0;
						}
						rowdata.add(dv);

					}// useCol ?
				}
if (r>rc-3){
	int zz=0;
	zz=1;
}
				dataTableRows.add(rowdata) ;
			} // r ->
			
			
		}catch (Exception e){
			out.printErr(1, "Critical error in <createRowOrientedTable()>, r,c: "+ir+","+ic+" ,  while max is: "+rc+", "+dataTable.size());
			e.printStackTrace() ;
		}
	 
		rc=0;
	}
	
	
	public void createTransposedForm() {
		// ArrayList<DataTableCol>
		// transposes  dataTable -> transposedTable  
		// the transposed table is always a numeric table, where previous columnheaders are replaced by enumeration 
		  
		
	}
	
	public int addDerivedColumn( TransformationStack varTransformStack, StackedTransformation st, int mode) {
		// 
		DataTableCol srcCol ;
		DataTableCol newColumn ;
		
		
		String sourceColumnLabel = "" , targetColumnLabel ="";
		int    sourceColumnIx = -1 , targetColumnIx =-1;
		
		sourceColumnLabel = varTransformStack.getVarLabel() ;
		
		// loop until there is no further variable of the intended name , avoid suffixes like "_c_c"
		targetColumnLabel = sourceColumnLabel+"_c" ;
		
		sourceColumnIx = columnHeaders.indexOf(sourceColumnLabel) ;
		targetColumnIx = columnHeaders.size() ;
		
		srcCol = this.dataTable.get(sourceColumnIx) ;
		if (mode<=0){
			newColumn = new DataTableCol( this, sourceColumnIx); // no data copy
		}else{
			newColumn = new DataTableCol( this, srcCol);		 // incl. data copy
		}
		newColumn.hasHeader = true;

		if (mode>=1){
			columnHeaders.add(targetColumnLabel) ;
		}
		
		dataTable.add(newColumn);

		// don't forget the rows perspective....
		ArrayList<Double>  row ;
		double v;
		if (dataTableRows != null) {
			for (int i = 0; i < this.dataTableRows.size(); i++) {

				row = dataTableRows.get(i);
				v = dataTable.get(sourceColumnIx).getValue(i);
				row.add(v);

			}
		}
		
		// return the index of the new column
		return (dataTable.size()-1) ;
	}

	public int getFirstIndexColumnCandidate(){
		int rc = -1;
		for (int i=0;i<formats.length;i++){
			
			if (formats[i]==0){
				rc=i;
				break;
			}
		}
		return rc;
	}
	
	
	public void createIndexValueMap() {
		int n,f;
		double dv;
		
		DataTableCol col;
		
		getIndexValueMap().clear();
		
		f = getFirstIndexColumnCandidate() ; 
		
		col = this.dataTable.get(f);
		
		n = col.size();
		
		// 
		for (int i=0;i<n;i++){
			
			dv = col.cellValues.get(i) ;
			indexValueMap.put(dv, i) ;
		}
		
	}

	private int getMaxScanRows() {
		
		return maxScanRows;
	}

	public void setMaxScanRows(int maxScanRows) {
		this.maxScanRows = maxScanRows;
	}


	private boolean hasHeader() {
		boolean rB=false ;
		int n;
		ArrayList<String> row ;
		int rowsToCheck = 5;
		int[] dcs = new int[rowsToCheck];
		
		n = this.colcount ;
		
		/*
		 * the idea is to check whether in the first row there are much more denied conversions
		 * than in the second(+)rows
		 * 
		 */
		row = (ArrayList<String>)getRowValuesArrayList(0,String.class);
		
		dcs[0] = ArrUtilities.checkTypeOfListItems( (Object)row, String.class, Double.class );
		
		for (int i=1;i<rowsToCheck;i++){
			row = (ArrayList<String>)getRowValuesArrayList(i,String.class);
			dcs[i] = ArrUtilities.checkTypeOfListItems( (Object)row, String.class, Double.class);
		}
		int z=0;
		int rowOfMaxConversionDenials = ArrUtilities.arrayMaxPos(dcs);
		int firstRowDcs = dcs[0];
		z = ArrUtilities.arraySum(dcs) - dcs[0];
		int pmi = ArrUtilities.arrayMinPos(dcs);
		dcs[0] = 0;
		int pmx = ArrUtilities.arrayMaxPos(dcs); 
		

		double vmx = dcs[pmx]; // the max value of conversion denials across all rows
		double vmi = dcs[pmi]; // the min value of conversion denials across all rows

		z = (int)(vmx-vmi);
		
		double c = (double)z/((double)(rowsToCheck-1)) ;
		
		rB = (rowOfMaxConversionDenials==0) && (firstRowDcs> (0.92*(double)n)) && // first row should consist almost completely of Strings 
		      ((c<(0.2*(double)n)) || (z<(0.2*(double)n)));   // all other rows should consist mostly of num AND
		                                                      // they should be almost equal in their formats per position
		
		
		return rB;
	}



	/**
	 * 
	 * returns the first index column
	 * @param lastColindex
	 * @return
	 */
	public int checkforIndexColumn( int lastColindex ){
		int ixcol = -1;
		
		
		
		return ixcol ;
	}
	
	
	public int[] checkforIndexColumns( int lastColindex ){
		int[] ixcol = new int[0];
		
		
		return ixcol ;
	}
	
	public void makeNumeric(){
		
	}
	
	

	public void addIndexColumn(){
		
		// resize and keep values ! : mvCountperColumn = new int[colcount] ;
		
	}
	
	
	public void applyFormula( int columnIndex, Formula formula){
		
		
		
	}

	public double applyFormula( int columnIndex, int rowIndex, Formula formula){
		double result=-1 ;
		
		
		
		return result;
	}


	
	
	// -------------------------------------------------------------------------------
	
	public void setRow( ArrayList<Double> values){
		
	}
	public void setRow( double[] values){
		int i,n=0;
		DataTableCol column;
		double val ;
		
		
		n= values.length ;
		
		n = adjustValuesCount(n) ;
		
		if (maxColCount<n){
			maxColCount=n ;
		}
		
		for (i=0;i<n;i++){
		
			if (i>values.length-1){
				val = -1.0;
			} else {
				val = values[i] ;
			}
			
			if (i< colcount()){
				column = dataTable.get(i) ;
			} else{
				column = new DataTableCol( dt, dataTable.size() );
				dataTable.add( column ) ;
			}
			  ;
				 
			if (mvActivated = true){
				if (val == mvIndicator){
					if (i<mvCountperColumn.size()){
						mvCountperColumn.set(i, mvCountperColumn.get(i)+1) ;
					}
				}
			}
			column.addValue(val) ;
		}
		
		i=0;
	}

	public void setRow( String[] valuesStr){
	
		if (isNumeric==true){
			
			setRowNumericals( valuesStr );
			
		} else {
			setRowCellStrings( valuesStr );
		}
		
		
	}

	protected void setRowCellStrings( String[] values  ){
		int i,n=0;
		DataTableCol column;
		String val ;
		
		
		n= values.length ;
		
		n = adjustValuesCount(n) ;
		
		if (maxColCount<n){
			maxColCount=n ;
		}
		
		for (i=0;i<n;i++){
		
			if (i>values.length-1){
				val = "-1.0";
			} else {
				val = values[i] ;
			}
			
			if (val.length() == 0){
				val = __MV_TEXTUAL ; // e.g. "M.V."
			}
			
			if (i< colcount()){
				column = dataTable.get(i) ;
			} else{
				column = new DataTableCol(dt, dataTable.size() );
				dataTable.add( column ) ;
			}
			  ;
				 
			 
			column.addValueStr(val) ;
			
			val = val+"" ;
		}
		i=0;
	}

	protected void setRowNumericals(String[] valuesStr){
		
		int ArrayListsize,j ;
		double val;
		double[] tmp ;
		String cellStr;
		
		try{
		ArrayListsize = valuesStr.length ;
		tmp = new double[ArrayListsize];
		
		for (j = 0; j < ArrayListsize; j++) {
	
			cellStr = valuesStr[j] ;
			 
			
			if (strgutil.isNumericX(cellStr )) {
				
				val = Double.parseDouble(valuesStr[j]);
				val = (Math.round(val*100000000.0)/100000000.0) ;
				
				tmp[j] = val ;
			} else {
				// all columns should be "NVE ordinalized" by reading through raw data adaptor and transformer
				tmp[j] = -1;
			}
		}
		
		setRow( tmp ) ;
		}catch(Exception e){
			String errmsg = "";
			out.diagnosticMsg.add(errmsg+"\n"+e.getStackTrace()) ;
		}
		j=0;
	}

	public void setRow( DataTableRow values){
		
		
	}

	public SomDataObject getSomData() {
		return somData;
	}

	public void setSomData(SomDataObject somData) {
		this.somData = somData;
	}

	public ArrayList<DataTableCol> getDataTable() {
		return dataTable;
	}
	

	public DataTableCol getDataTableColumn( int colindex) {
		 
		return dataTable.get(colindex);
	}
	
	/** index is NOT the value in index column, it is just the enum value, the i-th row */
	public   ArrayList<Double> getDataTableRow(int index) {
		
		ArrayList<Double> rowData = new ArrayList<Double> ();
		
		if ((index>=0) && (index< this.dataTableRows.size())){
			rowData = dataTableRows.get(index);
		}
		
		return rowData;
	}
	
	public  double[] getDataTableRowAsArray() {
		double[] rowData=null; 
		return rowData;
	}
	
	
	public ArrayList<ArrayList<Double>> getDataTableRows() {
		return dataTableRows;
	}

	public double getTableCell( int column, int row ) {
		double v=-1;
		
		this.getColumn(column).getValue(row) ;
		
		return v;
	}
	
	 
	
	public String getTableCellasStr() {
		String vstr="" ;
		return vstr;
	}
	
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getTablename() {
		return tablename;
	}

	public boolean isFilled(){
		boolean rb=false;
		
		if ((colcount>1) && (rowcount>2)){
			if (headerAvailable==true){
				rb = true ;
			}
		} else{
			out.diagnosticMsg.add("checking data fill status, colcount="+colcount+ "  rowcount" + rowcount)  ;
		}
		
		return rb;
	}
	
	
	public int colcount(){
		colcount = dataTable.size();
		return colcount ;
	}
	
	public int rowcount(){
		int rc = -1;
		DataTableCol col ;
		if (colcount()>0){
			
			col = dataTable.get(0) ;  
			rc = getRowcount(); // col.size(); 
		}
		rowcount = rc ;
		return rc;
	}
	
	public void setRowCount(int numberofRows){
		// actually creating an empty table, filled with "" or 0 according to type
	}
	
	
	private <T> ArrayList<T> getRowValuesArrayList(int index, Class<T> clazz) {

		ArrayList<T> arrayList = new ArrayList<T>() ;
		T val ;

		if (index<0){
			return arrayList;
		}
		if (index > colcount){
			return arrayList;
		}
		 
		
		for (int i=0;i<colcount;i++){
			
			DataTableCol col = dataTable.get(i);
			val = col.getValue(index,clazz) ;
			arrayList.add(val) ;
		}
		
		return arrayList ;
	}
	
	public ArrayList<Double> getRowValuesArrayList(int index){
		
		ArrayList<Double> numRowArrayList = new ArrayList<Double>() ;
		double val ;

		if (index<0){
			return numRowArrayList;
		}
		if (colcount==0){
			return numRowArrayList;
		}
		if (index > rowcount){
			return numRowArrayList;
		}
		 
		if ((this.dataTableRows != null) && (dataTableRows.size() >= dataTable.get(0).rowcount-2) && (index<dataTableRows.size())) {
			numRowArrayList = dataTableRows.get(index) ;
		} else {
			for (int i = 0; i < colcount; i++) {
				val = dataTable.get(i).getValue(index);
				numRowArrayList.add(val);
			}
		}
		
		return numRowArrayList ;
	}
	
	
	public double[] getRowValues( int index){
		double[] val = new double[0] ;
		
		if (index<0){
			return val;
		}
		if (index > colcount){
			return val;
		}
		
		val = new double[ colcount() ] ;
		
		
		for (int i=0;i<colcount;i++){
			val[i] = dataTable.get(i).getValue(index) ;
		}
		
		return val;
	}
	
	
	public void getRow( ){
		
		// double[] values
		
	}


	/**
	 * 
	 * 
	 * 
	 * @param index
	 * @param format   1 = double[] ;  2 = ArrayList<Double>
	 * @return an object, which needs to be casted by the caller
	 */
	public Object getRow( int index, int format ){
		
		Object obj = null ;
		// String[] valuesStr
		// DataTableRow values
		ArrayList<Double> numRowArrayList ;
		double[] numRowArr ;
		
		
		if (index<0){
			return obj;
		}
		if (index > rowcount+1){
			return obj;
		}
		
		
		if (format==1){ // as array 
			if (index==0){
				obj = (Object)(getColumnHeaders() ) ;
			}else{
				numRowArr = getRowValues( index ) ;
				obj = (Object)(numRowArr.clone() );
			}
		}
		else{
			if (format == 2) {
				if (index==0){
					// getColumnHeaders()
					obj = (Object)(columnHeaders); // ArrayList<String>
				}else{
					numRowArrayList = getRowValuesArrayList(index) ;
					obj = (Object)numRowArrayList ;  // ArrayList<Double>
					 
				}				
			}
		}
		
		return obj ;
	}
	
	
	public DataTableCol getColumn( int index ){
		DataTableCol dtc = null;
		
		if ((index>=0) && (index<dataTable.size())){
			dtc = dataTable.get(index);
		}
		return dtc ;
	}

	public DataTableCol getColumn( String headerlabel){
		int index = -1;
		DataTableCol col = null; 
		
		try{
			
			index = columnHeaders.indexOf(headerlabel);
			
			if (index>=0){
				col = dataTable.get(index) ;
			}
			
		}catch(Exception e){

		}
		return col ;
	}
	
	public String getColumnHeader( int index ){
		String colhead="";
		
		if ((index>=0) && (index<columnHeaders.size())){
			colhead = columnHeaders.get(index) ;
		}
		return colhead ;
		
	}

	public String getColumnHeader( DataTableCol colobj ){
		return getColumnHeader( colobj.index) ;
	}

	public boolean isNumeric() {
		return isNumeric;
	}

	public void setNumeric(boolean isNumeric) {
		DataTableCol colobj ;
		
		this.isNumeric = isNumeric;
		
		for (int i=0;i<colcount();i++){
			
			colobj = getColumn(i) ;
			colobj.isNumeric = isNumeric ;
		}
		
	}

	public int getMaxColCount() {
		return maxColCount;
	}

	public void setMaxColCount(int maxColCount) {
		this.maxColCount = maxColCount;
	}

	public ArrayList<DataTableCol> getTransposedTable() {
		return transposedTable;
	}

	public int getHeadersCount() {
		return headersCount;
	}

	public void setOut(PrintLog out) {
		this.out = out;
	}

	public String[] getColumnHeadersAsArray(){
		String[] headstr = new String[ columnHeaders.size() ];
		
		for (int i=0;i<headstr.length;i++){
			headstr[i] = columnHeaders.get(i) ;
		}
		return headstr ;
		
	}

	public ArrayList<String> getColumnHeaders(){
		if (columnHeaders==null){
			columnHeaders = new ArrayList<String>();
		}
		return columnHeaders ;
	}
	


	public int getColumnIndexOfType(int formatType) {
		int index=-1;
		if (formats!=null){
			for (int i = 0; i < formats.length; i++) {
				if (formats[i]==formatType){
					index=i;
					break;
				}
			}
		}
		return index;
	}

	public String translateFormatID( int fid){
		String fstr="";

		if ((fid>=0) && (fid<=19))
		switch (fid){
			case 0 : fstr =  "_ID";
			case 1 : fstr =  "_NUM";
			case 2 : fstr =  "_INT";
			case 3 : fstr =  "_ORGINT";
			case 4 : fstr =  "_BIN";
			case 5 : fstr =  "";
			case 6 : fstr =  "_TIME";
			case 7 : fstr =  "_DATE";
			case 8 : fstr =  "_DATETIME";
			case 9 : fstr =  "";
			case 10 : fstr = "_ORDSTR";
			case 11 : fstr = "_STR";
			case 12 : fstr = "_TXT";
			case 13 : fstr = "_BINSTR";
			case 14 : fstr = "";
			case 15 : fstr = "";
			case 16 : fstr = "";
			case 17 : fstr = "";
			case 18 : fstr = "";
			case 19 : fstr = "";
		}
		
		return fstr; 
	}
	
	public int[] getFormats() {
		return formats;
	}

	public void setFormats(int[] informats) {
		formats = new int[informats.length];
		System.arraycopy(informats, 0, formats, 0, formats.length);
	}

	public void setIndexValueMap(Map<Double,Integer> indexValueMap) {
		this.indexValueMap = indexValueMap;
	}

	public Map<Double,Integer> getIndexValueMap() {
		return indexValueMap;
	}

	public int getColcount() {
		if (colcount<=0){
			colcount = dataTable.size() ;
		}
		if (colcount<=0){
			colcount = columnHeaders.size() ;
		}
		return colcount;
	}

	public void setColcount(int colcount) {
		this.colcount = colcount;
	}

	public int getRowcount() {
		int r1=0,r2=0,r3=0;
		
		if (rowcount<=0){
			if (getColcount()>0){
				r1 = dataTable.get(0).size() ;
			}
			if (getColcount()>1){
				r2 = dataTable.get(1).size() ;
			}
			r3 = dataTable.get(getColcount()-1).size() ;
			
			rowcount = Math.max(r1, Math.max(r3, r2));
		}
		return rowcount;
	}

	public void setRowcount(int rowcount) {
		this.rowcount = rowcount;
	}

	public int[] getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(int[] columnTypes) {
		this.columnTypes = columnTypes;
	}

	public boolean isMvActivated() {
		return mvActivated;
	}
	public boolean getMvActivated() {
		return mvActivated;
	}

	public void setMvActivated(boolean mvActivated) {
		this.mvActivated = mvActivated;
	}

	public double getMvIndicator() {
		return mvIndicator;
	}

	public void setMvIndicator(double mvIndicator) {
		this.mvIndicator = mvIndicator;
	}

	public ArrayList<Integer> getMvCountperColumn() {
		return mvCountperColumn;
	}

	public void setMvCountperColumn(ArrayList<Integer> mvCountperColumn) {
		this.mvCountperColumn = mvCountperColumn;
	}

	
	public ArrayList<Integer> getDerivedColumns() {
		return derivedColumns;
	}

	public void setDerivedColumns(ArrayList<Integer> derivedColumns) {
		this.derivedColumns = derivedColumns;
	}

	public MissingValues getMissingValues() {
		return missingValues;
	}

	public void setMissingValues(MissingValues missingValues) {
		this.missingValues = missingValues;
	}

	public int getColcountControlMode() {
		return colcountControlMode;
	}

	public void setColcountControlMode(int colcountControlMode) {
		this.colcountControlMode = colcountControlMode;
	}

	public int getColcountLimited() {
		return colcountLimited;
	}

	public void setColcountLimited(int colcountLimited) {
		this.colcountLimited = colcountLimited;
	}

	public boolean isHeaderAvailable() {
		return headerAvailable;
	}
	public boolean getHeaderAvailable() {
		return headerAvailable;
	}

	public void setHeaderAvailable(boolean headerAvailable) {
		this.headerAvailable = headerAvailable;
	}

	public boolean isTableHasHeader() {
		return tableHasHeader;
	}
	public boolean getTableHasHeader() {
		return tableHasHeader;
	}

	public void setTableHasHeader(boolean tableHasHeader) {
		this.tableHasHeader = tableHasHeader;
	}

	public String getSourceFilename() {
		return sourceFilename;
	}

	public void setSourceFilename(String sourceFilename) {
		this.sourceFilename = sourceFilename;
	}

	public void setDataTable(ArrayList<DataTableCol> dataTable) {
		this.dataTable = dataTable;
	}

	public void setTransposedTable(ArrayList<DataTableCol> transposedTable) {
		this.transposedTable = transposedTable;
	}

	public void setColumnHeaders(ArrayList<String> columnheaders) {
		if (columnheaders != null){
			columnHeaders = columnheaders;
			colcount = columnHeaders.size(); 
		}
	}

	public void setHeadersCount(int headersCount) {
		this.headersCount = headersCount;
	}


	public void setOutPrn( PrintLog outprn){
		out = outprn;
	}

	public void setName(String tname) {
		 setTablename(tname); 
	}

 
	


	
}

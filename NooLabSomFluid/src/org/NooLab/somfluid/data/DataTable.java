package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

 
import org.NooLab.somfluid.components.MissingValues;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.util.Formula;
import org.NooLab.somtransform.algo.Binning;
import org.NooLab.utilities.ArrUtilities;
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
public class DataTable implements Serializable{

	private static final long serialVersionUID = 3655282650007767562L;

	// not avail: ExecSettings (config for diagnostic printouts etc.) settings ;
	// =================================
	
	// object references ..............

	transient SomDataObject somData;
	
	transient DataTable dt;

	// main variables / properties ....
	
	// our table consists of a list of columns
	ArrayList<DataTableCol> dataTable = new ArrayList<DataTableCol>() ; 
	
	// the transposed table is always a numeric table, where previous columnheaders are replaced by enumeration 
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
	int maxScanRows = 200 ;
	
	NveMappings nveMaps;
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
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities ();
	
	transient PrintLog out = new PrintLog(4,false) ;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	public DataTable( SomDataObject somdata, boolean isnumeric ){
		
		dt = this;
		isNumeric = isnumeric ;
		
		somData = somdata;
		missingValues = somData.getMissingValues() ;
		
		nveMaps = new NveMappings(this) ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	// creating a copy of the provided data table
	public  DataTable( DataTable inDatatable ) {
	
		dt= this;
		transferContent(inDatatable,dt);
	
	}
	
	public DataTable clone( ){
		
		DataTable table = new DataTable(somData, true);
		
		transferContent(this, table);
		return table;
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
		
		outDaTa.nveMaps = new NveMappings( inDatatable.nveMaps );
		
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
	public int importTable( DataTable importTable, ArrayList<NomValEnum> nvetions, TableImportSettings importSettings){ 
			 
		
		int resultState = -1;
		
		DataTableCol column, col, newColumn ;
		NomValEnum nve;
		
		int n,nn, z, i,err, _dataformat ;
		boolean indexColPresent;
		
		
		ArrayList<Integer> potentialNVEcols  ;  
		ArrayList<String> sdl  ;
		
		
		
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
			
			indexColPresent = false;												
			for (i=0;i<n;i++){  // TODO make this multidigested ...
															if (out!=null){ out.printprc(3, i, n, n/10, "") ;};
				column = importTable.getColumn(i) ;
				column.setMaxScanRows( importTable.getMaxScanRows() );
				
				// determine format
				_dataformat = column.determineFormat(tableHasHeader) ;
				   
				formats[i] = _dataformat ;  
				
				if (indexColPresent==false){
					indexColPresent = _dataformat==0;
				}
			} // i->n all columns of import table
			 
															if (out!=null){ out.print(3,"\nimporting columns...");};
			// if necessary, apply nve
			i=0;  // TODO make this multidigested ...
			for (i=0;i<n;i++){
				
				column = importTable.getColumn(i) ;
															if (out!=null){ out.printprc(3, i, n, n/5, "") ;};
				if (formats[i]>2){
															
				// some of the values can be replaced with a true num value, others will be replaced by "-1"
				// new values to -> newColumn

	 				// 
					if (formats[i] == 8){ // simple string ?
						// it could be still boolean, like yes/no, true/false etc...
						sdl = column.getStringsDiffList( tableHasHeader, 500, importTable.getMaxScanRows()); 
						
						if ((sdl.size()>2) && (sdl.size()<500)){
							// apply NVE: recode into integer, save mapping into the table 
							// as item in a list of mapping objects (index, map)
							
							NveMapping nveMap = new NveMapping( sdl ) ; // createMap()
							this.nveMaps.add( nveMap );
							column.applyNveRecodeMap( nveMap );
							
						} else {
							if (sdl.size()==2){
								
								formats[i] = 3;
							} 
						}
						
					}
					
					// if date, then serialize to start date

					if (formats[i] == 4){  
						// column.serializeDateEntries(tableHasHeader);
						// we create new columns: inverse value = age, month, day of month, week, year
					}
					
					// if time, then serialize to nanos of day
					if (formats[i] == 5){  
						
					}
					
					
					// if date+time, create a new column for time serializing
					if (formats[i] == 6){  
						
					}
					
					
					// if boolean, replace with 0 and 1
					if (formats[i] == 3){  
						// it could be 1,0 yes/no true/false t/f  y/n, ja/nein j/n s/n o/n +/- 
						column.recodeBinaryEntries(tableHasHeader);
					}
					
					
				} // format not num ?
				else{ // is num...
					
					
				}
				
				column.setFormat(formats[i]);
				column.makeNumeric( tableHasHeader ) ;
				
				/*
				newColumn.setFormat(formats[i]);
				newColumn.reset(0) ;
				newColumn.importColumn( column, 1 ) ;
				*/
				 
				
				// introduce into this table: structure is created, values copied
				
				// following the various conversions, all columns should contain numerical values
				column.isNumeric = true ;
				column.hasHeader = tableHasHeader;
				this.getColumn(i).importColumn(column, 1) ;
				
				
				
				if (formats[i]==0){
					this.getColumn(i).setAsIndexColumnCandidate(true);
				}
				if (formats[i]<=2){
					getColumn(i).cellValueStr.clear();
				}
				colcount = i;
				 
			} // i-> all formats positions == all columns
			
			// translate
			 
			if (indexColPresent == false){
				DataTableCol synthIndexColumn = new DataTableCol(this,0);
				for (i=0;i<n;i++){
					getColumn(i).index = i+1;
				}
				dataTable.add(0, synthIndexColumn) ; 
				synthIndexColumn.index=0;
				fillColumnAsIndex( synthIndexColumn , getColumn(1).size(), 0 , 1);
				formats = arrutil.resizeArray(formats.length+1, formats);
				
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
			
			determineVarietyOfColumnData();
			
			createRowOrientedTable();
			
		}catch (Exception e){
			resultState = -err ;
			e.printStackTrace() ;
		}
		
		return resultState;
		
	}
	
	/**
	 * checks how many different values occur in a column
	 */
	private void determineVarietyOfColumnData() {
		//  
		// histogram :
		// import flanagan.analysis.*; 
		// Stat.histogramBins(dvs, binWidth) ;
		DataTableCol col;
		
		
		
		for (int i=0;i<colcount;i++){
		
			Binning binnAlgo = new Binning();
			
			// if it is not index... and not empty...
			col  = this.getColumn(i);
			
			binnAlgo.setValues( col.getCellValues() );
			binnAlgo.calculate();
			binnAlgo.getDescriptiveResults() ;
			
			// put it to... the statsdescription of the variable
		}
	}
	
	

	private void fillColumnAsIndex(DataTableCol col, int count, int startIxVal, int increment) {
		double value;
		
		col.cellValues.ensureCapacity(count);
		
		for (int i=0;i<count;i++){
			value = startIxVal + (i*increment) ;
			col.addValue(value) ;
		}
		
	}

	public void createRowOrientedTable(){
		
		ArrayList<Double> rowdata;
		int rc ;
		double dv;
		
		try{
			dataTableRows.clear() ;
			// ArrayList<DataTableCol> dataTable = new ArrayList<DataTableCol>() ; dataTableRows
			// ArrayList< ArrayList<Double>>()
			
			rc = dataTable.get(0).getCellValues().size();
			
			for (int r=0;r<rc;r++){
				
				rowdata = new ArrayList<Double>(); 
				
				for (int c=0;c<dataTable.size();c++){
					
					dv = dataTable.get(c).getCellValues().get(r) ;
					rowdata.add(dv);
				}

				dataTableRows.add(rowdata) ;
			} // r ->
			
			
		}catch (Exception e){
			
			e.printStackTrace() ;
		}
		rc=0;
	}
	
	
	public void createTransposedForm() {
		// ArrayList<DataTableCol>
		// transposes  dataTable -> transposedTable  
		// the transposed table is always a numeric table, where previous columnheaders are replaced by enumeration 
		  
		
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

	@SuppressWarnings("unchecked")
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
		
		dcs[0] = arrutil.checkTypeOfListItems( (Object)row, String.class, Double.class);
		
		for (int i=1;i<rowsToCheck;i++){
			row = (ArrayList<String>)getRowValuesArrayList(i,String.class);
			dcs[i] = arrutil.checkTypeOfListItems( (Object)row, String.class, Double.class);
		}
		int z=0;
		int rowOfMaxConversionDenials = arrutil.arrayMaxPos(dcs);
		int firstRowDcs = dcs[0];
		z = arrutil.arraySum(dcs) - dcs[0];
		int pmi = arrutil.arrayMinPos(dcs);
		dcs[0] = 0;
		int pmx = arrutil.arrayMaxPos(dcs); 
		

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
				val = "M.V." ;
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
	
	
	public double getTableCell( int column, int row ) {
		double v=-1;
		
		this.getColumn(column).getValue(row) ;
		
		return v;
	}
	
	 
	
	public String getTableCellasStr() {
		String vstr="" ;
		return vstr;
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
			rc = col.size(); 
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
		return dataTable.get(index) ;
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
		return columnHeaders.get(index) ;
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
		 
		return columnHeaders ;
	}
	


	public int[] getFormats() {
		return formats;
	}

	public void setFormats(int[] formats) {
		this.formats = formats;
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
		if (rowcount<=0){
			if (getColcount()>0){
				rowcount = dataTable.get(0).size() ;
			}
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

	public NveMappings getNveMaps() {
		return nveMaps;
	}

	public void setNveMaps(NveMappings nveMaps) {
		this.nveMaps = nveMaps;
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

	public void setColumnHeaders(ArrayList<String> columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	public void setHeadersCount(int headersCount) {
		this.headersCount = headersCount;
	}


	public void setOutPrn( PrintLog outprn){
		out = outprn;
	}

	


	
}

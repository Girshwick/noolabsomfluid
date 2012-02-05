package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;

 
import org.NooLab.somfluid.util.Formula;
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
 */
public class DataTable implements Serializable{

	// not avail: ExecSettings (config for diagnostic printouts etc.) settings ;
	// =================================
	
	// object references ..............

	
	// main variables / properties ....
	ArrayList<DataTableCol> dataTable = new ArrayList<DataTableCol>() ;
	
	// the transposed table is always a numeric table, where previous columnheaders are replaced by enumeration 
	ArrayList<DataTableCol> transposedTable = new ArrayList<DataTableCol>() ;
	
	ArrayList<String> columnHeaders = new ArrayList<String>() ; 
	
	int[] formats ;
	
	int colcount;
	int rowcount;
	
	int[] columnTypes ;
	
	boolean mvActivated ;
	double mvIndicator = -1 ;
	
	int[] mvCountperColumn ;
	
	/**  if isNumeric, all data are transformed into values upon import of data, 
	 *   incl. NominalValuesEnumeration <br/>
	 *   if not numeric, then all data are hold as String */
	boolean isNumeric ;
	int maxScanRows = 200 ;
	
	NveMappings nveMaps;
	ArrayList<Integer> derivedColumns = new ArrayList<Integer>();
	
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
	StringsUtil strgutil = new StringsUtil();
	ArrUtilities arrutil = new ArrUtilities ();
	
	PrintLog out = new PrintLog(4,false) ;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	public DataTable( boolean isnumeric ){
		
		isNumeric = isnumeric ;
		
		nveMaps = new NveMappings(this) ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

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
			
			col = new DataTableCol(i) ;
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
		
		if ((mvCountperColumn==null) || (mvCountperColumn.length<colcount())){
			mvCountperColumn = new int[colcount] ;
		}
	}
	
	public double activateMissingValues(){
		
		mvActivated = true ;

		if ((mvCountperColumn==null) || (mvCountperColumn.length<colcount())){
			mvCountperColumn = new int[colcount] ;
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
		
		
		
		ArrayList<Integer> potentialNVEcols  ;  
		ArrayList<String> sdl  ;
		
		
		
		                               err = 1;
		try{
		
			
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
					columnHeaders.add( importTable.getColumnHeaders()[i]) ;
					
					col = new DataTableCol(i) ;
					this.dataTable.add(col) ;
					
				} // i-> n, all headers
				
			}
			
			  
			newColumn = new DataTableCol(-1) ;
			
			formats = new int[n] ;
			
			// check format of columns
															if (out!=null){ out.print(3,"\ncheck format of columns...");};
															
			for (i=0;i<n;i++){  // TODO make this multidigested ...
															if (out!=null){ out.printprc(3, i, n, n/10, "") ;};
				column = importTable.getColumn(i) ;
				column.setMaxScanRows( importTable.getMaxScanRows() );
				
				// determine format
				_dataformat = column.determineFormat(tableHasHeader) ;
				   
				formats[i] = _dataformat ;  
				
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
				
			} // i-> all formats positions == all columns
			
			// translate
			
			resultState = 0;
			
		}catch (Exception e){
			resultState = -err ;
			e.printStackTrace() ;
		}
		
		return resultState;
		
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
				column = new DataTableCol( dataTable.size() );
				dataTable.add( column ) ;
			}
			  ;
				 
			if (mvActivated = true){
				if (val == mvIndicator){
					if (i<mvCountperColumn.length){
						mvCountperColumn[i]++ ;
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
				column = new DataTableCol( dataTable.size() );
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
	

	public double[] getDataTableColumn() {
		double[] columnData=null;
		return columnData;
	}
	
	public double[] getDataTableRow() {
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
		if (index > colcount){
			return numRowArrayList;
		}
		 
		
		for (int i=0;i<colcount;i++){
			val = dataTable.get(i).getValue(index) ;
			numRowArrayList.add( val);
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

	public String[] getColumnHeaders(){
		String[] headstr = new String[ columnHeaders.size() ];
		
		for (int i=0;i<headstr.length;i++){
			headstr[i] = columnHeaders.get(i) ;
		}
		return headstr ;
	}
	


	public void setOutPrn( PrintLog outprn){
		out = outprn;
	}
	
}

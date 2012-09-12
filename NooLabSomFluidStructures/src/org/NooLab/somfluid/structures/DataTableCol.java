package org.NooLab.somfluid.structures;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;


import org.NooLab.somfluid.components.MissingValues;
import org.NooLab.somfluid.data.ColumnDerivations;
import org.NooLab.somfluid.data.NveMapping;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatistics;
 


public class DataTableCol implements Serializable,
									 DataTableColIntf{

	private static final long serialVersionUID = 1223985229527744867L;

	// object references ..............

	transient DataTable parentTable;
	
	
	// main variables / properties ....
	
	int index ; // its column enum value, will be set on construction
	long serialID ; // monotonic increasing value, sth like a name
	
	int levelOfDerivation = 0; // important for organizing the recalculation of complicated trees
	int recalculationIndicator = 9;
	
	ArrayList<Double> cellValues = new ArrayList<Double>() ;
	
	ArrayList<String> cellValueStr = new ArrayList<String>() ;

	int rowcount;
	 
	private ColumnDerivations derivations;
	
	transient BasicStatisticalDescription statisticalDescription ;
	
	// only for columns that contain normalized data, i.e. which are member of a normalized table
	transient BasicStatisticalDescription rawDataStatistics ; 
	
	transient MissingValues missingValues;
	
	/**   */
	int dataFormat = -1;
	boolean isNumeric = false;
	boolean isIndexColumnCandidate = false;
	
	// a particular "transformation"
	int copyofColumn = -1;
	
	
	int maxScanRows = -1 ;
	boolean visibleOutput = true;
	
	public boolean hasHeader;
	
	
	// constants ......................
	
	
	// volatile variables .............
	int nveInstances;
	
	
	// helper objects .................
	transient StringsUtil strgutil ;
	transient PrintLog out  ;

	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	public DataTableCol( DataTable parent, int index  ){
		this.index = index ;
		
		parentTable = parent;
		out = parent.out ;
		strgutil = parent.strgutil ;
		
		statisticalDescription = new BasicStatisticalDescription(true);
		derivations = new ColumnDerivations ();
		missingValues = parentTable.missingValues ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	
	public DataTableCol( DataTable parent, DataTableCol inColumn) {
		
		cellValues = new ArrayList<Double>( inColumn.cellValues ) ;
		
		cellValueStr = new ArrayList<String>(inColumn.cellValueStr) ;
		
		index = inColumn.index;
		// derivations nveInstances


		derivations = new ColumnDerivations( inColumn.derivations);
		
		statisticalDescription = new BasicStatisticalDescription( inColumn.statisticalDescription); 
		
		 
		dataFormat = inColumn.dataFormat;
		isNumeric = inColumn.isNumeric;
		isIndexColumnCandidate = inColumn.isIndexColumnCandidate;
		maxScanRows = inColumn.maxScanRows ;
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	
	public void importColumn( DataTableCol inColumn, int mode ){
		int i,n1,n2,nm=0 ;
		String str ;
		
		dataFormat = inColumn.dataFormat;
		isNumeric = inColumn.isNumeric ;
		isIndexColumnCandidate = inColumn.isIndexColumnCandidate ;
		// all the compound stuff too 
		derivations = new ColumnDerivations(inColumn.derivations);
		hasHeader = inColumn.hasHeader ;
		
		n1 = inColumn.cellValues.size(); 
		if (n1==0){
			n1 = inColumn.cellValueStr.size();
		}
		n2 = this.size();
		
		nm = n2 ;
		
		if ((n1>0) && (n2<=0) && (mode==1)){
			mode=2;
		}
		if (n1 != n2){
			if (mode==1){
				nm = Math.min( n1,n2 ) ;
			}
			if (mode==2){
				nm = Math.max( n1,n2 ) ;
				if (n2<nm){
					this.setSize(nm) ;
				}
			}
		}
		int strDataOffset=0; 
		if (hasHeader){ strDataOffset=1;} ;
		
		i=0;
		nm = (nm+strDataOffset);
		
		int spSize = inColumn.getCellValueStr(false).size() ;
		int zpSize = inColumn.getCellValues().size() ;
		
		for (i=0;i<nm;i++){ // __FORMAT_TIME __FORMAT_DATETIME
			
			if (( isStringFormat(dataFormat)) || (isDateTimeFormat(dataFormat) ) || (inColumn.cellValues.size()==0)){
				
if (i>=1385){
	int zz;
	zz=0;
}
				str="";
				if (i< spSize){
					str = inColumn.getValue(i, "");
				}
				setValue(i, str) ; // str
			}
			
			double v = -1.0;
			if (i<zpSize ){  // num
				v = inColumn.getValue(i) ;
				setValue(i, v) ;
			}
			
		} // i -> all cells of column
		i=0;
		if (cellValues.size()>0){
			this.rowcount = cellValues.size() ;
		}else{
			if (cellValueStr .size()>0){
				this.rowcount = cellValueStr.size();
			}	
		}
	}
	
	private boolean isStringFormat( int dataformatid ){
		boolean rB ;
		
		rB = ((dataformatid>= DataTable.__FORMAT_ORDSTR ) );
		
		return rB;
	}
	
	private boolean isDateTimeFormat( int dataformatid ){
		boolean rB ;
		
		rB = ((dataformatid>= DataTable.__FORMAT_TIME) ) && (dataformatid <= DataTable.__FORMAT_DATETIME);
		
		return rB;
	}
	
	public void calculateBasicStatistics() {
		BasicStatistics basicst;
		// in a dedicated worker class
		basicst = new BasicStatistics( statisticalDescription, missingValues, cellValues );
		
		if (this.dataFormat<8){
			basicst.calculate();
		}else{
			statisticalDescription.setMvCount( this.size()) ;
		}
		
		// now we have an updated version of the container for statistical values that we
		// have provided ablove as "statisticalDescription"
	}

 
	/** just a linear normalization using the statistics   */
	public void normalize( double min, double max ) {
		
		double dv = -1.0;
		
		for (int i=0;i<cellValues.size();i++){
			if ((max-min)==0){
				dv = -1;
			}else{
				dv = ((double)cellValues.get(i) - min)/(double)(max-min);
			}
			if (cellValues.get(i) != -1.0){ // this treatment of missing cvalues needs to be corrected, using the global object
				cellValues.set(i, dv) ;
			}
		} // i->
		dv = 0.0;
	}

	public void applyNveRecodeMap(NveMapping nveMap) {
		// TODO Auto-generated method stub
		
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	
	public ArrayList<String> getStringsDiffList( boolean hasHeader, int maxDiffItems, int maxScanRows){
		
		ArrayList<String> sdl = new ArrayList<String>() ;
		
		int i,n ;
		String str ;
		
		int rcount = cellValueStr.size();
		int startix=0;
		if (hasHeader){
			startix=1;
		}
		if (maxScanRows>5){
			if (rcount>maxScanRows){
				maxScanRows = rcount ;
			}
		}
			
		
		for (i=startix;i<rcount ;i++){
			
			str = cellValueStr.get(i) ;
			
			if ( (str.length()>0) ){
				
				if (sdl.indexOf(str)<0){
					
					sdl.add(str) ;
					if (maxDiffItems>=2){
						if (sdl.size()>=maxDiffItems){
							break;
						}
					}
				}
			}
	
		}
	
		
		return sdl ;
	}

	public void applyFormulaStack(){
		
		
	}

 
	public BasicStatisticalDescription getStatisticalDescription() {
		return statisticalDescription;
	}

	public void setStatisticalDescription(
			BasicStatisticalDescription statisticalDescription) {
		this.statisticalDescription = statisticalDescription;
	}

	public int determineFormat(){
		return determineFormat(false);
		
	}

	public int determineFormat(boolean tableHasHeader){
		int formatIndicator = -1;
		
		int i,n ,rcount ;
		String str ;
		int emptyCells =0, dates=0, nums=0, strs=0, txts=0,intnums=0 ,minNumStrLen = 999;
		long minNum=999999999999999999L;
		
		ArrayList<String> strValueVariety = new ArrayList<String>();
		ArrayList<Double> numValueVariety = new ArrayList<Double>();
		
		boolean isNumericx , isMonotonic=true;
		
		double lastNum = 0, d,v,sumOfDifferences=0;
		// .   .   .   .   .   .   .   .   .   .   .   .   .   .   .   .   . 
		
		int startRowIndex = 0;
		if (tableHasHeader){
			startRowIndex =1;
		}
		rcount = cellValueStr.size();
		
		if (maxScanRows>5){
			if (maxScanRows<rcount){
				rcount = maxScanRows;
			}else{
				maxScanRows= rcount;
			}
		}
		
		int zd=0;
		for (i=startRowIndex;i<rcount ;i++){
			
			str = cellValueStr.get(i) ;
			
			
			
			if ( (str.length()==0) || ((str.contentEquals( DataTable.__MV_TEXTUAL )))){ // "M.V."
				emptyCells++ ;
				continue ;
			} 

			if (minNumStrLen > str.length()){
				minNumStrLen = str.length();
			}
			
			isNumericx = strgutil.isNumericX(str) ;
			if ( isNumericx == true){
				 
				nums++ ;
				
				v =  Double.parseDouble(str);
				if ((zd>=1) && (isMonotonic)){
					d = v - lastNum ;
					if (d<0){ isMonotonic = false; }
					sumOfDifferences = sumOfDifferences + d;
				}
				lastNum = v; zd++;	
				
				if (v == (Math.round(v))){
					intnums++;
				}
				
				if (minNum > v){
					minNum = (long) v;
				}

				// we count the variety of values
				if (numValueVariety.contains(v)==false){
					numValueVariety.add(v);
				}
				
				continue;
			} // end any num kind 
			
			str = new String(str.trim() ) ;
			
			if (strgutil.isDateX(str)){
				
				dates++ ;
				continue;
			}
			
			if ( (str.length()>0) ){
				
				if (str.indexOf(" ")>0){
					txts++ ;
				} else {
					// is it nearly a num, just 1 char not num?
					// nearNumStrs++
					strs++ ;
					
					// we count the variety of values
					if (strValueVariety.contains(str)==false){
						strValueVariety.add(str);
					}
				} // string ? could be yes/no etc...
				
			}
				 
		} // i-> all str values in column
	
		n = rcount; // cellValueStr.size() ;
		formatIndicator = -1;
		
		if (n == (emptyCells+nums+startRowIndex) ){
			formatIndicator = DataTable.__FORMAT_NUM ; // 1
			
			d = Math.abs(sumOfDifferences/(zd-1) ) ;
			d = d - 1.0;
			if ( d <= (0.001/rcount) && (isMonotonic)){
				formatIndicator = DataTable.__FORMAT_ID; // 0 ;
			}else{
				if (isMonotonic){
					if ((numValueVariety.size()==nums) && (nums == intnums)){
						formatIndicator = DataTable.__FORMAT_ID; // 0 ;
					}
				}
			}
			
			if ((nums == intnums) && (formatIndicator>0)){
				formatIndicator = DataTable.__FORMAT_INT; // 2 ;
				if (minNumStrLen>=5){
					formatIndicator = DataTable.__FORMAT_ORGINT ;	
				}
			}
			
			if (numValueVariety.size()==2){
				formatIndicator = DataTable.__FORMAT_BIN;
			}
		} // some kind of num
		
		if (formatIndicator<0){
			if ((dates + emptyCells + startRowIndex == n)) {
				formatIndicator = DataTable.__FORMAT_DATE; // 4;
			}
		}
		if (formatIndicator<0){
			
			if ((strs > 0) && (txts == 0)) {
				formatIndicator = 8;
			}
			else{
				if ((strs > 0) && (txts > 0)) {
					formatIndicator = DataTable.__FORMAT_TXT; // 9;
				}else{
					formatIndicator = DataTable.__FORMAT_STR;// 8;
					
					if (strValueVariety.size()==2){
						formatIndicator = DataTable.__FORMAT_BINSTR;
					}else{
						if ((strValueVariety.size()>2) && (strValueVariety.size()< DataTable._MAX_ORDSTR_VARIABILITY )){
							formatIndicator = DataTable.__FORMAT_ORDSTR ;
						}
					}
				}
			}
		}
		
		strValueVariety.clear();
		numValueVariety.clear();
		
		return formatIndicator;
	}

 
	
	/**
	 * if successful, we change from  __FORMAT_BINSTR=13  to __FORMAT_BIN=5
	 * if more than 2 entries, we change to ordinal __FORMAT_ORD=3
	 * 
	 * @param tableHasHeader
	 */
	public int recodeBinaryEntries( boolean tableHasHeader) {
		int candidateFormat = -1;
		
		int cs,hoffset=0,ix,le;
		String str ;
		double v;
		IndexedDistances ixds = new IndexedDistances ();
		IndexDistance ixd; 
		// measure the entries into an array list, 
		//   if num, take the smaller a 0 the larger as 1
		//   if str, check for y,j,d,s <-> n, n->0, other as 1
		try{
		
			cs = cellValueStr.size();
			if (tableHasHeader){
				hoffset=1;
			}
			// first checking 
			for (int i=0+hoffset;i<cs;i++){ // make multi-digest, if there are a lot of records (>1000)
				
				str = cellValueStr.get(i).trim() ;
				if (str.length()>0){
					ix = ixds.getIndexByStr(str);
					if (ix<0){
						ixd = new IndexDistance(i,1.0,str);
					}else{
						ixd = ixds.getItem(ix) ;
						ixd.setDistance( ixd.getDistance() + 1.0);
					}
				}
			} // i->
			
			ixds.sort(-1) ;
			
			if (ixds.size()==2){
				candidateFormat = DataTable.__FORMAT_BIN;
			}
			if (ixds.size()>2){
				candidateFormat = DataTable.__FORMAT_ORD;
			}
				
			/*
				if ( (int)Math.round(v) != v ){
					v = ix;
				}
				
			*/
			
			for (int i=0+hoffset;i<cs;i++){ // make multi-digest, if there are a lot of records (>1000)
				
				str = cellValueStr.get(i).trim() ;
				if (str.length()>0){
					ix = ixds.getIndexByStr(str) ;
					if (ix>=0){
						v = ixds.getItem(ix).getDistance();
						
					}else{
						v=-1.0;
					}
				}else{
					v=-1.0;
				}
				if (i-hoffset>=cellValues.size()){ 
					cellValues.add(v);
				}else{
					setValue(i-hoffset,v) ;
				}
			} // ->

			le = cellValues.size();

		}catch(Exception e){
			e.printStackTrace();
		}
		
		return candidateFormat ;
	}

	/**
	 * 
	 * translates a column of strings into a column of values;<br/>
	 * 
	 * if the column of strings is not completely numeric (besides m.v.), then
	 * a numerical values enumeration will be applied
	 * 
	 * @return
	 */
	public int makeNumeric( boolean hasHeader ){  
		int i , r= -1,cs=-1 ;
		String str ;
		double val ;
		boolean isNumAll = true ;
		 
		// cellValues.setSize( cellValueStr.size() ) ;
	
		try{
			int hoffset = 0; 
			if (hasHeader){
				hoffset =1;
			}
			cs = cellValueStr.size();
			
			// first checking 
			for (i=0+hoffset;i<cs;i++){ // make multi-digest, if there are a lot of records (>1000)
				
				str = cellValueStr.get(i) ;
				
				if ( (str.length()==0) ||  (str.toLowerCase().contentEquals(DataTable.__MV_TEXTUAL.toLowerCase()))
						                                             ){
					str = "-1.0" ;
				}
				
				if (strgutil.isNumericX(str) == false){
					isNumAll = false ;
					nveInstances++ ;
				}
			}
			
			 
			i=0;
			for (i=0+hoffset;i<cellValueStr.size();i++){
				str = cellValueStr.get(i) ;
				
				if ( (str.length()==0) || 
					 (str.toLowerCase().contentEquals(DataTable.__MV_TEXTUAL.toLowerCase()))
					                                             ){
					val= -1.0 ;
				} else {
					if (strgutil.isNumericX(str) == false){
						val = -1.0;
					}else{
						val = Double.parseDouble(str) ;
					}
					
				}
				setValue(i-hoffset,val) ;
			}
			r = this.cellValues.size();
			 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return r ;
	}

	/**
	 * 
	 * returns the first index column
	 * @param lastColindex
	 * @return
	 */
	public int isIndexColumnCandidate( int colindex ){
		int ixcol = -1;
		
		
		
		return ixcol ;
	}
	
	public void setAsIndexColumnCandidate(boolean flag) {
		 
		isIndexColumnCandidate = flag;
	}
 
	
	public void setFormat(int formatid){
		if ((formatid>= DataTable.__FORMAT_TIME) ){
			// TODO copy everything into the cellValuesStr...
		}
		dataFormat = formatid;
		
	}

	public void setMaxScanRows( int maxscanrows) {
		 if (this.size()<maxscanrows){
			 maxscanrows = size()-2;
		 }
		this.maxScanRows = maxscanrows;
	}

	public void reset( int resetVal ){
		
		for (int i=0;i<cellValues.size();i++){
			
			this.setValue(i, resetVal) ;
		} // i -> all cells of column
		
	}
	public void reset( String resetStr ){
		
		for (int i=0;i<cellValueStr.size();i++){
			
			this.setValue(i, resetStr) ; 
		} // i -> all cells of column
		
	}

	public void resetAll( int resetVal, String resetStr ){
		
		reset( resetVal ) ;
		reset( resetStr ) ;
	}
	
	
	public void addValue( double value){
		
		cellValues.add(value) ;
		rowcount = cellValues.size() ;
	}
	
	public void addValueStr( String valstr){
		
		cellValueStr.add(valstr) ;
		this.rowcount = cellValueStr.size() ;
	}
	
	public ArrayList<Double> getCellValues() {
		return cellValues;
	}

	public double getValue( int index ){
		double val = -1;
		
		if ((index>=0) && (index<cellValues.size() )){
			val = cellValues.get(index)  ;
		}
		return val ;
	}

 

	public String getValue( int index , String str){
		return cellValueStr.get(index) ;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T  getValue( int index , Class<T> clazz){
		
		String cn = clazz.getSimpleName() ;
		T obj;
		
		
		if (cn.toLowerCase().contentEquals("string")){
			if ((cellValueStr.size()==0) || (index>=cellValueStr.size())){
				obj = (T) "";
			}else{
				obj = (T) cellValueStr.get(index);
			}
		}else{
			if ((cellValues.size()==0) || (index>=cellValues.size())){
				obj = (T)((Double)(-1.0));
			}else{
				obj = (T) cellValues.get(index)  ;
			}
		}
		return  obj;
	}
	
	
	public void setValue( int index, double value){
		
		try{
			
			if (index >= cellValues.size()){
				cellValues.add(value) ;
			}else{
				cellValues.set(index, value) ;
			}	
			
		}catch(Exception e){
			e.printStackTrace();
		}
		 
	}

	public void setValue( int index, String value){
		
		if (index >= cellValueStr.size()){
			cellValueStr.add(value) ;
		}else{
			cellValueStr.set(index, value) ;
		}
	}
	
	public void remove( int index){
		
		rowcount = cellValues.size() ;
	}	
	
	public void insert( int index, double value){
		
		cellValues.set(index, value) ;
		rowcount = cellValues.size() ;
	}
	
	public void setSize( int size){
		// cellValues.setSize( size) ;
		rowcount = cellValues.size() ; // ??? 
	}	

	public void setSize( int size, String str){

		// cellValueStr.setSize( size) ;
		rowcount = cellValueStr.size() ;
	}
	
	public void setSizeAll( int size){
		// cellValues.setSize( size) ;
		// cellValueStr.setSize( size) ;
		rowcount = cellValues.size() ;
	}	
	
	
	public int size( ){
		int n=-1;
		if (isNumeric==true){
			n = cellValues.size() ;
		}else{  int offset=0; if (hasHeader)offset=1;
			n = Math.max(0,cellValueStr.size()-offset); // ?????????????? if there is a header ... 
		}
		return n;
	}

	public int getSize( ){
		int n=-1;
		if (this.isNumeric){
			n = cellValues.size() ;  
		}else{
			n = cellValueStr.size() ;
		}
		return n;
	}
	
	
	public int getSize(String str ){
		
		return cellValueStr.size() ;
	}
	
	
	public int getDataFormat() {
		return dataFormat;
	}

	public boolean isNumeric() {
		return isNumeric;
	}

	public boolean isIndexColumnCandidate() {
		return isIndexColumnCandidate;
	}

	public int getNveInstances() {
		return nveInstances;
	}

	public ColumnDerivations getDerivations() {
		return derivations;
	}

	public boolean isHasHeader() {
		return hasHeader;
	}

	public int getCopyofColumn() {
		return copyofColumn;
	}

	public boolean isVisibleOutput() {
		return visibleOutput;
	}

	public void setOut(PrintLog out) {
		this.out = out;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getSerialID() {
		return serialID;
	}

	public void setSerialID(long serialID) {
		this.serialID = serialID;
	}

	public int getLevelOfDerivation() {
		return levelOfDerivation;
	}

	public void setLevelOfDerivation(int levelOfDerivation) {
		this.levelOfDerivation = levelOfDerivation;
	}

	public int getRecalculationIndicator() {
		return recalculationIndicator;
	}

	public void setRecalculationIndicator(int recalculationIndicator) {
		this.recalculationIndicator = recalculationIndicator;
	}

	public ArrayList<String> getCellValueStr( ) {
		return getCellValueStr( false );
	}
	
	public ArrayList<String> getCellValueStr(boolean removeHeader ) {
		ArrayList<String> cvs = new ArrayList<String>();
		
		if ((cellValueStr!=null) && (cellValueStr.size()>0)){
			cvs.addAll( cellValueStr) ;
			if (removeHeader){
				cvs.remove(0) ; cvs.trimToSize();
			}
		}
		return cvs;
	}

	public void setCellValueStr(ArrayList<String> cellValueStr) {
		this.cellValueStr = cellValueStr;
	}

	public int getRowcount() {
		return rowcount;
	}

	public void setRowcount(int rowcount) {
		this.rowcount = rowcount;
	}

	public int getMaxScanRows() {
		return maxScanRows;
	}

	public PrintLog getOut() {
		return out;
	}

	public void setCellValues(ArrayList<Double> cellvalues) {
		cellValues.clear();
		
		if ((cellvalues!=null) && (cellvalues.size()>0)){
			cellValues.addAll(cellvalues);
		}
		
	}
 

	public void setDerivations(ColumnDerivations derivations) {
		this.derivations = derivations;
	}

	public void setDataFormat(int dataFormat) {
		this.dataFormat = dataFormat;
	}

	public void setNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
	}

	public void setIndexColumnCandidate(boolean isIndexColumnCandidate) {
		this.isIndexColumnCandidate = isIndexColumnCandidate;
	}

	public void setCopyofColumn(int copyofColumn) {
		this.copyofColumn = copyofColumn;
	}

	public void setVisibleOutput(boolean visibleOutput) {
		this.visibleOutput = visibleOutput;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	public void setRawDataStatistics( BasicStatisticalDescription statsDescr ) {
		 
		rawDataStatistics = new BasicStatisticalDescription( statsDescr );
	}

	public boolean getIsEmpty(double percentThreshold) {
		// TODO Auto-generated method stub
		
		return false;
	}

	public void setNveInstances(int nveInstances) {
		this.nveInstances = nveInstances;
	}



	
	
}

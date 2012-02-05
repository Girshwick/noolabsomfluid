package org.NooLab.somfluid.data;

import java.util.ArrayList;

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;


import org.NooLab.somfluid.util.Formula;




public class DataTableCol {

	// object references ..............

	// TODO: reference to global missing value object,
	//       which can hold many missing value translations
	
	
	
	// the object for nominal values enumeration, a reference from data table
	NomValEnum nve = null ;
	
	// main variables / properties ....
	
	int index ; // its column enum value, will be set on construction
	
	int rowcount;
	
	// double[] cellvalue ; 
	
	ArrayList<Double> cellValues = new ArrayList<Double>() ;
	
	ArrayList<String> cellValueStr = new ArrayList<String>() ;
	
	int dataFormat = -1;
	boolean isNumeric = false;
	boolean isIndexColumnCandidate = false;
	
	int copyofColumn;
	int maxScanRows = -1 ;
	

	
	boolean visibleOutput = true;
	
	
	
	
	// constants ......................
	
	
	// volatile variables .............
	int nveInstances;
	
	
	// helper objects .................
	StringsUtil strgutil = new StringsUtil();
	 
	PrintLog out  ;

	private ColumnDerivations derivations;

	public boolean hasHeader;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	
	public DataTableCol(int index){
		this.index = index ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	
	public void importColumn( DataTableCol inColumn, int mode ){
		int i,n1,n2,nm=0 ;
		
		this.dataFormat = inColumn.dataFormat;
		this.isNumeric = inColumn.isNumeric ;
		this.isIndexColumnCandidate = inColumn.isIndexColumnCandidate ;
		// all the compund stuff too 
		this.derivations = new ColumnDerivations(inColumn.derivations);
		
		n1 = inColumn.cellValues.size();
		if (n1==0){
			n1 = inColumn.cellValueStr.size();
		}
		n2 = this.size();
		
		nm = n2 ;
		
		if ((n1>0) && (n2==0) && (mode==1)){
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
		
		i=0;
		for (i=0;i<nm;i++){
			if ((dataFormat>=8) || (inColumn.cellValues.size()==0)){
				this.setValue(i, inColumn.getValue(i, "")) ; // str
			}
			if (i< inColumn.getCellValues().size()){
				this.setValue(i, inColumn.getValue(i)) ;     // num
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

	public void recodeBinaryEntries(boolean tableHasHeader) {
		// TODO Auto-generated method stub
		
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



	public NomValEnum getNve() {
		return nve;
	}

	public int determineFormat(){
		return determineFormat(false);
		
	}

	public int determineFormat(boolean tableHasHeader){
		int formatIndicator = -1;
		
		int i,n ,rcount ;
		String str ;
		int emptyCells =0, dates=0, nums=0, strs=0, txts=0 ;
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
			
			if ( (str.length()==0) ){
				emptyCells++ ;
				continue ;
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
				
				continue;
			} 
			
			if (strgutil.isDateX(str)){
				
				dates++ ;
				continue;
			}
			
			if ( (str.length()>0) ){
				
				if (str.indexOf(" ")>0){
					txts++ ;
				} else {
					strs++ ;
				}
					
				
			}
				 
		} // i-> all str values in column
	
		n = rcount; // cellValueStr.size() ;
		formatIndicator = -1;
		
		if (n == (emptyCells+nums+startRowIndex) ){
			formatIndicator = 1 ;
			
			d = Math.abs(sumOfDifferences/(zd-1) ) ;
			d = d - 1.0;
			if ( d <= (0.001/rcount) && (isMonotonic)){
				formatIndicator = 0 ;
			}
		}
		
		if (formatIndicator<0){
			if ((dates + emptyCells + startRowIndex == n)) {
				formatIndicator = 4;
			}
		}
		if (formatIndicator<0){
			
			if ((strs > 0) && (txts == 0)) {
				formatIndicator = 8;
			}
			else{
				if ((strs > 0) && (txts > 0)) {
					formatIndicator = 9;
				}else{
					formatIndicator = 8;
				}
			}
		}
		
		return formatIndicator;
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
		int i , r= -1 ;
		String str ;
		double val ;
		boolean isNumAll = true ;
		 
		// cellValues.setSize( cellValueStr.size() ) ;
	
		try{
			int hoffset = 0; 
			if (hasHeader){
				hoffset =1;
			}
			// first checking 
			for (i=0+hoffset;i<cellValueStr.size();i++){
				
				str = cellValueStr.get(i) ;
				
				if ( (str.length()==0) ||  (str.toLowerCase().contentEquals("m.v."))
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
					 (str.toLowerCase().contentEquals("m.v."))
					                                             ){
					val= -1.0 ;
				} else {
					if (strgutil.isNumericX(str) == false){
						val = -1.0;
					}else{
						val = Double.parseDouble(str) ;
					}
					
					setValue(i-hoffset,val) ;
				}
				
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
		if ((formatid>=8) && (dataFormat<=2)){
			// TODO copy everything into the cellValuesStr...
		}
		dataFormat = formatid;
		
	}

	public void setMaxScanRows( int maxscanrows) {
		 
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
		
		if (cn.contentEquals("String")){
			obj = (T) cellValueStr.get(index);
		}else{
			obj = (T) cellValues.get(index)  ;
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
		rowcount = cellValues.size() ;
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
		}else{
			n = cellValueStr.size();
		}
		return n;
	}

	public int getSize( ){
		int n=-1;
		if (this.isNumeric){
			cellValues.size() ;  
		}else{
			cellValueStr.size() ;
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

	
	
}
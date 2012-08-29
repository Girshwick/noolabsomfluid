package org.NooLab.utilities.files;

import java.util.Vector;

import org.NooLab.utilities.strings.*;
import org.NooLab.chord.*;



/**
 * 
 * this class organizes multi-threaded parsing of tables, with any number of threads;<br/>
 * first, it reads the line, and then it sends it to a parallel
 * parsing process;<br/><br/>
 * 
 * Thus it is more suitable for files where the product RxC is large (>50'000);<br/> <br/>
 * 
 * 
 * 
 */
public class FastFileReader {

	Vector<double[]> table = null;
	
	int threadcount = 4;
	
	String effectiveSeparator , separator = "\t";
	
	DFutils filutil = new DFutils ();
	ArrUtilities arrutil = new ArrUtilities();
	StringsUtil strgutil = new StringsUtil() ;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public FastFileReader(){
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	
	// that's the connecting callback
	private void performParsingtoDouble( String textrow, int id){
		double[] values ;
		String[] strvalues ;
		
		strvalues = textrow.split( effectiveSeparator ) ;
		values = new double[strvalues.length] ;
		
		for (int i=0;i<strvalues.length;i++){
			values[i] = Double.parseDouble(strvalues[i]) ;
		}
		
		// the elements of the collection are already prepared
		table.set(id, values) ;
		
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	
	public Vector<double[]> readValuesTablefromFile( String filename){
		
		readFile2ValuesTable(filename);
		
		
		return table ;
	}
	
	
	public Vector<double[]> getValuesTable(){
		return table ;
	}
	
	@SuppressWarnings("unused")
	private void detectSeparator( Vector<String> textrows){
		int cc;
		
		cc = strgutil.getColumnsCount(textrows,8);
		
		effectiveSeparator = separator;
	}
	
	
	public int readFile2ValuesTable( String filename ){
		int r ;
		
		r = readFile2ValuesTable( filename, true) ;
		
		return r; 
	}

	public int readFile2ValuesTable( String filename, String separator ){
		int r ;
		
		effectiveSeparator = separator;
		r = performValuesTable( filename, separator ) ;
		
		return r; 
	}
	
	
	public int readFile2ValuesTable( String filename, boolean detectSeparator ){
	 
		int r=-1;
	 	 
		r = performValuesTable( filename,  ""); // "" cause a detection process
	 	
		return r;
	}
	
	
	private int performValuesTable( String filename, String separator ){
 
		int resultState=-1;
		 
		String str ;
		Vector<String> textrows ;
		
		double[] emptyarr  ; 
		
		ParseDigester digester ;
		
		// .............................................
		     
		// reading the file into a string collection
		textrows = filutil.readFileintoVectorstringTable( filename ) ;
		
		if (separator.length()==0){
			detectSeparator(textrows) ;
		
			str = strgutil.getEffectiveSeparator() ; 
			if (str.length()>0){
				effectiveSeparator = str ;
			}
		}
		 
 		// create the table: empty but all rows present
		table = new Vector<double[]>();
		
		emptyarr = new double[0]; // cc ?
		
		for (int i=0;i<textrows.size();i++){
			table.add( emptyarr );
 		}
		
		// then send it to the parsing routine via the multi-digester
		digester = new ParseDigester();
		
		digester.digestingStrings(textrows, threadcount) ;
		
		return resultState ;
	}
	
	
	public Vector<double[]>  readFile2ValueColumns( String filename ){
		
		Vector<double[]> table = null;
		
		
		return table ;
	}
	
	
	public Vector<String[]>  readFile2StringTable( String filename ){
		
		Vector<String[]> table = null;
		
		
		return table ;
	}

	public Vector<String[]>  readFile2StringColumns( String filename ){
		
		Vector<String[]> table = null;
		
		
		return table ;
	}	
	
	
	
	
	
	// ========================================================================
	class ParseDigester implements IndexedItemsCallbackIntf{
		 
		MultiDigester digester ; 
		 
		Vector<String>  rowText ;
		
		
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
		public ParseDigester(){
			
		}
		
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
		
		protected void digestingStrings( Vector<String> rowtext, int threadcount){ 
			 
			
			// providing also right now the callback address (=this class)
			// the interface contains just ONE routine: perform()
			digester = new MultiDigester(threadcount, (IndexedItemsCallbackIntf)this ) ; 
			 
			rowText = rowtext ;
			
			// note, that the digester need not to know "anything" about our items, just the amount of items
			// we would like to work on.
			// the digester then creates simply an array of indices, which then point to the actual items,
			// which are treated anyway here (below) !
			digester.prepareItemSubSets( rowText.size(),0 );
			 
			  
			digester.execute() ;
			
			// the digester itself waits until all threads have been completed
			digester = null;
		}
		
		 
		// this will be called back, the multi-threaded digester selects an id, which is called from within one of the threads
		// the processID is just for fun... (and supervision)
		public void perform( int processID, int id ) {
			String rowString ;

			rowString = rowText.get(id);
			// System.out.print("called back to <extracting> document, id ="+id ) ;
			
			
			// thats the call out to the parent class
			if ( rowString != null){
				
				// select routine based on parameter: -> String[], or double[] 
				
				performParsingtoDouble( rowString, id ) ;
				
			}
			
		}

		@Override
		public int getClosedStatus() {
			// 
			return 0;
		}
 
	} // inner class Digester
	
	 
}

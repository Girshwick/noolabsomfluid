package org.NooLab.math3.util;





import java.io.*;

import java.util.*;

import org.NooLab.math3.stat.MissingValueIntf;
import org.NooLab.math3.stat.clustering.EuclideanDoublePoint;


 


//import org.NooLab.utilities.xml.XmlFileRead;

/**
 * 
 * reading raw data into a table "TABLE" vector&lt;String&gt;
 * 
 * the raw data table will be used by the transforming instance
 * 
 */
public class RawFileData {
 

	MissingValueIntf mv ;

	int maxColumnCount;
	int maxRecordCount = -1;

	ArrayList<String> variables = new ArrayList<String>();
 

	boolean dataAvailable = false;

	// read mode = random, serial, block (begin, end) ?

  

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public RawFileData(MissingValueIntf missingValueIntf ) {
		mv = missingValueIntf ;
	}

	public EuclideanDoublePoint[] readfromFile( File srcFile, boolean columnHeaders) {

		EuclideanDoublePoint[] points = null;
		
		int record_counter = 0, sIDextend = 0, z = 0, j, return_value = -1;
		int vectorsize, datavectorsize, linecount, stepwidth;
		long id;
		File file;
		boolean IDcol_present = true, columnHeadersPresent=columnHeaders;
		String text;
		BufferedReader reader = null, reader0;
		FileReader fileReader;

		String cs, _id_rnum_str = "", hs1, hs2;
		String[] celldata = null;
 

		id = 0;
		return_value = -4;

		file = srcFile;

		return_value = -5;
		try {
			return_value = -6;

			fileReader = new FileReader(file);
			// reader = new BufferedReader( fileReader );
			reader0 = new BufferedReader(fileReader);

			text = null;

			return_value = -7;

			
			 
			
			// reading the first row
			text = reader0.readLine();
			// column headers should be read and stored !

			if (text.indexOf("\t") > 0) {
				celldata = text.split("\t");
			} else {
				if (text.indexOf(" ") > 0) {
					text = text.replace("  ", " ");
					celldata = text.split(" ");
				}

			}
 
			vectorsize = celldata.length;
			datavectorsize = vectorsize;

			if (columnHeaders){
				for (j = 0; j < vectorsize; j++) {
					if ((celldata != null) && (celldata[j] != null)) {
						cs = celldata[j];
					} else {
						cs = "col" + String.valueOf(j);
					}
	 				variables.add(cs);
				}
			}
			

			linecount = 0;
			while ((text = reader0.readLine()) != null) {
				linecount++;
			}
			stepwidth = 1 + (linecount / 10);

			points = new EuclideanDoublePoint[linecount ] ;

			 
			record_counter = 0;

			if (reader0 != null)
				reader0.close();
			fileReader.close();

			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);
			z = 0;
			 
			
			while (((text = reader.readLine()) != null) && (record_counter<=points.length)){
				
				record_counter++;
				if ((record_counter<=1) && (columnHeadersPresent)){ continue;}

				celldata = text.split("\t");

				if (vectorsize != celldata.length) {
					continue;
				}

				convertInputStrings( celldata, points, z ) ;

				
				if ((maxRecordCount > 0) && (record_counter >= maxRecordCount)) {
					break;
				}
				z++;
			} // while not eofile

			 
			
			 

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			 
		} catch (IOException e) {
			e.printStackTrace();

			 
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				 
			}
		}
		return points;
	}

	private void convertInputStrings( String[] celldata,
									  EuclideanDoublePoint[] points, 
									  int z) {
		
		int vectorsize = celldata.length ;
		
		double[] rowValues = new double[vectorsize];
		double numvalue;
		
		for (int i=0;i<vectorsize;i++){
			
			try{

				String str = celldata[i];
				if (str.length()==0){
					str = ""+mv.getValue() ;
				}
				numvalue = Double.parseDouble(str) ;
				
			}catch(Exception e){
				numvalue = mv.getValue() ;
			}
			rowValues[i] = numvalue;
		}
		
		
		points[z] = new EuclideanDoublePoint( rowValues );

		
	}

	public String[] getHeaders() {
		
		String[] colheaders = new String[0];
		
		if ((variables!=null) & (variables.size()>0)){
			colheaders  = new String[variables.size()];
			for (int i=0;i<variables.size();i++){
				colheaders[i] = variables.get(i) ;
			}
		}
		
		return colheaders;
	}

 

}





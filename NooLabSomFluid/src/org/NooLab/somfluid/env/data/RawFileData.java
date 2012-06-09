package org.NooLab.somfluid.env.data;





import java.io.*;

import java.util.*;


import org.NooLab.utilities.strings.StringsUtil;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.Variable;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.FileDataSource;

import org.NooLab.utilities.logging.PrintLog;


//import org.NooLab.utilities.xml.XmlFileRead;

/**
 * 
 * reading raw data into a table "TABLE" vector&lt;String&gt;
 * 
 * the raw data table will be used by the transforming instance
 * 
 */
public class RawFileData {

	// object references ..............
	SomDataObject somData;
	FileDataSource filesource;

	// main variables / properties ....

	int maxColumnCount;
	int maxRecordCount = -1;

	Vector<Variable> variables = new Vector<Variable>();

	DataTable datatable ;

	boolean dataAvailable = false;

	// read mode = random, serial, block (begin, end) ?

	// volatile variables .............

	// helper objects .................
	StringsUtil strgutil = new StringsUtil();

	ArrUtilities utils = new ArrUtilities();

	PrintLog out = new PrintLog(2,true) ;

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public RawFileData( SomDataObject somdata, PrintLog outprn) {
		
		somData = somdata;
		datatable = new DataTable( somData, true); 		 
		out = outprn;
	}

	public int readRawDatafromFile(String filename) {

		int record_counter = 0, sIDextend = 0, z = 0, j, return_value = -1;
		int vectorsize, datavectorsize, linecount, stepwidth;
		long id;
		File file;
		boolean IDcol_present = true;
		String text;
		BufferedReader reader = null, reader0;
		FileReader fileReader;

		String cs, _id_rnum_str = "", hs1, hs2;
		String[] celldata = null;
		ArrayList<String> observedColumns = new ArrayList<String>(); 
		
		Variable var;

		datatable.setOutPrn(out);

		id = 0;
		return_value = -4;

		file = new File(filename);

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

			return_value = -8;
			vectorsize = celldata.length;
			datavectorsize = vectorsize;

			for (j = 0; j < vectorsize; j++) {
				if ((celldata != null) && (celldata[j] != null)) {
					cs = celldata[j];
				} else {
					cs = "col" + String.valueOf(j);
				}

				String csp = cs.substring(0,1);
				if (strgutil.isNumericX(cs.substring(0,1))){
					cs = "col_"+cs;
				}
				cs = strgutil.cleanLabelFromLocales(cs);
				
				int cz=1;
				csp = cs;
				while (observedColumns.indexOf(csp)>=0){
					csp = cs+cz;
					cz++;
				}
				cs=csp;
				
				celldata[j] = cs;
				var = new Variable();
				var.setLabel(cs);

				variables.add(var);
				observedColumns.add(cs);
			}

			linecount = 0;
			while ((text = reader0.readLine()) != null) {
				linecount++; // note that these are rows except the header !!! total rowcount  = linecount + 1 !!!
			}
			stepwidth = 1 + (linecount / 10);

			datatable.opencreateTable(celldata);
			datatable.setNumeric(false);

			return_value = -10;
			record_counter = 0;

			if (reader0 != null)
				reader0.close();
			fileReader.close();

			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader); // creating a new reader in order to start from the beginning
			z = 0;
			celldata = new String[0] ;
											out.print(2, "reading " + linecount + " lines from file: "+filename );
			
			while ((text = reader.readLine()) != null) {
				//
											if (((z>1) && (celldata.length *linecount>120000)) || 
												((z<=1) && (celldata.length *linecount>120000))){
												out.printprc(2, z, linecount, linecount/10, "of import");
											}
				return_value = -11;

				celldata = text.split("\t");

				datatable.setRow(celldata);

				if (vectorsize != celldata.length) {
					// this should NOT throw an error !!!
					// System.out.println("VECTOR_SIZE!=arr.length-2");
					// return;
					/*
					 * instead, the data should be read anyway, - if vectorsize
					 * > arr.length, then vectorsize should be adopted , - if
					 * vectorsize < arr.length, then arr.length should be
					 * adapted
					 */
					if (vectorsize > celldata.length) {
						vectorsize = celldata.length;
					}
					;
					if (vectorsize < celldata.length) {
						// ??? how to do that ?

					}

				}

				return_value = -12;

				record_counter = record_counter + 1;
				if ((maxRecordCount > 0) && (record_counter >= maxRecordCount)) {
					break;
				}
				z++;
			} // while not eofile

			return_value = -20;

			datatable.setSourceFileName( filename );
			
			return_value = 0;

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
		return return_value;
	}

	public Variable getVariable(String label) {
		Variable var = null;

		try {

		} catch (Exception e) {

		}

		return var;
	}

	public Variable getVariable(int index) {
		Variable var = null;

		return var;

	}

	// ------------------------------------------------------------------------
	public DataTable getDataTable() {

		return datatable;

	}

}





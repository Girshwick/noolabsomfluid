package org.NooLab.somfluid.app.astor.stream;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.NooLab.itexx.comm.intf.SomTexxObservationIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.astor.SomAstorFrameIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.util.BasicStatisticalDescription;



/**
 * 
 * organizes the collection of data into the current table structure,
 * including using the SomTransformer for online transformation
 * 
 * sources are DB, file
 * 
 * it reads periodically the sources, or it gets informed (as observer) by 
 * instances like Texx (through objects that are formatted as SomTexxObservationIntf), 
 * which are able to create a randomgraph vector,  
 *
 * SomDataStreamer maintains 
 * 
 */
public class SomDataStreamer 
								extends 
											Observable 
								implements 
											SomTexxObservationIntf, 
											SomDataStreamerIntf {

	
	SomAstorFrameIntf  somAstorFrame ; // points to SomAssociativeStorage
	SomFluidProperties sfProperties ;
	
	private SomDataObject somDataObj;
	private int firstUseRowinTable;
	private DataTable dataTable;
	
	
	
	
	// ========================================================================
	public SomDataStreamer( SomAstorFrameIntf somastor, SomFluidProperties sfProps) {
		
		somAstorFrame = somastor ;
		sfProperties = sfProps;
		
		somDataObj = somAstorFrame.getSomDataObj() ;
		
	}
	// ========================================================================
	
	
	
	
	
	
	
	// ----------------------------------------------------
	class Receiver implements Observer{

		
		@Override
		public void update(Observable arg0, Object arg1) {
			// this comes from any kind of trigger that connects to external sources (other than a database)...
			// we collect it 
		}
		
		
	} // class Receiver
	
	
	public void addData(DataTable inDataTable) {
		
		int n, offset=0;
		String mainColHeader , colHeader;
		
		ArrayList<ArrayList<Double>> inDataRows ;
		ArrayList<DataTableCol> inColumns, columns ;
		ArrayList<String> inColHeaders,colHeaders ;
		
		DataTableCol inColumn, column;
		Variables variables;
		
		
		
		inDataRows  = inDataTable.getDataTableRows() ;
		
		// TODO: is there a threshold in sfProperties for the minimal number of records allowed to add ?
		n = inDataRows.size();
		if (n==0){
			return;
		}

		if (dataTable==null){
			dataTable = somDataObj.getData() ; 
		}
		
		variables = somDataObj.getVariables() ;
		inColumns = inDataTable.getDataTable() ;
		inColHeaders = inDataTable.getColumnHeaders() ;
		colHeaders = dataTable.getColumnHeaders() ;
		
		// what about maxima, minima of raw data in master table, to which we join the inDataTable
		columns = dataTable.getDataTable();
		
		dataTable.getSomData().calculateStatisticalDescription();
		
		double _max,_min,_mean,in_mean, in_max,in_min;
		int in_n;
		boolean hb;
		BasicStatisticalDescription bsd ,in_bsd;
		
		
		// note that we have to check the indices of the column by their headers,...these could be different !!!
		for (int i=0;i<columns.size();i++){
			mainColHeader = colHeaders.get(i);
			column = columns.get(i);
			
			int ix = inColHeaders.indexOf(mainColHeader) ;
			if (ix>=0){
				
				colHeader = inColHeaders.get(ix);
				hb = true;
				
				if (variables.getAbsoluteFieldExclusions().indexOf(colHeader)>=0){ hb=false;}
				if (column.isIndexColumnCandidate()){ hb=false;};
				
				if (column.isNumeric()==false){ hb=false;};
				if (variables.getExcludedNormalization().indexOf(colHeader)>=0){ hb=false;}
				
				inColumn = inColumns.get(i);
				
				if (hb){
					
					inColumn.calculateBasicStatistics() ;
					
					bsd = column.getStatisticalDescription();
					in_bsd = inColumn.getStatisticalDescription();
					_max = bsd.getMaxi();
					_min = bsd.getMini(); 
					
					_mean   = bsd.getMean();
					in_mean = in_bsd.getMean() ;
					 
					in_max = in_bsd.getMaxi() ;
					in_min = in_bsd.getMini();
					
				}// statistics needed?
				
				double v;
				String str;

				
				if ((column.size()==0) && (dataTable.rowcount()>3)){
					continue;
				}
				
				if (offset==0){
					offset = column.size();
				}else{
					if (offset>column.size()){
						offset=column.size();
					}
				}
				
				// transfer values
				for (int z=0;z<inColumn.size();z++){
					if (column.isNumeric()){
						v = inColumn.getCellValues().get(z);
						column.addValue(v) ;
					}else{
						str = inColumn.getCellValueStr().get(z);
						column.addValueStr(str) ;
					}
				}
				
				
			} // column exists?
		}// i-> all columns

		// create row perspective...
		n=0;
		
		// now we import the inDataTable (which includes normalizing) in order
		// to prepare the additional columns
		
		// this includes normalize the whole table, observing, whether we need to rebuild the SOM
		
		somDataObj.setStreamingRowOffset(offset);
		
		// this is quite different to importDataTable(), because we 
		somDataObj.extendDataTable( somDataObj.getTransformer(), dataTable, 1 );
		
		n=0;
	}

	// ----------------------------------------------------


	@Override
	public void setDataTableOffset(int firstUseRowinTable) {
		// 
		this.firstUseRowinTable = firstUseRowinTable;
	}

	@Override
	public int getDataTableOffset() {
		return firstUseRowinTable;
	}


	@Override
	public void setDataTableReference(DataTable datatable) {
		
		dataTable = datatable;
	}

	@Override
	public DataTable getDataTableReference() {
		return dataTable;
	}

	@Override
	public SomDataObject getSomDataObject() {
		return somDataObj;
	}
	@Override
	public void setSomDataObject(SomDataObject sdo) {
		somDataObj = sdo;
	}
	
}

package org.NooLab.somfluid.transformer;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;



/**
 * 
 * 
 * most basic functinoality: normalizing input data
 * 
 */
public class SomTransformer {

	SomFluidFactory sfFactory;
	SomDataObject somData;
	
	DataTable dataTableObj ;
	DataTable dataTableNormalized ;
	
	
	
	// ========================================================================
	public SomTransformer( SomFluidFactory factory, SomDataObject sdo) {

		sfFactory = factory;
		somData = sdo;
		
		dataTableObj = somData.getDataTable() ;
	}


	
	public void applyBasicNumericalAdjustments( ) {
	
		try{
			
			
			
		}catch(Exception e){
			
		}
		
	}



	public void setDataTable( DataTable inDatatable ) {
		 
		try{
			
			// creates a deep clone with content of inDatatable 
			dataTableObj = new DataTable( inDatatable ) ; 
			
		}catch(Exception e){
			
		}
	}


	
	public void applyAprioriLinkChecking() {
	 
		try{
			
			
			
		}catch(Exception e){
			
		}
	}



	public DataTable normalizeData() {
		int z;
		DataTableCol col, colNorm;
		ArrayList<DataTableCol> inDataTable = dataTableObj.getDataTable();
		ArrayList<DataTableCol> normDataTable;
		
		try{
			
			dataTableNormalized = new DataTable(somData, true); // the whole object
			
			dataTableNormalized.setSourceFileName( dataTableObj.getSourceFilename() );
			dataTableNormalized.setTableHasHeader( dataTableObj.isTableHasHeader() ) ;
			dataTableNormalized.setFormats( dataTableObj.getFormats().clone() ) ;
			dataTableNormalized.setColcount( dataTableObj.getColcount() ) ;
			dataTableNormalized.setRowcount( dataTableObj.getRowcount() ) ;
			dataTableNormalized.setColumnHeaders( new ArrayList<String>(dataTableObj.getColumnHeaders()) );
			
			normDataTable = dataTableNormalized.getDataTable();
			
			z=0;
			
			for (int i=0; i<inDataTable.size();i++){
				
				col = inDataTable.get(i);
				
				if ((col.getRecalculationIndicator()>0)){
					
					if (col.isIndexColumnCandidate()==false){
						col.calculateBasicStatistics();
					}
					// ArrayList<Double> cellValues
				}else{
					continue;
				}
				
				// we do not normalize index columns, we won't include it in the analysis anyway
				col.setRecalculationIndicator(0);
				colNorm = new DataTableCol(dataTableNormalized, col);//  dataTableNormalized, i
				
				if ((col.getDataFormat()<8) && (col.isIndexColumnCandidate()==false)){
					
					colNorm.normalize( col.getStatisticalDescription().getMini(), col.getStatisticalDescription().getMaxi() );

					// calculate stats for the normalized column
					colNorm.calculateBasicStatistics();
					// store the statistical description for the raw data in the column  
					// that contain the normalized data... such we will be able to translate ! 
					colNorm.setRawDataStatistics(col.getStatisticalDescription()) ;

				}else{
					col.setRecalculationIndicator(-3); // ignore, like blacklisted variables
				}
				
				normDataTable.add(colNorm);
			} // all columns
			
			z=0 ;
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return dataTableNormalized;
	}


	// ------------------------------------------------------------------------

	public DataTable getDataTableNormalized() {
		return dataTableNormalized;
	}

	
}

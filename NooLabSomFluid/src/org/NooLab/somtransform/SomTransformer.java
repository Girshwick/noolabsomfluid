package org.NooLab.somtransform;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variables;

import org.NooLab.somsprite.PotentialSpriteImprovement;



/**
 * 
 * 
 * most basic functinoality: normalizing input data
 * 
 */
public class SomTransformer {

	//SomFluidFactory sfFactory;
	SomDataObject somData;
	
	DataTable dataTableObj ;
	DataTable dataTableNormalized ;
	
	ArrayList<CandidateTransformation> candidateTransformations = new ArrayList<CandidateTransformation> ();
	
	// ========================================================================
	public SomTransformer( SomDataObject sdo) {

		
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



	public DataTable normalizeData( Variables variables) {
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
				colNorm.setSerialID(i) ;
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


	// send candidates into SomTransformer, they will be put just to a queue, 
	// but NOTHING will be changed regarding the transformations...  until a refresh or request for implementation will be sent...
	public void perceiveCandidateTransformations(ArrayList<PotentialSpriteImprovement> candidates) {
		
		String expr,exprName;
		String[] varStr = new String[2] ;
		int[] varix = new int[2] ;
		PotentialSpriteImprovement item;
		CandidateTransformation ctrans ;
		
		// we translate it into a more economic form, just the variables and the formula
		for (int i=0;i<candidates.size();i++){
			item = candidates.get(i) ;
			       expr = item.getExpression();
			       exprName = item.getExpressionName() ;
			       varix[0] = item.varIndex1 ;
			       varix[1] = item.varIndex2 ;
			       varStr[0] = somData.getVariablesLabels().get(varix[0]);
			       varStr[1] = somData.getVariablesLabels().get(varix[1]);
			       
			ctrans = new CandidateTransformation(exprName,expr,varix,varStr);

			candidateTransformations.add(ctrans) ;
		}
		
	}

	public void implementTransformations(){
		// putting candidateTransformations to the transformation model
		
	}
	
}

package org.NooLab.somscreen.linear;



import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.math3.linear.BlockRealMatrix;
import org.NooLab.math3.linear.RealMatrix;
import org.NooLab.math3.stat.correlation.*;


import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.math.array.DoubleArray;
import org.omg.CORBA.DoubleSeqHelper;



/** 
 * this provides a wrapper around calculations of correlation(s) for the {@link SomMapTable} 
 * 
 *                     
 *                     
 *                     
 */
public class MapCorrelation {

	SomDataObject somData;
	SomMapTable somMap ;

	SpearmansCorrelation spc = new SpearmansCorrelation ();
	
	PearsonsCorrelation pec = new PearsonsCorrelation();
	
	IndexedDistances sortedResultMatrix = new IndexedDistances() ;
	RealMatrix inMatrix , resultMatrix;
	
	ArrayList<String> smtVarLabels ;
	
	double[] referenceData;
	ArrayList<Integer> excludedItems = new ArrayList<Integer>();
	ArrayList<Integer> referenceItems = new ArrayList<Integer>();
	double mvValue = -1.0 ;
	
	
	// ========================================================================
	public MapCorrelation(SomDataObject somdata, SomMapTable sommap ) {
		// 
		somMap = sommap;
		somData = somdata;
	}
	// ========================================================================


	public void setReferenceData( double[] values) {
		if ((values==null) || (values.length<=1)){
			return;
		}
		referenceData = new double[values.length];
	}
	
	
	/**
	 * note that the provided index values have to refer to the SomMapTable !!!
	 * @param exclitems
	 */
	public void setExcludedItems(ArrayList<Integer> exclitems) {
		
		excludedItems = new ArrayList<Integer>(exclitems) ;
	}
	
	public void addExcludedItem( int itemindex) {
		if (itemindex>=0){
			excludedItems.add(itemindex);
		}
	}
	
	public void addExcludedItem( String varlabel) {
		
		if ((varlabel==null) || (varlabel.length()==0)){
			return;
		}
		
		if ((smtVarLabels==null) || (smtVarLabels.size()==0)){
			smtVarLabels = new ArrayList<String>(Arrays.asList( somMap.variables ));
		}
		int ix = smtVarLabels.indexOf(varlabel) ;
		if (ix>=0){
			excludedItems.add(ix);
		}
	}
	
	public void addReferenceItem( int itemindex ) {
		if (itemindex>=0){
			referenceItems.add(itemindex);
		}
	}
	
	public void addReferenceItem( String varlabel ) {
		if ((varlabel==null) || (varlabel.length()==0)){
			return;
		}
		if ((smtVarLabels==null) || (smtVarLabels.size()==0)){
			smtVarLabels = new ArrayList<String>(Arrays.asList( somMap.variables ));
		}
		int ix = smtVarLabels.indexOf(varlabel) ;
		if (ix>=0){
			referenceItems.add(ix);
		}
	}
	
	public void setReferenceItems(ArrayList<Integer> refitems) {
		// 
		referenceItems = new ArrayList<Integer>(refitems) ;
	}


	public ArrayList<String> getSelectionLabels( ArrayList<Integer> matrixIndexes ) {
		ArrayList<String> vLabels = new ArrayList<String>();
		
		for (int i=0;i<matrixIndexes.size();i++){
			int ix = matrixIndexes.get(i);
			if (ix>=0){
				String vlabel = smtVarLabels.get(ix);
				vLabels.add( vlabel);
			}
		}
		
		return vLabels;
	}
	/**
	 * refers to referenceItems 
	 * 
	 * @param serializedMatrixIndexes
	 * @return
	 */
	public ArrayList<String> getSelectionLabels( IndexedDistances serializedMatrixIndexes ) {
		ArrayList<String> vLabels = new ArrayList<String>();
		IndexedDistances smi = serializedMatrixIndexes;
		int ix=-1,ix1,ix2 ;
		String vlabel;
		
		for (int i=0;i<serializedMatrixIndexes.size();i++){
			
			ix1 = smi.getItem(i).getIndex() ;
			ix2 = smi.getItem(i).getIndex2() ;
			
			ix=-1;
			if (referenceItems.indexOf(ix1)>=0){
				if( referenceItems.indexOf(ix2)<0) {
					ix = ix2; 
				}
			}else{
				if (referenceItems.indexOf(ix2) >= 0) {
					if( referenceItems.indexOf(ix1)<0) {
						ix = ix1;
					}
				}
			}
			if (ix>=0) {
				vlabel = smtVarLabels.get(ix);
				if (vLabels.indexOf(vlabel)<0){
					vLabels.add(vlabel) ;			
				}
			}
		}
		
		return vLabels;
	}
	
	
	public double[][] getMatrix() {
		  
		return resultMatrix.getData() ;
	}


	public double[] getMatrixRow(int rowindex) {
		// 
		return resultMatrix.getRow(rowindex);
	}

	public double[] getMatrixColumn(int colindex) {
		// 
		return resultMatrix.getColumn(colindex);
	}


	/**
	 * 
	 * @param rowindex index of the row to visit
	 * @param corrCoeff threshold of the correlation value, only column indices that contain a larger value will be returned
	 * @param quantile the maximum quantile percentage that will be returned as a percentage of row size, values have to be in [0.0 .. 100.0] 
	 * @param d 
	 * @return
	 */
	public ArrayList<Integer> getMatrixRowTopValues(int rowindex, double corrCoeffLo, double corrCoeffHi, double quantile) {
		
		ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
		IndexedDistances ixds = new IndexedDistances ();
		double cc;
		
		double[][] rMatrix = resultMatrix.getData() ;
		try{
			
			for (int i=0;i< rMatrix[rowindex].length;i++){
				double v = rMatrix[rowindex][i] ;
				ixds.add( new IndexDistance(i,v,somMap.variables[i])) ;
			} // put the whole row into the structure
			
			ixds.sort(-1);
			int z=0, maxz, ix; 
			if (quantile>0){
				maxz = (int) (ixds.size()*quantile/100.0);
			}else{ 
				maxz = ixds.size() ;
			}
			for (int i=0;i<maxz;i++){
				cc = ixds.getItem(i).getDistance() ;
				if ((cc>corrCoeffLo) && (cc<=corrCoeffHi)){
					ix = ixds.getItem(i).getIndex() ;
					columnIndexes.add(ix) ;
				} // > corrCoeff ?
			} // i-> all items in sorted list
			
			ix=0;

		}catch(Exception e){
			columnIndexes = new ArrayList<Integer>();
		}
		
		
		return columnIndexes;
	}


	private void prepareData(){
		
		int rc=0,cc=0 ;
		ArrayList<Integer> excludedColumns = new ArrayList<Integer>();
 		
		try{
			
			if ((somMap==null) || (somMap.values.length<=1)){
				throw(new Exception("som-map table not defined in <MapCorrelation> "));
			}
			rc = somMap.values.length ;
			cc = somMap.values[0].length ;
			
			
			if ((smtVarLabels==null) || (smtVarLabels.size()==0)){
				smtVarLabels = new ArrayList<String>(Arrays.asList( somMap.variables ));
			}

			// check if the data contains only or too much  missing values... if yes, the exclude
			// excludedColumns
			
			inMatrix = new BlockRealMatrix(rc, cc);
			 
			int zc=0;
			for (int c=0;c<somMap.values.length;c++){
				
				if (excludedColumns.indexOf(c)<0){
					inMatrix.setRow( zc, somMap.values[c]);   
					zc++;
				}
				
			} // c->
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// String[]   variables = new String[0] ;
		// int tvIndex
		rc=0;
	}

	private void prepareResultsFull() {
		
		double corrcoeff;
		String vli,vlj ;
		
		sortedResultMatrix.clear();
		
		double[][] resultsArray = DoubleArray.copy( resultMatrix.getData() ) ;
		
		vli="";
		
		for (int i=0;i<resultsArray.length-1;i++){ // rows in c-matrix
			
			if ((i==somMap.tvIndex) && (referenceItems.indexOf(somMap.tvIndex )<0 )){
				// we exclude tv index, but only if we did not request it!
				continue;
			}
			for (int j=i+1;j<resultsArray.length;j++){ // columns
				
				if ((excludedItems.size()>0)){
					if ((excludedItems.indexOf(i)>=0) || (excludedItems.indexOf(j)>=0)){
						continue;
					}
				}
				if (referenceItems.size()>0){
					if ((referenceItems.indexOf(i)<0) ){ // e.g. if reference item is just the TV with tvindex = 1, we are interested in all values in row 1
						continue;
					}
				}
				corrcoeff = resultsArray[j][i] ;
				vli = somMap.variables[i] ;
				vlj = somMap.variables[j] ;
				
				sortedResultMatrix.add( new IndexDistance(i,j,corrcoeff, somMap.variables[i]+"::"+somMap.variables[j]) ) ;
			}	
		}
		
		if (sortedResultMatrix.size()>0){
			sortedResultMatrix.sort(-1);
			while (sortedResultMatrix.getItem(0).getDistance()>0.999){
				sortedResultMatrix.removeItem(0);
			}
		}
		
	} 

	public void calculateMatrix() throws Exception {
		//  
		if ((inMatrix==null) || (inMatrix.getData().length<=1)){
			prepareData();
		}
		if (inMatrix.getColumnDimension()>1){
			
			spc.activateMissingValueCheck(-1.0); // will be propagated to PearsonCorrelation, and ranking
			resultMatrix = spc.computeCorrelationMatrix(inMatrix) ;
		}else{
			throw(new Exception("data matrix is not complete, column count is c="+inMatrix.getColumnDimension()));
		}
		 
	}

	/**
	 * calculates the Spearman correlation of yValues to previously provided data (by {@link setReferenceData})  
	 * 
	 * @param yValues
	 * @return the correlation value
	 * @throws Exception
	 */
	public double calculateCorrelation( ArrayList<Double> yValues) throws Exception {
		int rc=0,cc=0 ;
		double[] yData;
		
		cc = 2 ;
		rc = yValues.size() ;
		
		if (rc != referenceData.length){
			throw(new Exception("Length of proovided column does not match the length of reference data column"));
		}
		
		yData = new double[yValues.size()];
		for (int i=0;i<yValues.size();i++){
			yData[i] = 	yValues.get(i) ;
		}
		/*
		inMatrix = new BlockRealMatrix(rc, cc);
		
		inMatrix.setColumn(0, referenceData);
		inMatrix.setColumn(1, yData);
		*/
		double spcCorrValue = spc.correlation(referenceData, yData) ;
		return spcCorrValue;
	}

	public void calculateMatrix( ArrayList<Double> values1, ArrayList<Double> values2) {
		// 
	}
	
	public void calculateMatrix( double[] yValues) {
		
	}

	public void calculateMatrix( double[] values1, double[] values2) {
		// 
	}

	public void calculateMatrix( int colIndex1, int colIndex2) {
		
	}

	public IndexedDistances getTop(int topN, String refVarLabel) {
		
		
		return null;
	}
	
	
	/**
	 * the returned list of IndexDisance items refers to the sorting of the variables in the SomMapTable;
	 * the indices are contained in these items
	 * 
	 * @param topN
	 * @return
	 */
	public IndexedDistances getTop(int topN) {
		
		if ((sortedResultMatrix==null) || (sortedResultMatrix.size()<=1)){
			prepareResultsFull();
		}
		
		IndexedDistances spcTopN = new IndexedDistances() ;
	 
		int n= Math.min(topN, sortedResultMatrix.size());
		
		for (int i=0;i<n;i++){
			spcTopN.add(  new IndexDistance( sortedResultMatrix.getItem(i) ) ) ;
		}
		
		return spcTopN;
	}


	public IndexedDistances getTop(int nOutOf, int topN) {
	 
		if ((sortedResultMatrix==null) || (sortedResultMatrix.size()<=1)){
			prepareResultsFull();
		}
		
		IndexedDistances spcTopN = new IndexedDistances() ;
	 
		for (int i=0;i<topN;i++){
			spcTopN.add(  new IndexDistance( sortedResultMatrix.getItem(i) ) ) ;
		}
		
		return spcTopN;
	}


	public void setMissingValue(double mv_value) {
		mvValue = mv_value;
		
	}


	public double getMvValue() {
		return mvValue;
	}


	public void setMvValue(double mvValue) {
		this.mvValue = mvValue;
	}
	
	
	
	
}

package org.NooLab.somtransform;

import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.MissingValues;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatistics;
import org.NooLab.somfluid.util.DescriptiveStatisticsValues;
import org.NooLab.somtransform.algo.distribution.EmpiricDistribution;
import org.NooLab.utilities.datatypes.IndexedDistances;




/**
 * 
 * 
 *
 */
public class NumPropertiesChecker {

	
	SomTransformer somTransformer ;
	SomFluidAppGeneralPropertiesIntf sfProperties;
	
	DataTable dataTable ;
	MissingValues missingValues; 
	
	String columnLabel = "" ;
	int columnIndex = -1;

	ArrayList<Double> datavalues = new ArrayList<Double> () ;
	ArrayList<Double> targetvalues = new ArrayList<Double> () ;
	
	BasicStatisticalDescription statisticalDescription ;
	BasicStatistics basicStatisticsEngine;
	
	ArrayList<Double> resultParameterValues ;
	
	
	// ========================================================================
	public NumPropertiesChecker( SomTransformer st, DataTable datatable, int colindex ){
		
		somTransformer = st ;
		dataTable = datatable ;
		columnIndex = colindex; 

		datavalues = dataTable.getColumn(colindex).getCellValues() ;
		sfProperties = (SomFluidAppGeneralPropertiesIntf) somTransformer.sfProperties;
	}
	
	public NumPropertiesChecker( SomTransformer somtransformer, ArrayList<Double> values ){
		
		somTransformer = somtransformer ;
		datavalues = values ;
		
		missingValues = somTransformer.dataTableObj.getMissingValues();
		sfProperties = (SomFluidAppGeneralPropertiesIntf) somTransformer.sfProperties;
	}
	// ========================================================================

	public void setTableColumnIndex(int index){
		columnIndex = index;
	}
	
	public void setColumnHeaderLabel(String label){
	
		columnLabel = label;
	}
	
	public void setTargetValues(ArrayList<Double> tvalues) {

		targetvalues = tvalues;
		
	}

	public void prepareStatisticalDescription(){
		
		statisticalDescription = new BasicStatisticalDescription(true);
		statisticalDescription.introduceValues( datavalues );
		// basic statistics (mean, var) is now available
		
		basicStatisticsEngine = new BasicStatistics( statisticalDescription, missingValues, datavalues );
		basicStatisticsEngine.calculate();
		
	}
	
	protected void performChecks(int idOfChecksToPerform) {
		
		int[] tasks;
		tasks = new int[1] ;
		tasks[0] = idOfChecksToPerform;
		
		
		if (Arrays.binarySearch(tasks, 1)>=0){ // TODO: create constants for those indicators
			boolean sz  = checkForSemanticZeroSplit(idOfChecksToPerform) ;	
		}
		
		if (Arrays.binarySearch(tasks, 2)>=0){
			boolean qsp = checkForModeSplit(idOfChecksToPerform) ; // kurtosis, 1-dimensional k-means, salient points (min,max,zero-regions)
			
		}
		
		if (Arrays.binarySearch(tasks, 3)>=0){
			boolean shp = checkForShapeScaling(idOfChecksToPerform) ;	
		}
	
		 
	}

	public ArrayList<Double> getResultParameterValues(){
		
		return resultParameterValues;
	}
	
	/**
	 * @return the statisticalDescription
	 */
	public BasicStatisticalDescription getStatisticalDescription() {
		return statisticalDescription;
	}

	
	public double[] getCoreTransformParameters( int task ) {
		
		double[] params = new double[0];
		double v;
		DescriptiveStatisticsValues  histoStats = statisticalDescription.getEmpiricDistribution().getHistoStats() ;
		
		if (task == SomTransformer.getAlgorithmIndexValue("AdaptiveLogShift")){ //   _ADV_TRANSFORM_LOGSHIFT){
			params = new double[3];
			params[0] = histoStats.getSumosRatio() ; // the larger the stronger
			params[1] = histoStats.getBoxRatio3() ;  // the smaller the stronger
			params[2] = histoStats.getKurtosis() ;
		}
		
		return params;
	}

	public ArrayList<Object> determineAdaptedParameters(int algotask, ArrayList<Double> values) {
		
		ArrayList<Object> params = new ArrayList<Object>();
		double[] analyticParams;
		
		analyticParams = getCoreTransformParameters( algotask );
		
		try{
			int ix = sfProperties.getAlgoDeclarations().getIndications().getIndexByStr("AdaptiveLogShift");
			if (algotask== ix){
				params = createAdaptedLogShift(values);
			}
			// further algos
			
		}catch(Exception e){
			
		}
		
		
		return params;
	}
	
	/**
	 * we increase the effect until the parameters have changed substantially 
	 * 
	 * @param values
	 * @return
	 */
	private ArrayList<Object> createAdaptedLogShift( ArrayList<Double> values ){
		
		ArrayList<Object> params = new ArrayList<Object>();
		ArrayList<Double> workingvals = new ArrayList<Double>();
		
		double v0,vw,vt,_max,_min,sc0 ;
		boolean completed=false;
		EmpiricDistribution ed;
		DescriptiveStatisticsValues  histostats ;
		NumPropertiesChecker npc ;
		double score ;
		
		
		ArrayList<Double> valuesBuffer = new ArrayList<Double>(values) ;  
		
		try{

			ed = statisticalDescription.getEmpiricDistribution();
			histostats = statisticalDescription.getEmpiricDistribution().getHistoStats() ;
			score = ed.getNegExpScore() ;
			
			// copy known histo and stats description into a new structure
			
			// we apply the formula log(k+ value), where 0<k<0.3
			double ptValue = 0.01 ;
			
			int z=0 ;
			while ((completed==false) && (z<10)){
				completed = true;
				// 1. copy 
				workingvals.clear() ;
				workingvals = new ArrayList<Double>(valuesBuffer) ;
				
				// 2.a transform values in the working copy, which is a temp list
				_max = -9999999999999999.09; _min = 999999999999999999999.09 ;
				for (int i=0;i<workingvals.size();i++){
					vw = workingvals.get(i);
					v0 = values.get(i) ;
					if (v0!=-1.0){
						vt = Math.log( ptValue + vw) ;
						if (_min>vt)_min=vt;
						if (_max<vt)_max=vt;
					}else{
						vt=-1.0 ;
					}
					workingvals.set(i, vt) ;
				}
				// 2.b normalize it
				for (int i=0;i<workingvals.size();i++){
					vw = workingvals.get(i);
					v0 = values.get(i) ;
					if (v0!=-1.0){
						vt = (vw-_min)/(_max-_min); 
						workingvals.set(i, vt) ;
					}
				}
				
				// 3. get statistics for list of values
				npc = new NumPropertiesChecker( somTransformer, workingvals ) ;
				statisticalDescription = npc.getStatisticalDescription() ;
				npc.prepareStatisticalDescription();
				
				// 4. get description for transformed values and compare to that of previous data 
				sc0 = npc.statisticalDescription.getEmpiricDistribution().getNegExpScore();
				if (sc0<20){
					completed = true;
				}else{
					ptValue = ptValue/10.0 ;
				}
				z++;
			} // -> completed ?
			
			params.add(ptValue) ;
			 
		}catch(Exception e){
		}
		                             
		return params;
	}
	
	
	/**
	 * if the provided testId is unknown, an exception will be thrown
	 * 
	 * @param testId one of the values of "SomTransformerIntf._ADV_TRANSFORM_*"
	 * 
	 * 
	 * @return
	 */
	public boolean checkFor(int testId) throws Exception{
		
		boolean rB=false;
		int requestavail=0;
		IndexedDistances declixds ;
		
		// refers to catalog.xml
		declixds = sfProperties.getAlgoDeclarations().getIndications();
		
		
		if (testId == declixds.getIndexByStr("AdaptiveDeciling") ){
			rB = checkForModeSplit(testId); requestavail=1;
		}
		if (testId == declixds.getIndexByStr("SemZeroDeciling")  ){
			rB = checkForSemanticZeroSplit(testId); requestavail=1;
		}
		if (testId == declixds.getIndexByStr("AdaptiveLogShift") ){
			rB = checkForLeftSkew(testId) || checkForRightSkew(testId); requestavail=1;
		}
		if (testId == declixds.getIndexByStr("ZTransform") ){
			rB = checkForZTransformProperty(testId); requestavail=1;
		}
		if (testId == declixds.getIndexByStr("ResidualsByCorrelation") ){
			rB = checkForLinearCorrelationResiduals(testId); requestavail=1; 
		}
		if (testId == declixds.getIndexByStr("ResidualsBySimpleCluster") ){
			rB = checkForSimpleClusteringResiduals(testId); requestavail=1;
		}
		  
		if (requestavail==0){
			throw(new Exception("requested test (id="+testId+") for numerical properties is not available."));
		}
		return rB;
	}

	private boolean checkForZTransformProperty(int testId) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean checkForModeSplit(int testId){
		boolean rB=false;
		
		// selectAlgoObject( listOfTransformations[t] ) ;
		
		
		return rB;
	}

	private boolean checkForSemanticZeroSplit(int testId){
		boolean rB=false;
		
	 
		
		return rB;
	}

	private boolean checkForShapeScaling(int testId) {
		return checkForLeftSkew(testId) || checkForRightSkew(testId);
	}

	private boolean checkForLeftSkew(int testId){
		boolean rB=false;
		double skew;
		
		skew = statisticalDescription.getSkewness();
		
		rB = statisticalDescription.getEmpiricDistribution().variableIsNegExp() ;
		
		return rB;
	}

	private boolean checkForRightSkew(int testId){
		boolean rB=false;
		
	
		
		return rB;
	}

	private boolean checkForLinearCorrelationResiduals(int testId) {
		// 
		return false;
	}

	private boolean checkForSimpleClusteringResiduals(int testId) {
		//  
		return false;
	}

	 
	 
}

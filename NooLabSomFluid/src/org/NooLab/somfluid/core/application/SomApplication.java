package org.NooLab.somfluid.core.application;

import java.util.ArrayList;


import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.DSomDataPerceptionAbstract;
import org.NooLab.somfluid.core.engines.det.results.ResultAspect;
import org.NooLab.somfluid.core.engines.det.results.ValidationSet;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;



/**
 * 
 * top-level entry point for using the SOM lattice as a classificator
 *  
 * note that there is a small companion that loads a model and classifies data, but is
 * not able to learn or to adapt (but it measures the classification events)  
 *  
 *  
 * TODO: if a large set of records is delivered for classification, 
 *       SomApplication offers multi-threading
 * 
 *
 */
public class SomApplication implements 	SomAppUsageIntf,
									  	SomAppValidationIntf{

	public static final double _RISK_BY_MODELSETTING = 3.0 ;
	
	DSom dSom;
	SomDataObject somData;
	ModelingSettings modset;
	
	DSomPerception dSomApp ;
	
	
	double toleratedRisk = _RISK_BY_MODELSETTING ;

	private int sizeOfResultList = 1 ;

	private boolean extendedResults = false;
		
	// ................................
	double[][] targetGroups;
	
	ArrUtilities arrutil = new ArrUtilities ();
	PrintLog out;
	
	// ========================================================================	
	public SomApplication(){
			
	}
	// ========================================================================

	
	public void setSomData( SomDataObject somdata){
		somData = somdata ;
		
		if (dSom!=null){
			if (targetGroups.length==0){
				retrieveTargetGroups();
			}
		}
	}
	
	public void setDSomInstance( DSom dsom){
		dSom = dsom;
		dSomApp = new DSomPerception(dSom);
		
		out = dSom.getOut() ;
		
		if ((targetGroups==null) ||(targetGroups.length==0)){
			retrieveTargetGroups();
		}
		
	}
	
	private void retrieveTargetGroups() {
		
		modset = dSom.getModelingSettings();
		targetGroups = modset.getClassifySettings().getTargetGroupDefinition() ;
	}
	
	
	public void setRiskAttitude( double toleratedRiskThreshold ){
		toleratedRisk = toleratedRiskThreshold;
	}
	
	public void setSizeOfResultList(int n){
		sizeOfResultList = n;
	}
	public void setExtendedResults(boolean flag){
		extendedResults = flag;
		if (extendedResults){
			if (sizeOfResultList<=1){
				sizeOfResultList = 3 ;
			}
		}
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * if maxProcessesCount > than available processors, it will be reduced accordingly
	 * 
	 * 0,1 = switch off
	 * 
	 * this uses NooLabChord, which is simply a dispatcher based on list of indices
	 */
	public void setAllowForParallelProcessing( int maxProcessesCount ){
		
	}
	
	/**
	 * 
	 */
	public void setBulkDataIndices( ArrayList<Integer> indexes) throws Exception{
		int[] _ixs = new int[indexes.size()] ;
		
		for (int i=0;i<indexes.size();i++){
			_ixs[i] = indexes.get(i) ;
		}
		setBulkDataIndices(_ixs) ;
	}
	public void setBulkDataIndices( int[] indexes) throws Exception{
		if (somData==null){
			throw(new Exception("You have to provide a <SomDataObject> first!"));
		}
		
		// is the largest index contained in this somData ?
		
		// prepare multi-threading
		
	}
	
	/**
	 * 
	 * extracts the relevant descriptors from the nodes as indicated by "nodeList"
	 * 
	 * @param nodeList
	 * @return
	 */
	private SomApplicationResults prepareResultSet( int observationIndex, ArrayList<IndexDistanceIntf> nodeList){
		
		SomApplicationResults resultObj = new SomApplicationResults();
		ResultAspect  rAspectSet;
		
		int ix , ct, tvIndex,p; 
		double dist,_ppv,_npv, ecrc,modelECR, trisk,tvValue ;
		
		
		
		ValidationSet vset ;
		MetaNode bmuNode ;
		IndexDistanceIntf ixDistBmu;
		ProfileVectorIntf bmuProfile ;
		
		ValidationSet  latticeVset = dSom.getSomLattice().getModelProperties().getTrainingSample() ;
		ix=0;
		
		for (int i=0;i<nodeList.size();i++){
	
			rAspectSet = new ResultAspect();
			
			
			ixDistBmu = nodeList.get(i) ;
			ix = ixDistBmu.getIndex() ;
			dist = ixDistBmu.getDistance() ;
			
			
			if (ix>=0){
				
				bmuNode = dSom.getSomLattice().getNode(ix) ;
				bmuProfile = bmuNode.getIntensionality().getProfileVector() ;
	
				tvIndex = bmuNode.getSimilarity().getIndexTargetVariable() ;
				tvValue = somData.getRecordByIndex( observationIndex, 2).get(tvIndex) ;
				
				p = arrutil.intervalIndexOf( tvValue, targetGroups,0);
				if (p>=0){
					p=p+1;
				}
				rAspectSet.observationClassType = p;
				
				rAspectSet.similarity = dist;
				int latticeSize = latticeVset.getObservationCount() ;
				
				rAspectSet.sizeOfClass = bmuNode.getExtensionality().getCount();;
				rAspectSet.relativeSizeOfClass = (double)rAspectSet.sizeOfClass/(double)latticeSize;
				rAspectSet.ppv = bmuNode.getExtensionality().getPPV();
				rAspectSet.npv = bmuNode.getExtensionality().getNPV();
				
				
				modelECR = dSom.getSomLattice().getModelProperties().getEcr() ;
				if ((i==0) || (resultObj.topEcrNodes==null)){
					resultObj.topEcrNodes = new ArrayList<Integer>() ;
				} 
				resultObj.topEcrNodes.add(ix);
				
				trisk = toleratedRisk;
				if (trisk>=3){
					ecrc = modelECR; 
				}else{
					ecrc = toleratedRisk ;
				}
				if (ecrc>(1- rAspectSet.ppv)){
					ct = 1 ;
				}else{
					ct = 0 ;
				}
				rAspectSet.classType = ct;
	
				if (extendedResults){
				 
					rAspectSet.profileVariablesStr = bmuNode.getIntensionality().getVariablesStr(1);
					rAspectSet.profileValues = bmuNode.getIntensionality().getValues(1) ;
				}
				resultObj.aspects.add(rAspectSet);
			} // ix ok?
		} // i->
		
		
		return resultObj;
	}
	// ========================================================================


	@Override
	public SomApplicationResults classify(int observationIndex) throws Exception{
		
		SomApplicationResults resultObj = new SomApplicationResults() ;
		ArrayList<IndexDistanceIntf> nodeList ;
		
		if (dSomApp==null){
			throw(new Exception("DSom-instance is not available, hence no classification is possible."));
		}

		ArrayList<Double> profilevalues ; // = new ArrayList<Double>(); 
		
		dSomApp.setBmuListSize( sizeOfResultList ); 
		
		try{

			// fetch the record "observationIndex" from the table of normalized data
			profilevalues = somData.getNormalizedDataTable().getDataTableRow(observationIndex);
		
			// ...and feed it to BMU search
			dSomApp.classifyRecord(observationIndex, profilevalues);

			// fetch results and stuff it to "resultObj" ...
			nodeList = dSomApp.getBmus() ;

			resultObj = prepareResultSet(observationIndex,nodeList) ;
			
			// TODO: measure this classification by storing it into the SomLib - repository (essentially a database) 
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return resultObj;
	}

	
	/**
	 * 
	 * particular settings should comprise
	 * - handling of value overflow
	 * - handling of mismatches in variable sets (note that different nodes may use different variables !)
	 * 
	 * this should provide detailed status / error codes, e.g. ... 
	 * - overflow in values, 
	 * - missing variables
	 * - superfluous variables
	 * 
	 * 
	 */
	@Override
	public SomApplicationResults classify(String[] fields, double[] data) {
		
		SomApplicationResults resultObj = new SomApplicationResults();
		
		
		
		return resultObj;
	}

	@Override
	public SomApplicationResults classify(String[] fields, double[][] tabledata) {
		
		SomApplicationResults resultObj = new SomApplicationResults();
		
		
		
		return resultObj;
	}

	@Override
	public String classify( SomApplicationEventIntf resultsEvent,
						    String[] fields, double[] data) {

		String guidStr = GUID.randomvalue() ;
		
		
		
		return guidStr;
	}

	@Override
	public String classify( SomApplicationEventIntf resultsEvent,
							ArrayList<String> fields, ArrayList<Double> data) {

		String guidStr = GUID.randomvalue() ;
		
		return guidStr;
	}



 
	
	
	
	
	
	
	
	
	// ========================================================================
	
	
	
	@Override
	public void waitForResults(boolean flag) {
		// TODO Auto-generated method stub
		
	}

 

	// ========================================================================
	 

	@Override
	public SomApplicationResults getResultObject(String guidStr) {
		// TODO Auto-generated method stub
		return null;
	}


	public double getToleratedRisk() {
		return toleratedRisk;
	}


	public void setToleratedRisk(double toleratedRisk) {
		this.toleratedRisk = toleratedRisk;
	}


	public int getSizeOfResultList() {
		return sizeOfResultList;
	}


	public boolean isExtendedResults() {
		return extendedResults;
	}

	
	
	
}

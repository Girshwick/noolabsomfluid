package org.NooLab.somfluid.clapp;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.SomFluidFactory;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;

import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.strings.StringsUtil;


/**
 * 
 * TODO:  statistical description gets not exported, because algorithms   
 *  	  are not included in transformation model, only their names -->> we need the parameters of algorithms, e.g. also for NVE !!!
 * 
 *
 */
public class SomAppClassifier implements SomProcessIntf, Serializable{

	private static final long serialVersionUID = 1300587404052091239L;


	public static final int _MODE_FILE        = 1 ; 
	public static final int _MODE_WIFISERVICE = 3 ;
	
	
	SomFluidFactory    sfFactory ;
	
	SomAppProperties   soappProperties ; 
	
	SomDataObject      somData;
	
	SomApplicationIntf somApplication;
	SomAppModelLoader  soappLoader;
	

	SomAppTransformer  soappTransform ;
	
	SomAppResultAnalyses resultAnalyses = null ;

	
	int nodeCount=0;
	
	ArrayList<SomAppSomObject> somObjects = new ArrayList<SomAppSomObject>() ; ; 
	int somObjectsCount = 0; 
	
	boolean transformationModelImported=false;
	boolean transformationsExecuted=false;
	
	// is a guid-identifiable object, containing guid, universal serial, data section, status, commands, results
	public ArrayList<Object> dataQueue = new ArrayList<Object>(); 
	transient ClassificationProcess classProcess=null;


	
	transient PrintLog out;
	transient DFutils fileutil = new DFutils(); // DEBUG ONLY !!
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities();
	// ========================================================================
	
	public SomAppClassifier(SomApplicationIntf somapplication, SomFluidFactory factory) {
		 
		somApplication = somapplication;
		sfFactory = factory ;
		
		soappProperties = sfFactory.getSomAppProperties() ;
		soappLoader = somApplication.getSomAppModelLoader() ;
	
		resultAnalyses = new SomAppResultAnalyses( somApplication ) ;
		
		out = sfFactory.getOut() ;
	}
	// ========================================================================	
	
	public boolean prepare() {
		boolean rB=false;
		
		try{	

			// soappClassify = soappLoader.getSoappClassifier() ;
			soappTransform = soappLoader.getSoappTransformer() ;
			
			somData = somApplication.getSomData() ;
			
			somObjectsCount = somObjects.size();
			
			String loadedModel = soappLoader.getActiveModel() ;
			//if (soappClassify.somObjects.size()>0){
			if (somObjects.size()>0){
				
				//SomAppSomObject somObject = soappClassify.somObjects.get(0);
				SomAppSomObject somObject = somObjects.get(0);
				
				nodeCount = somObject.soappNodes.getNodes().size();

				if (nodeCount >= 3){
					// rB= transformIncomingData();
					
				} // nodeCount >= 3?
			} // soappClassify.somObjects?
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return rB;
	}


	/**
	 * creates a guid-identifiable container object that is containing </br>
	 * - guid,  </br>
	 * - universal serial, </br> 
	 * - data section,  </br>
	 * - status,  </br>
	 * - commands,  </br>
	 * - results;  </br> </br>
	 * 
	 * @return
	 */
	public String createDataTask() {
		 
		
		SomAppDataTask soappDataTask = new  SomAppDataTask( ) ;
		
		// sfFactory, somApplication, this,
		
		soappDataTask.setDataObject( somData );
		
		dataQueue.add( soappDataTask );
		
		return soappDataTask.getGuidStr() ;
	}
	
	
	
	public SomAppSomObject getItem(int index){
		SomAppSomObject soappObj = null;
		
		soappObj = somObjects.get(index) ;
		return soappObj;
	}
	
	 
	/*
	 
	 	// how many som instances? check:  <som index="0">
		
		
		// <project>
			// <general>
		
			// <context>
		
			// <content>
		
		// <lattice>
			// description 
		
			// <nodes>
	 */
	
	
	/**
	 * used by services
	 * 
	 */
	public void importDescribedDataObject( SomClassDescribedDataIntf obj ){  
		// just a value vector and the indices of the respective column, or the column headers
		// will be prepared to a data object
		
		
		
	}
	
	public void importDescribedDataObject( SomClassProfileDataIntf obj ){
	
		
	}
	
	/**
	 * given the feature vector of the provided table and the feature vector of the profiles in the som model,
	 * we have to determine a map position-to-position
	 * 
	 */
	public ArrayList<String> getTargetVector(){
		
		ArrayList<String> targetVector = new ArrayList<String>();
		
		try{
			 
			// soappProperties.
			soappLoader.providedVarLabels.size() ;
			
			// somObjects = soappClassify.somObjects ;
			SomAppSomObject somobj = somObjects.get(0);
			
			targetVector = ((SomAppNodeIntf)somobj.soappNodes.getNode(0)).getAssignates() ;
			
		}catch(Exception e){
			
		}
		return targetVector;
	}
	
	/**
	 * called from the process
	 * 
	 * @param currObject 
	 */
	
	public void performClassification(Object dataTaskObject){
		
		ArrayList<Double> dataRecord ;
		SomAppDataTask datatask;
		Object obj ;
		int modelIndex=0;
		
		datatask = (SomAppDataTask)dataTaskObject;
		
		obj = datatask.preparedDataTable;
		
		if ((obj!=null) && (obj instanceof String)){
		
			// the SomDataObject should already exist due to initial import of data, 
			// just handle the record, inclusive normalization 
			
			introduceDataToSomApplication(); 
			/*
			 *  we need a flag about the context: 
			 *    - first time= norm data are available, 
			 *    - next time = data must be assimilated into SomDataObject, in case of service mode
			 */
		}
		
		if ((obj!=null) || (obj instanceof SomDataObject)){
			// nothing special to do
		}
		  
		
		executeClassificationModel( (SomDataObject)obj );
		
	}
	
	
	/**
	 * we are NOT establishing the full SOM!
	 * We just create a table of aligned records and determine the winner list by measuring the similarity 
	 * 
	 */
 
	@SuppressWarnings("unchecked")
	private void executeClassificationModel( SomDataObject somDataForClassification){
		 
		int allowedMvCount=0; // make option
		int modelIndex =0, mvc,n;
		boolean recordIsOk;
		
		// acts as a replacement for DSom*
		SomAppSomObject  somObject ; 
		
		IndexedDistances winners;
		SomAppResultAnalysis resultAnalysis ;
		DataTable dataTable ;
		ArrayList<Double> values,usageIndicationVector = new ArrayList<Double>();
		ArrayList<Integer> useIndexesVector = new ArrayList<Integer>() ;
		Variables variables;
		ArrayList<Integer> mvcExcludedRecords = new ArrayList<Integer> ();
		// the object SomAppNodes is a "replacement", surrogate for the VirtualLattice
	 	
		variables = somDataForClassification.getVariables() ;
		
		out.resetPrintOutCollection();
		
		if (somObjects.size()==1){
			somObject = somObjects.get(0);	
			usageIndicationVector = somObject.getUsageVector();
			useIndexesVector = (ArrayList<Integer>) variables.transcribeUseIndications(usageIndicationVector) ;
		}
		
		dataTable = somDataForClassification.getNormalizedDataTable() ;
		
		// just for debug
		ArrayList<String> tableVars = dataTable.getColumnHeaders() ;
		ArrayList<String> requiredVars = (ArrayList<String>)variables.deriveVariableSelection(useIndexesVector, 0) ;
		
		n = tableVars.size();
		n = requiredVars.size();
		// we also could prepare a dedicated temp table containing only required variables, by arranging 
		// the references to the respective columns
		
		int rc = dataTable.getRowcount() ;
		
		// we have to create a table = extract the relevant columns such that the 
		// resulting table matches the model
		// we need not rebuild the whole structure, just header, and rows
		
		// we also include the index variables ArrayList<String> getIndexVariableLabels()
		for (int i=0;i<rc;i++){
			values = dataTable.getRowValuesArrayList(i);
			
		}
		
		// 
		for (int i=0;i<rc;i++){

			values = dataTable.getRowValuesArrayList(i);

			// select the model that matches best the provided data = least number of missing values, disjoints between intensions
			if (somObjects.size()>1){
				
				somObject = somObjects.get(modelIndex);	
				usageIndicationVector = somObject.getUsageVector();
				useIndexesVector = (ArrayList<Integer>) variables.transcribeUseIndications(usageIndicationVector) ;
				
			}else{
				modelIndex = 0;
				somObject = somObjects.get(modelIndex);
			}
			
			
			try {
				mvc = checkMissingValuesForProfilePositions( useIndexesVector, values ) ;
				double mvcRatio = (double)mvc/((double)values.size()) ;
				recordIsOk = (mvc <= allowedMvCount) ;
				
				// we may also first check against the model, and allow the results if (one of) the winning node(s) 
				// matches the missing values in the provided data... there is some automatism in the SOM 
				
			} catch (Exception e) {
				e.printStackTrace();
				// put this to a special edition of "resultAnalysis"
				recordIsOk = false; mvcExcludedRecords.add(i);
			}

			if (recordIsOk){

				winners = somObject.getWinnerNodes( values );
				
				if (winners.size()>0){
					 
					resultAnalysis = (new SomAppResultAnalysis( somData, soappProperties,somObject,winners)).prepare(i,values); 
					resultAnalyses.add(resultAnalysis) ;

				}else{
					// special log ... should not happen at all ...
				}

			}

			
		} // i-> all records
		 
		rc=0;
		fileutil.writeFileSimple( soappProperties.getBaseModelFolder()+"testresult.txt", out.getPrintOutCollection());
		out.resetPrintOutCollection() ;
	}
	
	
	private int checkMissingValuesForProfilePositions(	ArrayList<Integer> useIndexesVector, 
														ArrayList<Double> values)  throws Exception {
		
		int mvc = 0;
		
		for (int p=0;p<useIndexesVector.size();p++){
		
			int vp = useIndexesVector.get(p) ;
			
			if (values.get(vp)<0){
				mvc++;
			}
		}
		
		return mvc;
	}
	 
	 
	
	private void introduceDataToSomApplication() {
		 
		
	}
	 
	
	// sending data to the process
	public void classifyRecord(){
		ArrayList<String> targetStruc ;
		int psz = 0;
		
		
		// the variables needed for the profile and for defining the values in the use vector
		targetStruc = getTargetVector();
		
											out.print(2,"performing classification of record ") ;
		// ArrayList<Object> dataQueue									
		
		
		// align 
		
		
		// prepare object
		// prepare a mini table according to feature vector in the SOM, incl. value profile  
		// mini table = feature vector according to profiles,  
		
		 
		
		// dataQueue.add() ;
		
	}

	public void classifyTable(){
		
		ArrayList<String> targetStruc ;
											out.print(2,"performing classification of data from table... ") ;
											
		// the variables needed for the profile and for defining the values in the use vector
		targetStruc = getTargetVector();
		
	}


	public void start(int modeFile) {
		
											out.print(3,"starting sub-process...") ;
		if (classProcess ==null){						
			
			classProcess = new ClassificationProcess ();
			classProcess.start();
			
		}else{
			if (classProcess.isRunning==false){
				classProcess.start();
			}
		}
		
	}
	
	
	public boolean processIsRunning() {

		return classProcess.isRunning ;
	}


	/**
	 * 
	 * this is an internal service, which 
	 * works on the data queue : this is maintained by the embedding parent class , where records are received;
	 *  
	 * here we just call the performing routine, which also is in the parent class
	 * 
	 *
	 */
	class ClassificationProcess implements Runnable{

		Thread classyThrd ;
		public boolean isRunning = false;
		public boolean isWorking=false;
		
		
		// ------------------------------------------------
		public ClassificationProcess(){
		
			
			classyThrd = new Thread(this,"classyThrd"); 
		}
		// ------------------------------------------------
		
		public void start(){
			classyThrd.start() ;
		}
		
		@Override
		public void run() {
			isRunning = true;
											out.print(2,"process for performing classification has been started") ;
			while (isRunning ){
				
				if ((isWorking==false) && (dataQueue.size()>0)){
					isWorking = true;
					
					while (dataQueue.size()>0){
						Object currObject = dataQueue.get(0) ;  // queue is empty ...
						dataQueue.remove(0) ;
						performClassification( currObject );
						
					}
					
					isWorking = false;
				} else {// working ?
				
					out.delay(5);
				}
			} // ->
			
			
			isRunning = false;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public boolean isWorking() {
			return isWorking;
		}

		public ArrayList<Object> getDataQueue() {
			return dataQueue;
		}
		
		
	}


	@Override
	public SomDataObject getSomDataObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNeighborhoodNodes(int index, int surroundN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SomFluidAppGeneralPropertiesIntf getSfProperties() {
		 
		return soappProperties ;
	}

	@Override
	public LatticePropertiesIntf getLatticeProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualLattice getSomLattice() {
		// TODO Auto-generated method stub
		return null;
	}

	public SomAppResultAnalyses getResultAnalyses() {
		return resultAnalyses;
	}

	@Override
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> getUsedVariablesIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		
		dataQueue.clear() ;
		classProcess.isWorking = false ;
		classProcess.isRunning = false;
		
		out.delay(1000);
		
		classProcess.classyThrd.interrupt();
		
		
	}

	@Override
	public void nodeChangeEvent(ExtensionalityDynamicsIntf extensionality, long result) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}

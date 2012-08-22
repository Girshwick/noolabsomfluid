package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
 
import org.NooLab.somfluid.astor.AstorSomField;
import org.NooLab.somfluid.components.variables.SomVariableHandling;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.Assignates;
import org.NooLab.somfluid.core.engines.det.results.SomTargetResults;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somscreen.EvoBasics;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.SomQuality;
import org.NooLab.somscreen.SomScreening;

import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
 
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;

 
/**
 * this implementation claims to contain ensemble based feature selection
 * http://java-ml.sourceforge.net/api/0.1.6/
 * 
 * TODO   minimal node size is not corrected for , there are nodes with 2 items in it ...
 * 
 * TODO   correlational shift, PCA, decomposing "info collecting path", as in screening
 * 
 */
public class DSomCore {

	DSom dSom;
	
	SomProcessIntf somProcess;
	SomFluidAppGeneralPropertiesIntf sfProperties ;
	SomFluidMonoTaskIntf sfTask;
	
	ModelingSettings modset;
	DataSampler dataSampler = new DataSampler();
	
	int tvColumnIndex=-1, indexColumnIndex=-1 ;
	
	int[] usagevector ;
	int[] blacklistPositions ;
	
	ArrayList<Double> useIntensityVector;
	
	// contains methods for determining the usage vector, inclusive selections for 
	// evolutionary optimization 
	Assignates assignates ;
	
	ArrayList<Integer> sampleRecordIDs;
	
	int absoluteRecordCount ;
	int somSteps; // initial value is taken from ModelingSettings
	 
	double neighbourhoodRadius ;
	double mapRadius = 1.0;
	double timeConstant;
	double learningRate, initialLearningRate;
	
	
	
	ModelingSettings modelingSettings ;  
	ClassificationSettings  classifySettings ;
	
	SomTargetResults somResults;
	
	// ------------------------------------------
	// ArrUtilities arrutil = new ArrUtilities();
	PrintLog out;
	
	// ========================================================================
	public DSomCore(DSom dsom) {
		 
		dSom = dsom;
		sfTask = dSom.somTask ; 
		 
		somProcess = dSom.getSomProcessParent() ;
		sfProperties = somProcess.getSfProperties();
		
		// ... dSom.somData;
		out = dSom.out;
		
		// out.print(2, "requesting neighborhood for <11> from particle field via Som-Lattice");
		// dSom.somLattice.getNeighborhoodNodes(11) ; out.delay(200);
		// dSom.somLattice.getNeighborhoodNodes(15) ;
		
		dataSampler.setModelingSettings( dSom.modelingSettings );
		
		modelingSettings = dSom.modelingSettings ;
		classifySettings = modelingSettings.getClassifySettings() ; 
		
		prepareSomProcess();
		
	}
	
	public DSomCore(AstorSomField astorSomField) {
		// TODO Auto-generated constructor stub
	}
	// ========================================================================
	
	
	public void close(){
		
		// arrutil=null;
		if (sampleRecordIDs!=null)sampleRecordIDs.clear();
		sampleRecordIDs=null;
	}
	
	public int perform() throws Exception {
		int result = -1;
		
		sfTask = dSom.somTask ; 
		dSom.inProcessWait = true;
		
		if (dSom.inProcessWait==false){
			new DsomStarter(); // ?? switched off only for DEBUG !!! abc124
			result=0;
		}else{
			// if somtype ...) -> performAstor()
			
			if (modset.getSomType() == SomFluidProperties._SOMTYPE_PROB ){
				result = performAstor();
			}else{
				result = performDSom();	
			}
			
		}
		return result; 
	}

	// just for starting in its own thread, but hiding the Runnable interface from/for the outside
	class DsomStarter implements Runnable{
	
		Thread dsomThrd;
		
		public DsomStarter(){
			
			
			dsomThrd = new Thread(this,"dsomThrd");
			dsomThrd.start();
		}
		
		@Override
		public void run() {
			sfTask = dSom.somTask ; 
			
			try {
				
				performDSom();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//informing the observer
		
	} // inner class DsomStarter

	protected void prepareSomProcess(){
	
		
		
		DataTable dtable ;
		Variables variables ;
		
		MetaNodeIntf node;
		
		ArrayList<Variable> blacklist;
		
		
		// determine the usagevector, excluding index columns tv column
	
		modset = dSom.modelingSettings ;
	
		assignates = new Assignates( dSom );
		
		// TODO: set the scale accordingly ! (esp string variables  
		dtable = dSom.somData.getDataTable();
		
		dSom.somData.introduceBlackList();
		
		variables = dSom.somData.getVariables();
		 
		if (tvColumnIndex>=0){
			variables.setTvColumnIndex(tvColumnIndex) ; 
		}else{
			int ix = variables.getTvColumnIndex() ;
			if (ix<0){
				int st = dSom.sfProperties.getModelingSettings().getSomType() ; // == _SOMTYPE_MONO ? == NOT ok, == _SOMTYPE_PROB no tv required
			}else{
				tvColumnIndex=ix;
			}
		}
		if (indexColumnIndex>=0){
			variables.setIdColumnIndex( indexColumnIndex );
		}else{
			int ix = variables.getIdColumnIndex(); 
			if (ix<0){
				ix = dSom.somData.getDataTable().getColumnIndexOfType(0);
				if (ix>=0){
					indexColumnIndex = ix;
					variables.setIdColumnIndex( indexColumnIndex );
				}
			}
		}
		
		// note that the TV will be included into the usagevector, 
		// but it will be excluded from similarity considerations !
		// usagevector = prepareUsageVector( dtable, variables ); // ?????????? only the first time ?
		// is of int[]
		// THIS IS WRONG !!! in screening, it always would return to the original setting
		// usagevector = variables.getUseIndicatorArray() ;
		
		usagevector = dSom.somLattice.getSimilarityConcepts().getUseIndicatorArray() ;
		 
		
		int tix = variables.getTvColumnIndex() ;
		if (tix>=0){
			usagevector[tix]=-2;
		}
		//  
		blacklistPositions = getBlackListPositions(variables);
		// TODO: whitelist ?
		// ................................................
		 
		absoluteRecordCount = dSom.somData.getRecordCount() ;
		
		if (modset.getValidationActive()){
			// get a validation sample, and the remaining datasample
											out.print(3, "preparing samples ...");
			  
			// establish sampling for validation
			
			double samplePortion = modelingSettings.getValidationSettings().getPortion();
			
			// creating the samples for training and validation, will be stored in dataSampler
			dataSampler.createBasicModelingSamples( absoluteRecordCount, samplePortion );
			
		}else{
			dataSampler.createRecordIndexMasterList(absoluteRecordCount);
		}
		// also: we may split of an out of modeling sample
		// also: bagging samples from remaining training set
		
		// ................................................
		// should be saved here
		// dSom.somLattice.distributeIntensionalitySurface().prepareWeightVector()
		
		somSteps = modset.getMaxSomEpochCount();
		
		dSom.somLattice.setSomData( somProcess.getSomDataObject() ) ;
		
		
		// set blacklist, usevector to similarity object of lattice, and sim object of all nodes
		SimilarityIntf simIntf = null ;
		
		for (int i=0;i<dSom.somLattice.size();i++){
			
			node = dSom.somLattice.getNode(i);

			simIntf = node.getSimilarity();
			
			// simIntf.setUsageIndicationVector(usagevector);
			simIntf.setBlacklistIndicationVector(blacklistPositions);
			
			simIntf.setIndexTargetVariable(tvColumnIndex) ;
			simIntf.setIndexIdColumn(indexColumnIndex) ;
			
			ArrayList<Double>  uv = simIntf.getUsageIndicationVector();
			node.cleanInitializationByUsageVector( uv );
		} // i-> all nodes
		
		                                                   // simIntf = null ????
											out.print(4,"similarity obj = "+simIntf.toString());

		// TODO: createClassesDictionary() from target variable column, simulation file, or tg definition
		
	}

	private int[] getBlackListPositions(Variables variables) {
		int[] blacklistPositions = new int[variables.size()] ;
		ArrayList<String> blacks;
		int ix, notFound=0;
		String blackvarLabel;
		
		blacks = variables.getBlacklistLabels() ;
		
		for (int i=0;i<blacks.size();i++){
			
			blackvarLabel = blacks.get(i) ;
			ix = variables.getIndexByLabel(blackvarLabel) ;
			// there could be wild cards
			if (ix>=0){
				if (ix<blacklistPositions.length){
					blacklistPositions[ix] = 1;
				}
			}else{
				notFound++;
			}
		}
				
		return blacklistPositions;
	}
	
	
	public int[] prepareUsageVector(DataTable dtable, Variables variables) {
		DataTableCol col;
		Variable var;
		 
		int[] blacklistPositions = getBlackListPositions(  variables) ;
		int[] usagevector = new int[variables.size()] ;
		
		// get that from Variables !
		

		for (int i=0;i<usagevector.length;i++){
			
			var = variables.getItem(i);
			col = dtable.getColumn(i);
			
			blacklistPositions[i] = 0;
			if (col.getStatisticalDescription().getMvCount()> 0.92*col.size()){
				var.setIsEmpty(1) ;
			}
			
			if ((var.isIndexcandidate()) || (col.getDataFormat()==0)){
				usagevector[i] = 0; 
				if (indexColumnIndex<0){ indexColumnIndex=i;}
				var.setIndexcandidate(true) ;
				
				continue;
			}
			 
			if ((var.isTVcandidate()) || ( var.getLabel().contentEquals( modset.getActiveTvLabel() ))){
				usagevector[i] = 0;
				if ((var.isTV()) || ( var.getLabel().contentEquals( modset.getActiveTvLabel() ))){
					usagevector[i] = 1; 
					tvColumnIndex=i;
				}
				continue;
			}
			 
			if (col.getDataFormat()>=DataTable.__FORMAT_ORDSTR){
				usagevector[i] = 0; 
				continue;
			}
			
			if (col.getStatisticalDescription().getMvCount()> 0.11*col.size()){
				usagevector[i] = 0; 
				continue;
			}
			if (variables.getBlacklist().size()>0){
				
				boolean hb = (variables.getBlacklistLabels().contains(var.getLabel()) );
				if ( hb ){
					usagevector[i] = 0; 
					blacklistPositions[i] = 1;
					continue;
				}
			}
			
			
			usagevector[i] = 1;
		}

		
		return usagevector;
	}
	private int performAstor() throws Exception {
		
		int resultCode=-1;
		SomVariableHandling variableHandling;
		SomQuality sq ;
		
		
		variableHandling = new SomVariableHandling( dSom.somData, modelingSettings  );
		

		resultCode = executeSOM() ; // >0 == ok 
		 
		if (resultCode<0){
			return resultCode;
		}
		
		dSom.onCoreProcessCompleted(resultCode);// VirtualLattice@1f07597, each loop a different one!
		return resultCode; 
	}

	// ========================================================================
	
	
	private int performDSom() throws Exception{

		boolean done=false;
		int resultCode= -1;
		
		SomVariableHandling variableHandling;
		SomQuality sq ;
		
		
		variableHandling = new SomVariableHandling( dSom.somData, modelingSettings  );
		variableHandling.determineSampleSizes( dSom.somLattice.size() ) ;

		 
		
		// before starting with our L2-process, we need the info about ClassificationSettings.getTargetGroupDefinition()
		// which we have to set empirically if we are in multi-mode
		int tm = modset.getClassifySettings().getTargetMode();
		
		if (tm == ClassificationSettings._TARGETMODE_MULTI){ // TODO: needed an option which blocks the recalc of TGs! 
			// check if targetgoupdefs are false, or auto = true, if not: do nothing here, even if we have to stop 
			
			
			variableHandling.getEmpiricTargetGroups(  true ); 
			// there are different flavors of that, actually, it also can perform "adaptive binning" into a number of groups,
			// perhaps based on mono-variate clustering (in turn based on the spatial distribution of distances)
			
			
			double[][] tGdefinition = variableHandling.getTargetGroups();
			this.classifySettings.setTGdefinition(tGdefinition);
		}
		
		
		if (modset.getSomType() == SomFluidProperties._SOMTYPE_MONO ){
			// if we are "modeling" i.e. working guided by a target variable, we have to distribute the use vector
			 ArrayList<Double> usevector = null ;
			 MetaNode  node;
			 int  tix ;
			 
		}

		
		resultCode = executeSOM() ; // >0 == ok 
		 
		if (resultCode<0){
			return resultCode;
		}
		//dSom.resultsRequested=false;
		if (dSom.resultsRequested){
			// this will put the results into the "som": the lattice will know about the mode, 
			// the TV and the TG, and the nodes will know
			// about their ppv regarding those definitions
			somResults = new SomTargetResults( dSom, dataSampler, dSom.somLattice , modelingSettings );
		
			// performs a validation if the validation sample is present, and collects the results for both samples
			// results are then written to ???? 
			somResults.prepare(); // 
		}
		
		dSom.onCoreProcessCompleted(resultCode);// VirtualLattice@1f07597, each loop a different one!
		return resultCode;                      
	}
	
	/**
	 *  executing the SOM;
	 *  1. basically, this just organized the learning epochs, where "organizing" meanse
	 *     to determine the actual sample of records (only their indexes, of course).
	 *     The first epoch is just basic priming, the second more extended priming of the SOM ;
	 *     for that we need not to take all records (only about 3% or at least 100 records)
	 *  2. the initial learning rate and neighborhood parameters are adjusted, becoming
	 *     smaller and smaller with increasing epoch count,
	 *  3. the data are sent to the core SOM process, i.e. the SOM now perceives the data vectors,
	 *     according to the settings of use vectors and weight vectors
	 *    
	 */
	private int executeSOM() throws Exception{
		
		int result = -1;
		DSomDataPerception somDataPerception = null;
		double learningRate;
		
		int  limitforConsideredRecords = 0, actualRecordCount  ;
		
		int currentEpoch =0;
		 
		
		try{
			modset = dSom.modelingSettings ;	

			
ArrayList<Double>  uv1 = dSom.somLattice.getNode(0).getIntensionality().getUsageIndicationVector();
ArrayList<Double>  uv2 = dSom.somLattice.getNode(0).getSimilarity().getUsageIndicationVector();
			
String str1 = ArrUtilities.arr2Text(uv1, 1);
String str2 = ArrUtilities.arr2Text(uv2, 1);

out.print(4, "execute SOM, use vectors (int) : "+str1);
out.print(4, ".                        (sim) : "+str2);


			absoluteRecordCount = this.dataSampler.getSizeTrainingSet();
			actualRecordCount =  absoluteRecordCount; // we ALWAYS have a training sample available! ;
			
			if (absoluteRecordCount<=1){
				throw(new Exception("critical problem: absoluteRecordCount = "+absoluteRecordCount));
			}
			
			initialLearningRate = modset.getInitialLearningRate();
			learningRate = initialLearningRate; 
					
			// initial maximum radius for influence; it is covering the map only for rather small maps.
			// in very large maps there are several mechanisms (pre-sampling, multi-winners) that 
			// help to avoid a large initial radius
			// TODO make it more adaptive (asymptotically to a max size of N nodes)
			mapRadius = 1.2* (Math.sqrt( dSom.somLattice.size() )/2.0) ; 
			
	        // later we will have the possibility for particular block sampling schemes, 
			// utilizing the dataSampler object
			
			if (limitforConsideredRecords > 1) {
				actualRecordCount = limitforConsideredRecords;
			}
			
			int maxRC = dSom.modelingSettings.getAbsoluteRecordLimit() ;
			int globalLimit =  dataSampler.getGlobalLimit(); // 1239 ;
			if ( maxRC >10){
				if ((globalLimit<0) || ((globalLimit>10) && (globalLimit>maxRC))){
					globalLimit = maxRC ; 
					dataSampler.setGlobalLimit(globalLimit);
				}
			}	
											double[] vv = dSom.somData.getNormalizedSomData().getRowValues(10) ;
											int n=vv.length;
											int outlevel=2;
											if ((sfTask.getCounter()>3) && (dSom.volatileSampling==false)){
												outlevel=3;
											}
											if (displayProgress(0)){
												out.print(outlevel, "Som learning is starting on "+dataSampler.getTrainingSet().size()+" records from training sample.");
											}
											
			while ((currentEpoch < somSteps) && (dSom.getUserbreak()==false)) {
				 	/*
				 	 _SOMDISPLAY_PROGRESS_NONE  = -1 ;
				 	 
					 _SOMDISPLAY_PROGRESS_BASIC =  0 ;
					 _SOMDISPLAY_PROGRESS_STEPS =  1 ;
					 _SOMDISPLAY_PROGRESS_PERC  =  2 ;   getShowSomProgressMode()
					*/
											if (displayProgress(2)){
												out.print( 2, "Som learning epoch... "+(currentEpoch+1));
											}else{
												if ((currentEpoch>=3) && (displayProgress(1))){
													out.print( 2, "Som learning epoch... "+(currentEpoch+1));
												}
											}
if (currentEpoch>=3){
	int nn;
	nn=0;
}
				// before (re-)starting, we have to reset the node statistics, and the list of entries, except the weights of the vector
				clearNodesExtension( currentEpoch );
				
				// sample size is dependent on epoch and number of records
				// adjustSampleAndSteps( currentEpoch, absoluteRecordCount, globalLimit ); // 
				adjustSampleAndSteps( currentEpoch, dataSampler.getTrainingSet(), globalLimit ); //
				
				actualRecordCount = sampleRecordIDs.size() ;
				
				adjustIntensityParameters( currentEpoch, actualRecordCount ) ;
				 
				// now feeding the data into the somLattice
				somDataPerception = new DSomDataPerception( dSom, sampleRecordIDs );
				
				somDataPerception.setLoopParameters( currentEpoch, somSteps );
				somDataPerception.setDynamicsParameters( learningRate, timeConstant , neighbourhoodRadius) ;
										//  epoch 1 :       0.2           411             6.5
				somDataPerception.go();
				
				somDataPerception.closeThreads(); 
				
				currentEpoch++;
				
				// consoleDisplay();
			} // currentEpoch -> maxSomEpochCount 
				
									
			
			result=0 ;
			sampleRecordIDs.clear();
			sampleRecordIDs=null;
			
		}catch(Exception e){
			if (LogControl.Level >=2){
				// e.printStackTrace();
			}else{
				out.printErr(1, e.getMessage());
			}
			e.printStackTrace();
			result = -7 ;
		}
		if (result==0){
			int outlevel=2;
			if (sfTask.getCounter()>3)outlevel=3;
			out.print(outlevel, "basic Som learning has been finished ("+result+").");
		}
		if (somDataPerception!=null){
			somDataPerception.clear() ;
		}
		return result;
	}


	private boolean displayProgress(int level) {
		boolean rB=false;
		
		if (level<=0){
			rB = (sfProperties.getShowSomProgressMode()>= SomFluidProperties._SOMDISPLAY_PROGRESS_NONE) ;
		}
		if (level==1){
			rB = (sfProperties.getShowSomProgressMode()>= SomFluidProperties._SOMDISPLAY_PROGRESS_BASIC) ;
		}
		if (level==2){
			rB = (sfProperties.getShowSomProgressMode()>= SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS) ;
		}
		if (level>=3){
			rB = (sfProperties.getShowSomProgressMode()> SomFluidProperties._SOMDISPLAY_PROGRESS_PERC) ;
		}
		
		
		
		return rB;
	}
	private void restoreModelFromHistory( SomScreening somScreening, int bestHistoryIndex) throws Exception {
		
		int _bestHistoryIndex, n ;
		String str;
		ArrayList<Integer> indexes ;
		ArrayList<Double> histUsagevector, uv ;

		EvoBasics evoBasics;
		EvoMetrices evoMetrices;
		SomTargetedModeling targetMod;
		SomTargetResults somResults;
		
		_bestHistoryIndex = bestHistoryIndex;
		evoMetrices = somScreening.getEvoMetrices();
		
		evoBasics = somScreening.getEvoBasics() ;
		
		if (_bestHistoryIndex<0)_bestHistoryIndex= evoBasics.getBestModelHistoryIndex() ;
		
		histUsagevector = evoMetrices.getBestResult().getUsageVector() ; 

		indexes = evoMetrices.determineActiveIndexes(histUsagevector);
		str = ArrUtilities.arr2Text(indexes) ;
										out.print(2,"restoring best model (history index :"+_bestHistoryIndex+"), variable indices : "+str);
		
 
		uv = dSom.somLattice.getSimilarityConcepts().getUsageIndicationVector();
		n = dSom.somLattice.getSimilarityConcepts().getUsageIndicationVector().size();
		
		dSom.somLattice.reInitNodeData() ;
		
		dSom.somLattice.getSimilarityConcepts().setUsageIndicationVector(histUsagevector) ;
		executeSOM() ; // 
		
		somResults = new SomTargetResults( dSom, dataSampler, dSom.somLattice, modelingSettings );
		somResults.prepare();
		
	}

	
	class ExecuteSom{
		
		public ExecuteSom(){
			
		}
		
		
		public void go(){
			
		}
	}
	// ------------------------------------------------------------------------
	/**
	 * 
	 * the ClassesDictionary contains the 0/1 class indicator<br/><br/>
	 * we need to do it close to the SOM, since the class assignment could change while learning:<br/>
	 * there are learning modes, which optimize the SOM, or some stats params of it, or are
	 * dependent on the node profiles... in such cases, the classes assignment can change ONLINE
	 * WHILE executing the SOM!!  <br/><br/>	 
	 *  
	 * 
	 * besides the binary indicator, it also contains<br/>
	 * a vector with ID, true (physical record number), kind of sample (in TV modeling), 
	 * 
	 */
	private void createClassesDictionary(){
		
		// somClassesDictionary = new ClassesDictionary();
		
		// providing the reference to the modelingSettings object
		// modset.setSomClassesDictionary( somClassesDictionary ) ;
	}


	private void  adjustSampleAndSteps( int currentEpoch, ArrayList<Integer> baseSet, int globalLimit)  {
		
		
		int dsomSize,targetcount, actualRecordCount ;
		double dsomT= 2.0;
		boolean adjustDefaultStepCount=false;
		
		actualRecordCount = baseSet.size() ;
		
		dsomSize = dSom.getSize();
		dsomT = Math.log10( 10+dsomSize ) ;
		
		if ((globalLimit>=11) && (globalLimit < baseSet.size() )){
			targetcount = globalLimit;
		}else{
			targetcount = baseSet.size();
		}
		
		  
		sampleRecordIDs = dataSampler.createEffectiveRecordList( DataSampler._SAMPLE_TRAINING, targetcount, currentEpoch, somSteps ); // actualRecordCount
		
		double x = ((double)actualRecordCount)/ ((double)dsomSize/dsomT);
		adjustDefaultStepCount = x < 11.0;
		
		if ((currentEpoch <= 1) || (adjustDefaultStepCount)){
			if (actualRecordCount>0){
				somSteps = somSteps + (int)(Math.round(( (double)dsomSize)/((double)actualRecordCount)));
			}else{
				somSteps = 1;
			}
			if (somSteps<3)somSteps=3;
			if (somSteps>5)somSteps=5;
		}
	
	}
	
	@SuppressWarnings("unused")
	private void  adjustSampleAndSteps( int currentEpoch, int actualRecordCount, int globalLimit)  {
		int dsomSize,targetcount ;
		double dsomT= 2.0;
		boolean adjustDefaultStepCount=false;
		
		dsomSize = dSom.getSize();
		dsomT = Math.log10( 10+dsomSize ) ;
		
		if ((globalLimit>15) && (globalLimit < absoluteRecordCount)){
			targetcount = globalLimit;
		}else{
			targetcount = absoluteRecordCount;
		}
		
		// contians potential info about master samples, absolut record count limits, etc.
		dataSampler.setModelingSettings( this.dSom.modelingSettings ) ;
		
		sampleRecordIDs = dataSampler.createEffectiveRecordList( 0, targetcount, currentEpoch, somSteps ); // actualRecordCount
		double x = ((double)actualRecordCount)/ (dsomSize/dsomT);
		adjustDefaultStepCount = x < 11.0;
		
		if ((currentEpoch <= 1) || (adjustDefaultStepCount)){
			somSteps = somSteps + (int)(Math.round(( dsomSize)/(actualRecordCount)));
			if (somSteps<3)somSteps=3;
			if (somSteps>5)somSteps=5;
		}
	
	}
 
	
	private void adjustIntensityParameters( int currentEpoch, int actualRecordCount){
		
		double d, _f, speed2=1.0;
		
		double mapsize ;
		int nodecount = dSom.somLattice.size();
		
		mapsize = Math.sqrt( nodecount ) ;
			
		mapRadius = mapsize * 0.4 ;
		
		if (mapRadius > 1.3) {
			d = 0;
		} else {
			d = 1;
		}
		//                            n = 100 -> 74.5  , 250 -> 107, 850 -> 160
		// timeConstant =  ((d + actualRecordCount * Math.pow(mapRadius, 0.25)) / (d + Math.log10(mapRadius)));
        timeConstant =  ((d + Math.log( actualRecordCount)* Math.log( actualRecordCount) * Math.pow(mapRadius, 0.45)) / (d + Math.log10(mapRadius)));
		
		if (somSteps + 1 > 1) {
	
			_f = (float) (1.0f - ((0.6 * (currentEpoch - 1.0f) / (somSteps)) + 0.2f))/speed2;
			
			neighbourhoodRadius = (double) (1.1f * mapsize / (2.0f) * _f);
	
			learningRate = (double) ( (initialLearningRate *(currentEpoch))* (1.0f * _f) + learningRate + 0.02f);
		}
	                                     // out.print(2," _f : "+_f+ "   NeighbourhoodRadius "+NeighbourhoodRadius+ "    LearningRate "+LearningRate);
		
		 
	}

 
	
	private void clearNodesExtension( int currentEpoch) {
		
		NodeTask task;
		LatticeFutureVisor latticeFutureVisor;
		int n=0;
		MetaNodeIntf  node;
		ExtensionalityDynamicsIntf  ext;
		IntensionalitySurfaceIntf  ints;
		
		try{
			node=null;
			n = dSom.somLattice.size();
			for (int i=0;i<n;i++){
			
				node = dSom.somLattice.getNode(i);
				
				ext  = node.getExtensionality();
				ints = node.getIntensionality() ;
				
				ext.clear();
				
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		n=n+1-1;
		if (currentEpoch >= 1) {
			 // send a message to all nodes
			
			
			
			/*
			latticeFutureVisor = new LatticeFutureVisor( dSom.somLattice,  NodeTask._TASK_SETVAR );
			 
			
			task = new NodeTask( NodeTask._TASK_SETVAR, (Object)so.encode( (Object)vars.getActiveVariableLabels()) );
			// do it for all nodes
			dSom.somFluidParent.notifyAllNodes( task );
			
			latticeFutureVisor.waitFor();
			*/
		}
	}

	/**
	 * 
	 * extracting the profiles from the SomLattice into a simple table.
	 * 
	 * this then may be used for SomSprite or for SomIdeals
	 * 
	 * 
	 */
	public SomMapTable exportSomMapTable() {
		
		SomMapTable smt = new SomMapTable();
		/*
		 	double[][] values = new double[0][0] ; 
			String[]   variables = new String[0] ;
		 */
		
		boolean hb, nodeIsApplicable;
		String varLabel;
		int refNodeCount=0, vix, tvindex=-1 ;
		
		ArrayList<MetaNode> nodes;
		MetaNode node;
		
		nodes = dSom.somLattice.getNodes() ;
		ProfileVectorIntf  profileVector; 
		
		ArrayList<Double> pValues;
		Variables  variables;
		Variable variable ;
		ArrayList<Variable> varList ;
		
		ArrayList<String>  nodeVarStr ;// = new ArrayList<String>();
		ArrayList<String>  activeVarStr = new ArrayList<String>();
		ArrayList<String>  compoundVarStr = new ArrayList<String>();
		ArrayList<Double>  activeProfileValues = new ArrayList<Double>() ;
		double activeProfileValue;
		ArrayList<Double> useIndicators , latticeuseIndicators;
		
		variables = this.dSom.somData.getVariables() ;
		// not implemented: varList = variables.getActiveVariables();
		
		latticeuseIndicators = dSom.somLattice.getSimilarityConcepts().getUsageIndicationVector() ;
		
		/*
		 * we need two loops, since compared/extracted nodes may be of different structure 
		 * the first one finding the compound vector that can be used to describe all nodes,
		 */
		for (int i=0;i<nodes.size();i++){
			
			node = nodes.get(i) ;
			useIndicators = node.getSimilarity().getUsageIndicationVector();
			// ATTENTION this does NOT contain the target variable
			// also: blacklist..., 
			
			profileVector = node.getIntensionality().getProfileVector(); // in "exportSomMapTable()"
			 
			
			nodeVarStr = node.getIntensionality().getProfileVector().getVariablesStr() ;
			
			// we do this for each node, though in most cases this is redundant, 
			// -> , nodes are NOT necessarily showing the same assignates/features !!
			for (int v=0;v<nodeVarStr.size();v++){
				
				// exclude variables that have -1 as profile values (mv portion too large)
				varLabel = nodeVarStr.get(v) ;
				
				vix = variables.getIndexByLabel( varLabel ); 
				if (vix<0){continue;}
				
				variable = variables.getItem(vix) ;
				
				 
				hb = true;
				
				// hb =  variable.isTV(); // variable.isUsed() ||
				{
					if ((hb) || (variable.isTV() )){
						hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) && (variable.isTVcandidate()==false) ;	
					}
				}
				
				if ((hb) && ((useIndicators.get(v)>0.0) || (useIndicators.get(v)==-2.0))){
					varLabel = variable.getLabel() ;
					if (compoundVarStr.indexOf( varLabel )<0 ){
						compoundVarStr.add( variable.getLabel() ) ;
						if (variable.isTV()){
							tvindex = v; 
						}
					}
				}
			} // v-> all variables
			
			// we might use some filter, such as MV in profile, size of node, or a filter by value of any variable
			// such filters may reduce the number of nodes we are effectively referring to 
			refNodeCount++;
			
		} // i-> all nodes

		if (compoundVarStr.size()==0){
			return smt;
		}
		
		String tvLabel = dSom.getSomData().getVariables().getTargetVariable().getLabel() ;
		if (compoundVarStr.indexOf(tvLabel)<0){
			compoundVarStr.add(tvLabel) ;
			tvindex = compoundVarStr.size()-1 ;
		}
		if (tvindex<0){
			tvindex = compoundVarStr.indexOf(tvLabel);
		}
			
		smt.values = new double[refNodeCount][compoundVarStr.size()] ; 
		smt.variables = new String[ compoundVarStr.size()] ; 
		// that would be wrong!
		// smt.tvIndex = dSom.getSomData().getVariables().getTvColumnIndex() ;
		smt.tvIndex = tvindex ; 
		int rnc=0;
		
		for (int i=0;i<nodes.size();i++){
			
			node = nodes.get(i) ;
			useIndicators = node.getSimilarity().getUsageIndicationVector();
			// also: blacklist...
			
			nodeIsApplicable=true;
			// apply the filter that acts on the node
			if (nodeIsApplicable==false){
				continue;
			}
			
			profileVector = node.getIntensionality().getProfileVector(); // in "exportSomMapTable()"
			 
			
			// we export used vars + TV
			pValues = profileVector.getValues() ;
			
			// we do this for each node, though in most cases this is redundant, 
			// et, nodes are NOT necessarily showing the same assignates/features !!
			for (int v=0;v<variables.size();v++){
				
				variable = variables.getItem(v) ;
				
				hb = (useIndicators.get(v)>0.0) || (variable.isTV());
				if (hb){
					hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) ;	
				}
				
				if (hb){
					varLabel = variable.getLabel() ;
					
					activeVarStr.add( varLabel ) ;
					
					// activeProfileValue = pValues.get(v) ;
					// activeProfileValues.add(activeProfileValue) ;
					
					vix = compoundVarStr.indexOf(varLabel) ;
					if (vix>=0){
						smt.variables[vix] = varLabel;
						smt.values[rnc][vix] = pValues.get(v);
						if (variable.isTV()){
							smt.tvIndex = vix;
						}
					}
				}
				
			} // v-> all variables
			rnc++;
			
			
		} // i-> all nodes
		
		
		
		return smt;
	}


	private void consoleDisplay() {
		MetaNodeIntf  node;
		ArrayList<Double> values;
		int nrc;
		String str ;
		
		out.print(2,true,"\nintensional profiles of nodes:");
		
		for (int i=0;i< dSom.somLattice.size();i++){
			node = dSom.somLattice.getNode(i) ;
			
			values = node.getIntensionality().getProfileVector().getValues();
			nrc = node.getExtensionality().getCount() ;
		
			
			str = modelingSettings.arrutil.arr2text( values ,2 ) ;
			out.print(2,true, "i "+i+"  ---  [n:"+nrc+"] "+str ); // output suppressing the prefix ...
		}
		out.print(2,true," - - - - - \n");
	}
	// ========================================================================
	
	
	public double getMapRadius() {
		 
		return mapRadius;
	}


	public DataSampler getDataSampler() {
		return dataSampler;
	}


	public DSom getParent() {
		return dSom;
	}


	/**
	 * @return the tvColumnIndex
	 */
	public int getTvColumnIndex() {
		return tvColumnIndex;
	}


	/**
	 * @param tvColumnIndex the tvColumnIndex to set
	 */
	public void setTvColumnIndex(int tvColumnIndex) {
		this.tvColumnIndex = tvColumnIndex;
	}


	/**
	 * @return the indexColumnIndex
	 */
	public int getIndexColumnIndex() {
		return indexColumnIndex;
	}


	/**
	 * @param indexColumnIndex the indexColumnIndex to set
	 */
	public void setIndexColumnIndex(int indexColumnIndex) {
		this.indexColumnIndex = indexColumnIndex;
	}
	
	
	
	
	
	
	
	
	
	
}

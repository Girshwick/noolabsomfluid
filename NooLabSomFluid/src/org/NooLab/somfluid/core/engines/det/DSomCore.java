package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
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
import org.NooLab.somscreen.SomScreening;
import org.NooLab.somsprite.SomSprite;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;

 

public class DSomCore {

	DSom dSom;
	
	
	ModelingSettings modset;
	DataSampler dataSampler = new DataSampler();
	
	
	int targetVarColumn= -1;
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
	
	
	// ------------------------------------------
	ArrUtilities arrutil = new ArrUtilities();
	PrintLog out;
	
	// ========================================================================
	public DSomCore(DSom dsom) {
		 
		dSom = dsom;
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
	// ========================================================================


	public void perform() {
		 
		new DsomStarter(); // ?? switched off only for DEBUG !!! abc124
		
		// performDSom();
	}


	// ========================================================================
	
	
	// jst for starting in its own thread, but hiding the Runnable interface from/for the outside
	class DsomStarter implements Runnable{
	
		Thread dsomThrd;
		
		public DsomStarter(){
			
			dsomThrd = new Thread(this,"dsomThrd");
			dsomThrd.start();
		}
		
		@Override
		public void run() {
			performDSom();
		}
		
	} // inner class DsomStarter

	protected void prepareSomProcess(){
	
		int tvColumnIndex=-1, indexColumnIndex=-1 ;
		
		DataTable dtable ;
		DataTableCol col;
		
		Variables variables ;
		Variable var;
		
		MetaNodeIntf node;
		
		ArrayList<Variable> blacklist;
		
		
		// determine the usagevector, excluding index columns tv column
	
		modset = dSom.modelingSettings ;
		
		dtable = dSom.somData.getDataTable();
	
		assignates = new Assignates( dSom ); 
		
		variables = dSom.somData.getVariables();
		
		usagevector = new int[variables.size()] ;
		blacklistPositions = new int[variables.size()] ;
		
		
		// note that the TV will be included into the usagevector, 
		// but it will be excluded from similarity considerations !
		
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
			 
			if (col.getDataFormat()>=8){
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

		// ................................................
		// int _n = sampleRecordIDs.size();
		absoluteRecordCount = dSom.somData.getRecordCount() ;
		
		if (modset.getValidationActive()){
			// get a validation sample, and the remaining datasample
											out.print(2, "preparing samples ...");
			 
			// establish sampling for validation
			
			double samplePortion = modelingSettings.getValidationSettings().getPortion();
			
			// creating the samples for training and validation, will be stored in dataSampler
			dataSampler.createBasicModelingSamples( absoluteRecordCount, samplePortion );
			
		}else{
			dataSampler.createRecordIndexMasterList(absoluteRecordCount);
		}
		// also: wwe may split of an out of modeling sample
		// also: bagging samples from remaining training set
		
		// ................................................
		// should be saved here
		// dSom.somLattice.distributeIntensionalitySurface().prepareWeightVector()
		
		somSteps = modset.getMaxSomEpochCount();
		
		
		// set blacklist, usevector to similarity object of lattice, and sim object of all nodes
		SimilarityIntf simIntf = null ;
		
		for (int i=0;i<dSom.somLattice.size();i++){
			
			node = dSom.somLattice.getNode(i);

			
			
			simIntf = node.getSimilarity();
			
			simIntf.setUsageIndicationVector(usagevector);
			simIntf.setBlacklistIndicationVector(blacklistPositions);
			
			simIntf.setIndexTargetVariable(tvColumnIndex) ;
			simIntf.setIndexIdColumn(indexColumnIndex) ;
			
			
			node.cleanInitializationByUsageVector( simIntf.getUsageIndicationVector() );
		} // i-> all nodes
		
		
											out.print(4,"similarity obj = "+simIntf.toString());

		// TODO: createClassesDictionary() from target variable column, simulation file, or tg definition
		
	}

	// ========================================================================
	
	
	
	private void performDSom(){
		
		int loopcount=0;
		boolean done=false;
		
		SomTargetResults somResults;
		
		SomSprite somSprite ;
		SomScreening somScreening;
		
		SomMapTable somMapTable ;
		
		
		
		while ((done==false) && (dSom.getUserbreak()==false)){
		
			// the initial execution of the SOM
			// this is a simple run of the SOM. no sprites, for evolutionary optimization
			// which we could add here in this loop below. (loop level L3/L4)
			// about loop levels see:
			// http://theputnamprogram.wordpress.com/2011/12/21/technical-aspects-of-modeling/
				
			done = executeSOM()>=0 ;
			// will be put into a class
			//  new ExecuteSom( params ).go().prepareResults() ;
			// this also includes SomBags !!!
			
			// ......................................
			
			somResults = new SomTargetResults( dSom, dataSampler, modelingSettings );
			
			somResults.prepare();

			consoleDisplay();
			// TODO: release event message
			
			// ......................................
			
			if (modset.getMaxL2LoopCount()>0){
				// the Level 2 loop
				/*
				 * note that the SomFluid instance always contains the full spectrum of tools, yet,
				 * it behaves as such or such (Som, Sprite, Optimizer, transformer), according to the request.
				 * 
				 * L2 loops make sense only WITH sprite and screening...
				 */
				
				if ((modset.getSpriteAssignateDerivation() )&& (dSom.getUserbreak()==false)){

					// create instance
					somSprite = new SomSprite( dSom , modelingSettings );
				 
					// export maptable, or data, dependent on record number, to the sprite
					somMapTable = exportSomMapTable();
 					
					somSprite.acquireMapTable( somMapTable );
					
					somSprite.startSpriteProcess(1); 
					// 1 = will wait for completion , but may react to messages and requests
					
					// now integrate new variables, introduce it both on raw level as well as on transformed lavel
					// try to reduce it to the raw level
					
					
				} // SpriteAssignateDerivation() ?
				
				if ((modset.getEvolutionaryAssignateSelection() ) && (dSom.getUserbreak()==false)){
					/*
					 *  in case of evolutionary modeling we need to integrate
					 *  - validation, for calculating the cost function
					 *  - sub-sampling = using significantly decreased samples for evolutionary optimization
					 *                   we prepare a set of samples which we then assign to the modeling runs?
					 *               
					 *  basically, it is a meta process that performes executeSOM()
					 *  -> put this execsom into a class 
					 *  (new ExecuteSom( params )).go().prepareResults() ;
					*/
					
					somScreening = new SomScreening( dSom , modelingSettings );
					somScreening.setModelResultSelection( new int[]{SomScreening._SEL_TOP, SomScreening._SEL_DIVERSE} ) ;
					somScreening.setModelResultSelectionSize(20) ;
					somScreening.startScreening();
					// somScreening will reference settings and results for the collection of models 
				}	
				
				if (modelingSettings.getSomCrystalization() ){
					
				}
			} // any L2 loop ?
			
			 
			loopcount++;
			
			if (loopcount> modset.getMaxL2LoopCount()){ // default = -1 == off;  (modset.getMaxL2LoopCount()>0) &&
				done = false;
			}
		  
			// TODO: if there was no effect of sprite and screen, then stop the L2 loop anyway,
			
			
		} // done ?
	}
	
	
	class ExecuteSom{
		
		public ExecuteSom(){
			
		}
		
		
		public void go(){
			
		}
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
	private int executeSOM(){
		
		int result = -1;
		DSomDataPerception somDataPerception;
		double learningRate;
		
		int  limitforConsideredRecords = 0, actualRecordCount  ;
		
		int currentEpoch =0;
		 
		
		try{
			modset = dSom.modelingSettings ;	
			
			
			// absolute_record_count has been set in "setData(SomDataObject)" below...
			absoluteRecordCount = this.dataSampler.getSizeTrainingSet();
			actualRecordCount =  absoluteRecordCount; // we ALWAYS have a training sample available! ;
			
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
											out.print(1, "Som learning is starting on "+dataSampler.getTrainingSet().size()+" records from training sample.");
			
			while ((currentEpoch < somSteps) && (dSom.getUserbreak()==false)) {
											out.print(1, "Som learning epoch... "+(currentEpoch+1));
											
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
				currentEpoch++;
				
				// consoleDisplay();
			} // currentEpoch -> maxSomEpochCount 
				
											out.print(1, "Som learning has been completed.");
			
			result=0 ;
			
		}catch(Exception e){
			e.printStackTrace();
			result = -7 ;
		}
		if (result==0){
			out.print(1, "Basic Som learning has been finished.");
		}
		return result;
	}

 

	// ========================================================================
	
	 
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
		
		if ((globalLimit>15) && (globalLimit < baseSet.size() )){
			targetcount = globalLimit;
		}else{
			targetcount = baseSet.size();
		}
		
		  
		sampleRecordIDs = dataSampler.createEffectiveRecordList( DataSampler._SAMPLE_TRAINING, targetcount, currentEpoch, somSteps ); // actualRecordCount
		
		double x = ((double)actualRecordCount)/ (dsomSize/dsomT);
		adjustDefaultStepCount = x < 11.0;
		
		if ((currentEpoch <= 1) || (adjustDefaultStepCount)){
			somSteps = somSteps + (int)(Math.round(( dsomSize)/(actualRecordCount)));
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

 
	
	private void clearNodesExtension( int currentEpoch){
		
		NodeTask task;
		LatticeFutureVisor latticeFutureVisor;
		
		MetaNodeIntf  node;
		ExtensionalityDynamicsIntf  ext;
		IntensionalitySurfaceIntf  ints;
		
		for (int i=0;i<dSom.somLattice.size();i++){
		
			node = dSom.somLattice.getNode(i);
			
			ext  = node.getExtensionality();
			ints = node.getIntensionality() ;
			
			ext.clear();
			
		}
		
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
	private SomMapTable exportSomMapTable() {
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
		ArrayList<Double> useIndicators ;
		
		variables = this.dSom.somData.getVariables() ;
		// not implemented: varList = variables.getActiveVariables();
		
		/*
		 * we need two loops, since compared/extracted nodes may be of different structure 
		 * the first one finding the compound vector that can be used to describe all nodes,
		 */
		for (int i=0;i<nodes.size();i++){
			
			node = nodes.get(i) ;
			useIndicators = node.getSimilarity().getUsageIndicationVector();
			// also: blacklist..., 
			
			profileVector = node.getIntensionality().getProfileVector();
			
			nodeVarStr = node.getIntensionality().getProfileVector().getVariablesStr() ;
			
			// we do this for each node, though in most cases this is redundant, 
			// et, nodes are NOT necessarily showing the same assignates/features !!
			for (int v=0;v<nodeVarStr.size();v++){
				
				// exclude variables that have -1 as profile values (mv portion too large)
				varLabel = nodeVarStr.get(v) ;
				
				vix = variables.getIndexByLabel( varLabel ); if (vix<0){continue;}
				variable = variables.getItem(vix) ;
				
				hb = true;
				
				// hb =  variable.isTV(); // variable.isUsed() ||
				if ((hb) || (variable.isTV() )){
					hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) && (variable.isTVcandidate()==false) ;	
				}
				
				if ((hb) && (useIndicators.get(v)>0.0)){
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
		smt.values = new double[refNodeCount][compoundVarStr.size()] ; 
		smt.variables = new String[ compoundVarStr.size()] ; 
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
			
			profileVector = node.getIntensionality().getProfileVector();
			
			// we export used vars + TV
			pValues = profileVector.getValues() ;
			
			// we do this for each node, though in most cases this is redundant, 
			// et, nodes are NOT necessarily showing the same assignates/features !!
			for (int v=0;v<variables.size();v++){
				
				variable = variables.getItem(v) ;
				
				hb = useIndicators.get(v)>0.0 ;
				if (hb){
					hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) ;	
				}
				
				if (hb){
					varLabel = variable.getLabel() ;
					
					activeVarStr.add( varLabel ) ;
					
					activeProfileValue = pValues.get(v) ;
					activeProfileValues.add(activeProfileValue) ;
					
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
	
	
	
	
	
	
	
	
	
	
}

package org.NooLab.somfluid.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.*;

import org.NooLab.somfluid.*;
import org.NooLab.somfluid.properties.* ;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.util.PowerSetSpringSource;

 
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.*;
import org.NooLab.somfluid.core.engines.det.results.*;

import org.NooLab.somscreen.*;
import org.NooLab.somsprite.*;
import org.NooLab.somtransform.CandidateTransformation;
import org.NooLab.somtransform.SomTransformer;
import org.apache.commons.collections.CollectionUtils;





/**
 * 
 * behaves like SomFluid for its child "SomTargetedModeling{}"
 * 
 * note that the particle field remains the same, but the event sink is reestablished anew each time
 * when the child has been created 
 * 
 * TODO: - checking the top metrices against different samples and selecting the most robust one
 *       - checking a model where sprite variables are replaced by their raw parents
 *         - one by one
 *         - all at once
 *         -> this provides the argument that the sprite contains more information that the mere difference (opposing Minsky's difference engine)
 *          
 */
public class ModelOptimizer implements SomHostIntf, ProcessCompletionMsgIntf{

	SomFluid somFluid ;
	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
	SomDataObject somDataObj ;
	SomProcessIntf somProcess;
	
	SomTransformer somTransformer;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	SomModelDescription modelDescription ;
	ParetoPopulationExplorer populationExplorer;
	Coarseness coarseness;

	RobustModelSelector robustModel; 
	SomOptimizerXmlReport xmlReport;
	 
	
	int numberOfRuns = -1, dependenciesDepth=-1 ;
	
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();
	VariableSubsets subsets = new VariableSubsets();
	ArrayList<OptimizerProcess> processes ;
	
	ModelOptimizer modOpti;
	int resumeMode;
	
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;

	SpelaResults spelaResults ;
	
	
	PrintLog out ; 
	ArrUtilities arrutil = new ArrUtilities();
	public ModelProperties mozResults;
	ArrayList<Integer> freshlyAddedVariables = new ArrayList<Integer> ();
	ArrayList<Integer> allAddedVariables = new ArrayList<Integer> ();
	
	// ========================================================================
	public ModelOptimizer(  SomFluid somfluid, 
						    SomFluidTask task,
							SomFluidFactory factory ) {
		//
		somFluid = somfluid;
		sfTask = task;
		sfFactory = factory;
	
		
		sfProperties = sfFactory.getSfProperties() ;
		
		modelingSettings = sfFactory.getSfProperties().getModelingSettings();
		optimizerSettings = modelingSettings.getOptimizerSettings();
			
		numberOfRuns = sfTask.getNumberOfRuns();
		dependenciesDepth = sfTask.getDerivatesDepth();
		
		out = sfFactory.getOut() ;
		
		sfTask.setCompleted(false);
		sfTask.setSomHost(modOpti) ;
		modOpti = this;
		
		resumeMode = sfTask.getResumeMode() ;
		
		spelaResults = new SpelaResults();
		
		xmlReport = new SomOptimizerXmlReport( this );
		
		prepare();
	}
	
	
	
	private void prepare() {
		
		boolean transformerModelLoaded;
		// somDataObj = somFluid.getSomDataObject(index) ;
		
		// if enforced then somDataObj.clear();
		try {
			
			if (sfTask.getResumeMode()>=1){
				// load the SomDataObject
				sfTask.setResumeMode(0); // switch it OFF !! the "SimpleSingleModel" object also checks this var
				
				somDataObj = SomDataObject.loadSomData(sfProperties);
				
				somFluid.getSomDataObjects().clear() ;
				somFluid.addSomDataObjects(somDataObj);
				somDataObj = somFluid.getSomDataObject(0) ;
				somDataObj.reestablishObjects();
				
				somDataObj.setOut(out) ;
				somTransformer = new SomTransformer( somDataObj, sfProperties );
				somDataObj.setTransformer(somTransformer) ;
				somDataObj.setFactory(sfFactory) ;
				
				// Variables ???
				
			}else{
				if (sfTask.getResumeMode() == 0) {
					somDataObj = somFluid.loadSource(""); // somTransformer datatablenormalized ???
					// source is also defined in properties
				}
			}
			 
			
			processes = new ArrayList<OptimizerProcess>();
			OptimizerProcess process;
			int n = 1;

			
			somTransformer = somDataObj.getTransformer();
			// StackeTransformations still empty....  different somDataObj ???

			// if not initialized... first test whether it is available
			// there should be a project file containing a persistent list of transformer models
			// that apply to the current raw data file (somDataObj knows about the raw data file)
			transformerModelLoaded = false;
			// transformerModelLoaded = somFluid.loadLastOfKnownTransformerModels( somDataObj ) ;  
			
			// ... do it, or enforce it if requested
			if (transformerModelLoaded==false){
				somTransformer.initializeTransformationModel();
			}
			
			subsets.prepare(n) ;

			// in principle we could send these tasks to different threads or machines
			for (int i=0;i<n;i++){
				process = new OptimizerProcess( this, subsets, i);
				processes.add(process);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void perform() {
		int activatedPix;
		
		for (OptimizerProcess process: processes){
			 
			activatedPix = process.start() ;
		}
		 	
	}
	
	
	private int saveSingleSom( SimpleSingleModel singleSom ) {
		// 
		
		singleSom.save();
		
		return 0;
	}
	
	
	
	class OptimizerProcess implements Runnable{
		
		int index;
		ModelOptimizer moptiParent;
		VariableSubsets variableSubsets;
		
		SomSprite dependencyCheck;
		
		SomScreening somScreening  =null;
		
		Thread moptiThrd;
		ArrayList<Integer> variableSubsetIndexes ;
		private ArrayList<String> currentVariableSelection = new ArrayList<String>() ;
		
		
		// --------------------------------------------------------------------
		public OptimizerProcess( ModelOptimizer mopti, VariableSubsets subsets, int index){
			
			this.index = index;
			this.moptiParent = mopti;
			variableSubsets = subsets;
			
			variableSubsetIndexes = subsets.getSubset(index) ;
			moptiThrd = new Thread(this,"mOptiProcThrd-"+index) ;
		}
		// --------------------------------------------------------------------
		
		public int start(){
			moptiThrd.start();
			return index;
		}
		
		@SuppressWarnings("unchecked")
		public void optimizeOnVariableSubset(){
			
			// variableSubsetIndexes
			boolean done=false, candidatesOK;
			int loopcount=0, n, vn;
			double _mScore1 = 9999.09, _mScore2=9999.09;
			VirtualLattice somLattice ;
			ModelProperties _mozResults=null;
			EvoBasics _evoBasics =null ;
			EvoMetrices _evoMetrices = null;
			// ArrayList<CandidateTransformation> lastDependencyProposals;
			AnalyticFunctionTransformationsIntf lastDependencyProposals = null;
			
			PowerSetSpringSource pset;
			currentVariableSelection = modelingSettings.getInitialVariableSelection() ;

			performSingleRun(loopcount, true);
			somLattice = somProcess.getSomLattice();

			 
			
			// ................................................................
			
			int z=0;
			while ((done==false) && (somFluid.getUserbreak()==false)){
				
				
				// after any sprite+evo optimization, a further pair of sprite+evo opti may yield even better results 
				if (modelingSettings.getMaxL2LoopCount()>0){
					
											// out.print(2, "variables(a1) n = "+somDataObj.variables.size()	);
					if ((modelingSettings.getEvolutionaryAssignateSelection() ) && (somFluid.getUserbreak()==false)){
						
						if ((loopcount==0) && (resumeMode>=1)){

							// we load a simple som from archive and run it once
							SomScreening somscreener = new SomScreening( moptiParent );
							try {
								
								somscreener.establishFromStorage(); // TODO needs to be calculated and logged by evohistory 
								currentVariableSelection = new ArrayList<String> (somscreener.getCurrentVariableSelection()) ; 
								 
								
							} catch (Exception e) {
								e.printStackTrace();
								break;
							}
							resumeMode = 0;
							
						}
						//else
						{
						// size of var vector ?
							somScreening = new SomScreening( moptiParent );
							try {
								somScreening.setInitialVariableSelection( currentVariableSelection,true  ) ;
								
							} catch (Exception e) {
								e.printStackTrace();
								break;
							}
						}	
						// ? is not properly delivered, and init of powerset hangs 
						 
						// will be done inside there: somScreening.acquireMapTable( somProcess.getSomLattice().exportSomMapTable() );
						 
						somScreening.setModelResultSelection( new int[]{SomScreening._SEL_TOP, SomScreening._SEL_DIVERSE} ) ;
						somScreening.setModelResultSelectionSize(20) ;
						
						// provide the results of the model, will also calculate the quality score
						
						somScreening.setInitialModelQuality( somProcess );
											// out.print(2, "variables(a2) n = "+somDataObj.variables.size() );
						
						
						
							// passes blacklist, absolute accessibility & evoweights as criteria for 
							// initializing the basic itemset, from which then the powerset is constructed 
						somScreening.setInitialRelevanciesOfVariables( _evoBasics );

						if (loopcount>0){ //
							// will imply firstly a special check of new variables, which will be tested
							// together with top N (1..3)  metrices of previous loop step
							somScreening.setSpecialInterestVariables(freshlyAddedVariables);

							// TODO if (loopcount>=2)somScreening.setinitialWeights(); 
							// adapt the values from previous loop, but not the original ones... reduced contrasts,
	
						} // loopcount > 0 ?
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (a) : "+vn);						
						somScreening.startScreening(1,loopcount);
						
															
						// first getting it, we have to check whether the new results are better than the last one 
											out.print(2, "SomScreening has been finished, re-establishing the best of the evaluated models...");
											
						_mozResults = restoreModelFromHistory( somScreening,  -1 ) ; 
						 				
						
											out.print(2, "The best model has been re-established.");
											
						somScreening.getEvoBasics().getBestModelHistoryIndex();
						
											String str = ArrUtilities.arr2Text( somScreening.getEvoMetrices().getBestResult().getVarIndexes() ) ;
											out.print(2, "Indices of selected Variables : "+ str);
											
						// the starting metric for the next loop is just the best of the previous screening ...
						currentVariableSelection = somDataObj.variables.deriveVariableSelection( somScreening.getEvoMetrices().getBestResult().getVarIndexes(), 0) ;
						_mozResults.setVariableSelection(currentVariableSelection);
											
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (b) : "+vn);
											
						evoMetrices = somScreening.getEvoMetrices() ;	
						 
						// evoMetrices.sort( EvoMetrices._SORT_SCORE,-1 );
						
						_mScore1 = evoMetrices.getBestResult().getSqData().getScore();
						
						if ((_mScore1<_mScore2) || (mozResults==null)){
							mozResults = new ModelProperties(_mozResults);
							mozResults.sqData = new SomQualityData( evoMetrices.getBestResult().getSqData());
							_mScore2 = _mScore1 ;
						}
						
						// 
						 
						evoBasics = somScreening.getEvoBasics() ;
						_evoBasics = new EvoBasics(evoBasics);
						
						_evoMetrices = _evoBasics.integrateEvoMetricHistories( _evoMetrices, evoMetrices, loopcount) ;
						
						evoMetrices.sort( EvoMetrices._SORT_SCORE,-1 );
						
					} // checking variable metrices ? == modelingSettings.getEvolutionaryAssignateSelection() ?
					
					
					int s1,s2 ;
					boolean hb = ( (modelingSettings.getMaxL2LoopCount()> loopcount ) && (modelingSettings.getSpriteAssignateDerivation() )&& (somFluid.getUserbreak()==false));
					 
					if ((hb)  ){ // ...AND HERE && (_mozResults != null )){
						// not in the last one of the L2-loops, such we always have (n-1) sprites for (n) evo opti
						s1 = evoBasics.getEvolutionaryWeights().size() ;
											// vn = somDataObj.variableLabels.size() ; 
											// out.print(4, "somDataObj, size of variableLabels (c) : "+vn);
						
						dependencyCheck = new SomSprite( somDataObj, somTransformer, sfProperties);
						
						// 
						SomMapTable smt = somLattice.exportSomMapTable(11); // 1+10 = 11: sort mode 1,  
						
						dependencyCheck.acquireMapTable( smt ); 
						if (lastDependencyProposals!=null){
							dependencyCheck.addProposalsAsKnown( lastDependencyProposals.getItems()) ;
						}else{
							//  nothing, it is ???
						}
						
						// TODO check that... returns the same proposals even with a different sommap
						dependencyCheck.startSpriteProcess( modOpti,1 );
						
						candidatesOK = false;
						if ((dependencyCheck!=null) && (dependencyCheck.getCandidates()!=null) && (dependencyCheck.getCandidates().getItems().size()!=0)){
							candidatesOK = true;
						}else{
							// if no additional derived variables -> we may exit the L2-loop 
							break;
						}
						
						// send candidates into SomTransformer, they will be put just to a queue, 
						// but NOTHING will be changed regarding the transformations...  
						// implementation will be triggered by instances of SomHostIntf (such like ModelOptimizer)
						// perceiveCandidateTransformations(candidates) ;
						if (candidatesOK){
							lastDependencyProposals = dependencyCheck.getProposedCandidates();
							
							somTransformer.implementWaitingTransformations(1);

							
							if (somTransformer.getLastErrorMsg().length()==0){
								somTransformer.incDerivationLevel();
								somDataObj.ensureTransformationsPersistence(1);
								try {
									somDataObj.saveSomDataTables();
								} catch (Exception e) {
									 
									e.printStackTrace();
								}
							}
							
							// register in order to avoid repetitions AND avoid repetitions across loops !!
							freshlyAddedVariables = (ArrayList<Integer>) CollectionUtils.disjunction(  freshlyAddedVariables, somTransformer.getAddedVariablesByIndex()) ;
							allAddedVariables = somTransformer.getAddedVariablesByIndex() ;
							
							
							
							// are the vectors evo-weight, evo-count ok ? -> adjust their length
							s2 = evoBasics.getEvolutionaryWeights().size() ;
							int kd = s2-s1 ;
							if ((freshlyAddedVariables!=null) &&(kd<freshlyAddedVariables.size())){
								Collections.sort(freshlyAddedVariables) ;
								ArrayList<String> addedVariableLabels = (ArrayList<String>)somDataObj.variables.getLabelsForIndexList(freshlyAddedVariables) ; 
								evoBasics.adjustEvoVectorsForChanges(kd, addedVariableLabels) ;
							}
							
							// now we need to re-init the somlattice, as the structure has been changed !
							// for instance next "exportSomMapTable()" will likely fail, since it is still of different size
							/* does not get updated!
							   node.getIntensionality().getProfileVector().getVariablesStr() ;
							   node.getIntensionality().getProfileVector();
							 */
							somLattice.refreshDataSourceLink();
							somLattice.reInitNodeData(0);
							
							// n = somDataObj.getVariables().getUsageIndicationVector().size() ;
							// we need to initialize the som and its lattice before starting to screen
							performSingleRun(loopcount, false);
							
						}

					}else{
						// arrived at loopcount, or user-break 
						done = true;
						
						// save SOM, such that a re-calculation will not be necessary if we want to export
						
						//
						break;
						
					} // getSpriteAssignateDerivation() ?
					
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (f) : "+vn);

					
					_mozResults = null ; // -> checking whether evocounts are taken in/from previous run ?
					
					// 
					somScreening.close();
					somScreening = null;
					System.gc() ;
					
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (g) : "+vn);
				    
				} // getMaxL2LoopCount ?
				
				loopcount++;
			}// main loop -> done ?
			// ................................................................
			
			// TODO: SomModelDescription, dedicatedVariableCheck(): -> not al variables get checked !!!
			
			// TODO: creating results
			
			// TODO capture results into: spelaResults ....
			// on option: remove low-rated derived variables from tables and lists in SomDataObject
			evoMetrices.close() ;
			
			evoMetrices = _evoMetrices;
			evoMetrices.prepare();
											String str = arrutil.arr2text( evoMetrices.getBestResult().getVarIndexes());
											// translate into variable names
											ArrayList<String> mlabels= somDataObj.variables.getLabelsForIndexList(evoMetrices.getBestResult().getVarIndexes()) ;
											
											String vstr =  arrutil.arr2text(mlabels, ",");
											
											double _score = Math.round( 1000.0 * evoMetrices.getBestResult().getActualScore())/1000.0 ;
											out.printErr(2, "best model (score: "+_score+"): "+str );
											out.print(2, "variable labels : "+vstr +"\n ");
											                 
											
			somQuality = somScreening.getSomQuality();
			
			if (moptiParent.modelingSettings.isDetermineRobustModels()){ 
				// we run the som screening in a special mode, just to dedicatedly explore the less visited variables
				
				
			}
			
			// validation only if: moptiParent.modelingSettings.getValidationSettings().getActivation() ;
			
			// in the following post-processing, we should collect the metrices as well !!
			if (moptiParent.modelingSettings.isDetermineRobustModels()){
				
				robustModel = new RobustModelSelector( modOpti );  // check "evoMetrices" ...
				// includes  MultiCrossValidation & SomModelDescription !
				
				robustModel.setBaseVariableSelection( currentVariableSelection );
				
				robustModel.isSamplingIncluded( moptiParent.modelingSettings.isCheckingSamplingRobustness() );
				robustModel.setTopNSubsetSize(10) ;
				
				robustModel.check();  
				
				EvoMetrik em = robustModel.getBest() ;
				modelDescription = robustModel.modelDescription ;
				
			}
			
			// finally a lot of diagnostic stuff 
			if (moptiParent.modelingSettings.isExtendedDiagnosis()){
				
				
				if ((modelDescription==null) || (modelDescription.isCalculated()==false)){
					modelDescription = new SomModelDescription( moptiParent );
					modelDescription.setInitialVariableSelection( currentVariableSelection  ) ;
					modelDescription.calculate() ;
				}

				// metrics remains constant , max number of clusters changes 
				coarseness = new Coarseness( moptiParent );
				coarseness.setBaseVariableSelection( currentVariableSelection  ) ;
				coarseness.evaluate();
								
				populationExplorer = new ParetoPopulationExplorer( moptiParent );
				populationExplorer.explore();
				
				if ((robustModel==null) || (robustModel.getMultiCrossValidation()==null) || (robustModel.getMultiCrossValidation().isCalculated()==false)){
					// metrics remains constant, based on different samplings
					MultiCrossValidation validation = new MultiCrossValidation( moptiParent );
					validation.setBaseVariableSelection( currentVariableSelection  ) ;
					validation.perform();
				}
				
				
				MetricsStructure metricsStructure = new MetricsStructure( moptiParent, evoMetrices);
				
			} // ?
			
		
			
			// TODO make results persistent: save data, model and its properties into a dir structure, 
			//      which later can be unpacked, searching in archive and selective unpacking should be allowed
			//      saving is delegated, the "Persistencer" class just organizes it
			
			
			
			// TODO create a report as xml, which can be rendered into a result display elsewhere 
			//      creating the xml is delegated to the respective worker classes, it is just organized there
			//      == a summarizing call
			OutResults outresult= new OutResults( modOpti ,sfProperties);

			outresult.createModelOptimizerReport();  
			// according to OutputSettings, writes to files and creates a dir package
			
			
			// consoleDisplay(); // of profile values for nodes
			// release event message, better use an event listener instead of a direct callback
			// at least an Observer ??
			
			sfTask.setSomHost(modOpti) ;
			sfTask.setCompleted(true);
			somFluid.onTaskCompleted( sfTask );
		}
	  

		
		
		private void performSingleRun(int index, boolean collectresults){
		// the same "package" is running in SomScreening 
			SomFluidTask _task = new SomFluidTask(sfTask);
			_task.setNoHostInforming(true);
			
			SimpleSingleModel simo ;
			ModelProperties somResults;
			
			simo = new SimpleSingleModel(somFluid , _task, sfFactory );
			
			simo.setDataObject(somDataObj);

			simo.setInitialVariableSelection( currentVariableSelection  ) ;
			
			simo.perform();
			
			somProcess = simo.somProcess ;
			
			if (collectresults){
				somResults = simo.getSomResults();
				somResults.setIndex(index);
			}
			// simo.somProcess.getSomLattice();
		}
		
		@Override
		public void run() {
			 
			optimizeOnVariableSubset();
		}
		
		 
	} // ----------------------------------------------------------------------
	
	
	private void singleRun(int z){
		
			
			long serialID=0;
			serialID = SerialGuid.numericalValue();
			
			SomTargetedModeling targetedModeling;
			
			sfTask.setCallerStatus(0) ;
			
			targetedModeling = new SomTargetedModeling( modOpti, sfFactory, sfProperties, sfTask, serialID);
			
			targetedModeling.setSource(0);
			
			targetedModeling.prepare(usedVariables);
			
			String guid = targetedModeling.perform(0);
			
			out.print(2, "\nSom ("+z+") is running , identifier: "+guid) ; 

			while (targetedModeling.isCompleted()==false){
				out.delay(10);
			}
			targetedModeling.clear() ;
			targetedModeling = null;
	}

	

	private ModelProperties restoreModelFromHistory( SomScreening somScreening, int bestHistoryIndex) {
		
		SimpleSingleModel simo ;
		ModelProperties restoredResults;
		
		int _bestHistoryIndex, n ;
		String str;
		ArrayList<Integer> indexes ;
		ArrayList<Double> histUsagevector, uv ;
		ArrayList<String> varSelection ;
		
		EvoBasics evoBasics;
		EvoMetrices evoMetrices;
		
		SomFluidTask _task = new SomFluidTask(sfTask);
		_task.setNoHostInforming(true);
		
		_bestHistoryIndex = bestHistoryIndex;
		
		evoMetrices = somScreening.getEvoMetrices();
		evoBasics = somScreening.getEvoBasics() ;
		
		if (_bestHistoryIndex<0)_bestHistoryIndex= evoBasics.getBestModelHistoryIndex() ;
		
		histUsagevector = evoMetrices.getBestResult().getUsageVector() ; 
 		
		indexes = evoMetrices.determineActiveIndexes(histUsagevector);
										str = arrutil.arr2text(indexes) ;
										out.print(2,"restoring best model (history index :"+_bestHistoryIndex+"), variable indices : "+str);
		
		
		simo = new SimpleSingleModel( somFluid , _task, sfFactory );
		
		simo.setDataObject(somDataObj) ;
		 
		varSelection = somDataObj.getVariables().getVariableSelection( histUsagevector );
		 
		
		simo.setInitialVariableSelection( varSelection ) ;
		
		simo.perform();
		
		restoredResults = simo.getSomResults() ;
		
		restoredResults.setVariableSelection( varSelection ) ;
		
		// save temporarily
		
		saveSingleSom(simo) ;
		
		//
		return restoredResults;
	
	}

	



	class VariableSubsets{
		
		ArrayList<ArrayList<Integer>> subsets = new ArrayList<ArrayList<Integer>>();
		
		// ------------------------------------------------
		public VariableSubsets(){
			
		}
		// ------------------------------------------------
		
		
		public void prepare(int count){
			
			int nparts=1, nvtotal, nvwhite, nvblack,nvtv=1, nvix=1, availableVarsCount;
			Variables vars;
			String label;
			
			vars = somDataObj.getVariables();
			
			nvtotal = vars.size() ;
			nvwhite = vars.getWhitelistLabels().size();
			nvblack = vars.getBlackList().size() ;
			nvtv =   vars.getAllTargetedVariables().size() ;
			nvix =   vars.getAllIndexVariables().size() ;
			
			// subtract index var, target vars, whitelist, blacklist
			availableVarsCount = nvtotal - nvwhite - nvblack - nvtv - nvix ;
			
			
			
			if (count>1){
				nparts = (int) ((double)availableVarsCount/((double)count))*availableVarsCount;	
			}else{
				nparts =1;
			}
			
			ArrayList<Integer> subset ;
			subset = new ArrayList<Integer>();
			int sizeOfSubsets = (int)Math.round( (double)availableVarsCount/(double)nparts );
				
			int allocated=0;
			int setix=0;
			for (int i=0;i<vars.size();i++){
				
				double fract = (double)allocated/(double)sizeOfSubsets ;
				if((i>0) && (fract==0.0) && (setix<count)){
					
					subset = new ArrayList<Integer>();
				}
				
				
				Variable v = vars.getItem(i) ;
				label = v.getLabel() ;
				 
				
				if (variableIsAllocatable(vars,i)){
					subset.add(i) ;
				}
				
				
			} // i-> all parts
			
		}
		
		private boolean variableIsAllocatable( Variables variables, int index ){
			boolean rB=true;
			Variable variable;
			
			variable = variables.getItem(index) ;
			
			if (rB){
				rB = variable.isID()==false;
			}
			if (rB){
				rB = variable.isTV()==false;
			}
			if (rB){
				rB = variable.isTVcandidate()==false;
			}
			if (rB){
				rB = variables.getBlackList().contains(variable)==false;
			}
			
			return rB;
		}
		
		public ArrayList<Integer> getSubset(int index){
			ArrayList<Integer> subset = null;
			
			return subset;
		}
		
	} // inner class VariableSubsets

	
	

	@Override
	public void onTargetedModelingCompleted(ModelProperties results) {

		out.print(3, "results received by main instance for Som ("+results.dSomGuid+")");
		
		double tps = results.getTrainingSample().getTpSingularity();

	}


	// ========================================================================

	@Override
	public void processCompleted(Object processObj, Object msg) {
		
		out.printErr( 2, "processCompleted() for dependencycheck()");
	}



	public void test() {
		int z;
		// here we could run them in parallel if we would have several lattices
		for (int i = 0; i < 100; i++) {
			out.printErr( 2, "------------ memory status change (step " + i + ") : "
							+ Memory.observe() + "  still free : "
							+ Memory.currentFreeMemory(1));
			singleRun(i);
	if ((i > 20) || (i % 10 == 0)) {
		z = 0;
	}
		}
	}



	@Override
	public SomFluid getSomFluid() {
		return somFluid;
	}



	public int getResumeMode() {
		return resumeMode;
	}



	public void setResumeMode(int resumeMode) {
		this.resumeMode = resumeMode;
	}



	@Override
	public ModelProperties getSomResults() {

		// here we return the results of the single best model
		
		return null;
	}



	/**
	 * @return the sfTask
	 */
	public SomFluidTask getSfTask() {
		return sfTask;
	}



	/**
	 * @return the sfFactory
	 */
	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}



	/**
	 * @return the somDataObj
	 */
	public SomDataObject getSomDataObj() {
		 
		return somDataObj;
	}



	/**
	 * @return the sfProperties
	 */
	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}



	@Override
	public SomProcessIntf getSomProcess() {
		 
		return somProcess;
	}



	/**
	 * @return the evoBasics
	 */
	public EvoBasics getEvoBasics() {
		return evoBasics;
	}



	/**
	 * @return the evoMetrices
	 */
	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}



	/**
	 * @return the somQuality
	 */
	public SomQuality getSomQuality() {
		return somQuality;
	}



	/**
	 * @return the mozResults
	 */
	public ModelProperties getResults() {
		return mozResults;
	}



	/**
	 * @return the spelaResults
	 */
	public SpelaResults getSpelaResults() {
		return spelaResults;
	}

	
}

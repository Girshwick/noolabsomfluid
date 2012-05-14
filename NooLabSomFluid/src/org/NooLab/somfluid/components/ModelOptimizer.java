package org.NooLab.somfluid.components;

import java.util.ArrayList;
 
import java.util.Collections;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.*;

import org.NooLab.somfluid.*;
import org.NooLab.somfluid.properties.* ;

 
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.components.post.Coarseness;
import org.NooLab.somfluid.components.post.MultiCrossValidation;
import org.NooLab.somfluid.components.post.OutResults;
import org.NooLab.somfluid.components.post.ParetoPopulationExplorer;
import org.NooLab.somfluid.components.post.RobustModelSelector;
import org.NooLab.somfluid.components.post.SomOptimizerXmlReport;
import org.NooLab.somfluid.components.post.SpelaResults;
import org.NooLab.somfluid.core.SomProcessIntf;

import org.NooLab.somfluid.core.engines.det.*;
import org.NooLab.somfluid.core.engines.det.results.*;

import org.NooLab.somscreen.*;
import org.NooLab.somsprite.*;
 
import org.NooLab.somtransform.SomTransformer;
import org.apache.commons.collections.CollectionUtils;



// the core of the SOM is hosted by class "DSomDataPerception"

/**
 * 
 * behaves like SomFluid for its child "SomTargetedModeling{}"
 * 
 * note that the particle field remains the same, but the event sink is reestablished anew each time
 * when the child has been created 
 * 
 * 
 * 
 *       
 * TODO: - checking the top metrices against different samples (CV) and selecting the most robust one
 *         - create several samples (8) for each sample size
 *         - determine dependency of quality from sample size (various samples for each size)
 *         
 *       - allow for nesting if 
 *         - cases are very rare
 *         - if requested
 *         - if tp-singularity is near 0
 *         
 *       - checking a model where sprite variables are replaced by their raw parents
 *         - one by one
 *         - all at once
 *         -> this provides the argument that the sprite contains more information that the mere difference (opposing Minsky's difference engine)
 *       - persistence storage of explored metrices for resume...   
 *       
 *       - measure concordance between models
 *         := [0..1] portion of cases rated by both models equally
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
	OutResults outresult;
	
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
		
		if (sfProperties.getInitialNodeCount()>200){
			sfProperties.setMultiProcessingLevel(1) ;
		}
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
		 
		
		for (OptimizerProcess process: processes){
			// int activatedPix = 
			process.start() ;
		}
		 	
	}
	 
	
	private int saveSingleSom( SimpleSingleModel singleSom ) {
		// 
		
		singleSom.save();
		
		return 0;
	}
	
	
	/**
	 * performing a heuristic that comprises pairwise correlation and pairwise MWU
	 * 
	 */
	private void performInitialGuessOfWeights() {
		// 
		int tvindex=-1;
		Variables variables;
		String tvarLabel;
		double score1,score2 ;
		
		variables = somDataObj.variables ;
		tvindex = variables.getTvColumnIndex() ;
		tvarLabel = variables.getItem(tvindex).getLabel() ;
		ArrayList<Double> varColData = new ArrayList<Double>(); 
		
		
		if ((tvindex <0) || (tvarLabel.length()==0)){
			return;
		}
		
		//ArrayList<Double> tvColData = somDataObj.normalizedSomData.getColumn(tvindex).getCellValues() ;
		
		for (int i=0;i<variables.size();i++){
			varColData.clear();
			if (variables.openForInspection( variables.getItem(i))){
				varColData.addAll( somDataObj.normalizedSomData.getColumn(tvindex).getCellValues() ) ;
				
				// calculate abs(correlation) and its r^2 between those two columns
				// we calculate score1 = (1+ m) * (1+r^2) ;

				score1 = 1.0;
				
				// calculate significance of MWU between those two columns
				// we calculate score2 = 1 + MWUsignificance/1.7 ;
				
				score2 = 1.0;
				
				// mix these results into an initial weight, which always is >=0.5 !!
				// we calculate (score1 + score2)/2 or geometric mean ...
				
				score1 = (score1 + score2)/2.0;
				
				if (score1>1.4){
					// -> evoweight = 0.5
					
				}
			}
			
		} // i->
		
		
		
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
		private int lastCanonicalStepCount=0;
		
		
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
			int loopcount=0,  vn;
			double _mScore1 = 9999.09, _mScore2=9999.09;
			VirtualLattice somLattice ;
			SomScreening somscreener ;
			ModelProperties _mozResults=null;
			EvoBasics _evoBasics =null ;
			EvoMetrices _evoMetrices = null;
			// ArrayList<CandidateTransformation> lastDependencyProposals;
			AnalyticFunctionTransformationsIntf lastDependencyProposals = null;
			
			//PowerSetSpringSource pset;
			currentVariableSelection = modelingSettings.getInitialVariableSelection() ;
 
			// ................................................................
			
			
			currentVariableSelection = prepareInitialVariableGuess( ) ;
			
			performSingleRun(loopcount, true);
			somLattice = somProcess.getSomLattice();
			
			performInitialGuessOfWeights();
			
			 
			while ((done==false) && (somFluid.getUserbreak()==false)){
				
				
				// after any sprite+evo optimization, a further pair of sprite+evo opti may yield even better results 
				if (modelingSettings.getMaxL2LoopCount()>0){
					
											// out.print(2, "variables(a1) n = "+somDataObj.variables.size()	);
					if ((modelingSettings.getEvolutionaryAssignateSelection() ) && (somFluid.getUserbreak()==false)){
						
						if ((loopcount==0) && (resumeMode>=1)){

							// we load a simple som from archive and run it once
							somscreener = new SomScreening( moptiParent );
							try {
								
								somscreener.establishFromStorage(); 
								//  
								currentVariableSelection = new ArrayList<String> (somscreener.getCurrentVariableSelection()) ; 
								// we drop/overwrite this somscreener below   
								
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

				if (modelingSettings.getPerformCanonicalExploration()){
					// did we reach a minimum number of models so far?
					// we demand a distance of 50 models, or the complete change of the top 5 (10) models
					int evosteps = moptiParent.evoMetrices.size();
					
					boolean hb = checkForCanonicalsActivation(evosteps, lastCanonicalStepCount);
					
					if (hb){
						/*
						 *  essentially the same as SomScreening, but the elements used in top5 models are
						 *  successively set to blacklist, starting with the strongest variable;
						 *  after removal, x (around 20) models are calculated;
						 *  the global ranking list of evo-devo-history remains active !!  
						 */
						performCanonicalExploration();	
					}
					
					lastCanonicalStepCount = evosteps ; 
				} // PerformCanonicalExploration ?

				loopcount++;
			}// main loop -> done ?
			// ................................................................
			
			if (modelingSettings.getPerformCanonicalExploration()){
				int evosteps = moptiParent.evoMetrices.size();
				
				boolean hb = checkForCanonicalsActivation(evosteps, lastCanonicalStepCount);
				
				if (hb){
					performCanonicalExploration();	
				}
				
				if (moptiParent.modelingSettings.isSearchForLinearModels()){
					// we pre-calc the model description already here, searching for variables which are
					// contributing much, but of low contrast
					// we fix all other variables as white list, and search for a replacement of that variable
					// TODO
					modelDescription = new SomModelDescription( moptiParent );
					modelDescription.setInitialVariableSelection( currentVariableSelection  ) ;
					modelDescription.calculate() ;

					SynonymicalsIdentifier syd = new SynonymicalsIdentifier ( moptiParent, modelDescription ) ;
				}
				
			} // PerformCanonicalExploration ?
			
			
			// TODO: SomModelDescription, dedicatedVariableCheck(): -> not all variables get checked !!!
			
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

			CrossValidation cv = new CrossValidation( modOpti ); 
			
			
			// TODO make results persistent: save data, model and its properties into a dir structure, 
			//      which later can be unpacked, searching in archive and selective unpacking should be allowed
			//      saving is delegated, the "Persistencer" class just organizes it
			
			
			
			// TODO create a report as xml, which can be rendered into a result display elsewhere 
			//      creating the xml is delegated to the respective worker classes, it is just organized there
			//      == a summarizing call
			outresult= new OutResults( modOpti, sfProperties);
			
			outresult.createModelOptimizerReport(); 
			// the actual calls to create the results are performed in the respective worker classes that do exploration,
			// the access this OutResults object from ModelOptimizer!  

			
			/*
			 * before checking the best model, we should check the model description for all the top 10 models
			 * and select that model as best which contains least enigmas (like contribution+, contrast=0) 
			 * 
			 * But we should also report those enigmas if we suppress them 
			 * 
			 */
			
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
				modelDescription = robustModel.getModelDescription() ;
				
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
				
				
				// cartography of the Pareto-frontier
				populationExplorer = new ParetoPopulationExplorer( moptiParent );
				populationExplorer.explore();
				
				if ((robustModel==null) || (robustModel.getMultiCrossValidation()==null) || (robustModel.getMultiCrossValidation().isCalculated()==false)){
					// metrics remains constant, based on different samplings
					MultiCrossValidation validation = new MultiCrossValidation( moptiParent );
					validation.setBaseVariableSelection( currentVariableSelection  ) ;
					validation.perform();
					outresult.createDiagnosticsReport(validation);
				}
				
				/* 
				 * checking for synonyms, antagonists: 
				 *  - which variables occur never together in good models? -> check them! deterioration? -> candidates for bags
				 *  - which variables are bound together ? check pairwise contribution! deterioration pair>single? 
				 *        -> bind them together via cluster z-model
				 *        -> check particular reversal of correlations
				 */
				MetricsStructure metricsStructure = new MetricsStructure( moptiParent, evoMetrices);
				
			} // ?
			
			// test if the current model in somProcess is the best one, if not recalculate
			
			actualizeByBestModel();
			 
			
			// test, if the lattice contains the stats 
			this.moptiParent.somProcess.getSomLattice().establishProperNodeStatistics();
			 
			// according to OutputSettings, writes to files and creates a dir package
			// effective columns are defined via "EvoMetrices.setOutputColumns()"
			// OutResults.getStringTable() invokes default columns being set in "setOutputColumns()"
			// TODO: on option in OutputSettings, that activates, and reads from file, or settings
			//       defaults should be defined there
			
			out.print(2, "... results per metric \n"+ outresult.getHistoryTableAsString() ) ;
			System.out.println("\n");
			out.print(2, "... contributions of variables \n"+modelDescription.getVariableContributions().getResultStringTable() ) ;
			System.out.println("\n");
			out.print(2, "... contrast between target group and non-target group in variables \n"+modelDescription.getVariableContrasts().getResultStringTable() ) ;
			
			// consoleDisplay(); // of profile values for nodes
			// release event message, better use an event listener instead of a direct callback
			// at least an Observer ??
			
			sfTask.setSomHost(modOpti) ;
			sfTask.setCompleted(true);
			somFluid.onTaskCompleted( sfTask );
		}
	  
		
		@SuppressWarnings("unchecked")
		private SomProcessIntf actualizeByBestModel() {
			
			int tvix = -1,a=1; 
			
			EvoMetrik bestresult ,topmetric  ;
			ArrayList<Integer> varindexes, mdiff  ;
			
			
			evoMetrices.sort( EvoMetrices._SORT_SCORE, -1);
			
			bestresult = evoMetrices.getBestResult() ;
			
			// consistent ?
			
			
			//
			topmetric = evoMetrices.getItems().get(0) ;
			varindexes = somProcess.getUsedVariablesIndices();
			
			topmetric.setVarIndexes( (ArrayList<Integer>)somDataObj.getVariables().transcribeUseIndications( topmetric.getUsageVector() ) );
			
			tvix = somDataObj.variables.getTvColumnIndex() ;
			
			if ((tvix>=0) && (varindexes.indexOf(tvix)<0)){
				varindexes.add(tvix);
			}
			Collections.sort(varindexes) ;
			
			mdiff = (ArrayList<Integer>)CollectionUtils.disjunction( varindexes, topmetric.getVarIndexes());
			
			if ((mdiff.size()>0) || (a>0)){
											out.print(2, "establishing best model into SOM (variable indices: "+ArrUtilities.arr2Text(topmetric.getVarIndexes())+")...");
				// _mozResults = restoreModelFromHistory( somScreening, 0 ) ;
				somScreening.calculateSpecifiedModel( topmetric.getUsageVector(), true );
											out.print(2, "establishing best model has been completed.");
/*
VirtualLattice _somLattice = moptiParent.somProcess.getSomLattice() ;
out.printErr(2, "lattice 0 "+_somLattice.toString());
_somLattice = somScreening.getSomProcess().getSomLattice() ;
out.printErr(2, "lattice 8 "+_somLattice.toString());
*/
		        moptiParent.somProcess = somScreening.getSomProcess() ;
			}
			return somProcess;
		}
		
		
		private void performCanonicalExploration() {
			// 
			CanonicalExploration canonicalExploration ;
			
			canonicalExploration = new CanonicalExploration(moptiParent);
		}

		
		private boolean checkForCanonicalsActivation(int evosteps, int lastStepPoint) {
			boolean rB=false ;
			ArrayList<EvoMetrik> evoMetricesTopN ;
			
			if (lastStepPoint<=0){
				evoMetricesTopN = new ArrayList<EvoMetrik>();
				if (evoMetrices.size() > 10) {
					for (int i = 0; i < 5; i++) {
						evoMetricesTopN.add( evoMetrices.getEvmItems().get(i) );
					} // ->
				}
				return true;
			}
			
			if ( evosteps - lastStepPoint > 50){
				rB=true;
			}
			if ((rB==false) && (evoMetrices.size()>10)) {
				evoMetrices.sort(EvoMetrices._SORT_SCORE, -1);
				// new top 5
				
				
			}

			// new top 5 from current history
			evoMetricesTopN = new ArrayList<EvoMetrik>();
			for (int i = 0; i < 5; i++) {
				evoMetricesTopN.add( evoMetrices.getEvmItems().get(i) );
			} // ->

			return rB;
		}
		
		
		
		@SuppressWarnings("unchecked")
		private ArrayList<String> prepareInitialVariableGuess() {
			ArrayList<String> varSelection, previousVariableSelection;
			int preSelectMode, vix;
			
			IndexedDistances ixds ;
			Variables variables;
			Variable variable;
			VirtualLattice somLattice ;
			SomMapTable somMapTable;
			SomScreening _somscreener ;
			
			previousVariableSelection = new ArrayList<String>();  
			previousVariableSelection.addAll(currentVariableSelection) ;
			
			preSelectMode = modelingSettings.getInitialAutoVariableSelection() ;
			
			if (( preSelectMode>0) || (currentVariableSelection.size()<=1)){
				
				ixds = new IndexedDistances() ;
				variables = somDataObj.variables ;
				
				int recCount = somDataObj.normalizedSomData.getColumn(0).getSize() ;
				if (recCount<=3){
					return currentVariableSelection;
				}
				// we need to run the SOM once in order to get the MapTable...
				// we select all variables except black ones... 
				//       	alternatively we could perform the PCA on the raw data, then we 
				// 			would create a SomMapTable from the norm DataTable
				
				currentVariableSelection = variables.getLabelsForVariablesList(variables,true); 
											// true: keep only the applicable ones by means of "openForInspection()"
				
				int s = currentVariableSelection.size()-1 ; 
				while (s>=0){
					String varLabel = currentVariableSelection.get(s) ;
					vix = variables.getIndexByLabel(varLabel) ;
					if (vix>=0){
						variable = variables.getItem(vix) ;
						if (variables.openForInspection(variable)){
							// TODO: if mv count >0.3 drop it, but remember the drop by using IndexedDistances
							double mvr = (double)variable.getMvCount()/(double)recCount;
							if (mvr>0.3){
								IndexDistance ixd = new IndexDistance( vix,mvr,varLabel);
								ixds.add(ixd);
								currentVariableSelection.remove(s) ;
							}
							
						}
					}
					s--;
				} // ->
				if ((currentVariableSelection.size()<3) && (ixds.size()>0)){
					ixds.sort(1);
					for (int d=0;d<ixds.size();d++){
						currentVariableSelection.add( ixds.getItem(d).getGuidStr()) ;
						if (currentVariableSelection.size()>3){
							break;
						}
					}
				}
				
				//limit to 50 variables by random
				if (currentVariableSelection.size()>50){
					currentVariableSelection = ArrUtilities.pickRandomSelection( currentVariableSelection, 50);
				}
				performSingleRun( 0, false); // false: no collecting of result
				
				
				somLattice = somProcess.getSomLattice() ;
				somMapTable = somLattice.exportSomMapTable() ;
				_somscreener = new SomScreening( moptiParent );
				
				
				if (preSelectMode==1){
					
					varSelection = new ArrayList<String>();
					ArrayList<Integer> varindexes = _somscreener.principalComponents( somMapTable);
					
					for (int i=0;i<varindexes.size();i++){
						int smtix = varindexes.get(i) ;
						String str = somMapTable.variables[smtix] ;
						
						vix = variables.getIndexByLabel(str) ;
						if ((vix>=0) && (vix != variables.getTvColumnIndex() )){
							varSelection.add(str) ;
						}
					}
					// remove TV
					
					if (varSelection.size()>=3){
						previousVariableSelection.clear();
						previousVariableSelection.addAll( varSelection ) ;
					}else{
						for (int i=0;i<varSelection.size();i++){
							
							if (variables.getBlacklistLabels().indexOf( varSelection.get(i))<0){
								previousVariableSelection.add( varSelection.get(i) );
							}
						}
					}
					varSelection.clear() ;
					varindexes.clear();
					
					currentVariableSelection.clear() ;  
					currentVariableSelection.addAll(previousVariableSelection) ;
				}
				
				if (preSelectMode==2){
					
				}
				if (preSelectMode==3){
					
				}
			}
			
			previousVariableSelection.clear();
			
			return currentVariableSelection;
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
if (bestHistoryIndex==0){
	n=0;
}
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
			out.printErr(2, "------------ memory status change (step " + i + ") : " + 
					        Memory.observe()+ "  still free : " + 
					        Memory.currentFreeMemory(1));
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



	public OutResults getOutresult() {
		return outresult;
	}



	public void setOutresult(OutResults outresult) {
		this.outresult = outresult;
	}



	public PrintLog getOut() {
		return out;
	}



	/**
	 * @return the mozResults
	 */
	public ModelProperties getResults() {
		return mozResults;
	}


	@Override
	public String getOutResultsAsXml(boolean asHtmlTable) {
		String xmlstr="";
		  
		xmlstr = this.outresult.createXmlResults(asHtmlTable);
		
		return xmlstr;
	}


	/**
	 * @return the spelaResults
	 */
	public SpelaResults getSpelaResults() {
		return spelaResults;
	}

	
	
}

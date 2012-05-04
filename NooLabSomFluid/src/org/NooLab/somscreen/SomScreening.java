package org.NooLab.somscreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;


import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;

import org.NooLab.somfluid.*;
import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.*;
import org.NooLab.somfluid.core.engines.det.* ;
import org.NooLab.somfluid.core.engines.det.results.*;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.properties.*;
import org.NooLab.somfluid.util.PowerSetSpringSource;

import org.NooLab.somscreen.linear.*;



/**
 * http://www.klaus-meffert.de/doc/monalisa.html
 * GP, JGAP
 * Note that genetic algorithms/GP is not applicable to our problem, since GA/GP need large populations.
 * This is affordable only if the fitness function is cheap, however, our fitness function is a full model,
 * As a consequence, even to start a GA population, we would have to build [n=population.size] models, typically
 * 500+ or so.
 * 
 * Hence we do it more abstract:
 * we need 
 * - 2+ levels of memory, 
 * - a cost function
 * 
 * 
 * This class determines the relevance weights of variables by means of
 * an evolutionary screening;
 * The weight of a variable represents the probability that this variable
 * improves the quality of a model. 
 * 
 * The most important property is that the resulting feature selection is NOT 
 * assuming anything about linearity of the model 
 * 
 * the search for an appropriate model create many models that are almost of identical power,
 * but of different structure.
 * As a consequence, a meta-reasoning about variables becomes possible, describing their
 * structural contribution to the model;
 * 
 * - contribution to discrimination of a variable within a model
 * - degree of non-linearity
 * - degree of saliency: milieu variable, or active variable
 * - detection of pseudo targets
 * 
 * the search may be accelerated by partitioning the data (limiting the size of the bags), both, per
 * variable and per record
 *
 * after the pre-last com phase, i.e. in the creative phase before mere clustering, we could apply a 
 * PCA / eigenvalue decomposition across the profiles (with or without referring to the usevector),
 * in order to infer the main components 
 * 
 * TODO: - periodically (every n*larger period) , we test the top-3 models completely, 
 *       
 *       - confounder identification : large evo-weight , large contribution, small model
 *        
 *       - evolution on the symbolic level: after each screening raid, metrices from 10 (N, an apriori set constant (at least 3+ == default)    
 *        
 */
public class SomScreening {

	public static final int _SEL_TOP = 1;
	public static final int _SEL_DIVERSE = 2;

	SomFluid somFluid;
	SomHostIntf somHost ; 

	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
 
	Variables variables ;
	
	// DSom dSom;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	SomProcessIntf somProcess;
	
	SomDataObject somData;
	
	SomMapTable somMapTable;
	
	SomQuality somQuality;
	
	ArrayList<Variable>	originalBlacklist;
	
	ArrayList<String> basicPowsetItems = new ArrayList<String>();;
	
	EvoBasics previousEvoBasics;
	
	int totalSelectionSize;
	int[] selectionMode;
	
	// .... data ................................
	
	private ArrayList<String> currentVariableSelection = new ArrayList<String>() ;
	private ArrayList<String> specialInterestVariables = new ArrayList<String>();
	
	EvoMetrices evoMetrices;
	EvoBasics  evoBasics;
	int currentBestHistoryIndex = 0;
	PowerSetSpringSource powerset; // resides in EvoMetrices
	
	// .... control .............................
	private boolean screeningIsRunning = false;
	private boolean stoppingCriteriaSatisfied = false;
	int dedicatedChecks=0;
	
	int largePeriod = 4;
	double closeInspectionMultiple = 3.0;
	
	ArrUtilities arrutil = new ArrUtilities ();
	PrintLog out = new PrintLog(2,true);
	private Random random;
		
	// ========================================================================
	public SomScreening( SomHostIntf somhost ) {
	
		somHost = somhost;
		somFluid = somHost.getSomFluid() ;
		sfProperties = somFluid.getSfProperties() ;
		
		sfTask = somHost.getSfTask() ;
		sfFactory = somHost.getSfFactory() ;
		somData = somHost.getSomDataObj() ;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		random = modelingSettings.getRandom();
		//somTargetedModeling = dSom.getEmbeddingInstance();
		 
		variables = somData.getVariables() ;
		originalBlacklist = new ArrayList<Variable>( somData.getVariables().getBlacklist() ); 

		
		evoMetrices = new EvoMetrices( somHost,largePeriod ) ;
		evoBasics = evoMetrices.getEvoBasics() ;
		
		powerset = evoMetrices.getPowerset() ;
		
		evoBasics.setKnownVariables( somData.getVariablesLabels() );
		
		
		
		out.setPrefix("[SomFluid-screen]") ;
	}
	// ========================================================================
	
	public void close() {
		
		evoMetrices.close();
		evoMetrices=null;
	}

	public void acquireMapTable( SomMapTable smt) {
			somMapTable = new SomMapTable(smt) ;
			 
	}


	public void setInitialModelQuality( SomProcessIntf somprocess ) {
		ArrayList<Double> uv ;
		int tvIndex = -1 ;
		VirtualLattice somLattice;
		 
		somProcess = somprocess;
		somLattice = somProcess.getSomLattice() ;
		
		uv = somLattice.getNode(0).getSimilarity().getUsageIndicationVector() ;
		
		ModelProperties modelProperties = somLattice.getModelProperties() ;
		tvIndex = modelProperties.getTargetVariableIndex() ;
		evoMetrices.setTvIndex(tvIndex) ;
		evoMetrices.registerResults( 0, modelProperties,  uv, 1) ; // TODO still empty
	}


	public void setInitialVariableSelection( ArrayList<String> vs) {
	
		currentVariableSelection = new ArrayList<String>();
		currentVariableSelection.addAll(vs) ;
		
	}

	public void setInitialRelevanciesOfVariables(EvoBasics evoBasics) {
		// will be passed through to generation of powerset in "startScreening" 
		int i;
		double ew;
		IndexDistance ixd;
		IndexedDistances ixds = new IndexedDistances();
		String varlabel ;
		Variables variables;
		ArrayList<String> blackliststr;
		
		basicPowsetItems.clear();
		basicPowsetItems.addAll( somData.getNormalizedDataTable().getColumnHeaders() );
		
		variables = somData.getVariables() ;
		blackliststr = variables.getBlacklistLabels() ;
		
		if (blackliststr.size()>0){

			i=basicPowsetItems.size()-1;
			
			while (i>=0){
				varlabel = basicPowsetItems.get(i) ;
			
				if (blackliststr.indexOf(varlabel)>=0){
					basicPowsetItems.remove(i);
				}
				i--;
			}
		} // any blacklisted variables ?

		try{
			

			// now resorting, using IndexedDistances
			if (evoBasics!=null){
				i=0;
				// first we transfer it ixds
				for (int k=0;k<variables.size();k++){
					
					varlabel = variables.getItem(k).getLabel();
					if (basicPowsetItems.indexOf(varlabel)>=0){
						// new variables will be added at the end
						if (k<evoBasics.evolutionaryWeights.size()){
							ew = evoBasics.evolutionaryWeights.get(k) ;
							if (ew<=0)ew=0.72 ;
							ixd = new IndexDistance(k,ew , varlabel );
							ixds.add(ixd) ;
						}else{
							ixd = new IndexDistance(k, 0.9 + (random.nextDouble()/20.0 - random.nextDouble()/20.0) , varlabel );
							ixds.add(ixd) ;
						}
					}
				} // k-> all positions in evoweight
				ixds.sort(-1) ;
				
				// now reestablishing the sorted item set
				basicPowsetItems.clear() ;
				for (int k=0;k<ixds.size();k++){
					basicPowsetItems.add( ixds.getItem(k).getGuidStr()) ;
				}
				//this.evoBasics.evolutionaryWeights = new ArrayList<Double>();
				
				previousEvoBasics = new EvoBasics(evoBasics) ;
				evoMetrices.setEvoBasics( previousEvoBasics ) ;
			} // evoweights available ?
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		i=0;
	}
	
	
	public void setSpecialInterestVariables( ArrayList<Integer> variableIxes ) {
		ArrayList<String> varSelection;
		
		specialInterestVariables.clear() ;
		
		if ((variableIxes==null) || (variableIxes.size()==0)){
			return ;
		}
		
		varSelection = somData.getVariables().deriveVariableSelection( variableIxes, 0) ;
		
		if ((varSelection!=null) && (varSelection.size()>0)){
			specialInterestVariables.addAll(varSelection) ;
		}
		
	}
	
	
	

	public void startScreening( int wait, int ithScreen) {
		
		
		EvolutionarySearch evoSearch ;
		
		// save the original metric for rollback
		
		// save the modelproperties (training, validation) for subsequent comparisons 
		
		// get a first guess, from SVD, PCA, covar / corr  matrix
		
		screeningIsRunning=false;
		
		// define the set sampling mechanism
		
		// we sort them according to some rule, such that the relevant items are taken first
		// we also remove those which are defined as black apriori, or absolutely not accessible
		
		powerset.setScramblingActive( true) ;
		powerset.setAbsoluteSizeLimit(15000) ;	

		powerset.setItems( basicPowsetItems, ithScreen ); // ithScreen is measuring the evo-loopcount, and can be used for inducing further scrambling of items
					 
		
		
		powerset.getConstraints().addExcludingItems( variables.getBlacklistLabels() );
		powerset.getConstraints().addExcludingItems( variables.getLabelsForVariablesList(variables.getAllIndexVariables()) );
		powerset.getConstraints().addExcludingItems( variables.getLabelsForVariablesList(variables.getAllTargetedVariables(0)) ); 
		
		powerset.getConstraints().addMandatoryItem( variables.getActiveTargetVariableLabel() );
		powerset.getConstraints().addMandatoryItems( variables.getWhitelistLabels() ); 
		
		int nvar  = 10 ;
		nvar = variables.size() - powerset.getConstraints().getExcludingItems().size();
		if (nvar > 5 )nvar =nvar -1;
		if (nvar > 50){nvar= 50;}
		powerset.getConstraints().setMaximumLength( nvar);
		powerset.getConstraints().setMinimumLength(3) ;
		
		powerset.activateConstraints(1);
		
		// start the process... 
		evoSearch  = new EvolutionarySearch( ithScreen );
		evoSearch.start() ;
		
		while (screeningIsRunning==false){
			out.delay(100);
		}
		
		while (screeningIsRunning){ 
			out.delay(100) ;
		}
		
		out.print(2,"leaving SomScreening...");
		System.out.println() ;
	}

	/* 
	 * 
	 * here we register the latest results and compare it to some "fixed points", that is...
	 * - the best model
	 * - the first model
	 * - the best models within a certain %-based deviation in quality 
	 * - the best 3 models
	 *  
	 * @param targetMod
	 */
	
	
	// --------------------------------------------------------------------		
	
	
	// ========================================================================
	
	private ModelProperties performSingleRun(int index){
		SomProcessIntf somprocess ;
		SimpleSingleModel simo ;
		ModelProperties somResults;
		SomFluidTask _task = new SomFluidTask(sfTask);
		_task.setNoHostInforming(true);
		
		if (somProcess!=null){
			// clear the extensionality of the lattics
		}
		
		
		
		simo = new SimpleSingleModel(somFluid , _task, sfFactory );
		 
		simo.setDataObject(somData);
	
		simo.setInitialVariableSelection( currentVariableSelection  ) ;
		 
		simo.perform();
		
		while (_task.isCompleted()==false){
			out.delay(10);
		}
		somProcess = simo.getSomProcess();
		somResults = simo.getSomResults();
		
		somResults.setIndex(index);
		
		somProcess.clear();
		
		return somResults;
	}
		 
	
	private SomTargetedModeling _singleRun(int z, ArrayList<Double> impUsagevector){
		
		SomTargetedModeling targetedModeling;
		Variables selectedVariables = null;
 
		

		if ((impUsagevector == null) || (impUsagevector.size() <= 1)) {
			return null;
		}

		long serialID = 0;
		serialID = SerialGuid.numericalValue();
		
		
		sfTask.setCallerStatus(0) ;
		 
		
		targetedModeling = new SomTargetedModeling( somHost, sfFactory, sfProperties, sfTask, serialID);
		
		targetedModeling.setSource(0);
		
		// targetedModeling.prepare(usedVariables);
		
		selectedVariables =  targetedModeling.setUsedVariablesIndicatorVector(impUsagevector) ;
		
		targetedModeling.init( selectedVariables );
		
		// targetedModeling.prepare(usedVariables);
		
		
		String guid = targetedModeling.perform(0);
		 
		somProcess = targetedModeling ;
		 
		 
		out.print(2, "\nSom ("+z+") is running , identifier: "+guid) ; 

		while (targetedModeling.isCompleted()==false){
			out.delay(10);
		}
		
		return targetedModeling;
	}
	/**
	 * in the basic variant "everything" remains the same, except the use vector
	 * we also take a copy of the dsom, and of the initialized lattice
	 * 
	 */
	public SomTargetedModeling calculateSpecifiedModel( ArrayList<Double> impUsagevector ){
		
		Variables selectedVariables = null;
		
		SomTargetedModeling targetMod = null;
		Variables vars ;
		ArrayList<String> blacklistLabels = new ArrayList<String>(); 
		ArrayList<String> initialUsageVector = new ArrayList<String>(); 
		
		
		if ((impUsagevector==null) || (impUsagevector.size()<=1)){
			return null;
		}
		
		try{
			
			// using singlesimplemodel ...
			
			targetMod = new SomTargetedModeling( null , false, 1); 
												 // true: create a new lattice
			somProcess = targetMod;
			
			  
			// create a blacklist from the difference  and provide it to the Variables-object, respect index and TV's
			// in DSomCore then this will be used to create the actual usageVector
			
			vars = somData.getActiveVariables() ; // all vars
			
			// we have to refer to the original blacklist, which we probably extend
			// is of type ArrayList<Variable> originalBlacklist
			
			// we create a deep copy, leaving the source unchanged
			selectedVariables = new Variables( vars );
			
			for (int i=0;i<impUsagevector.size();i++){
				if (impUsagevector.get(i)>0){
					// selectedVariables.additem( vars.getItem(i) );
					initialUsageVector.add( vars.getItem(i).getLabel() ) ;
				}else{
					blacklistLabels.add( vars.getItem(i).getLabel() ) ;
				}
			}
			/*
			
							  String  str = somData.getVariables().getActiveTargetVariableLabel() ;
							  Variable tvVar = somData.getVariables().getActiveTargetVariable() ;
			selectedVariables.setTargetVariable( tvVar );
							  int tvix = somData.getVariables().getTvColumnIndex() ;
							  int tvIndex = dSom.getTargetVariableColumn() ;
			selectedVariables.setTvColumnIndex(tvIndex) ;
			
			selectedVariables.setIdLabel( somData.getVariables().getIdLabel() ) ;
			selectedVariables.setIdVariables( somData.getVariables().getIdVariables() ) ;
			*/			  
			selectedVariables.setBlacklistLabels(blacklistLabels) ;
			
			selectedVariables.setInitialUsageVector( initialUsageVector ) ;
			
			//  
			somData.getVariables().setInitialUsageVector(initialUsageVector) ;
			targetMod.init( selectedVariables );
			
			// dsom does not exist here!
			//targetMod.getdSom().getSomLattice().getSimilarityConcepts().setUsageIndicationVector(impUsagevector) ;
			
			// SomTargetedModeling
			// 1 == prevent nested optimization ... 
			targetMod.perform(1);
			
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		if (selectedVariables!=null)selectedVariables.clear(0);
		selectedVariables=null;
		return targetMod; 
	}

	protected ArrayList<Integer> checkProposedSelectionForBlockedVariables( ArrayList<Integer> selection ){
		int ix , bix;
		String vlabel;
		ArrayList<String> blacks = variables.getBlacklistLabels();
		
		int i=selection.size()-1;
		while (i>=0){
			
			ix = selection.get(i) ;
			vlabel = variables.getItem(ix).getLabel() ;
			
			
			
			bix = blacks.indexOf(vlabel);
			if (bix>=0){
				selection.remove(i);
			}
			
			i--;
		}
			
			
		return selection;
	}
	
	public void setModelResultSelection(int[] selectionmode) {
		// TODO Auto-generated method stub
		selectionMode = selectionmode ;
	}


	public void setModelResultSelectionSize(int selsize) {
		
		totalSelectionSize = selsize ;
	}


	
	
	/**
	 * 
	 * this class performs the following steps
	 * 
	 * 1. checking the quality of the initial model, e.g. refering to target variable and validation
	 * 2. creating a few variations
	 * 3. running modeling on it, by "cloning" somTargetedModeling
	 *    yet, we need a new DSom instance !! -> just transferring settings
	 * 
	 * 
	 * 
	 * Any variable has a "screener's score" based on a list of tasks to be fullfiled (->EvoTaskOfVariable);
	 * 
	 * The trick is to ask the variable which task it would like to accomplish, i.e. there is sth like a 
	 * simple "inner state" 
	 * This is conditional to the evolutionary weight / count, which is the primary factor for selection
	 * 
	 * tasks are: 
	 * - variable is the only one changed (add/removed)
	 * - variable is part of large set of changes
	 * - variable is changed together with another good one
	 * - variable is changed together with a bad one
	 * 
	 * effect on evolutionary weights are different for each of the types of involvements
	 * on small changes
	 * 
	 * like there are tasks for each variable to be fulfilled, there are also tasks for
	 * the overall screening process
	 * The release of working on any of the possible tasks (for variables, or for global tasks) is 
	 * based on external "pressures" and "readiness" 
	 * 
	 * global tasks are
	 * - large changes 
	 *   > minimum distance [3..6] steps, dependent on size of history , # of variables 
	 *   > maximum distance [10..20(50)] steps (history, # variables))
	 *   length of period is dependent on history of changes, yet, with increasing duration of no-change happening,
	 *   the pressure increases to release one 
	 *  
	 * - small changes
	 * 
	 * - measuring contribution of variables
	 *   there may be a setting, that restricts the number of "background variables"
	 * 
	 * - 
	 *  
	 *  
	 * 
	 */
	class EvolutionarySearch implements Runnable{
		
		SomQuality somquality;
		 
		
		ArrayList<Integer> linearIndexSelection = new ArrayList<Integer>(); 
		ArrayList<Double> previousUseVector = new ArrayList<Double>();
		
		PCA pca;
		Decomposition decomposing ;
		
		Thread evosearchThrd;
		boolean userBreak=false;


		private long startTime;
		private long timeSinceStart;
		double hoursFraction ;
		int loopCount;
		
		// --------------------------------------------------------------------
		public EvolutionarySearch(int loopcount){
			
			evosearchThrd = new Thread(this, "evosearchThrd") ; 
			loopCount = loopcount;
		}
		// --------------------------------------------------------------------		
		
		
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			int z=1, r=0;
			 
			startTime = System.currentTimeMillis();
			out.delay(120);
			screeningIsRunning = true;
			
			try{
				// somHost is e.g. ModelOptimizer
				ArrayList<Double> uv;
				// ArrayList<Integer> usixes = somHost.getSomProcess().getUsedVariablesIndices() ;
				// ArrayList<Double> uv = (ArrayList<Double>)variables.transcribeUseIndications( usixes ) ;
				uv = somHost.getSomProcess().getUsageIndicationVector(false) ;
				     /* somHost    = org.NooLab.somfluid.components.ModelOptimizer
				      * SomProcess = org.NooLab.somfluid.core.engines.det.SomTargetedModeling
				      * 			 UsageIndicationVector
				      */
				previousUseVector = new ArrayList<Double>(uv);
				evoMetrices.registerMetricAsExplored(uv);
				
				// 
				z = dedicatedVariableCheck( specialInterestVariables ,z) ;
				dedicatedChecks = z ;
				 
if ((specialInterestVariables!=null) && 
	(specialInterestVariables.size()>0)){
	z=1;
	// LogControl.Level = 3 ; 
}
				while ((stoppingCriteriaSatisfied == false) && (userBreak==false)){
					z++;
					
					r = performEvoSearch(z);
				
						if (r<0){ // in case of critical errors
							stoppingCriteriaSatisfied = true;
						}
						
					timeSinceStart =  (System.currentTimeMillis() - startTime);
					hoursFraction = (double)timeSinceStart/((double)(1000 *60 * 60) );
					
					if (hoursFraction>sfProperties.getModelingSettings().getOptimizerSettings().getDurationHours()){
						stoppingCriteriaSatisfied = true;
					}
					// TODO for time based maximum exploration
					out.delay(2) ;
				} // ->

											String str = evoMetrices.toString();
											out.print(2, "explored metrices : \n"+str+"\n") ;

											out.printErr(2,	"Looping for evolutionary search has been stopped "+
													    	"(reason: "+r+
													    	", stopping criteria: "+stoppingCriteriaSatisfied+
													    	", user break: "+userBreak+").");
											
				// establish last known model
				// evoMetrices ...
				
				if (currentBestHistoryIndex>0){
					evoBasics.bestModelHistoryIndex = currentBestHistoryIndex-1;
					
				}
				
				
			}catch(Exception e){
				e.printStackTrace() ;
			}
			
			screeningIsRunning = false;
		}
		
		
		/**
		 * from SomScreen 2 on, we may meet added variables that have been derived by the sprite process;</br>
		 * these added variables will now be tested dedicatedly, since with a larger number of variables the
		 * effect of the introduced synthetic variables could remain unknown for a long time.</br></br>
		 * 
		 * The test consists simply by taking the best known metric, and adding the new variables one by one;
		 * if an improvement is found, pairs of new variables will also be tested.</br></br> 
		 * 
		 * @param specialInterestVariables
		 */
		@SuppressWarnings("unchecked")
		private int dedicatedVariableCheck( ArrayList<String> specialVariables, int z) {
			 
			int result = -1;
			String selectedVariable,str;
			boolean isNewBest, improvement=false;
			int  smode=1;
			
			ArrayList<String> baseMetric = new ArrayList<String>(), bestMetric = new ArrayList<String>();
			ArrayList<Integer> proposedSelection = null, baseMetricIndexes, av=null, rv=null ;
			ArrayList<Double> uv ;
			
			ModelProperties results;
			
			
			
			if ((specialVariables==null) || (specialVariables.size()==0)){
				return 0;
			}
			
			if ((currentVariableSelection==null) || (currentVariableSelection.size()<=1)){
				return 0;
			}
			
			
			try{
				// adjusting the length of evo vector
				int en, vn;
				en = evoMetrices.evoBasics.evolutionaryCounts.size() ;
				vn = variables.size() ;
				
				if (vn>en){
					for (int c=0;c<vn-en;c++){
						evoMetrices.evoBasics.evolutionaryCounts.add(0) ;
						evoMetrices.evoBasics.evolutionaryWeights.add(0.5) ;
					}
				}
				
				// looping
				improvement= true;
				
				while (improvement){
					improvement = false;
					
					baseMetricIndexes = variables.getIndexesForLabelsList(currentVariableSelection) ; 
					baseMetric = new ArrayList<String>(currentVariableSelection);
						
					previousUseVector = variables.getUseIndicationForLabelsList( baseMetric ) ;
					evoMetrices.currentBaseMetrik = evoMetrices.bestResult;

					
					int i=0;
					while (i<specialVariables.size()){
						z++;
						
						selectedVariable = specialVariables.get(i) ;

						if (baseMetric.indexOf(selectedVariable)>=0){
							i++;
							continue;
						}
						currentVariableSelection.clear();
						currentVariableSelection.addAll( baseMetric );
						currentVariableSelection.add(selectedVariable);
						
						uv = variables.getUseIndicationForLabelsList(currentVariableSelection) ;
						proposedSelection = (ArrayList<Integer>) variables.transcribeUseIndications( currentVariableSelection ) ;
	 
						av = variables.determineAddedVariables( previousUseVector, uv, false);
						rv = variables.determineRemovedVariables( previousUseVector, uv,false);
						
						results = performSingleRun(z);  //  
											
						// ..................................................
													//out.print(2, "lattice address : "+targetMod.getdSom().getSomLattice().toString());
						// calculates "SomQuality2 and stores it in "evoResultItem" 
						if (results.getTrainingSample().getObservationCount()<8 ){
							return 5;
						}
												
												str = arrutil.arr2text(proposedSelection);
												out.printErr(1, "proposed Selection (a): "+str+"\n") ;	
						// results = ModelProperties as retrieved from somHost.getSomResults()
						isNewBest = evoMetrices.registerResults( z, results , uv, smode) ; 
						
						// adapting the evoweights
						// calls "evoBasics.getEvoTasks().updateEvoTaskItem", 
						// for the respective variable index positions, then renormalizeParameters();
						evoMetrices.registerMetricChangeEffects(av, rv, isNewBest);
													if (sfProperties.getShowSomProgressMode() == SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS ){
														String ewstr = ArrUtilities.arr2Text( evoMetrices.evoBasics.evolutionaryWeights, 2) ;
														String ecstr = ArrUtilities.arr2Text( evoMetrices.evoBasics.evolutionaryCounts);
														out.print(2,"\nevolutionary weights :  "+ewstr);
														out.print(2,"evolutionary counts  :  "+ecstr+"\n");
													}
						evoMetrices.registerMetricAsExplored(uv);
						
						
						if ((isNewBest) || (z<=1)){
							// update "somMapTable" because this table is used as input for PCA etc...
							somMapTable = somProcess.getSomLattice().exportSomMapTable() ;
							
							bestMetric = variables.getLabelsForUseIndicationVector( somData.getVariables(), evoMetrices.bestResult.usageVector) ;
							// baseMetricIndexes = variables.getIndexesForLabelsList( baseMetric ) ; 
							// we do not change the basis before having checked all the variables, 
							// instead we keep track and buffer the best one, THEN restart subsequently!
							
							currentBestHistoryIndex = z;
							// evoMetrices.evmItems.size();
							improvement = true;
						}
						
						i++;
					} // i-> all items from provided list
					
					
					if ((improvement) && (bestMetric.size()>0)){
						currentVariableSelection.clear();
						currentVariableSelection.addAll( bestMetric ) ;
						
					}
					
				} // -> improvement ?
				
				
				if (baseMetric.size()>0){
					currentVariableSelection.clear();
					currentVariableSelection.addAll( baseMetric ) ;
				}
				
				result = 0;
			}catch(Exception e){
				e.printStackTrace() ;
			}
			return z;
		}
		
		
		
		private int performEvoSearch(int z){
			
			int result = -1,smode=0;
			String str, modestr="";
			boolean isNewBest ;
			DSom _dSom;
			SomTargetedModeling targetMod;
			ArrayList<Double> uv ;
			ArrayList<Integer> proposedSelection = null, av=null, rv=null ;
			
			isNewBest = false ;
			 								if ((evoMetrices!=null) && (evoMetrices.bestResult!=null) && (evoMetrices.bestResult.sqData!=null)){
			 									double v = evoMetrices.bestResult.sqData.score ;
			 									str = String.format("%.2f", v) ;
			 									out.print(2,"performEvoSearch(), step: "+z+" (best metric from step "+(currentBestHistoryIndex)+" of score = "+str+")   ");
			 								}
			try{

				isNewBest = false ;
				
				// create new SomMapTable from last map ;
				if (somProcess!=null){
					// we have to export the whole table for the purpose of PCA, i.e. ALL fields
					// TODO: alternatively, we use the top-60% quantil of all variables by evo weight
					//       combined with all variables for which evoweight > 0.45;
					somMapTable = somProcess.getSomLattice().exportSomMapTable();
					// if we do not calculate all variables, i.e. also the non-used, the sommaptable will 
					// have much less columns than the original table
					// applying PCA to a table with only very few columns is not reasonable 
				}
				
				// these functions also apply linear methods like PCA to the somMapTable (NOT to the raw data!!!) 
				if ((z% largePeriod == 0) || (z<=1)){
					// ensure that changes are indeed large !!
					proposedSelection = specifyLargeChange(z); modestr="large"; smode=1;
					z++;
				}else{
					
					proposedSelection = specifySmallChanges(z); modestr="small"; smode=2;
				}
				
				proposedSelection = checkProposedSelectionForBlockedVariables( proposedSelection );
				
				if (proposedSelection.size()<=1){ // it includes the tv
					proposedSelection = evoBasics.getQuantilByWeight(0.2, 0.45, 0.8 , true) ; // there are different versions of it 
				} // whatever is met first: fraction of all, lo, hi value for weight
				if (modelingSettings.getTargetedModeling()){
					
					str = variables.getActiveTargetVariableLabel(); 
					int tvix = somData.getNormalizedDataTable().getColumnHeaders().indexOf(str) ;
					
					if ((tvix>=0) && (proposedSelection.indexOf(tvix)<0)){
						proposedSelection.add(tvix);
					}
				}
				Collections.sort(proposedSelection) ;
				
											str = arrutil.arr2text(proposedSelection);
											out.printErr(1, "proposed Selection (b): "+str+"\n") ;											
				if (proposedSelection.size()==0){
					return -3;
				}
				uv = variables.deriveUsageVector( somMapTable.variables, proposedSelection ,0 ) ;
				
				// --------------------------------------------------
				if ((previousUseVector!=null) && (previousUseVector.size()>0)){
					av = variables.determineAddedVariables( previousUseVector, uv, false);
					rv = variables.determineRemovedVariables( previousUseVector, uv,false);

					if ((av.size()==0) && (rv.size()==0)){
						if (z>=1)z--;
					}

				}else{
					av = new ArrayList<Integer>();
					rv = new ArrayList<Integer>();
				}
				previousUseVector = new ArrayList<Double>(uv);
 															
				// the nodes do not know about tvindex in their similarity ....
				// ..................................................
				// targetMod = calculateSpecifiedModel( uv );
				// targetMod = singleRun(z, uv);
													
				currentVariableSelection = variables.deriveVariableSelection( proposedSelection ,0 ) ; 
				
				// TODO this should use a copy of the original somlattice in order to make parallelization possible !!!
				ModelProperties results = performSingleRun(z);
				// ..................................................
											//out.print(2, "lattice address : "+targetMod.getdSom().getSomLattice().toString());
				// calculates "SomQuality2 and stores it in "evoResultItem" 
				if (results.getTrainingSample().getObservationCount()<8 ){
					return 5;
				}

if (results.getTrainingSample().getRoc().getAuC()<0.67){
	z=z+1-1 ;
	// 4,5,15: very bad AuC but good score ???
}
if (results.getTrainingSample().getRoc().getAuC()>0.86){
	z=z+1-1 ;
	// 
}
				
				// results = ModelProperties as retrieved from somHost.getSomResults()
				isNewBest = evoMetrices.registerResults( z, results , uv, smode) ; 
				
				// adapting the evoweights
				// calls "evoBasics.getEvoTasks().updateEvoTaskItem", 
				// for the respective variable index positions, then renormalizeParameters();
				evoMetrices.registerMetricChangeEffects(av, rv, isNewBest);
				
											if (sfProperties.getShowSomProgressMode() == SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS ){
												String ewstr = ArrUtilities.arr2Text( evoMetrices.evoBasics.evolutionaryWeights, 2) ;
												String ecstr = ArrUtilities.arr2Text( evoMetrices.evoBasics.evolutionaryCounts);
												out.print(2,"\nevolutionary weights :  "+ewstr);
												out.print(2,"evolutionary counts  :  "+ecstr+"\n");
											}
				evoMetrices.registerMetricAsExplored(uv);
				
				
				if ((isNewBest) || (z<=1)){
					// update "somMapTable" because this table is used as input for PCA etc...
					somMapTable = somProcess.getSomLattice().exportSomMapTable() ;
					
					currentBestHistoryIndex = z; // store best uv separately ???
				}
				checkStoppingCriteria( somProcess, z-dedicatedChecks , currentBestHistoryIndex);
				
				if ( z % ((int)(((double)largePeriod) * closeInspectionMultiple )) ==0){
					// perform a close inspection of the "top" models according to the spela scheme
					// the same work will be done in the end after modeling !
					
				}
			  
				System.gc();
				result=0;
				
			}catch(Exception e){
				String estr;
				screeningIsRunning=false;
				if (proposedSelection!=null){
					estr = ""+proposedSelection.size();
				}else{
					estr="null";
				}
				out.printErr(2, "Exception in performEvoSearch(change:"+modestr+"), length of selection: "+estr);
				result = -3;
				e.printStackTrace() ;
				
			}

			return result;
		}

		//  SomTargetedModeling targetMod
		private void checkStoppingCriteria( SomProcessIntf somprocess, int z, int bestItemStep) {
			boolean hb=false;
			double qvalue ;
			/*
			 *  1- either count of loops, or
			 *  2- count of loops without relevant change, which is
			 *    dependent on loop count value of last change
			 *  3- average evoCount for variables of weight larger than ew [0.3...0.45]   
			 *  4- semantic state: avg evoCount as in (3) AND information distribution of weights
			 *     where some are rules out, some are preferred and most are within [0.46 .. 0.65]  
			 */
			 
			if (z<1)z=1;
			
			int maxs = optimizerSettings.getMaxStepsAbsolute() ;
			
			if (loopCount==0){
				if ( optimizerSettings.isShortenedFirstCycleAllowed()){
					if ((optimizerSettings.getMaxStepsAbsolute()>47) || (optimizerSettings.getMaxStepsAbsolute()<0)){
						maxs = 23 ;
					}
				}
			}
			maxs = maxs + dedicatedChecks;
			
			if (evoMetrices.evmItems.size()>=maxs){
				out.print(2, ""+(evoMetrices.evmItems.size())+" steps have been performed by optimizer, now stopping because it reached the <MaxStepsAbsolute="+maxs+"> ");
				hb = true;
			}
			
			int maxv = optimizerSettings.getMaxAvgVariableVisits() ;
			if (hb==false){
				// TODO 
			}
			
			if (hb==false){
				int maxnis = optimizerSettings.getAtLeastWithoutChange() ;
				if (z-bestItemStep>maxnis){
					out.print(2, ""+(evoMetrices.evmItems.size())+" steps have been performed by optimizer, while last "+(z-bestItemStep)+" steps have been without improvement, "+
								 "now stopping because it eceeded the threshold <AtLeastWithoutChange="+maxnis+"> ");
					hb = true;
				}
			}
			if ((hb==false) && (evoMetrices.getItems().size()>5)){
				ArrayList<EvoMetrik> eml = evoMetrices.getItems();
				int cix = currentBestHistoryIndex-1;
				
				if ((eml!=null) && (eml.size()>0) ){
					
					if (currentBestHistoryIndex<=0){
						cix=0;
					}else{
						cix = cix-1;
					}
					if ((cix>=0)&&(cix < eml.size())){
						EvoMetrik  em = eml.get(cix);
						if (em!=null){
							qvalue = em.mainScore;
							double smq = optimizerSettings.getStopAtNormalizedQuality();
							if ((qvalue>=0.0001) &&(qvalue < smq)){
								out.print(2, ""+z+" steps have been performed by optimizer, "+
											 "now stopping because achieved model quality is better than request (t="+String.format("%.3f", smq)+") ");
								hb = true;
							} // em
						}// ?
					} // < eml.size() ?
				} // e?
				
				
			} // evoMetrices>5 ?
			
			stoppingCriteriaSatisfied = hb ;
		}

		private ArrayList<Integer> specifySmallChanges(int z) {
			
			ArrayList<Integer> selection = new ArrayList<Integer>();
			
			calculateVariableStatus();
			
			selection = evoMetrices.getNextVariableSelection(z, 0 );
			
			return selection;
		}
		
		
		private ArrayList<Integer> specifyLargeChange( int z) throws Exception {
			
			double[] influencevector = new double[0];
			ArrayList<Integer> selection = new ArrayList<Integer>();
			
if (z==24){
	z=24;
}
			// we have to use the sommap of the best model here, not the first one!
			calculateVariableStatus();
			// TODO should not access String variables (scaling=8+)
			// linearIndexSelection contains variable indices
			if (linearIndexSelection.size()>0){
				
				influencevector = evoMetrices.deriveSimpleInfluenceVector(influencevector, variables.deriveUsageVector( somMapTable.variables,linearIndexSelection,0) );

				if ((z <= 1)) { // || (emphasizeLinearSolutions ))
					// also initializes EvoBasics.evolutionaryWeights for all values of z>1, this is called through
					// registration of results
					evoMetrices.influenceEvolutionaryWeights(influencevector);
				}
				evoMetrices.addSuggestions(linearIndexSelection);

				// consider evoweights ...
			}
											out.print(2, "specifyLargeChange(), getNextVariableSelection calling ...");
			// care about the indices over there !!! 
			selection = evoMetrices.getNextVariableSelection(z, 1);
											out.print(2, "specifyLargeChange(), getNextVariableSelection done.");
if (selection.size()<=1){
	     // independent large change: evoMetrices + best + usageVector
	        selection = evoMetrices.getNextVariableSelection(z, 3);
}

 
			updateVariablesCoreWeight();

			return selection;
		}
		
	

		private void updateVariablesCoreWeight() {
			 
			Variable variable;
			double evoweight;
			int evocount ;
			
			for (int i=0;i<somData.getVariables().size();i++){
				
				variable = somData.getVariables().getItem(i) ;
				evoweight = evoBasics.getEvolutionaryWeights().get(i) ;
				evocount  = evoBasics.getEvolutionaryCounts().get(i) ;
				
				variable.setSelectionWeight(evoweight) ;
				variable.setSelectionCount(evocount) ;
			}
			
				
			
		}
		/**
		 * translating indexes from somMapTable to global variable list
		 * @param indexes as of "somMapTable"
		 * @return 
		 */
		private ArrayList<Integer> translateVariableIndexesToGlobal(ArrayList<Integer> indexes){
			int smtabix, varix;
			String varStr;
			int n = somMapTable.variables.length ;
			
			for (int i=0;i< indexes.size() ;i++){
				
				smtabix = indexes.get(i);  
				// somMapTable contains ONLY used variables form the last modeling perform()
				if ((smtabix >= 0) && (smtabix < somMapTable.variables.length)) {
					varStr = somMapTable.variables[smtabix];
					if ((varStr!=null) && (varStr.length() > 0)) {
						varix = somData.getVariables().getIndexByLabel(varStr);
						indexes.set(i, varix);
					}
				}
			}
			
			return indexes;
		}
		
		
		@SuppressWarnings("unchecked")
		private void calculateVariableStatus(){
			
			ArrayList<Integer> selection1, selection2 ;
			
			// use this one also in Sprite! 
			// variance per variable and variance of that variance across clusters
			// very small -> linear, mid -> likely to be influential , very large -> no structure
			
			
			
			// calculate statistical measures like EigenSom, SVD
			selection1 = decomposition();
			
			selection2 = principalComponents() ;
			
			// create a union, each element is contained only once
			
			// these are now the variables that are relevant from a "linear/ization" point-of-view !
			// e.g. suitable for a first selection
			
			linearIndexSelection = new ArrayList<Integer>(); 
			  
			linearIndexSelection = (ArrayList<Integer>) CollectionUtils.union(selection1, selection2) ;
			
			if (linearIndexSelection.size()>0){
				linearIndexSelection = translateVariableIndexesToGlobal( linearIndexSelection ) ;
			}
											out.print(2, "linear index selections completed.");
		}

		private ArrayList<Integer> decomposition(){

			ArrayList<Integer> selection = new ArrayList<Integer>();
			
			decomposing = new Decomposition( somMapTable );
			decomposing.calculate() ;
			selection = decomposing.getSelection() ;
			
			return selection;
		}	
		
			// ----------------------------------------------------
		private ArrayList<Integer> principalComponents(){
			int n;
			ArrayList<Integer> selection = new ArrayList<Integer>();
			
			double[] pcaVector = new double[0];
			double[][] pcaVectors = new double[0][0];

			IndexDistance ixd ;
			// testing the pca:  pca = new PCA();
											out.print(3,"evaluating principal components ...");
			try{
				if ((somMapTable==null) || (somMapTable.values.length<=5) || (somMapTable.variables.length<=3)){
					return selection;
				}

				pca = new PCA( somMapTable);
				pca.calculate();
				pca.prepareResults() ;
				
				ArrayList<int[]> vs = pca.getRelatedVariablesSet();
				selection = pca.getLargeUnifiedSet() ;
				
				n = vs.size(); 
				
				double info[] = pca.getVectorInformation();
				// based on the info vector, we retrieve the indexes of the vector that are "promising"
				int[] vix = pca.getFilteredVectorIndices(0.86,  somMapTable.tvIndex); 
				
				
				
				// only those with info value >0.9
				for (int i=0;i<vix.length;i++){
					pcaVector = pca.getVector(vix[i]);
					pcaVectors = pca.getVectors(vix);
					ixd = new IndexDistance(vix[i],info[i],"");
				}
				
				// only for debug,  to prevent erasure-by-compiler ...
				
				n = info.length;
				n = pcaVector.length ;
				n = pcaVectors.length ;
				
				// selection = arrutil.changeArrayStyle(vix) ;
				
				// cerating a list of IndexedDistances
												out.print(3,"evaluating principal components done.");
				if (selection.size()>0)selection.trimToSize() ;
				
			}catch(Exception e){
				out.printErr(2, "PCA has not been calculated due to the following problem:\n"+
							    ""+
							    e.getMessage()+"");
			}
			
			return selection;
		}
	
		/**
		 *  this integrates new variales as they have been constructed by SomSprite
		 */
		private void adoptNewVariables() {
			Variable variable;  
		
			
			for (int i=0;i<somData.getVariables().size();i++){
				
				variable = somData.getVariables().getItem(i) ;
				
				if ((variable.isDerived()) ){
					
				}
				
			} // i-> all variables
		}



		public void start(){
			evosearchThrd.start() ;
		}
		public void stop(){
			userBreak = true;
		}

		public ArrayList<Integer> getLinearIndexSelection() {
			return linearIndexSelection;
		}
		
	} // inner class EvolutionarySearch

	 
	
	
	public SomQuality getSomQuality() {
		return somQuality;
	}


	public void setSomQuality(SomQuality somQuality) {
		this.somQuality = somQuality;
	}


	public int getTotalSelectionSize() {
		return totalSelectionSize;
	}


	public void setTotalSelectionSize(int totalSelectionSize) {
		this.totalSelectionSize = totalSelectionSize;
	}


	/**
	 * @return the evoMetrices
	 */
	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}

	public EvoBasics getEvoBasics() {
		return evoBasics;
	}
	
	public EvoBasics getEvoBasicsEx() {
		
		EvoBasics eb = new EvoBasics( evoBasics );
		 
		return eb;
	}


	public void setEvoBasics(EvoBasics evoBasics) {
		this.evoBasics = evoBasics;
	}


	public int[] getSelectionMode() {
		return selectionMode;
	}


	public void setSelectionMode(int[] selectionMode) {
		this.selectionMode = selectionMode;
	}


	public boolean isScreeningIsRunning() {
		return screeningIsRunning;
	}


	public void setScreeningIsRunning(boolean screeningIsRunning) {
		this.screeningIsRunning = screeningIsRunning;
	}

	
}


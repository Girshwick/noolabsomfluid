package org.NooLab.somscreen;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;


import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
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
 * the search for an appropriate model ceate many models that are almost of identical power,
 * but of different structure.
 * As a consequence, a meta-reasoning about variables becomes possible, describing their
 * structural contribution to the model;
 * 
 * - contribution to discrimination of a variable within a model
 * - degree of non-linearity
 * - degee of saliency: milieu variable, or active variable
 * - detection of pseudo targets
 * 
 * the search may be accelerated by partitioning the data (limiting the size of the bags), both, per
 * variable and per record
 *
 * after the pre-last com phase, i.e. in the creative phase before mere clustering, we could apply a 
 * PCA / eigenvalue decompoision across the profiles (with or without refering to the usevector),
 * in order to infer the main components 
 * 
 * TODO: - periodically (every n*larger period) , we test the top-3 models completely, 
 *       
 *       - confounder identification
 *        
 *        
 *        
 */
public class SomScreening {

	public static final int _SEL_TOP = 1;
	public static final int _SEL_DIVERSE = 2;

	SomFluid somFluid;
	SomHostIntf somHost ; 

	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
 
	
	// DSom dSom;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	SomProcessIntf somProcess;
	
	SomDataObject somData;
	
	SomMapTable somMapTable;
	
	SomQuality somQuality;
	
	ArrayList<Variable>	originalBlacklist;
	
	int totalSelectionSize;
	int[] selectionMode;
	
	// .... data ................................
	
	private ArrayList<String> currentVariableSelection = new ArrayList<String>() ;
	
	EvoMetrices evoMetrices;
	EvoBasics  evoBasics;
	int currentBestHistoryIndex = 0;
	PowerSetSpringSource powerset; // resides in EvoMetrices
	
	// .... control .............................
	private boolean screeningIsRunning = false;
	private boolean stoppingCriteriaSatisfied = false;
	
	int largePeriod = 4;
	double closeInspectionMultiple = 3.0;
	
	ArrUtilities arrutil = new ArrUtilities ();
	PrintLog out;
	

	
	
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
		
		//somTargetedModeling = dSom.getEmbeddingInstance();
		 
		 
		originalBlacklist = new ArrayList<Variable>( somData.getVariables().getBlacklist() ); 

		evoMetrices = new EvoMetrices( somHost,largePeriod ) ;
		evoBasics = evoMetrices.getEvoBasics() ;
		
		powerset = evoMetrices.getPowerset() ;
		
		evoBasics.setKnownVariables( somData.getVariablesLabels() );
		
		
		out = sfFactory.getOut() ;
	}
	// ========================================================================
	
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


	public void startScreening( int wait) {
		
		EvolutionarySearch evoSearch ;
		
		// save the original metric for rollback
		
		// save the modelproperties (training, validation) for subsequent comparisons 
		
		// get a first guess, from SVD, PCA, covar / corr  matrix
		
		screeningIsRunning=false;
		
		// define the set sampling mechanism
		powerset.setItems( somData.getVariablesLabels() );
		
		
		Variables variables = somData.getVariables();
		
		
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
		evoSearch  = new EvolutionarySearch();
		evoSearch.start() ;
		
		while (screeningIsRunning==false){
			out.delay(100);
		}
		
		while (screeningIsRunning){
			out.delay(100) ;
		}
		
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
		
		
		if (somProcess!=null){
			// clear the extensionality of the lattics
		}
		
		
		
		simo = new SimpleSingleModel(somFluid , sfTask, sfFactory );
		 
		simo.setDataObject(somData);
	
		simo.setInitialVariableSelection( currentVariableSelection  ) ;
		 
		simo.perform();
		
		while (sfTask.isCompleted()==false){
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
		
		
		// --------------------------------------------------------------------
		public EvolutionarySearch(){
			
			evosearchThrd = new Thread(this, "evosearchThrd") ; 
			
		}
		// --------------------------------------------------------------------		
		
		
		
		@Override
		public void run() {
			int z=0, r=0;
			 
			out.delay(120);
			screeningIsRunning = true;
			
			try{
				
				
				while ((stoppingCriteriaSatisfied == false) && (userBreak==false)){
					z++;
					
					// this integrates new variales as they have been constructed by SomSprite
					// ??? would be only necesary, if sprite and screening would be mixed... 
					// adoptNewVariables() ;
					
					r = performEvoSearch(z);
						if (r<0){
							stoppingCriteriaSatisfied = true;
							 
						}
						
					out.delay(2) ;
				} // ->
				
											out.print(2,"Looping for evolutionary search has been stopped "+
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
			 									out.print(2,"\n---   ---   ---   ---   ---    "+
			 												"performEvoSearch(), step: "+z+", best metric from step "+(currentBestHistoryIndex-1)+" of score = "+str);
			 								}
			try{
				
				isNewBest = false ;
				
				// create new SomMapTable from last map ;
				if (somProcess!=null){
					// we have to export the whole table for the purpose of PCA, i.e. ALL fields
					somMapTable = somProcess.getSomLattice().exportSomMapTable(1);
				}
				
				// these functions also apply linear methods like PCA to the somMapTable (NOT to the raw data!!!) 
				if ((z% largePeriod == 0) || (z<=1)){
					proposedSelection = specifyLargeChange(z); modestr="small"; smode=2;
					z++;
				}else{
					
					proposedSelection = specifySmallChanges(z); modestr="large"; smode=1;
				}
				
				
											
											str = arrutil.arr2text(proposedSelection);
											out.print(2, "\nproposedSelection: "+str+"\n\n") ;

				 
				uv = deriveUsageVector( proposedSelection ,0 ) ;
				
				  
				// cuv = dSom.getSomLattice().getSimilarityConcepts().getUsageIndicationVector();
				
				// --------------------------------------------------
				if ((previousUseVector!=null) && (previousUseVector.size()>0)){
					av = determineAddedVariables( previousUseVector, uv);
					rv = determineRemovedVariables( previousUseVector, uv);
				}else{
					av = new ArrayList<Integer>();
					rv = new ArrayList<Integer>();
				}
				
				previousUseVector = new ArrayList<Double>(uv);
if (z>=32){
	z=z+1-1;
	// infinite time consuming for step 34
	/* weird results: --> set break point in SomTargetResults...

	 * wrong ??? : quality @ ecr, risk compliant nodes count n=1,  tp=57, fp=14,  tp singularity =0.538,  AuC 0.8983
	 * wrong ??? : quality @ ecr, risk compliant nodes count n=0,  tp=0,  fp=4,   tp singularity =0.318,  AuC 0.8983

	 * and then    model score: 22.876  
	 */
}
															
				// the nodes do not know about tvindex in their similarity ....
				// ..................................................
				// targetMod = calculateSpecifiedModel( uv );
				// targetMod = singleRun(z, uv);
													
				currentVariableSelection = deriveVariableSelection( proposedSelection ,0 ) ; 
				
				// TODO this should use a copy of the original somlattice !!!
				ModelProperties results = performSingleRun(z);
				// ..................................................
											//out.print(2, "lattice address : "+targetMod.getdSom().getSomLattice().toString());
				// calculates "SomQuality2 and stores it in "evoResultItem" 
				
				// putting the results to the list ?
			    
										         // somHost.getSomResults()
				isNewBest = evoMetrices.registerResults( z, results , uv, smode) ; 
												 // TODO still empty
				
				// adapting the evoweights
				// calls "evoBasics.getEvoTasks().updateEvoTaskItem", 
				// for the respective variable index positions, then renormalizeParameters();
				evoMetrices.registerMetricChangeEffects(av, rv, isNewBest);
				
				evoMetrices.registerMetricAsExplored(uv);
				
				
				if ((isNewBest) || (z<=1)){
					// update "somMapTable" because this table is used as input for PCA etc...
					somMapTable = somProcess.getSomLattice().exportSomMapTable() ;
					
					currentBestHistoryIndex = z; // store best uv separately ???
				}
				checkStoppingCriteria( somProcess, z , currentBestHistoryIndex);
				
				if ( z % ((int)(((double)largePeriod) * closeInspectionMultiple )) ==0){
					// perform a close inspection of the "top" models according to the spela scheme
					// the same work will be done in the end after modeling !
					
				}
				
				/* targetMod.clear(); we need to put the SOMScreening BEFORE dsom, and directly into SomFLuid as a task,
				 		keeping only the history, evo sampling stuff, and best lattice, also in a dedicated storage container
				 		then all the structures can be removed.
				 		-> also the dependency screener has to be rooted in SomFluid
				 		
				 		we bundle this into SomWave in order to keep the somfluid clean
				*/
				// targetMod.clear() ;
				// targetMod=null;
				
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

		/**
		 * @param refUseVector usage vector that serves as reference
		 * @param useVector usage vector that is examined for removed variables
		 * 
		 * @return list of index positions that have been removed from "refUseVector" to "useVector"
		*/
		private ArrayList<Integer> determineRemovedVariables( ArrayList<Double> refUseVector, ArrayList<Double> useVector) {
			ArrayList<Integer> varIndexes = new ArrayList<Integer>();
			
			for (int i=0;i<refUseVector.size();i++){
				
				if (i<useVector.size()){
					
					if ((refUseVector.get(i)>0) && (useVector.get(i)<=0)){
						varIndexes.add(i) ;
					}
				}
				
			} // i->
			
			return varIndexes;
		}


		/**
		 * 
		 * @param refUseVector usage vector that serves as reference
		 * @param useVector usage vector that is examined for added variables
		 * 
		 * @return list of index positions that have been added from "refUseVector" to "useVector"
		 */
		private ArrayList<Integer> determineAddedVariables(ArrayList<Double> refUseVector, ArrayList<Double> useVector) {
			ArrayList<Integer> varIndexes = new ArrayList<Integer>();
			
			for (int i=0;i<refUseVector.size();i++){
				
				if (i<useVector.size()){
					
					if ((refUseVector.get(i)<=0) && (useVector.get(i)>0)){
						varIndexes.add(i) ;
					}
				}
				
			} // i->

			return varIndexes;
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
		
		
		
		private ArrayList<String> deriveVariableSelection( ArrayList<Integer> proposedindexes, int mode ) {
			ArrayList<String> varSelection = new ArrayList<String>();
			
			for (int i=0;i<proposedindexes.size();i++){
				if (proposedindexes.get(i)>0){
					int ix = proposedindexes.get(i);
					String vstr = somData.getVariablesLabels().get(ix);
					varSelection.add(vstr) ;
				}
			}
			
			return varSelection;
		}
		
		/**
		 * 
		 * 
		 * @param indexes index values
		 * @param mode =0: refering to global list, =1: refering "somMapTable"
		 * @return
		 */
		private ArrayList<Double> deriveUsageVector( ArrayList<Integer> indexes, int mode ) throws Exception{ 
			
			int smtabix,varix ;
			String varStr;
			
			DataTable dtable;
			Variables variables;
			
			double[] usevector ;
			ArrayList<Double> usageVector = new ArrayList<Double>();
			
			if (indexes.size()<=1){
				throw(new Exception("selection is empty, usage vector could not be prepared.")) ;
			}
			
				
			try{
				

				// starting with somMapTable but finally referring to the global vector (inclusive the transform-New variables)
				dtable = somData.getDataTable();
				variables = somData.getVariables();
	 
				
				
				// usevector = dSom.getdSomCore().prepareUsageVector(dtable, variables) ;
				 
				usevector = new double[variables.size()] ;
				varix = variables.getTvColumnIndex() ;
				if (varix<0){
					//varix = dSom.getTargetVariableColumn() ;	
					variables.setTvColumnIndex(varix) ;
					int n=10/0;
				}
				if (varix<0){
					return usageVector ; // it is still empty -> nothing will happen
				}
				
				usevector [varix] = 1;
				// for (int i=0;i<somData.getVariables().size();i++ ){ }
				
				// int n = somMapTable.variables.length ;
				
				for (int i=0;i< indexes.size() ;i++){
					
					smtabix = indexes.get(i);  
					// somMapTable contains ONLY used variables form the last modeling perform()
					if (mode>0){
						varStr = somMapTable.variables[smtabix] ;
						varix = variables.getIndexByLabel(varStr) ;
					}else{
						varix = smtabix;
					}
					
					usevector[varix] = 1 ;
				}
				
				usageVector = ArrUtilities.changeArraystyle(usevector) ;
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return usageVector;
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
			 
			int maxs = optimizerSettings.getMaxStepsAbsolute() ;
			if (z>maxs){
				out.print(2, ""+z+" steps have been performed by optimizer, now stopping because it reached the <MaxStepsAbsolute="+maxs+"> ");
				hb = true;
			}
			if (hb==false){
				int maxnis = optimizerSettings.getAtLeastWithoutChange() ;
				if (z-bestItemStep>maxnis){
					out.print(2, ""+z+" steps have been performed by optimizer, while last "+(z-bestItemStep)+" steps have been without improvement, "+
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
			
if (z==10){
	z=10;
}
			// we have to use the sommap of the best model here, not the first one!
			calculateVariableStatus(); // TODO should not access String variables (scaling=8+)
			// linearIndexSelection contains variable indices
			if (linearIndexSelection.size()>0){
			influencevector = evoMetrices.deriveSimpleInfluenceVector(influencevector, deriveUsageVector(linearIndexSelection,0) );

				if ((z <= 1)) { // || (emphasizeLinearSolutions ))
					// also initializes EvoBasics.evolutionaryWeights for all values of z>1, this is called through
					// registration of results
					evoMetrices.influenceEvolutionaryWeights(influencevector);
				}
				evoMetrices.addSuggestions(linearIndexSelection);

				// consider evoweights ...
				selection = evoMetrices.getNextVariableSelection(z, 1);

				updateVariablesCoreWeight();
			}
			
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
											out.print(2,"evaluating principal components ...");
			try{
				

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
												out.print(2,"evaluating principal components done.");
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


	public EvoBasics getEvoBasics() {
		return evoBasics;
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


	/**
	 * @return the evoMetrices
	 */
	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}

	public void setInitialVariableSelection( ArrayList<String> vs) {

		currentVariableSelection = new ArrayList<String>();
		currentVariableSelection.addAll(vs) ;
		
	}
}


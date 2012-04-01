package org.NooLab.somfluid.components;

import java.util.ArrayList;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.*;

import org.NooLab.somfluid.*;
import org.NooLab.somfluid.properties.* ;
import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.*;
import org.NooLab.somfluid.core.engines.det.results.*;

import org.NooLab.somscreen.*;
import org.NooLab.somsprite.*;





/**
 * 
 * behaves like SomFluid for its child "SomTargetedModeling{}"
 * 
 * note that the particle field remains the same, but the event sink is reestablished anew each time
 * when the child has been created 
 * 
 * 
 */
public class ModelOptimizer implements SomHostIntf{

	SomFluid somFluid ;
	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
	SomDataObject somDataObj ;
	SomProcessIntf somProcess;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	int numberOfRuns = -1, dependenciesDepth=-1 ;
	
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();
	VariableSubsets subsets = new VariableSubsets();
	ArrayList<OptimizerProcess> processes ;
	
	ModelOptimizer modOpti;
	
	PrintLog out ; 
	ArrUtilities arrutil = new ArrUtilities();
	
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
		
		modOpti = this;
		prepare();
	}
	
	
	
	private void prepare() {
		// somDataObj = somFluid.getSomDataObject(index) ;
		try {
			
			somDataObj = somFluid.loadSource("");
			 
			
			processes = new ArrayList<OptimizerProcess>();
			OptimizerProcess process;
			int n = 1;

			
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
	
	
	
	class OptimizerProcess implements Runnable{
		
		int index;
		ModelOptimizer moptiParent;
		VariableSubsets variableSubsets;
		
		SomScreening somScreening;
		EvoBasics  evoBasics;
		
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
		
		public void optimizeOnVariableSubset(){
			// variableSubsetIndexes
			boolean done=false;
			int loopcount=0;
			
			
			
			currentVariableSelection = modelingSettings.getInitialVariableSelection() ;
			
			// ................................................................
			int z=0;
			while ((done==false) && (somFluid.getUserbreak()==false)){
				
				performSingleRun(loopcount);
				
				if (modelingSettings.getMaxL2LoopCount()>0){
					
					if ((modelingSettings.getSpriteAssignateDerivation() )&& (somFluid.getUserbreak()==false)){
						
						
					}
					if ((modelingSettings.getEvolutionaryAssignateSelection() ) && (somFluid.getUserbreak()==false)){
						
						somScreening = new SomScreening( moptiParent );
						somScreening.setInitialVariableSelection( currentVariableSelection  ) ;
						
						 
						// will be done inside there: somScreening.acquireMapTable( somProcess.getSomLattice().exportSomMapTable() );
						 
						somScreening.setModelResultSelection( new int[]{SomScreening._SEL_TOP, SomScreening._SEL_DIVERSE} ) ;
						somScreening.setModelResultSelectionSize(20) ;
						
						// provide the results of the model, will also calculate the quality score
						
						somScreening.setInitialModelQuality( somProcess );
						
						somScreening.startScreening(1);
						

											out.print(2, "SomScreening has been finished, re-establishing the best of the evaluated models...");
						restoreModelFromHistory( somScreening,  -1 ) ; 
											out.print(2, "best model has been restored.");
					}
					
				} // getMaxL2LoopCount ?
				
				loopcount++;
			}// main loop -> done ?
			// ................................................................
			
			// consoleDisplay(); // of profile values for nodes
			// TODO: release event message
		}
	  

		private void performSingleRun(int index){
		// the same "package" is running in SomScreening 
			
			SimpleSingleModel simo ;
			ModelProperties somResults;
			
			simo = new SimpleSingleModel(somFluid , sfTask, sfFactory );
			
			simo.setDataObject(somDataObj);

			simo.setInitialVariableSelection( currentVariableSelection  ) ;
			
			simo.perform();
			
			somProcess = simo.somProcess ;
			
			somResults = simo.getSomResults();
			somResults.setIndex(index);
		}
		
		@Override
		public void run() {
			 
			optimizeOnVariableSubset();
		}
		
		/*
		private void _obs_performDSom_long(){
			
			int loopcount=0;
			boolean done=false;
			
			SomVariableHandling variableHandling;
			
			SomTargetResults somResults;
			
			

			SomMapTable somMapTable ;

			SomSprite somSprite ;
			SomScreening somScreening;
			SomQuality sq ;
			
			EvoBasics  evoBasics;
			 
			variableHandling = new SomVariableHandling( somDataObj, modelingSettings );
			variableHandling.determineSampleSizes( sfProperties.getInitialNodeCount()) ;
			
			
			// before starting with our L2-process, we need the info about ClassificationSettings.getTargetGroupDefinition()
			// which we have to set empirically if we are in multi-mode
			int tm = modelingSettings.getClassifySettings().getTargetMode();
			
			if (tm == ClassificationSettings._TARGETMODE_MULTI){ // TODO: needed an option which blocks the recalc of TGs! 
				// check if targetgoupdefs are false, or auto = true, if not: do nothing here, even if we have to stop 
				
				
				variableHandling.getEmpiricTargetGroups(  true ); 
				// there are different flavors of that, actually, it also can perform "adaptive binning" into a number of groups,
				// perhaps based on mono-variate clustering (in turn based on the spatial distribution of distances)
				
				
				double[][] tGdefinition = variableHandling.getTargetGroups();
				modelingSettings.getClassifySettings().setTGdefinition(tGdefinition);
			}
			
			/*
			if (modset.getSomType() == SomFluidProperties._SOMTYPE_MONO ){
				// if we are "modeling" i.e. working guided by a target variable, we have to distribute the use vector
				 ArrayList<Double> usevector = null ;
				 MetaNode  node;
				 int  tix ;
				 
				 for (int i=0; i<dSom.getSomLattice().size();i++){
					 
					 node = dSom.getSomLattice().getNode(i);
					 usevector = new ArrayList<Double>(node.getSimilarity().getUsageIndicationVector()) ;
					 tix =  node.getSimilarity().getIndexTargetVariable() ;
					 // usevector.set(tix, 0.0) ;
					 
					 node.getIntensionality().setUsageIndicationVector(usevector) ;
					 node.getIntensionality().setTargetVariableIndex(tix) ;
					 // the list of used variables is an important aspect of an intension and should be made available there
					 // from here it is used by SomApplication (app, validation), 
					 // note that the usevector in intension excludes the TV !!! 
					 // it is also part of "node.similarity", which uses it for learning
				 }
				   
			}
			* /
			DSom dSom;
			while ((done==false)  ){
			
			 
				// ......................................
				
				if ((modelingSettings.getMaxL2LoopCount()>0) ){
					// the Level 2 loop                       will be 1+ for optimizer for which we need just plain results
					/*
					 * note that the SomFluid instance always contains the full spectrum of tools, yet,
					 * it behaves as such or such (Som, Sprite, Optimizer, transformer), according to the request.
					 * 
					 * L2 loops make sense only WITH sprite and screening...
					 * /
					
					if ((modelingSettings.getSpriteAssignateDerivation() ) ){
						/*
						// create instance
						dSom.getSomProcessParent().
						
						somSprite = new SomSprite(  , modelingSettings );
					 
						// export maptable, or data, dependent on record number, to the sprite
						somSprite.acquireMapTable( exportSomMapTable() );

						                            // 1 = will wait for completion, but may react to messages and requests
						somSprite.startSpriteProcess(1); // DEBUG ONLY switched off
						
						// now integrate new variables, introduce it both on raw level as well as on transformed lavel
						// try to reduce it to the raw level
						
						* /
					} // SpriteAssignateDerivation() ?
					
					if ((modelingSettings.getEvolutionaryAssignateSelection() )  ){
						/*
						 *  in case of evolutionary modeling we need to integrate
						 *  - validation, for calculating the cost function
						 *  - sub-sampling = using significantly decreased samples for evolutionary optimization
						 *                   we prepare a set of samples which we then assign to the modeling runs?
						 *               
						 *  basically, it is a meta process that performes executeSOM()
						 *  -> put this execsom into a class 
						 *  (new ExecuteSom( params )).go().prepareResults() ;
						* /
						
						// let SomTransformer implement the waiting candidate transformations
						
						   // TODO ....
						
						
						// 
						
						/* 
						somScreening = new SomScreening( moptiParent  );
						
						somScreening.acquireMapTable( exportSomMapTable() );
						
						somScreening.setModelResultSelection( new int[]{SomScreening._SEL_TOP, SomScreening._SEL_DIVERSE} ) ;
						somScreening.setModelResultSelectionSize(20) ;
						
						// provide the results of the model, will also calculate the quality score
						
						somScreening.setInitialModelQuality( dSom  );
						
						somScreening.startScreening(1);
						// somScreening will reference settings and results for the collection of models

						// will contain weights and counts (also avail through each variable, but this is more efficient)
						* /
						
												out.print(2, "SomScreening has been finished, re-establishing the best of the evaluated models...");
						// restoreModelFromHistory( somScreening,  -1 ) ; 
												out.print(2, "best model has been restored.");
					}	
					
					if (modelingSettings.getSomCrystalization() ){
						// create an idealization into a secondary instance of dSom
						
						
					}
				} // any L2 loop ?
				
				 
				loopcount++;
				
				if (loopcount> modelingSettings.getMaxL2LoopCount()){ // default = -1 == off;  (modset.getMaxL2LoopCount()>0) &&
					done = false;
				}
			  
				// TODO: if there was no effect of sprite and screen, then stop the L2 loop anyway,
				
				
			} // done ?
		}
		*/
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

	

	private void restoreModelFromHistory( SomScreening somScreening, int bestHistoryIndex) {
		
		SimpleSingleModel simo ;
		ModelProperties restoredResults;
		
		int _bestHistoryIndex, n ;
		String str;
		ArrayList<Integer> indexes ;
		ArrayList<Double> histUsagevector, uv ;
		ArrayList<String> varSelection ;
		
		EvoBasics evoBasics;
		EvoMetrices evoMetrices;
		
		
		_bestHistoryIndex = bestHistoryIndex;
		
		evoMetrices = somScreening.getEvoMetrices();
		evoBasics = somScreening.getEvoBasics() ;
		
		if (_bestHistoryIndex<0)_bestHistoryIndex= evoBasics.getBestModelHistoryIndex() ;
		
		histUsagevector = evoMetrices.getBestResult().getUsageVector() ; 
 		
		indexes = evoMetrices.determineActiveIndexes(histUsagevector);
										str = arrutil.arr2text(indexes) ;
										out.print(2,"restoring best model (history index :"+_bestHistoryIndex+"), variable indices : "+str);
		
		
		simo = new SimpleSingleModel( somFluid , sfTask, sfFactory );
		
		simo.setDataObject(somDataObj) ;
		 
		varSelection = somDataObj.getVariables().getVariableSelection( histUsagevector );
		 
		
		simo.setInitialVariableSelection( varSelection ) ;
		
		simo.perform();
		
		restoredResults = simo.getSomResults() ;
		/*
		uv = somProcess.getSomLattice().getSimilarityConcepts().getUsageIndicationVector();
		n = somProcess.getSomLattice().getSimilarityConcepts().getUsageIndicationVector().size();
		
		
		somProcess.getSomLattice().reInitNodeData() ;
		
		somProcess.getSomLattice().getSimilarityConcepts().setUsageIndicationVector(histUsagevector) ;
		
		executeSOM() ; // 
		
		somResults = new SomTargetResults( dSom, dataSampler, modelingSettings );
		somResults.prepare();
		*/
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
		
	}

	@Override
	public void onTargetedModelingCompleted(ModelProperties results) {

		out.print(2, "results received by main instance for Som ("+results.dSomGuid+")");
		
		double tps = results.getTrainingSample().getTpSingularity();

	}


 





	// ========================================================================

	public void test() {
		int z;
		// here we could run them in parallel if we would have several lattices
		for (int i = 0; i < 100; i++) {
			out.printErr(
					2,
					"------------ memory status change (step " + i + ") : "
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

}

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
	
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;

	
	PrintLog out ; 
	ArrUtilities arrutil = new ArrUtilities();
	public ModelProperties mozResults;
	
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
		
		SomScreening somScreening  =null;
		SomModelDescription modelDescription ;
		ParetoPopulationExplorer populationExplorer;
		
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
						
						// if no additional variables -> we may exit the loop 
						done = true;
						if (somScreening!=null){
							break;
						}
					}else{
						done = true;
						if (somScreening!=null){
							break;
						}
					} // getSpriteAssignateDerivation() ?
					
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
						mozResults = restoreModelFromHistory( somScreening,  -1 ) ; 
											out.print(2, "The best model has been re-established.");
											somScreening.getEvoBasics().getBestModelHistoryIndex();
											String str = ArrUtilities.arr2Text( somScreening.getEvoMetrices().getBestResult().getVarIndexes() ) ;
											out.print(2, "Indices of selected Variables : "+ str);
					}
					evoBasics = somScreening.getEvoBasics() ;
					evoMetrices = somScreening.getEvoMetrices() ;
					somQuality = somScreening.getSomQuality();

					evoMetrices.sort( EvoMetrices._SORT_SCORE );
					
					populationExplorer = new ParetoPopulationExplorer( moptiParent );
					populationExplorer.explore();
					
					modelDescription = new SomModelDescription( moptiParent );
					modelDescription.calculate() ;

					
				} // getMaxL2LoopCount ?
				
				loopcount++;
			}// main loop -> done ?
			// ................................................................
			
			// consoleDisplay(); // of profile values for nodes
			// release event message, better use an event listener instead of a direct callback
			// at least an Observer ??
			
			sfTask.setSomHost(modOpti) ;
			sfTask.setCompleted(true);
			somFluid.onTaskCompleted( sfTask );
		}
	  

		private void performSingleRun(int index){
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
			
			somResults = simo.getSomResults();
			somResults.setIndex(index);
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
		
	}

	@Override
	public void onTargetedModelingCompleted(ModelProperties results) {

		out.print(3, "results received by main instance for Som ("+results.dSomGuid+")");
		
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

	
}

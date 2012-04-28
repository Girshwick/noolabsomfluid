package org.NooLab.somfluid.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.somscreen.EvoBasics;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.EvoMetrik;
import org.NooLab.somscreen.SomQuality;
import org.NooLab.somscreen.SomQualityData;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * describes the model on the level of the variables, given a particular "best" model
 * 
 * - removing any of them singularly,  
 * - correlations in data vs across the map vs within TP(0,ECR) clusters
 * 
 * 
 * 
 */
public class SomModelDescription implements Serializable{

	private static final long serialVersionUID = 8888493931363341188L;

	ModelOptimizer moptiParent ;
	SomHostIntf somHost;
	SomFluid somFluid;
	
	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	SomProcessIntf somProcess;
	
	SomDataObject somData ; 
	
			
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;
	EvoMetrices evometrices ;
	
	ArrayList<String> currentVariableSelection;
		
	VariableContributions variableContributions; 
	
	private PrintLog out;
	ArrUtilities arrutil = new ArrUtilities ();

	private boolean completeCheck = false;

	
	
	// ========================================================================
	public SomModelDescription( ModelOptimizer mopti ) {

		moptiParent = mopti ;
		
		somHost = (SomHostIntf)moptiParent ;
		
		somFluid = somHost.getSomFluid() ;
		sfProperties = somFluid.getSfProperties() ;
		
		sfTask = somHost.getSfTask() ;
		sfFactory = somHost.getSfFactory() ;
		somData = somHost.getSomDataObj() ;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		evoBasics = mopti.evoBasics;
		evoMetrices = mopti.evoMetrices;
		somQuality = mopti.somQuality;
		
		evometrices = new EvoMetrices(moptiParent, 0);
		variableContributions = new VariableContributions( somHost, evometrices);
		
		
		
		out = moptiParent.out ;
	}
	// ========================================================================
	
	
	
	public void calculate() {
		// should start a thread and wait
		dedicatedVariableChecking() ;
		
		// now we can evaluate results as they are stored in "evometrices" (SMALL "m"!)
		// preparing and filling the results into VariableContributions 
		
	}

	private void dedicatedVariableChecking() {
		
		ContributionChecks cochk = new ContributionChecks();
		
		cochk.start() ;
		
		while (cochk.checkingIsRunning==false){
			out.delay(100);
		}
		
		
		
		classifyingContributions();
	}
	
	
	/**
	 * performs an investigation of the potential of individual variables given a base model, which usually
	 * will be the best available model;</br> 
	 * according to the mode-parameter, the metric of the base model is changed either by removing
	 * individual variables from the metric, or by adding single not-used variables one-by-one.
	 * 
	 * In this way, we can describe the variables by some parameters that allow for a classification;
	 * this classification could be accomplished by k-means into 5 clusters
	 * 
	 * </br></br>
	 * control parameter :
	 *  &lt;0 : check by removing single variables of the base metric;</br> 
	 *  =1 : check by adding non-blacklisted variables, from top 10 of metrices, which are not used in the best one (="baseMetric")
	 *  &gt;2 : check by adding non-blacklisted variables (could be expensive in case of large initial sets)
	 * 
	 * @param mode 
	 */
	@SuppressWarnings("unused")
	private void dedicatedVariableCheck( int mode ) {
		 
		String selectedVariable,str;
		boolean isNewBest ;
		int  smode=1,ix, currentBestHistoryIndex=-1;
		
		ArrayList<String> baseMetric = new ArrayList<String>(), bestMetric = new ArrayList<String>();
		ArrayList<Integer> proposedSelection = null, baseMetricIndexes, av=null, rv=null ;
		ArrayList<Double> uv ;
		ArrayList<Double> previousUseVector ;
		
		ModelProperties results;
		
		EvoMetrices _evometrices = variableContributions.evometrices ;
		
		Variables variables = somData.getVariables() ;
		System.gc();
		
		try{
			  								out.print(2, "...\nSomModelDescription, dedicatedVariableCheck(): a posteriori test for contribution per variable...\n ");
				
				baseMetricIndexes = variables.getIndexesForLabelsList(currentVariableSelection) ; 
				baseMetric = new ArrayList<String>(currentVariableSelection);
					
				previousUseVector = variables.getUseIndicationForLabelsList( baseMetric ) ;
				_evometrices.setCurrentBaseMetrik( evoMetrices.getBestResult() );
				variableContributions.baseScore = evoMetrices.getBestResult().getActualScore() ;
				
				int z=0;
				int i=0;
				while (i<currentVariableSelection.size()){
					z++;
					
					// this variable we remove, then we check the results, the difference provides the contribution
					// there are 2*3 types of contributions: affecting TP, FP or both, either positively or negatively
					// additionally we may distinguish between pure TP, pure FP efficacy
					selectedVariable = currentVariableSelection.get(i) ;
											out.print(2,"...checking contribution for variable: "+selectedVariable);
					currentVariableSelection.clear();
					currentVariableSelection.addAll( baseMetric );
					
					ix = currentVariableSelection.indexOf( selectedVariable );
					currentVariableSelection.remove( ix ) ;
					
					uv = variables.getUseIndicationForLabelsList(currentVariableSelection) ;
					proposedSelection = (ArrayList<Integer>) variables.transcribeUseIndications( currentVariableSelection ) ;
 
					av = variables.determineAddedVariables( previousUseVector, uv, false);
					rv = variables.determineRemovedVariables( previousUseVector, uv,false);
					
					
					          System.gc(); out.delay(100) ;  
					results = performSingleRun(z);  // TODO : object separation, takes incredible long !!!! 
					/*
					 * how is it done in SomScreener ?? check threads... 
					 * the difference is that in SomScreener it runs in a further encapsulating thread ("EvolutionarySearch")
					 */ 
					// ..................................................
					
					// sqdata = null -> how to fill it ... check SomScreener ...
					// evoMetrices.getBestResult().getSqData() ==  EvoMetrik.SomQualityData sqData
					
					
					if (results.getTrainingSample().getObservationCount()<8 ){
						continue;
					}

					isNewBest = _evometrices.registerResults( z, results , uv, smode) ; 
					
					// adapting the evoweights
					// calls "evoBasics.getEvoTasks().updateEvoTaskItem", 
					// for the respective variable index positions, then renormalizeParameters();
					_evometrices.registerMetricChangeEffects(av, rv, true);
					_evometrices.registerMetricAsExplored(uv);
					
											Collections.sort(proposedSelection);
											str = arrutil.arr2text(proposedSelection);
											out.printErr(1, "proposed Selection: "+str+"\n") ;	
				
					
					if ((isNewBest) || (z<=1)){
						
						bestMetric = variables.getLabelsForUseIndicationVector( somData.getVariables(), evometrices.getBestResult().getUsageVector()) ;
						currentBestHistoryIndex = z;
					}
					
					// logging for the contribution table
					Variable v = somData.getVariables().getItemByLabel( selectedVariable ) ; 
					VariableContribution vc = new VariableContribution( variableContributions, v );
					
					int resultsAvailable= 1;
					// one of them is null???
					if (variableContributions==null){
						str = "variableContributions = null";  out.printErr(2, str) ;
						resultsAvailable= 0;
					}
					if (resultsAvailable== 0){
						out.printErr(2, "proposed selection has been skipped...") ;
						continue;
					}
					
					
					EvoMetrik em = evoMetrices.getItems().get( evoMetrices.size()-1) ;
					double _cScore = em.getSqData().getScore() ;
					
					results.sqData = new SomQualityData( em.getSqData() );
					
					vc.scoreDelta = variableContributions.baseScore - results.sqData.getScore() ;
					 
					vc.sqData = new SomQualityData( results.sqData ) ; 
					
					variableContributions.getItems().add(vc);
					
					int zn = somProcess.getSomLattice().nodes.size();
					zn = somProcess.getSomLattice().nodes.size();
					
					// somProcess.getSomLattice().refreshDataSourceLink();
					// somProcess.getSomLattice().reInitNodeData(0);
					
					evoMetrices.close() ;
					System.gc();
					i++;
				} // i-> all items from provided list
				 
			
			if (baseMetric.size()>0){
				currentVariableSelection.clear();
				currentVariableSelection.addAll( baseMetric ) ;
			}
			
			
			 
			 
		}catch(Exception e){
			str="";
			out.printErr(2, str);
			
			e.printStackTrace() ;
		}
		
	}



	private void classifyingContributions() {
		 
		
	}



	public void setInitialVariableSelection( ArrayList<String> vs) {
		currentVariableSelection = new ArrayList<String>();
		currentVariableSelection.addAll(vs) ;
	}
	
	

	private ModelProperties performSingleRun(int index){
		
		SomProcessIntf somprocess ;
		SimpleSingleModel simo ;
		ModelProperties somResults;
		
		SomFluidTask _task = new SomFluidTask(sfTask);
		_task.setNoHostInforming(true);
		
		if (somProcess!=null){
			// clear the extensionality of the lattice
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
		simo = null; // ??
		System.gc(); out.delay(100) ;
		return somResults;
	}


	class ContributionChecks implements Runnable{
		
		SomQuality somquality;
		Thread contriChkThrd;
		boolean checkingIsRunning;
		
		public ContributionChecks(){
			contriChkThrd = new Thread(this, "contchkThrd") ;
		}
		
		public void start() {
		 
			contriChkThrd.start() ;
		}

	 
		@Override
		public void run() {
			 
			checkingIsRunning = true;
			
			dedicatedVariableCheck( -1 );
			
			if (completeCheck){
				dedicatedVariableCheck( 1 );
			}
			
			checkingIsRunning = false;
		}
	
		
	}
	/**
	 * @return the completeCheck
	 */
	public boolean isCompleteCheck() {
		return completeCheck;
	}



	/**
	 * @param completeCheck the completeCheck to set
	 */
	public void setCompleteCheck(boolean completeCheck) {
		this.completeCheck = completeCheck;
	}
		 
	
	
}

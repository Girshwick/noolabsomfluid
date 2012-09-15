package org.NooLab.somfluid.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.post.OutResults;
import org.NooLab.somfluid.components.post.VariableContrast;
import org.NooLab.somfluid.components.post.VariableContrasts;
import org.NooLab.somfluid.components.variables.VariableContribution;
import org.NooLab.somfluid.components.variables.VariableContributions;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.tasks.SomFluidTask;
import org.NooLab.somscreen.EvoBasics;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.EvoMetrik;
import org.NooLab.somscreen.SomQuality;
import org.NooLab.somscreen.SomQualityData;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.ArrUtilities;

/**
 * 
 * describes the model on the level of the variables, given a particular "best" model
 * 
 * - removing any of them singularly,  
 * - correlations in data vs. across the map vs. within TP(0,ECR) clusters
 *   detecting significant differences in the correlation matrices 
 * - linearity in terms of contrast between target group and non-target group 
 * 
 * A t-test for the difference between two non-independent Pearson correlations
 * williams_steiger_test.pdf
 */
public class SomModelDescription implements Serializable{

	private static final long serialVersionUID = 8888493931363341188L;

	transient ModelOptimizer moptiParent ;
	transient SomHostIntf somHost;
	transient SomFluid somFluid;
	
	SomFluidTask sfTask;
	transient SomFluidFactory sfFactory;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	transient SomProcessIntf somProcess;
	
	SomDataObject somData ; 
	
			
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;
	EvoMetrices evometrices ;
	
	ArrayList<String> baseVariableSelection, currentVariableSelection;
		
	VariableContributions variableContributions; 
	
	VariableContrasts variableContrasts ;
	
	OutResults outresult;
	
	boolean completeCheck = false;
	boolean isCalculated = false;
	
	transient private PrintLog out;
	transient ArrUtilities arrutil = new ArrUtilities ();

	ClassificationSettings classificationSettings;

	
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
		
		classificationSettings = sfProperties.getModelingSettings().getClassifySettings() ;
		
		evoBasics = mopti.evoBasics;
		evoMetrices = mopti.evoMetrices;
		somQuality = mopti.somQuality;
		
		// evoMetrices = modelOptimizer.evoMetrices ;
		evometrices = new EvoMetrices(moptiParent.evoMetrices, false); // moptiParent, 0);
		evometrices.reset();
		
		// is this copying correct ??? 
		variableContributions = new VariableContributions( somHost, evometrices);
		variableContrasts = new VariableContrasts(somHost);
		
		outresult = moptiParent.outresult ;
		
		
		out = moptiParent.out ;
	}
	// ========================================================================
	
	
	public void clear() {
		variableContributions.getItems().clear();
		variableContrasts.getItems().clear();
		evometrices.reset();
	}


	public void calculate(int flavor) {
		
		if (flavor<=1){
			contributionsChecking() ;
		}
		if (flavor==2){
			linearityDescriptors() ;
		}
		if (flavor>2){
			calculate()  ;
		}
		
	}
	public void calculate() {
		// should start a thread and wait
		contributionsChecking() ;
		
		// now we can evaluate results as they are stored in "evometrices" (SMALL "m"!)
		// preparing and filling the results into VariableContributions 
		
		linearityDescriptors();
		//linearity in terms of contrast between target group and non-target group
		
		
		outresult.createDiagnosticsReport(this);
	}

	public void setInitialVariableSelection( ArrayList<String> vs) {
		
		baseVariableSelection = new ArrayList<String>();
		currentVariableSelection = new ArrayList<String>();
		
		currentVariableSelection.addAll(vs) ;
		baseVariableSelection.addAll(vs) ; 
	}
	
	
	public ArrayList<String> getNegativeContributionVar() {
		
		ArrayList<String> problematicVarLabels = new ArrayList<String>(); 
		VariableContribution vc;
		
		
		for (int i=0;i<variableContributions.size();i++){
		
			vc = variableContributions.getItems().get(i) ;
			if (vc.getScoreDelta()>0){
				problematicVarLabels.add( vc.getVariableLabel() ) ;
			}
		}
		
		 
		return problematicVarLabels;
	}


	private void linearityDescriptors() {
		
	
		LinearityChecks linchk = new LinearityChecks();
		linchk.start() ;
		
		while (linchk.checkingIsRunning==false){
			out.delay(10);
		}
		while (linchk.checkingIsRunning){
			out.delay(100);
		}
	}
	
	private void contributionsChecking() {
		
		ContributionChecks cochk = new ContributionChecks();
		
		cochk.start() ;
		
		while (cochk.checkingIsRunning==false){
			out.delay(10);
		}
		while (cochk.checkingIsRunning){
			out.delay(100);
		}
		
		classifyingContributions();
	}
	
	

	
	
	
	/**
	 * characterizing the contributions: there are 4 different classes, regarding TP and FP; </br>
	 * this method just provides a collection of it
	 * 
	 */
	private void classifyingContributions() {
		 
		VariableContribution vc ;
		
		variableContributions.sort(1) ; // smallest score deltas (strongest negative ones) first == those with highest contribution
		
		for (int i=0;i<variableContributions.size();i++){
			vc = variableContributions.getItem(i) ;
			
			
		} // i->
	}



	private ModelProperties performSingleRun(int index){
		
		SomProcessIntf somprocess ;
		SimpleSingleModel simo ;
		ModelProperties somResults;
		
		SomFluidTask _task = new SomFluidTask(sfTask);
		_task.setNoHostInforming(true);
		
		if (somProcess!=null){
			// clear the extensionality of the lattice
		} else{
			if (somProcess!=null){
				somProcess.clear();
			}
			simo = null; 
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
		//  sqdata ("SomQuality") is still null here, will be calculated later 
		// as part of EvoMetrik in EvoMetrices in "registerResults()"
		
		somResults.setIndex(index);
		
		// somProcess.clear();
		// simo = null; // ??
		System.gc(); out.delay(100) ;
		return somResults;
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
	
	
	public VariableContributions getVariableContributions() {
		return variableContributions;
	}

	public VariableContrasts getVariableContrasts() {
		return variableContrasts;
	}
	
	// ........................................................................
	/**
	 * SVD on correlational matrix, Cronbach's alpha 
	 */
	class MappingChecks  implements Runnable{

		public MappingChecks(){
		
		}

		
		private double cronbachAlpha(){
		
			// http://de.wikipedia.org/wiki/Cronbachs_Alpha  , 
			// = indication for consistency :: calculate two versions, one with native r, other with abs(r)
			// measure for internal consistency of a scale 
			
			// 1. calculate average of all correlation coefficients between all predictors := "cr"
			
			// 2. calculate ( N * cr)/(1 + (N-1)*cr), where N number of included predictors
			
			return 0.0;
		}
		
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	

	// ........................................................................
	/**
	 * 
	 */
	class LinearityChecks  implements Runnable{
		
		ClassificationSettings cs ;
		Variables variables;
		
		/** the first list represents the target group, the second one the non-target group, 
		 * as it is implied by the target variable and the the ECR */
		ArrayList<ArrayList<Long>> recordIndexes ;  
		ArrayList<String> colHeaders;
		
		
		Thread linearChkThrd;
		boolean checkingIsRunning=false ;
		
		//-----------------------------------------------------------
		public LinearityChecks(){
			linearChkThrd = new Thread(this, "linearChkThrd") ;
			variables = somData.variables ;
			
			cs = modelingSettings.getClassifySettings() ;
		}
		//-----------------------------------------------------------

		
		/**
		 * 
		 * this checks for USED (mode=0) or ALL (mode=1) variables the statistical description and contrasts
		 * between the groups as they are established by 
		 * - target clusters according to ECR
		 * - 
		 * 
		 * @param mode
		 */
		private void dedicatedLinearityCheck( int mode ) {
			
			boolean nodeRepresentsTarget ;
			double effectiveECR, cval ,nodesPpv;
			int gix,vix, nix,vn,targetGroupSize, casesCount;
			String vlabel;
			double prefSens,effectivePpv,effectiveSens;
			int nodecases = 0 ;
			
			Variable variable;
			VariableContrast vci ;
			
			int tvIndex;
			VirtualLattice somLattice;
			MetaNode node ;

			IndexDistance ntNodeSlot;
			IndexedDistances nontargets = new IndexedDistances();
			
			vn = variableContrasts.size();
			
			recordIndexes = new ArrayList<ArrayList<Long>>();
			recordIndexes.add( new ArrayList<Long>()) ;
			recordIndexes.add( new ArrayList<Long>()) ;
			
			colHeaders = somData.normalizedSomData.getColumnHeaders();
			variableContrasts.setTvIndex( variables.getTvColumnIndex()) ;
			
			// preparing the structure
			
			for (int i=0;i<variables.size();i++){
				
				variable = variables.getItem(i) ;
				vlabel = variable.getLabel() ;
				
				if ((variables.openForInspection(variable)) || (i==variables.getTvColumnIndex())){
					
					vci = new VariableContrast( variableContrasts ); 	
					vci.setVariableLabel( vlabel ) ;
					vix = colHeaders.indexOf(vlabel) ;
					int vix2 = variables.getIndexByLabel(vlabel) ;
					vci.setVariableIndex(vix);
					
					if ( currentVariableSelection.indexOf(vlabel)>=0){
						vci.setUsed(true) ;
					}
					vci.setVariableReference(variable) ;
					variableContrasts.addItem(vci);
				} // not excluded by any reason ...
				
			} // i->
			
			// on option, we shifted the ECR such that the sensitivity is met
			
			effectiveECR = cs.getEcr() ; // TODO: later: taking the effective SOM from the lattice, where we have stored it 
			
			
			// first: establishing the groups of data, in a first step as 2 lists (TG, non-TG) of record indexes  
			
			somLattice = somHost.getSomProcess().getSomLattice() ;	
				
			for (int n=0;n<somLattice.getNodes().size();n++){
				
				node = somLattice.getNodes().get(n) ;
				
				nodesPpv = node.getExtensionality().getPPV() ;
				nodeRepresentsTarget = (1.0 - nodesPpv <= effectiveECR) ;
				
				if (nodeRepresentsTarget ){
					gix = 0;
					recordIndexes.get(gix).addAll( node.getExtensionality().getListOfRecords() );	
				}else{
					nontargets.add( new IndexDistance( n,0, nodesPpv,"") );
				}
				
			} // n->
			
			
			int capacityCount = (int) (classificationSettings.getCapacityAsSelectedTotal() * somLattice.getModelProperties().getTrainingSample().getSampleSize() ); 
			targetGroupSize = recordIndexes.get(0).size() ;
			/*
			 	cs.setErrorCostRatioRiskPreference( 0.18 );  
				cs.setPreferredSensitivity( 0.15);   
				cs.setCapacityAsSelectedTotal( 0.35 );
				cs.setEcrAdaptationAllowed(true) ;
			 */
			if (classificationSettings.isEcrAdaptationAllowed()){
				nontargets.sort(-1) ;
				casesCount = somLattice.getModelProperties().getTrainingSample().getCasesCount() ;
				prefSens = classificationSettings.getPreferredSensitivity();
				effectiveSens = ((double)targetGroupSize/(double)casesCount) ;
				
				if ((prefSens > 0.0) && (effectiveSens<prefSens)){
					nodesPpv = 0.0;
					
					for (int nt=0;nt<nontargets.size();nt++){
						
						ntNodeSlot = nontargets.getItem(nt) ;
						nix = ntNodeSlot.getIndex() ;
						
						node = somLattice.getNodes().get(nix) ; 
						// the ppv of that node
						nodesPpv = node.getExtensionality().getPPV() ;
						
						// would it be still < capacityCount ?
						if ((recordIndexes.get(0).size() + node.getExtensionality().getListOfRecords().size())<capacityCount){
							
							recordIndexes.get(0).addAll( node.getExtensionality().getListOfRecords() );
							ntNodeSlot.setSecindex(1);
							// get true cases of it
							nodecases =  nodecases + (int) (nodesPpv * node.getExtensionality().getListOfRecords().size()) ;
						}else{
							break ;
						}
						if (nodecases > (int)(prefSens * (double)casesCount) ){
							break;
						}
					} // -> all items in ixds catalog
					effectiveECR = (1.0-nodesPpv) ;
				} 
			
			} // adapt target group selection in case of low frequency ?
			
			nix=0;
				
			// put all (of the rest) items in catalog to the non target group
			for (int nt=0;nt<nontargets.size();nt++){
				
				ntNodeSlot = nontargets.getItem(nt) ;
				nix = ntNodeSlot.getIndex() ;
				
				if (ntNodeSlot.getSecindex()<=0){ // =1 we put it to the target group
					// do it
					node = somLattice.getNodes().get(nix) ; 
					recordIndexes.get(1).addAll( node.getExtensionality().getListOfRecords() );
				}
			} // all items in ixds catalog
			
			targetGroupSize = recordIndexes.get(0).size() ;
			if (targetGroupSize<5){
				variableContrasts.setRemainsUndefined(true);
				return;
			}
			
			gix=0;
			DataTableCol datacol;
			
			// crossing the two groups with variables 
			try{
				ArrayList<Double> tgValues = new ArrayList<Double>() ;
				ArrayList<Double> nonTgValues = new ArrayList<Double>() ;
				
				VariableContrast vc; 
				
				for (int i=0;i<variableContrasts.size();i++){
					vc = null;
					vc = variableContrasts.getItem(i) ;
					 
					// for this variable: looking up and collecting the values for record indexes in normalized data table,
					// each VariableContrast contains a 2 column table of values
					vix = vc.getVariableIndex();
					datacol = somData.normalizedSomData.getColumn(vix) ;
					
					int rc = datacol.getCellValues().size() ;
					
					vc.setGroupedValues( new double[3][rc]) ;
					vc.initializeGroupedValues(-1.0);
					
					nonTgValues.clear();
					tgValues.clear();
					
					for (int r=0;r<datacol.getCellValues().size();r++){
						
						cval = datacol.getCellValues().get(r) ;
						
						// TODO: ???? reference of record index: structural index (starting with ???) or value of index column?
						
						if (recordIndexes.get(0).indexOf(r)>=0){ 
							tgValues.add(cval);
							// vc.getGroupedValues()[0][r] = cval ;
						}
						if (recordIndexes.get(1).indexOf(r)>=0){
							nonTgValues.add(cval);
							
						}
					} // r-> all data row values
				
					vc.getGroupedValues()[0] = (double[]) arrutil.changeArrayStyle(tgValues) ;
					vc.getGroupedValues()[1] = (double[]) arrutil.changeArrayStyle(nonTgValues) ;
					 
					nonTgValues.addAll(tgValues);
					vc.getGroupedValues()[2] = (double[]) arrutil.changeArrayStyle(nonTgValues) ;
					
					nonTgValues.clear();
					tgValues.clear();
				} // i->
				
				gix=0;
				// calculating the statistics: location contrast, and ratio's thereof, first the used, and then the not-used variables
				for (int i=0;i<variableContrasts.size();i++){
					
					if (i!=variables.getTvColumnIndex()){
						
						vc = variableContrasts.getItem(i) ;
						String str = vc.getVariableLabel() ;
											out.print(3,"  . . .  calculating contribution statistics for variable : "+str);
						vc.calculateSummaryStatistics();
					}
				}
				
				
			}catch(Exception e){	
				e.printStackTrace();
			}
			
			variableContrasts.setRemainsUndefined(false);
		} // dedicatedLinearityCheck()
		
		
		public void start() {
			linearChkThrd.start() ;
		}
		
		@Override
		public void run() {
			checkingIsRunning = true;
			
			dedicatedLinearityCheck( -1 );
			
			if (completeCheck){
				dedicatedLinearityCheck( 1 );
			}
			
			checkingIsRunning = false;
			isCalculated = true;
			
		}
		
		
	}
	// ........................................................................


	class ContributionChecks implements Runnable{
		
		SomQuality somquality;
		Thread contriChkThrd;
		boolean checkingIsRunning=false;
		
		public ContributionChecks(){
			contriChkThrd = new Thread(this, "contchkThrd") ;
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
		@SuppressWarnings({ "unused", "unchecked" })
		private void dedicatedContributionCheck( int mode ) {
			 
			String selectedVariable,str;
			boolean isNewBest ;
			int  smode=1,ix, currentBestHistoryIndex=-1, initialLoopCount=-1,bestModelHistoryIndex , ccLoopCount=-1;
			
			SomQualityData bestSqData = new SomQualityData();
			ArrayList<String> baseMetric = new ArrayList<String>(), bestMetric = new ArrayList<String>();
			ArrayList<Integer> proposedSelection = null, baseMetricIndexes, av=null, rv=null ;
			ArrayList<Double> uv ;
			ArrayList<Double> previousUseVector ;
			
			ModelProperties results;
			Variable variable;
			EvoMetrices _evometrices = null ;// = new EvoMetrices( variableContributions.evometrices, false ); 
			 
			
			initialLoopCount = evoMetrices.getCurrentBaseMetrikIndex();
			initialLoopCount = evoMetrices.getExploredMetrices().size()-1 ;
			ccLoopCount = initialLoopCount ;
			
			
			Variables variables = somData.getVariables() ;
			System.gc();
			
			try{
												System.out.println();
												out.printErr(2, "\n...SomModelDescription, dedicatedVariableCheck(): a posteriori test for contribution per variable, base metric: { }...");
				  								System.out.println();
				  			
				  	boolean canonicalAdoption = true;
				  	int outperformingContributionTextIndex = -1;
				  	ArrayList<String> outperformingMetric = new ArrayList<String>(); 
				  	
				  	while (canonicalAdoption){
				  		
				  		canonicalAdoption = false;
				  		outperformingContributionTextIndex = -1;
				  		

						baseMetricIndexes = variables.getIndexesForLabelsList(currentVariableSelection) ; 
						baseMetric = new ArrayList<String>(currentVariableSelection);
						
						bestMetric = new ArrayList<String>(baseVariableSelection) ;
						previousUseVector = variables.getUseIndicationForLabelsList( baseMetric ) ;
						
						_evometrices = new EvoMetrices(somHost, 0 ); 
						
						
						// _evometrices.setCurrentBaseMetrik( evoMetrices.getBestResult() );
						_evometrices.setCurrentBaseEvoMetrik( evoMetrices.getBestResult() );
						variableContributions.setBaseScore(evoMetrices.getBestResult().getActualScore()) ;
						variableContributions.setBaseMetric( baseVariableSelection );
						
						variableContributions.setBestSqData( evoMetrices.getEvmItems().get(0).getSqData() );
						
						
						
						int z=0;
						int i=0, di=0 ;
						
						while (i<baseMetric.size()){
							z++;
							ccLoopCount++;
							
							// this variable we remove, then we check the results, the difference provides the contribution
							// there are 2*3 types of contributions: affecting TP, FP or both, either positively or negatively
							// additionally we may distinguish between pure TP, pure FP efficacy
							selectedVariable = baseMetric.get(i) ;
							
							int _ix = variables.getIndexByLabel(selectedVariable);
							variable = variables.getItem(_ix);
							
							boolean hb = (variable.isIndexcandidate() || (_ix==variables.getTvColumnIndex()) ||
										  variable.isTVcandidate() || variable.isTV() || variable.isID() || 
										  variables.getBlacklistLabels().indexOf(selectedVariable)>=0 );
							if ((hb) || (variables.openForInspection(variable)==false)){
								di++;
								i++; continue;
							}
							
													
							currentVariableSelection.clear();
							currentVariableSelection.addAll( baseMetric );
							
							ix = currentVariableSelection.indexOf( selectedVariable );
							currentVariableSelection.remove( ix ) ;
												
													
							uv = variables.getUseIndicationForLabelsList(currentVariableSelection) ;
							proposedSelection = (ArrayList<Integer>) variables.transcribeUseIndications( currentVariableSelection ) ;
										        Collections.sort(proposedSelection);
							av = variables.determineAddedVariables( previousUseVector, uv, false);
							rv = variables.determineRemovedVariables( previousUseVector, uv,false);
							
												out.printErr(2,	"...checking contribution for variable (index:"+(variables.getIndexByLabel(selectedVariable))+
																", "+(i+1-di)+" of "+(baseMetric.size()-di)+"): "+
																selectedVariable+"\n");
												
							          System.gc(); out.delay(10) ;  										
							results = performSingleRun(z); 
							  
							   
							// ..................................................
							
							// EvoMetrik.SomQualityData.sqdata still = null here... 
							// 
							
							if (results.getTrainingSample().getObservationCount()<8 ){ 
								i++;
								continue;
							}
							// _evometrices is shortcut for "variableContributions.evometrices"
							isNewBest = _evometrices.registerResults( z, results , uv, smode) ; 
							
							// adapting the evoweights
							// calls "evoBasics.getEvoTasks().updateEvoTaskItem", 
							// for the respective variable index positions, then renormalizeParameters();
							_evometrices.registerMetricChangeEffects(av, rv, true);
							_evometrices.registerMetricAsExplored(uv);
							
													Collections.sort(proposedSelection);
													str = arrutil.arr2text(proposedSelection);
													out.printErr(1, "proposed Selection: "+str+"\n") ;	
						
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
								i++;
								continue;
							}
							
							
							EvoMetrik em = _evometrices.getItems().get( _evometrices.size()-1) ;
							double _cScore = em.getSqData().getScore() ;
							
							results.sqData = new SomQualityData( em.getSqData() );
							
							double scdv = variableContributions.getBaseScore() - _cScore ; // results.sqData.getScore();
							vc.setScoreDelta(scdv) ;
							 
							vc.setSqData(new SomQualityData( results.sqData )) ; 
							
							variableContributions.getItems().add(vc);

							
							int zn = somProcess.getSomLattice().getNodes().size();
							zn = somProcess.getSomLattice().getNodes().size();
							
							somProcess.clear();
							 
							
							if (isNewBest){
								bestSqData = new SomQualityData( em.getSqData() );
							}
							if ((isNewBest) || (z<=1)){
								
								bestMetric = variables.getLabelsForUseIndicationVector( somData.getVariables(), _evometrices.getBestResult().getUsageVector()) ;
								currentBestHistoryIndex = ccLoopCount;
								// we should at least restart the final descriptive analysis: contribution
								outperformingContributionTextIndex = z-1; // TODO: is this pointing to the correct evometrik ??
								outperformingMetric = currentVariableSelection;
								
							}

							
							System.gc();
							i++;
						} // i-> all items from provided list
						  
					
						if (baseMetric.size()>0) {
							currentVariableSelection.clear();
							currentVariableSelection.addAll( baseMetric ) ;
						}
					
						// integrating metrices  , TODO: does this work ?
						_evometrices = evoBasics.integrateEvoMetricHistories( variableContributions.getEvometrices(), 
																			  _evometrices, ccLoopCount );
						// 
						
						
				  		if (outperformingContributionTextIndex>0){
				  			// adopt currentVariableSelection
				  			
				  			bestModelHistoryIndex = outperformingContributionTextIndex ;
				  			currentVariableSelection = outperformingMetric;
				  			
				  			// clear contribution structure
				  			variableContributions.clear();
				  			variableContrasts.clear() ;

				  			evoBasics.setBestModelHistoryIndex(bestModelHistoryIndex);
				  			
				  			// bestSqData.g
				  			
				  			outperformingMetric.clear();
				  			canonicalAdoption = true;
				  			
				  		}
				  	} // -> canonicalAdoption = false... repeats if a new best metric would be found
				  	
				variableContributions.setEvometrices( _evometrices );
				ArrayList<EvoMetrik> ems = _evometrices.getItems(); 
				
				// -> to main list of evo-devo-history .... TODO: check if this is also sorting !!!
				evoMetrices = evoBasics.integrateEvoMetricHistories( evoMetrices, _evometrices, 0);
				
				/*
				EvoMetrik em = ems.get( currentBestHistoryIndex );
				EvoMetrices vems = variableContributions.getEvometrices();
				EvoMetrik vem = vems.getBestResult();
				
				if ((vem.getSqData()==null) && (bestSqData!=null) ){// && (em.getSqData()!=null)
					vem.setSqData( em.getSqData() ) ;
				}
				*/
				
			}catch(Exception e){
				str="";
				out.printErr(2, str);
				
				e.printStackTrace() ;
			}
			ix=0;
		}

		public void start() {
		 
			contriChkThrd.start() ;
		}

	 
		@Override
		public void run() {
			 
			checkingIsRunning = true;
			
			dedicatedContributionCheck( -1 );
			
			if (completeCheck){
				dedicatedContributionCheck( 1 );
			}
			
			checkingIsRunning = false;
			isCalculated = true;
		}
	
		
	}


	public boolean isCalculated() {
		return isCalculated;
	}



	public void setCalculated(boolean isCalculated) {
		this.isCalculated = isCalculated;
	}
		 
	/**
	 * 
	 * 
	 * @param evometrices
	 * @param top_n
	 */
	public void identifyMostLinearModels( EvoMetrices evometrices, int top_n){
		
		// list must be sorted...
		
		// 
		
	}
	
}

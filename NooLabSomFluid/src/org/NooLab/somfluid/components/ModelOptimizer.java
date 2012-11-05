package org.NooLab.somfluid.components;

import java.util.ArrayList;

 
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;

import org.math.array.StatisticSample;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.*;
import org.NooLab.utilities.nums.NumUtilities;
import org.NooLab.utilities.strings.ArrUtilities;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.repulsive.components.data.SurroundResults;

import org.NooLab.math3.stat.correlation.SpearmansCorrelation;
import org.NooLab.math3.stat.inference.MannWhitneyUTest;
import org.NooLab.somfluid.*;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.* ;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.tasks.SomFluidTask;

 
import org.NooLab.somfluid.components.post.Coarseness;
import org.NooLab.somfluid.components.post.MultiCrossValidation;
import org.NooLab.somfluid.components.post.OutResults;
import org.NooLab.somfluid.components.post.ParetoPopulationExplorer;
import org.NooLab.somfluid.components.post.RobustModelSelector;
import org.NooLab.somfluid.components.post.SomOptimizerXmlReport;
import org.NooLab.somfluid.components.post.SpelaResults;
import org.NooLab.somfluid.components.variables.VariableSubsets;
import org.NooLab.somfluid.core.SomProcessIntf;

import org.NooLab.somfluid.core.engines.det.*;
import org.NooLab.somfluid.core.engines.det.results.*;

import org.NooLab.somscreen.*;
import org.NooLab.somscreen.linear.LinearStatsDescription;
import org.NooLab.somscreen.linear.MapCorrelation;
import org.NooLab.somsprite.*;
 
import org.NooLab.somtransform.SomTransformer;



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
 * TODO : add entries in exported xml package
 *       - expiry  
 *          - timestamp (long) where this model invalidates
 *          - %change of data 
 *          - remodeling 1/0 auto-update of model : an external instance may check this section and start remodeling
 * 
 * 
 * 		<som index="0">
    		<project>
      			<general>
        			<name label="bank2" />
 *       
 *      basic description of variables: linearity score, contribution 
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
	VariableSubsets variablesPartition ;
	ArrayList<OptimizerProcess> processes ;
	
	ModelOptimizer modOpti;
	int resumeMode;
	
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;

	SpelaResults spelaResults ;
	OutResults outresult;

	public ModelProperties mozResults;
	ArrayList<Integer> freshlyAddedVariables = new ArrayList<Integer> ();
	ArrayList<Integer> allAddedVariables = new ArrayList<Integer> ();

	
	transient StatisticSample sampler = new StatisticSample(172839);
	transient PrintLog out ; 
	transient ArrUtilities arrutil = new ArrUtilities();
	transient NumUtilities numutil= new NumUtilities ();
	
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
		sfTask.setTaskType( SomFluidTask._TASK_MODELING); // "M" 
		
		resumeMode = sfTask.getResumeMode() ;
		
		spelaResults = new SpelaResults();
		
		xmlReport = new SomOptimizerXmlReport( this );
		
		if (sfProperties.getInitialNodeCount()>200){ // mpp for multidigester
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
			
			somTransformer = somDataObj.getTransformer().getSelfReference();
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

			somDataObj.variableLabels = somDataObj.variables.getLabelsForVariablesList(somDataObj.variables) ;
			
			variablesPartition = new VariableSubsets(this);
			
			processes = new ArrayList<OptimizerProcess>();
			OptimizerProcess process;
			int n = 1;

			n = preparePartitions();
			n=1; 
			
			// in principle we could send these tasks to different threads or machines
			for (int i=0;i<n;i++){
				process = new OptimizerProcess( this, variablesPartition, i);
				processes.add(process);
			}

			sfProperties.getOutputSettings().setLastResultLocation("");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void perform() {
		 
		
		for (OptimizerProcess process: processes){
			// int activatedPix = 
			process.start() ;
		}
		 
		// if we had several partitions, we must collect them into a single one...
		
	}
	 

	/**
	 * if we have a large set of variables we create overlapping splits
	 * 
	 * 
	 * @throws Exception
	 */
	private int preparePartitions() throws Exception{
		
		int partCount=1;
		int np=1,bnVar,nVar ;
		String str;
		ArrayList<String> currVariableSelection ; 
		Variables variables = modOpti.somDataObj.variables ;
		
		nVar  = modOpti.somDataObj.getNormalizedDataTable().getColcount() ;
		bnVar = variables.getBlacklistLabels().size() ;
		
		// this should be included everywhere
		currVariableSelection = modelingSettings.getInitialVariableSelection() ;
		if (currVariableSelection==null) currVariableSelection = new ArrayList<String>();
		
		Variable tvar = variables.getTargetVariable();
		if (tvar!=null){
			str = tvar.getLabel() ;
			if (str.length()>0) currVariableSelection.add( str ) ;
		}
		
		variablesPartition.setSharedSet(currVariableSelection);
		
		str = variables.getIdLabel();
		if (str.length()>0) currVariableSelection.add( str ) ;
		
		if (nVar-bnVar > 80){
			nVar = nVar-bnVar;
			
		}else{
			np=1;
		}
		
		np=1;
		variablesPartition.prepare(np) ;
		
		return partCount;
	}
	
	
	public void saveResults() {
		OutputSettings outs = sfProperties.getOutputSettings();
		
		if (outs.writeResultFiles()){
			
			DFutils fileutil = new DFutils();
			
			boolean asHtmlTable= outs.isHtmlCompatible() ;
			
			String dir = sfProperties.getSystemRootDir();                        // D:/data/projects/
			String prj = sfProperties.getPersistenceSettings().getProjectName(); // +"bank2" -> D:/data/projects/bank2/
			
			dir = DFutils.createPath(dir, prj);
			dir = DFutils.createPath(dir, "export/results/");
			
			// 111  1000
			String xRootDir = fileutil.createEnumeratedSubDir( dir, "", 0, 19, -3 ); // 19 = maxCount, -3 = remove oldest by date, -2 = remove first by sort
											//  basePath, subdirPrefix, startEnumValue , maxCount, removeMode){
			String srXstring = getOutResultsAsXml(asHtmlTable);
			
			String filename = DFutils.createPath(xRootDir, "somresults.xml");
			fileutil.writeFileSimple(filename, srXstring);	
			
			if (fileutil.fileexists(filename)){
				out.print(2, "results of model optimizing have been written to file... \n"+
							 "                       "+filename ) ;
				sfProperties.getOutputSettings().setLastResultLocation(xRootDir);
			}
		}
		
	}



	// should be done earlier... where moz is created in SomFluid 
	public void checkConsistencyOfRequest() throws Exception {
	
		boolean requestIsOk = true;
		DataTableCol tvcol = null;
		String msgStr="" ,tvLabel="";
		int tvindex = -1 ; 
		ClassificationSettings cs ;
		double[][] tgGroups ;
		
		
		Variables variables =  somDataObj.getVariables() ;
		DataTable ndatatable = somDataObj.getNormalizedDataTable() ;
		cs = sfProperties.getModelingSettings().getClassifySettings();
		tgGroups = cs.getTargetGroupDefinition();
		
		cs.setActiveTargetVariable( tvLabel = variables.getTargetVariable().getLabel() );
		
		// we have to check rather soon whether such values occur at all 
		// in the TV column of the table: on data import, or reloading of the SomData 
	
		// sfPropertiesDefaults.setSingleTargetDefinition( "raw", 0.1, 0.41, "intermediate" ) ;
		// the Single-Target-Mode can be defined with several non-contiguous intervals within [0..1] 
	
		// 1. do we work in somType = mono?
		if (sfProperties.getSomType() == FieldIntf._SOMTYPE_PROB){
			requestIsOk = false;
			msgStr = "The type of som (associative storage) does not match the type of the process (Optimizer)." ;
		}
		if (requestIsOk){
		// 2. does the target variable exist ? and are values >0 ? how many MV in TV column
			tvindex = variables.getTvColumnIndex() ; 
			tvLabel = variables.getTargetVariable().getLabel() ;
			if (tvLabel==null)tvLabel="" ;
			
			if ((tvindex<0) && (tvLabel.length()==0)){
				requestIsOk = false;
				msgStr = "type of process is 'optimization', yet, no target variable definition could be found." ;
			}
		}
		if (requestIsOk){
			
			if ((tvindex>=0) && (ndatatable.getColumnHeaders().indexOf(tvLabel)!=tvindex)){
				requestIsOk = false;
				msgStr = "mismatch in target veriable pointers.";
			}
		}
	
		if (requestIsOk){
			tvcol = ndatatable.getColumn(tvindex) ;
			
			int nv = tvcol.getCellValues().size();
			if (nv<3){
				requestIsOk = false;
				msgStr = "no data found in target column of table containing normalized data." ;
			}
		}
		
		if (requestIsOk){
			if ((tgGroups==null) || (tgGroups.length==0)){
				requestIsOk = false;
				msgStr = "No target group has been defined, thus no modeling is possible." ;	
			}
		}
		
		if ((requestIsOk) && (tvcol!=null)){
			int ix,mvc=0,tgx=0,tgc=0;
			ix=0;
			for (int i=0;i<tvcol.getCellValues().size();i++){
				double v=tvcol.getCellValues().get(i);
				
				if (v<0){
					mvc++;
				}else{ 
					ix = numutil.isWithInIntervals(tgGroups,v,1);
					// tgGroups
					// boolean hb = NumUtilities
					if (ix<0){
						tgx++;
					}else{
						tgc++;
					}
				}
			} // i-> all values
			if (tgc<=3){
				requestIsOk = false;
				msgStr = "The number of cases according to target group definition is too low (n="+tgc+"), no modeling is possible, thus stopping." ;
			}
			double mr = (double)mvc/(double)tvcol.getCellValues().size();
			if ((requestIsOk) && ((mr>0.81) || ( tgc<=8))){
				requestIsOk = false;
				msgStr = "The number valid records (non-MV) and cases in the target column is preventing reasonable modeling";
			}
		}
	
		if (requestIsOk){
		// 3. will the definition of the target groups yield any case count >3 
		
		}
		
		if (requestIsOk == false){
			throw(new Exception("Request for running the Optimizer is not consistent: "+ msgStr));
		}
	}

	
	public void detectCollinearVariables() {
		
		
		ArrayList<String> vLabels;
		Variables variables = somDataObj.variables;
		Variable v;
		
		vLabels = variables.getLabelsForVariablesList(variables);
		
		
		int i=vLabels.size()-1;
		while (i>=0){
			
			String vlabel = vLabels.get(i);
			v = variables.getItem(i) ;
			if ( (variables.getBlacklistLabels().indexOf(vlabel)>=0) ||
				 (variables.getAbsoluteFieldExclusions().indexOf(vlabel)>=0) ||
				 (v.isIndexcandidate() || (v.isTVcandidate()) || (v.isID())|| (v.isTV()))
			    ){
				vLabels.remove(i) ;
			}
			
			i--;
		} // i->0
		detectCollinearVariables(vLabels) ;
	}

	/**
	 * it is assumed that any check on blacklisted etc has been accomplished in advance !
	 * 
	 * @param vLabels
	 */
	public void detectCollinearVariables( ArrayList<String> vLabels) {
		
		int ix,rc, cc, nc = 0;
		String varLabel;
		Variables variables = somDataObj.variables;
		SomMapTable xSomMap = new SomMapTable();
		IndexedDistances topCorrelations;
		ArrayList<String> selectedVarLabels ;
		ArrayList<Integer> topIxes, allCollinears = new ArrayList<Integer> ();
		IndexedDistances ixds = new IndexedDistances ();
		
		DataTable ntable = somDataObj.getNormalizedDataTable()  ;
		
		if (ntable.colcount()==0){
			return ;
		}
		
		DataTableCol col = ntable.getColumn(0);
		
											out.print(2, "detecting collinearity among variables...");
		try {
			rc = 0;
			cc = vLabels.size();
			rc = col.getCellValues().size() ;
			xSomMap.variables = (String[]) arrutil.changeArrayStyle( vLabels ) ;
			xSomMap.values = new double[rc][cc] ;
			
			for (int i = 0; i < vLabels.size(); i++) {

				varLabel = vLabels.get(i);
				ix = variables.getIndexByLabel(varLabel);
				ix = ntable.getColumnHeaders().indexOf(varLabel) ;
				
				if (ix>=0){
					col = ntable.getColumn(ix) ;
					int csz = col.getCellValues().size() ;
					for (int z=0;z<csz;z++){
						xSomMap.values[z][i] = col.getCellValues().get(z); 
					}
					
				}else{}

			} // i->

			if ((xSomMap != null) && (xSomMap.variables.length >= 3)) {

				MapCorrelation mc = new MapCorrelation(somDataObj, xSomMap);

				mc.setMissingValue(-1.0);
				mc.calculateMatrix();

				ix=0;
				// now we have to retrieve for each row the maximum values
				double[][] rMatrix = mc.getMatrix() ;
				
				for (int i=0;i< rMatrix.length;i++){
					
					varLabel = xSomMap.variables[i] ;
					
					topIxes = mc.getMatrixRowTopValues(i, 0.81, 0.999, 50.0) ; // 1st: corr coeff, 2nd: quantile as a max value 
					
					// we keep only variable i and remove topIxes
					// yet, first we collect all indexes from xSomMap
					if ((topIxes!=null) && (topIxes.size()>0) ){
						
						double v = xSomMap.values[i][ topIxes.get(0)];
						if ((v<0.8) || (v==1.0)){
							continue;
						}
						allCollinears.addAll(topIxes) ;
						
						for (int k=0;k<topIxes.size();k++){
							int    vix  = variables.getIndexByLabel(varLabel) ;
							       ix   = topIxes.get(k);
							String vlabel = xSomMap.variables[ix] ;
							int    cix  = variables.getIndexByLabel(vlabel) ;
							double cv   = rMatrix[i][ix] ;
							if (vix != cix){
								ixds.add( new IndexDistance( cix,vix,cv,vlabel) ) ;
							} // probably a dedicated list dealing with collinearity ?
						}     // we could need the getInProcessExclusions for more short-living moves...
					}
					ix=0;
				} // i-> all rows
				
				// Collections.sort( allCollinears );
				
				// selectedVarLabels = mc.getSelectionLabels( allCollinears );
				
				ix=0;
				
				
			} // anything available

		 
			if ((ixds.size() > ((double)vLabels.size()/10.0)) || (ixds.size()>20)){
				variables.getInProcessExclusions().addAll(ixds) ;
				nc = ixds.size();
			}else{
				nc=0;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
											out.print(2, "detecting collinearity among variables completed, "+nc+" collinear items found.");
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
	private ArrayList<Double> performInitialGuessOfWeights() {
		
		ArrayList<Double> guesses = new ArrayList<Double>();
		int tvindex=-1;
		Variables variables;
		String tvarLabel="", varlabel ;
		double score1,score2,gscore ,v,_max=-99999999.09, _min=99999999.09;
		
		variables = somDataObj.variables ;
		tvindex = variables.getTvColumnIndex() ;
		
		if (tvindex>=0){
			tvarLabel = variables.getItem(tvindex).getLabel() ;
		}
		ArrayList<Double> tvColData, varColData = new ArrayList<Double>(); 
		
		
		if ((tvindex <0) || (tvarLabel.length()==0)){
			return guesses;
		}
		
		tvColData = somDataObj.normalizedSomData.getColumn(tvindex).getCellValues() ;
		
		MannWhitneyUTest mwu = new MannWhitneyUTest();
		SpearmansCorrelation spc = new SpearmansCorrelation ();
		
		
		mwu.setReferenceData( tvColData ) ;
		spc.setReferenceData( tvColData ) ;
											int outlevel=3;
											if (variables.size()>100){
												outlevel=2;
											}
											out.print(2, "calculating the initial guesses for weights based on raw data...");
		for (int i=0;i<variables.size();i++){
			
											out.printprc(outlevel, i, variables.size(), variables.size()/10, "") ;
			
			varlabel = variables.getItem(i).getLabel();
			varColData.clear();
			guesses.add(0.0) ;
			
			if ((variables.openForInspection( variables.getItem(i))) &&
				(variables.isTargetVariableCandidate(varlabel, 0)==false) &&
				(variables.getAbsoluteFieldExclusions().indexOf(varlabel)<0)){
				varColData.addAll( somDataObj.normalizedSomData.getColumn(i).getCellValues() ) ;
				
				if (varColData.size()<tvColData.size()){
					// should not happen anyway... 
					for (int z=0;z<(tvColData.size()-varColData.size());z++){
						varColData.add(-1.0);
					}
				}
				try {
					
					double uValue = mwu.mannWhitneyU( varColData );
					v = mwu.getpValue();
					score1 = 0.5 + (1.0-v); // we hope for inseparability : lower scores if columns are different (= p-Value larger))
				} catch (Exception e) {
					score1 = 0.0;
				} 

				
				// calculate abs(correlation) and its r^2 between those two columns
				// we calculate score1 = (1+ m) * (1+r^2) ;
				score2 = 0.0;
				try {
if (varlabel.toLowerCase().contains("_c")){
	int kk;
	kk=0;
}
					score2 = Math.abs( spc.correlation(varColData) ) ;
					
				} catch (Exception e) {
					out.printErr(3, "problem in spc for variable <"+varlabel+">...") ;
				} 
				
				// mix these results into an initial weight, which always is >=0.5 !!
				// we calculate (score1 + score2)/2 or geometric mean ...
				if (score2>=0.0){
					gscore = (score1 + 2.0*score2)/3.0;
					guesses.set(i,gscore);
				}else{
					gscore = -1.0;
				}
				if (gscore!=-1.0){
					if (_max<gscore)_max=gscore;
					if (_min>gscore)_min=gscore;
				}
			}
			
		} // i->
		
		for (int i=0;i<guesses.size();i++){
			
			v = guesses.get(i);
			varlabel = variables.getItem(i).getLabel();
			
			if ((i!=tvindex) && (v>0.01)){
				
				gscore = 0.43 + Math.abs((v - _min)/(_max-_min) )/7.0;
				guesses.set(i,gscore);
			}else{
				if (v!=0.0){
					guesses.set(i,0.4);
				}
			}
		}
		
		varColData.clear();varColData=null;
		return guesses;
	}



	class OptimizerProcess implements Runnable{
		
		int optimizerProcIndex;
		ModelOptimizer moptiParent;
		VariableSubsets variableSubsets;
		
		SomSprite dependencyCheck;
		
		SomScreening somScreening  =null;
		int contributionCheckings=0;
		
		Thread moptiThrd;
		ArrayList<Integer> variableSubsetIndexes ;
		private ArrayList<String> currentVariableSelection = new ArrayList<String>() ;
		private int lastCanonicalStepCount=0;
		private boolean finalRefinement = false;
		private ArrayList<Double> initialweights;
		
		
		// --------------------------------------------------------------------
		public OptimizerProcess( ModelOptimizer mopti, VariableSubsets subsets, int index){
			
			optimizerProcIndex = index;
			this.moptiParent = mopti;
			variableSubsets = subsets;
			
			variableSubsetIndexes = subsets.getSubset(index) ;
			moptiThrd = new Thread(this,"mOptiProcThrd-"+index) ;
		}
		// --------------------------------------------------------------------
		
		public int start(){
			moptiThrd.start();
			return optimizerProcIndex;
		}
		
		@SuppressWarnings("unchecked")
		public void optimizeOnVariableSubset(){
			
			// variableSubsetIndexes
			boolean done=false, breakLoop=false,candidatesOK, caredForEvoBalance=false;
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
			
			currentVariableSelection = somDataObj.getVariables().confirmVariablesAvailability(currentVariableSelection);
			
			// sfProperties.getModelingSettings().
			if (currentVariableSelection.size()<=1){
				LinearStatsDescription lsd = new LinearStatsDescription( somDataObj.getNormalizedDataTable() );
				lsd.setTargetVariable("");
				lsd.setExcludedVariables( somDataObj.getVariables().getBlacklistLabels()) ;
				currentVariableSelection = lsd.rankVariables( 1 , 5) ;
				
				if (currentVariableSelection.size()<=1){
					currentVariableSelection = getRandomVarSelection(8);
				}
			}
			// ................................................................
			 
			// if there are >70 variables we try to find sets of collinear variables
			// basically, we may apply Spearman or K++ clustering
			// the resulting exclusions will be stored into the structured list "inProcessExclusions" (="IndexedDistances{}")
			// this list will be "cleared" after the first step in the L2 loop, such that the 
			// removed variable gets the same weight as the variable which has been used as prototype
			
			// detectCollinearVariables();  // TODO:  upon option
			
			
			
			// 
			currentVariableSelection = prepareInitialVariableGuess( ) ;
			
			if ((currentVariableSelection.size()<=3) && (somDataObj.variables.size()>15)){
				currentVariableSelection = (ArrayList<String>) CollectionUtils.union( currentVariableSelection, getRandomVariableSelection(3)) ;
			}
			int r = performSingleRun(loopcount, true);
			if (r<0){
				return;
			}
			somLattice = somProcess.getSomLattice();
			
			initialweights = performInitialGuessOfWeights();
			_evoBasics = new EvoBasics();
			_evoBasics.setKnownVariables( moptiParent.somDataObj.variableLabels );
			_evoBasics.setEvolutionaryWeights(initialweights) ;
			
			// we need to get the target ratio : how many cases in the data?
			// if the ratio is large enough, we need not to take all data !! (if option allows for it
			
			// in exploring the combinations, we sort along the evo weights 
// ----------------------------------------------------------------------------------------------------
			 
			while ((done==false) && (somFluid.getUserbreak()==false)){
				
				// after any sprite+evo optimization, a further pair of sprite+evo optimization may yield even better results 
				if (modelingSettings.getMaxL2LoopCount()>0){
					
											// out.print(2, "variables(a1) n = "+somDataObj.variables.size()	);
					if ((modelingSettings.getEvolutionaryAssignateSelection() ) && (somFluid.getUserbreak()==false)){
						
						if ((loopcount==0) && (resumeMode>=1)){

							// we load a simple som from archive and run it once
							somscreener = new SomScreening( moptiParent,optimizerProcIndex );
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

						somScreening = new SomScreening(moptiParent,optimizerProcIndex);
						try {
							if (variablesPartition.size()>1){ somScreening.setVariablesPartition(variablesPartition); }
							somScreening.setInitialVariableSelection(currentVariableSelection, true);

						} catch (Exception e) {
							e.printStackTrace();
							break;
						}
						
						somScreening.setFinalRefinement(finalRefinement) ;
						 
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
						
						try{
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (a) : "+vn);						
							somScreening.startScreening(1,loopcount);
						
						} catch (Exception e) {
							e.printStackTrace();
							break;
						}								
						// first getting it, we have to check whether the new results are better than the last one 
											out.print(2, "SomScreening has been finished, re-establishing the best of the evaluated models...");
											
						_mozResults = restoreModelFromHistory( somScreening,  -1 ) ; 
						somLattice = somProcess.getSomLattice() ;
						                    out.print(2, "somLattice = "+somLattice.toString()) ;
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
					
					// --------------------------------------------------------------------------------------------------
					
					int s1,s2 ;
					boolean hb = ( (modelingSettings.getMaxL2LoopCount()> loopcount ) && (modelingSettings.getSpriteAssignateDerivation() )&& (somFluid.getUserbreak()==false));
					 
					if ((hb)  ){ // ...AND HERE && (_mozResults != null )){
						// not in the last one of the L2-loops, such we always have (n-1) sprites for (n) evo opti
						s1 = evoBasics.getEvolutionaryWeights().size() ;
											// vn = somDataObj.variableLabels.size() ; 
											// out.print(4, "somDataObj, size of variableLabels (c) : "+vn);
						
						dependencyCheck = new SomSprite( somDataObj, somTransformer, sfProperties);
						
						SomMapTable smt = somLattice.exportSomMapTable(1);

						// is smt well-defined here ?? == somLattice NOT cleared ??
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
							breakLoop=true;;
						}
						
						// send candidates into SomTransformer, they will be put just to a queue, 
						// but NOTHING will be changed regarding the transformations...  
						// implementation will be triggered by instances of SomHostIntf (such like ModelOptimizer)
						// perceiveCandidateTransformations(candidates) ;
						if ((candidatesOK) && (breakLoop==false)){
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
						breakLoop=true;
						
					} // getSpriteAssignateDerivation() ?
					
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (f) : "+vn);

					
					_mozResults = null ; // -> checking whether evocounts are taken in/from previous run ?
					
					//
					if (breakLoop==false){
						// only if we will repeat, we should clear that... we need it subsequently to the L2 loop!
						somScreening.close();
						somScreening = null;
						System.gc() ;
					}
					
											vn = somDataObj.variableLabels.size() ; 
											out.print(4, "somDataObj, size of variableLabels (g) : "+vn);
				    
					
				} // getMaxL2LoopCount > 0?
				
// ----------------------------------------------------------------------------------------------------
				
				if (breakLoop){
					done=true;
				}
				loopcount++;
				
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
				
				
				
				if ( (modelingSettings.getOptimizerSettings().isBalancedEvolutionaryExploration()) && 
					 (caredForEvoBalance==false) &&
					 (loopcount>1) && (loopcount == modelingSettings.getMaxL2LoopCount()-1)){
					done = false;
					caredForEvoBalance = true;
					// explicitly care for balanced evolutionary counts, such that min count >= 8
					// we already have a object for this that is working within process: "TaskRatedPressure"
					// here we call an explicit strategy
				}
				// enforcing the exploration may lead to metrices with malicious variables, i.e. a negative predictive contribution
				// these we have to check
				if ((done) || (loopcount >= modelingSettings.getMaxL2LoopCount()-2 )){
					
					modelDescription = new SomModelDescription(moptiParent);
					modelDescription.setInitialVariableSelection(currentVariableSelection);
					modelDescription.calculate(1);

					ArrayList<String> removals = modelDescription.getNegativeContributionVar();
	 
	 
					if ((removals.size() > 0) && (contributionCheckings <= 2)) {
						contributionCheckings++;
						int szb = currentVariableSelection.size();
						currentVariableSelection = (ArrayList<String>) CollectionUtils.subtract( currentVariableSelection, removals);
								
						if (szb > currentVariableSelection.size()) {
							done = false;
							finalRefinement = true; 
						}
					}
				}
			}// main loop -> done ?
			// ................................................................
			if (modelDescription!=null){
				modelDescription.clear(); modelDescription=null;
			}
			
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
					//  
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
			
			evoMetrices = _evoMetrices; // ???
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
			
			
			/*
			 * Now, if we export the model, we should determine some important use parameters for the model,
			 * and those should be saved into a section in som.xml
			 * These parameters may differ from characteristics measured during modeling 
			 * 
			 * - characteristics of ppv (on nodes) -> tp rate, portion selected, post prediction risk, sensitivity, specificity  
			 *  
			 * 
			 * 
			 */
			
			
			sfTask.setSomHost(modOpti) ;
			sfTask.setCompleted(true);
			somFluid.onTaskCompleted( sfTask );
		}
	  



		private ArrayList<String> getRandomVariableSelection(int minSelSize) {
			
			String vlabel ;
			
			ArrayList<String> selection = new ArrayList<String> ();
			ArrayList<Integer> selixes = new ArrayList<Integer> ();
			Variables variables = somDataObj.variables ;
			
			ArrayList<String> notThose = variables.collectAllNonCommons();
			
			int xm =  (int)(1.4 *( 6.7739*Math.log( variables.size() ) - 15));
			xm = (int) Math.min( xm, variables.size()*0.4) ;
			
			while (selixes.size()<xm){
				int rix = (int) (variables.size() * sampler.getNextUniformRandom());
				Variable v = variables.getItem(rix) ;
				vlabel = v.getLabel() ;
				
				if ((v.isTV() ) || ((notThose.indexOf(vlabel)<0) && (v.isIndexcandidate()==false))){
					selixes.add(rix) ;
				}
			}
			
			Collections.sort(selixes);
			
			for (int i=0;i<selixes.size();i++){
				vlabel = variables.getItem(i).getLabel() ;
				selection.add(vlabel);
			}
			
			return selection;
		}

		
		
		private ArrayList<String> getRandomVarSelection( int varcount) {
			
			ArrayList<String> selection = new ArrayList<String>();
			Variables variables = moptiParent.somDataObj.getVariables() ;
			String vlabel;
			
			ArrayList<String> nonCommonVars = variables.collectAllNonCommons();
			
			int n =  variables.size() - nonCommonVars.size() ;
			double rv,pr = (1.0*(double)varcount)/((double)n) ;
			
			boolean selected=false;
			
			while (selected==false){
				for (int i=1;i<n;i++){
					vlabel = variables.getItem(i).getLabel() ;
					if (nonCommonVars.indexOf(vlabel)<0){
						
						rv = sampler.getNextUniformRandom();
						if (rv<=pr){
							if (selection.indexOf(vlabel)<0){
								selection.add(vlabel);
							}
						}
						if ((selection.size()>=varcount) || (selection.size()==variables.size()-1)){
							selected=true;
							break ;
						}
					}
				}
				
			}
			
			return selection;
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
											out.print(2, "prepare initial guess for variable selection ...");
				preSelectMode = 2;
				
				ixds = new IndexedDistances() ;
				variables = somDataObj.variables ;
				
				int recCount = somDataObj.normalizedSomData.getColumn(0).getCellValues().size() ;
				if (recCount<=3){
					return currentVariableSelection;
				}
				// we need to run the SOM once in order to get the MapTable...
				// we select all variables except black ones... 
				//       	alternatively we could perform the PCA on the raw data, then we 
				// 			would create a SomMapTable from the norm DataTable
				
				currentVariableSelection = variables.getLabelsForVariablesList(variables,true); 
											// true: keep only the applicable ones by means of "openForInspection()"
				currentVariableSelection = variables.cleanListByinProcessExclusions(currentVariableSelection) ;
				
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
				int xm =  4 + (int)(1.4 * 6.7739*Math.log( variables.size() ) - 15);
				xm = (int) Math.min(Math.min( xm, variables.size()*0.4),21) ;

				if (currentVariableSelection.size()>xm){
					currentVariableSelection = ArrUtilities.pickRandomSelection( currentVariableSelection, xm);
				}

				if (preSelectMode==1){
					//limit to max 21 variables by random
					performSingleRun( 0, false); // false: no collecting of result
					// we could apply biased sampling for faster execution here 
				
					somLattice = somProcess.getSomLattice() ;
					somMapTable = somLattice.exportSomMapTable() ;
					_somscreener = new SomScreening( moptiParent, optimizerProcIndex );
					
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
					
					// is there a whitelist ?
					
					currentVariableSelection.clear() ;  
					currentVariableSelection.addAll(previousVariableSelection) ;
				}
				
				if (preSelectMode==2){
					//  we create a table in SomMapTable-format from the table (applying some sampling)
					// then selection2 = principalComponents(somMapTable) ;
					try{

						somMapTable = somDataObj.extractSimpleTable( 0.25, 500, true) ;
																	// true: remove any id, tv, black listed !		
						if (somMapTable.values.length>1){

							_somscreener = new SomScreening( moptiParent, optimizerProcIndex );
							ArrayList<Integer> varindexes = _somscreener.principalComponents( somMapTable);
							// these varindexes are NOT referring to variables !!! we have to translate them from "smt" to "variables"
							varindexes = somMapTable.getTranslatedIndexValues( variables.getLabelsForVariablesList(variables), varindexes) ;
							
							// additionally to the PCA, we calculate a correlation matrix, selecting 

							ArrayList<Double> weights = performInitialGuessOfWeights();
							
							ixds = new IndexedDistances (); 
							for (int i=0;i<somDataObj.variables.size();i++){
								ixds.add( new IndexDistance( i,weights.get(i), somDataObj.variables.getItem(i).getLabel()) ) ;	
							}
							
							ixds.sort(-1) ; // sorting along the weights, largest first
							while (ixds.getItem(0).getDistance()>0.98 ){
								ixds.removeItem(0) ;
							}
							while (ixds.size()>10 ){
								ixds.removeItem(10) ;
							}
							
							ixds.sort( 2, 1); // sorting along the index, smallest first
							
							for (int i=0;i<5;i++){
								String str = ixds.getItem(i).getGuidStr() ;
								vix = ixds.getItem(i).getIndex() ;
								if (varindexes.indexOf(vix)<0){
									varindexes.add( vix) ;
								}
							}
							
							Collections.sort(varindexes) ;
							
							// is there a whitelist ?
							if (varindexes.size()>=3){
								currentVariableSelection.clear() ;  
								currentVariableSelection = variables.getLabelsForIndexList(varindexes);
								
							}
							
						}

					}catch(Exception e){
						currentVariableSelection.clear();
					}
				}
				
				if (preSelectMode==3){
					
				}
			}
			
			previousVariableSelection.clear();
			
			return currentVariableSelection;
		}
		
		
		private int performSingleRun(int index, boolean collectresults){
			
		// the same "package" is running in SomScreening 
			SomFluidTask _task = new SomFluidTask(sfTask);
			_task.setNoHostInforming(true);
			
			SimpleSingleModel simo ;
			ModelProperties somResults;
			
			_task.setCounter(index);
			
			simo = new SimpleSingleModel(somFluid , _task, sfFactory );
			
			simo.setDataObject(somDataObj);

									double[] vv = somDataObj.normalizedSomData.getRowValues(10) ;
									int n=vv.length;
			simo.setInitialVariableSelection( currentVariableSelection  ) ;
			
			simo.perform();
			
			somProcess = simo.somProcess ;
			
			if (collectresults){
				somResults = simo.getSomResults();
				if (somResults!=null){
					somResults.setIndex(index);
				}
			}
			// simo.somProcess.getSomLattice();
			return simo.getLastState() ;
		}
		
		@Override
		public void run() {
			 
			try {
				
				optimizeOnVariableSubset();
				
				
			} catch (Exception e) {
				out.printErr(2, e.getMessage() ) ;
				e.printStackTrace();
			}
			
		}
		
		 
	} // ----------------------------------------------------------------------
	
	
	private void singleRun(int z) throws Exception{
		
			
			long serialID=0;
			serialID = SerialGuid.numericalValue();
			
			SomTargetedModeling targetedModeling;
			
			sfTask.setCallerStatus(0) ;
			sfTask.setCounter(z) ;
			
			try{
			

				targetedModeling = new SomTargetedModeling( modOpti, sfFactory, sfProperties, sfTask, serialID);
				
				targetedModeling.setSource(0);
				
				targetedModeling.prepare(usedVariables);
				
				String guid = targetedModeling.perform(0);
				
				out.print(2, "\nSom ("+z+") is running , identifier: "+guid) ; 

				while (targetedModeling.isCompleted()==false){
					out.delay(10);
				}

				targetedModeling.clear() ;
				
			}catch(Exception e){
				// restart option ...
				if (out.getPrintlevel()>=1){
					e.printStackTrace();
				}
			}
			
			targetedModeling = null;
	}

	

	private ModelProperties restoreModelFromHistory( SomScreening somScreening, 
													 int bestHistoryIndex) {
		
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
		
		somProcess = simo.getSomProcess();
											out.print(2, "somLattice(0) = "+simo.getSomProcess().getSomLattice().toString()) ;
		restoredResults = simo.getSomResults() ;
		
		restoredResults.setVariableSelection( varSelection ) ;
		
		// save temporarily
		
		saveSingleSom(simo) ;
		
		//
		return restoredResults;
	
	}

	



	
	

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



	public SomModelDescription getModelDescription() {
		return modelDescription;
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



	public void test() {
		int z;
		// here we could run them in parallel if we would have several lattices
		for (int i = 0; i < 100; i++) {
			out.printErr(2, "------------ memory status change (step " + i + ") : " + 
					        Memory.observe()+ "  still free : " + 
					        Memory.currentFreeMemory(1));
			try {
				
				singleRun(i);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			if ((i > 20) || (i % 10 == 0)) {
				z = 0;
			}
		}
	}



	@Override
	public void selectionEventRouter(SurroundResults results, VirtualLattice somLattice) {
		 
		somLattice.handlingRoutedSelectionEvent(results);
	}



	@Override
	public void addStreamingData(DataTable dataTable) {
		// TODO Auto-generated method stub
		
	}

	
	
}

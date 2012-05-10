package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.*;


import org.apache.commons.collections.CollectionUtils;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;

import org.NooLab.somfluid.*;
import org.NooLab.somfluid.properties.*;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.util.*;
import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.engines.det.*;
import org.NooLab.somfluid.core.engines.det.results.*;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;



/**
 * 
 * 
 *
 */
public class EvoMetrices implements Serializable{

	private static final long serialVersionUID = -705859747441069918L;

	public static final int _SORT_SCORE = 1;
	public static final int _SORT_INDEX = 2;
	public static final int _SORT_SIZE  = 3;


	// transient DSom dSom;
	transient SomHostIntf somHost ;

	SomFluidFactory sfFactory;
	SomDataObject somData  ;

	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings;
	
	transient OutputSettings outsettings ; 
	transient PowerSetSpringSource powerset;
	
	int tvIndex = -1;
	
	ArrayList<EvoMetrik> evmItems = new ArrayList<EvoMetrik>();  
	transient EvoBasics  evoBasics;
	
	/** simply the best... will be restored after completion of the evo-devo */
	EvoMetrik bestResult;
	
	/** this is taken from an intermediate result following the large change  */
	EvoMetrik currentBaseMetric;
	int currentBaseMetricIndex = -1;
	
	// TODO  logging combinations that we already have seen, as a int[] for fast match
	//       in order to avoid repetitions

	int largePeriod;        
	
	// in...
	ArrayList<Integer> suggestedVarIxes = new ArrayList<Integer>();
	ArrayList<Integer> urgingVarIxes    = new ArrayList<Integer>();
	
	// out...
	ArrayList<Integer> proposedVariableIndexes = new ArrayList<Integer>();
	
	IndexedDistances topSortedVariables = new IndexedDistances();
	
	ArrayList<ArrayList<Integer>> exploredMetrices = new ArrayList<ArrayList<Integer>>();
	
	MetricsHistory  emHistory;
	
	Random jrandom;
	transient PrintLog out; 
	transient ArrUtilities arrutil = new ArrUtilities();
	
	// ==========================================================================
	public EvoMetrices( SomHostIntf somhost , int largeperiod){
	 
		somHost = somhost;
		sfFactory = somHost.getSfFactory() ;
		somData  = somHost.getSomDataObj() ;


		modelingSettings = somHost.getSfProperties().getModelingSettings() ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		outsettings = somHost.getSfProperties().getOutputSettings() ;
		
		evoBasics = new EvoBasics();
		bestResult = new EvoMetrik() ;
		
		jrandom = sfFactory.getRandom() ;
		jrandom.setSeed(3577) ;
		int d = jrandom.nextInt(50);
		
		int n;
		n = Math.max(2, somData.getNormalizedDataTable().colcount()-3);
		int preferredLimit = (int) (n*0.7) ;
		if (preferredLimit>40)preferredLimit=40;
		
		powerset = new PowerSetSpringSource( somData ) ; 
		powerset.setBlacklistedVarLabels(somData.getVariables().getBlacklistLabels()) ;
		
		largePeriod = largeperiod;
		
		
		
		out = somData.getOut() ;
	}
	
	/**
	 * for cloning
	 * 
	 * @param templEvoMetrices
	 */
	public EvoMetrices(EvoMetrices templEvoMetrices, boolean inclObjects) {
		
		
		somHost = templEvoMetrices.somHost;
		sfFactory = somHost.getSfFactory() ;
		somData  = somHost.getSomDataObj() ;
		
		jrandom =  sfFactory.getRandom() ;
		
		modelingSettings = somHost.getSfProperties().getModelingSettings() ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		
		evoBasics = new EvoBasics( templEvoMetrices.evoBasics );
		if (templEvoMetrices.evmItems.size() > 0) {
			
			this.evmItems.addAll( templEvoMetrices.evmItems ) ;
			if (templEvoMetrices.bestResult == null) {
				// get it
				bestResult = determineBestResult(templEvoMetrices);
				if (bestResult == null) {
					if (templEvoMetrices != null) {

					}
				}
			} else {
				if (templEvoMetrices.bestResult != null) {
					if (templEvoMetrices.bestResult.usageVector == null) {
						int k;
						bestResult = determineBestResult(templEvoMetrices);
					} else {
						bestResult = new EvoMetrik(templEvoMetrices.bestResult);
					}
				}
			}
		} // anything in the list at all ?
		else{
			int k;
			k=0;
		}
		if (inclObjects){
			powerset = new PowerSetSpringSource( somData ) ; 
			powerset.setBlacklistedVarLabels(somData.getVariables().getBlacklistLabels()) ;
		}
		
		exploredMetrices = new ArrayList<ArrayList<Integer>>();
		exploredMetrices.addAll( templEvoMetrices.getExploredMetrices() );
		
		topSortedVariables = new IndexedDistances();
		  
		
		largePeriod = templEvoMetrices.largePeriod;
		out = somData.getOut() ;
		
	}

	
	public EvoMetrik determineBestResult(EvoMetrices metrices) {
		EvoMetrik em,topResult = null;
		double bestScore = 999.0; 
		
		
		for (int i=0;i<metrices.evmItems.size();i++){
			em = metrices.evmItems.get(i);
			
			if (bestScore > em.actualScore){
				bestScore = em.actualScore;
				topResult = em;
			}
		}
		
		if (topResult!=null){
			topResult = new EvoMetrik(topResult) ;
		}
		return topResult;
	}

	// ==========================================================================


	public void close() {
		
		try{
			evoBasics.evolutionaryCounts.clear();
			evoBasics.evolutionaryWeights.clear() ;
			evoBasics.knownVariables.clear() ;
			if (powerset!=null){
				powerset.close();
			}
			powerset = null;
			
			exploredMetrices.clear() ;
			evmItems.clear() ;
			suggestedVarIxes.clear() ;
			proposedVariableIndexes.clear() ;
			
		}catch(Exception e){
			// lazy silence ...
		}
	}

	public void reset() {
		for (int i=0;i<evoBasics.evolutionaryCounts.size();i++){
			evoBasics.evolutionaryCounts.set(i, 0);
		}
		for (int i=0;i<evoBasics.evolutionaryWeights.size();i++){
			evoBasics.evolutionaryWeights.set(i, 0.5);
		}
		evmItems.clear();
	}

	public int size() {
		return evmItems.size() ;
	}

	/**
	 * this method registers 
	 *  - "model" = use vector, 
	 *  - ecr, 
	 *  - quality ( modelProperties: type-I/II errors (also for multigroups!), regression) 
	 *              modelProperties refers itself to an interface tht describes the cost-function !
	 *  ....
	 *  subsequent to registration, compares the last model to the previous ones
	 *  - first
	 *  - best
	 *  - most similar (if present, 1 or 2 positions different only)
	 *  - top 5
	 *  
	 *  and finally adjusts the evolutionary weights, that in turn will influence the selection of new variables
	 *  
	 *  variables carry a copy of the evoweight (which allows integration of several optimizer processes!!!)) 
	 *    accomplished in SomScreening.updateVariablesCoreWeight()
	 *    
	 *  - evo weight
	 *  - evo count
	 *  - an "optimizer selection score" which is composed by evo weight, evo count
	 *  
	 *  the zone between 0.42 and 0.58 is a zone of indifference
	 *  with a small probability we select variables additionally from this set for large changes
	 * 
	 *  
	 *  if there are many models calculated, we can characterize the population, determine the pareto frontier
	 *  and create some "cross-overs"
	 * @return 
	 *  
	 *   
	 */
	public boolean registerResults( int z, 
								    ModelProperties modelProperties,
									ArrayList<Double> usevector , 
									int selectionMode ) { // small(2) or large(1)
		
		
		// SomTargetedModeling targetMod, 
		// retrieves results and calculates the quality score
		boolean improvement=false;
		double scoreDifference = 100.0 ;
		
		SomQuality sq;
		EvoMetrik evoResultItem;
		
		if (out==null)out = somData.getOut() ;
		
		evoResultItem = new EvoMetrik();
		
		// always the same ??? somHost.getSomProcess() 
		sq = new SomQuality( somHost.getSomProcess(), modelProperties , usevector); // here, sqdata is created and calculated
		sq.acquireResultValues() ;
		
		evoResultItem.index = z;
		evoResultItem.step  = evmItems.size()+1 ;
		
		evoResultItem.sqData = sq.somQualityData ;
		evoResultItem.usageVector = new ArrayList<Double>(usevector) ;
		// we should save it in compressed form also ...
		evoResultItem.varIndexes = new ArrayList<Integer>(determineActiveIndexes(usevector) );
		evoResultItem.mainScore = sq.somQualityData.score ;
		evoResultItem.actualScore = sq.somQualityData.score ;
		
		evmItems.add(evoResultItem) ;
		
		// comparison only for z>1
		if (z>1){
			// compare sq to bestSomQuality
			scoreDifference = sq.somQualityData.score - bestResult.sqData.score ;
			improvement = scoreDifference<0;
											out.printErr(1, "model score: "+String.format( "%.3f", sq.somQualityData.score)+", RoC-AuC: "+String.format( "%.3f", sq.somQualityData.rocAuC));
		     								
			// updateEvoBasicsData( scoreDifference ,bestResult.usageVector , evoResultItem.usageVector,1.0); 
			// THIS IS EMPTY
			
		    if (( improvement) && (scoreDifference!=0) && (sq.somQualityData.score !=0) && (bestResult.sqData.score!=0)){
				bestResult.sqData      = new SomQualityData( evoResultItem.sqData );
				bestResult.usageVector = new ArrayList<Double>(evoResultItem.usageVector) ;
				bestResult.varIndexes  = new ArrayList<Integer>(evoResultItem.varIndexes) ;
				bestResult.index       = evoResultItem.index ;
			}else{
				improvement=false;
			}
			
			// also changes to last
		    if (selectionMode>=2){
		    	if ((currentBaseMetric!=null) && (currentBaseMetric.sqData!=null)){
		    		scoreDifference = sq.somQualityData.score -  currentBaseMetric.sqData.score ;
		    		// updateEvoBasicsData( scoreDifference ,currentBaseMetrik.usageVector , evoResultItem.usageVector, 0.3);
		    	}
		    }
		     
		}else{
			 
			bestResult.sqData = new SomQualityData( evoResultItem.sqData );
			bestResult.usageVector = new ArrayList<Double>(evoResultItem.usageVector) ;
			bestResult.varIndexes = new ArrayList<Integer>(evoResultItem.varIndexes) ;
			bestResult.index = evoResultItem.index ;
											out.printErr(2, "first model score: "+String.format( "%.3f", bestResult.sqData.score));
			initializeEvoBasicsData();
		}
		 
		return improvement;
	}
	

	public void registerMetricAsExplored(ArrayList<Double> uv) {
		ArrayList<Integer> indexes ;
		
		indexes = determineActiveIndexes(uv) ;
		
		exploredMetrices.add(indexes);
		
		if (outsettings==null){
			outsettings = somHost.getSfProperties().getOutputSettings() ;
		}

	}

	
	public ArrayList<Integer> determineActiveIndexes(ArrayList<Double> usevector){
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for (int i=0;i<usevector.size();i++){
			
			if (usevector.get(i)>0){
				indexes.add(i);
			}
		}
		
		return indexes ;
	}
	
	/**
	 * 
	 *  here we have two lists that are indicating the change in variable selction for the very last 
	 *  variable selection "metric";
	 * 
	 *  We check the change of the quality (score) from the pre-last to the last metric, and translate this
	 *  into changes in the evoweight
	 * 
	 *  the changes of evoweight are scaled by the number of variables changed, and then
	 *  expressed as a relative value, which additionally is sclaed down with respect to the distance to 1
	 *  (such that "1" is approached asymptotically)
	 *  
	 *  the sequence of calls is such:
	 *  
	 *  av = determineAddedVariables( cuv, uv);
	 *  rv = determineRemovedVariables( cuv, uv);
	 *  targetMod = calculateSpecifiedModel( uv );
	 *  evoMetrices.registerResults( z, dSom, uv) ;
	 *  >>>> evoMetrices.registerMetricChangeEffects(av, rv);
	 * 
	 * @param addedVarIxes
	 * @param removedVarIxes
	 */
	public void registerMetricChangeEffects( ArrayList<Integer> addedVarIxes, ArrayList<Integer> removedVarIxes, boolean isNewBest) {
		// 
		int ne,totalchanges = 0, ix, actionIndicator, ec;
		double scoreDelta = 0.0, ccs,cscore,ew,scalew,sc1=-999.09,sc2=-999.09,cntscale;
		
		totalchanges = addedVarIxes.size() + removedVarIxes.size() ;
	
		if (totalchanges==0){
			return;
		}
		
		if (totalchanges>2){
			actionIndicator = 2;
		}else{
			actionIndicator = 1;
		}
		
		ne = evmItems.size();
		
		if (ne>0){
			sc1 = evmItems.get(ne-1).sqData.score ;
		}
		if (ne>1){
			sc2 = evmItems.get(ne-2).sqData.score ;
			
			scoreDelta = 100*(sc1 - sc2)/Math.max(sc1,sc2) ;  // positive: larger == worse
		}
		
		
		// thats now a percentage value
		scoreDelta = scoreDelta/Math.max(sc1,sc2);
		
		cscore = scoreDelta/((double) totalchanges);
		if (isNewBest)cscore=cscore*2.0;
		// >0 worsening ,  <0 improving
		// now adding to the score of each of the fields, 
		// actual change is scaled dependent on the distance to 0 or 1, thus
		// the most prominent changes occur around 0.5
		
		for (int i=0;i<addedVarIxes.size();i++){
			
			ix = addedVarIxes.get(i) ;
			evoBasics.incEvolutionaryCount(ix) ;
			ec = evoBasics.evolutionaryCounts.get(ix) ;
if (ec<0){
	ec=ec+1-1;
}
			if (ec<0){
				continue;
			}
			ew = evoBasics.evolutionaryWeights.get(ix);
			
			if (scoreDelta>0) {
				scalew = ew;
			}else{
				scalew = 1- ew ;
			}
			 
			cntscale = Math.max( 1.0, (double)Math.log(1.0+ Math.max(1.0,27.0/(double)ec)))  ;
			 
			ccs = cscore *cntscale ;
			
			ccs = ccs * scalew ;
			
			
			ew = ew - ccs ;
			ew = Math.min( 1.0, ew) ;
			ew = Math.max( 0.0, ew) ;
			evoBasics.evolutionaryWeights.set(ix, ew) ;
			
			String varLabel = somData.getVariables().getItem(ix).getLabel() ;
			EvoTasks et = evoBasics.getEvoTasks();
			if (et!=null){
				et.updateEvoTaskItem(varLabel, actionIndicator ) ;
			}
		}
		
		// ------------------------------------------------
		for (int i=0;i<removedVarIxes.size();i++){
			
			ix = removedVarIxes.get(i) ;
			
			evoBasics.incEvolutionaryCount(ix) ;
			ec = evoBasics.evolutionaryCounts.get(ix) ;
			
			ew = evoBasics.evolutionaryWeights.get(ix);
			
			if (scoreDelta>0) {
				scalew =  ew;
			}else{
				scalew = 1- ew ;
			}
			
			cntscale = Math.max( 1.0, (double)Math.log(1.0+ Math.max(1.0,27.0/(double)ec)))  ;
			 
			ccs = cscore *cntscale ;
			
			ccs = cscore * scalew ;
			
			
			ew = ew - ccs ;
			ew = Math.max( 0.0, ew) ;
			ew = Math.min( 1.0, ew) ;
			evoBasics.evolutionaryWeights.set(ix, ew) ;
			
			String varLabel = somData.getVariables().getItem(ix).getLabel() ;
			if (evoBasics.getEvoTasks()!=null){
				evoBasics.getEvoTasks().updateEvoTaskItem(varLabel, -actionIndicator ) ; 
			}
		}

		if (evoBasics.getEvoTasks()!=null){
			evoBasics.getEvoTasks().renormalizeParameters();
		}
	}
	
	
	
	/**
	 * here the EvoMetrices object acquires suggestions that mostly are taken from statistical methods;
	 * yet, it may also be used by other evo-processes to exchange intermediate results.
	 * 
	 * @param suggSelection
	 */
	public void addSuggestions(ArrayList<Integer> suggSelection) {
		// 
		int n=suggSelection.size() ;
		
		suggestedVarIxes = new ArrayList<Integer> (suggSelection);
		
	}

	public void prepare() {
		prepare(-1) ;
	}
	
	public void prepare( int maxN ) {
		
		EvoMetrik em ;
		int k;
		ArrayList<String> listedM = new ArrayList<String>();
		
		
		if (outsettings==null){
			outsettings = somHost.getSfProperties().getOutputSettings() ;
		}

		try{
		
			sort(EvoMetrices._SORT_SCORE, -1);
			
			emHistory = new MetricsHistory(somHost, this) ;
			
			for (int i=0;i<evmItems.size();i++){
				
				try{
				
					em = evmItems.get(i);
					
					String str = ArrUtilities.arr2Text( em.getVarIndexes() ).trim();
					str = StringsUtil.replaceall(str, " ", ""); 
					
					if (listedM.indexOf(str)<0){
						listedM.add(str);	
						emHistory.addEvoMetrikAsItem(em) ;
					}
				}catch(Exception e){
				}
			}// i->
			
			if (maxN>3){
				// apply various filter modes, dependent on limits on count of metrices 
				
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		listedM.clear();
		k=0;
	}
	

	public String getAsXml() {
		
		return "";
	}

	
	public MetricsHistory getAsHistory() {
		
		return emHistory;
	}

	public String getStringTable( ) {
		return getStringTable( 0, new String[]{},"\t");
	}
	
	public String getStringTable( int mode, String[] indicators, String colSeparator ) {
		
		ArrayList<ArrayList<String>> rows;
		ArrayList<String> row;
		String tableStr="";
		
		setOutputColumns( indicators, emHistory);
			 
		rows = prepareMetricsHistoryOutput();
		
		// translating into a single string
		for (int i=0;i<rows.size();i++){
			row = rows.get(i) ;
			
			for (int c=0;c<row.size();c++){
				tableStr = tableStr+row.get(c);
				if (c<row.size()-1){
					tableStr = tableStr + colSeparator;
				}
			}
			if (i<rows.size()-1){
				tableStr = tableStr + "\n";
			}
		}
		
		return tableStr;
	}
	

	

	private ArrayList<ArrayList<String>> prepareMetricsHistoryOutput(){
		
		
		// creating the header as list of strings
		ArrayList<String> headers = emHistory.createHeaderRow();
		
		// creating the body of table, as list of (list of strings)
		ArrayList<ArrayList<String>> trows = emHistory.createTableRows( emHistory, headers ) ;
		
		// inserting header
		trows.add(0,headers) ;
		
		//
		return trows;
	}
	
	
	private void setOutputColumns( String[] listOfFieds, MetricsHistory emHistory){
		
		if (outsettings==null){
			outsettings = somHost.getSfProperties().getOutputSettings() ;
		}
		// just a lookup for the programmer
		// ArrayList<String> _tmp_allfields = emHistory.getAllFieldLabels();    
		
		// TODO take this from the output properties
		// Map<String,Integer> outdef = new HashMap<String,Integer>();
		// emHistory.setOutputColumns( outdef ) ; not functional yet
		outsettings.resetOutputDefinition();
		
		if ((listOfFieds==null) || (listOfFieds.length==0)){
			outsettings.setOutputColumn("index", 1);
			outsettings.setOutputColumn("step", 2);
			outsettings.setOutputColumn("score", 3);
			outsettings.setOutputColumn("tprate", 4);
			outsettings.setOutputColumn("fprate", 5);
			outsettings.setOutputColumn("ppv", 6);
			outsettings.setOutputColumn("rocauc", 7);
			outsettings.setOutputColumn("rocstp", 8);
			outsettings.setOutputColumn("variableindexes", 9);

		}else{
			for (int i=0;i<listOfFieds.length;i++){
				outsettings.setOutputColumn(listOfFieds[i], i+1);
			}
		}
	}
	
	private void initializeEvoBasicsData(){
		int n,z;
		double v;
		
		n = bestResult.usageVector.size() ;
			
		if (evoBasics.evolutionaryWeights.size()< n){
			z=0;
			while (evoBasics.evolutionaryWeights.size()< n){
				
				if (z+1> evoBasics.evolutionaryWeights.size()){
					v=0;
					if (bestResult.usageVector.get(z)>0.0){
						v=0.53;
					}
					evoBasics.evolutionaryWeights.add( v ) ;
					evoBasics.evolutionaryCounts.add(0) ;
				}
				
				z++;
			} // ->
		} // size ?

	}
	

	@SuppressWarnings("unused")
	private void updateEvoBasicsData( double scoreDifference,
									  ArrayList<Double> referUsageVector, 
									  ArrayList<Double> comparedVector,
									  double cscale) {
		Double rUse,cUse;
		
		// scoreDifference, evocount, cscale  
		// using intersection, subtract, 
		
		for (int i=0;i<referUsageVector.size();i++){
			
			rUse = referUsageVector.get(i);
			cUse = comparedVector.get(i) ;
			
			if ((rUse>=0) && (cUse>=0)){
				
				if ((rUse==0) && (cUse>=0)){ // added
					
				}
				if ((rUse>=0) && (cUse==0)){ // removed
					
				}
			} // both not mv, not tv?
			
		} // i-> all positions
		 

		
	}

	/**
	 * 
	 * @param mode  0=small change,  1=large change
	 * @return 
	 */
	public ArrayList<Integer> getNextVariableSelection( int z, int mode) {
		
		ArrayList<Integer> selection = new ArrayList<Integer>();
		// TODO avoid suggesting KNOWN selections !!
		
		double averageCount = (calculateEvoCountsTotalSum())/((double)evoBasics.evolutionaryCounts.size());
		averageCount = averageCount/((double)evoBasics.evolutionaryCounts.size() );
		
		boolean found=false;
		int zz=0;
		int pLimit = powerset.getAbsoluteSizeLimit();
		if (pLimit<100){
			pLimit = 10000 ;
		}
		while ((found==false) && (zz<pLimit)){
			zz++ ;
			
if (zz>1000){
	zz=zz+1-1;
}
			if (mode==3){
				prepareIndependentSelectionChange(z);
			}
			if (mode==1){
				if (z>3){
					// == the first large change after some small changes
					currentBaseMetric = bestResult;
				}
 
				prepareLargeSelectionChange(z);
				// results of this is not in global ArrayList<Integer> proposedVariableIndexes
			
			}
			if (mode==0){
				// bestSomQuality
				prepareSmallSelectionChange(z,1); // 1 = max elements changed
			}
		
			if (averageCount<0.00001){
			
			}
			// based on the selection score, which may be interpreted as a threshold for selection probability
			
			found = proposedMetricIsUnexplored( proposedVariableIndexes) && (proposedVariableIndexes.size()>=2);
		}
		
		suggestedVarIxes.clear();
		proposedVariableIndexes.trimToSize();
		return (proposedVariableIndexes);
	}
	
	
	
	@SuppressWarnings("rawtypes")
	private boolean proposedMetricIsUnexplored( ArrayList<Integer> proposedVariables) {
		boolean rB=true;
		ArrayList<Integer> metric;
		Collection disJunction;
		
		if (exploredMetrices==null){
			return rB;
		}
		for (int i=0;i<exploredMetrices.size();i++){
			metric = exploredMetrices.get(i) ;
			disJunction = CollectionUtils.disjunction(metric, proposedVariables) ;
			if (disJunction.size()==0){
				rB=false;
				break;
			}
		}
		
		return rB;
	}
	@SuppressWarnings("unchecked")
	private void prepareSmallSelectionChange( int z,int vChgCount) {
		double uv ,sprob; 
		int n,vn, suggix;
		
		String vlabel = "";
		 
		// is the new metric completely contained in the best one?
		ArrayList<Integer> proposedVarIxes = new ArrayList<Integer>();
		ArrayList<String> setItems = new ArrayList<String>();
		ArrayList<String> setItems2;
		ArrayList<Double> uvec ; 
		ArrayList<Double> selectionProbabilities = new ArrayList<Double>();
		
		
		try{
		
			// TODO: also asking variables for participation
			/*
			 *  we already have a selection "suggestedVarIxes", which is derived from
			 *  statistical tests.
			 *  
			 *  for these, we increase the selection probability by 20%
			 *  
			 *  This query is organized by the following class "TaskRatedPressure", which
			 *  works on the list EvoBasics.evoTasks  ArrayList<EvoTaskOfVariable>
			 */
			
			TaskRatedPressure trp = new TaskRatedPressure( somData , optimizerSettings , evoBasics) ;  
			trp.determineUrgingVariables();
			
			urgingVarIxes = trp.getSuggestions(2);
			// if (urgingVarIxes.size()>0)suggestedVarIxes.addAll(urgingVarIxes) ;
			
			
			/* now we have the best metric and a list of suggested variables
			   additionally, we have the evolutionary weights;
			   
			   all three sets we will now reflect in the selection probability, starting with evoweights
			
			   powerset.setSelectionProbability( "A",0.62 ); // for one individually
			   
			*/
			
			Variables variables = somData.getVariables();
			n = bestResult.usageVector.size() ;
			vn = variables.size();
			
			if (n>vn){
				out.print(2,"vector sizes, var count ?");
			}
			
			selectionProbabilities.addAll( evoBasics.evolutionaryWeights );
			if (n>selectionProbabilities.size()){
				selectionProbabilities.add(0.5);
			}
			if (n>evoBasics.evolutionaryWeights.size()){
				evoBasics.evolutionaryWeights.add(0.5);
			}

			{
				// translate it into index values
				for (int i = 0; i < n; i++) {
					uv = bestResult.usageVector.get(i);
					if (uv > 0) {
						
						sprob = selectionProbabilities.get(i);
						sprob = sprob + (1.0 - sprob) * 0.3;
						selectionProbabilities.set(i, sprob);
						vlabel = variables.getItem(i).getLabel();
						setItems.add(vlabel);
					}
				}
			}
			
			 
			n = selectionProbabilities.size();
			if (n>vn){
				selectionProbabilities.remove(selectionProbabilities.size()-1);
			}
			for (int i=0; i<n;i++ ){
				vlabel = variables.getItem(i).getLabel() ;
				powerset.setSelectionProbability( vlabel, selectionProbabilities.get(i) );
			}
			
			int lo,hi ;
			lo=2;
			hi=1;
			// largePeriod
			if ( z%3==0){
				lo=2;
				hi=0;
			}
			// everything is the almost same ??? as in large changes until here for small changes
			
			setItems2 = powerset.getNextSimilar( setItems, lo,hi ); //????
			
			double v = (double)(z-1.0)/((double)largePeriod)-1.0 ;
			if ((z==2) || (v<0.000001)){
				// remove the field with lowest evoweight  
				
				 
				if (setItems2.size()>=4){
					setItems2 = removeLowestEvoWeightPosition(setItems2);
					if (jrandom.nextDouble()>0.7){
						setItems2 = addBestEvoWeightPosition(setItems2);
					}
				}else{
					setItems2 = addBestEvoWeightPosition(setItems2);
				}
				
			}
			
			proposedVarIxes = variables.getIndexesForLabelsList( setItems2 );
			Collections.sort( proposedVarIxes );
			
			uvec = deriveUseVector( proposedVarIxes ) ;
			currentBaseMetric = evmItems.get(evmItems.size()-1);
			currentBaseMetric.usageVector = uvec ; 
			currentBaseMetricIndex = evmItems.size()-1 ;
			
		}catch(Exception e){
			String str= "vlabel " + vlabel;
			out.print(2, str) ;
			e.printStackTrace() ;
		}
				
		
		proposedVariableIndexes = proposedVarIxes ;
		
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<String> removeLowestEvoWeightPosition( ArrayList<String> setItems) {
		
		Variables variables = somData.getVariables();
		ArrayList<Integer> xxList,indexList;
		indexList = variables.getIndexesForLabelsList(setItems);
		int rn=0;

		if (setItems.size() >= 4) rn = 1;
		if (setItems.size() >= 6) rn = 2;
		if (setItems.size() >= 21)rn = 3;

		if (rn>0){
			
			xxList = getExtremeEvoWeightVarIxes(indexList, rn, -1);

			if (xxList.size() > 0) {
				indexList = (ArrayList<Integer>) CollectionUtils.subtract( indexList, xxList);
				if (indexList.size()>=3){
					setItems = translateVariableIndexesToStrings(indexList);
				}
			}
			
		}

		return setItems;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<String> addBestEvoWeightPosition( ArrayList<String> setItems) {
		
		Variables variables = somData.getVariables();
		ArrayList<Integer> xxList,indexList;
		indexList = variables.getIndexesForLabelsList(setItems);
		int rn=0;

		if (setItems.size() >= 4) rn = 1;
		if (setItems.size() >= 6) rn = 2;
		if (setItems.size() >= 21)rn = 3;

		if (rn>0){
			
			xxList = getExtremeEvoWeightVarIxes(indexList, rn, 2);

			if (xxList.size() > 0) {
				indexList = (ArrayList<Integer>) CollectionUtils.union( indexList, xxList);
				setItems = translateVariableIndexesToStrings(indexList);
			}
			
		}

		return setItems;
	}
	
	
	
	@SuppressWarnings("unused")
	private void _obs_prepareSmallSelectionChange( int z,int vChgCount) {
		
		double uv ; 
		int n,d,k;
											out.print(2, "prepareSmallSelectionChange(1) ...  ");
		ArrayList<Integer> proposedVarIxes = new ArrayList<Integer>();
		ArrayList<Integer> baseRVarIxes = new ArrayList<Integer>();

		ArrayList<Integer> aCandidates, rCandidates ;
		
		TaskRatedPressure trp = new TaskRatedPressure( somData , optimizerSettings , evoBasics) ;  
		trp.setpreferrables( suggestedVarIxes );
		trp.determineUrgingVariables();
		
											out.print(2, "prepareSmallSelectionChange(2) ...  ");
		urgingVarIxes = trp.getSuggestions(2);

			
		// n = bestResult.usageVector.size() ;
		if ((currentBaseMetric==null) || (currentBaseMetric.usageVector.size()<=2)){
			currentBaseMetric = bestResult; 
		}
		n = currentBaseMetric.usageVector.size() ;
		
		// translate it into index values
		for (int i=0; i<n;i++ ){
			uv = currentBaseMetric.usageVector.get(i) ;
			if (uv>0){
				baseRVarIxes.add(i);
			} ;
		}
		
											out.print(2, "prepareSmallSelectionChange(3) ...  ");
		proposedVarIxes.addAll(baseRVarIxes) ;
		
		// sth removing or adding, dependent on probability...
		// here: only removing 1 or 2 fields
if (z==8){
	z=8;
}
		int st=0; 
		boolean selectionIsFirstTime = false;
		
		while (selectionIsFirstTime==false){
			
											out.print(2, "prepareSmallSelectionChange(4) ...  ");
			// TODO from all available, we select also from urgingVarIxes
											
			if (proposedVarIxes.size()>3){
				randomSubSelectionOfIndexes( proposedVarIxes, proposedVarIxes.size()-1, tvIndex) ;
				
				// based on prob., remove a further one
				if (jrandom.nextDouble()>0.9){
					randomSubSelectionOfIndexes( proposedVarIxes, proposedVarIxes.size()-1, tvIndex) ;
				}
			}
			
			
			
			selectionIsFirstTime = selectionIsFirstTime( proposedVarIxes );
			
			if (selectionIsFirstTime==false){
				if ((st>0) && (st%2==0)){
					// add one
				}
			}
			if (st>5){
				break;
			}
			st++;
		} // ->
		
		
		
											out.print(2, "prepareSmallSelectionChange() finished.  ");
		
		proposedVariableIndexes = proposedVarIxes ;
	}
	
	
	
	private void prepareIndependentSelectionChange(int z){

		double uv, sprob;
		int n, suggix;
		boolean selectionIsFirstTime;
		String vlabel;

		// is the new metric completely contained in the best one?
		ArrayList<Integer> proposedVarIxes = new ArrayList<Integer>();
		ArrayList<String> setItems = new ArrayList<String>();
		ArrayList<String> topItems;
		ArrayList<Integer> topIxes;

		ArrayList<Double> uvec;
		ArrayList<Double> selectionProbabilities = new ArrayList<Double>();
		
		
		try {

					// TODO: also asking variables for participation
					/*
					 *  we already have a selection "suggestedVarIxes", which is derived from
					 *  statistical tests.
					 *  
					 *  for these, we increase the selection probability by 20%
					 *  
					 *  This query is organized by the following class "TaskRatedPressure", which
					 *  works on the list EvoBasics.evoTasks  ArrayList<EvoTaskOfVariable>
					 */
					
			TaskRatedPressure trp = new TaskRatedPressure(somData, optimizerSettings, evoBasics);
			trp.determineUrgingVariables();

			urgingVarIxes = trp.getSuggestions(2);
			// if (urgingVarIxes.size()>0)suggestedVarIxes.addAll(urgingVarIxes)

					
					/* now we have the best metric and a list of suggested variables
					   additionally, we have the evolutionary weights;
					   
					   all three sets we will now reflect in the selection probability, starting with evoweights
					
					   powerset.setSelectionProbability( "A",0.62 ); // for one individually
					   
					*/
					
			Variables variables = somData.getVariables();
			n = bestResult.usageVector.size();

			// translate it into index values
			for (int i = 0; i < n; i++) {
				uv = bestResult.usageVector.get(i);
				if (uv > 0) {
					
					vlabel = variables.getItem(i).getLabel();
					setItems.add(vlabel);
				}
			}
			
			//
			int lo, hi;
			lo = 0;
			hi = 4;
			
			// everything is the almost same until here for small changes
			setItems = powerset.getNextSimilar(setItems, lo, hi);
if (setItems.size()<=1){
	lo=0;
}
			proposedVarIxes = variables.getIndexesForLabelsList(setItems);

			Collections.sort(proposedVarIxes);

			uvec = deriveUseVector(proposedVarIxes);
			currentBaseMetric = evmItems.get(evmItems.size() - 1);
			currentBaseMetric.usageVector = uvec;
			currentBaseMetricIndex = evmItems.size() - 1;

		} catch (Exception e) {
			e.printStackTrace();
		}

		proposedVariableIndexes = proposedVarIxes;
	}
	
	/**
	 * given the  bestSomQuality, we change only max 62% of the metric,
	 * evolutionary weights / counts are used to determine the selectio probability
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	private void prepareLargeSelectionChange( int z){
		double uv ,sprob; 
		int n, suggix;
		boolean selectionIsFirstTime;
		String vlabel;
		 
		// is the new metric completely contained in the best one?
		ArrayList<Integer> proposedVarIxes = new ArrayList<Integer>();
		ArrayList<String> setItems = new ArrayList<String>();
		ArrayList<String> topItems ;
		ArrayList<Integer> topIxes ;
		
		ArrayList<Double> uvec ; 
		ArrayList<Double> selectionProbabilities = new ArrayList<Double>();
		
		
		try{
											if(out==null){
												out = somData.getOut() ;
											}
											out.print(3, "prepareLargeSelectionChange (1) ... ");
			
			
			// TODO: also asking variables for participation
			/*
			 *  we already have a selection "suggestedVarIxes", which is derived from
			 *  statistical tests.
			 *  
			 *  for these, we increase the selection probability by 20%
			 *  
			 *  This query is organized by the following class "TaskRatedPressure", which
			 *  works on the list EvoBasics.evoTasks  ArrayList<EvoTaskOfVariable>
			 */
			
			TaskRatedPressure trp = new TaskRatedPressure( somData , optimizerSettings , evoBasics) ;  
			trp.determineUrgingVariables();
			
			urgingVarIxes = trp.getSuggestions(2);
			// if (urgingVarIxes.size()>0)suggestedVarIxes.addAll(urgingVarIxes) ;
			
			
			/* now we have the best metric and a list of suggested variables
			   additionally, we have the evolutionary weights;
			   
			   all three sets we will now reflect in the selection probability, starting with evoweights
			
			   powerset.setSelectionProbability( "A",0.62 ); // for one individually
			   
			*/
											out.print(3, "prepareLargeSelectionChange (2) ... ");
			Variables variables = somData.getVariables();
			n = bestResult.usageVector.size() ;
			
			selectionProbabilities.addAll( evoBasics.evolutionaryWeights );
											out.print(3, "prepareLargeSelectionChange (3) ... ");
			if (evmItems.size()==1){ 
				// use the statistical suggestion
				n = suggestedVarIxes.size();
				for (int i=0; i<n;i++ ){
					suggix = suggestedVarIxes.get(i) ;
					vlabel = variables.getItem(suggix).getLabel() ;
					setItems.add(vlabel);
				}
				// TODO we need a proportionality to improvement in evoweight change !!!
				
											out.print(3, "prepareLargeSelectionChange (4) ... ");
				
				// we will have a problem with large number of variables here
				// initialization: first call triggers "prepare()"
				powerset.getNextRandom() ;
				
											out.print(3, "prepareLargeSelectionChange (5) ... ");
			} else{
				// translate it into index values
				for (int i = 0; i < n; i++) {
					uv = bestResult.usageVector.get(i);
					if (uv > 0) {
						sprob = selectionProbabilities.get(i);
						sprob = sprob + (1.0 - sprob) * 0.3; 
						selectionProbabilities.set(i, sprob);
						vlabel = variables.getItem(i).getLabel();
						setItems.add(vlabel);
					}
				}
				// lo-freq add/replace from suggestedVarIxes
				
			}
											out.print(3, "prepareLargeSelectionChange (6) ... ");
			// this indices are NOT NECESSARILY in parallel to the list of variables !!
			// we have to translate them
			n = suggestedVarIxes.size();
			for (int i=0; i<n;i++ ){
				suggix = suggestedVarIxes.get(i) ;
				if ((suggix>0) && (suggix!=variables.getIdColumnIndex() )){
					 
					sprob = selectionProbabilities.get(suggix);
					sprob = sprob + (1.0-sprob)*0.3 ;
					selectionProbabilities.set(suggix,sprob);
				} ;
			}
			n = selectionProbabilities.size();
			for (int i=0; i<n;i++ ){
				vlabel = variables.getItem(i).getLabel() ;
				powerset.setSelectionProbability( vlabel, selectionProbabilities.get(i) );
			}
			
			 
			
			// 
			int lo,hi ;
			lo=1;
			hi=4;
			if ( z<=2){
				lo=0;
				hi=1;
			}
			// largePeriod
			if ( z%(3*largePeriod)==0){
				lo=5;
				hi=2;
			}
if (setItems.size()==0){
	z=z+1-1;
}
			// everything is the almost same until here for small changes 
			setItems = powerset.getNextSimilar(setItems, lo,hi ); //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			
			proposedVarIxes = (variables.getIndexesForLabelsList( setItems ));
			proposedVarIxes.trimToSize() ;
			// retrieving top weighted variables, this takes into account evocount, and will take
			// next lower weighted vars  // TODO parameterize that...
			topIxes = getTopEvoWeightVarIxes(4);
			if ((topIxes!=null) && ( topIxes.size()>2)){
									// XXX change from 0
				
				randomSubSelectionOfIndexes( proposedVarIxes, proposedVarIxes.size()-1 , tvIndex) ;
				randomSubSelectionOfIndexes( topIxes, 2 , tvIndex) ;
				
				proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,topIxes);
			}

			setItems = translateVariableIndexesToStrings(proposedVarIxes) ;
			
			if (setItems.size()>=5){
				setItems = removeLowestEvoWeightPosition(setItems);
			}else{
				setItems = addBestEvoWeightPosition(setItems);
			}
if (setItems.size()<=1){
	n=0;
	 
}
			
			// Set<Integer >tempset = translateLocalIndexes( strset );
			
			if (setItems.size()>1){
			// a list of Strings 	
				powerset.addStringSetAsExpicitlyExcluded( setItems ); // this is not ready yet
			
				proposedVarIxes = variables.getIndexesForLabelsList( setItems );
				Collections.sort( proposedVarIxes );
			
				uvec = deriveUseVector(proposedVarIxes);
				currentBaseMetric = evmItems.get(evmItems.size() - 1);
				currentBaseMetric.usageVector = uvec;
				currentBaseMetricIndex = evmItems.size() - 1;
			}
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
				
		
		proposedVariableIndexes = proposedVarIxes ;
	}
	
	private ArrayList<String> translateVariableIndexesToStrings( ArrayList<Integer> ixes) {
		ArrayList<String> items = new ArrayList<String>();
		int ix;
		
		for (int i=0;i<ixes.size();i++){
			
			ix = ixes.get(i) ;
			if (ix>=0){
				String str = somData.getVariables().getItem(ix).getLabel() ;
				items.add(str);
			}
		}// i->
		
		return items;
	}
	
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void _obs_prepareLargeSelectionChange( int z){	
		double uv ; 
		int n,d,k;
		boolean selectionIsFirstTime;
		
		ArrayList<Integer> dIxes, iIxes, topIxes=null;
		// is the new metric completely contained in the best one?
		ArrayList<Integer> proposedVarIxes = new ArrayList<Integer>();
		ArrayList<Integer> bestRVarIxes = new ArrayList<Integer>();
		
		ArrayList<Double> uvec ; 
		
		try{
		
			// TODO: also asking variables for participation
			/*
			 *  we already have a selection "suggestedVarIxes", which is derived from
			 *  statistical tests.
			 *  Yet, this would not lead to an exploration of the space
			 *  
			 *  so we ask the variables themselves for their internal "pressure" state
			 *  preferring slightly the items listed in "suggestedVarIxes" 
			 *  
			 *  This query is organized by the following class "TaskRatedPressure", which
			 *  works on the list EvoBasics.evoTasks  ArrayList<EvoTaskOfVariable>
			 */
			
			TaskRatedPressure trp = new TaskRatedPressure( somData , optimizerSettings , evoBasics) ;  
			trp.setpreferrables( suggestedVarIxes );
			trp.determineUrgingVariables();
			if (z<21){
				urgingVarIxes = trp.getSuggestions(4);
			} else {
				urgingVarIxes = trp.getSuggestions(2);
			}
			
			if (suggestedVarIxes.size()==0){
				prepareSmallSelectionChange(z,1);
				return;
			}
			// 
			n = bestResult.usageVector.size() ;
			
			if (((z%10==0) || (z%3==0)) &&(urgingVarIxes.size()>0)){
				if (urgingVarIxes.size()>0)suggestedVarIxes.addAll(urgingVarIxes) ;
			}
			
			// translate it into index values
			for (int i=0; i<n;i++ ){
				uv = bestResult.usageVector.get(i) ;
				if (uv>0){
					bestRVarIxes.add(i);
				} ;
			}
			bestRVarIxes.trimToSize() ;			
if ((z%5==0) || (z==11)){
	z=z+1-1;
}
			// now we have suggestedVarIxes, bestRVarIxes, from which we create a "mixture"
			int st=0; 
			selectionIsFirstTime = false;
			while (selectionIsFirstTime==false){
			
				n = suggestedVarIxes.size();
				if (n>=6){
					d= Math.max(1, (int) Math.ceil(n*0.27) );
					n=n-d;
					randomSubSelectionOfIndexes( suggestedVarIxes, n, -3) ;
				}
				if (z>21){
					topIxes = getTopEvoWeightVarIxes(3); // TODO parametrize that...
				}
				dIxes = (ArrayList<Integer>) CollectionUtils.disjunction(bestRVarIxes, suggestedVarIxes ) ;
				n = dIxes.size();
				
				iIxes = (ArrayList<Integer>) CollectionUtils.intersection( bestRVarIxes,suggestedVarIxes );
				k = iIxes.size();
				
				d = n-k;
				
				if ((k>=suggestedVarIxes.size()-1)){
					// the size of the intersection is (almost) as large as "suggestedVarIxes"
					proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,suggestedVarIxes) ;
				}else{
					n = (int) Math.max(2,(double)dIxes.size() * 0.71);
					randomSubSelectionOfIndexes( dIxes, n , tvIndex) ;
					proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,dIxes);
				}
				if (proposedVarIxes.size()<=3){ 
					proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,urgingVarIxes);
					if (dIxes.size()>=8){
						n = (int) Math.max(2,(double)dIxes.size() * 0.65);
						randomSubSelectionOfIndexes( dIxes, n , tvIndex) ;
					}
					proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,dIxes);
					if ((topIxes!=null) && ( topIxes.size()>0)){
						
						proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,topIxes);
					}
				}
				
				if ((tvIndex>=0) && (proposedVarIxes.indexOf(tvIndex)<0)){ 
					proposedVarIxes.add(tvIndex );
				}
				
				// alread seen? compare against "exploredMetrices"
				// TODO: or if we have seen this selection already before...
				selectionIsFirstTime = selectionIsFirstTime( proposedVarIxes );
				
				if (selectionIsFirstTime==false){
					if (proposedVarIxes.size()>=6){
						n = (int) Math.max(3,(double)proposedVarIxes.size() * 0.85);
						randomSubSelectionOfIndexes( proposedVarIxes, n , tvIndex) ;
					}
					{	// add
						urgingVarIxes = trp.getSuggestions(6+st);
						randomSubSelectionOfIndexes( urgingVarIxes, 2 , tvIndex) ;
						proposedVarIxes = (ArrayList<Integer>) CollectionUtils.union( proposedVarIxes,urgingVarIxes);
					}
				}
					
				
				if (proposedVarIxes.size()<=1){
					selectionIsFirstTime=false;
				}
				if (st>5){
					break;
				}
				st++;
			}
			
			uvec = deriveUseVector( proposedVarIxes ) ;
			currentBaseMetric = evmItems.get(evmItems.size()-1);
			currentBaseMetric.usageVector = uvec ; 
			currentBaseMetricIndex = evmItems.size()-1 ;
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
				
		
		proposedVariableIndexes = proposedVarIxes ;
	}
	
	
	private boolean selectionIsFirstTime(ArrayList<Integer> indexes) {
		boolean metricIsFresh = true;
		int dn;
		
		
		for (int i=0; i< exploredMetrices.size();i++){
			dn = CollectionUtils.disjunction( exploredMetrices.get(i), indexes).size();
			if (dn==0){
				metricIsFresh = false;
				break;
			}
		}
		
		return metricIsFresh;
	}
	
	/**
	 * 
	 * retrieving the N fields of extreme values (max or min) from the provided list of field indexes 
	 * 
	 * @param indexList
	 * @param xN
	 * @param direction
	 * @return
	 */
	private ArrayList<Integer> getExtremeEvoWeightVarIxes( ArrayList<Integer> indexList, int xN, int direction) {
		
		ArrayList<Integer> xEvoIxes = new ArrayList<Integer>();
		int ix;
		double ew,minew = 9999.09, maxew = -9.09;
		IndexDistance ixd ;
		IndexedDistances ixDists = new IndexedDistances(); 
		
		
		for (int i=0;i<indexList.size();i++){
			
			ix = indexList.get(i);
			if (ix>=0){
				ew = evoBasics.evolutionaryWeights.get(ix) ;
				if (direction<0){
					if (ew<minew){
						minew  = ew ;
						ixd = new IndexDistance(ix,ew, "");
						ixDists.add(0,ixd);	
					}
					
				}
				if (direction>0){
					if (ew>maxew){
						maxew  = ew ;
						ixd = new IndexDistance(ix,ew, "");
						ixDists.add(0,ixd);	
					}
					
				}
			}
		}
		
		
		ixDists.sort(-direction) ;
		
		
		for (int i=0;i< xN;i++){
			if (i<ixDists.size()){
				int p = ixDists.getItem(i).getIndex();
				if (p >= 0) {
					xEvoIxes.add(p);
				}
			}
		} // i ->
		
		return xEvoIxes;
	}
	
	/**
	 * retrieving the global top N fields from evolutionary weights
	 * 
	 * @param topN
	 * @return
	 */
	private ArrayList<Integer> getTopEvoWeightVarIxes(int topN) {
		
		ArrayList<Integer> ixes = new ArrayList<Integer>();
		
		int p;
		double ew, maxw=-9.0;
		IndexDistance ixd ;
		IndexedDistances ixDists = new IndexedDistances(); 
		// topSortedVariables
		// bestResult
		
		for (int i=0;i<evoBasics.evolutionaryWeights.size();i++){
			
			ew = evoBasics.evolutionaryWeights.get(i);
			ixd = new IndexDistance(i,ew,"");
			if (maxw<ew){
				maxw=ew;
				ixDists.add(0,ixd);
			}else{
				ixDists.add(ixd);
			}
		}
		
		ixDists.sort(-1);
		
		if (topN>ixDists.size()-1){
			topN = ixDists.size()-1 ;
		}
		
		for (int i=0;i<topN;i++){
			p = ixDists.getItem(i).getIndex();
			if (p>=0){
				ixes.add(p) ;
			}
			
		} // i ->
		
		return ixes;
	}

	private void randomSubSelectionOfIndexes( ArrayList<Integer> ixes, int newN, int dontTouchIxValue){
		int p;
		
		if (ixes.size()==0){
			return;
		}
		p = jrandom.nextInt(100) ; p = jrandom.nextInt();
		if (newN>=ixes.size()){
			newN = ixes.size()-1 ;
		}
		if (ixes.indexOf(dontTouchIxValue)>=0){
			newN=newN-1;
		}
		while (ixes.size()>newN){
			p = jrandom.nextInt( ixes.size() ) ;
			if ((p>=0) && (p<ixes.size())){
				if (ixes.get(p)!= dontTouchIxValue){
					ixes.remove(p) ;	
				} // ?
				
			} // ?
		} // ->
		
	}
	
	
	/**
	 * 
	 * this method may be used to directly influence the evolutionary weights, for instance also as 
	 * a result of some statistical modeling of the variables ;
	 * 
	 * 
	 * @param influencevector
	 */
	public void influenceEvolutionaryWeights( double[] influencevector ){
		int z,ec;
		double ew;
		
		int n = influencevector.length ;
		
		if (evoBasics.evolutionaryWeights.size()< n){
			z=0;
			while (evoBasics.evolutionaryWeights.size()< n){
				
				if (z+1> evoBasics.evolutionaryWeights.size()){
					evoBasics.evolutionaryWeights.add( influencevector[z] ) ;
					evoBasics.evolutionaryCounts.add(0) ;
				}
				
				z++;
			}
		}
			
		ec=0;
		for (int i=0;i<influencevector.length;i++){
			
			ec = evoBasics.evolutionaryCounts.get(i);
			ew = evoBasics.evolutionaryWeights.get(i);
			
			if (ec==0){
				if (ew>0){
					ew = (influencevector[i] +ew)/2.0;
				}else{
					ew = influencevector[i] ;
				}
			}else{
				ew = (ew*(double)ec + influencevector[i])/((double)(ec+1));
			}
			evoBasics.evolutionaryWeights.set(i, ew) ;
		}
		
		ec=0;
	}

	
	public double[] deriveSimpleInfluenceVector( double[] influencevector,
			 									 ArrayList<Double> useVector) {
	//  
		if ((influencevector == null) || (influencevector.length == 0)) {
			influencevector = new double[useVector.size()];
			
			for (int i = 0; i < influencevector.length; i++) {
				influencevector[i] = 0.5;
			}
		}

		for (int i = 0; i < useVector.size(); i++) {
			if (useVector.get(i)>0){
				influencevector[i] = influencevector[i] + (1.0 - influencevector[i]) * Math.min( 0.18,useVector.get(i));
			}
		}

		return influencevector;
	}
	
	
	
	protected void calculateComparison(){
		
		
	}

	
	private ArrayList<Double> deriveUseVector( ArrayList<Integer> indexes ) throws Exception{ 
		
		int varix ;
		Variables variables;
		
		double[] usevector ;
		ArrayList<Double> usageVector = new ArrayList<Double>();
		
		if (indexes.size()<=1){
			throw(new Exception("selection is empty, usage vector could not be prepared.")) ;
		}
		
			
		try{
			

			// starting with somMapTable but finally referring to the global vector (inclusive the transform-New variables)
			variables = somData.getVariables();
 
			// usevector = dSom.getdSomCore().prepareUsageVector(dtable, variables) ;
			 
			usevector = new double[variables.size()] ;
			varix = variables.getTvColumnIndex() ;
			if (varix<0){
			
				// varix = dSom.getTargetVariableColumn() ;	
				variables.setTvColumnIndex(varix) ;
				int n = 10/0;
			}
			if (varix<0){
				return usageVector ; // it is still empty -> nothing will happen
			}
			
			usevector [varix] = 1;
			
			for (int i=0;i< indexes.size() ;i++){
				
				varix = indexes.get(i);  
				usevector[varix] = 1 ;
			}
			
			usageVector = ArrUtilities.changeArraystyle(usevector) ;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return usageVector;
	}

	private double calculateEvoCountsTotalSum() {
		int sum = 0;
		
		for (int i=0;i<evoBasics.evolutionaryCounts.size();i++){
			sum = sum + evoBasics.evolutionaryCounts.get(i) ;
		}
		
		return sum;
	}
	
	
	public void addEvmItems(ArrayList<EvoMetrik> evmitems) {
	 
		ArrayList<String> listedM = new ArrayList<String>();
		
		if ((evmitems==null) || (evmitems.size()==0)){
			return;
		}
		
		if (evmItems==null)evmItems = new ArrayList<EvoMetrik>();
		
		for (int i=0;i<evmItems.size();i++){

			EvoMetrik em = evmItems.get(i);
			
			String str = ArrUtilities.arr2Text( em.getVarIndexes() ).trim();
			str = StringsUtil.replaceall(str, " ", ""); 

			if (listedM.indexOf(str)<0){
				listedM.add(str);
			}
				
		}
		
		for (int i=0;i<evmitems.size();i++){
			
			EvoMetrik em = evmitems.get(i);
			
			String str = ArrUtilities.arr2Text( em.getVarIndexes() ).trim();
			str = StringsUtil.replaceall(str, " ", ""); 
			
			if (listedM.indexOf(str)<0){

				listedM.add(str);
				evmItems.add( new EvoMetrik( em ) );
				evmItems.get( evmItems.size()-1).step = evmItems.size() ;
			}
		}
		listedM.clear();
	}

	 
	
	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}

	public void setModelingSettings(ModelingSettings modelingSettings) {
		this.modelingSettings = modelingSettings;
	}

	public OptimizerSettings getOptimizerSettings() {
		return optimizerSettings;
	}

	public void setOptimizerSettings(OptimizerSettings optimizerSettings) {
		this.optimizerSettings = optimizerSettings;
	}

	public int getTvIndex() {
		return tvIndex;
	}

	public void setTvIndex(int tvIndex) {
		this.tvIndex = tvIndex;
	}

	public ArrayList<EvoMetrik> getItems() {
		return evmItems;
	}

	public void setItems(ArrayList<EvoMetrik> items) {
		evmItems = items;
	}

	public ArrayList<Double> getEvolutionaryWeights() {
		return evoBasics.evolutionaryWeights;
	}

	public void setEvolutionaryWeights(ArrayList<Double> evolutionaryWeights) {
		this.evoBasics.evolutionaryWeights = evolutionaryWeights;
	}

	public ArrayList<Integer> getEvolutionaryCounts() {
		return evoBasics.evolutionaryCounts;
	}

	public void setEvolutionaryCounts(ArrayList<Integer> evolutionaryCounts) {
		this.evoBasics.evolutionaryCounts = evolutionaryCounts;
	}

	public EvoBasics getEvoBasics() {
		return evoBasics;
	}

	public void setEvoBasics(EvoBasics evoBasics) {
		this.evoBasics = evoBasics;
	}

	/**
	 * @return the powerset
	 */
	public PowerSetSpringSource getPowerset() {
		return powerset;
	}



	public ArrayList<Integer> getProposedVarSelection() {
		
		return proposedVariableIndexes;
	}

	/**
	 * @return the currentBaseMetrik
	 */
	public EvoMetrik getCurrentBaseMetric() {
		return currentBaseMetric;
	}

	/**
	 * @param currentBaseMetrik the currentBaseMetrik to set
	 */
	public void setCurrentBaseEvoMetrik(EvoMetrik metric) {
		this.currentBaseMetric = metric;
	}
 

	/**
	 * @return the evmItems
	 */
	public ArrayList<EvoMetrik> getEvmItems() {
		return evmItems;
	}

	/**
	 * @return the bestResult
	 */
	public EvoMetrik getBestResult() {
		return bestResult;
	}

	/**
	 * @return the urgingVarIxes
	 */
	public ArrayList<Integer> getUrgingVarIxes() {
		return urgingVarIxes;
	}

	/**
	 * @return the exploredMetrices
	 */
	public ArrayList<ArrayList<Integer>> getExploredMetrices() {
		return exploredMetrices;
	}

	/**
	 * @return the currentBaseMetrikIndex
	 */
	public int getCurrentBaseMetrikIndex() {
		return currentBaseMetricIndex;
	}

	/**
	 * @param currentBaseMetrikIndex the currentBaseMetrikIndex to set
	 */
	public void setCurrentBaseMetrikIndex(int metricIndex) {
		this.currentBaseMetricIndex = metricIndex;
	}

	/**
	 * @return the suggestedVarIxes
	 */
	public ArrayList<Integer> getSuggestedVarIxes() {
		return suggestedVarIxes;
	}

	/**
	 * @param suggestedVarIxes the suggestedVarIxes to set
	 */
	public void setSuggestedVarIxes(ArrayList<Integer> suggestedVarIxes) {
		this.suggestedVarIxes = suggestedVarIxes;
	}

	/**
	 * @return the proposedVariableIndexes
	 */
	public ArrayList<Integer> getProposedVariableIndexes() {
		return proposedVariableIndexes;
	}

	/**
	 * @param proposedVariableIndexes the proposedVariableIndexes to set
	 */
	public void setProposedVariableIndexes(
			ArrayList<Integer> proposedVariableIndexes) {
		this.proposedVariableIndexes = proposedVariableIndexes;
	}

	/**
	 * @return the topSortedVariables
	 */
	public IndexedDistances getTopSortedVariables() {
		return topSortedVariables;
	}

	/**
	 * @param topSortedVariables the topSortedVariables to set
	 */
	public void setTopSortedVariables(IndexedDistances topSortedVariables) {
		this.topSortedVariables = topSortedVariables;
	}

	/**
	 * @param evmItems the evmItems to set
	 */
	public void setEvmItems(ArrayList<EvoMetrik> evmItems) {
		this.evmItems = evmItems;
	}

	/**
	 * @param bestResult the bestResult to set
	 */
	public void setBestResult(EvoMetrik resultmetrik) {
		if (bestResult!=null){
			this.bestResult = new EvoMetrik(resultmetrik);
		}
	}

	/**
	 * @param urgingVarIxes the urgingVarIxes to set
	 */
	public void setUrgingVarIxes(ArrayList<Integer> urgingVarIxes) {
		this.urgingVarIxes = urgingVarIxes;
	}

	/**
	 * @param exploredMetrices the exploredMetrices to set
	 */
	public void setExploredMetrices(ArrayList<ArrayList<Integer>> exploredMetrices) {
		this.exploredMetrices = exploredMetrices;
	}

	@Override
	public String toString(){
		String istr, outstr="";
		 
		for (int i=0;i<evmItems.size();i++){
			EvoMetrik em = evmItems.get(i) ;
			istr = ArrUtilities.arr2Text( em.varIndexes);
			outstr = outstr+"  "+(i+1)+":  "+istr+" \n" ;
		}
		
		return outstr;
	}
	
	public String toStringLastN(int lastN){
		String istr, outstr="";
		
		int totaln = evmItems.size();
		int firstp = totaln - lastN ;
		if (firstp<0)firstp=0;
		
		for (int i=firstp;i<evmItems.size();i++){
			EvoMetrik em = evmItems.get(i) ;
			istr = ArrUtilities.arr2Text( em.varIndexes);
			outstr = outstr+"  "+(i+1)+":  "+istr+"\n" ;
		}
		
		return outstr;
	}
	public String toStringFirstN(int firstN){
		String istr, outstr="";
		 
		int lastp = firstN;
		if (lastp>evmItems.size()-1)lastp=evmItems.size()-1;
		
		for (int i=0;i<lastp;i++){
			EvoMetrik em = evmItems.get(i) ;
			istr = ArrUtilities.arr2Text( em.varIndexes);
			outstr =  outstr+"  "+(i+1)+":  "+istr+"\n" ;
		}
		
		
		return outstr;
	}

	/**
	 *  EvoMetrices._SORT_SCORE , _SORT_INDEX , _SORT_SIZE
	 * @param criterion
	 */
	@SuppressWarnings("unchecked")
	public void sort(int criterion , int direction) {
		
		Collections.sort( evmItems, new evmComparator(criterion,direction));	
	}
	

	@SuppressWarnings("rawtypes")
	class evmComparator implements Comparator{

		int criterion=0, direction=1;
		
		public evmComparator(int c, int direction){
			criterion = c;
			this.direction = direction;
		}

		
		@Override
		public int compare(Object obj1, Object obj2) {
			
			int result = 0;
			EvoMetrik evm2,evm1;
			double v1=0,v2=0 ;
			
			evm1 = (EvoMetrik)obj1;
			evm2 = (EvoMetrik)obj2;
			
			if (criterion==EvoMetrices._SORT_SCORE){ 
				v1 = evm1.getSqData().score ;
				v2 = evm2.getSqData().score ;
			}
			if (criterion==EvoMetrices._SORT_INDEX){
				
			}
			if (criterion==EvoMetrices._SORT_SIZE){
				
			}
 
			// top down
				if (v1>v2){
					result = -1;
				}else{
					if (v1<v2){
						result =  1 ;
					}
				}
				
				if (direction<0){
					result = result * (-1) ;
				}
			
			return result;
		}
		
	}
 
	
	
	
}











package org.NooLab.somsprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;

import org.NooLab.math3.random.RandomDataImpl;
import org.NooLab.math3.stat.clustering.Cluster;
import org.NooLab.math3.stat.clustering.EuclideanDoublePoint;
import org.NooLab.math3.stat.clustering.KMeansPlusPlusClusterer;
import org.NooLab.math3.stat.correlation.SpearmansCorrelation;
import org.NooLab.math3.stat.inference.MannWhitneyUTest;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.categories.similarity.SimilarityCalculator;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.somfluid.core.nodes.ClusterNode;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somsprite.func.SpriteFuncIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.TimeAnchorIntf;
import org.NooLab.utilities.vm.RuntimeInfo;

import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.Covariance;

// import org.apache.mahout.clustering.fuzzykmeans.*;
import org.math.array.DoubleArray;
 

/**
 * 
 * for all formulas func(a,b):
 *   - check the association between a+TV, b+TV, f(a,b)+TV, i.e. referring to explicit formulas
 *   - check the linear, non-topological grouping using KNN clustering, 
 *     in case of multi-group target, or multi-variate checks
 *     
 * create a SOM-based transformation of variables into a score, based on subsamples 
 *   of max 5 variables, creating transformative SOMs of size <10..20
 *   the transformative SOM is equivalent to global pre-processing and scoring
 *   yet, it also could be developed in a node-specific manner      
 *   
 *   function-building happens due to idealization, variable vector reduction etc.
 *   it adds a score to the data, which translates the non-linearity into a linear measure
 *   while being resistant to noise 
 *   from all sup5-combinations, only the best 2 or three will be taken 
 *   
 *   additionally, the analytic stuff could be used right-away!!!
 *   
 *   
 * 
 * for large SOMs we need a parallelized version of the algorithms, let's say 100'000 nodes to cluster check....
 * @author kwa
 *
 */
public class DependencyScreener {

	SomSprite  somSprite;
	
	SomDataObject somData;
	
	SomMapTable somMapTable, transposedSomMapTable;
	Evaluator evaluator;
	DScreenerObserver screeningObserver ;
	
	// in case of multi-threading, we have to maintain separate evaluators for each thread, 
	// since the Evaluator class is not thread-safe...
	ArrayList<Evaluator> evaluators = new ArrayList<Evaluator>();
	RandomDataImpl randomData = new RandomDataImpl();
	
	IndexedDistances ixdPair = new IndexedDistances();
	
	// this "EuclideanDoublePoint" knows by itself how to calculate the distance
	KMeansPlusPlusClusterer<EuclideanDoublePoint> kMeans ;
	// TODO: we use k-means to estimate the match in multi-target case
	
	ArrayList<ArrayList<AnalyticFunctionSpriteImprovement>> bestEstimatedUtilitiesList = new ArrayList<ArrayList<AnalyticFunctionSpriteImprovement>>();
	ArrayList<AnalyticFunctionSpriteImprovement> bestEstimatedUtilities = new ArrayList<AnalyticFunctionSpriteImprovement>();
	ArrayList<AnalyticFunctionSpriteImprovement> knownTransformations;
	
	ArrayList<Integer> lastBeuCheckIndex = new ArrayList<Integer>();
	ArrayList<Integer> beusBestN = new ArrayList<Integer>();
	double maxEstimatedImprovement=0.0;
	
	int limitCountForExploredCombination = 10000000;
	
	double thresholdEu = -1.0 ;
	int dCountThreshold = 5;
	int screeningListSize=0;
	
	boolean stopScreening=false;
	DScreenerDigester dsd = null;
	boolean multiParallel=true;
	DependencyChecker[] dependencyCheckers = new DependencyChecker[1] ;

	private int threadCount ;

	int totalCountOfChecks=0, totalCountOfChecksPerformed=0 ;
	int[] processCallCounter = new int[1];
	int lastMsgIndex=0,lastMsgIndex2=0;
	private int displaymode ;
	private long starttime =0;
	private PrintLog out ;
	private int outlevel=3;
	long lastPrcTime = 0;

	boolean detailedDisplay=true;
	
	// ========================================================================
	public DependencyScreener(SomSprite somsprite, SomMapTable smt, Evaluator ev) {
		
		somSprite = somsprite;
		somMapTable = smt;
		evaluator = ev;
		
		kMeans = new KMeansPlusPlusClusterer<EuclideanDoublePoint>( (new Random(1234)) );
		
		threadCount = Math.max(2, RuntimeInfo.processorCount()-2);
		threadCount = 1;
		
		out = somSprite.out;
	}
	// ========================================================================	

	
	public void close(){
		
		if (screeningObserver!=null){
			screeningObserver.observerIsRunning = false;
		}
		
		for (int i=0;i<evaluators.size();i++){
			evaluators.get(i).clear();
			evaluators.set(i,null);
		}
		evaluators.clear();
		ixdPair.clear();
		
		for (int i=0;i<bestEstimatedUtilitiesList.size();i++){
			bestEstimatedUtilitiesList.get(i).clear();	
		}
		bestEstimatedUtilitiesList.clear();
		bestEstimatedUtilities.clear();
		knownTransformations.clear();
		
	}

	
	// ------------------------------------------------------------------------
	class DependencyChecker implements Runnable{
		
		ArrayList<int[]> depVarIndexesList;
		int[] depVarIndex ; 
		int index, processID;
		private boolean dcheckIsRunning=false;
		boolean dcheckIsActive=false;
		ArrayList<Integer> indexQueue = new ArrayList<Integer> ();
		 
		MannWhitneyUTest mwu = new MannWhitneyUTest(randomData);
		SpearmansCorrelation spc = new SpearmansCorrelation();
		
		Thread dckThrd;
		// put this to powers of 10 to get more frequent output
		private int verbose = 100;
		
		// ....................................................................
		public DependencyChecker( int[] depVarIndex, int index, int processID ){
			this.depVarIndex = depVarIndex; 
			this.index = index;
			this.processID = processID;
		}

		public DependencyChecker( int processID, ArrayList<int[]> varIndexesList  ){
			this.processID = processID;
			depVarIndexesList = varIndexesList;
		}
		// ....................................................................
		
		private void _show_progress(){// int nthCheck, int recn){
			totalCountOfChecksPerformed++;
			int recn = totalCountOfChecks ;
			int index = totalCountOfChecksPerformed ; // nthCheck;
			
			long dT,now = System.currentTimeMillis() ;
			dT = now -lastPrcTime;
			
			if ((index<threadCount) || ((index%10==0) && (index<522)) || (index>=599)){ // we also might display the time till completion
			
				if (displaymode >= SomFluidProperties._SOMDISPLAY_PROGRESS_PERC){
					//out.print(2, "screening for dependencies ("+(i)+" of "+depVarIndexes.size()+") in variables ("+depVarIndex[0]+","+depVarIndex[1]+")..." );
					if ((screeningListSize*recn>8000000) && ((index<((double)Math.max(screeningListSize*0.031,1499))) ) ){
						if ((index-10<-threadCount+5) || (index<=1) || (index >= ((int)150*threadCount) )){
							if (index>lastMsgIndex+threadCount+1){
								out.printprc(outlevel, index, screeningListSize, (screeningListSize)/(150), "");
								lastMsgIndex=index;
								lastPrcTime = System.currentTimeMillis();
							}
						}
					}else{
						int f = 10;
						if (screeningListSize>12000)f=20;
						if (index % f ==0){
							out.printprc(outlevel, index, screeningListSize, screeningListSize/f, "");
						}else{
							if ((f>3000) && (index % f < threadCount)){
								out.printprc(outlevel, index, screeningListSize, screeningListSize/5, "");
							}
						}
						
						
						int dm = index % (int)(200*threadCount);
						if ( (screeningListSize*recn>8000000)  && ((Math.abs(dm-100) < (threadCount-1)) || ( dm<=Math.max(0,(threadCount-1))))){
							String str="";
							if (index<1000){ str =" ...remaining" ; }
							if (index>(lastMsgIndex+2*threadCount)){
								out.printCompletionTime(outlevel, starttime,index,screeningListSize , threadCount, true, str);
								lastMsgIndex=index;
								lastPrcTime = System.currentTimeMillis();
							}
						}  
					}
				}
			}else{
				
				if ((dT>1000*60*10) || ((lastPrcTime == 0) && (index>10))){
					out.printprc(outlevel, index, screeningListSize, 1, "");
					lastMsgIndex=index;
					lastPrcTime = System.currentTimeMillis();
				}
			}
			

			
		}

		public void check( int index){
			// put it to the queue
			indexQueue.add(index);
		}

		private void perform( ){ // int[] depVarIndex, int index, int processID
			
			 
			
			ArrayList<AnalyticFunctionSpriteImprovement> beu;
			ArrayList<AnalyticFunctionSpriteImprovement> estimatedUtils ;
			
			String[] depVar = new String[3];
			double[][] depVarValues = new double[5][somMapTable.values.length];  // somMapTable.values.length == number of nodes in the SOM
		
			depVar[0] = somMapTable.variables[ depVarIndex[0] ];
			depVar[1] = somMapTable.variables[ depVarIndex[1] ];
										
			int recn = somMapTable.values[0].length ; 
										if (processID==0){ // avoiding collisions
											// _show_progress( ArrUtilities.arraySum(processCallCounter), recn) ;
										}
			int va = depVarIndex[0];
			int vb = depVarIndex[1];
			/*
			double[] testarr1 = new double[transposedSomMapTable.values[va].length] ;
			double[] testarr2 = new double[transposedSomMapTable.values[va].length] ;
			
			System.arraycopy( transposedSomMapTable.values[va], 0, testarr1, 0, transposedSomMapTable.values[va].length);
			System.arraycopy( transposedSomMapTable.values[vb], 0, testarr2, 0, transposedSomMapTable.values[vb].length);
			*/
			// even copying is not necessary...
			try{
				
				// no copying is necessary, just referencing the rows in the transposed table
				// get column a
				depVarValues[0] = transposedSomMapTable.values[va] ;
				// get column b
				depVarValues[1] = transposedSomMapTable.values[vb] ;
				// get column target
				depVarValues[2] = transposedSomMapTable.values[somMapTable.tvIndex] ;
				
			}catch(Exception e){
				return;
			}
			
			/*
			// get columns
			for (int r=0;r<somMapTable.values.length;r++){
				// get column a
				depVarValues[0][r] = somMapTable.values[r][va] ;
				
				// get column b
				depVarValues[1][r] = somMapTable.values[r][vb] ;
				// get column target
				depVarValues[2][r] = somMapTable.values[r][somMapTable.tvIndex] ;
				
			} // all rows in extracted columns
			*/
			
			
											out.print(5, ".\n.\n.dT (1) ");
			// this returns a relative improvement of the association
			AssociationDifferential ad = new AssociationDifferential();
											out.print(5, "dT (2) ");
			estimatedUtils = ad.check(va,vb,depVarValues, evaluators.get(processID), mwu, spc );
											out.print(5, "dT (50) ");
											
			if ((estimatedUtils!=null) && (estimatedUtils.size()>0)){
				
				// bestEstimatedUtilities.addAll(estimatedUtils) ;
				beu = bestEstimatedUtilitiesList.get(processID);
				
				beu.addAll(estimatedUtils) ;
											out.print(5, "dT (52) ");
				/*
				if (thresholdEu<0){
					beu.addAll(estimatedUtils) ;
				}else{
					for (int k=0;k<estimatedUtils.size();k++){
						if (estimatedUtils.get(k).estimatedImprovement > thresholdEu){
							beu.add( estimatedUtils.get(k) ) ;
						}
						
					}
				}
				*/ 
			}
			estimatedUtils.clear() ;
											out.print(5, "dT (55) ");
			processCallCounter[processID]++;
		}

		public void start(){
			if (dckThrd==null){
				dckThrd = new Thread(this,"dckThrd-"+processID);
				dckThrd.start();
			}
		}
		
		@Override
		public void run() {
			int z=0;
			int ix;
			dcheckIsActive=false;
			dcheckIsRunning = true;
			
			while (dcheckIsRunning){
			
				if (dcheckIsActive==false){
					dcheckIsActive = true;
					
					if (indexQueue.size()>0){
						z++;
						ix = indexQueue.get(0); 
						depVarIndex = depVarIndexesList.get(ix) ;
						
						perform();
						
						
						if (verbose > 100000)verbose=100000;
						if ((z%(100000/verbose)==0) && (processID==0)){
							out.print(3, "process: "+processID+", z: "+z+"... ") ;
						}
						_show_progress();
						
						// out.delay(50);
						indexQueue.remove(0) ;
						out.delay(5);
						if (z%10==0){ 
							screeningObserver.gcWaiting++;
						}
						// Thread.yield() ;
					}
					dcheckIsActive = false;
					
					if (indexQueue.size()==0){
						out.delay(1);
					}
				}
				
			} // ->
			
		}
		
		 
	}
	// ----------------------------------------------------

	private int dcWorking(){
		int rwc = 0;
		if ((dependencyCheckers != null) && (dependencyCheckers.length > 0)) {
			for (int i = 0; i < dependencyCheckers.length; i++) {
				if ((dependencyCheckers[i].indexQueue.size()>0) && (dependencyCheckers[i].dcheckIsRunning )){
					rwc++;
				}
			}
		}
		return rwc;
	}
	
	
	
	/** we need to encapsulate it speedily in order to avoid collisions
	 * yet e should not create a new object for each call, instead we prepare a 
	 * a "checker object" (instance of "DependencyChecker()") apriori when preparing multi-threading
	 */
	protected void checkDependencyIndex( int[] depVarIndex, int index, int processID){
	
		ArrayList<AnalyticFunctionSpriteImprovement> beu ;
		
		dependencyCheckers[processID].check(index );
		
		/*
		try{
		
			int bix = lastBeuCheckIndex.get(processID);
			if ((index - bix )>1000){
	
				beu = bestEstimatedUtilitiesList.get(processID);
				if (beu.size()>0){
					Collections.sort(beu, new euComparable(-1));
					
					int n = Math.min(20, beu.size());
					beusBestN.set(processID ,0);
					for (int i=0;i<n;i++){
						if (beu.get(i).estimatedImprovement > 3){
							beusBestN.set(processID, beusBestN.get(processID)+1);
						}
					}
					maxEstimatedImprovement = Math.max( maxEstimatedImprovement, beu.get(0).estimatedImprovement) ;
				} // anything collected so far
				
			} // > 1000 checks since last supervision ?
			
		}catch(Exception e){
			
		}
		*/
	}
	
	/**
	 * - pre-calculating the combinations and preparing a list to work through; 
	 * - calling (1 thread) or organizing the calling (2+ threads) 
	 */
	public void go(){
	 
		String varLabel;
		int  recn ;
		int[] depVarIndex;
		
		Variables variables;
		ArrayList<int[]> depVarIndexes = new ArrayList<int[]>() ;
		
		AnalyticFunctionSpriteImprovement euItem;
		
		somData = somSprite.somData;
		variables = somData.getVariables() ;
		recn = somData.getNormalizedDataTable().getColumn(0).size() ;
		
		// TODO first get known transformations and affected derived variables, such that we can 
		//      prevent repeated suggestion of the same transformation 
		 
		knownTransformations = variables.getKnownTransformations();
		
		// 
		int tvindex = somMapTable.tvIndex ;

		try{
			
			// we separate the tasks in a very trivial way;
			// yet, this supports parallel processing of the list
			 
			// potential problem here: not all functions are symmetric for a,b
			for (int a=0;a<somMapTable.values[0].length-1;a++ ){
				if (a==tvindex){
					continue;
				}
				 
				for (int b=a+1;b<somMapTable.values[0].length;b++ ){
					if (b==tvindex){
						continue;
					}
  
					depVarIndex = new int[3] ;
					depVarIndex[0] = a;
					depVarIndex[1] = b;
					depVarIndex[2] = somMapTable.tvIndex ; 
					
					if (depVarIndexes.size()<100000000){
						depVarIndexes.add(depVarIndex) ;
					}else{
						out.printErr(2,"not all combinations will be explored to to their huge number, limit has been set at a,b="+a+","+b+"  ");
						break;
					}
					if (depVarIndexes.size()>=limitCountForExploredCombination){ // user option: limit
						break;
					}
				}  
			} // b->
			
			totalCountOfChecks = depVarIndexes.size();
			
			transposedSomMapTable = new SomMapTable();
			transposedSomMapTable.values = DoubleArray.transpose(somMapTable.values) ;
			
			screeningListSize = depVarIndexes.size() ;
			starttime = System.currentTimeMillis();
											displaymode = somSprite.sfProperties.getShowSomProgressMode();
											if (depVarIndexes.size()*recn>800000){
												displaymode = SomFluidProperties._SOMDISPLAY_PROGRESS_PERC;
												outlevel=2;
											}
											out.print(2, "going to evaluate "+(depVarIndexes.size())+" explicit bi-variate functions...") ;
			 								
			// we could start a supervisor process, since we will implement anyway only 20..30 improvements
			// (dependent on the number of variables) we need not calculate all of them
			// scrambling the index arrays ensures that we visited all variables rather soon
			screeningObserver = new DScreenerObserver();
			screeningObserver.start();
			
			// maxEstimatedImprovement
			
		    if (depVarIndexes.size()<200){
		    	multiParallel=false;
		    }
		    
		    // multiParallel=false;
			if (multiParallel){
			
				processCallCounter = new int[threadCount] ;
				dependencyCheckers = new DependencyChecker[threadCount] ; 
				
				lastMsgIndex=0;
				// we maintain a separate list for results for each of the threads, in order to avoid collisions
				for (int i=0;i<threadCount;i++){
					bestEstimatedUtilitiesList.add( new ArrayList<AnalyticFunctionSpriteImprovement>() );
					
					evaluators.add( new Evaluator(somSprite) );
					somSprite.createFunctions( evaluators.get(evaluators.size()-1)) ;
					lastBeuCheckIndex.add(0) ;
					beusBestN.add(0);
					
					dependencyCheckers[i] = new DependencyChecker(i,depVarIndexes);
					dependencyCheckers[i].start() ;
				}
				
				dsd = new DScreenerDigester();
				// dsd.balancedExecution = true;
				dsd.shuffledIndices = true;
				dsd.digestingDependencyItems(depVarIndexes, threadCount);
				dsd.start() ;
				
				while (dsd.getClosedStatus() <= 0) {
					out.delay(5);
				}
				out.delay(102);
				while (dcWorking()>0){
					out.delay(101);
				}
				
				// combine the partial results
				bestEstimatedUtilities.clear();
				for (int i=0;i<threadCount;i++){
					bestEstimatedUtilities.addAll( bestEstimatedUtilitiesList.get(i) );
				}
				
			}else{
				
				dependencyCheckers[0] = new DependencyChecker(0,depVarIndexes);
				// we have just the default evaluator in this list...
				evaluators.add( evaluator );
				// TODO: this could be multi-threaded easily (??)... steps are completely independent
											out.print(2, "screening for dependencies (n="+depVarIndexes.size()+")..." );
											
				for (int i = 0; i < depVarIndexes.size(); i++) {

					depVarIndex = depVarIndexes.get(i);
					checkDependencyIndex(depVarIndex, i, 0);

				} // i-> all pairs
				
				if (bestEstimatedUtilitiesList.size()>0){
					bestEstimatedUtilities.addAll( bestEstimatedUtilitiesList.get(0) );
				}
				
			}							
			
			screeningObserver.observerIsRunning = false;							
			
			Collections.sort(bestEstimatedUtilities, new euComparable(-1));
			
			// remove known ones
			removeKnownpreviousProposals();
			
			// reduce for similar, only <N> items per pair of variable
			reduceListOfCandidates(bestEstimatedUtilities); 
			
											out.print(2, "all pairs checked, preparing potential improvements...");
			
			
			String expressionName , expression, str1, str2;
			SpriteFuncIntf func;
			double kvalue ;
			
			for (int i=0;i<bestEstimatedUtilities.size();i++){
				
				euItem = bestEstimatedUtilities.get(i);
				         //fix1 = euItem.funcIndex1 ; fix2 = euItem.funcIndex2 ;

				expressionName = evaluator.getExpressionName(euItem.funcIndex1); 
				expression = evaluator.getExpression(expressionName );
				
				func = (SpriteFuncIntf) evaluator.functions.get(expressionName) ;
				FunctionCohortParameterSet cps = func.getCohortParameterSet() ;
				
				if (cps!=null){
					if ((cps.cohortValues.length>0) && (euItem.funcIndex2<0)){
						int k=0;
						k = cps.cohortValues.length/2;
						euItem.funcIndex2=k;
					}
					if ((euItem.funcIndex2 >=0) && (euItem.funcIndex2 < cps.cohortValues.length)){
						
						kvalue = cps.cohortValues[ euItem.funcIndex2 ] ;
						kvalue = (Math.round( kvalue*1000.0)/1000.0) ;
						// replace k by value
						expression = expression.replace( cps.varPLabel, ""+kvalue+"") ;
					}
				}
				 
				// refresh expression setting ...
				euItem.setExpression(expression);
				euItem.setExpressionName(expressionName);
				  
				// translate indices from "somMapTable" to SomDataObject
				varLabel = somMapTable.variables[euItem.varIndex1] ; 
				euItem.varIndex1 = variables.getIndexByLabel(varLabel) ;  str1 = varLabel;
				
				varLabel = somMapTable.variables[euItem.varIndex2] ;
				euItem.varIndex2 = variables.getIndexByLabel(varLabel) ;  str2 = varLabel;
				
				if (somSprite.isProposalKnown(euItem)){
					euItem.funcIndex1 = -1 ; euItem.funcIndex2 = -1 ;
					euItem.varIndex1  = -1 ; euItem.varIndex2  = -1 ;
					euItem.setExpression("") ;
					euItem.estimatedImprovement = -1.0 ;
				}
											if (euItem.varIndex1>0){
												out.print(2, "var 1,  var 2  : "+euItem.varIndex1+", "+euItem.varIndex2+"  : "+expression );
												out.print(2, "   labels a,b  :           "+str1+", "+str2);
											}                               
				// also register encoding, and print it
											
											
										
			}
			
											
			// remove empty items
			AnalyticFunctionSpriteImprovement fs ;
			int i=bestEstimatedUtilities.size()-1;
			
			while(i>=0){
				
				fs = bestEstimatedUtilities.get(i) ;
				if ((fs.funcIndex1<0) || (fs.getExpressionName().length()==0)){
					bestEstimatedUtilities.remove(i) ;
				}
				i--;
			}// ->
			
			int n = bestEstimatedUtilities.size();
			if (n>5){
				n=5;
				bestEstimatedUtilities = new ArrayList<AnalyticFunctionSpriteImprovement>( bestEstimatedUtilities.subList(0, n));
			}
											out.print(2, bestEstimatedUtilities.size()+" potential improvements found.");
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		 
	}

	// ========================================================================
	class DScreenerDigester implements Runnable, org.NooLab.chord.IndexedItemsCallbackIntf{
		 
		MultiDigester digester ;
		ArrayList<int[]> list;
		int closed=-1;
		boolean balancedExecution=false, shuffledIndices=false ;
		int _id0=1998, _id1 = 2211;
		boolean diagnosisConsole = false;
		
		Thread dsdThrd ;
		
		// ................................................
		public DScreenerDigester(){
			dsdThrd = new Thread(this,"dsdThrd")  ;
		}
		// ................................................
		
		public void start(){
			dsdThrd.start() ;
		}
		
		@Override
		public void run() {
			 
			performDsD();
		}



		private void performDsD() {
			 
			digester.execute() ;
			
			
			// the digester itself waits until all threads have been completed
			closed = 1;
			out.delay(110);
			
			digester = null;
		}
		
		protected void digestingDependencyItems( ArrayList<int[]> list, int threadcount){ 
			 
			this.list = list; 
			// providing also right now the callback address (=this class)
			// the interface contains just ONE routine: perform()
			digester = new MultiDigester(threadcount, (IndexedItemsCallbackIntf)this ) ;
			// threads are created already by creating the multi object! they are then in wait state 
			
			balancedExecution = false;
			shuffledIndices = false;
			digester.setDiagnosticPrintOut(0); 
			digester.setBalancedExecution( balancedExecution); 
			digester.setShuffledIndices(shuffledIndices) ; // before preparing calling "prepareItemSubSets()" !! 
			
			digester.setPriority( MultiDigester._PRIORITY_HI ) ;
			
			// note, that the digester need not to know "anything" about our items, just the amount of items
			// we would like to work on.
			// the digester then creates simply an array of indices, which then point to the actual items,
			// which are treated anyway here (below) !
			int itemCount = list.size() ;
			digester.prepareItemSubSets( itemCount,0 );
			 

		}
		
		
		/**
		 * this is called from the MultiDigester via the callback interface
		 */
		@Override
		public void perform(int processID, int id) {
			// 
			
			// thats the call out to the parent class
			dependencyCheckers[processID].check(  id );
			// checkDependencyIndex( dvxItem, id , processID) ;
			
		}

		@Override
		public int getClosedStatus() {
			 
			return closed;
		}
		
		
		
	} // DScreenerDigester
	// ========================================================================
	
	
	
	// ========================================================================
		class DScreenerObserver implements Runnable{
	
			Thread screenobsThrd ;
			boolean observerIsRunning = false;
			public int gcWaiting=0;
			
			// ................................................
			public DScreenerObserver(){
			
				screenobsThrd = new Thread(this, "screenobsThrd"); 
			}
			// ................................................
			
			public void start(){
				screenobsThrd.start() ;
			}
			@Override
			public void run() {
				perform();
			}
			
			private void perform(){
				observerIsRunning = true;
				int gcc=0, remainingItems=0 ;
				ArrayList<AnalyticFunctionSpriteImprovement> cbeusList = new ArrayList<AnalyticFunctionSpriteImprovement>();
				ArrayList<AnalyticFunctionSpriteImprovement> beus; 
				AnalyticFunctionSpriteImprovement funcImp ;
				
				try{
					
					while (observerIsRunning) {
						int z=0;
						try {
							// z=3000 = waiting 5 minutes, which roughly means 3-5000 checks
							while ((z<1000) && (observerIsRunning) && (stopScreening==false)){
								Thread.sleep(100); z++;
								
								if ((gcc==0) && (gcWaiting>0)){
									gcc=1;
									System.gc();
									gcWaiting--;
									if (gcWaiting<0)gcWaiting=0;
									gcc=0;
								}

								if (z%100==0){
									remainingItems = dcWorking();
								}
						}
							
						} catch (Exception e) {
							continue;
						}
	
						
						if (observerIsRunning==false){
							break;
						}
						out.delay(150);
						remainingItems = dcWorking();
						if (remainingItems==0){
							while (dcWorking()==0){
								for (int i = 0; i < dependencyCheckers.length; i++) {
									dependencyCheckers[i].dcheckIsRunning = false;
								}
							}
							observerIsRunning=false;
						}
						
						if (bestEstimatedUtilitiesList.size() > 0) {
							int beuN = bestEstimatedUtilitiesList.get(0).size();
							if (beuN > 100) {
								// maxEstimatedImprovement
															out.print(2, "");
								cbeusList.clear() ;
								for (int i=0;i<bestEstimatedUtilitiesList.size();i++){
									beus = bestEstimatedUtilitiesList.get(i);
									cbeusList.addAll( beus );
								}
	
								Collections.sort(cbeusList, new euComparable(-1));
								if (cbeusList.size()>0){
									funcImp = cbeusList.get(0);
									double eu = funcImp.estimatedImprovement ;
									out.print(2, "observing screening, best expected estimated improvement : "+String.format("%.3f", eu)) ;
									
									if (cbeusList.size()>500){
										int k = Math.min(cbeusList.size()/10, 1000);
										thresholdEu = cbeusList.get(k).estimatedImprovement ;
									}
								}
								
	if (cbeusList.size()>10000){
		stopScreening = true;
	}
								if (stopScreening){
									if (dsd!=null){
										dsd.closed=1; // this will close all internal threads
										dsd.digester.close(); // throws a poison pill into the processes
									}
									out.delay(500);
									
									
									while (dsd.digester.getRunningThreadsCount()>0){
										out.delay(100);
										dsd.digester.stopAll();
									}
									
									observerIsRunning=false;
								}
							} // anything registered
						} // anything calculated ?
					} // observerIsRunning -> 
					
					
					
				}catch(Exception e){
				} // try
				
			} // run
			
			
		}

	class AssociationDifferential implements TimeAnchorIntf{
				
				ClassificationSettings cls ;
				int delayed=0;
				
				public AssociationDifferential(){
					
				}

				@Override
				public void redirection( long timeDelay) {
					
					out.print(2, " <<<<<<<<<<<<<<<<<<<< time delay threshold exceeded ("+timeDelay+" ms)...") ;
					delayed++;
				}

				@SuppressWarnings("unchecked")
				public ArrayList<AnalyticFunctionSpriteImprovement> check( 	int v1index, int v2index, 
																			double[][] depVarValues, 
																			Evaluator evaluator ,
																			MannWhitneyUTest mwu , 
																			SpearmansCorrelation spc ){
					double v1,v2,fv,fmax ,fmin , vesti;  
					int nex , knnClusterCount = 2,targetMode ;
					String expressionName, expressionStr;
					double[] measure;
					double[][] tgd ;
					Object resultObj;
					ArrayList<Double> fvalues = new ArrayList<Double> ();
					ArrayList<ArrayList<Double>> resultVectors = new ArrayList<ArrayList<Double>>();
					ArrayList<Double> rvec ;
					
					ArrayList<IndexTuple> candidateImprovements = new ArrayList<IndexTuple> ();
					IndexTuple ixtup;
					
					AnalyticFunctionSpriteImprovement pimp; 
					ArrayList<AnalyticFunctionSpriteImprovement> potentialImprovements = new ArrayList<AnalyticFunctionSpriteImprovement>() ;
					
					
					
					cls = somSprite.modelingSettings.getClassifySettings();
					
					targetMode = cls.getTargetMode();
					tgd = cls.getTargetGroupDefinition() ;
					
					if ((tgd==null) || 
						( (tgd.length==0) && (targetMode>=1))){
						// get the empirical description
						
					}
					v1=0;
															/*
															out.setTimeThresholdingAnchor( (TimeAnchorIntf)this);
															out.setTimeThresholding(true,900);
															*/
															out.resetTimeThresholding();
															delayed=0;
															out.print(5, "\n\n------------------------------\n. ");
															
					
															 
															
					// all expressions
					nex = evaluator.xList.size() ;
					 
													if (out!=null)out.print(4,true,"...");
					for (int f=0;f<nex;f++){
															out.print(5, "dT (15)\n func <"+f+"> "); out.resetTimeThresholding() ;
						expressionName = evaluator.getExpressionName(f);
													if (out!=null){
														out.print(4, "expr = "+expressionName+"  for variables: "+v1index+","+v2index);
													}
		 
													
						fmax = -1.0 ;fmin=9999999999.9;
						
						// here a further loop that is handling cohort parameters... down till the end of the f-loop 
						// set a boolean
						if (evaluator.functions.containsKey(expressionName)){
							// Object obj = evaluator.functions.get(expressionName) ;
							//SpriteFuncIntf func = (SpriteFuncIntf)obj;
							//FunctionCohortParameterSet cps = func.getCohortParameterSet() ;
							
						}
															out.print(5, "dT (17) "); out.resetTimeThresholding() ;
						resultVectors.clear();
															out.print(5, "dT (18) "); 
						// calculating all values for given variables (v1index,v2index) and selected function f
						for (int i=0;i<depVarValues[0].length;i++){
																													out.resetTimeThresholding();
							v1 = depVarValues[0][i];
							v2 = depVarValues[1][i];
						
							// we easily may have a further param, ... it is a dynamic param
							// if chohortParamIsPresent -> including v3
							// else
							fv = SpriteFuncIntf.__MISSING_VALUE;
							
							if ((v1!=-1.0) && (v2!=-1.0)){
								resultObj = evaluator.eval(expressionName, v1,v2) ;   
															out.print(5, "dT (20) "); out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (20) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< func = "+expressionName+" i:"+i+" ,  variables: "+v1index+","+v2index); delayed=0;
															};
								// functions like log() automatically turn to type "Complex" for results if the argument (like a+b in log(a+b) ) is negative 
							}else{
								resultObj = -1.0 ;
							}
							
							
							if (resultObj instanceof ArrayList<?>){
								fv = ((ArrayList<Double>)resultObj).get(0) ;
								fvalues = new ArrayList<Double>( ((ArrayList<Double>)resultObj) );
								resultVectors.add( fvalues );
								// effectively, this builds up a dynamic table
							}else{
								try{
									fvalues = new ArrayList<Double> ();
									
									if (resultObj!=null){
										String cn = resultObj.getClass().getSimpleName();

										if (cn.startsWith("Double")) {
											fv = (Double) resultObj;
											// we change to ArrayList with 1 member,
											// such we can always use a loop below
											fvalues.add(fv);
										} else {
											fv = -1.0;
										}
										resultVectors.add(fvalues);
									}
								}catch(Exception e){
									String cn =""; 
									if (resultObj!=null){
										cn = resultObj.getClass().getSimpleName() ;
									}
									String emsg = "Problem in DependencyScreener (expression: "+expressionName+") when attempting to convert "+cn+" into (Double); func index : "+f+" ;  value index i : "+i+" ; v1index,v2index : "+v1index+", "+ v2index ;

									System.err.println(emsg); // in AssociationDifferential.check()
									e.printStackTrace();
								}
							
							}
							 
						
						} // i-> all values
															
						/*
						 * each result vector contains either 1 value (standard, no cohort function) or
						 * many values, resulting from the array of values provided for one of the variables;
						 * 
						 * effectively, we have to deal with a dynamic table comprising vn columns
						 * 
						 * length of the table = length of "resultVectors" = length of "depVarValues[3]"
						 */
						// k = resultVectors.size() - depVarValues[3].length;
						int vn = resultVectors.get(0).size(); // = size of fvalues: # of positions we have to traverse
															out.print(5, "dT (25) \n v/size = "+vn);
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (25) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															};
															
															out.resetTimeThresholding() ;
						for (int v=0;v<vn;v++){
															
							// for col v of the table "resultVectors", we refill depVarValues[3] with the respective values
							for (int rv=0;rv<resultVectors.size();rv++){
								
								rvec = resultVectors.get(rv);
								if (v<rvec.size()){
									fv = rvec.get(v);
									if (fv > 0) {
										depVarValues[3][rv] = fv;
										if (fv != SpriteFuncIntf.__MISSING_VALUE) {
											if (fmax < fv) fmax = fv;
											if (fmin > fv) fmin = fv;
										}
									}
								}else{
									// mv condition... no result vector for this cluster in this function
									// out.print(2, "disregarded: V1: "+v1index+", V2: "+v2index+" ,  result vector rv: a"+rv+", item v: "+v);
								}
							} // rv->
							if ((fmax==fmin) || (fmax<=0.0) || (fmax-fmin==0.0)){
								continue;
							}                     
															
															out.print(5, "dT (28) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (28) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															}; 
							// normalize; ???
							
							for (int i=0;i<depVarValues[0].length;i++){
								fv = depVarValues[3][i] ;
								
								fv = (fv-fmin)/(fmax-fmin) ;
								depVarValues[3][i] = fv ;
							}
															 out.print(5, "dT (29) ");out.resetTimeThresholding() ;
															 if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (29) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															 };
							measure = new double[4] ;
							// now the statistics ...
							// 1. Mann-Whitney ...
							measure[0] = calculateAlignments(depVarValues, mwu);
															out.print(5, "dT (30) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (30) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< func = "+f+" , expression = "+expressionName);delayed=0;
															};
							// 2. correlation
							measure[1] = calculateCorCovar(depVarValues, spc );
															out.print(5, "dT (31) "); out.resetTimeThresholding() ;
															if (delayed>0){  
																out.resetTimeThresholding() ; out.print(2, ".              dT (31) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< func = "+f+" , expression = "+expressionName);delayed=0;
															}; 
							// 3. interpreting columns as instances of vectors, calculating distance
							measure[2] = calculateVectorSim(depVarValues);
							
															out.print(5, "dT (32) ");out.resetTimeThresholding() ;
															if (delayed>0){  
																out.resetTimeThresholding() ; out.print(2, ".              dT (32) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< func = "+f+" , expression = "+expressionName);delayed=0;
															} ;
							// 4. in case of multi-group, "stickyness" of groups as measured by "inverse ppv" 
						
							measure[3] = 0.0;
							
							
							
							
							if (targetMode == ClassificationSettings._TARGETMODE_MULTI){
								// in this case, the improvement of the association needs to be measured by 
								// means of bivariate clustering; the improvement measure reflects the change 
								// of the ratio of variance intra vs inter (center) vs total  
								// we use simple knn, controlled by numbers of cluster from TV (or reasonably chosen
								
								// cluster count = group count 
								if ((tgd!=null) && (tgd.length>1)){
									knnClusterCount = (int) (4 + (2+ Math.log(3.8 + 2*(tgd.length-1)))) ;
								}else{
									knnClusterCount = 3;
								}
								
							}else{
								if ((tgd!=null) && (tgd.length>1)){
									knnClusterCount = 4 + (2*(tgd.length-1)) ;
								}else{
									knnClusterCount = 3;
								}
							}
		
							// if there are too few records for this cluster size, we have to reduce iteratively
							/*
							while (((1+depVarValues[0].length)/(1+knnClusterCount)) <9){
								knnClusterCount = (int) (knnClusterCount *0.8);
							}
							*/
							// this is not ready to use yet
							// measure[4] = calculateMultiGroupAlignment( depVarValues , knnClusterCount );
							
							
							// dependent on the target mode (1 group or many, discrete or corr), 
							// we apply different weightings for the measure
							vesti = estimateImprovement( targetMode, measure ) ;
															out.print(5, "dT (35) ");out.resetTimeThresholding() ;
															if (delayed>0){  
																out.resetTimeThresholding() ; out.print(2, ".              dT (35) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															} ;
		if (vesti>1000){
		 
			vesti=2.2;
		}
							ixtup = new IndexTuple(vesti, f,v);
							candidateImprovements.add( ixtup ) ;
													if (out!=null)out.print(4, "   - - - > estimated improvement (f,p:"+f+","+v+"): "+String.format("%.3f",vesti));
															out.print(5, "dT (38) ");
							measure=null;
							
						} // all columns in dynamic table for the current expression
						 
						
					} // f-> all expressions
					
					
					// we select the top-2 maximum for the imported pair of variables
					
					ArrayList<IndexTuple> ixtups = getBestCandidates( candidateImprovements );
					
															if (delayed>0) out.print(2, "dT (40) ");
					
					if ((ixtups!=null) && (ixtups.size()>0)){
															out.print(5, "dT (41), ixtups = "+ixtups.size());
						for (int ixt=0;ixt<2;ixt++){
							ixtup = ixtups.get(ixt);
		
							int fs = ixtup.indexes[0];
															out.print(5, "dT (42) ");
							expressionName = evaluator.getExpressionName(fs);
							expressionStr = evaluator.getExpression(fs);
							
							pimp = new AnalyticFunctionSpriteImprovement (v1index, v2index , ixtup.value);
							pimp.setFunctions( ixtup.indexes[0],ixtup.indexes[1] );
							pimp.setExpressionName( expressionName );
							pimp.setExpression( expressionStr );
							
							potentialImprovements.add(pimp) ;
															out.print(5, "dT (43) ");
							
						} // ixt->
					}
					
					// we return the selection 
					ixtups.clear();
					ixtups=null;
					candidateImprovements.clear() ;
					candidateImprovements=null;
															out.print(5, "dT (48) ");
															out.setTimeThresholding(false,500);
					return potentialImprovements ;
				}
		
				
				private ArrayList<IndexTuple> getBestCandidates( ArrayList<IndexTuple> candidates ) {
					IndexTuple itm,item = null;
					
					ArrayList<IndexTuple> items = new ArrayList<IndexTuple>();
					double iv ;
					
					double min=99999999999.09, max = -1.0;
					
					// int n = candidates.size();
					 
			        int z=0 ;
			        for(int i=0;i<candidates.size();i++){
		
			        	item = candidates.get(i) ;
			        	iv = item.value ;
			        	
			            if (min>iv)min=iv;
			            if (max<iv){
			            	max=iv;   
			            	
			            	itm = new IndexTuple(item);
			            	items.add(0, itm); // we need to create a new instance as a copy
			            	
			            	
			            } else{
			            	if (items.size()<=1){
			            		items.add(new IndexTuple(item));
			            	}else{
			            		if ((items.size()>=2) && (iv> items.get(1).value)){
				            		items.add(1, new IndexTuple(item));
				            	}
			            	}
			            }
			            
			            if (items.size()>5){
		            		items.remove(items.size()-1) ;
		            	}
			            z++; // System.out.println("Key :"+key+"  value :"+value);
			        }
					 
					return items;
				}
		
				
				private double estimateImprovement( int tm, double[] measure) {
					 
					double msum ;
					double v,estImp =0.0;
					int z=0;
					msum = 0.0;
					
					for (int i=0;i<=2;i++){
						
						v = measure[i] ;
						if ((v>=0) && ( Double.isNaN(v)==false )){
							msum = msum + v;
							z++;
						}
					}
					
					if (z>0){
						estImp = msum/((double)z) ;
					}
					
					return estImp ;
				}
		
				private double calculateAlignments(double[][] depVarValues, MannWhitneyUTest mwu) {
					
					double rating = 0;
					double[] ms = new double[3] ;
					// 
					// Class WilcoxonSignedRankTestImpl
					// http://commons.apache.org/math/userguide/stat.html#a1.4_Simple_regression
					// http://www.jsc.nildram.co.uk/api/index.html
				
					// TestUtils.pairedTTest(sample1, sample2, alpha)
					// int n = depVarValues[0].length;
					 
					// MannWhitneyUTest mwu = new MannWhitneyUTest();
					// this creates the value for the U-statistic
					// ms[0] = mwu.mannWhitneyU( depVarValues[0], depVarValues[2]) ;
					
					
					try {
						
															out.print(5, "dT (300) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (300) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															};
						// mwu.setReferenceData(depVarValues[2]) ;
						depVarValues[0] = mwu.ensureDataConformance( depVarValues[0] );
						depVarValues[1] = mwu.ensureDataConformance( depVarValues[1] );
						depVarValues[3] = mwu.ensureDataConformance( depVarValues[3] );
						depVarValues[2] = mwu.ensureDataConformance( depVarValues[2] );
															
															out.print(5, "dT (301) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (301) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															};

						
						ms[0] = mwu.mannWhitneyUTest( depVarValues[0], depVarValues[2]) ;
															
															out.print(5, "dT (302) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (302) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															};
						
						ms[1] = mwu.mannWhitneyUTest( depVarValues[1], depVarValues[2]) ;
															
															out.print(5, "dT (303) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (303) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															};
						
						ms[2] = mwu.mannWhitneyUTest( depVarValues[3], depVarValues[2]) ;
															
															out.print(5, "dT (304) ");out.resetTimeThresholding() ;
															if (delayed>0){ 
																out.resetTimeThresholding() ; out.print(2, ".              dT (304) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");delayed=0;
															};

						
						
						if ((ms[2]>0.9) && (ms[2]>ms[0]) && (ms[2]>ms[1]) && ((ms[1]>0.001 || ms[0]>0.001))){
							rating = (ms[2]*ms[2]+ms[2])/(ms[1]+ms[0]) ;
						} else{
							rating = (1.0+ms[2])/(1.0+Math.max( ms[0], ms[1]));
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					ms=null;
					return rating;
				}
		
				/**
				 * 
				 * actually, not the correlation is interesting, but the residuals are even more interesting
				 * 
				 * @param depVarValues
				 * @return
				 */
				private double calculateCorCovar( double[][] depVarValues , SpearmansCorrelation sc) {
					double rating = 0.0, cRatio, scValueV1, scValueV2, scValueVV, maxREV;
					
					double[][] mdata ;
					RealMatrix covMatrix ;
					
					
					
					scValueV1 = sc.correlation( depVarValues[0], depVarValues[2]) ;
					scValueV2 = sc.correlation( depVarValues[1], depVarValues[2]) ;
					
					scValueVV = sc.correlation( depVarValues[3], depVarValues[2]) ;
					
					if ((scValueVV>0.9) && (scValueVV> scValueV1) && (scValueVV> scValueV2)){
						cRatio = (scValueVV*scValueVV+scValueVV)/(scValueV2 + scValueV1) ;
					} else{
						cRatio = (1.0+scValueVV)/(1.0+Math.max( scValueV1, scValueV2));
					}
					if (Double.isNaN(cRatio)){
						cRatio = 0.1 ;
					}
					// cRatio = scValueV1/scValueV1;
						
					// remove col 3,4
					mdata = somSprite.arrutil.changeDimensions( depVarValues, 0, 3, 0); // double[][] values, int dimension, int newSize, int offset
					mdata = somSprite.arrutil.arrayTranspose(mdata) ;
					
					Covariance cov = new Covariance(mdata) ;
					
					covMatrix = cov.getCovarianceMatrix();
					
					EigenDecomposition ceigen = new EigenDecompositionImpl( covMatrix, 0.01) ; 
					double[] realEigenValues = ceigen.getRealEigenvalues() ;
					// double[] imagEigenValues = ceigen.getImagEigenvalues() ; 
					
					maxREV = somSprite.arrutil.arrayMax( realEigenValues ) ;
					// double maxRIV = somSprite.arrutil.arrayMax( imagEigenValues ) ;
					
					rating = cRatio * (1.0+ maxREV/10.0) ;
					
					ceigen=null ;
					sc = null ;
					mdata = null ;
					
					return rating;
				}
		
				 
				private double calculateVectorSim(double[][] depVarValues) {
					
					double dsimR = 0.0, s1,s2 ;
					
					SimilarityCalculator simcalcV1,simcalcV2;
					ArrayList<Double> v1,v2,ztv;
					
					v1  = somSprite.arrutil.changeArrayStyle( depVarValues[0] );
					v2  = somSprite.arrutil.changeArrayStyle( depVarValues[1] );
					ztv = somSprite.arrutil.changeArrayStyle( depVarValues[2] );
					
					simcalcV1 = new SimilarityCalculator(v1,ztv);
					simcalcV2 = new SimilarityCalculator(v2,ztv);
					
					s1 = simcalcV1.calc();
					s2 = simcalcV2.calc();
					
					if (s1>0){
						dsimR = s2/s1 ;
					}
					
					simcalcV1 = null  ;
					simcalcV2 = null  ; v1.clear();v2.clear();ztv.clear();
					
					return dsimR;
				}
		
				/**
				 * 
				 * "stickyness" of groups as measured by "inverse ppv"
				 * 
				 * "inverse ppv" : 
				 *   -  uses TV as variable and measures the variance of the not-uses variable per cluster
				 *      this should be significantly & consistently smaller for v2
				 *   -  cluster count = target group count
				 *   
				 *                  
				 * if we cluster for instance (A) records defined by {v1-TV} then v2 should be associated more close to TV,
				 * if we cluster (B) v2-TV then alas, and additionally, the ppv for all groups should be better than in case (A)  
				 * 
				 * 
				 * 
				 * again, two columns
				 * 
				 * @param depVarValues
				 * @return
				 */
				public double calculateMultiGroupAlignment(double[][] depVarValues , int knnClusterCount) {
					double rating = 0.0;
					int n, cc;
					int numClusters = 4 ; // default for simple target, 1 target group
					
					ClusterNode node;
					ArrayList<ClusterNode> nodes = new ArrayList<ClusterNode>() ;
					
					EuclideanDoublePoint[] points ;
					
					
					kMeans.setInactiveColumns() ; 
					kMeans.setMissingValue( -1.0 ) ;  
					kMeans.useNormalizedData( true ) ;     
					
					points = new EuclideanDoublePoint[ depVarValues[0].length ]; 
					
					// length not correct...
					for (int i=0;i<depVarValues[0].length;i++){
					
						
						points[i] = new EuclideanDoublePoint(new double[] { depVarValues[0][i],depVarValues[1][i], depVarValues[2][i]});	
					}// i->
					
				 	
					int iterations = Math.min(14, Math.max(4,depVarValues[0].length/3));
					
					// calculating
					kMeans.setUseIndicator( new int[]{1,1,0}); // exclude tv
					List<Cluster<EuclideanDoublePoint>> v1v2clusters = kMeans.cluster( Arrays.asList(points), 
																				   	   knnClusterCount, 
																				   	   iterations );
					
					kMeans.setUseIndicator( new int[]{1,0,1}); // exclude tv
					List<Cluster<EuclideanDoublePoint>> v1tvclusters = kMeans.cluster( Arrays.asList(points), 
							   														   knnClusterCount, 
							   														   iterations );
					kMeans.setUseIndicator( new int[]{0,1,1}); // exclude tv
					List<Cluster<EuclideanDoublePoint>> v2tvclusters = kMeans.cluster( Arrays.asList(points), 
							   														   knnClusterCount, 
							   														   iterations );
					
					// is there any kind of anisotropy across the clusters: TV #%, association v1-v2, v1-tv, v2-tv
					// checking clusters: size, coverage of target variable, relative risk + ppv 
					n = v1v2clusters.size() ;
					cc = v1v2clusters.size() ;
					
					int n1 = v1tvclusters.size() ;
					int n2 = v2tvclusters.size() ;
					
					// transferring the node data into our basic node structure
					for (int i=0;i<cc;i++){
						node = new ClusterNode(i+1) ;
						
						
						nodes.add(node) ;
					}
					 
					
					return rating ;
				}
				
			} // inner class

	public class euComparable implements Comparator<AnalyticFunctionSpriteImprovement>{
		 
		int sortdirection = 1;
		
		public euComparable( int direction){
			sortdirection = direction;
		}
		public euComparable(){
			sortdirection = 1;
		}
		
	    @Override
	    public int compare(AnalyticFunctionSpriteImprovement item1, AnalyticFunctionSpriteImprovement item2) {
	    	int result=0;
	    	
	    	if (item1.estimatedImprovement < item2.estimatedImprovement){
	    		if (sortdirection>0){
	    			result = -1;
	    		}else{
	    			result = 1;
	    		}
	    	}else{
	    		if (item1.estimatedImprovement > item2.estimatedImprovement){
	    			if (sortdirection>0){
		    			result = 1;
		    		}else{
		    			result = -1;
		    		}
		    	}	
	    	}
	    	
	        return result;
	    }
	}
	
	private void removeKnownpreviousProposals() {
		 
		
		AnalyticFunctionSpriteImprovement fs ;
		
		if (somSprite.previousProposals==null){
			return;
		}
		
		int n = somSprite.previousProposals.size() ;
		if (n==0){
			return;
		}
		
		int i=bestEstimatedUtilities.size()-1;
		while(i>=0){
			
			fs = bestEstimatedUtilities.get(i) ;
			if (fs.getExpressionName().length()==0){ 
				bestEstimatedUtilities.remove(i); 
				i--;
				continue;
			}
			
			if (somSprite.isProposalKnown(fs)){
				bestEstimatedUtilities.remove(i);
			}
			 
			i--;
		} // while ->
		i=0;
	}
	private void reduceListOfCandidates( ArrayList<AnalyticFunctionSpriteImprovement> items) {
		
		AnalyticFunctionSpriteImprovement item, coitem;
		int n,i,lastN=-1;
		boolean hb;
		
		i = items.size()-1;
		
		// 1. drop all bad ones
		while (i>=0){
			if (items.get(i).estimatedImprovement<1.5){
				items.remove(i) ;
			}
			i--;
		}

		// 2. remove multiple entries for a particular pair of variables, 
		//    keeping only 1 or 2, dependent on the total size (thus we need a meta-loop)
		items.trimToSize() ;
		n = items.size();
		int nFromSamePair = 3;
		int nFromSameLabelFunc = 2;
		int nOnSameVar = Math.max(1, nFromSamePair-1);
		
		while ((items.size()>dCountThreshold) && (nFromSamePair>0)){
 			i=0;
			// note that the list is sorted...
 			for (int k = 0; k < items.size()-1; k++) {
 				
 				item = items.get(k) ;
 				
 				if (item.estimatedImprovement<0){
 					continue;
 				}
 				int idPairCount=0;
 				int[] idBaseVarCount= new int[3];
 				
 				for (int j = k+1; j < items.size(); j++) {
 					coitem = items.get(j) ;
 					hb=true;
 					
 					// ?? drop it ?
 					if ((item.varIndex1==coitem.varIndex1) && (item.varIndex2==coitem.varIndex2)){
 						idPairCount++;
 					}else{
						if (item.varIndex1 == coitem.varIndex1) {
							idBaseVarCount[0]++;
						}
						if (item.varIndex2 == coitem.varIndex2) {
							idBaseVarCount[1]++;
						}
						if ( ((item.varIndex1 == coitem.varIndex1) ||  (item.varIndex2 == coitem.varIndex2)) &&
							 (item.funcIndex1 == coitem.funcIndex1)){
							idBaseVarCount[2]++;
						}
 					}
 					
 					hb = (idPairCount<nFromSamePair) && 
 					     (idBaseVarCount[0]<=nOnSameVar+2) && (idBaseVarCount[1]<=nOnSameVar+2) &&
 					     ((idBaseVarCount[2]< nFromSameLabelFunc)); 
 					
 					if (hb==false){
 						coitem.estimatedImprovement = -1.01 ;
 					}
 				} // j-> i+k -> all
 				
 				
			} // i->

			// 3. drop all those for which we have reduced the estimated improvement
			i = items.size()-1;
			while (i>=0){
				item = items.get(i);
				if ((item.estimatedImprovement<1.5) ){
					items.remove(i) ;
				}
				i--;
			}
			n = items.size(); if (lastN==n){break;} ; lastN=n;
			if (nFromSamePair>=2){
				nFromSamePair--;
			}else{
				
			}
 		} // items count > 5 (threshold)
		
		n = items.size();
		
		// 4. absolute limitation of list to "dCountThreshold"
		while (items.size()>dCountThreshold){
			items.remove(dCountThreshold) ;
		}
		
		
		items.trimToSize() ;
	}
	
	
	public AnalyticFunctionTransformationsIntf getListOfCandidatePairs() { // ArrayList<AnalyticFunctionSpriteImprovement> 
		
		AnalyticFunctionTransformations afCandidates = new AnalyticFunctionTransformations();
		
		// ArrayList<AnalyticFunctionSpriteImprovement> candidates ;
		
		afCandidates.items = new ArrayList<AnalyticFunctionSpriteImprovement>(bestEstimatedUtilities);
		
		return afCandidates;
	}
	
	

	public int getdCountThreshold() {
		return dCountThreshold;
	}

	public void setdCountThreshold(int dCountThreshold) {
		this.dCountThreshold = dCountThreshold;
	}


	public int getLimitCountForExploredCombination() {
		return limitCountForExploredCombination;
	}


	public void setLimitCountForExploredCombination(int limitCountForExploredCombination) {
		this.limitCountForExploredCombination = limitCountForExploredCombination;
	}
	

}

package org.NooLab.somsprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.NooLab.math3.stat.clustering.Cluster;
import org.NooLab.math3.stat.clustering.EuclideanDoublePoint;
import org.NooLab.math3.stat.clustering.EuclideanIntegerPoint;
import org.NooLab.math3.stat.clustering.KMeansPlusPlusClusterer;
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
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.Covariance;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
// import org.apache.mahout.clustering.fuzzykmeans.*;
 

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
	
	SomMapTable somMapTable;
	Evaluator evaluator;
	
	IndexedDistances ixdPair = new IndexedDistances();
	
	// this "EuclideanDoublePoint" knows by itself how to calculate the distance
	KMeansPlusPlusClusterer<EuclideanDoublePoint> kMeans ;
	// TODO: we use kmeans to estimate the match in multi-target case
	
	ArrayList<AnalyticFunctionSpriteImprovement> bestEstimatedUtilities = new ArrayList<AnalyticFunctionSpriteImprovement>();
	ArrayList<AnalyticFunctionSpriteImprovement> knownTransformations;
	
	int dCountThreshold = 5;
	
	PrintLog out ;
	
	// ========================================================================
	public DependencyScreener(SomSprite somsprite, SomMapTable smt, Evaluator ev) {
		
		somSprite = somsprite;
		somMapTable = smt;
		evaluator = ev;
		
		kMeans = new KMeansPlusPlusClusterer<EuclideanDoublePoint>( (new Random(1234)) );
		
		
		out = somSprite.out;
	}
	// ========================================================================	
	
	public void go(){
	
		double dr;
		String str,varLabel;
		int[] depVarIndex ;
		String[] depVar = new String[3];
		double[][] depVarValues = new double[5][somMapTable.values.length];
		int fix1,fix2,ix0,ix1;
		
		Variables variables;
		ArrayList<int[]> depVarIndexes = new ArrayList<int[]>() ;
		
		AnalyticFunctionSpriteImprovement euItem;
		ArrayList<AnalyticFunctionSpriteImprovement> estimatedUtils ;
		
		
		somData = somSprite.somData;
		variables = somData.getVariables() ;
		
		
		// TODO first get known transformations and affected derived variables, such that we can 
		//      prevent repeated suggestion of the same transformation 
		
		 
		knownTransformations = variables.getKnownTransformations();
		
		
		// 
		int tvindex = somMapTable.tvIndex ;

		try{
			
			// we separate the tasks in a very trivial way;
			// yet, this supports parallel processing of the list
			dr=0;
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
					depVarIndexes.add(depVarIndex) ;
					
				} 
			} // b->
			
			
			// TODO: this could be multi-threaded easily...
			for (int i=0;i<depVarIndexes.size();i++){
											
				depVarIndex = depVarIndexes.get(i) ;
				
				depVar[0] = somMapTable.variables[ depVarIndex[0] ];
				depVar[1] = somMapTable.variables[ depVarIndex[1] ];
											
											if ((i==0) || (i%10==0)){
												int dmode = somSprite.sfProperties.getShowSomProgressMode();
												if (dmode >= SomFluidProperties._SOMDISPLAY_PROGRESS_PERC){
													out.print(2, "screening for dependencies ("+(i)+" of "+depVarIndexes.size()+") in variables ("+depVarIndex[0]+","+depVarIndex[1]+")..." );
												}else{
													if (i==0){
														out.print(2, "screening for dependencies (n="+depVarIndexes.size()+")..." );
													}
												}
											}
				int va = depVarIndex[0];
				int vb = depVarIndex[1];
				
				// get columns
				for (int r=0;r<somMapTable.values.length;r++){
					// get column a
					depVarValues[0][r] = somMapTable.values[r][va] ;
					// get column b
					depVarValues[1][r] = somMapTable.values[r][vb] ;
					// get column b
					depVarValues[2][r] = somMapTable.values[r][somMapTable.tvIndex] ;
					// TODO: we need also a binarized version of the TV, into 2 groups or n groups
					
				} // all rows in extracted columns
				
				// this returns a relative improvement of the association
				AssociationDifferential ad = new AssociationDifferential();
				estimatedUtils = ad.check(va,vb,depVarValues);
				
				if ((estimatedUtils!=null) && (estimatedUtils.size()>0)){
					bestEstimatedUtilities.addAll(estimatedUtils) ;
				}
				
			} // i-> all pairs
											
											
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
				         fix1 = euItem.funcIndex1 ; fix2 = euItem.funcIndex2 ;

				expressionName = evaluator.getExpression(euItem.funcIndex1); 
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
				if ((fs.funcIndex1<0) || (fs.getExpression().length()==0)){
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
		dr=0; 
	}

	
	private void removeKnownpreviousProposals() {
		 
		boolean hb;
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
			if (fs.getExpression().length()==0){ 
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
	
	
	class AssociationDifferential{
		
		ClassificationSettings cls ;
		
		public AssociationDifferential(){
			
			
		}
		
		@SuppressWarnings("unchecked")
		public ArrayList<AnalyticFunctionSpriteImprovement> check( int v1index, int v2index, double[][] depVarValues ){
			
			double v1,v2,fv,fmax ,fmin , vesti;
			int nex , knnClusterCount = 2,targetMode,k;
			String expressionName;
			double[] measure;
			double[][] tgd ;
			Object resultObj;
			ArrayList<Double> fvalues = new ArrayList<Double> ();
			ArrayList<ArrayList<Double>> resultVectors = new ArrayList<ArrayList<Double>>();
			
			ArrayList<IndexTuple> candidateImprovements = new ArrayList<IndexTuple> ();
			IndexTuple ixtup;
			
			AnalyticFunctionSpriteImprovement pimp; 
			ArrayList<AnalyticFunctionSpriteImprovement> potentialImprovements = new ArrayList<AnalyticFunctionSpriteImprovement>() ;
			FunctionCohortParameterSet cps ; 
			
			cls = somSprite.modelingSettings.getClassifySettings();
			
			targetMode = cls.getTargetMode();
			tgd = cls.getTargetGroupDefinition() ;
			
			if ((tgd==null) || 
				( (tgd.length==0) && (targetMode>=1))){
				// get the empirical description
				
			}
			v1=0;
			
			// all expressions
			nex = evaluator.xList.size() ;
			 
											if (out!=null)out.print(4,true,"...");
			for (int f=0;f<nex;f++){

				expressionName = evaluator.getExpression(f);
											if (out!=null)out.print(4, "expr = "+expressionName+"  for variables: "+v1index+","+v2index);
if ((expressionName.contains("logist2x")) ||
	(expressionName.contains("logist3")) 
	){
	k=0;
}
				fmax = -1.0 ;fmin=9999999999.9;
				
				// here a further loop that is handling cohort parameters... down till the end of the f-loop 
				// set a boolean
				if (evaluator.functions.containsKey(expressionName)){
					Object obj = evaluator.functions.get(expressionName) ;
					SpriteFuncIntf func = (SpriteFuncIntf)obj;
					cps = func.getCohortParameterSet() ;
					
				}
				resultVectors.clear();
				
				// calculating all values for given variables (v1index,v2index) and selected function f
				for (int i=0;i<depVarValues[0].length;i++){
				
					v1 = depVarValues[0][i];
					v2 = depVarValues[1][i];
				
					// we easily may have a further param, ... it is a dynamic param
					// if chohortParamIsPresent -> includin v3
					// else
					fv = SpriteFuncIntf.__MISSING_VALUE;
					
					resultObj = evaluator.eval(expressionName, v1,v2) ;
					
					
					if (resultObj instanceof ArrayList<?>){
						fv = ((ArrayList<Double>)resultObj).get(0) ;
						fvalues = new ArrayList<Double>( ((ArrayList<Double>)resultObj) );
						resultVectors.add( fvalues );
						// effectively, this build up a dynamic table
					}else{
						fvalues = new ArrayList<Double> ();
						fv = (Double)resultObj;
						// we change to ArrayList with 1 member, such we can always use a loop below
						fvalues.add(fv);
						resultVectors.add( fvalues );
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
				k = resultVectors.size() - depVarValues[3].length;
				int vn = resultVectors.get(0).size(); // = size of fvalues: # of positions we have to traverse
				
				for (int v=0;v<vn;v++){
					k=0;
					// for col v of the table "resultVectors", we refill depVarValues[3] with the respective values
					for (int rv=0;rv<resultVectors.size();rv++){
						
						fv = resultVectors.get(rv).get(v) ;
						
						depVarValues[3][rv] = fv ;
						if (fv != SpriteFuncIntf.__MISSING_VALUE){
							if (fmax<fv )fmax = fv;
							if (fmin>fv )fmin = fv;
						}
						
					} // rv->
					if ((fmax==fmin) || (fmax<=0.0) || (fmax-fmin==0.0)){
						continue;
					}
					
					// normalize; ???
					
					for (int i=0;i<depVarValues[0].length;i++){
						fv = depVarValues[3][i] ;
						
						fv = (fv-fmin)/(fmax-fmin) ;
						
						depVarValues[3][i] = fv ;
					}
					measure = new double[4] ;
					// now the statistics ...
					// 1. Mann-Whitney ...
					measure[0] = calculateAlignments(depVarValues);
					
					// 2. correlation
					measure[1] = calculateCorCovar(depVarValues);
					
					// 3. interpreting columns as instances of vectors, calculating distance
					measure[2] = calculateVectorSim(depVarValues);
					
					
					// 4. in case of multi-group, "stickyness" of groups as measured by "inverse ppv" 
				
					measure[3] = 0.0;
					
					
					
					
					if (targetMode == ClassificationSettings._TARGETMODE_MULTI){
						// in this case, the improvement of the association needs to be measured by 
						// means of bivariate clustering; the improvement measure reflects the change 
						// of the ratio of variance intera vs inter (center) vs total  
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

					// if there are too few records for this clustersize, we have to reduce iteratively
					while (((1+depVarValues[0].length)/(1+knnClusterCount)) <9){
						knnClusterCount = (int) (knnClusterCount *0.8);
					}
					
					// this is not ready to use yet
					// measure[4] = calculateMultiGroupAlignment( depVarValues , knnClusterCount );
					
					
					// dependent on the target mode (1 group or many, discrete or corr), 
					// we apply different weightings for the measure
					vesti = estimateImprovement( targetMode, measure ) ;
if (vesti>1000){
	k=0;
	vesti=2.2;
}
					ixtup = new IndexTuple(vesti, f,v);
					candidateImprovements.add( ixtup ) ;
											if (out!=null)out.print(4, "   - - - > estimated improvement (f,p:"+f+","+v+"): "+String.format("%.3f",vesti));
				} // all columns in dynamic table for the current expression
				 
				
			} // f-> all expressions
			
			
			// we select the top-2 maximum for the imported pair of variables
			
			ArrayList<IndexTuple> ixtups = getBestCandidates( candidateImprovements );
			
					
			
			if ((ixtups!=null) && (ixtups.size()>0)){
				
				for (int ixt=0;ixt<2;ixt++){
					ixtup = ixtups.get(ixt);

					int fs = ixtup.indexes[0];
					
					expressionName = evaluator.getExpression(fs);
					
					pimp = new AnalyticFunctionSpriteImprovement (v1index, v2index , ixtup.value);
					pimp.setFunctions( ixtup.indexes[0],ixtup.indexes[1] );
					
					potentialImprovements.add(pimp) ;
					
				} // ixt->
			}
			
			// we return the selection 
			
			return potentialImprovements ;
		}

		
		private ArrayList<IndexTuple> getBestCandidates( ArrayList<IndexTuple> candidates ) {
			IndexTuple itm,item = null;
			
			ArrayList<IndexTuple> items = new ArrayList<IndexTuple>();
			double iv ;
			
			double min=99999999999.09, max = -1.0;
			
			int n = candidates.size();
			 
	        int z=0, zz=0 ;
	        for(int i=0;i<candidates.size();i++){

	        	item = candidates.get(i) ;
	        	iv = item.value ;
	        	
	            if (min>iv)min=iv;
	            if (max<iv){
	            	max=iv;  zz=z;
	            	
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

		private double calculateAlignments(double[][] depVarValues) {
			
			double rating = 0;
			double[] ms = new double[3] ;
			// 
			// Class WilcoxonSignedRankTestImpl
			// http://commons.apache.org/math/userguide/stat.html#a1.4_Simple_regression
			// http://www.jsc.nildram.co.uk/api/index.html
		
			// TestUtils.pairedTTest(sample1, sample2, alpha)
			int n = depVarValues[0].length;
			n=n+1-1;
			MannWhitneyUTest mwu = new MannWhitneyUTest();
			// this creates the value for the U-statistic
			// ms[0] = mwu.mannWhitneyU( depVarValues[0], depVarValues[2]) ;
			
			
			try {
				ms[0] = mwu.mannWhitneyUTest( depVarValues[0], depVarValues[2]) ;
				ms[1] = mwu.mannWhitneyUTest( depVarValues[1], depVarValues[2]) ;
				ms[2] = mwu.mannWhitneyUTest( depVarValues[3], depVarValues[2]) ;
			
				if ((ms[2]>0.9) && (ms[2]>ms[0]) && (ms[2]>ms[1]) && ((ms[1]>0.001 || ms[0]>0.001))){
					rating = (ms[2]*ms[2]+ms[2])/(ms[1]+ms[0]) ;
				} else{
					rating = (1.0+ms[2])/(1.0+Math.max( ms[0], ms[1]));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return rating;
		}

		/**
		 * 
		 * actually, not the correlation is interesting, but the residuals are even more interesting
		 * 
		 * @param depVarValues
		 * @return
		 */
		private double calculateCorCovar( double[][] depVarValues ) {
			double rating = 0.0, cRatio, scValueV1, scValueV2, scValueVV, maxREV, maxRIV;
			double[] ms = new double[3] ;
			double[][] mdata, tdata ;
			RealMatrix covMatrix ;
			
			SpearmansCorrelation sc = new SpearmansCorrelation();
			
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
			mdata = somSprite.arrutil.changeDimensions( depVarValues, 0, 3, 0);
			mdata = somSprite.arrutil.arrayTranspose(mdata) ;
			
			Covariance cov = new Covariance(mdata) ;
			
			covMatrix = cov.getCovarianceMatrix();
			
			EigenDecomposition ceigen = new EigenDecompositionImpl( covMatrix, 0.01) ; 
			double[] realEigenValues = ceigen.getRealEigenvalues() ;
			double[] imagEigenValues = ceigen.getImagEigenvalues() ; 
			
			maxREV = somSprite.arrutil.arrayMax( realEigenValues ) ;
			maxRIV = somSprite.arrutil.arrayMax( imagEigenValues ) ;
			
			rating = cRatio * (1.0+ maxREV/10.0) ;
			
			return rating;
		}

		 
		private double calculateVectorSim(double[][] depVarValues) {
			
			double dsimR = 0.0, s1,s2 ;
			double[][] tdata;
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
		private double calculateMultiGroupAlignment(double[][] depVarValues , int knnClusterCount) {
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
	// ..............................................................
	 
	

	public AnalyticFunctionTransformationsIntf getListOfCandidatePairs() { // ArrayList<AnalyticFunctionSpriteImprovement> 
		
		AnalyticFunctionTransformations afCandidates = new AnalyticFunctionTransformations();
		
		ArrayList<AnalyticFunctionSpriteImprovement> candidates ;
		
		afCandidates.items = new ArrayList<AnalyticFunctionSpriteImprovement>(bestEstimatedUtilities);
		
		return afCandidates;
	}

	public int getdCountThreshold() {
		return dCountThreshold;
	}

	public void setdCountThreshold(int dCountThreshold) {
		this.dCountThreshold = dCountThreshold;
	}
	

}

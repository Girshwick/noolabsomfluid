package org.NooLab.somfluid.core.categories.similarity;
 

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


 
/**
 * 
 * this is defined on the level of the VirtualLattice; </br>
 * from there, it is imported as reference into the nodes by  "similarity = importSimilarityConcepts()" </br> </br>
 * 
 * This means that the useage vector can be set upfront, quite early in the process, and
 * the calculation of similarity always can assume aligned vectors. </br>
 * 
 * We also need an abstraction from the actual core concept in the similarity function
 * 
 */
public class Similarity implements 
									// content, parameters, such like useIntensity, that could be specific for a particular node
									// 
									SimilarityIntf,
									// content-free methods, outsourced for implementational convenience (later perhaps as plugin)
									SimilarityCore{

	int similarityType = _SIMDIST_ADVSHAPE ;
	
	ArrayList<Double> useIntensity = new ArrayList<Double>();
	ArrayList<Double> usageIndicationVector = new ArrayList<Double>();
	ArrayList<Double> blacklistIndicationVector = new ArrayList<Double>();
	
	
	ArrayList<Integer> excludedVariableIndexes = new ArrayList<Integer>();
	
	int indexIdColumn = -1;
	int indexTargetVariable = -1;
	
	// ===========
	public Similarity(){
		
	}
	public Similarity( Similarity inSimilarity) {

		useIntensity = new ArrayList<Double>();
		
		if (inSimilarity.useIntensity!=null) useIntensity.addAll( inSimilarity.useIntensity) ;
		
		if (inSimilarity.usageIndicationVector!=null) usageIndicationVector.addAll( inSimilarity.usageIndicationVector );
		
		if (inSimilarity.blacklistIndicationVector!=null) blacklistIndicationVector.addAll( inSimilarity.blacklistIndicationVector );
		
		
		excludedVariableIndexes = new ArrayList<Integer>() ;
		if (inSimilarity.excludedVariableIndexes!=null) excludedVariableIndexes.addAll( inSimilarity.excludedVariableIndexes ) ;
		
		indexIdColumn = inSimilarity.getIndexIdColumn();
		indexTargetVariable = inSimilarity.indexTargetVariable ;
		
		
	}
	// ========================================================================
	public void clear(){
		if (useIntensity!=null){
			useIntensity.clear();
			usageIndicationVector.clear();
			blacklistIndicationVector.clear();
			excludedVariableIndexes.clear();
			
		}
	}
	
	public void close(){
		clear();
		useIntensity = null ;
		usageIndicationVector = null ;
		blacklistIndicationVector = null ;
		excludedVariableIndexes = null ;
		
	}
	// ------------------------------------------------------------------------
	 
	@Override
	public void adjustLengthOfUsageIndicationVector(int newsize) {
 
		
		int uvn = usageIndicationVector.size() ;
		
		if (uvn<newsize){
			for (int i=0;i<(newsize-uvn);i++){
				usageIndicationVector.add(0.0) ;
				 
			}
		}
		if (uvn>newsize){
			for (int i=0;i<(uvn-newsize);i++){
				if (newsize < usageIndicationVector.size()){
					usageIndicationVector.remove(newsize) ;
				}
			}
		}
		
		usageIndicationVector.trimToSize() ;
		
		isoSizingVectors( blacklistIndicationVector,0.0 );
		isoSizingVectors( useIntensity ,0.5);
	}
	
	private void isoSizingVectors( ArrayList<Double> tVector, double dval ){
		int uvn, bvn ; 
		
		uvn = usageIndicationVector.size();
		bvn = tVector.size() ;
		
		if (uvn>bvn){
			for (int i=0;i<(uvn-bvn);i++){
				tVector.add(dval);
			}
		}
		if (uvn<bvn){
			for (int i=0;i<(bvn-uvn);i++){
				tVector.remove(uvn) ; 
			}
		}
		tVector.trimToSize() ;
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void setBlacklistIndicationVector(int[] blacklistPositions) {

		
		// Collection cL = Arrays.asList(ArrayUtils.toObject(blacklistPositions));
		// DON'T DO THIS!!! it will store the vlaues as Integer obj in the Double object,
		// any later usage will throw a cast exception (Integer -> Double)
		
		if ((blacklistPositions==null) || (blacklistPositions.length==0)){
			return;
		}
		
		blacklistIndicationVector = new ArrayList<Double>();
		
		for (int i=0;i<blacklistPositions.length;i++){
			blacklistIndicationVector.add( blacklistPositions[i]*1.0 ) ;
		}
		
	}


	public void setExcludedColumns( ArrayList<Integer> indexes){
		
		excludedVariableIndexes = new ArrayList<Integer>(indexes);
	}
	
	/** 
	 * this flavor requires a globally constant setting of the usagevector and the weight vector, which
	 * is usually available in SOM modeling
	 */
	@Override
	public double similarityWithinDomain( ArrayList<Double> vector1, // should be the data from the nodes intensional profile vector
										  ArrayList<Double> vector2, boolean suppressSQRT) {
		// useIntensity
		SimilarityCalculator sc;
		  
		sc = (new SimilarityCalculator(vector1,vector2));
		sc.suppressSQRT = suppressSQRT;
		sc.indexIdColumn = indexIdColumn;
		sc.indexTargetVariable = indexTargetVariable;
		
		return sc.calc(usageIndicationVector ); // using global useIntensity
	}

	@Override
	public double similarityWithinDomain( ArrayList<Double> vector1,
										  ArrayList<Double> vector2, 
										  ArrayList<Double> useIntensity,
										  boolean suppressSQRT) {
		 
		return (new SimilarityCalculator(vector1,vector2)).calc(useIntensity); // using imported useIntensity
		
	}

	/**  
	 * here, we have to align the vectors first before we can calculate the similarity,
	 * else, this siilarity has two components: set-sim + feature-sim 
	 */
	@Override
	public double similarityAcrossDomains( ArrayList<Double> vector1,
										   ArrayList<Double> vector2) {
		
		return 0;
	}

	@Override
	public void setSimilarityFunctions() {
		
		
	}

	@Override
	public void getListofSimilarityFunctions() {
		
		
	}

	
	
	public int getSimilarityType() {
		return similarityType;
	}


	public void setSimilarityType(int similarityType) {
		this.similarityType = similarityType;
	}


	public ArrayList<Integer> getExcludedVariableIndexes() {
		return excludedVariableIndexes;
	}


	public void setExcludedVariableIndexes( ArrayList<Integer> excludedVariableIndexes) {
		this.excludedVariableIndexes = excludedVariableIndexes;
	}


	public int getIndexIdColumn() {
		return indexIdColumn;
	}


	public void setIndexIdColumn(int indexIdColumn) {
		this.indexIdColumn = indexIdColumn;
	}


	public int getIndexTargetVariable() {
		return indexTargetVariable;
	}


	public void setIndexTargetVariable(int indexTV) {
		this.indexTargetVariable = indexTV;
	}


	public ArrayList<Double> getUseIntensity() {
		return useIntensity;
	}


	public void setUseIntensity(ArrayList<Double> useIntensity) {
		this.useIntensity = useIntensity;
	}


	@Override
	public int[] getUseIndicatorArray() {
		int [] usearr = new int[usageIndicationVector.size()] ;
		
		for (int i=0;i<usearr.length;i++){
			if (usageIndicationVector.get(i)>0.0){
				usearr[i] = 1;
				if (i==indexTargetVariable){
					usearr[i] = -2;
				}
			}
			 
		}
		return usearr;
	}
	
	public ArrayList<Double> getUsageIndicationVector() {
		return usageIndicationVector;
	}

	
	@Override
	public void setUsageIndicationVector(int[] usagevector) {
		// ArrayList<Double>
		usageIndicationVector = new ArrayList<Double>();
		
		for (int i=0;i<usagevector.length;i++){
			usageIndicationVector.add( (double)(1.0*usagevector[i]) ) ;
		}
		// this does not work, despite the values are put into the array, underneath it 
		// remains integer, and later (next read op) a ClassCastException will be thrown!!!!
		// Collection cL = Arrays.asList(ArrayUtils.toObject(usagevector));
		// usageIndicationVector = new ArrayList<Double>( cL );
	}


	public void setUsageIndicationVector(ArrayList<Double> usageIndicationVector) {
		
		this.usageIndicationVector = usageIndicationVector;
	}

	
	public ArrayList<Double> getBlacklistIndicationVector() {
		return blacklistIndicationVector;
	}


	public void setBlacklistIndicationVector( ArrayList<Double> blacklistIndicationVector) {
		this.blacklistIndicationVector = blacklistIndicationVector;
	}


	 
	
	
	
}

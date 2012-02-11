package org.NooLab.somfluid.core.categories.similarity;

import java.util.ArrayList;

public interface SimilarityIntf {
 
	public static final int _SIMDIST_ADVSHAPE  = 1;
	public static final int _SIMDIST_EUCLID    = 2;
	
	/** 
	 * this version assumes that the two vectors are from the same domain, where a domain is 
	 * defined by an arbitrarily, yet fixed, ordered set of variables.
	 * it is further assumed that this sort is the same for both vectors.
	 * Advantage: pretty fast ;
	 * there is an additoinal parameter, which allows to suppress taking the SQRT. Determining the BMU in
	 * a list of nodes or a list of records returns the same result, but is faster. Only the final comparison,
	 * and the determination of the influence to the neighborhood requires taking the SQRT
	 * 
	 * the global usevector (binary indication whether to use a variable or not) will be considered,
	 * the weight of the values = contribution to similarity is 1 for all positions.
	 * 
	 *  
	 * @param vector1 data, expected to be from [0..1]
	 * @param vector2 data, expected to be from [0..1]
	 * @param suppressSQRT boolean flag
	 */
	public double similarityWithinDomain( ArrayList<Double> vector1, ArrayList<Double> vector2, boolean suppressSQRT);

	/**
	 * 
	 * like its sibling;
	 * the difference is that the useIntensity is explicitly transmitted.
	 * 
	 * the weight of the values = contribution to similarity is from [0..1] for all positions,
	 * i.e. it scales between 0 (do not consider for simularity calculation) to 1 (use as is);
	 * any other value in between is weighting of the value.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param useIntensity
	 * @param suppressSQRT
	 */
	public double similarityWithinDomain( ArrayList<Double> vector1, ArrayList<Double> vector2, 
										  ArrayList<Double> useIntensity, boolean suppressSQRT);
	
	
	
	/**
	 * this version of the similarity calculation does not need aligned vectors, or vectors of the same length.
	 * field-wise matching is done with regards to the variable labels
	 * 
	 * Additional to the geometric/analytic measure, the difference of the sets (e.g. following Tversky) are 
	 * considered to calculate the similarity
	 * 
	 */
	public double similarityAcrossDomains( ArrayList<Double> vector1, ArrayList<Double> vector2) ;
	
	
	
	public void setSimilarityFunctions(); // by index

	public void getListofSimilarityFunctions(); // by indexed objects 
	
	
	
	// ------------------------------------------------------------------------
	
	

	
	public int getSimilarityType() ;

	public void setSimilarityType(int similarityType)  ;


	public ArrayList<Integer> getExcludedVariableIndexes() ;

	public void setExcludedVariableIndexes( ArrayList<Integer> excludedVariableIndexes) ;


	public int getIndexIdColumn() ;

	public void setIndexIdColumn(int indexIdColumn) ;


	public int getIndexTargetVariable() ;

	public void setIndexTargetVariable(int indexTargetVariable) ;

	
	public void setUsageIndicationVector(int[] usagevector);
	public void setUsageIndicationVector(ArrayList<Double> usageIndicationVector) ;
	
	public ArrayList<Double> getUsageIndicationVector() ;
	
	
	public void setBlacklistIndicationVector(int[] blacklistPositions) ;
	public void setBlacklistIndicationVector( ArrayList<Double> blacklistIndicationVector) ;
	 
	public ArrayList<Double> getBlacklistIndicationVector() ;
	
	
	
	
}

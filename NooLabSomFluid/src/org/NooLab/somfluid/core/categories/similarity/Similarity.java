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
	
	ArrayList<Double> useIntensity ;
	ArrayList<Double> usageIndicationVector = new ArrayList<Double>();
	ArrayList<Double> blacklistIndicationVector = new ArrayList<Double>();
	
	
	ArrayList<Integer> excludedVariableIndexes;
	
	int indexIdColumn = -1;
	int indexTargetVariable = -1;
	
	
	public Similarity(){
		
	}
	
	 
	@Override
	public void setUsageIndicationVector(int[] usagevector) {
		// ArrayList<Double>
		usageIndicationVector = new ArrayList<Double>();
		
		for (int i=0;i<usagevector.length;i++){
			usageIndicationVector.add( (double)(1.0*usagevector[i]) ) ;
		}
		// this does not work, underneath it remains integer, and later (next read op) a ClassCastException will be thrown!
		// Collection cL = Arrays.asList(ArrayUtils.toObject(usagevector));
		// usageIndicationVector = new ArrayList<Double>( cL );
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void setBlacklistIndicationVector(int[] blacklistPositions) {

		
		Collection cL = Arrays.asList(ArrayUtils.toObject(blacklistPositions));
		blacklistIndicationVector = new ArrayList<Double>( cL );	
	}


	public void setExcludedColumns( ArrayList<Integer> indexes){
		
		excludedVariableIndexes = new ArrayList<Integer>(indexes);
	}
	
	/** 
	 * this flavor requires a globall yconstant setting of the usagevector and the weight vector, which
	 * is usually available in SOM modeling
	 */
	@Override
	public double similarityWithinDomain( ArrayList<Double> vector1, // should be the data from the nodes intensional profile vector
										  ArrayList<Double> vector2, boolean suppressSQRT) {
		// useIntensity
		return (new SimilarityCalculator(vector1,vector2)).calc(usageIndicationVector ); // using global useIntensity
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


	class SimilarityCalculator{
		
		ArrayList<Double> vector1, vector2 ;
		
		public SimilarityCalculator( ArrayList<Double> _vector1, ArrayList<Double> _vector2 ){
			
			vector1 = _vector1;
			vector2 = _vector2;
		}

		public double calc(ArrayList<Double> useIntensity) {
			
			double d ;
			
			d = advancedDistance( vector1,vector2, useIntensity ) ;
			return d;
		}

		public double calc() {
			
			double d ;
			//ArrayList<Double> useIntensity = new ArrayList( Arrays.asList( ));
			
			d = advancedDistance( vector1,vector2, useIntensity ) ;
			return d;
		}

		private double advancedDistance( ArrayList<Double> vector1,
										 ArrayList<Double> vector2, ArrayList<Double> useIntensity) {

			if (vector1.size() != vector2.size()) {
				System.out.println( "Error! vector1.length (" + vector1.size()+""+
									") <> vectorsize (" + vector2.size() + ")");
				return -1;
			}
			double c, d, d0, df, ic1 = 0, ic2 = 0, iq;
			int i,u=0, z, distanceMeth, fvp=-1;

			distanceMeth = 2;
			d = 0;
			z = 0;
			d0 = 0;

			for (i = 0; i < vector2.size(); i++) {

				if ((i < useIntensity.size()) ) {
					double ui = useIntensity.get(i);
					if (ui <= 0.0){
						u++;
						continue;
					}
				}
				if (fvp<0){fvp=i;}
				if ((vector1.get(i) < 0.0) || (vector2.get(i) < 0.0)) {
					if ((vector1.get(i) > -4) && (vector2.get(i) > -4)
							&& (i != indexIdColumn)
							&& (i != indexTargetVariable)) {
						d = d + 0.6;
					}
				} else {
					c = vector1.get(i) - vector2.get(i);

					d = d + c * c;
					if (c > 0.5) {
						c = c + ((Math.sqrt(c - 0.5)) * 0.812);
						if (c > 1) {
							c = 1;
						}
					}
					d0 = d0 + Math.abs(c);
					z = z + 1;

					if (distanceMeth >= 2) {
						c = 0;
						if (i < vector2.size() - 1) {
							ic1 = vector1.get(i) - vector1.get(i + 1);
							ic2 = vector2.get(i) - vector2.get(i + 1);
						} else {
							ic1 = vector1.get(i) - vector1.get(fvp); // TODO not 0, but first non-Index-column  id
																	
																	 
																	
							ic2 = vector2.get(i) - vector2.get(fvp);
						}
						c = Math.abs(ic1 - ic2);

						if (ic2 != 0) {
							iq = ic1 / ic2;
							if (iq < 0) {
								c = c + Math.sqrt(c / 2);
								if (c > 0.5) {
									c = 0.5;
								}
							}
						}

					} // distanceMeth>=2
					d = d + 0.3 * Math.abs(c);
				}
			}

			df = d / z + 0.2 * (d0 / z);
			return df;
		}
		
	} // inner class SimilarityCalculator
	
	
	
}

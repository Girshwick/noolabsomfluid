package org.NooLab.somfluid.core.categories.similarity;

import java.util.ArrayList;




public class Similarity implements SimilarityIntf{

	@Override
	public double similarityWithinDomain( ArrayList<Double> vector1,
										  ArrayList<Double> vector2, boolean suppressSQRT) {
		 
		return 0;
	}

	@Override
	public double similarityWithinDomain( ArrayList<Double> vector1,
										  ArrayList<Double> vector2, 
										  ArrayList<Double> useIntensity,
										  boolean suppressSQRT) {
		 
		return 0;
	}

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

}

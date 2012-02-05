package org.NooLab.somfluid.core.categories.intensionality;

import java.util.ArrayList;


/**
 * 
 * note that this is NOT what usually is called "weight vector" in numerous publications.
 * Actually, the common use is a misnomer. 
 * What usually is called weight vector we call "Profile Vector".
 * The weight vector is indeed NOT referring to the values that are used to
 * determine the BMU. The weights are the proportions (=weights) of the 
 * contribution of each of the variables in a vector to the similarity
 * 
 * 
 */
public class WeightVector {

	ArrayList<Double> values = new ArrayList<Double> ();
	
	public WeightVector(){
		
	}
}

package org.NooLab.somfluid.components;

import java.util.ArrayList;

import org.NooLab.somfluid.data.Variables;


/**
 * exploring the question: "is it possible to build a comparatively good model without the top-rated variables"
 * 
 * schedule of removal for used is based on score from :
 * 
 * - high evocount
 * - high contrast
 * 
 * 
 * 
 * variables are blacklisted
 */
public class CanonicalExploration {

	ModelOptimizer modelOptimizer;
	ArrayList<String> originalBlacklist ;
	Variables variables;
	
	// ========================================================================
	public CanonicalExploration(ModelOptimizer modopti) {

		modelOptimizer = modopti;
		originalBlacklist = new ArrayList<String>(variables.getBlacklistLabels());
		
	}
	// ========================================================================
	
	
	
}

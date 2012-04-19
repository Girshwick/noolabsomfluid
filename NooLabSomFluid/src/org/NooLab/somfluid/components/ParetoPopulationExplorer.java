package org.NooLab.somfluid.components;

import org.NooLab.somscreen.EvoBasics;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.SomQuality;


/**
 * 
 * describing models on the level of whole metrices
 * 
 * - which variables are counteracting ?
 * - which variables are replacements of each other ?
 *   -> is it possible to combine them for a good model ?
 *   
 * - is it possible to combine top models into bags?
 *   - deriving a meta model taking the individual SOMs as measurement devices = null models ?  
 * 
 * 
 * 
 * 
 * 
 */
public class ParetoPopulationExplorer {

	
	ModelOptimizer moptiParent ;
	
	SomDataObject somData ; 
	
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;
	
	
	/**
	 * 
	 * determines the alpha/beta trade-off,
	 * calculates a neg exponential fitting
	 * 
	 * then observing the change in structure between alpha-beta preference
	 * for those models
	 * 
	 * describing the asymmetry of models of a given preference when switching to its mirror
	 * as a distance to the pareto frontier
	 * 
	 * @param mopti
	 */
	public ParetoPopulationExplorer(ModelOptimizer mopti) {

		moptiParent = mopti ;
		
		evoBasics = mopti.evoBasics;
		evoMetrices = mopti.evoMetrices;
		somQuality = mopti.somQuality;
		
		somData = mopti.getSomDataObj() ;

	}

	public void explore() {

		
	}

}

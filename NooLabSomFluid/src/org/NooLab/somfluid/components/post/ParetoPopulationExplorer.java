package org.NooLab.somfluid.components.post;

import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
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

	transient OutResults outresult;
	
	
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
		
		evoBasics = mopti.getEvoBasics();
		evoMetrices = mopti.getEvoMetrices();
		somQuality = mopti.getSomQuality();
		
		somData = mopti.getSomDataObj() ;

		outresult = moptiParent.getOutresult() ;
	}

	public void explore() {

		
	}

}

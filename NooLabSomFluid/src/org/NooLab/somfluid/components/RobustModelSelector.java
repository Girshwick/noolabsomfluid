package org.NooLab.somfluid.components;

import org.NooLab.somfluid.components.ModelOptimizer.OptimizerProcess;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.EvoMetrik;

 



/**
 * from a top N subset from all metrices, we select the most robust one, where "robustness"
 * is defined by change of predictivity dependent on 
 * - removing/exchanging single variables
 * 
 *  
 * 
 *
 */
public class RobustModelSelector{

	ModelOptimizer modelOptimizer ;
	
	SomDataObject somData ;
	EvoMetrices evoMetrices; 
	
	
	// ========================================================================
	public RobustModelSelector(ModelOptimizer modOpti){
		modelOptimizer = modOpti ;
		
		somData = modelOptimizer.somDataObj ;
		evoMetrices = modelOptimizer.evoMetrices ;
		
	}
	// ========================================================================
	
	
	
	public void isSamplingIncluded(boolean checkingSamplingRobustness) {
		// 
		
	}



	public void setTopNSubsetSize(int i) {
		// TODO Auto-generated method stub
		
	}



	public EvoMetrik getBest() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


} // inner class RobustModelSelector




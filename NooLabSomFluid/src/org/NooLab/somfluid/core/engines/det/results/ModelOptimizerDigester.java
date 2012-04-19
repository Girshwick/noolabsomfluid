package org.NooLab.somfluid.core.engines.det.results;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;




public class ModelOptimizerDigester  extends SomResultHandlerAbs {


	ModelOptimizer moz;
	
	
	// ========================================================================
	public ModelOptimizerDigester(  ModelOptimizer moz, SomFluidFactory factory) {//){
		super( moz, factory );
		this.moz = moz;
	}
	// ========================================================================

	

	public void handlingResults(){
		
		if (moz.getEvoMetrices()==null){
			return;
		}
		out.printErr(1, "handling results of model optimizer...");
		
		
		int en = moz.getEvoMetrices().getEvmItems().size();
		
		out.print(2, en+ " models have been calculated."); // best one, coverage, non-linearity, comparison to PCA
		// moz.getSpelaResults() ;
		
	}
	
	
}

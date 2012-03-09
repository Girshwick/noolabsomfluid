package org.NooLab.somscreen;

import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * This class determines the relevance weights of variables by means of
 * an evolutionary screening;
 * The weight of a variable represents the probability that this variable
 * improves the quality of a model. 
 * 
 * The most important property is that the resulting feature selection is NOT 
 * assuming anything about linearity of the model 
 * 
 * the search for an appropriate model ceate many models that are almost of identical power,
 * but of different structure.
 * As a consequence, a meta-reasoning about variables becomes possible, describing their
 * structural contribution to the model;
 * 
 * - contribution to discrimination of a variable within a model
 * - degree of non-linearity
 * - degee of saliency: milieu variable, or active variable
 * - detection of pseudo targets
 * 
 * the search may be accelerated by partitioning the data (limiting the size of the bags), both, per
 * variable and per record
 *
 * after the pre-last com phase, i.e. in the creative phase before mere clustering, we could apply a 
 * PCA / eigenvalue decompoision across the profiles (with or without refering to the usevector),
 * in order to infer the main components 
 *
 */
public class SomScreening {

	public static final int _SEL_TOP = 1;
	public static final int _SEL_DIVERSE = 2;

	DSom dSom;
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	
	SomQuality somQuality;
	int totalSelectionSize;
	int[] selectionMode;
	
	SomTargetedModeling somTargetedModeling;
	PrintLog out;
	
	
	// ========================================================================
	public SomScreening(DSom dsom, ModelingSettings modset) {
	
		dSom = dsom;
		modelingSettings = modset ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		somTargetedModeling = dSom.getEmbeddingInstance();
		
		out = dSom.getOut() ;
	}
	// ========================================================================
	
	
	public void startScreening() {
		
	}

	public void setModelResultSelection(int[] selectionmode) {
		// TODO Auto-generated method stub
		selectionMode = selectionmode ;
	}


	public void setModelResultSelectionSize(int selsize) {
		
		totalSelectionSize = selsize ;
	}

	
	/**
	 * 
	 * this class performs the following steps
	 * 
	 * 1. checking the quality of the initial model, e.g. refering to target variable and validation
	 * 2. creating a few variations
	 * 3. running modeling on it, by "cloning" somTargetedModeling
	 *    yet, we need a new DSom instance !! -> just transferring settings
	 * 
	 */
	class EvolutionarySearch{
		
		SomQuality somquality;
		
		public EvolutionarySearch(){
			
			
		}
		
		
		/**
		 * in the basic variant "everything" remains the same, except the use vector
		 * we also take a copy of the dsom, and of the initialized lattice
		 * 
		 */
		private void calculateSpecifiedModel( double[] usevector){
			
			SomTargetedModeling targetMod;
			
			targetMod = new SomTargetedModeling( somTargetedModeling );
			
			targetMod.init();
			
			targetMod.perform();
		}
		
	} // inner class EvolutionarySearch
}


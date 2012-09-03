package org.NooLab.somfluid.components.post;

import java.util.ArrayList;

import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * the influence of the potential resolution as the maximum number of nodes onto the quality of model
 *  
 * the max number of clusters changes : up 3x, at least 100, down 3
 * 
 * 1. metrics remains constant,
 * 2. metrics get optimized in a small number of steps (stopping if more than 20 models did not improve)   
 * 
 * 
 *
 */
public class Coarseness {

	transient SomHostIntf somHost;
	transient ModelOptimizer modelOptimizer ;
	
	transient SomDataObject somData ;
	private EvoMetrices evoMetrices; 
	
	ArrayList<String> baseVariableSelection ;
	 
	
	// ..........................................
	
	/** // n different resolutions */
	int numberOfScalePoints = 7; 
	
	/**    */
	int ccountInfimum = 5;
	
	/**    */
	int ccountSupremum = 150;
	
	
	// ..........................................
	transient OutResults outresult;
	
	transient private PrintLog out;

	// ========================================================================
	public Coarseness(ModelOptimizer moptiParent) {
		
		modelOptimizer = moptiParent;
		somHost = (SomHostIntf)modelOptimizer ;
		somData = somHost.getSomDataObj() ;
		
		evoMetrices = new EvoMetrices(modelOptimizer, 0);
		
		outresult = modelOptimizer.getOutresult() ;
		out = modelOptimizer.getOut() ;
	}
	// ========================================================================

	
	public void setBaseVariableSelection( ArrayList<String> varSelection){
		baseVariableSelection = new ArrayList<String>(varSelection);
	}
	
	
	public void check(){
		
	}


	public void evaluate() {
		//  

		
		outresult.createDiagnosticsReport(this);

	}


	public ArrayList<String> getBaseVariableSelection() {
		return baseVariableSelection;
	}


	public int getNumberOfScalePoints() {
		return numberOfScalePoints;
	}


	public void setNumberOfScalePoints(int numberOfScalePoints) {
		this.numberOfScalePoints = numberOfScalePoints;
	}


	public int getCcountInfimum() {
		return ccountInfimum;
	}


	public void setCcountInfimum(int ccountInfimum) {
		this.ccountInfimum = ccountInfimum;
	}


	public int getCcountSupremum() {
		return ccountSupremum;
	}


	public void setCcountSupremum(int ccountSupremum) {
		this.ccountSupremum = ccountSupremum;
	}


	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}


	public void setEvoMetrices(EvoMetrices evoMetrices) {
		this.evoMetrices = evoMetrices;
	}
	
	
	
	
}

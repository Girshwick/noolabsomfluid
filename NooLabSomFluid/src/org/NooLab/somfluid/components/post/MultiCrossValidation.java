package org.NooLab.somfluid.components.post;

import java.util.ArrayList;

import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somscreen.EvoMetrices;

public class MultiCrossValidation {

	ModelOptimizer modelOptimizer ;
	
	SomDataObject somData ;
	EvoMetrices evoMetrices;

	private ArrayList<String> baseVariableSelection; 
	
	boolean isCalculated = false;
	
	OutResults outresult;
	
	
	// ========================================================================
	public MultiCrossValidation(ModelOptimizer moptiParent) {
		modelOptimizer = moptiParent;
		outresult = modelOptimizer.getOutresult() ;
		outresult.createDiagnosticsReport(this);
	}
	// ========================================================================



	public void perform() {
		// TODO Auto-generated method stub
		
		isCalculated = false;
	}



	public void setBaseVariableSelection(ArrayList<String> varSelection) {
		baseVariableSelection = new ArrayList<String>(varSelection);
	}



	public boolean isCalculated() {
		return isCalculated;
	}



	public void setCalculated(boolean isCalculated) {
		this.isCalculated = isCalculated;
	}
	
	
	
	
}

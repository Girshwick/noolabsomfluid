package org.NooLab.somfluid.components.post;

import java.util.ArrayList;

import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.SomModelDescription;
 
import org.NooLab.somscreen.EvoBasics;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.EvoMetrik;
import org.NooLab.utilities.logging.PrintLog;

 



/**
 * from a top N subset from all metrices, we select the most robust one, where "robustness"
 * is defined by change of predictivity dependent on 
 * 
 * - removing/exchanging single variables
 * - changing resolution
 * - changing sample 
 * 
 * note that is indicated to use surrogate extension of the data !! 
 *  
 * the result is expressed as a relative score 1+ 
 * this score describes the capability to generalize
 * 
 * upon the list of most robust models, the dependencies are checked 
 *
 */
public class RobustModelSelector{

	ModelOptimizer modelOptimizer ;
	
	SomDataObject somData ;
	EvoMetrices evoMetrices; 
	
	SomModelDescription modelDescription;
	MultiCrossValidation multiCrossValidation ;
	
	ArrayList<String> baseVariableSelection ;
	
	transient OutResults outresult;
	
	transient private PrintLog out;
	
	
	
	// ========================================================================
	public RobustModelSelector(ModelOptimizer modOpti){
		modelOptimizer = modOpti ;
		
		somData = modelOptimizer.getSomDataObj() ;
		evoMetrices = modelOptimizer.getEvoMetrices() ;
		
		outresult = modelOptimizer.getOutresult() ;
		out = modelOptimizer.getOut() ;
	}
	// ========================================================================
	
	
	
	public void isSamplingIncluded(boolean checkingSamplingRobustness) {
		// 
		
	}



	public void setTopNSubsetSize(int mcount) {
		// 
		
	}

	public void setBaseVariableSelection( ArrayList<String> varSelection){
		baseVariableSelection = new ArrayList<String>(varSelection);
	}

	public EvoMetrik getBest() {
		// TODO Auto-generated method stub
		
		return null;
	}

	

	public void check() {
		//  
		EvoBasics _evoBasics = new EvoBasics();
		
		modelDescription = new SomModelDescription( modelOptimizer );
		modelDescription.setInitialVariableSelection( baseVariableSelection  ) ;
		modelDescription.calculate() ;
		
		outresult.createDiagnosticsReport(modelDescription);
		
		// integrate these ...  :  evoMetrices = modelOptimizer.evoMetrices !!
		EvoMetrices _evoMetrices = modelDescription.getVariableContributions().getEvometrices() ;
		evoMetrices = _evoBasics.integrateEvoMetricHistories( evoMetrices, _evoMetrices, 1) ;
		
		// metrics remains constant, based on different samplings
		multiCrossValidation = new MultiCrossValidation( modelOptimizer );
		multiCrossValidation.setBaseVariableSelection( baseVariableSelection  ) ;
		multiCrossValidation.perform();
		// TODO: we again should collect the metrices, though into a different container, and
		//       indicating the sample
		
		
		
		
	}




	// ------------------------------------------------------------------------

	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}



	public SomModelDescription getModelDescription() {
		return modelDescription;
	}



	public MultiCrossValidation getMultiCrossValidation() {
		return multiCrossValidation;
	}
	
	
	


} // inner class RobustModelSelector




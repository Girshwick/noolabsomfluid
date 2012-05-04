package org.NooLab.somfluid.components;

import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.utilities.logging.PrintLog;

public class OutResults {

	
	transient ModelOptimizer modelOptimizer ;
	transient SomDataObject somData ;
	
	transient SomFluidProperties sfProperties ;
	OutputSettings outputSettings ;
	ModelingSettings modelingSettings;
	
	EvoMetrices evoMetrices ;
	
	
	
	transient private PrintLog out;
	private String historyTableAsString;
	
	
	// ========================================================================
	public OutResults(ModelOptimizer modOpti, SomFluidProperties sfprops) {

		modelOptimizer = modOpti;
		sfProperties = sfprops;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		outputSettings = sfProperties.getOutputSettings() ;
		
		somData = modelOptimizer.getSomDataObj() ; 
		
		evoMetrices = modelOptimizer.evoMetrices ;
		
		out = modelOptimizer.out ;
		
	}
	// ========================================================================
	
	
	public void createModelOptimizerReport() {
		
		// String xstr = xmlReport.toString();
		// sfFactory.publishReport(xstr);
		
		// str = evoMetrices.toString();
		// out.print(2, "explored metrices (all): \n"+str+"\n") ;
		
		// sort the evo metrices
		String rstr ;
		
		// TODO table header, rocSTP
		evoMetrices.prepare(); // prepares the history table, "MetricsHistory"
		                       // from which documents can be rendered... possible call: prepare(max) 
		
		rstr = evoMetrices.getStringTable(); // returns the history table as tab separated table (e.g. for excel)
				out.print(2, "... results per metric \n"+rstr) ;
		
		historyTableAsString = rstr;
		// rstr = evoMetrices.getAsXml();       // returns the history table as xml (e.g. for persistence, exchange)
		// MetricsHistory mh = evoMetrices.getAsHistory(); // gets the history as object
		 
	}


	public String getHistoryTableAsString() {
		return historyTableAsString;
	}

}

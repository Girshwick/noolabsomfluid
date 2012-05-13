package org.NooLab.somfluid.components.post;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.SomModelDescription;
import org.NooLab.somfluid.components.variables.VariableContribution;
import org.NooLab.somfluid.components.variables.VariableContributions;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.EvoMetrik;
import org.NooLab.somscreen.SomQualityData;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;

public class OutResults implements Serializable{

	
	transient ModelOptimizer modelOptimizer ;
	transient SomDataObject somData ;
	
	transient SomFluidProperties sfProperties ;
	OutputSettings outputSettings ;
	ModelingSettings modelingSettings;
	
	EvoMetrices evoMetrices ;
	VariableContrasts variableContrasts ;
	VariableContributions variableContributions ;
	EvoMetrik bestMetric;
	
	transient ArrUtilities arrutil = new ArrUtilities ();
	transient private PrintLog out;
	private String historyTableAsString;
	
	// ========================================================================
	public OutResults(ModelOptimizer modOpti, SomFluidProperties sfprops) {

		updateRelationships( modOpti,  sfprops);
		
	}
	// ========================================================================
	
	public void updateRelationships(ModelOptimizer modOpti, SomFluidProperties sfprops){
		
		modelOptimizer = modOpti;
		sfProperties = sfprops;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		outputSettings = sfProperties.getOutputSettings() ;
		
		somData = modelOptimizer.getSomDataObj() ; 
		
		evoMetrices = modelOptimizer.getEvoMetrices() ;
		
		out = modelOptimizer.getOut() ;
	}
	
	public void createModelOptimizerReport() {
		
		// String xstr = xmlReport.toString();
		// sfFactory.publishReport(xstr);
		
		// str = evoMetrices.toString();
		// out.print(2, "explored metrices (all): \n"+str+"\n") ;
		
		// sort the evo metrices
		String rstr ;
		
		//  
		evoMetrices.prepare(); // prepares the history table, "MetricsHistory"
		                       // from which documents can be rendered... possible call: prepare(max) 
		
		// TODO: should be called with parameters, that define the columns  
		rstr = evoMetrices.getStringTable(); // returns the history table as tab separated table (e.g. for excel)
				
		
		historyTableAsString = rstr;
		// rstr = evoMetrices.getAsXml();       // returns the history table as xml (e.g. for persistence, exchange)
		// MetricsHistory mh = evoMetrices.getAsHistory(); // gets the history as object
		 
		bestMetric = evoMetrices.getEvmItems().get(0) ;
		
		// -----------------------------
		
		// create basic table for best
		
		
		// create average table for top-8 (priority against:) or those within 2 score points
		// display: mean of out-parameters, var of out-parameters, list of invoked variables, with freq
		
		
		
		// create everything as xml ...
		
	}


	public void createDiagnosticsReport(Coarseness coarseness) {
		// 
		
	}


	public void createDiagnosticsReport(SomModelDescription smd) {
		// variables' contribution, linear contrasts ....
		
		variableContrasts = smd.getVariableContrasts() ;
		
		variableContributions = smd.getVariableContributions() ;
		
		createContributionsReport( variableContributions ) ;
		
		createContrastsReport( variableContrasts ) ;
	}

	
	public void createContributionsReport(VariableContributions vcs) {
		// variables' contribution, linear contrasts ....
		 
		String str, numstr , varLabel;
		int z;
		double _scoredelta;
		double ppv=0.0,specificity=0.0, sensitivity=0.0;
		
		Variable variable;
		VariableContribution vc ;
		SomQualityData sqData ;
		
		ArrayList<ArrayList<String>> vrows = new ArrayList<ArrayList<String>>();
		ArrayList<String> vrow;
		ArrayList<String> headers = new ArrayList<String> ();
		
		String tableStr="";
		
		ArrayList<String> initialVariableSelection = vcs.getBaseMetric() ;
		 
		
		// pre-amble
		vrow = new ArrayList<String>();
		str = "\ncontributions are measured as the effect provoked by removing a single variable+\n"+
		      "negative score delta means increasing score (worse model), deltas in frequencies are represented directly\n";
		vrow.add(str);
		vrows.add(vrow);
		
		vrow = new ArrayList<String>();
		str = "best score : " + String.format("%.3f", vcs.getBaseScore() ) ; vrow.add(str) ;
		str = arrutil.arr2text( initialVariableSelection, ", ") ; vrow.add(str+"\n") ; 
		
		vrows.add(vrow);
		SomQualityData bsq = vcs.getBestSqData() ;
		// header
		String[] strings = new String[]{"removed Variable","score delta","rocauc",
                						"TP",
				                        "FP",
				                        "TN",
				                        "FN",
                						"dTP",
				                        "dFP",
				                        "dTN",
				                        "dFN",
				                        "PPV","sens","spec","class"} ;
		headers = new ArrayList<String>( Arrays.asList( strings ));
		vrows.add(headers) ; 
		
		vrow = new ArrayList<String>();
		
		str = "reference: best model" ; vrow.add(str) ;
		str = " --- " ; vrow.add(str) ;
		
		
		str = String.format("%.4f", bsq.getRocAuC() ) ; vrow.add(str) ;
		str = ""+bsq.getTp() ; vrow.add(str) ;
		str = ""+bsq.getFp() ; vrow.add(str) ;
		str = ""+bsq.getTn() ; vrow.add(str) ;
		str = ""+bsq.getFn() ; vrow.add(str) ;
		str = " " ; vrow.add(str) ;
		str = " " ; vrow.add(str) ;
		str = " " ; vrow.add(str) ;
		str = " " ; vrow.add(str) ;

		ppv = (double)bsq.getTp()/(double)(bsq.getTp() + bsq.getFp());
		sensitivity = (double)bsq.getTp()/(double)(bsq.getTp() + bsq.getFn());;
		specificity = (double)bsq.getTn()/(double)(bsq.getTn() + bsq.getFp());;
		
		str = String.format("%.3f", ppv ) ; vrow.add(str) ;
		str = String.format("%.3f", sensitivity ) ; vrow.add(str) ;
		str = String.format("%.3f", specificity ) ; vrow.add(str) ;

		
		str = "\n" ; vrow.add(str) ;
		vrows.add(vrow) ; 
		
		// vrows.add(headers) ;
		for(int i=0;i<vcs.size();i++){
			
			vc = vcs.getItem(i) ;
		
			sqData = vc.getSqData() ;
			variable = vc.getVariable() ;
			varLabel = vc.getVariableLabel() ;  
			if (variable.isTV()){
				continue;
			}
			vrow = new ArrayList<String>();
			vrow.add(varLabel) ; 
			
			_scoredelta = vc.getScoreDelta() ;
			str = String.format("%.2f", _scoredelta ) ; vrow.add(str) ;
			
			str = String.format("%.4f", sqData.getRocAuC() ) ; vrow.add(str) ;
			
			str = ""+sqData.getTp() ; vrow.add(str) ;
			str = ""+sqData.getFp() ; vrow.add(str) ;
			str = ""+sqData.getTn() ; vrow.add(str) ;
			str = ""+sqData.getFn() ; vrow.add(str) ;

			int dtp,dfp,dtn,dfn;
			
			dtp = sqData.getTp()-bsq.getTp();
			dfp = sqData.getFp()-bsq.getFp();
			dtn = sqData.getTn()-bsq.getTn();
			dfn = sqData.getFn()-bsq.getFn();
			
			str = ""+(dtp) ; vrow.add(str) ;
			str = ""+(dfp) ; vrow.add(str) ;
			str = ""+(dtn) ; vrow.add(str) ;
			str = ""+(dfn) ; vrow.add(str) ;
			
			ppv = (double)sqData.getTp()/(double)(sqData.getTp() + sqData.getFp());
			sensitivity = (double)sqData.getTp()/(double)(sqData.getTp() + sqData.getFn());;
			specificity = (double)sqData.getTn()/(double)(sqData.getTn() + sqData.getFp());;
			
			str = String.format("%.3f", ppv ) ; vrow.add(str) ;
			str = String.format("%.3f", sensitivity ) ; vrow.add(str) ;
			str = String.format("%.3f", specificity ) ; vrow.add(str) ;
			
			int vcc= vc.getContributionClass();
			if (vcc<0){
				vc.determineContributionClass();
				vcc= vc.getContributionClass();
			}
			str = ""+ vcc ; vrow.add("") ;
			
			if(i>=vcs.size()-1){
				vrow.add("\n") ;
				
			}
			vrows.add(vrow);
		} // i ->
		
		// create table String from vrows
		tableStr = createStringTable(vrows);
		vcs.setResultStringTable(tableStr);
		// create XML string
		
		
	} // createContributionsReport()
	
	
	public void createContrastsReport(VariableContrasts vcs) {
		// variables' contribution, linear contrasts ....
		
		String varLabel , str, numstr ;
		double t_nT_ratio,v;
		double[] mwSignificance; 
		BasicStatisticalDescription[] sDs;
		VariableContrast vc ;
		
		Variables variables;
		Variable variable ;
		
		ArrayList<ArrayList<String>> vrows = new ArrayList<ArrayList<String>>();
		ArrayList<String> vrow;
		
		ArrayList<String> headers = new ArrayList<String> ();
		String tableStr="";
		
		variables = somData.getVariables() ;
		// header
		String[] strings = new String[]{"Variable","mean TG", "mean non-TG", "mean all", "ratio T/nT", "MWU T:nT significance%", "MWU T:all sign.%" } ; 
																	// "Corr T", "Corr nT", "ratio Cn/CnT", "WS Test C(T,nT) 
		headers = new ArrayList<String>( Arrays.asList( strings ));
		vrows.add(headers) ; 
	
		int firstDataRowIx = vrows.size();
		
		for(int i=0;i<vcs.size();i++){
			
			vc = vcs.getItem(i) ;
			
			varLabel = vc.getVariableLabel() ;
			variable = variables.getItemByLabel(varLabel) ;
	
	        int ix = variables.getIndexByLabel(varLabel) ;
	        sfProperties.getModelingSettings() ;
	        somData.getNormalizedDataTable();
	        
			if ((variable.isTV() || (ix == variables.getTvColumnIndex()))){
				// continue;
				varLabel = varLabel+ " (TV)" ;
			}else{
				ix=-1;
			}
			
			if (vc.isUsed){
				
				t_nT_ratio = -1.0 ;
				vrow = new ArrayList<String>();
				vrow.add(varLabel);
				
				mwSignificance = vc.pValueMWU ;
				sDs = vc.statisticalDescription;
				
				// grouped
				v = sDs[0].getMean() ; 
				str = String.format("%.3f", v ) ; vrow.add(str) ;
				
				// not grouped
				v = sDs[1].getMean() ;
				str = String.format("%.3f", v ) ; vrow.add(str) ;
				
				// all
				v = sDs[2].getMean() ;
				str = String.format("%.3f", v ) ; vrow.add(str) ;
				
				// ratio
				if (sDs[1].getMean()!=0.0){
					t_nT_ratio = sDs[0].getMean()/sDs[1].getMean() ;
				}else{
					t_nT_ratio = -1.0;
				}
				str = String.format("%.3f", t_nT_ratio ) ; vrow.add(str) ;
				
				// contrast T:nT
				v = mwSignificance[0]*100.0 ;
				str = String.format("%.4f", v ) ; vrow.add(str) ;
				
				// contrast T:all
				v = mwSignificance[1] ;
				str = String.format("%.4f", v ) ; vrow.add(str) ;
				
				// contrast nT:all
				// v = mwSignificance[0] ;
				
				// correlation
				
				
				// sign. for difference
				
				if (ix>=0){
					vrows.add(firstDataRowIx,vrow);
				}else{
					vrows.add(vrow);
				}
			} // a used one ?
			
			
			
		} // i ->
		
		// create table String from vrows
		tableStr = createStringTable(vrows);
		vcs.setResultStringTable(tableStr);
		
		// create XML string
		
		
		
	} // createContrastsReport()

	public void createDiagnosticsReport(MultiCrossValidation multiCrossValidation) {
		// 
		
	}

	

	
	private String createStringTable( ArrayList<ArrayList<String>> tablerows ) {
		
		ArrayList<String> row;
		String tableStr="", colSeparator="\t";
		
	 
			  
		
		// translating into a single string
		for (int i=0;i<tablerows.size();i++){
			row = tablerows.get(i) ;
			
			for (int c=0;c<row.size();c++){
				tableStr = tableStr+row.get(c);
				if (c<row.size()-1){
					tableStr = tableStr + colSeparator;
				}
			}
			if (i<tablerows.size()-1){
				tableStr = tableStr + "\n";
			}
		}
		
		return tableStr;
	}
	
	
	// ------------------------------------------------------------------------
	
	public OutputSettings getOutputSettings() {
		return outputSettings;
	}

	public void setOutputSettings(OutputSettings outputSettings) {
		this.outputSettings = outputSettings;
	}

	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}

	public void setModelingSettings(ModelingSettings modelingSettings) {
		this.modelingSettings = modelingSettings;
	}

	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}

	public void setEvoMetrices(EvoMetrices evoMetrices) {
		this.evoMetrices = evoMetrices;
	}

	public VariableContrasts getVariableContrasts() {
		return variableContrasts;
	}

	public void setVariableContrasts(VariableContrasts variableContrasts) {
		this.variableContrasts = variableContrasts;
	}

	public VariableContributions getVariableContributions() {
		return variableContributions;
	}

	public void setVariableContributions(VariableContributions variableContributions) {
		this.variableContributions = variableContributions;
	}

	public String getHistoryTableAsString() {
		return historyTableAsString;
	}
	public void setHistoryTableAsString(String historyTableAsString) {
		this.historyTableAsString = historyTableAsString;
	}

}
package org.NooLab.somfluid;


import org.NooLab.utilities.datatypes.IndexedDistances;


public class OutputSettings {

	public static final int _RESULTFILE_MHISTORY    = 1;
	public static final int _RESULTFILE_MODELROC    = 3;

	public static final int _RESULTFILE_MODELVIEW   = 6;
	public static final int _RESULTFILE_MODELVALID  = 7;

	public static final int _RESULTFILE_MODELSOMMAP = 8;
	
	public static final int _RESULTFILE_VLINEARITY  = 11;
	public static final int _RESULTFILE_VCONTRIBUT  = 12;
	public static final int _RESULTFILE_VARENCODE   = 13;
	
	
	public static final int _RESULTFILE_MCOARSENESS = 15;
	
	public static final int _RESULTFILE_DATATRANSF  = 20;
	
	// ......................................
	
	
	String resultfileOutputPath = "" ;
	boolean asXml = false;
	
	
	IndexedDistances resultFilesMap = new IndexedDistances();
	
	String outfileMetricsHistory  = "metrics_history_<PRJ>_<DATE>.dat" ;
	String outfileModelSummary    = "model_summarytable_<PRJ>_<DATE>";
	String outfileModelValidation = "model_validation_<PRJ>_<DATE>";
	String outfileModelSomMap     = "model_sommap_<PRJ>_<DATE>.dat" ;
	String outfileRocData         = "model_rocdata_<PRJ>_<DATE>.data";
	String outfileVarEncoding     = "variables_encoding_<PRJ>_<DATE>.dat" ;
	String outfileVarLinearity    = "variables_linearity_<PRJ>_<DATE>.dat" ;
	String outfileVarContribution = "variables_contribution_<PRJ>_<DATE>.dat";
	String outfileModelCoarseness = "model_coarseness_<PRJ>_<DATE>.dat" ;
	String outfileDataTransform   = "data_transformlist_<PRJ>_<DATE>.dat" ;
	
	
	private boolean resultFileZipping = true;
	
	
	// ========================================================================
	public OutputSettings(){
		
		setDefaults() ;
	}
	// ========================================================================
	
	private void setDefaults(){
		
		resultFilesMap.put( _RESULTFILE_MHISTORY,    outfileMetricsHistory, 1) ;

		resultFilesMap.put( _RESULTFILE_MODELVIEW,   outfileModelSummary, 1) ;
		resultFilesMap.put( _RESULTFILE_MODELVALID,  outfileModelValidation, 1) ;
		
		resultFilesMap.put( _RESULTFILE_MODELROC,    outfileRocData, 1) ;
		
		 
		resultFilesMap.put( _RESULTFILE_VARENCODE,   outfileVarEncoding, 1) ;
		resultFilesMap.put( _RESULTFILE_VLINEARITY,  outfileVarLinearity, 1) ;
		resultFilesMap.put( _RESULTFILE_VCONTRIBUT,  outfileVarContribution, 1) ;
		resultFilesMap.put( _RESULTFILE_MCOARSENESS, outfileModelCoarseness, 1) ;

		resultFilesMap.put( _RESULTFILE_DATATRANSF,  outfileDataTransform, 1) ;
		
		resultFilesMap.put( _RESULTFILE_MODELSOMMAP, outfileModelSomMap, 1) ;
		
		
	}
	
	/**
	 * 
	 * 
	 * @param approxIdentifier : part of the identifier, e.g. "roc", or "validation"
	 * @param context  model, variable, data, history
	 * @return
	 */
	public String getResultFileName( String approxIdentifier, String context){
		String resultFilename = "";
		
		
		
		return resultFilename;
	}
	
	
	public void setResultFilenames( IndexedDistances filesMap) {
		resultFilesMap = new IndexedDistances() ;
		resultFilesMap.addAll(filesMap);
	}


	public String getResultfileOutputPath() {
		return resultfileOutputPath;
	}


	public void setResultfileOutputPath(String resultfileOutputPath) {
		this.resultfileOutputPath = resultfileOutputPath;
	}

	public void setAsXml(boolean flag) {
		asXml = flag;
		
	}

	public boolean isAsXml() {
		return asXml;
	}



	public String getOutfileMetricsHistory() {
		return outfileMetricsHistory;
	}

	public void setOutfileMetricsHistory(String outfileMetricsHistory) {
		this.outfileMetricsHistory = outfileMetricsHistory;
	}

	public String getOutfileVarLinearity() {
		return outfileVarLinearity;
	}

	public void setOutfileVarLinearity(String outfileVarLinearity) {
		this.outfileVarLinearity = outfileVarLinearity;
	}

	public String getOutfileRocData() {
		return outfileRocData;
	}

	public void setOutfileRocData(String outfileRocData) {
		this.outfileRocData = outfileRocData;
	}

	public String getOutfileVarContribution() {
		return outfileVarContribution;
	}

	public void setOutfileVarContribution(String outfileVarContribution) {
		this.outfileVarContribution = outfileVarContribution;
	}

	public String getOutfileModelCoarseness() {
		return outfileModelCoarseness;
	}

	public void setOutfileModelCoarseness(String outfileModelCoarseness) {
		this.outfileModelCoarseness = outfileModelCoarseness;
	}

	public String getOutfileModelSummary() {
		return outfileModelSummary;
	}

	public void setOutfileModelSummary(String outfileModelSummary) {
		this.outfileModelSummary = outfileModelSummary;
	}

	public String getOutfileModelValidation() {
		return outfileModelValidation;
	}

	public void setOutfileModelValidation(String outfileModelValidation) {
		this.outfileModelValidation = outfileModelValidation;
	}

	public IndexedDistances getResultFilesMap() {
		return resultFilesMap;
	}

	public void setResultFilesMap(IndexedDistances resultFilesMap) {
		this.resultFilesMap = resultFilesMap;
	}

	public void createZipPackage(boolean flag) {
		resultFileZipping = flag ;
		
	}

	public boolean isResultFileZipping() {
		return resultFileZipping;
	}

	public void setResultFileZipping(boolean resultFileZipping) {
		this.resultFileZipping = resultFileZipping;
	}

}

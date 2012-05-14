package org.NooLab.somfluid;


import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;

import com.jamesmurty.utils.XMLBuilder;


public class OutputSettings implements Serializable{

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
	
	/** for organizing the output */
	IndexedDistances catalogFields;
	
	
	boolean isIncludeResultsToExportedPackages;
	boolean isIncludeDataToExportedPackages;


	
	private boolean resultFileZipping = true;
	PersistenceSettings persistenceSettings;
	private boolean exportApplicationModel;
	
	// ========================================================================
	public OutputSettings(PersistenceSettings ps){
		
		persistenceSettings = ps ;
		
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
	
	
	
	public void defineOutCatalogSelection( String[] outitems){
		
		
	}
	
	public void addOutCatalogSelection( String outitem){
		
		
	}

	public void initializeCatalog(){
	
		IndexDistance cf ;
		catalogFields = new IndexedDistances();
		 
		// the secondary index is used to indicate whether it should be used or not
		// the score field is used to determine the column's position in the output 
		cf = new IndexDistance(1,  0, 1,"index");         	catalogFields.add(cf) ;
		cf = new IndexDistance(2,  0, 2,"step"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(3,  0, 3,"score"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(4,  0, 4,"variableindexes"); catalogFields.add(cf) ;
		cf = new IndexDistance(5,  0, 5,"truepositives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(6,  0, 6,"truenegatives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(7,  0, 7,"falsepositives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(8,  0, 8,"falsenegatives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(9,  0, 9,"tprate"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(10, 0, 10,"tnrate"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(11, 0, 11,"fprate"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(12, 0, 12,"fnrate"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(13, 0, 13,"ppv"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(14, 0, 14,"npv"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(15, 0, 15,"sensitivity"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(16, 0, 16,"specificity"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(17, 0, 17,"rocauc"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(18, 0, 18,"rocstp"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(19, 0, 19,"risk"); 			catalogFields.add(cf) ;
	}

	public IndexedDistances getCatalogFields() {
		return catalogFields;
	}

	public ArrayList<String> getAllFieldLabels() {
		return catalogFields.getAllFieldLabels() ;
	}

	public int setOutputColumn(String fieldLabel, int columnPosition) {
		int result=-1;
		int ix ;
		ix = catalogFields.getIndexByStr(fieldLabel) ;
		
		if (ix>=0){
			catalogFields.getItem(ix).setSecindex(1);
			catalogFields.getItem(ix).setDistance(columnPosition);
			result=0;
		}
		return result;
	}
	
	public void resetOutputDefinition() {
		int mode = 1;
		if (mode<0){
			//catalogFields.clear();
			//return;
		}
		for (int i=0;i<catalogFields.size();i++){
			catalogFields.getItem(i).setSecindex(0) ;
		}
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

	public String getOutfileModelSomMap() {
		return outfileModelSomMap;
	}

	public void setOutfileModelSomMap(String outfileModelSomMap) {
		this.outfileModelSomMap = outfileModelSomMap;
	}

	public String getOutfileVarEncoding() {
		return outfileVarEncoding;
	}

	public void setOutfileVarEncoding(String outfileVarEncoding) {
		this.outfileVarEncoding = outfileVarEncoding;
	}

	public String getOutfileDataTransform() {
		return outfileDataTransform;
	}

	public void setOutfileDataTransform(String outfileDataTransform) {
		this.outfileDataTransform = outfileDataTransform;
	}

	public PersistenceSettings getPersistenceSettings() {
		return persistenceSettings;
	}

	public void setPersistenceSettings(PersistenceSettings persistenceSettings) {
		this.persistenceSettings = persistenceSettings;
	}

	public void exportApplicationModel(boolean flag) {
		exportApplicationModel = flag;
	}

	public boolean isExportApplicationModel() {
		return exportApplicationModel;
	}

	public void setExportApplicationModel(boolean exportApplicationModel) {
		this.exportApplicationModel = exportApplicationModel;
	}

	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {
		 
		return null;
	}

	public boolean isIncludeResultsToExportedPackages() {
		 
		return isIncludeResultsToExportedPackages;
	}

	public void setIncludeResultsToExportedPackages(boolean flag) {
		isIncludeResultsToExportedPackages = flag;
	}
	
	

	public boolean isIncludeDataToExportedPackages() {
		return isIncludeDataToExportedPackages;
	}

	public void setIncludeDataToExportedPackages(boolean flag) {
		isIncludeDataToExportedPackages = flag;
	}

}

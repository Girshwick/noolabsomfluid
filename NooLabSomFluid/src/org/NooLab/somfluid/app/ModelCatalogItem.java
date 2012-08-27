package org.NooLab.somfluid.app;

import java.io.Serializable;
import java.util.ArrayList;



public class ModelCatalogItem implements Serializable{

	private static final long serialVersionUID = -70110278096990015L;
	
	ArrayList<String> fieldlabels    = new ArrayList<String>();
	ArrayList<String> requiredfields = new ArrayList<String>();
	
	String packageName = "" ;
	String modelName = "" ;
	String modelVersion = "" ;
	long timevalue = 0;
	double modelscore = -1.0 ;
	
	
	transient boolean confirmed = false;
 

	// ------------------------------------------------------------------------
	public ModelCatalogItem(){
		
	}
	// ------------------------------------------------------------------------	


	public ArrayList<String> getFieldlabels() {
		return fieldlabels;
	}

	public void setFieldlabels(ArrayList<String> fieldlabels) {
		this.fieldlabels = fieldlabels;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}


	public String getModeVersion() {
		return modelVersion;
	}


	public void setModeVersion(String modeVersion) {
		this.modelVersion = modeVersion;
	}


	public String getModelVersion() {
		return modelVersion;
	}


	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}


	public long getTimevalue() {
		return timevalue;
	}


	public void setTimevalue(long timevalue) {
		this.timevalue = timevalue;
	}


	public double getModelscore() {
		return modelscore;
	}


	public void setModelscore(double modelscore) {
		this.modelscore = modelscore;
	}
	
}

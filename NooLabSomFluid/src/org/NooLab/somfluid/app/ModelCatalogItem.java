package org.NooLab.somfluid.app;

import java.io.Serializable;
import java.util.ArrayList;



public class ModelCatalogItem implements Serializable{

	private static final long serialVersionUID = -70110278096990015L;
	
	ArrayList<String> fieldlabels = new ArrayList<String>();
	
	String packageName;
	String modelName;

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
	
}

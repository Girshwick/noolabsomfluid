package org.NooLab.somtransform;

import java.io.Serializable;



public class CollectedVariable implements Serializable{

	private static final long serialVersionUID = -1073077265381986040L;

	String varlabel="";
	int variableIndex = -1;
	String transformStackGuid = "";
	
	// ------------------------------------------------------------------------	
	public CollectedVariable(){
		
	}
	
	public CollectedVariable(String guid, String vlabel, int varIndex) {
		varlabel = vlabel;
		variableIndex = varIndex;
		transformStackGuid = guid;
	}
	// ------------------------------------------------------------------------


	public String getVarlabel() {
		return varlabel;
	}

	public void setVarlabel(String varlabel) {
		this.varlabel = varlabel;
	}

	public int getVariableIndex() {
		return variableIndex;
	}

	public void setVariableIndex(int variableIndex) {
		this.variableIndex = variableIndex;
	}

	public String getTransformStackGuid() {
		return transformStackGuid;
	}

	public void setTransformStackGuid(String transformStackGuid) {
		this.transformStackGuid = transformStackGuid;
	}
	
	
}

package org.NooLab.somtransform;

import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;



/**
 * 
 * 
 * 
 */
public class SomAssignatesDerivationTree extends SomAssignatesDerivationTreeAbstract{

	private static final long serialVersionUID = 6527320310829877737L;
	
	String baseVariableLabel = "";
	String baseGuid = "";

	int baseVariableIndex = -1;


	
	// ========================================================================
	public SomAssignatesDerivationTree(SomDataObject somData) {
		super(somData);
		
	}
	
	void clear() {
		baseVariableLabel = "";
		baseGuid = "";
		baseVariableIndex = -1;
		variableLabels.clear();
		stackGuids.clear();
	}
	// ========================================================================


	
	// ------------------------------------------------------------------------

	public String getBaseVariableLabel() {
		return baseVariableLabel;
	}

 
	public void setBaseVariableLabel(String baseVariableLabel) {
		this.baseVariableLabel = baseVariableLabel;
	}
 

	public String getBaseGuid() {
		return baseGuid;
	}
 

	public void setBaseGuid(String baseGuid) {
		this.baseGuid = baseGuid;
	}
 

	public int getBaseVariableIndex() {
		return baseVariableIndex;
	}
 

	public void setBaseVariableIndex(int baseVariableIndex) {
		this.baseVariableIndex = baseVariableIndex;
	}




	
	
}

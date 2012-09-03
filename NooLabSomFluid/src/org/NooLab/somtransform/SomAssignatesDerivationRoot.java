package org.NooLab.somtransform;

import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;

/**
 * 
 * 
 * 
 */
public class SomAssignatesDerivationRoot extends SomAssignatesDerivationTreeAbstract {

	private static final long serialVersionUID = 170779312256466838L;
	
	
	String anchorVariableLabel = "";
	String anchorGuid = "";

	int anchorVariableIndex = -1;

	ArrayList<CollectedVariable> collectedVariables;
	
	// ========================================================================
	public SomAssignatesDerivationRoot(SomDataObject somData) {
		super(somData);
		
	}
	
	void clear() {
		anchorVariableLabel = "";
		anchorGuid = "";
		anchorVariableIndex = -1;
		variableLabels.clear();
		stackGuids.clear();
	}
	// ========================================================================

	
	
}

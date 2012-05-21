package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;


/**
 * 
 * 
 * 
 */
public abstract class SomAssignatesDerivationTreeAbstract implements Serializable{

	private static final long serialVersionUID = -4851996263466977133L;

	transient SomDataObject somData;
	
	protected ArrayList<CollectedVariable> collectedVariables;
	
	protected ArrayList<String> variableLabels = new ArrayList<String>();
	protected ArrayList<String> stackGuids     = new ArrayList<String>();
	
	
	// ========================================================================
	public SomAssignatesDerivationTreeAbstract(SomDataObject somdata) {

		somData = somdata ;
	}
	// ========================================================================

  
	public void setCollectedVariables(ArrayList<CollectedVariable> collectedVars) {
		 
		collectedVariables = new ArrayList<CollectedVariable>(collectedVars) ;
	}
 
	public ArrayList<CollectedVariable> getCollectedVariables() {
		return collectedVariables;
	}

 
	public void updateVariableLabels(){
		
		if (collectedVariables==null){
			return;
		}
		
		for (int i=0;i<collectedVariables.size();i++){
			
			String vlabel = collectedVariables.get(i).varlabel ;
			variableLabels.add( vlabel );

			String tguid = collectedVariables.get(i).varlabel ;
			stackGuids.add( tguid );
		}
	
	}
	
	
}

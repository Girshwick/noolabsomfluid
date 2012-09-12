package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.utilities.datatypes.IndexDistance;


/**
 * 
 * 
 * 
 */
public abstract class SomAssignatesDerivationTreeAbstract implements Serializable{

	private static final long serialVersionUID = -4851996263466977133L;

	transient SomDataObject somData;
	
	protected ArrayList<CollectedVariable> collectedVariables = new ArrayList<CollectedVariable>();
	
	protected ArrayList<String> variableLabels = new ArrayList<String>();
	protected ArrayList<String> stackGuids     = new ArrayList<String>();
	
	
	// ========================================================================
	public SomAssignatesDerivationTreeAbstract(SomDataObject somdata) {

		somData = somdata ;
	}

	// ========================================================================

	
	public void addCollectedVariables(ArrayList<CollectedVariable> cvs) {
	
		for (int i=0;i<cvs.size();i++){
			CollectedVariable cv = cvs.get(i) ;
			
			int ix = findCollectedVariableByLabel(cv.varlabel) ;
			
			if (ix<0){
				collectedVariables.add(cv);
			}
			
		}
	}
  
	public void setCollectedVariables(ArrayList<CollectedVariable> collectedVars) {
		 
		collectedVariables = new ArrayList<CollectedVariable>(collectedVars) ;
	}
	
	
	public int findCollectedVariableByLabel( String varLabel){
		
		int index=-1, ix;
		
		for (int i=0;i<collectedVariables.size();i++){
			if (collectedVariables.get(i).varlabel.contentEquals(varLabel)){
				index = i;
				break;
			}
		}
		
		
		return index;
	}
 
	public ArrayList<CollectedVariable> getCollectedVariables() {
		if (collectedVariables==null){
			collectedVariables = new ArrayList<CollectedVariable>(); 
		}
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
	
	public void sortCollectedVariables(int direction) {
		
		
		Collections.sort(collectedVariables, new ixdComparator(direction));
		
	}


	class ixdComparator implements Comparator{

		int direction=0;
		
		public ixdComparator(int dir){
			direction = dir;
		}

		
		@Override
		public int compare(Object obj1, Object obj2) {
			
			int result = 0;
			CollectedVariable cv2,cv1;
			double v1,v2 ;
			
			cv1 = (CollectedVariable)obj1;
			cv2 = (CollectedVariable)obj2;
			
			v1 = cv1.variableIndex ;
			v2 = cv2.variableIndex ;
			
			if (direction>=0){
				if (v1>v2){
					result = 1;
				}else{
					if (v1<v2){
						result = -1 ;
					}
				}
			}else{
				if (v1>v2){
					result = -1;
				}else{
					if (v1<v2){
						result = 1 ;
					}
				}
				
			}
			
			return result;
		}
		
	}

	
	
}

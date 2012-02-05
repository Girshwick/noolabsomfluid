package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;

 

public class Variables implements Serializable, VariablesIntf{

	private static final long serialVersionUID = 4509035222518112241L;

	
	ArrayList<Variable>   items = new ArrayList<Variable>() ;
	
	// ---------------------------------------------------
	ArrayList<Variable>   whitelists = new ArrayList<Variable>() ; 
	ArrayList<Variable>	  blacklist = new ArrayList<Variable>() ;
	
	ArrayList<String>	  whitelistLabels = new ArrayList<String>() ;
	ArrayList<String> 	  blacklistLabels = new ArrayList<String>() ;

	
	ArrayList<Variable>   idVariables = new ArrayList<Variable>() ;
	ArrayList<Variable>   targetedVariables = new ArrayList<Variable>() ;
	
	Variable 			  targetVariable ;
	String 				  idLabel;
	
	int tvColumnIndex = -1;
	int idColumnIndex = -1;
	
	ArrayList<String> 	  initialUsageVector = new ArrayList<String>() ;
	
	
	
	// ========================================================================
	public Variables(){
		
	}
	// ========================================================================

	public int size(){
		return items.size();
	}
	/** only the items array will be cleared*/
	public void clear(){
		items.clear();
	}
	
	/** everything will be cleared */
	public void clearAll(){
		items.clear();
		initialUsageVector.clear(); 
		idVariables.clear();
		whitelists.clear();
		blacklist.clear();
		whitelistLabels.clear();
		blacklistLabels.clear();
		targetedVariables.clear();
	}

	public Variable getItem( int index ){
		return items.get(index) ;
	}
	public void removeItem( int index ){
		items.remove(index) ;
	}
	public void removeItem( Variable v){
		items.remove(v) ;
	}
	
	
	public void additem( Variable v){
		items.add(v);
	}
	public void setItem( int index, Variable v ){
		items.set(index, v) ;
	}
	
	

	@Override
	public ArrayList<Variable> getActiveVariables() {
		 
		return null;
	}



	@Override
	public ArrayList<String> getActiveVariableLabels() {
		ArrayList<String> activeVars = new ArrayList<String>(); 
		boolean hb ;
		String varLabel="", varstr;
		
		
		for (int i=0;i<items.size();i++){
			hb = true;
			 
			varLabel = items.get(i).getLabel() ;
			
			hb = (blacklistLabels.contains(varLabel)==false);

			if ((hb) && (varLabel.length()>0)){
				activeVars.add(varLabel);
			}
		}// i->
		
		return activeVars;
	}

	@Override
	public String getActiveTargetVariableLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	public Variable getActiveTargetVariable() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ArrayList<String> getAllTargetedVariables() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ArrayList<String> getAllIndexVariables() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
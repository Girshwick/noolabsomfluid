package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.utilities.strings.StringsUtil;

 

public class Variables implements Serializable, VariablesIntf{

	private static final long serialVersionUID = 4509035222518112241L;

	
	ArrayList<Variable>   items = new ArrayList<Variable>() ;
	
	// ---------------------------------------------------
	ArrayList<Variable>   whiteLists = new ArrayList<Variable>() ; 
	ArrayList<Variable>	  blackList = new ArrayList<Variable>() ;  
	
	ArrayList<String>	  whitelistLabels = new ArrayList<String>() ;
	ArrayList<String> 	  blacklistLabels = new ArrayList<String>() ;

	
	ArrayList<Variable>   idVariables = new ArrayList<Variable>() ;
	ArrayList<Variable>   targetedVariables = new ArrayList<Variable>() ;
	
	Variable 			  targetVariable ;
	String 				  idLabel;
	
	int tvColumnIndex = -1;
	int idColumnIndex = -1;
	
	ArrayList<String> 	  initialUsedVariablesStr = new ArrayList<String>() ;
	ArrayList<Double> 	  usageIndicationVector = new ArrayList<Double>() ;
	
	StringsUtil strgutil = new StringsUtil();
	
	// ========================================================================
	public Variables(){
		
	}
	// ========================================================================

	public Variables( Variables vars ) {
		Variable item ;
		
		for (int i=0;i<vars.size() ; i++){
			item = new Variable( vars.getItem(i)) ;
			items.add(item);
		}
		targetVariable = vars.targetVariable ;
		tvColumnIndex = vars.tvColumnIndex ;
		idColumnIndex = vars.idColumnIndex ;          
		idLabel = vars.idLabel  ;
		
		whiteLists = new ArrayList<Variable>( vars.whiteLists  ) ; 
		blackList = new ArrayList<Variable>( vars.blackList  ) ;  
		
		whitelistLabels = new ArrayList<String>( vars.whitelistLabels  ) ;
		blacklistLabels = new ArrayList<String>( vars.blacklistLabels  ) ;

		
		idVariables = new ArrayList<Variable>( vars.idVariables  ) ;
		targetedVariables = new ArrayList<Variable>( vars.targetedVariables  ) ;
		
	}

	public int size(){
		return items.size();
	}
	
	/** only the items array will be cleared*/
	public void clear(int mode){
		if (mode==1){
			items.clear();
			return;
		}
		
		whiteLists.clear();
		blackList.clear();  
		
		whitelistLabels.clear();
		blacklistLabels.clear();

		
		idVariables.clear();
		targetedVariables.clear();
		
		
	}
	
	/** everything will be cleared */
	public void clearAll(){
		items.clear();
		initialUsedVariablesStr.clear(); 
		idVariables.clear();
		whiteLists.clear();
		blackList.clear();
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
	

	
	public ArrayList<String> getVariableSelection( int[] useindicator) {
	
		ArrayList<String> usedLabels = new ArrayList<String>();
		String label;
		
		for (int i=0;i<items.size();i++){
			if (useindicator[i]>0){
				label = items.get(i).getLabel();
				usedLabels.add(label) ;
			}
		}
		return usedLabels;
	}


	public ArrayList<String> getVariableSelection( ArrayList<Double> usagevector) {
		
		ArrayList<String> usedLabels = new ArrayList<String>();
		String label;
		
		for (int i=0;i<items.size();i++){
			if (usagevector.get(i)>0.0){
				label = items.get(i).getLabel();
				usedLabels.add(label) ;
			}
		}
		return usedLabels;
	}

	public ArrayList<String> getLabelsForVariablesList(Variables vars){
		ArrayList<String> labels = new ArrayList<String>();
		Variable v;
		
		for (int i=0;i<vars.size();i++){
			v=vars.getItem(i) ;
			labels.add( v.getLabel()) ;
		}
		return labels;
	
	}
	
	
	public ArrayList<String> getLabelsForVariablesList(ArrayList<Variable> vars){
	
		ArrayList<String> labels = new ArrayList<String>();
		Variable v;
		
		for (int i=0;i<vars.size();i++){
			v=vars.get(i) ;
			labels.add( v.getLabel()) ;
		}
		return labels;
	}

	/**
	 * returns a translation of labels to indexes, the list will have the same length!!
	 * if a string is not found, the index value of the respective position will be set to -1;
	 */
	public ArrayList<Integer> getIndexesForLabelsList(ArrayList<String> strings) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		String inlabel,label;
		ArrayList<String> varLabels = getLabelsForVariablesList(items);
		
		for (int i=0;i<strings.size();i++){
			inlabel = strings.get(i) ;
			int p = varLabels.indexOf(inlabel) ;
			indexes.add(p) ;
		}
		
		return indexes;
	}

	@Override
	public ArrayList<Variable> getActiveVariables() {
		ArrayList<Variable> selection = new ArrayList<Variable>(); 
		
		
		return selection;
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
		String activeTV = "" ;
		
		if (targetVariable != null){
			activeTV = targetVariable.getLabel() ;
		}else{

			for (Variable v:items){
				if (v.isTV()){
					activeTV = v.getLabel();
					if (targetVariable==null){
						targetVariable=v;
					}
					break;
				}
			}
			
		}
		return activeTV;
	}
	
	public Variable getActiveTargetVariable() {
		Variable activeTV = null ;
		
		return activeTV;
	}



	@Override
	public ArrayList<Variable> getAllTargetedVariables() {
	
		return  getAllTargetedVariables(1) ;
	}
	//getLabelsForVariablesList
	
	public ArrayList<Variable> getAllTargetedVariables(int includingActiveTargetVariable) {
		ArrayList<Variable> atv= new ArrayList<Variable>();
		
		boolean hb;
		
		for (Variable v: items){
			hb = v.isTV();
			if (includingActiveTargetVariable>=1){
				if (v.isTV()){
					hb=true;
				}
			}else{
				if (v.isTV()){
					hb=false;
				}
			}
			if (hb){
				atv.add(v) ;
			}
		} // -> all variables

		return atv;
	}

	public ArrayList<Variable> getTargetedVariables() {
		return targetedVariables;
	}
	@Override
	public ArrayList<Variable> getAllIndexVariables() {
	
		ArrayList<Variable> aixv= new ArrayList<Variable>();
		boolean hb;
		
		for (Variable v: items){
			hb = v.isID() || v.isIndexcandidate();
			if (hb){
				aixv.add(v) ;
			}
		} // -> all variables
		return aixv;
	}
	
	public Variable getTargetVariable() {
		return targetVariable;
	}

	public ArrayList<Variable> getIdVariables() {
		if (idVariables.size()==0){
			idVariables = getAllIndexVariables();
		}
		return idVariables;
	}

	public ArrayList<Variable> getItems() {
		return items;
	}

	public void setItems(ArrayList<Variable> items) {
		this.items = items;
	}

	public ArrayList<Variable> getWhitelists() {
		return whiteLists;
	}

	public void setWhitelists(ArrayList<Variable> whitelists) {
		this.whiteLists = whitelists;
	}

	public void setWhitelistLabels(ArrayList<String> whitelistLabels) {
		this.whitelistLabels = whitelistLabels;
	}

	public void setBlacklist(ArrayList<Variable> blacklist) {
		this.blackList = new ArrayList<Variable>(blacklist);
	}

	public void setBlackList(ArrayList<Variable> blackList) {
		this.blackList = blackList;
	}

	public ArrayList<Variable> getBlacklist() {
		if (blackList==null){
			blackList = new ArrayList<Variable>();
		}
		return blackList;
	}

	public ArrayList<String> getBlacklistLabels() {
		return blacklistLabels;
	}

	public void setBlacklistLabels(ArrayList<String> blacklistlabels) {
		
		
		if (blackList==null){
			blackList = new ArrayList<Variable>() ;
			blacklistLabels = new ArrayList<String>();
		}
		
		blacklistLabels = new ArrayList<String>( blacklistlabels );
		
		blackList.clear();
		
		addBlacklistLabels( blacklistlabels );
		
	}
	
	public void addBlacklistLabels(ArrayList<String> blacklistlabels) {
		String varLabelStr ;
		int ix;
		 
		try{
			
			if (blacklistLabels==null){
				blacklistLabels = new ArrayList<String>();
			}
			if (blackList==null){
				blackList = new ArrayList<Variable>() ;
			}

			for (int i=0;i<blacklistlabels.size();i++){
				
				varLabelStr = blacklistlabels.get(i) ;  
				ix = getIndexByLabel( varLabelStr );
				
				if (blacklistLabels.indexOf(varLabelStr)<0){
					blacklistLabels.add(varLabelStr) ;
					
				}
				if (ix>=0){
					blackList.add( items.get(ix) );
				}
			}

		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}
	
	public void addBlacklistLabel( String blacklistlabel ) {
		
		ArrayList<String> bLabels = new ArrayList<String>();
		
		if (blackList==null){
			blackList = new ArrayList<Variable>() ;
			blacklistLabels = new ArrayList<String>();
		}
		bLabels.add(blacklistlabel) ;
		addBlacklistLabels(bLabels) ;
	}

	
	
	public ArrayList<Variable> getWhiteLists() {
		return whiteLists;
	}

	public ArrayList<String> getWhitelistLabels() {
		return whitelistLabels;
	}

	public void setWhiteLists(ArrayList<Variable> whiteLists) {
		this.whiteLists = whiteLists;
	}

	public ArrayList<Variable> getBlackList() {
		return blackList;
	}

	public void setIdVariables(ArrayList<Variable> idVariables) {
		this.idVariables = idVariables;
	}

	public void addTargetedVariableByLabel(String tvarLabel) {
		int tix;
		
		tix = this.getIndexByLabel(tvarLabel) ;
		addTargetedVariable( items.get(tix)) ;
		
	}
	public void addTargetedVariable(Variable tvar) {
		targetedVariables.add(tvar);
	}
	public void setTargetedVariables(ArrayList<Variable> targetedVariables) {
		this.targetedVariables = targetedVariables;
	}

	public void setTargetVariable(Variable targetvariable) {
		
		targetVariable = targetvariable;
		tvColumnIndex = items.indexOf(targetVariable);
		if (targetedVariables.contains(targetVariable)==false){
			targetedVariables.add(targetVariable) ;
		}
	}

	public String getIdLabel() {
		return idLabel;
	}

	public void setIdLabel(String idLabel) {
		this.idLabel = idLabel;
	}

	public int getTvColumnIndex() {
		return tvColumnIndex;
	}

	public void setTvColumnIndex(int tvColumnIx) {
		int tix = tvColumnIndex;
		
		this.tvColumnIndex = tvColumnIx;
		
		if (targetVariable!=null){
			targetVariable.setTV(false);
		}
		if ((targetVariable==null) || (tix!=tvColumnIx)){
			if (tvColumnIx>=0){
				targetVariable = getItem(tvColumnIx) ;
				targetVariable.setTV(true);
			}
		}
	}

	public int getIdColumnIndex() {
		return idColumnIndex;
	}

	public void setIdColumnIndex(int idColumnIx) {
		 
		 
		this.idColumnIndex = idColumnIx;
	}

	public ArrayList<Double> getUsageIndicationVector() {
		
		usageIndicationVector.clear();
		
		 
		String vlabel;
		Variable v;
		boolean isBlack ;
		
		isBlack=false;
		
		for (int i=0;i<items.size();i++){
			v = items.get(i) ;
			vlabel = v.getLabel() ;
			isBlack = this.blacklistLabels.indexOf(vlabel)>=0 ;
			
			if ((isBlack==false) && (v.isID()==false) && (v.isIndexcandidate()==false)){
				if (initialUsedVariablesStr.indexOf(vlabel)>=0){
					usageIndicationVector.add(1.0) ;
				}else{
					usageIndicationVector.add(0.0) ;
				}
				if (v.isTV()){
					// usageIndicationVector.set(i,-2.0) ;
				}
			}else{
				usageIndicationVector.add(0.0) ;
			}
		} // i->
		
		 
		
		return usageIndicationVector;
	}

	public ArrayList<String> getInitialUsageVector() {
		return initialUsedVariablesStr;
	}

	public void setInitialUsageVector(ArrayList<String> initialUseVector) {
		int n;
		if ((initialUseVector==null) || (initialUseVector.size()<=1)){
			n=0;
		}
		this.initialUsedVariablesStr = new ArrayList<String>( initialUseVector ) ;
		// initialized as existing but empty list: ArrayList<Double> usageIndicationVector = new ArrayList<Double>() ;
		if (usageIndicationVector.size() != items.size()){
			for (int i=0;i<items.size();i++){
				usageIndicationVector.add(0.0);
				String label = items.get(i).getLabel();
				if (initialUseVector.contains(label )){
					usageIndicationVector.set(i,1.0);
				}
			}
		}
		n=0;
	}

	public int[] getUseIndicatorArray() {
		int[] uses = new int[items.size()];
		String vlabel;
		Variable v;
		boolean isBlack ;
		
		isBlack=false;
		
		for (int i=0;i<items.size();i++){
			v = items.get(i) ;
			vlabel = v.getLabel() ;
			isBlack = this.blacklistLabels.indexOf(vlabel)>=0 ;
			
			if ((isBlack==false) && (v.isID()==false) && (v.isIndexcandidate()==false)){
				if (initialUsedVariablesStr.indexOf(vlabel)>=0){
					uses[i]=1;
				}
			}
		} // i->
		
		return uses;
	}
	
	/**
	 * 
	 * todo: for large number of variables, we should set up a treemap... (assuming, that the set is not changing
	 * @param varLabel
	 * @return
	 */
	public int getIndexByLabel(String varLabel) {
		int index=-1;
		boolean hb;
		// items ArrayList<Variable>
		Variable item;
		
		for (int i=0;i<items.size();i++){
			item = items.get(i) ;
			if ((item!=null) && (item.getLabel().contentEquals(varLabel))){
				index=i;
				break ;
			}else{
				hb = strgutil.matchSimpleWildcard( varLabel, item.getLabel()) ;
				if (hb){
					index = i;
					break ;
				}
			}
		}
		
		return index;
	}
	
}

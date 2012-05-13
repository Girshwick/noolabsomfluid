package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somsprite.AnalyticFunctionSpriteImprovement;
import org.NooLab.utilities.ArrUtilities;
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
	
	ArrayList<String> 	  initialUsedVariablesStr   ;
	ArrayList<Double> 	  usageIndicationVector   ;
	
	/** 
	 * describes whether a variable is allowed to touch or not across arbitrary contexts;
	 * it is sth like a super-blacklist   
	 */
	ArrayList<Integer> 	  absoluteAccessible ;
	
	transient StringsUtil strgutil ;
	
	// ========================================================================
	public Variables(){
		
		initialUsedVariablesStr = new ArrayList<String>() ;
		usageIndicationVector = new ArrayList<Double>() ;
		absoluteAccessible = new ArrayList<Integer>();
		strgutil = new StringsUtil();
	}
	// ========================================================================

	public Variables( Variables vars ) {
		Variable item ;
		
		initialUsedVariablesStr = new ArrayList<String>() ;
		usageIndicationVector = new ArrayList<Double>() ;
		absoluteAccessible = new ArrayList<Integer>();
		
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
		
		strgutil = null;
		strgutil = new StringsUtil();
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

	public ArrayList<AnalyticFunctionSpriteImprovement> getKnownTransformations() {
		 
		ArrayList<AnalyticFunctionSpriteImprovement> knownTransforms = new ArrayList<AnalyticFunctionSpriteImprovement>(); 
	
		
		
		return knownTransforms;
	}

	public boolean openForInspection( Variable v ) {
		boolean hb;
		
		hb = blacklistLabels.indexOf( v.getLabel() )<0 ;
		
		
		if (hb) hb = !v.isID(); 
		if (hb) hb = !v.isTV(); 
		if (hb) hb = !v.isIndexcandidate(); 
		if (hb) hb = !v.isTVcandidate(); 
			
		for (int i=0;i<blacklistLabels.size();i++){
			if (hb){
				hb = (strgutil.matchSimpleWildcard( v.getLabel() , blacklistLabels.get(i)) ==false);
				if (hb==false){
					break;
				}
			}else{
				break;
			}
		}
		
		return hb;
	}

	
	public Variable getItem( int index ){
		Variable v=null;
		
		if ((index>=0) && (index<items.size())){
			v = items.get(index) ;
		}
		return v; 
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

	public ArrayList<String> getLabelsForUseIndicationVector( Variables vars, ArrayList<Double> useIndications ){
		ArrayList<String> labels = new ArrayList<String>();
		String str;
		
		
		if ((useIndications==null) || (useIndications.size()==0)){
			return labels;
		}
		
		for (int i=0;i<useIndications.size();i++){
			
			if (useIndications.get(i)>=0.00000001){
				str = items.get(i).getLabel();
				labels.add(str);
			}
		}// i->
		
		return labels;
	}
	

	public ArrayList<String> getLabelsForVariablesList(Variables vars, boolean applyOpenFilter){
		
		ArrayList<String> labels = new ArrayList<String>();
		Variable v;
		
		for (int i=0;i<vars.size();i++){
			v=vars.getItem(i) ;
			
			if (openForInspection(v)){
				labels.add( v.getLabel()) ;
			}
		}
		return labels;
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
	
	public ArrayList<String> getLabelsForIndexList(ArrayList<Integer> indexes){
		
		ArrayList<String> labels = new ArrayList<String>();
		Variable v;
		
		for (int i=0;i<indexes.size();i++){
			v = items.get( indexes.get(i)) ;
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

	
	public ArrayList<Double> getUseIndicationForLabelsList( ArrayList<String> stringList ) {
		
		ArrayList<Double> useIndicationVector = new ArrayList<Double>();
		String vlabel;
		double ui;
		ArrayList<String> varLabels = getLabelsForVariablesList(items);
		
		for (int i=0;i<items.size();i++){
			ui=0.0;
			vlabel = items.get(i).getLabel() ;
			int p = stringList.indexOf(vlabel) ;
			if (p>=0)ui=1.0;
			useIndicationVector.add(ui);
		}
		
		return useIndicationVector;
	}
	
	
	/**
	 * returns a translation of labels to indexes, the list will have the same length!!
	 * if a string is not found, the index value of the respective position will be set to -1;
	 */
	public ArrayList<Integer> getIndexesForLabelsList(ArrayList<String> stringList) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		String inlabel,label;
		ArrayList<String> varLabels = getLabelsForVariablesList(items);
		
		for (int i=0;i<stringList.size();i++){
			inlabel = stringList.get(i) ;
			int p = varLabels.indexOf(inlabel) ;
			indexes.add(p) ;
		}
		
		return indexes;
	}

	@Override
	public ArrayList<Variable> getActiveVariables() {
		ArrayList<Variable> selection = new ArrayList<Variable>(); 
		
		selection.addAll( this.items) ;
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
					if (blackList.contains( items.get(ix) )==false){
						blackList.add( items.get(ix) );
					}
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

	/**
	 * @return the absoluteAccessible
	 */
	public ArrayList<Integer> getAbsoluteAccessible() {
		return absoluteAccessible;
	}

	/**
	 * @param absoluteAccessible the absoluteAccessible to set
	 */
	public void setAbsoluteAccessible(ArrayList<Integer> absoluteAccessible) {
		this.absoluteAccessible = absoluteAccessible;
	}

	public ArrayList<Variable> getBlackList() {
		return blackList;
	}

	public void setIdVariables(ArrayList<Variable> idVariables) {
		this.idVariables = idVariables;
	}

	public void addTargetedVariableByLabel(String tvarLabel) throws Exception{
		int tix;
		
		tix = getIndexByLabel(tvarLabel) ;
		addTargetedVariable( items.get(tix)) ;
		
	}
	
	public void addTargetedVariable(Variable tvar) {
		if (targetedVariables.indexOf(tvar)<0){
			targetedVariables.add(tvar);
		}
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

	public void setTvColumnIndex(int columnIndex) {
		int tix = tvColumnIndex;
		
		this.tvColumnIndex = columnIndex;
		
		if (targetVariable!=null){
			targetVariable.setTV(false);
		}
		if ((targetVariable==null) || (tix!=tvColumnIndex)){
			if (tvColumnIndex>=0){
				targetVariable = getItem(tvColumnIndex) ;
				targetVariable.setTV(true);
			}
		}
		if (tvColumnIndex>=0){
			items.get(tvColumnIndex).setTV(true) ;
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
	// public void setInitialUsageVector(ArrayList<String> initialUseVector) {
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
	
	
	public Variable getItemByLabel(String varLabel) {
		
		Variable variable= null;
		boolean hb;
		// items ArrayList<Variable>
		Variable item;
		
		for (int i=0;i<items.size();i++){
			item = items.get(i) ;
			if ((item!=null) && (item.getLabel().contentEquals(varLabel))){
				variable = item ;
				break ;
			}else{
				hb = strgutil.matchSimpleWildcard( varLabel, item.getLabel()) ;
				if (hb){
					variable = item ;
					break ;
				}
			}
		}
		
		return variable;
	}
	
	/**
	 * 
	 * TODO: for large number of variables, we should set up a TreeMap... (assuming, that the set is not changing)
	 * @param varLabel
	 * @return
	 */
	public int getIndexByLabel(String varLabel) {
		
		int index=-1;
		boolean hb;
		// items ArrayList<Variable>
		Variable item;
		String itemVLabel;
		for (int i=0;i<items.size();i++){
			item = items.get(i) ;  itemVLabel = item.getLabel() ;
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


	/**
	 * @param refUseVector usage vector that serves as reference
	 * @param useVector usage vector that is examined for removed variables
	 * 
	 * @return list of index positions that have been removed from "refUseVector" to "useVector"
	*/
	public ArrayList<Integer> determineRemovedVariables( ArrayList<Double> refUseVector, ArrayList<Double> useVector, boolean supprTV) {
		ArrayList<Integer> varIndexes = new ArrayList<Integer>();
		
		for (int i=0;i<refUseVector.size();i++){
			
			if (i<useVector.size()){
				
				if ((refUseVector.get(i)>0) && (useVector.get(i)<=0)){
					boolean hb = supprTV == false;
					if (supprTV){
						hb = (i != this.tvColumnIndex); 
					}
					if (hb){
						varIndexes.add(i) ;
					}
				}
			}
			
		} // i->
		
		return varIndexes;
	}


	/**
	 * 
	 * @param refUseVector usage vector that serves as reference
	 * @param useVector usage vector that is examined for added variables
	 * @param b 
	 * 
	 * @return list of index positions that have been added from "refUseVector" to "useVector"
	 */
	public ArrayList<Integer> determineAddedVariables(ArrayList<Double> refUseVector, ArrayList<Double> useVector, boolean supprTV) {
		ArrayList<Integer> varIndexes = new ArrayList<Integer>();
		
		for (int i=0;i<refUseVector.size();i++){
			
			if (i<useVector.size()){
				
				if ( (refUseVector.get(i)<=0) && (useVector.get(i)>0)){
					boolean hb = supprTV == false;
					if (supprTV){
						hb = (i != this.tvColumnIndex); 
					}
					if (hb){
						varIndexes.add(i) ;
					}
				}
			}
			
		} // i->

		return varIndexes;
	}
	/* 
	 * 
	 * here we register the latest results and compare it to some "fixed points", that is...
	 * - the best model
	 * - the first model
	 * - the best models within a certain %-based deviation in quality 
	 * - the best 3 models
	 *  
	 * @param targetMod
	 */
	 

	// --------------------------------------------------------------------		
	
	
	/**
	 * translating index values indicating the position in the list into the list of strings ;
	 * refers to the list of variables
	 * 
	 * mode : not used so far
	 */
	public ArrayList<String> deriveVariableSelection( ArrayList<Integer> proposedindexes, int mode ) {
		
		ArrayList<String> varSelection = new ArrayList<String>();
		
		for (int i=0;i<proposedindexes.size();i++){
			if (proposedindexes.get(i)>0){
				int ix = proposedindexes.get(i);
				String vstr = items.get(ix).getLabel() ;
				varSelection.add(vstr) ;
			}
		}
		
		return varSelection;
	}
	
	/**
	 * 
	 * 
	 * @param indexes index values
	 * @param mode =0: refering to global list, =1: refering "somMapTable"
	 * @return
	 */
	public ArrayList<Double> deriveUsageVector( String[] refVariableLabels, ArrayList<Integer> indexes, int mode ) throws Exception{ 
		
		int smtabix,varix ;
		String varStr;
		
		//DataTable dtable;
		Variables variables;
		
		double[] usevector ;
		ArrayList<Double> usageVector = new ArrayList<Double>();
		
		if (indexes.size()<=1){
			// throw(new Exception("selection is empty, usage vector could not be prepared.")) ;
			return usageVector;
		}
		
			
		try{
			

			// starting with somMapTable but finally referring to the global vector (inclusive the transform-New variables)
			//dtable = somData.getDataTable();
			//variables = somData.getVariables();
 
			
			
			// usevector = dSom.getdSomCore().prepareUsageVector(dtable, variables) ;
			 
			usevector = new double[items.size()] ;
			varix = getTvColumnIndex() ;
			if (varix<0){
				//varix = dSom.getTargetVariableColumn() ;	
				setTvColumnIndex(varix) ;
				int n=10/0;
			}
			if (varix<0){
				return usageVector ; // it is still empty -> nothing will happen
			}
			
			usevector [varix] = 1;
			// for (int i=0;i<somData.getVariables().size();i++ ){ }
			
			// int n = somMapTable.variables.length ;
			
			for (int i=0;i< indexes.size() ;i++){
				
				smtabix = indexes.get(i);  
				// somMapTable contains ONLY used variables form the last modeling perform()
				if (mode>0){
					varStr = refVariableLabels[smtabix] ;
					varix = getIndexByLabel(varStr) ;
				}else{
					varix = smtabix;
				}
				
				usevector[varix] = 1 ;
			}
			
			usageVector = ArrUtilities.changeArraystyle(usevector) ;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return usageVector;
	}

	public ArrayList<Double> transcribeUseIndications( int[] indications ) {
		
		return null;
	}

	/**
	 * accepts ArrayLists of type Double, Integer, String
	 * 
	 * dependent on type, a transcription is provided:
	 *  from double = full vector indicating activity by values >0.0  -->  list of indexes 
	 *  from int    = just the reverse of that
	 *  from String = list of variable labels -> list of indexes
	 *  
	 *  
	 * @param indications
	 * @return
	 */
	public Object transcribeUseIndications( ArrayList indications ) {
		
		return transcribeUseIndications( this, indications) ;
	}
	
	public Object transcribeUseIndications( Variables variables, ArrayList indications ) {
		Object listobj = null;
		ArrayList<Double> usageIndicationVector = new ArrayList<Double>();
		ArrayList<Integer> usedVarIndexes = new ArrayList<Integer>();
		ArrayList<String> varLabels = new ArrayList<String>();
		
		Object listitem = indications.get(0);
		
		String cfstr = listitem.getClass().toString();
		//System.out.println("Format description 1 : "+cfstr);

		if (cfstr.toLowerCase().contains("string")){
			for (int i=0;i<indications.size();i++){
				String varLabel = (String)indications.get(i) ;
				int ix = variables.getIndexByLabel(varLabel) ;
				usedVarIndexes.add(ix ) ;
			}
			
			listobj = (Object)usedVarIndexes; 
		}

		if (cfstr.toLowerCase().contains("double")){
			// into indexes of selected variables
			
			for (int i=0;i<indications.size();i++){
				usageIndicationVector.add( (Double)indications.get(i) ) ;
			}
			
			listobj = (Object) transcribeUsageIndicationVector(usageIndicationVector);
		}
		
		if (cfstr.toLowerCase().contains("int")){
			// from index positions to full indicationn vector

			for (int i=0;i<indications.size();i++){
				usedVarIndexes.add( (Integer)indications.get(i) ) ;
			}

			listobj = (Object) transcribeUseIndexes(usedVarIndexes);
		}
		
		return listobj;
	}
	
	private ArrayList<Double> transcribeUseIndexes( ArrayList<Integer> usedVariablesIndices) {
		ArrayList<Double> usageIndicationVector = new ArrayList<Double>();

		for (int i=0;i<items.size();i++){
			usageIndicationVector.add(0.0) ;
		}

			
		for (int i=0;i<usedVariablesIndices.size();i++){
			int ix = usedVariablesIndices.get(i);
			usageIndicationVector.set(ix, 1.0) ;
		}
		
		return usageIndicationVector;
	}
	
	
	private ArrayList<Integer> transcribeUsageIndicationVector( ArrayList<Double> usageIndicationVector) {
		ArrayList<Integer> usedVarIndexes = new ArrayList<Integer>();

		for (int i=0;i<usageIndicationVector.size();i++){
			double iv = usageIndicationVector.get(i);
			if (iv>0){
				usedVarIndexes.add(i) ;
			}
		}
		
		return usedVarIndexes;
	}

	public void reestablishObjects() {
	 
		strgutil = new StringsUtil();
	}

	
}

package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.StringsUtil;
import org.apache.commons.collections.CollectionUtils;

import com.jamesmurty.utils.XMLBuilder;





public class TransformationModel implements Serializable{

	private static final long serialVersionUID = 3682621801185851481L;

	String tmGuid = "" ;
	
	transient SomTransformerIntf somTransformer;
	transient SomDataObject  somData ;
	
	
	/** any of the variables gets a transformation stack assigned as soon as an initialization is requested */
	ArrayList<TransformationStack> variableTransformations = new ArrayList<TransformationStack>();
	
	ArrayList<String> originalColumnHeaders = new ArrayList<String>() ;
	ArrayList<String> derivedColumnHeaders  = new ArrayList<String>() ;
	
	ArrayList<String> requiredVariables  = new ArrayList<String>() ;
	SomAssignatesDerivations derivations;
	ArrayList<SomAssignatesDerivationTree> derivationTrees ;
	
	ArrayList<String> xmlImage = new ArrayList<String>() ;
	
	transient Variables variables ; 
	transient SomFluidXMLHelper xEngine = new SomFluidXMLHelper();
	transient StringsUtil strgutil = new StringsUtil();
	transient PrintLog out ;

	
	
	// ========================================================================
	public TransformationModel(SomTransformerIntf transformer, SomDataObject somdata){
		
		tmGuid = GUID.randomvalue() ;
		somTransformer = transformer;
		somData = somdata;
		
		out = somData.getOut();
	}
	// ========================================================================	
	
	public String getXML() {
		return getXML(null);
	}
		
	public String getXML(XMLBuilder builder) {
		 
		boolean localXml;
		String xmlstr="",xstr = "" ;
		
		TransformationStack tStack ;
		

		// opening
		if (builder==null){
			builder = xEngine.getXmlBuilder( "transformations" );
			localXml = true;
		}else{
			builder = builder.e( "transformations" );
			localXml = false;
		}
		
		builder = builder.e("storage")
        			.e("format").a("embedded", "0").up()
        		  .up();
		
		for (int i=0;i<variableTransformations.size();i++){
											if ((i<=1) || (i%50==0) || (i>variableTransformations.size()-3)){
												out.printprc(2, i, variableTransformations.size(), variableTransformations.size()/10 , " of transformation stacks visited") ;
											}
			tStack = variableTransformations.get(i);
			
			xstr = tStack.getXML(builder,i, false);
			// xmlstr = xmlstr + xstr+"\n";
		}
		
		
		builder = builder.up();
		builder = builder.c("all transformations visited ... ");
		
		if ((localXml) ){
			xmlstr = xEngine.getXmlStr(builder, true);
		}else{
			xmlstr="\n<!--  -->\n" ;
		}
		
		String[] xmlstrs = xmlstr.split("\n");
		xmlImage = new ArrayList<String>( strgutil.changeArrayStyle(xmlstrs) );

		return xmlstr;
	}

	public TransformationStack getItem(int index) {
		TransformationStack tstack=null;
		
		if ((index>=0) && (index<variableTransformations.size())){
			tstack = variableTransformations.get(index);
		}
		
		return tstack;
	}

	public int getIndexByOutputReferenceGuid(String tguid) {
	
		TransformationStack ts;
		int index = -1;
	
		for (int i = 0; i < variableTransformations.size(); i++) {
	
			ts = variableTransformations.get(i);
	
			if (ts.outputColumnIds.indexOf(tguid) >= 0) {
				index = i;
				break;
			}
		}
		return index ;
	}
	
	
	
	public void clearData() {

		TransformationStack tstack = null, ts;
		StackedTransformation st;
		
		for (int i=0;i< variableTransformations.size();i++){
			
			ts = variableTransformations.get(i);
			for (int s=0;s< ts.getItems().size();s++){
				
				st = ts.getItems().get(s) ;
				
				st.clearData();
								
			}
			if (tstack!=null){
				break;
			}
		}
		
		
	}

	public ArrayList<String> extractRequiredChains() {
		return extractRequiredChains(0);
	}
	/**
	 * 
	 * 
	 * @param filtermode 1=remove entries that refer to roots that are not required
	 * 
	 * @return
	 */
	public ArrayList<String> extractRequiredChains(int filtermode) {
		
		ArrayList<String> requiredChainVariables = new ArrayList<String>(), treeVars; 
		String varLabel, ctvar ;
		ArrayList<SomAssignatesDerivationTree> candTrees;
		
		
		ctvar="" ;
		
		if (derivations.derivationTrees.size()<=1){
			derivations.createDerivationTrees();
		}
		// these variables are raw, untransformed
		for (int i=0;i<requiredVariables.size();i++){
			
			varLabel = requiredVariables.get(i);
			
			candTrees = derivations.getTreesByVariable( 1,varLabel);
			
			for (int c=0;c<candTrees.size();c++){
				
				treeVars = derivations.getVariablesOfTree( candTrees.get(c) );
				for (int v=0;v<treeVars.size();v++){
					ctvar = treeVars.get(v).trim();
					if (requiredChainVariables.indexOf(ctvar)<0){
						requiredChainVariables.add(ctvar) ;
					}
				}
			}// c-> all candidate trees, should be only one, since we are asking for raw variables...
			
		} // i-> all required variables
		
		ctvar="" ;
		if (filtermode>=1){
			// chains are constructed bottom-up, hence they may contain (derived) variables, that refer to roots,  
			// which are NOT part of the required set of raw variables: these will be removed here 
			for (int i=0;i<requiredChainVariables.size();i++){
				
				varLabel = requiredChainVariables.get(i) ; // e.g.  Kunde_seit, d1b_Stammkapital_Gruendungsdatum_c
				
				candTrees = derivations.getTreesByVariable( 1,varLabel);
				for (int c=0;c<candTrees.size();c++){
					treeVars = derivations.getVariablesOfTree( candTrees.get(c) );
					int isz = CollectionUtils.intersection( treeVars, requiredVariables ).size();
					if (isz==0){
						requiredChainVariables.set(i, "");
					}
				}
			 
				
			}//i->
			
		}
		
		// remove empty entries
		int r=requiredChainVariables.size()-1;
		while (r>=0){
			
			if (requiredChainVariables.get(r).length()==0){
				requiredChainVariables.remove(r);
			}
			
			r--;
		}
		requiredChainVariables.trimToSize() ;
		
		return requiredChainVariables;
	}
	
	
	public void setOriginalColumnHeaders() {
		
		ArrayList<String> rawcolHeaders = new ArrayList<String>();
		originalColumnHeaders.size();
		ArrayList<SomAssignatesDerivationTree> dTrees ;
		
		dTrees = derivations.derivationTrees ; // the trees for all approvably (!) raw variables...
		
		for (int i=0;i<dTrees.size();i++){
			String varlabel = dTrees.get(i).baseVariableLabel;
			rawcolHeaders.add(varlabel) ;
		}
		originalColumnHeaders.clear();
		originalColumnHeaders.addAll(rawcolHeaders) ;
	}

	public void setOriginalColumnHeaders(ArrayList<String> stritems) {
		originalColumnHeaders.clear();
		originalColumnHeaders.addAll(stritems) ;
	}
	
	public void setDerivedColumnHeaders(ArrayList<String> colHeaders) {
		derivedColumnHeaders = colHeaders;
	}

	
	/**
	 * TODO: only the first time (= map is empty) we go through the loop, else we use the 2-map
	 *       especially for many variables it is expensive
	 */
	public TransformationStack findTransformationStackByStackPositionGuid( String transformStackGuid){
		
		TransformationStack tstack = null, ts;
		StackedTransformation st;
		
		for (int i=0;i< variableTransformations.size();i++){
			
			ts = variableTransformations.get(i);
			for (int s=0;s< ts.getItems().size();s++){
				
				st = ts.getItems().get(s) ;
				if (st.idString.contentEquals(transformStackGuid) ){
					tstack = ts;
					break;
				}
			}
			if (tstack!=null){
				break;
			}
		}
		
		return tstack ;
	}

	public TransformationStack  findTransformationStackByGuid( String stackedTransformGuid){
		
		TransformationStack tstack = null, ts;
		StackedTransformation st;
		
		for (int i=0;i< variableTransformations.size();i++){
			
			ts = variableTransformations.get(i);
			 
			if (ts.transformGuid.contentEquals(stackedTransformGuid)){
				tstack = ts;
				break;
			}
		}
		
		return tstack ;
	}
	
	public int findTransformationStackByLabel(String varLabel) {
		
		Variables variables = somData.getVariables();
		
		Variable variable = variables.getItemByLabel(varLabel) ; 
		
		return findTransformationStackByVariable(variable);
	}
	
	public int findTransformationStackByVariable(Variable variable) {

		int index=-1;
		
		Variable tsVar;
		TransformationStack ts;
		
		for (int i=0;i< variableTransformations.size();i++){

			ts = variableTransformations.get(i);

			tsVar = ts.getBaseVariable();
			if (variable == tsVar) {
				index = i;
				break;
			}
		}
		
		return index;
	}

	public StackedTransformation findStackedTransformationByGuid( String stackedTransformGuid ){
		
		StackedTransformation strans=null, st ;
		TransformationStack tstack = null, ts;
		
		for (int i=0;i< variableTransformations.size();i++){
			
			ts = variableTransformations.get(i);
			for (int s=0;s< ts.getItems().size();s++){
				
				st = ts.getItems().get(s) ;
				if (st.idString.contentEquals(stackedTransformGuid) ){
					strans = st;
					break;
				}
			}
			
		}
		 
		return strans;
	}

	/**
	 * search recursively back from a given element, and collects the trace;
	 * note that backward references down to the root are only through multi-argument algorithms; </br> </br>
	 * 
	 * if there are no back ward references found for the given element, writer algorithms are searched
	 * that write into the given column </br> </br>
	 * 
	 * call it like  int[] trace = ( label, 1 , new int[]{}) </br> </br> 
	 * 
	 * @param idStr  label of variable, guid of stack or guid of stack position
	 * @param srcType 1=label, 2=stack guid, 3= stack position guid 
	 * @return a list of indices that refer to "Variables"
	 */
	public ArrayList<Integer> findTransformationRootStackIndex( String idStr , int srcType, ArrayList<Integer> rootingIndexes ){

		String varLabel="",srcVarLabel;
		int ix;
		TransformationStack tStack = null, ts;
		StackedTransformation st;

		
		try{
			
			if (rootingIndexes.size()==0){
				variables = somData.getVariables() ;
			}
			if (srcType<=1){ 
				varLabel = idStr ; 
				ix = findTransformationStackByLabel( varLabel) ;
				tStack = this.variableTransformations.get(ix) ;
	
			}
			
			if (srcType==2){ 
				tStack = findTransformationStackByGuid( idStr) ;
				varLabel = tStack.varLabel ; 
			}
			
			if (srcType==3){ 
				tStack = findTransformationStackByStackPositionGuid( idStr ) ;
				varLabel = tStack.varLabel ; 
			}
			
			// register the index of the incoming variable, which we are going to check
			ix = variables.getIndexByLabel(varLabel) ; 
			rootingIndexes.add( ix );
			
			
			
			for (int i=0;i<tStack.inputVarLabels.size();i++){
				srcVarLabel = tStack.inputVarLabels.get(i) ;
				ix = variables.getIndexByLabel(srcVarLabel) ;
				if (srcVarLabel.length()>0){
					rootingIndexes = findTransformationRootStackIndex( srcVarLabel, 1,rootingIndexes );
				}
			}
			
			for (int i=0;i<tStack.size();i++){
				
				st = tStack.getItem(i);
				AlgorithmIntf algo = (AlgorithmIntf)st.algorithm;
				
				if (algo.getType() == AlgorithmIntf._ALGOTYPE_VALUE){
					// backward -> check incoming
					for (int s=0;s<st.inputColumnLabels.size();s++){
						srcVarLabel = st.inputColumnLabels.get(s) ;
						// that's again not perfectly correct, it collects too much if there is
						// more than 1 multi-source algorithm, where those are dependent on different source sets   
						// 
						if (srcVarLabel.length()>0)
						rootingIndexes = findTransformationRootStackIndex( srcVarLabel, 1,rootingIndexes );
					}
				}
				if (algo.getType() == AlgorithmIntf._ALGOTYPE_WRITER){
					// forward -> check outgoing

				}

				
			} // all items in stack
			
			
			
		}catch(Exception e){
			
		}
		
		return rootingIndexes ;
	}
	
	
	
	public int findParentIndex( StackedTransformation st, String parentGuid ) {
		
		int index = -1, ix;
		String varLabel, stguid ;
		TransformationStack pvarTStack; 
		Variables variables = somData.getVariables() ;
		
		
		stguid = st.idString ;
		pvarTStack = findTransformationStackByStackPositionGuid( parentGuid ) ;
		
		varLabel = pvarTStack.varLabel ;
		index = variables.getIndexByLabel(varLabel) ;
		
		return index;
	}

	
	public int getTransformStackIndexByOutputGuid(String transformGuid) {
		
		int index=-1;
		TransformationStack ts, tstack=null;
		 
		for (int i = 0; i < variableTransformations.size(); i++) {

			ts = variableTransformations.get(i);

			if (ts.outputColumnIds.indexOf(transformGuid)>=0) {
				tstack = ts;
				break;
			}
		}

		if ((tstack != null) && (tstack.transformGuid.contentEquals(transformGuid) == false)) { 
			// not the same
			index = tstack.index;
		}

		return index;
	}
	
	
	public int getIndexByLabel(String varLabel) {
		int index=-1;
		
		String tslabel;
		TransformationStack tstack = null, ts;
		
		for (int i=0;i< variableTransformations.size();i++){
			
			ts = variableTransformations.get(i);
			 
				
				tslabel = ts.getBaseVariable().getLabel();
				if ( tslabel.contentEquals(varLabel) ){
					index = i;
					break;
				}else{
					boolean hb = (strgutil.matchSimpleWildcard( tslabel, varLabel)) || (strgutil.matchSimpleWildcard( tslabel, varLabel)) ;
					if (hb){
						index = i;
						break ;
					}
				}
			 
			
		}
		
		return index;
	}

	public TransformationStack getItemByLabel(String varLabel) {
		TransformationStack ts=null;
		
		int ix = getIndexByLabel(varLabel);
		if (ix>=0){
			ts = variableTransformations.get(ix);
		}
		
		return ts;
	}
	
	
	public String getTmGuid() {
		return tmGuid;
	}

	public void setTmGuid(String tmGuid) {
		this.tmGuid = tmGuid;
	}

	public ArrayList<TransformationStack> getVariableTransformations() {
		return variableTransformations;
	}

	public void setVariableTransformations(ArrayList<TransformationStack> variableTransformations) {
		this.variableTransformations = variableTransformations;
	}

	public ArrayList<String> getXmlImage() {
		return xmlImage;
	}

	public void setXmlImage(ArrayList<String> xmlImage) {
		this.xmlImage = xmlImage;
	}

	public ArrayList<String> getOriginalColumnHeaders() {
		return originalColumnHeaders;
	}

	public ArrayList<String> getRequiredVariables() {
		return requiredVariables;
	}

	public void setRequiredVariables(ArrayList<String> requiredVariables) {
		this.requiredVariables = requiredVariables;
	}

	public SomAssignatesDerivations getDerivations() {
		return derivations;
	}

	public void setDerivations(SomAssignatesDerivations derivations) {
		this.derivations = derivations;
	}

	public ArrayList<String> getDerivedColumnHeaders() {
		return derivedColumnHeaders;
	}



	 
	
	
}

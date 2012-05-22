package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.StringsUtil;

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
	
	
	
	ArrayList<String> xmlImage = new ArrayList<String>() ;
	
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
	public TransformationStack findTransformationStackByStackGuid( String transformStackGuid){
		
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

	public int findParentIndex( StackedTransformation st, String parentGuid ) {
		
		int index = -1, ix;
		String varLabel, stguid ;
		TransformationStack pvarTStack; 
		Variables variables = somData.getVariables() ;
		
		
		stguid = st.idString ;
		pvarTStack = findTransformationStackByStackGuid( parentGuid ) ;
		
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

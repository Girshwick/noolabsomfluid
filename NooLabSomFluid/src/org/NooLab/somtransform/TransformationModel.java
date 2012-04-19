package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.StringsUtil;





public class TransformationModel implements Serializable{

	private static final long serialVersionUID = 3682621801185851481L;

	String tmGuid = "" ;
	
	SomTransformer somTransformer;
	SomDataObject  somData ;
	
	/** any of the variables gets a transformation stack assigned as soon as an initializaiton is requested */
	ArrayList<TransformationStack> variableTransformations = new ArrayList<TransformationStack>();


	
	StringsUtil strgutil = new StringsUtil();
	PrintLog out ;
	
	// ========================================================================
	public TransformationModel(SomTransformer transformer, SomDataObject somdata){
		
		tmGuid = GUID.randomvalue() ;
		somTransformer = transformer;
		somData = somdata;
		
		out = somData.getOut();
	}
	// ========================================================================	
	
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

	/**
	 * TODO: only the first time (= map is empty) we go through the loop, else we use the 2-map
	 *       especially for many variables it is expensive
	 */
	public TransformationStack findTransformationStackByGuid( String stackedTransformGuid){
		
		TransformationStack tstack = null, ts;
		StackedTransformation st;
		
		for (int i=0;i< variableTransformations.size();i++){
			
			ts = variableTransformations.get(i);
			for (int s=0;s< ts.getItems().size();s++){
				
				st = ts.getItems().get(s) ;
				if (st.idString.contentEquals(stackedTransformGuid) ){
					tstack = ts;
					break;
				}
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
		pvarTStack = findTransformationStackByGuid( parentGuid ) ;
		
		varLabel = pvarTStack.varLabel ;
		index = variables.getIndexByLabel(varLabel) ;
		
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

	 
	
	
}

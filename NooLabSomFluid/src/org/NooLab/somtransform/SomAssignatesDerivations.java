package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.ArrUtilities;



/**
 * 
 * this creates lookup lists for the usage of raw variables.</br>
 * It contains two collections:
 * </br></br>
 * 1. For each raw variable, we create a list that contains all variables which are dependent on it == trees </br>
 * 2. for each derived variable, we determine the set of raw variables necessary to calculate it == roots
 * </br></br>
 * We need these lists for: </br> 
 * - checking the consistency of the model (missing input variable?) </br>
 * - determining the list of raw variables necessary for classification </br>
 * (- optimizing the calculation via caches ? </br>
 *
 */
public class SomAssignatesDerivations  implements Serializable{

	private static final long serialVersionUID = -8912309061077552908L;


	transient SomTransformer somTransformer;
	

	transient SomFluidAppGeneralPropertiesIntf sfProperties;
	transient SomDataObject somData;
	transient DataTable dataTableObj;
	
	transient TransformationModel transformationModel ;
	
	// these are containers that contain list of variables !!
	ArrayList<SomAssignatesDerivationTree> derivationTrees = new ArrayList<SomAssignatesDerivationTree>();
	ArrayList<SomAssignatesDerivationRoot> derivationRoots = new ArrayList<SomAssignatesDerivationRoot>();
	
	transient PrintLog out;
	
	
	// ========================================================================
	public SomAssignatesDerivations(SomTransformer transformer){
		
		initializeObjects(transformer) ;
	}


	public void initializeObjects(SomTransformer transformer){
		
		somTransformer = transformer ;
		somData = somTransformer.somData ;
		sfProperties = somTransformer.sfProperties;
		
		dataTableObj = somData.getDataTable() ;
		out = somData.getOut();
	}
	// ========================================================================	
	
	public void initialize(){
		
		if (derivationTrees!=null){
			derivationTrees.clear();
			derivationTrees=null;
		}
		derivationTrees = new ArrayList<SomAssignatesDerivationTree>();
		
		initializeTrees();
		initializeRoots() ;
		
	}
	
	
	public SomAssignatesDerivationTree getTreeByGuid( String tGuid){
		
		SomAssignatesDerivationTree dTree=null, dT;
		
		for (int i=0;i<derivationTrees.size();i++){
			
			dT = derivationTrees.get(i) ;
			if (dT.baseGuid.contentEquals(tGuid)){
				dTree = dT;
				break;
			}
		}
			
		return dTree;
	}
	
	
	public SomAssignatesDerivationRoot getRootByVariable(int mode, String varlabel){
		return null;
	}
	
	
	/**
	 * a derived variable could be present in several trees, so we have to check them all</br>
	 * @param mode  0=compare to base variable, 1=compare to any variable in the trees 
	 */
	public ArrayList<SomAssignatesDerivationTree> getTreesByVariable(int mode, String varlabel){
		
		String dvlabel ;
		ArrayList<SomAssignatesDerivationTree> dTrees = new ArrayList<SomAssignatesDerivationTree>();
		SomAssignatesDerivationTree dTree=null, dT;
		CollectedVariable cv ; 
		
		for (int i=0;i<derivationTrees.size();i++){
		
			dT = derivationTrees.get(i) ;
			dvlabel = dT.baseVariableLabel;
			
			if (mode<=0){
				if (dvlabel.contentEquals(varlabel)){
					dTrees.add(dT) ;
				}
			}
			
			if (mode>=1){
				boolean found = false;
				
				if ((dT.collectedVariables==null) || (dT.collectedVariables.size()==0)){
					if (dvlabel.contentEquals(varlabel) ){
						 
						dTrees.add(dT) ;
					}
				}
				if ((dT.collectedVariables!=null) && (dT.collectedVariables.size()>0)){
					
					for (int c=0;c<dT.collectedVariables.size();c++){
						
						cv = dT.collectedVariables.get(c);
						dvlabel = cv.varlabel ;
						
						if ( dvlabel.contentEquals(varlabel) ){
							found = true;
							dTree = dT;
							break;
						}
					} // c-> all collected variables
					if (found){
						dTrees.add(dTree) ;
					}
				}
				
			}
			
		} //i->
		
		return dTrees;
	}


	public ArrayList<String> getVariablesOfTree(SomAssignatesDerivationTree dTree) {
		ArrayList<String> vars = new ArrayList<String>(); 
		
		for (int i=0;i<dTree.collectedVariables.size();i++){
			
			String vlabel = dTree.collectedVariables.get(i).varlabel;
			if (vars.indexOf(vlabel)<0){
				vars.add( vlabel);
			}
		} // i->
		return vars;
	}


	@SuppressWarnings("unchecked")
	public void createDerivationTrees(){
		
		int ix, tix;
		String guidBaseVar, varlabel ;
		ArrayList<TransformationStack> tStacks;
		ArrayList<String> derivedVariablesStr, existentVariablesStr ,allVariablesStr ;
		ArrayList<Integer> trace  ;
		TransformationStack tStack;
		SomAssignatesDerivationTree derivTree;
		ArrayList<CollectedVariable> collectedVariables = null;
		CollectedVariable collectedVariable ;
		Variables variables = somData.getVariables();
		Variable  tVar;
		
		
		
		if (out==null)out= somData.getOut();
		ix=0;
		
		if (transformationModel==null){
			return;
		}
		if ((derivationTrees==null) || (derivationTrees.size()==0)){
			return;
		}
			
		int nn = derivationTrees.get(0).collectedVariables.size();
		if (nn>0){
			nn=nn+1-1;
		}
		
		ix=0;
		existentVariablesStr = variables.getLabelsForVariablesList(variables);
		
		
		existentVariablesStr = variables.getLabelsForVariablesList(variables, 0); // 0=raw 1=derived 2=all
		allVariablesStr = variables.getLabelsForVariablesList(variables, 2); // just for debug...
		
											out.print(2,"creating derivation trees for variables...");
		for (int i=0;i<derivationTrees.size();i++){ // should just contain the raw variables
											out.printprc(2, i, derivationTrees.size(), derivationTrees.size()/10, "");
			derivTree = derivationTrees.get(i);
			
			guidBaseVar = derivTree.baseGuid ; // this is the guid of the transform stack, which is attached to the variable 
			varlabel = derivTree.baseVariableLabel ;
			
if ( (varlabel.toLowerCase().startsWith("xd")) ||
	 (varlabel.toLowerCase().contains("d1"))){
	int k;
	k=0;
}			
			if (guidBaseVar.length()>0){
				// this should be always true, since we start with non-derived variables only
				// variableCollected( 1, guidBaseVar, null);
				if (variableCollected( guidBaseVar ) == false){
											out.print(4, "...checking transformation influences for variable <"+varlabel+">...");
											
					collectedVariables = derivTree.getCollectedVariables(); // provides a new empty list for the beginning, if necessary
					
					collectedVariables = collectVariablesInTree( guidBaseVar, collectedVariables, derivTree, 0); // recursively traverse the variables
				
					derivTree.setCollectedVariables(collectedVariables);
				}else{
					// before we delete it, we have to check whether it is itself a derived variable
					derivTree.clear();
				}
				
				// collectedVariables.clear(); // ??? 
			}
		} // i -> all items in derivationTrees

		ArrayList<CollectedVariable> cvs;
		
		derivedVariablesStr = variables.getLabelsForVariablesList(variables, 1); // 0=raw 1=derived 2=all
		
		trace  = new ArrayList<Integer> ();

		
		for (int i=0;i<derivedVariablesStr.size();i++){
											out.printprc(3, i, derivedVariablesStr.size(), derivedVariablesStr.size()/10, "");
			cvs = new ArrayList<CollectedVariable>();
			
			varlabel = derivedVariablesStr.get(i) ;
			tix = transformationModel.getIndexByLabel(varlabel);

 

			trace.clear();  
			trace = transformationModel.findTransformationRootStackIndex( varlabel, 1, trace);

if (trace.size()>2){
	int z;
	z=0;
}
			Collections.sort(trace);   
			trace = ArrUtilities.removeDoubleEntries(trace) ;
			ArrayList<Integer> traceRoots = new ArrayList<Integer> ();
			traceRoots.clear() ;
			
			// transform the finds into "CollectedVariable"s
			for (int t = 0; t < trace.size(); t++) {

				tVar = variables.getItem(trace.get(t) );
				
				if (tVar.isDerived() == false){
					traceRoots.add( trace.get(t) );
				}
				if (tVar.isDerived() == true){
					// we do not include raw variables here as a trace for derivation trees (yet we would need it for the root view!!)
					// 
					tStack = transformationModel.getItemByLabel( tVar.getLabel() ) ;
					
					if (tStack!=null){
						CollectedVariable cv = new CollectedVariable(tStack.transformGuid, tStack.varLabel, trace.get(t));
						cvs.add(cv);
					}
				}
				
				
			}// t->
			
			
			if (cvs.size() >= 1) {
 
				int min = ArrUtilities.arraymin(traceRoots, -1);
				
				if (min >= 0) {
					// insert it to ANY raw variable listed in the trace:
					// we begun with the derived end, and proceeded to the raw

					for (int t = 0; t < traceRoots.size(); t++) {

						// check the traces whether they are "basic" == not derived, then add the rest of the trace there too
						
						tVar = variables.getItem(traceRoots.get(t));
						if (tVar.isDerived() == false) {
							
							// put it to the respective tree that is attached to the
							// current variable under scrutiny
							String tGuid = transformationModel.getItem( traceRoots.get(t) ).transformGuid;
							derivTree = getTreeByGuid(tGuid);

							if (derivTree != null) {
								// this will check for double entries before actually adding
								derivTree.addCollectedVariables(cvs);

							} // tree found ?
							else {
								out.printErr(2, "derivation tree chain not found for variable : " + varlabel);
							}
							
							
							
						} //raw ?
						min=min+1-1;
					} //t->
				} // reasonable values in list of trace indexes ?

				

			} // any cvs found ?
			
			// put it to the roots, possibly before the trees...
			// derivRoot = getRootByGuid(tGuid);
			// derivRoot.addTraces( varLabel, trace) ;
		}
		
		
		// remove cleared items
		int d = derivationTrees.size()-1;
		while (d>=0){
	
			derivTree = derivationTrees.get(d);
			
			if (derivTree.baseGuid.length()==0){ 
				derivationTrees.remove(d) ;
			}else{
				derivTree.sortCollectedVariables(1);
			}
			
			d--;
		}
		
	 
		ix=0;
	}


	private void addRootsTraces(String varlabel, ArrayList<Integer> trace) {
		// TODO Auto-generated method stub
		
		//derivationRoots.
		
	}


	private ArrayList<CollectedVariable> collectVariablesInTree( String transformStackGuid, 
																 ArrayList<CollectedVariable> collectedVariables, 
																 SomAssignatesDerivationTree derivTree,
																 int depth) {
		 
		int tix, dix, vix, algotype;
		String varlabel="", sGuid ;
		TransformationStack tStack;
		SomAssignatesDerivation  dTree;
		StackedTransformation stackItem;
		String tGuid, vGuid;
		Variables variables;
		CollectedVariable collectedVariable;
		
		
		try{
			variables = somData.getVariables();
			tStack = transformationModel.findTransformationStackByGuid(transformStackGuid) ;
			
			if (tStack==null){
				return collectedVariables;
			}
			tix = tStack.index ;
			 
			varlabel = tStack.varLabel;
			 

			
			sGuid = tStack.transformGuid ; // guid of stack, should be the same as the input variable "transformStackGuid"
			vix = variables.getIndexByLabel(varlabel) ;
			  
			// the variable should not be added to the current tree
			if (variableCollectedByBaseVariable( transformStackGuid, collectedVariables) == false){
				
				// adding the incoming variable to the list
				collectedVariable = new CollectedVariable( transformStackGuid, varlabel, vix);
				collectedVariables.add(collectedVariable) ;
			}else{
				return collectedVariables;
			}
			tix=0;
			String stackBaseVLabel = tStack.varLabel ;							
			for (int t=0;t<tStack.items.size();t++){
				stackItem = tStack.getItem(t);
				algotype = stackItem.getAlgorithmType() ;
				
				/*
				 * ATTENTION: not all transfers of data between columns are due to the push of copy/writers !
				 *            formulas and other multi-source algorithms may fetch/pull the data !!   
				 */
				if (algotype == AlgorithmIntf._ALGOTYPE_WRITER){
					
					String vlabel = stackItem.outputColumnLabel ;
					String guid = stackItem.outputColumnId;
					// transformId = stackItem.idString;
					
					// recursively traverse the variables
					collectedVariables = collectVariablesInTree( guid, collectedVariables,derivTree, depth+1); 
					
				}
				if ((algotype == AlgorithmIntf._ALGOTYPE_VALUE) && 
					(((AlgoTransformationIntf)stackItem.algorithm).getInputColumnsCount()>1)){
					
					// get all input columns
					String[] parentVars = tStack.baseVariable.getParentItems(  ) ;
					
					// extend the trees where those columns appear by the found variable
					for (int p=0;p<parentVars.length;p++){
					
						String backtrcVarStr = parentVars[p];
if (backtrcVarStr.toLowerCase().contains("unde_seit_c")){
	int k;
	k=0;   // should be added to "gruendungsdatum"...
}
						
						// get an tree where this variable is part of
						collectedVariables = introduceVariableToTreesByParent( collectedVariables, backtrcVarStr, varlabel, sGuid);
						// what's about cascaded arithmetic expressions?
					}
				}
				
				// search based on guids
				
				String stiGuid = stackItem.idString;
				String stackGuid = tStack.transformGuid ;
				
				// search all references 
				// collectedVariables
				
			}
			
		}catch(Exception e){
			System.err.println( "error while collecting dependencies in transformation model, varlabel: "+varlabel+" ,  depth:"+depth);
			e.printStackTrace();
		}
		
		
		return collectedVariables;
	}
	
	/**
	 * 
	 * well, we check the variable "treeVarlabel", from which we know that it has a parent variable
	 * yet, we should not just put  
	 * 
	 * @param parentVarLabel
	 * @param treeVarlabel
	 * @param guid
	 * @return
	 */
	private ArrayList<CollectedVariable> introduceVariableToTreesByParent( ArrayList<CollectedVariable> collectedVariables, 
																		   String parentVarLabel, String treeVarlabel, String guid) {

		ArrayList<SomAssignatesDerivationTree> dTrees;
		SomAssignatesDerivationTree dTree,pdTree ;
		boolean cb ;
		int vix ,tix,pvix;
	 
		ArrayList<TransformationStack> tStacks;
		TransformationStack tStack, ptStack;
		CollectedVariable cv=null;
		ArrayList<String> tsParents;
		
		tStacks = transformationModel.variableTransformations ; 
		
		tix = transformationModel.getIndexByLabel(parentVarLabel) ;
		tStack = tStacks.get(tix) ;
		 
		tsParents =  new ArrayList<String>(Arrays.asList( tStack.baseVariable.getParentItems() )) ;
		tsParents.addAll( tStack.inputVarLabels ) ;
		
		if (tsParents.size()>0){  
			
			for (int p=0;p<tsParents.size();p++){
				parentVarLabel = tsParents.get(p) ;
				// this now is more "basic" == closer to the raw variable than the one provided as input
				// for this we have to get its tree
				 
				dTrees = transformationModel.derivations.getTreesByVariable(0, parentVarLabel); // 0=base,
				
				if ((dTrees!=null) && (dTrees.size()>0)){
					pdTree = dTrees.get(0) ;
					
					int ptix = transformationModel.getIndexByLabel(parentVarLabel) ;
					ptStack = tStacks.get(ptix) ;
					
					if (variableCollected( 0, ptStack.transformGuid, pdTree.collectedVariables) ==false){
						pvix = somData.getVariables().getIndexByLabel(treeVarlabel) ;
						cv = new CollectedVariable( tStack.transformGuid, treeVarlabel,pvix );
						pdTree.collectedVariables.add( cv );	
					}
					
					
					if (variableCollected( 0, ptStack.transformGuid, pdTree.collectedVariables) ==false){
						
						// cv = new CollectedVariable( tStack.transformGuid, treeVarlabel,pvix );
						// pdTree.collectedVariables.add( cv );	
					}

					// we have to dig down to the ground = the variable does not have input columns as parents
					// thereby we have to collect the variables on the way
					pdTree.collectedVariables = introduceVariableToTreesByParent( pdTree.collectedVariables, parentVarLabel, treeVarlabel, guid);
				}
				return collectedVariables;
			}
		}
		
		dTrees = transformationModel.derivations.getTreesByVariable(1, parentVarLabel); // 0=base,  1=any

		
		for (int d=0;d<dTrees.size();d++){
			
			dTree = dTrees.get(d) ;
			
			vix = somData.getVariables().getIndexByLabel(treeVarlabel) ;
			cv = new CollectedVariable( guid, treeVarlabel,vix ); // 
			
			// add it, if it is not included so far in the current collection
			cb = variableCollected( 0, guid, dTree.collectedVariables) ;
			
			if (cb==false){
				dTree.getCollectedVariables().add(cv) ; 
			}
		} // d->
		
		return collectedVariables ;
	}


	private boolean variableCollectedByBaseVariable( String tStackGuid, ArrayList<CollectedVariable> collectedVars) {

		boolean contained = false;
		SomAssignatesDerivationTree derivTree ; 
		CollectedVariable cv , baseCv;
		 
		
		if ((contained==false) && (collectedVars!=null) && (collectedVars.size()>0)){
			
			baseCv = collectedVars.get(0);
			
			for(int i=0;i<collectedVars.size();i++){
				cv = collectedVars.get(i);
				if (cv.transformStackGuid.contentEquals(tStackGuid)){
					contained=true;
					break;
				}
			}
		}
		return contained ;
	}
	
	private boolean variableCollected( String tStackGuid) {
		return variableCollected( 1, tStackGuid, null);
	}
	
	private boolean variableCollected( int anymode , String tStackGuid, ArrayList<CollectedVariable> collectedVars) {
		
		boolean contained = false;
		SomAssignatesDerivationTree derivTree ; 
		CollectedVariable cv ;
		
		if (anymode>=1){
			for (int i = 0; i < derivationTrees.size(); i++) {

				derivTree = derivationTrees.get(i);

				if (derivTree.collectedVariables != null) {

					for (int d = 0; d < derivTree.collectedVariables.size(); d++) {
						cv = derivTree.collectedVariables.get(d);
						if (cv.transformStackGuid.contentEquals(tStackGuid)) {
							contained = true;
							break;
						}
					}// d->
					if (contained) {
						break;
					}
				}

			}
		}
		if ((contained==false) && (collectedVars!=null) && (collectedVars.size()>0)){
			for(int i=0;i<collectedVars.size();i++){
				cv = collectedVars.get(i);
				if (cv.transformStackGuid.contentEquals(tStackGuid)){
					contained=true;
					break;
				}
			}
		}
		return contained ;
	}


	/**
	 * 
	 * Trees : For each raw variable, we create a list that contains all variables which are dependent on it  </br>
	 * yet, we do not save the tree, only the list of variables that are dependent from the base  
	 * 
	 * transformationModel.variableTransformations.get() -> TransformationStack
	 * 
	 */
	private int initializeTrees(){
		
		int result=-1;
		int vn=0,ix,tix;
		String varLabel,vGuid;
		ArrayList<String> colheaders;
		ArrayList<TransformationStack> tStacks;
		TransformationStack tStack;
		SomAssignatesDerivationTree dT,derivTree; //  
		
		transformationModel = somTransformer.transformationModel ;
		
		try{
			Variables variables = somData.getVariables();
	
			if (transformationModel==null){
				out.printErr(2, "the <transformationModel> has been unexpectadly found to be =null.");
				return -5;
			}
			
			derivationTrees.clear() ;
			
			tStacks = transformationModel.variableTransformations ;
			colheaders = somData.getDataTable().getColumnHeaders();
			
			vn = colheaders.size();
			int vz = variables.size() ;
			ix=0;
			
			ArrayList<String> existentVariablesStr ;
			existentVariablesStr = variables.getLabelsForVariablesList(variables,0);
			
			result=1;
			vz = existentVariablesStr.size() ;
			
			for (int i=0;i<vz;i++){
				result = -3;
				
				varLabel = existentVariablesStr.get(i);
				Variable variable = variables.getItemByLabel(varLabel);

				if (variable.isDerived()==false){

					derivTree = new SomAssignatesDerivationTree( somData );
					derivTree.baseVariableLabel = varLabel ;
					tix = transformationModel.getIndexByLabel( derivTree.baseVariableLabel ) ;
					
					if (tix>=0){
						
						tStack = transformationModel.getVariableTransformations().get(tix);
		
						if (tStack.inputVarLabels!=null){
							int tsinn = tStack.inputVarLabels.size();

							if (tsinn > 0) {
								continue;
							}
						}
							
						vGuid = tStack.getGuid() ;
						String[] pVar = tStack.baseVariable.getParentItems();
						int pn = pVar.length ;
						
						derivTree.baseGuid = vGuid ; // guid of stack ~ serves also as GUID of column = used variable;
						derivTree.baseVariableIndex = somData.getVariables().getIndexByLabel( varLabel ) ;
						
						// is there another tStack, which uses this one as output ?
						// here we check a forward-link which equals the ID of the currently selected tStack
						// relevant field in tStack is  tStack.outputColumnIds 
						
						int potix = transformationModel.getTransformStackIndexByOutputGuid( vGuid ); 
						if (potix<0){
							derivationTrees.add(derivTree) ;
						}else{
							
							// actually, we should drill down, until there is no inputVarLabel (.length = 0 ?)
							String tGuid = tStacks.get(potix).transformGuid ;
							dT = this.getTreeByGuid(tGuid);
							if (dT != null) {
								int vix = somData.getVariables().getIndexByLabel(varLabel);
								CollectedVariable cv = new CollectedVariable(vGuid, varLabel, vix);
								dT.getCollectedVariables().add(cv);
							}
						}
						
					} // stack available for variable ?

				} // raw variable ?
								
			} // i-> all variables in raw file
			
			vn = derivationTrees.size();
			if (vn>0)result=0;
			
		}catch(Exception e){
			e.printStackTrace() ;
			result=-7;
		}
		return result ;
	}


	/**
	 * 
	 *  Roots : for each derived variable, we determine the set of raw variables necessary to calculate it 
	 *  
	 *  raw variables will point to itself, 
	 *   
	 */
	private void initializeRoots(){
		
		int vn;
		ArrayList<String> colheaders;
		ArrayList<TransformationStack> tStacks;
		TransformationStack tStack;
		SomAssignatesDerivation derivRoots;
		
		tStacks = transformationModel.variableTransformations ;
		colheaders = somData.getDataTable().getColumnHeaders();
		
		vn = colheaders.size();
		
		
		for (int i=0;i<vn;i++){
			
			
			
		} // i-> all variables
	}

	
	
	
	
	
	
	// ------------------------------------------------------------------------
	
	public TransformationModel getTransformationModel() {
		return transformationModel;
	}

	public void setTransformationModel(TransformationModel transformationModel) {
		this.transformationModel = transformationModel;
	}



	 
}

package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.logging.PrintLog;



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
	
	transient SomFluidProperties sfProperties;
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
		
		derivationTrees = new ArrayList<SomAssignatesDerivationTree>();
		
		initializeTrees();
		initializeRoots() ;
		
	}
	
	public void createDerivationTrees(){
		
		int ix;
		String guid, varlabel ;
		ArrayList<TransformationStack> tStacks;
		TransformationStack tStack;
		SomAssignatesDerivationTree derivTree;
		ArrayList<CollectedVariable> collectedVariables = null;
		CollectedVariable collectedVariable ;
		Variables variables = somData.getVariables();
		
		if (out==null)out= somData.getOut();
		ix=0;
		
		if (transformationModel==null){
			return;
		}
		
		for (int i=0;i<derivationTrees.size();i++){
			
			derivTree = derivationTrees.get(i);
			
			guid = derivTree.baseGuid ; // this is the guid of the transform stack, which is attached to the variable 
			varlabel = derivTree.baseVariableLabel ;
			
			if (guid.length()>0){
														
				if (variableCollected( guid ) == false){
											out.print(2, "...checking transformation indluences for variable <"+varlabel+">...");
					collectedVariables = new ArrayList<CollectedVariable>(); // provide an empty list for the beginning
				
					collectedVariables = collectVariablesInTree( guid, collectedVariables, derivTree, 0); // recursively traverse the variables
				
					derivTree.setCollectedVariables(collectedVariables);
				}else{
					derivTree.clear();
				}
				
				// collectedVariables.clear(); // ??? 
			}
			
		} // i -> all items in derivationTrees
		
		// remove cleared items
		int d = derivationTrees.size()-1;
		while (d>=0){
	
			derivTree = derivationTrees.get(d);
			
			if (derivTree.baseGuid.length()==0){ 
				derivationTrees.remove(d) ;
			}
			
			d--;
		}
		
		ix=0;
	}


	// ------------------------------------------------------------------------
	
	public SomAssignatesDerivationTree getTreeByGuid(int mode, String tGuid){
		return null;
	}
	
	public SomAssignatesDerivationRoot getRootByVariable(int mode, String varlabel){
		return null;
	}
	/**
	 * a derived variable could be present in several trees, so we have to check them all
	 * @param mode  0=compare to base variable, 1=compare to any variable in the tree 
	 */
	public ArrayList<SomAssignatesDerivationTree> getTreesByVariable(int mode, String varlabel){
		
		ArrayList<SomAssignatesDerivationTree> dTrees = new ArrayList<SomAssignatesDerivationTree>();
		SomAssignatesDerivationTree dTree=null, dT;
		CollectedVariable cv ; 
		
		for (int i=0;i<derivationTrees.size();i++){
		
			dT = derivationTrees.get(i) ;
			if (mode<=0){
				if (dT.baseVariableLabel.contentEquals(varlabel)){
					dTrees.add(dTree) ;
				}
			}
			if (mode>=1){
				boolean found = false;
				
				for (int c=0;c<dT.collectedVariables.size();c++){
					
					cv = dT.collectedVariables.get(c);
					if (cv.varlabel.contentEquals(varlabel) ){
						found = true;
						dTree = dT;
						break;
					}
				} // c-> all collected variables
				if (found){
					dTrees.add(dTree) ;
				}
			}
			
		} //i->
		
		return dTrees;
	}


	public ArrayList<String> getVariablesOfTree(SomAssignatesDerivationTree dTree) {
		ArrayList<String> vars = new ArrayList<String>(); 
		
		for (int i=0;i<dTree.collectedVariables.size();i++){
			
			
		} // i->
		
		return vars;
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
		ArrayList<String> colheaders;
		ArrayList<TransformationStack> tStacks;
		TransformationStack tStack;
		SomAssignatesDerivationTree derivTree; //  
		
		transformationModel = somTransformer.transformationModel ;
		
		try{
			

			if (transformationModel==null){
				out.printErr(2, "the <transformationModel> has been unexpectadly found to be =null.");
				return -5;
			}
			tStacks = transformationModel.variableTransformations ;
			colheaders = somData.getDataTable().getColumnHeaders();
			
			vn = colheaders.size();
			result=1;
			
			for (int i=0;i<vn;i++){
				result = -3;
				
				derivTree = new SomAssignatesDerivationTree( somData );
				
				derivTree.baseVariableLabel = colheaders.get(i);
				tix = transformationModel.getIndexByLabel( derivTree.baseVariableLabel ) ;
				
				if (tix>=0){
					tStack = transformationModel.getVariableTransformations().get(tix);

					derivTree.baseGuid = tStack.getGuid() ; // guid of stack ~ serves also as GUID of column = used variable;
					derivTree.baseVariableIndex = somData.getVariables().getIndexByLabel( derivTree.baseVariableLabel ) ;
					
				} // stack available for variable ?
				
				derivationTrees.add(derivTree) ;
				
			} // i-> all variables in raw file
			
			if (vn>0)result=0;
			
		}catch(Exception e){
			e.printStackTrace() ;
			result=-7;
		}
		return result ;
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
			 
if (varlabel.toLowerCase().startsWith("rechtsform_c")){
	int k;
	k=0;
}
			
			sGuid = tStack.transformGuid ; // guid of stack, should be the same as the input variable "transformStackGuid"
			vix = variables.getIndexByLabel(varlabel) ;
			  
			// the variable should not be added to any of the trees
			if (variableCollectedByBaseVariable( transformStackGuid, collectedVariables) == false){
				
				// adding the incoming variable to the list
				collectedVariable = new CollectedVariable( transformStackGuid, varlabel, vix);
				collectedVariables.add(collectedVariable) ;
			}else{
				return collectedVariables;
			}
			tix=0;
											
			for (int t=0;t<tStack.items.size();t++){
				stackItem = tStack.getItem(t);
				algotype = stackItem.algorithmType ;
				
				if (algotype == AlgorithmIntf._ALGOTYPE_WRITER){
					
					String vlabel = stackItem.outputColumnLabel ;
					String guid = stackItem.outputColumnId;
					// transformId = stackItem.idString;
					
					// recursively traverse the variables
					collectedVariables = collectVariablesInTree( guid, collectedVariables,derivTree, depth+1); 
					
				}
			}
			
		}catch(Exception e){
			System.err.println( "error while collecting dependencies in transformation model, varlabel: "+varlabel+" ,  depth:"+depth);
			e.printStackTrace();
		}
		
		
		return collectedVariables;
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
	
	private boolean variableCollected(String tStackGuid) {
		return variableCollected( tStackGuid, null);
	}
	
	private boolean variableCollected( String tStackGuid, ArrayList<CollectedVariable> collectedVars) {
		
		boolean contained = false;
		SomAssignatesDerivationTree derivTree ; 
		CollectedVariable cv ;
		
		
		for (int i=0;i<derivationTrees.size();i++){
			
			derivTree = derivationTrees.get(i);
			
			if (derivTree.collectedVariables!=null){
				
				for(int d=0;d<derivTree.collectedVariables.size();d++){
					cv = derivTree.collectedVariables.get(d);
					if (cv.transformStackGuid.contentEquals(tStackGuid)){
						contained=true;
						break;
					}
				}// d->
				if (contained){
					break;
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

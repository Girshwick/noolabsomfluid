package org.NooLab.somfluid.app;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.somtransform.StackedTransformation;
import org.NooLab.somtransform.TransformationModel;
import org.NooLab.somtransform.TransformationStack;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;

import org.NooLab.utilities.strings.StringsUtil;


/**
 * 
 * TODO:  statistical description gets not exported, because algorithms   
 *  	  are not included in transformation model, only their names -->> we need the parameters of algorithms, e.g. also for NVE !!!
 * 
 *
 */
public class SomAppClassifier implements SomProcessIntf, Serializable{

	private static final long serialVersionUID = 1300587404052091239L;


	public static final int _MODE_FILE        = 1 ; 
	public static final int _MODE_WIFISERVICE = 3 ;
	
	
	SomFluidFactory    sfFactory ;
	
	SomAppProperties   soappProperties ; 
	
	SomApplicationIntf somApplication;
	SomAppModelLoader  soappLoader;
	

	SomAppTransformer  soappTransform ;

	SomDataObject somData;
	
	int nodeCount=0;
	
	ArrayList<SomAppSomObject> somObjects = new ArrayList<SomAppSomObject>() ; ; 
	int somObjectsCount = 0; 
	
	boolean transformationModelImported=false;
	boolean transformationsExecuted=false;
	
	// is a guid-identifiable object, containing guid, universal serial, data section, status, commands, results
	public ArrayList<Object> dataQueue = new ArrayList<Object>(); 
	transient ClassificationProcess classProcess=null;

	
	transient private ArrayList<String> transformedVariables = new ArrayList<String>();
	transient private ArrayList<String> postponedVariables   = new ArrayList<String>();

	
	transient PrintLog out;
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities();
	// ========================================================================
	
	public SomAppClassifier(SomApplicationIntf somapplication, SomFluidFactory factory) {
		 
		somApplication = somapplication;
		sfFactory = factory ;
		
		soappProperties = sfFactory.getSomAppProperties() ;
		soappLoader = somApplication.getSomAppModelLoader() ;
	
		
		
		out = sfFactory.getOut() ;
	}
	// ========================================================================	
	
	public boolean prepare() {
		boolean rB=false;
		
		try{	

			// soappClassify = soappLoader.getSoappClassifier() ;
			soappTransform = soappLoader.getSoappTransformer() ;
			
			somData = somApplication.getSomData() ;
			
			somObjectsCount = somObjects.size();
			
			String loadedModel = soappLoader.getActiveModel() ;
			//if (soappClassify.somObjects.size()>0){
			if (somObjects.size()>0){
				
				//SomAppSomObject somObject = soappClassify.somObjects.get(0);
				SomAppSomObject somObject = somObjects.get(0);
				
				nodeCount = somObject.soappNodes.getNodes().size();

				if (nodeCount >= 3){
				
					rB= transformIncomingData();
					 
					
				} // nodeCount >= 3?
			} // soappClassify.somObjects?
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return rB;
	}


	public String createDataTask() {
		 
		
		SomAppDataTask soappDataTask = new  SomAppDataTask( ) ;
		
		// sfFactory, somApplication, this,
		
		soappDataTask.setDataObject( somData );
		
		dataQueue.add( soappDataTask );
		
		return soappDataTask.getGuidStr() ;
	}
	/**
	 * this cares for correct object references (SomDataObject, TransformationModel, etc.) and
	 * applies the transformation stack to the provided data
	 * 
	 * as a result, the table with normalized data will be available
	 * only necessary transformations will be applied
	 * 
	 */
	protected boolean transformIncomingData() {
		boolean rB=false;
		ArrayList<String> targetStruc ;
		
		
		try {
			
			if (transformationModelImported == false){
				establishTransformationModel();
			}
			
			// soappTransform.requiredVariables
			if (transformationsExecuted == false){
				
					executeTransformationModel();
			}
			rB = transformationModelImported && transformationsExecuted ;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rB;
	}
		
	
	@SuppressWarnings("unchecked")
	private void executeTransformationModel() throws Exception {
		
		String varLabel;
		int ix, tix, rvix,ctvix, rdix,rdcix, popoVarIx;
		
		int[] tformats;

		Variables variables;
		Variable variable;

		DataTable rawDataTable, normDataTable;
		ArrayList<Double> cellvalues;
		ArrayList<String> columnVarLabels = new ArrayList<String>();
		ArrayList<TransformationStack> stackList;

		TransformationModel transformModel;
		TransformationStack varTStack;
		StackedTransformation sT;

		SomTransformer sot; //just for debug 

		try{
		

			// --------------------------------------------
			rawDataTable = soappTransform.getDataTableObj();
			columnVarLabels = rawDataTable.getColumnHeaders();

			int columnLen = rawDataTable.getColumn(0).size() ;
			normDataTable = new DataTable(somData, true);

			normDataTable.setColumnHeaders(columnVarLabels);
			tformats = new int[normDataTable.getColcount()];
			for (int i = 0; i < tformats.length; i++) {
				tformats[i] = 1;
			}
			normDataTable.setFormats(tformats);
				
				/*
				 * exporting the transform model should also provide a list of variables that appear in the chained trees
				 * these we would need to select the necessary stacks, to avoid the superfluous ones
				 */

			transformModel = soappTransform.getTransformationModel() ;
			variables = somData.getVariables() ;
				
				// we need to refer to the imported model for setting up the target table = normDataTable
					// 1. first the data which are provided by the input
				
				for (int i=0;i<rawDataTable.getColcount();i++){
					DataTableCol column = new DataTableCol(normDataTable, i);
					 
					normDataTable.getDataTable().add(column) ;
				}
					// 2. the columns that are derived, we do not distinguish or analyze the dependencies here
				
			stackList = transformModel.getVariableTransformations();
					
			for (int i=0;i<stackList.size();i++){
			
				// base variable is available through serialized persistence, it has been loaded through transform.xml
				
				Variable perBaseVar = stackList.get(i).getBaseVariable();
				varLabel = perBaseVar.getLabel();

				if ((perBaseVar != null) && (perBaseVar.isDerived())) {
					ctvix = soappTransform.requiredchains.indexOf(varLabel);
					if (ctvix >= 0) {

						Variable newCVar = new Variable(perBaseVar);

						DataTableCol column = new DataTableCol(normDataTable, i);
						normDataTable.getDataTable().add(column);
						normDataTable.getColumnHeaders().add(newCVar.getLabel());
						variables.additem(newCVar);
					}

				}
			}
				// should be available, comes with the TransformationModel-object: it contains trees & chains...
				// SomAssignatesDerivations soappDerives = soappTransform.getSomDerivations() ;
			
			columnVarLabels = variables.getLabelsForVariablesList(variables);
			
			
			boolean ready=false;
			
			while (ready==false){
				ready=true;
				
				try{
					
					for (int i = 0; i < columnVarLabels.size(); i++) {
						varLabel = columnVarLabels.get(i);
												out.print(2,"establishing transformation stack (1) for variable : "+ varLabel) ;
												
						popoVarIx = postponedVariables.indexOf(varLabel) ;				
						if ((popoVarIx>=0) || (transformedVariables.indexOf(varLabel)<0)){
							
							if (transformVariable( normDataTable, rawDataTable, varLabel, (popoVarIx>=0) ) ){
								 
								transformedVariables.add(varLabel) ;
								if (popoVarIx>=0){
									postponedVariables.remove( popoVarIx);
								}
							}else{
								postponedVariables.add(varLabel) ;
							}
							
						} // still to treat ?
						
					} // all variables
					
				}catch(Exception e){
					e.printStackTrace();
				}
				
				ready = postponedVariables.size()==0;
			}
			 
			// soappTransform.requiredVariables
				
			// updating all stacks from the perspective of the normalized Table

			columnVarLabels = normDataTable.getColumnHeaders();

			for (int i = 0; i < columnVarLabels.size(); i++) {

				boolean calculateCol = false;
				varLabel = columnVarLabels.get(i);
				variable = variables.getItemByLabel(varLabel);

				// obsolete: variable.setRawFormat(rawDataTable.getFormats()[i]);

				rvix = soappTransform.requiredVariables.indexOf(varLabel);

				calculateCol = (rvix >= 0) || (variable.isDerived());

				if (calculateCol) {

				}

			} // -> all columns in normalized table

			// filling all empty columns in norm table with -1, such that they are defined
			normDataTable.getColumnHeaders().trimToSize();  columnVarLabels = normDataTable.getColumnHeaders();
			DataTableCol tablecol;
			normDataTable.setTablename("normalized");
			
			for (int i=0;i<normDataTable.getColcount();i++){
				
				varLabel = columnVarLabels.get(i) ;
				tablecol = normDataTable.getColumn(i) ;
				
				if (tablecol.getCellValues().size()==0){
				
					cellvalues = ArrUtilities.createCollection( columnLen, -1.0) ;
					tablecol.getCellValues().addAll(cellvalues) ;
				}
				
			}
			
			// create row perspective
			normDataTable.createRowOrientedTable();		
	
			transformationsExecuted = true;
		}catch(Exception e){
			e.printStackTrace();
		}
	 
	}
	
	@SuppressWarnings("unchecked")
	private boolean transformVariable(	DataTable normDataTable, DataTable rawDataTable, 
										  
										String varLabel,
										boolean revisitedVariable ) {
		boolean transformSuccess  ;
		
	 
		int ix, tix, rvix,ctvix, rdix,rdcix;
		int columnLen, failure=0;
		
		int[] tformats;

		Variables variables;
		Variable variable;

		ArrayList<Double> cellvalues;
		 
		ArrayList<TransformationStack> stackList;

		TransformationModel transformModel;
		TransformationStack varTStack;
		StackedTransformation sT;
		
		
		
		transformSuccess = false;
		try{
			
			transformModel = soappTransform.getTransformationModel() ;
			variables = somData.getVariables() ;
			
			if (revisitedVariable){
				// remove previously introduced stack and data (first data in algo, then algo the stackedTransforms
				
			}
			
			columnLen = rawDataTable.getColumn(0).size() ;

			//for (int i = 0; i < columnVarLabels.size(); i++) 
			{
				// varLabel = columnVarLabels.get(i);
				
				variable = variables.getItemByLabel(varLabel);

				// rdcix = rawDataTable.getColumnHeaders().indexOf(varLabel);
				rdcix = variables.getIndexByLabel(varLabel);
				if (rdcix >= 0) {
					int[] dFormats = rawDataTable.getFormats();
					if (rdcix < dFormats.length) {
						variable.setRawFormat(dFormats[rdcix]);
					} else {
						variable.setRawFormat(1);
						// == the out-format of the StackedTransformation in the parent stack !!!!
					}
				}

				rvix = soappTransform.requiredVariables.indexOf(varLabel);
				ctvix = soappTransform.requiredchains.indexOf(varLabel);
                // TODO better: using requiredRoots...  which would avoid unnecessary work
				if ((ctvix >= 0) || (revisitedVariable)){
					// applying the tStack to this variable

					// getting the index on the collection of transformation stacks by variable label
					tix = transformModel.getIndexByLabel(varLabel);

					// get the index in the raw table (which we got as input data for the classification task)
					rdix = rawDataTable.getColumnHeaders().indexOf(varLabel);
												out.print(2,"establishing transformation stack (2) for variable : "+ varLabel) ;
					// selecting the transformation stack by previously determined index
					varTStack = transformModel.getVariableTransformations().get(tix);
					varTStack.setFirstFormat(variable.getRawFormat());
					varTStack.setBaseVariable(variable); varTStack.getItems().trimToSize();

					for (int s = 0; s < varTStack.size(); s++) {

						sT = varTStack.getItem(s);

						sT.setPluginSettings(soappProperties.getPluginSettings());
						sT.setTransformOriginator(soappProperties.getPluginSettings().getTransformationOriginator());

						if (sT.getAlgorithmType() < 0) {
							sT.setAlgorithmType(varTStack.determineAlgoType(sT.getAlgorithmName()));
						}

						sT.createAlgoObject(sT.getAlgorithmType());
							
							if (sT.getAlgorithm() == null){
						    	throw(new Exception("The requested algorithm "+sT.getAlgorithmName()+" could not be instantiated: instantiation failed (object=null).")) ;
							}
							
							if (varTStack.defineAlgorithmObject( sT )==false){
								throw(new Exception("The requested algorithm "+sT.getAlgorithmName()+" could not be configured.")) ;
							}
						

	if (sT.getAlgorithmName().toLowerCase().contains("arith")){
		int k;
		k=0;  
	}
						
						AlgorithmIntf dyAlgo = ((AlgorithmIntf)sT.getAlgorithm()) ; 
						dyAlgo.setParameters( sT.getAlgoParameters() ) ;
						 
						if (sT.getAlgorithmType() == AlgorithmIntf._ALGOTYPE_PASSIVE){
						// StatisticalDescriptionStandard@f37a62	
						}
						dyAlgo.getParameters().setRecalculationBlocked(true) ;
						// it is on behalf of the algorithm whether it can be blocked or not;
						// e.g. LinNorm does not offer the possibility, while stats description does. 

						if (sT.getInData() == null) {

							sT.createInDataContainer();
							sT.createOutData();
						}

						if ((s==0) && (sT.getInData().size() == 0)) {
							
							if ((variable.isDerived()) || (rvix<0)) {
								// this establishes a reference to the outdata of the stack
								// note that arithmetic expressions do NOT copy data forwardly
								soappTransform.connectTransformStacksForData(varTStack, 1, false); //
							} else {
							}
							

							// dependent on data format, we have to introduce values differently
							if (sT.getInFormat() > DataTable.__FORMAT_ORGINT) { 
								// text, string, dates are imported as strings
								if (rvix >= 0) { 
									ArrayList<String> cellvalueStr = somData.getDataTable().getColumn(rdix).getCellValueStr();
									sT.getInData().add(cellvalueStr);
								}
							} else {
								if (rvix >= 0) { 
									// only for those we have raw values
									cellvalues = somData.getDataTable().getColumn(rdix).getCellValues();
								} else {
									cellvalues = ArrUtilities.createCollection(columnLen, -1.0);
									// this will be corrected on update
								}
								sT.getInData().add(cellvalues);
							}
							int n = sT.getInData().size();
							if (n == 0) {

							}

						}

						if (s == 0) {
							sT.setInFormat(variable.getRawFormat());

							// setInData(null);
						}

						// before updating we have to check if this stack position contains a writer-algorithm
						if (sT.getAlgorithmType() == AlgorithmIntf._ALGOTYPE_WRITER) {

							// we should not introduce it here
							// introduceDerivedVariable( sT, varTStack,normDataTable ,variable );
							// create a new column in the norm table, caring for names and Guids, 
							// setting derived flags there will be no update of the new column !

							// yet, we have to collect it, and later check, whether they are introduced
						} // new column
						if (s>0){
							if (sT.getInputColumnLabels().size()>1){
								ArrayList<Double> srcCellValues;
								AlgoTransformationIntf dynTAlgo = ((AlgoTransformationIntf)sT.getAlgorithm()) ; 
								
								for (int c=0;c<sT.getInputColumnLabels().size();c++){
									String srcLabel = sT.getInputColumnLabels().get(c) ;
									int srcLabelTableIx = normDataTable.getColumnHeaders().indexOf(srcLabel) ;
									if (srcLabelTableIx>=0){
										srcCellValues = normDataTable.getColumn(srcLabelTableIx).getCellValues();
										if (srcCellValues.size()==0){
											 
											/*
											 * there could be several reasons for failure
											 * - indeed not YET calculated
											 *   - due to forward dependencies
											 *   - because the source variable is not part of the required set 
											 * - a derived variable refers to a raw variable that is not included in the data
											 */
											Variable srcVariable = variables.getItemByLabel(srcLabel);
											if (srcVariable==null){
												cellvalues = ArrUtilities.createCollection( columnLen, -1.0) ;
											}
											if (srcVariable!=null){
												int srcvix = soappTransform.requiredchains.indexOf(srcLabel);
												if (srcvix<0){
													// it is not an accident, it is an intention
													// a derived variable is included, but one of its base variables not,
													// so it is not part of the model
													cellvalues = ArrUtilities.createCollection( columnLen, -1.0) ;
												}
												if ((variable.isDerived() == false)) {
													
												} else {

													// postponedVariables
													postponedVariables.add(varLabel);
													break;
												}
											} // srcVariable available
										} // srcCellValues empty?
										{
											if (srcCellValues.size() > 0) {
												dynTAlgo.addValueTableColumn(srcCellValues);
											}
										}
									}
								}
							}
							
						}
						if (rvix<0){
							
						}
					} // s -> all positions in stack for the current column
	if (varLabel.contains("d1")){
		int z;
		z=0;
		
	}
	
					if (failure==0){
						

						varTStack.setLatestDataDescriptionItem(-1);
						varTStack.setFirstItemForUpdate(0);
						// update is exclusively backward directed (to the left of the table), 
						// NEVER forward directed, to the right of the table thus we can call update here
						varTStack.update();

						int colix = normDataTable.getColumnHeaders().indexOf(varLabel);
						if (colix>=0){
							DataTableCol tablecol = normDataTable.getColumn(colix) ;
							
							cellvalues = varTStack.getLastPosition().getOutData();
							tablecol.getCellValues().clear();
							if ((cellvalues==null) && (cellvalues.size()==0)){
								cellvalues = ArrUtilities.createCollection( columnLen, -1.0) ;
							}
							tablecol.getCellValues().addAll(cellvalues) ;
							 
						}else{ // "physical" column identified ?
							failure++;
						}
					} // failure ?
					
					
				} // a necessary raw variable

			} // i->

			 
		
			transformSuccess = failure==0; 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return transformSuccess;
	}
	
	
	
	@SuppressWarnings("unused")
	private void introduceDerivedVariable( 	StackedTransformation sT, 
											TransformationStack varTStack, 
											DataTable normDataTable,
											Variable variable) {

		Variable newVariable;
		String varLabel,newVarLabel;
		int newIndex, six, currentFormat;
		TransformationStack newTStack ;
		
		
		varLabel = varTStack.getBaseVariable().getLabel() ;
		newVarLabel = sT.getOutputColumnLabel() ;
		
		newIndex = normDataTable.addDerivedColumn( varTStack, sT, 0 ) ;
		 
		normDataTable.getColumnHeaders().add( newVarLabel );
		currentFormat = variable.getRawFormat() ;
		
		newVariable = somData.addDerivedVariable( newIndex , varLabel, newVarLabel, sT.getIdString() );
		newVariable.setRawFormat( variable.getRawFormat()) ;
		newVariable.setDerived(true);	
		
		// here, we do NOT create a new TransformationStack that reflects the new variable
		// instead we have to select it from the existent ;
		TransformationModel transformModel = soappTransform.getTransformationModel() ;
		
		six = transformModel.getIndexByLabel(newVarLabel);
		
		newTStack = transformModel.getItem(six) ;
		
		newTStack = new TransformationStack( soappTransform, soappProperties.getPluginSettings() );
		
		newTStack.setBaseVariable(newVariable) ;
		newTStack.setVarLabel(newVarLabel) ;
		newTStack.setLatestFormat(currentFormat) ;
		newTStack.getInputVarLabels().add( varTStack.getBaseVariable().getLabel() ) ; 
		
		// ...should already be there ...
		// transformModel.getVariableTransformations().add( newTStack ) ;
		sT.setOutputColumnId( newTStack.getGuid() );
		
		// back reference... ??
		newTStack.addInputVarLabel(varLabel);
		
	}
	/**
	 * this establishes references and object data for SomAppTransformer = soappTransform,
	 * such that this instance knows about data, and the transformation rules
	 * 
	 */
	protected void establishTransformationModel() {
	
		String varLabel ;
		int ix;

		TransformationModel transformModel ;
		TransformationStack tstack ,modTstack ;
		ArrayList<String> targetStruc ;
		ArrayList<TransformationStack> modelTransformStacks ;
		
		
		
		// the variables needed for the profile
		targetStruc = getTargetVector();
		 
		soappTransform.setSomData( somApplication.getSomData() );
		
		
		somApplication.getSomData().getVariablesLabels() ;
		 
		
		// from here we transfer...
		modelTransformStacks = soappTransform.getTransformationModel().getVariableTransformations();
		transformModel = soappTransform.getTransformationModel() ;
		 
		
		transformationModelImported = true;
	}
	
	
	
	
	
	public SomAppSomObject getItem(int index){
		SomAppSomObject soappObj = null;
		
		soappObj = somObjects.get(index) ;
		return soappObj;
	}
	
	 
	/*
	 
	 	// how many som instances? check:  <som index="0">
		
		
		// <project>
			// <general>
		
			// <context>
		
			// <content>
		
		// <lattice>
			// description 
		
			// <nodes>
	 */
	
	
	/**
	 * used by services
	 * 
	 */
	public void importDescribedDataObject( SomClassDescribedDataIntf obj ){  
		// just a value vector and the indices of the respective column, or the column headers
		// will be prepared to a data object
		
		
		
	}
	
	public void importDescribedDataObject( SomClassProfileDataIntf obj ){
	
		
	}
	
	/**
	 * given the feature vector of the provided table and the feature vector of the profiles in the som model,
	 * we have to determine a map position-to-position
	 * 
	 */
	public ArrayList<String> getTargetVector(){
		
		ArrayList<String> targetVector = new ArrayList<String>();
		
		try{
			 
			// soappProperties.
			soappLoader.providedVarLabels.size() ;
			
			// somObjects = soappClassify.somObjects ;
			SomAppSomObject somobj = somObjects.get(0);
			
			targetVector = ((SomAppNodeIntf)somobj.soappNodes.getNode(0)).getAssignates() ;
			
		}catch(Exception e){
			
		}
		return targetVector;
	}
	
	/**
	 * called from the process
	 * 
	 * @param currObject 
	 */
	
	public void performClassification(Object dataTaskObject){
		
		ArrayList<Double> dataRecord ;
		SomAppDataTask datatask;
		Object obj ;
		
		
		datatask = (SomAppDataTask)dataTaskObject;
		
		obj = datatask.preparedDataTable;
		
		if ((obj!=null) && (obj instanceof String)){
		
			// the SomDataObject should already exist due to initial import of data, 
			// just handle the record, inclusive normalization 
			
			introduceDataToSomApplication(); 
			/*
			 *  we need a flag about the context: 
			 *    - first time= norm data are available, 
			 *    - next time = data must be assimilated into SomDataObject, in case of service mode
			 */
		}
		
		if ((obj!=null) || (obj instanceof SomDataObject)){
			// nothing special to do
		}
		 
		executeClassificationModel();
		
	}
	
	
	/**
	 * we are NOT establishing the full SOM!
	 * We just create a table of aligned records and determine the winner list by measuring the similarity 
	 * 
	 */
	private void executeClassificationModel() {
	
		// acts as a replacement for DSom*
		SomAppSomObject  somObject = somObjects.get(0);
		
		DataTable dataTable ;
		ArrayList<Double> values,usageVector = null;
		
		IndexedDistances winners;
		
		
		// the object SomAppNodes is a "replacement", surrogate for the VirtualLattice
		SomAppNodes somPseudoLattice;
		
		
		
		// TODO IMPORTANT !!!
		somObject.setUsageVector(usageVector);
		
		
		
		dataTable = somData.getNormalizedDataTable() ;
		
		for (int i=0;i<dataTable.getRowcount();i++){

			values = dataTable.getRowValuesArrayList(i);
			
			winners = somObject.getWinnerNodes( values );
			
			if (winners.size()>0){
				
			}else{
				
			}
			
		} // i-> all records
		
		
		
	}
	
	
	private void introduceDataToSomApplication() {
		 
		
	}
	
	
	
	
	// sending data to the process
	public void classifyRecord(){
		ArrayList<String> targetStruc ;
		int psz = 0;
		
		
		// the variables needed for the profile and for defining the values in the use vector
		targetStruc = getTargetVector();
		
											out.print(2,"performing classification of record ") ;
		// ArrayList<Object> dataQueue									
		
		
		// align 
		
		
		// prepare object
		// prepare a mini table according to feature vector in the SOM, incl. value profile  
		// mini table = feature vector according to profiles,  
		
		 
		
		// dataQueue.add() ;
		
	}

	public void classifyTable(){
		
		ArrayList<String> targetStruc ;
											out.print(2,"performing classification of data from table... ") ;
											
		// the variables needed for the profile and for defining the values in the use vector
		targetStruc = getTargetVector();
		
	}


	public void start(int modeFile) {
		
											out.print(3,"starting sub-process...") ;
		if (classProcess ==null){						
			
			classProcess = new ClassificationProcess ();
			classProcess.start();
			
		}else{
			if (classProcess.isRunning==false){
				classProcess.start();
			}
		}
		
	}
	
	
	/**
	 * 
	 * this is an internal service, which 
	 * works on the data queue : this is maintained by the embedding parent class , where records are received;
	 *  
	 * here we just call the performing routine, which also is in the parent class
	 * 
	 *
	 */
	class ClassificationProcess implements Runnable{

		private Thread classyThrd ;
		public boolean isRunning = false;
		public boolean isWorking=false;
		
		
		// ------------------------------------------------
		public ClassificationProcess(){
		
			
			classyThrd = new Thread(this,"classyThrd"); 
		}
		// ------------------------------------------------
		
		public void start(){
			classyThrd.start() ;
		}
		
		@Override
		public void run() {
			isRunning = true;
											out.print(2,"process for performing classification has been started") ;
			while (isRunning ){
				
				if ((isWorking==false) && (dataQueue.size()>0)){
					isWorking = true;
					
					while (dataQueue.size()>0){
						Object currObject = dataQueue.get(0) ;  // queue is empty ...
						dataQueue.remove(0) ;
						performClassification( currObject );
						
					}
					
					isWorking = false;
				} else {// working ?
				
					out.delay(5);
				}
			} // ->
			
			
			isRunning = false;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public boolean isWorking() {
			return isWorking;
		}

		public ArrayList<Object> getDataQueue() {
			return dataQueue;
		}
		
		
	}


	@Override
	public SomDataObject getSomDataObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNeighborhoodNodes(int index, int surroundN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SomFluidProperties getSfProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LatticePropertiesIntf getLatticeProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualLattice getSomLattice() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> getUsedVariablesIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}

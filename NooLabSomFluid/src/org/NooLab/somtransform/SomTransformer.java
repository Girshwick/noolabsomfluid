package org.NooLab.somtransform;

 
import java.io.File;
import java.util.*;

 
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.AlgorithmDeclarationsLoader;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.env.data.NormValueRangesIntf;
import org.NooLab.somfluid.properties.PersistenceSettings;
 
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.storage.PersistentAgentIntf;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.util.BasicStatisticalDescription;

import org.NooLab.somsprite.AnalyticFunctionSpriteImprovement;
import org.NooLab.somsprite.AnalyticFunctionTransformationsIntf;
import org.NooLab.somtransform.algo.distribution.EmpiricDistribution;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.datetime.DateTimeValue;

import com.jamesmurty.utils.XMLBuilder;



/**
 * 
 * the most basic functionality of this is normalization of input data;
 * 
 * it also maintains a "transformation model", which is a collection of stacks of elementary transformations;
 * any of the variables can be assigned such a stack 
 * </br></br>
 * 
 * Handling the algorithm stacks</br>
 * (1) transformation and creating new columns is strictly separated</br>
 * (2) algorithms can change the format from in-data to out-data </br>
 * (3) stack can be supplemented with plug-in algorithms, that need to implement </br> 
 *     one of three interfaces: for transforming, writing new columns, or measurements </br>
 * </br></br>
 * 
 * 1+2 mark a strong difference to Prospero's transformer </br></br>
 * 
 * the very first position in the transformation stack receives raw, NON_NORMALIZED data !!!! </br></br>
 * 
 * TODO: algorithms :
 *         - analysis of residuals relative to outcome, also on the level of modeling </br></br>
 *         - surrogates on various levels: 
 *              - within variable by empiric distribution
 *              - by bi-variate association
 *              - by Cholesky
 *              - by full scale group centroids <<<<<<<<<< this is actually the generalization of any multi-variate case 
 *              
 *     --- relative risk weight
 *            for binaries
 *            for ordinals = optimal scaling
 *            for numerals = by clustering, 10 groups, operationalized as rewriting by distance to centroid         
 * 
 */
public class SomTransformer 
								extends	//	for SomTransformer used in modeling and SomAppTransformer, used in classification
											SomTransformerAbstract 		
								implements 
											SomTransformerClientIntf,
											PersistentAgentIntf{

	//SomFluidFactory sfFactory;

	
	int derivationLevel = 0 ;
	int revision=1;
	String version = "1.0";
	
 

	
	IndexedDistances advAutoTransformations = new IndexedDistances();
	
	/**
	 * for any variable that is already know as blacklisted apriori, we may block its transformation 
	 */
	boolean excludeBlacklisted = false ;
	int realizedCount;
	
	ArrayList<String> xmlImage = new ArrayList<String>() ;
	
	transient String lastErrorMsg="" ;
	
	
	
	// ========================================================================
	public SomTransformer( SomDataObject sdo, SomFluidAppGeneralPropertiesIntf sfprops) {
		super( sdo , sfprops);			//    SomFluidProperties 
		

		initialization = new SomTransformerInitialization();
		somDerivations = new SomAssignatesDerivations(this);
		
		fileorg = sfProperties.getFileOrganizer() ;
		fileutil = fileorg.getFileutil();
		
		out = somData.getOut() ;
		out.setDisplayMemory(true);
	}

 
	public SomTransformer(SomFluidFactory factory, SomFluidProperties sfProperties) {
		 							//        flavor: transformer properties
	}


	/**
	 * creates the basic structures : 
	 * for each of the variables a TransformationStack will be initialized;</br>
	 * this stack remains empty though!
	 * 
	 */
	public void initializeTransformationModel(){
		
		int ix;
		Variables variables;
		Variable variable;
		TransformationStack tstack ;
		
		variables = somData.getVariables() ;
		if (transformationModel.variableTransformations==null){
			transformationModel.variableTransformations = new ArrayList<TransformationStack>();
		}
		// transformationModel.variableTransformations.clear() ;
		
		for (int i=0;i<variables.size();i++){
			
			variable = variables.getItem(i) ;
			
			ix = this.transformationModel.findTransformationStackByVariable( variable ); // not the label, but the object !
			
			if (ix<0){
				tstack = new TransformationStack( this, sfProperties.getPluginSettings() );
			
				// such the stack knows about the raw in-format (valueScaleNiveau), the label, basic min & max 
				tstack.baseVariable = variable; 
				tstack.varLabel = variable.getLabel() ;
			
				transformationModel.variableTransformations.add(tstack) ; 
			}
		}
		 
		initialization.setInitialized(true, somData.getRecordCount(), variables.size()  );
	}
 
	
	

	public static int getAlgorithmIndexValue(String string) {
		int index = -1;
		
		
		
		return index;
	}
	
	public int algorithmIndexValue(String str) {
		return getAlgorithmIndexValue(str) ;
	}
	
	
	private String createRevisionDetailsString(){
		/*
		int derivationLevel = 0 ;
		String version = "1.0";
		int revision=1;
		*/
		
		String str ="", vs;
		vs = strgutils.replaceAll( version, ".", ""); 
		str = "d"+derivationLevel+"_v"+vs+"_r"+revision ;
		
		return str;
	}

	/**
	 * we save it in as a serialized object here 
	 * 
	 */
	public int save() {
	
		return -2;
		// storageDevice.storeObject(this, filepath);
	}

	public void saveXml() {
		
		String xstr="", filepath , vrstr, filename = "";
		PersistenceSettings ps;
		
		// boolean embeddedObject = sfProperties.getPersistenceSettings().isExportTransformModelAsEmbeddedObj() ;
		extractTransformationsXML( false );
		
		// xstr = xmlImage.toString(); not useful uses ", " to separate items
		xstr = this.arrutil.arr2text( xmlImage, "\n");
			
		ps = sfProperties.getPersistenceSettings();
		 
		vrstr = createRevisionDetailsString() ;
		 
		filename = ps.getProjectName() + "_"+vrstr+ fileorg.getFileExtension( FileOrganizer._TRANSFORMER ) ;
		filepath = fileutil.createpath( fileorg.getTransformerDir(), filename);
		
		
		fileorg.careForArchive( FileOrganizer._TRANSFORMER, filepath );
		
		fileutil.writeFileSimple(filepath, xstr);
		System.gc(); out.delay(100);
	} 

	public void extractTransformationsXML(boolean embeddedObject ){
		
		
		extractTransformationsXML( derivationLevel, version, revision, embeddedObject);
	}
	/** 
	 * here we extract the rules as XML, together with necessary informations about source etc.</br> 
	 * 
	 * this is needed for the application of the SOM model to new data, since this new data need to be 
	 * transformed according to the same rules !
	 * 
	 * the transformations are all stored and accessible via 
	 * transformationModel.variableTransformations.get(i)
	 * 
	 * 
	 */
 
	public void extractTransformationsXML( int derivationLevel, String version, int revision, boolean embeddedObject){
		
		XMLBuilder builder;
		String xmlstr ;
		
		this.derivationLevel = derivationLevel ;
		this.version = version ;
		this.revision = revision ;
		
		// create target directory = temporary by java  
		try {
			
			// String tmpdir = DFutils.createTempDir( SomDataObject._TEMPDIR_PREFIX).getAbsolutePath();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		// first we have to transfer the parameters from algo's into the stack
		
		transferAlgorithmParametersToTransformationStack();
		
		// save data to serialized object file (obj + ser = string !), we might transfer it through some wires...
		
											out.print(2,"exporting transformation model...");
		builder = xEngine.getXmlBuilder( "somtransformer" );
		
		builder = builder.importXMLBuilder( getProjectDescriptionXml( derivationLevel, version, revision) );
		
		// this needs correction ... !!!
		
		builder = builder.importXMLBuilder( getSourceDescriptionXml() );
		// builder = builder.e("sources").up();
		
		// creating XML about the context
		getTransformerContextAsXml(builder);
		
		analyzeDerivationsOfVariables(); 
		
		transformationModel.derivations = somDerivations ;

		transformationModel.setOriginalColumnHeaders();
		
		if (somData.getNormalizedDataTable()==null){
			return;
		}
		
		transformationModel.setDerivedColumnHeaders( new ArrayList<String>( somData.getNormalizedDataTable().getColumnHeaders() )); 

		
		embeddedObject=true;
		if (embeddedObject){
			// ... transformationModel as serialized object
			String serTModelObjStr = strobj.encode( transformationModel ) ;
			// create xml embedding, put format info
			
			int rqvn = transformationModel.requiredVariables.size() ;
			String rqvarListStr = xEngine.digestStringList( transformationModel.requiredVariables );
			
			/*
			 * here we do not know about the selection as defined by the Som, we only know the required variables;
			 * given these we select only those derived variables which could be built by means of the variables 
			 * otherwise in the list
			 * 
			 */
			ArrayList<String>  rqchnList  = transformationModel.extractRequiredChains(1) ;

			String rqchnListStr = xEngine.digestStringList( rqchnList ); 
			
			
			builder = builder.e("transformations")
			
			                      .e("requiredvariables")  // this is just for fast access without reading/decoding the object, e.g. for catalog
			                                .a("count", ""+rqvn) 
			                                .a("list", rqvarListStr).up()
			                      .e("requiredchains")  // this is just for fast access without reading/decoding the object, e.g. for catalog
			                                .a("count", ""+rqchnList.size()) 
			                                .a("list", rqchnListStr).up()
			                      .e("storage")
			                          .e("format").a("embedded", ""+xEngine.booleanize(embeddedObject)).up()
			                      .up()
			                      .e("objectdata").t(serTModelObjStr).up()
			                 .up();
			// "//transformations/storage/format", "type") ;
			
		}else{
			// creating the XML String from transformations, add a chapter "transformations"
											out.print(2,"extractTransformationsXML(), get xml of transformationModel... ");
			transformationModel.getXML(builder);
		}
		
		xmlstr = xEngine.getXmlStr(builder, true);
		
		xmlstr = strgutils.replaceAll(xmlstr, "<parameters/>", "");
		
		String[] xmlstrs = xmlstr.split("\n");
		xmlImage = new ArrayList<String>( strgutils.changeArrayStyle(xmlstrs) );
											out.print(2,"exporting transformation model finished.");
		// out.print(2, xmlstr) ;
	}
	
	
	
	private void transferAlgorithmParametersToTransformationStack() {
		 
		
		ArrayList<TransformationStack> tStacks ;
		TransformationStack tStack;
		StackedTransformation sT ;
		
		
		tStacks = this.transformationModel.getVariableTransformations();
		for (int i=0;i<tStacks.size();i++){
			
			tStack = tStacks.get(i);
			
			for (int s=0;s<tStack.items.size();s++){
				
				sT = tStack.items.get(s);
				sT.algoParameters = ((AlgorithmIntf)sT.algorithm).getParameters()  ;
				//  upon re-establishing the transformation model, first the objects  
				//  will be created, then the parameters will be transferred
				
			} // all stack positions
			
		} // i-> all stacks
		
		
		
	}
 


	private SomAssignatesDerivations analyzeDerivationsOfVariables() {
		 
		if (transformationModel==null){
			transformationModel = new TransformationModel(this, somData);
		}
		somDerivations.initialize();
		
		somDerivations.createDerivationTrees() ;
		
		transformationModel.derivationTrees = new ArrayList<SomAssignatesDerivationTree>(somDerivations.derivationTrees) ;
		
		return somDerivations;
	}

	public void determineRequiredRawVariablesByIndexes( ArrayList<Integer> proposedindexes){
		
		ArrayList<Integer> removalsIxs = new ArrayList<Integer>() ;
		ArrayList<String> selectedModelVars;
		int tvindex,rix;
		Variables variables = somData.getVariables();
		
		tvindex = variables.getTvColumnIndex();   removalsIxs.add(tvindex);
		int iix = variables.getIdColumnIndex() ;  removalsIxs.add(iix);
		
		
		removalsIxs.addAll( variables.getIndexesForVariablesList( variables.getIdVariables() ) ) ; 
		removalsIxs.addAll( variables.getIndexesForVariablesList( variables.getTargetedVariables() ) ) ; 
		
		if (removalsIxs.size()>0){
			
			for (int i=0;i<removalsIxs.size();i++){
				rix = removalsIxs.get(i) ;
				int ix = proposedindexes.indexOf(rix);
				if (ix>=0){
					proposedindexes.remove(ix) ;
				}	
			} // i->
			
		}
		
		
		selectedModelVars = variables.deriveVariableSelection(proposedindexes, 0);
		// remove target variable
		
			
		determineRequiredRawVariables(selectedModelVars);
		
	}
	
	
	public void determineRequiredRawVariables( ArrayList<String> selectedModelVars){
		
		ArrayList<String> reqVars = new ArrayList<String>();
		String varlabel ;
		
		ArrayList<SomAssignatesDerivationTree> dTrees;
		
		
		varlabel = "";
		
		// 
		selectedModelVars.trimToSize();
		
		// in this way it should also work to create the roots representation
		for (int i=0;i<selectedModelVars.size();i++){
		
			varlabel = selectedModelVars.get(i);
			
 
			dTrees = transformationModel.derivations.getTreesByVariable( 1, varlabel); // 0=base, 1=any
			// dtransformationModel.derivations.getVariablesOfTree( dTree ) ;
			
			for (int d=0;d<dTrees.size();d++){
				varlabel = dTrees.get(d).baseVariableLabel ;
				if (reqVars.indexOf(varlabel)<0)reqVars.add(varlabel) ;
			}
			// derivationTrees. 
		}// i->
		
		transformationModel.requiredVariables.clear();
		transformationModel.requiredVariables.addAll(reqVars) ;
	}


	public ArrayList<String> getXmlImage() {
		if (xmlImage==null){
			xmlImage = new ArrayList<String> ();
		}
		return xmlImage;
	}


	private XMLBuilder getSourceDescriptionXml() {
		
		XMLBuilder builder = xEngine.getXmlBuilder( "sources" );
		
		// PersistenceSettings ps = sfProperties.getPersistenceSettings();
		// ps.getProjectName();
		
		int varcount=0, reccount=0;
		long filedatetimestamp=0,bytecount=0;
		
		varcount = somData.getData().getHeadersCount() ;
		varcount = somData.getData().getColcount() ;
		reccount = somData.getDataTable().getRowcount() ;
		
		String srcfilename = somData.getDataTable().getSourceFilename();
		File file = new File(srcfilename);
		filedatetimestamp = file.lastModified() ;
		bytecount = file.length();

		// SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		// System.out.println("After Format : " + sdf.format(file.lastModified()));
		
		builder = builder
						 .e("sourcetype").a("value", ""+sfProperties.getSourceType()).up()
						 .e("identifier").a("name", sfProperties.getDataSrcFilename()).up()
						 .e("size").a("variables", ""+varcount)
						           .a("records",""+reccount)
						           .a("bytes",""+bytecount)
						           .a("filedate",""+filedatetimestamp)
						 .up()
						 ;
				
		builder = builder.up();
		
		return builder;
	}


	private XMLBuilder getProjectDescriptionXml( int derivationLevel, String version, int revision ) {
		PersistenceSettings ps = sfProperties.getPersistenceSettings();
		
		 
		
		XMLBuilder builder = xEngine.getXmlBuilder( "project" );
		String datestr= (new DateTimeValue(0, 0)).get("d/M/yyyy HH:mm:ss");
		
		builder = builder.e("name")
								.a("label", ps.getProjectName()).up()
						  .e("version")
						        .a("version", version)
								.a("revision", ""+revision).up()
						  .e("date").a("value", datestr).a("expiry", "").up()
								
								 
						 .e("derivationLevel").a("value", ""+derivationLevel).up()		
						 // .e("sourcetype").a("value", ""+sfProperties.getSourceType()).up()
						 .e("identifier").a("name", sfProperties.getDataSrcFilename()).up()
						 .e("systemrootdir").a("identifier", ps.getPathToSomFluidSystemRootDir()).up()
						 .e("extendingsource").a("value",strgutils.booleanize(sfProperties.isExtendingDataSourceEnabled())).up()
						 .e("simulation").a("mode", ""+ _sfProperties.getDataUseSettings().getSimumlationMode())
						 				 .a("size",""+ _sfProperties.getDataUseSettings().getSimulationSize()).up()
						 ;
				
		builder = builder.up();
		
		return builder;
	}

 


	private String getTransformerContextAsXml(XMLBuilder builder){
		
		return  "";
	}
	
	
	// called from SomDataObject
	public void applyAdvNumericalTransforms(  ) {
		applyAdvNumericalTransforms( null) ;
	}


	/**
	* at this stage we have only standard transforms
	* this method checks for "non-normality" in the widest sense, but also only by simple means.</br></br>
	* basically, it checks the distribution of values for the following properties:</br>
	*  - strong left or right shift (skewness) </br>
	*  - presence of multi-modes (2+ maxima in the distribution of non-ordinal values, =kurtosis) </br>
	*  - presence of negative values </br></br>
	*     
	* the method organizes the respective more specialized "workers" (which act as classes);</br>
	* such they could also be called one-by-one; the "workers" calculate the histogram by their own;</br></br>
	* 
	* else, the package of the transformations that are understood as "basic adjustments" can be changed easily.</br></br>  
	* 
	*/
	public void applyAdvNumericalTransforms(IndexedDistances listOfPutativeTransforms) {

		int k, r, advAprioriTransformsCount = 0;
		String tvLabel, varlabel;
		boolean hb, targetDefined = false;
		TransformationStack varTStack, newVarTStack;
		Variable variable;

		advAutoTransformations = new IndexedDistances(); // will be overwritten most likely...
		IndexDistance advAutoTransformation;

		NumPropertiesChecker npc;
		ArrayList<Double> values, tvalues = null;
		DataTable dataTable;
		BasicStatisticalDescription statisticalDescription = null;

		if ((listOfPutativeTransforms == null) || (listOfPutativeTransforms.size() == 0)) {

			advAutoTransformations = createDefaultListOfAdvancedTransforms();

		} else {
			advAutoTransformations.addItems(listOfPutativeTransforms);
		}

			try{
				
				dataTable = dataTableNormalized ;
				int cn = transformationModel.variableTransformations.size() ;
											int outlevel=3;
											if (cn* dataTable.getColumn(0).size()>200000){outlevel=2; }
											//out.print(2,"");
				// here we treat ONLY NUMERICAL COLUMNS !!!
				for (int i=0;i<cn; i++){
											out.printprc(outlevel, i, cn, cn/10, "");
					varTStack = transformationModel.variableTransformations.get(i) ;
					variable = varTStack.baseVariable ;
					
					npc = new NumPropertiesChecker(this, dataTable, i) ;
					// check numerical characteristics: log shift? deciling? semantic zero?
					// where is the description of the histogram ?
						
					if ( (varTStack.items.size()>0) && (variable.getRawFormat() > DataTable.__FORMAT_ID) && 
						 (variable.getRawFormat() <= DataTable.__FORMAT_INT)){
	
						// check whether this column has already been identified as "ordinal"
						if (varTStack.outputColumnIds.size()>0){
							int fid = variable.getRawFormat(); // ???  e.g. __FORMAT_INT = 2;  __FORMAT_ORD = 3
						}
						
						// get the out-data of the last transformation in the stack
						values = varTStack.getOutData(0);
	
						if ((values == null) || (values.size() == 0)) {
							varTStack.setExported(false);
							continue;
						}
						
						varlabel = variable.getLabel() ;
						 
						npc = new NumPropertiesChecker(this, values);
						npc.setColumnHeaderLabel(variable.getLabel());
	
						tvLabel = sfProperties.getModelingSettings().getActiveTvLabel();
						targetDefined = (sfProperties.getModelingSettings().getTargetedModeling() && (tvLabel.length()>0));
						
						// no correlation or residual methods in this case
						if (targetDefined) {
							int tmode = sfProperties.getModelingSettings().getClassifySettings().getTargetMode();
							if ((tmode == 0) || (tmode == 5)) {
								targetDefined = false; // ???
							}
						}
						 
						int tix = somData.getVariables().getIndexByLabel(tvLabel) ;
						if (somData.getVariables().getTvColumnIndex()<0)somData.getVariables().setTvColumnIndex(tix);
						
						tvalues = null;
						if (tix<0)targetDefined=false;
						if (targetDefined) {
							
							hb=true;
							tvalues = dataTableNormalized.getColumn(tix).getCellValues();
							if (tvalues.size()==0){hb=false;}
							if (hb){
								npc.setTargetValues(tvalues);
							}
						}
	if (varlabel.toLowerCase().contentEquals("stammkapital")){ // stammkapital is a strong candidate for logshift!!!
		k=0;
	}
												out.print(3, "analyzing variable for transformation : "+varlabel );
						
						try{
							
							// contains the class "statistics package" as methods and results, which  
							// can be transferred to the algorithm if required
							npc.prepareStatisticalDescription();
							
							EmpiricDistribution ed = npc.statisticalDescription.getEmpiricDistribution();
							if (ed.isVariableIsNominal()){
								// stop processing of this var, just make an entry to the variables data format
								// int vix = somData.getVariables().getIndexByLabel(varlabel) ;
								
								variable.setRawFormat( DataTable.__FORMAT_ORD ) ;
								variable.setValueScaleNiveau(Variable._VARIABLE_SCALE_NOMINAL);
								dataTableNormalized.getColumn(tix).setFormat( DataTable.__FORMAT_ORD ) ;
								
								continue;
							}
							 
						}catch(Exception e){
							 
						}
						
						 
						statisticalDescription = npc.getStatisticalDescription() ;
						
						
						// ...........................
						// if (a) 
						for (int t=0;t<advAutoTransformations.size();t++){
							
							advAutoTransformation = advAutoTransformations.getItem(t);
							
							 
							ArrayList<Object> objs = new ArrayList<Object>();
							                                         
							// this refers to "builtinscatalog.xml", while the "advAutoTransformations" are declared in "catalog.xml"
							int ix = sfProperties.getAlgoDeclarations().getIndications().getIndexByStr("ResidualsByCorrelation");
							// better check for group label, which is "residuals" ...
							
							if ((ix>=0) && (advAutoTransformations.getItem(t).getIndex2() >= ix)){
								if (targetDefined == false) {
									continue;
								}
							} // ?
							
							try{
								hb = npc.checkFor( advAutoTransformations.getItem(t).getIndex2());
								 
							} catch (Exception e) {
								// in this case, the test ID is unknown by the NumPropertiesChecker
								hb = false;
								out.print(2, "numerical properties for variable  <"+varlabel+"> could not be determined." );
							}
	
							 
							if (hb){
								advAprioriTransformsCount++;
								// the column header in DataTable remains <label>_c !!!
								
								// dependent on the result we introduce first a new column for the new transformation
								// we define a particular name for it
								// TODO get defined abbreviation
								String str = varTStack.varLabel+"_" + createAlgorithmIndicatorLabel( advAutoTransformation );
						 
								newVarTStack = createAddVariableByCopy(1, varTStack, str); // 1= table of normalized data
							
								newVarTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
								newVarTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
								
								// ------------------------------
								// TODO make this dynamic ... needs a map from task to name of algorithm
								// ...then the requested stack to the new variable / branch 
								// varTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
								
								String algoLabel = advAutoTransformation.getGuidStr() ; 
								       // e.g. "AdaptiveLogShift"
								StackedTransformation stnew = newVarTStack.introduceAlgorithmizedStackPosition( algoLabel ) ;
								 
								objs = npc.determineAdaptedParameters( advAutoTransformation.getIndex2(), values);
								
								if ((objs!=null) && (objs.size()>0)){
									 
									 
									// last position in list => 
									//    1= produce normalized data, 
									//    0= leave it raw : default, (column will contain values <0, >1 !)
									// params.add(1.0) ;
									
									// is declared in the top-most interface "AlgorithmIntf" !!!						
									AlgorithmIntf algo = ((AlgorithmIntf)stnew.algorithm) ;
									objs.add(algo) ;
									algo.setParameters(objs);
									
									
								}
								// ------------------------------
															
								newVarTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
								newVarTStack.update() ;
								
								newVarTStack.introduceAlgorithmizedStackPosition("LinearNormalization") ;
								 
								// ensuring the in-data for the first transformation
								String basevarLabel = varTStack.baseVariable.getLabel() ;
								ensureInDataForFirstTransformation( this.somData.getNormalizedDataTable(),  newVarTStack , basevarLabel, 1 );
							 	
								// in DataTable, -1 are replaced by 0 !!! see also writing the table...
								r = newVarTStack.update() ;
								k = newVarTStack.getOutData(0).size();
								if ((r<0) || (k==0)){
									throw(new Exception("introducing advanced transformation (<"+algoLabel+">) failed."));
								}
							}
							
							
						} // t-> all suggested transformations 
						
						
						if (statisticalDescription!=null){
							statisticalDescription.clear() ;
						}
						statisticalDescription=null;
					} // num col ?
					
					
					
				} // i-> all variables == all positions in transformation model = list of tstacks

			if (advAprioriTransformsCount > 0) {
				out.print(2, "" + advAprioriTransformsCount + " advanced transformations have been applied.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		k = 0; // in DataTable, -1 are replaced by 0 !!! see also writing the
				// table...
	}

	/**
	 * use this to provide candidate transformations; the "candidates" object
	 * need to implement the interface "AnalyticFunctionTransformationsIntf"
	 * 
	 */
	public void perceiveCandidateTransformations( AnalyticFunctionTransformationsIntf candidates, int intoFreshStack) {								

		String expr, exprName;
		String[] varStr = new String[2];
		int[] varix = new int[2];
		AnalyticFunctionSpriteImprovement item;
		CandidateTransformation ctrans;

			try{
				
	
				if (intoFreshStack<=0)intoFreshStack=-1;
				if (intoFreshStack>1) intoFreshStack= 1;
	
				// we translate it into a more economic form, just the variables and the formula
				for (int i=0;i<candidates.getItems().size();i++){
					item = candidates.getItems().get(i) ;
					       expr = item.getExpression();
					       exprName = item.getExpressionName() ;
					       varix[0] = item.varIndex1 ;
					       varix[1] = item.varIndex2 ;
					       
					       varStr[0] = somData.getVariables().getItem( varix[0]).getLabel() ;
					       varStr[1] = somData.getVariables().getItem( varix[1]).getLabel() ;
					       
					       // varStr[0] = somData.getVariablesLabels().get(varix[0]);
					       // varStr[1] = somData.getVariablesLabels().get(varix[1]);
					       
					ctrans = new CandidateTransformation(exprName,expr,varix,varStr);
					ctrans.setDemandForFreshStack(intoFreshStack) ;
					candidateTransformations.add(ctrans) ;
				}
				
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public SomDataObject implementWaitingTransformations( ) {
	
		return implementWaitingTransformations(1) ;
	} 

	 
	/**
	 * 
	 * this takes "candidateTransformations" (a list of simple classes of type "CandidateTransformation") and
	 * implements it to the requested TransformationStacks;
	 * 
	 * the candidate transformations have been imported into a queue before by the method
	 * perceiveCandidateTransformations( AnalyticFunctionTransformationsIntf )
	 * 
	 * the transformation task itself knows whether it should be placed into a new variable or
	 * whether the transformation should be added to the stack
	 * 
	 *  
	 * @param target 0=base data table , 1=normalized data table (the usual target)
	 * @return
	 */
	public SomDataObject implementWaitingTransformations( int target ) {
		
		int nv,ra,tix=-1,n,vix,arexIndex ,r=0;
		String str, newVarLabel="", cn;
		boolean nameAdj = false;
		CandidateTransformation ct;
		// Map<String,Integer> varMapping ;
		IndexedDistances varMapping = new IndexedDistances();
		char chr;
		
		ArrayList<Double> colvalues;
		
		TransformationStack varTStack,newVarTStack , srcstack;
		StackedTransformation st , stDataSourceStackItem ;
		
		ArrayList<Object> params;
		Variables variables;
		
		int sourcemode=1;
		
		lastErrorMsg = "starting: implementWaitingTransformations()";
		// putting candidateTransformations to the transformation model, 
		// creating new "Variable"s and extending the SomDataObject (saving the old version, 
		// and by using a different name, also the new version
		
		
		// adding columns should check whether all data need to be transformed, or better: which one
		
		// SomDataObject should be extended smartly !!!!!!
		// in order to avoid a full transformation (hundreds of variables, 10000's of records), by just adding a single variable

		/*
		 * note that the original data table must remain unchanged if target=1 !
		 * 
		 */
		variables = somData.getVariables() ; 
		realizedCount=0;
		
		
		for (int i=0; i<candidateTransformations.size(); i++){
			nameAdj = false;
			// varMapping = new TreeMap<String,Integer> () ;
			// varMapping.clear();
			varMapping = new IndexedDistances();
			
			ct = candidateTransformations.get(i) ;
			
			str = ct.expression ;
			
			if ((str==null) || (str.length()==0)){
				continue;
			}
			
			// int[] ct.getVariablesIx();
			
			for (int vi=0;vi<ct.getVariablesIx().length;vi++){
				vix = ct.getVariablesIx()[vi] ;
				chr = (char)(97+vi) ;
				str = Character.toString(chr);
				varMapping.add( new IndexDistance(vi,vix, str) ); //  a, 10 --> expression variable "a" means columnindex 10
			}  //  char c = s.charAt(0);
			
			//
			nv = transformationModel.variableTransformations.size() ;
			 
			String basevarLabel = ct.getVariablesStr()[0] ;
			
if (basevarLabel.contains("Bisher_c2")){
	n=0;
}
			tix = transformationModel.getIndexByLabel(basevarLabel) ;
			vix = variables.getIndexByLabel(basevarLabel) ;
			// Variable variable = variables.getItem(vix) ;
			 
			varTStack = transformationModel.variableTransformations.get(tix) ;
			// contains now a reference to the parent in the tree, == the first variable in the expression
			             // 
			// should be able to deal with string also ... StringExpression
			try {
				if (ct.demandForFreshStack>0){
					
					newVarLabel = createLabelForSpriteDerivedVariable( ct.variablesStr ) ;
					
					// should also define single-column input data, it also inserts it into the targeted table !
					newVarTStack = createAddVariableByCopy( target, varTStack, newVarLabel); 
					// 
					varTStack = newVarTStack;  
				}else{
					return null;
				}
				
				if (varTStack.size() == 0) {
					st = varTStack.introduceAlgorithmizedStackPosition("MissingValues");
					
				}
				
				// ensuring the in-data for the first transformation...
				// note that all transformations also exist as columns in the normalized table, thus there 
				// always should be a basevarLabel.. TODO XXX TEST THAT for transforms on transforms
				ensureInDataForFirstTransformation( this.somData.getNormalizedDataTable(),  varTStack , basevarLabel, 1 );
				// connectTransformStacksForData( varTStack, 1, false); 
				
				st = varTStack.introduceAlgorithmizedStackPosition("ArithmetExpression");
				arexIndex = varTStack.size()-1 ;
				
				varTStack.baseVariable.setParentItems( ct.variablesStr ) ;
				varTStack.setInputVariables( ct.variablesStr );
				
				// provide parameters
				AlgorithmParameters aps = new AlgorithmParameters( ((AlgorithmIntf) st.algorithm) );
				params = new ArrayList<Object>();

				params.add(ct.expression);
				params.add(varMapping);
				 
				((AlgorithmIntf) st.algorithm).setParameters(params);
				((AlgoTransformationIntf) st.algorithm).setInputColumnsCount(ct.variablesStr.length) ;
				
				// in-data has n columns  (n>=1), dependent on transformation ...
				varTStack.items.get(0).getInData().clear() ;
				varTStack.items.get(arexIndex).getInData().clear() ;
				
				for (int vi=0;vi<ct.getVariablesIx().length;vi++){
					// get column index
					vix = ct.getVariablesIx()[vi] ;
					tix = transformationModel.getIndexByLabel( ct.getVariablesStr()[vi] ) ;
					
					srcstack = transformationModel.variableTransformations.get(tix) ;
					// update parent columns as indicated by the label
					// srcstack.getOutdataUpdated() ; // !!!!  CARE about copyplain ! 
					
					if ((srcstack.size()==0) || 
						(srcstack.getItems().get(0).outData.size()<=1) || 
						(srcstack.getItems().get(srcstack.size()-1).outData.size()<=1) ){
						srcstack.update() ;
					}

					// from the parent stack, get a reference to the stack item that will be used as a data source 
					// since we have just updated, the last item in the source stack should be able to provide data
					// TODO: instead of the last one, we should search for the last one that provides data !!
					stDataSourceStackItem = getDataSourceFromParentStack(srcstack, srcstack.size()-1 ) ;


					// get column data according to sourcing option as expressed by "StackedTransformation.stackPosForInData"
					colvalues = getColumnData( stDataSourceStackItem, vix, tix , sourcemode);                            
					
					// supplementing the first item in stack with input data without conditions = always
					if (vi==0){
						varTStack.items.get(0).getInData().add( colvalues );
						//               0 == first pos in stack
					}
					
						// if we have arithmet
						st = varTStack.items.get(arexIndex) ;
						cn = st.algorithm.getClass().getSimpleName() ;
						
					if (cn.contains("ArithmetExpression")){
						
						st.getInData().add( colvalues );
						st.multiVarInput = true;
						// we need to update the GUIDs of the backward links
						st.inputColumnIDs.add( srcstack.transformGuid );
						st.inputColumnLabels.add( srcstack.varLabel );
						// ... and adopting the out label name of the latest copy operator in the source stack
						// it looks for the transformation = stack item, that has the property outputColumnId = transformGuid
						if (nameAdj==false){
							nameAdj = setOutcolumnLabelForLastWriter( target, srcstack, varTStack.transformGuid , varTStack.varLabel );
							
						}
					}
					
				}
				
				
				/*	wrong length, for adjustment !!
						somDataObj.variables.setAbsoluteAccessible( )
						somDataObj.variables.usageindicationvector
						somDataObj.variableLables
				 */
				
				
				nv = transformationModel.variableTransformations.size();
				addedVariablesByIndex.add(nv-1);
				
				ra = varTStack.update() ; // check why the ValueTable does not get cleared... as in SomApp...
				
				if (ra==0){
					realizedCount++;
				}
				n = variables.size() ;
				if (variables.getUsageIndicationVector().size()<n){
					double uv = 0.0;
					// if (ct.)
					{
						uv=1.0;
					}
					variables.getUsageIndicationVector().add(uv);
				}
				if (variables.getAbsoluteAccessible().size()<n){
					variables.getAbsoluteAccessible().add(1) ;
				}
				String varLabel = varTStack.varLabel;
				if (somData.getVariablesLabels().indexOf( varLabel)<0){
					somData.getVariablesLabels().add( varLabel ) ;
				}
				
				r=0;
			} catch (Exception e) {
				e.printStackTrace();
				r=-7;
				lastErrorMsg= e.getMessage()+"\n"+e.getStackTrace().toString() ;
			}
		}// i-> all waiting candidateTransformations
		 
		if (r==0){
			lastErrorMsg="";
		}
		candidateTransformations.clear();
		return somData;
		// later we will adapt the latticeuseIndicators, which are from getSimilarityConcepts().getUsageIndicationVector() ;
		// 
	}

	public void createDataDescriptions() {
		// TODO Auto-generated method stub
		
	}


	private String createAlgorithmIndicatorLabel(IndexDistance transformItem) {
		String labelSnip="", str, xstr;
		
		str = transformItem.getGuidStr() ;
		
		// check whether the group label is available
		
		// extract all capital letters
		xstr = strgutils.extractCapitals(str);
		if (xstr.length()==0){
			xstr = str.substring(0,3); 
		}
		if (xstr.length()==1){
			xstr = xstr + str.substring(1,2); 
		}
		labelSnip = xstr.toLowerCase();
		
		return labelSnip;
	}


	/**
	 * 
	 * hard-coded set of advanced but still rather simple transforms
	 *  
	 * these are all mapped to algorithms
	 * 
	 */
	public IndexedDistances createDefaultListOfAdvancedTransforms() {
		
		IndexedDistances autoTransforms;
		
		AlgorithmDeclarationsLoader  adecl = sfProperties.getAlgoDeclarations();
		
		autoTransforms = adecl.readAdvAutoTransforms();
		 
		return autoTransforms;   
		 
	}



	/**
	 * this does NOT just ensure a linear normalization for the TransformationStack;
	 * it does so only if the values are outside [0..1];
	 * 
	 * this allows to "compress" binary variables which otherwise 
	 *  (1) dominate non-binary variables, due to their salient variance effects
	 *  (2) do not allow to introduce noising
	 * 
	 */
	public void ensureNormalizedDataRange(){
		
		
	}

	/**
	 * 
	 */
	public int addDataColumn( DataTableCol column, String name, int target ){
		int result = -1;
		
		
		return result ;
	}
	
	
	public int addDataRecords( ){
		int result = -1;
		
		
		return result ;
	}

	
	/**
	 * creates a deep clone with content of inDatatable
	 *  
	 */
	public void setDataTable( DataTable inDatatable ) {
		 
		try{
			dataTableObj = new DataTable( inDatatable ) ; 
			
		}catch(Exception e){
			
		}
	}


	public void createSurrogateData( double percentage , int mode){
		
	}
	 

	
	public DataTable getNormalizedDataTable(){
		return dataTableNormalized;
	}

	public void importExpectedNormValueRanges( String filename ){
		
	}
	
	public void importExpectedNormValueRanges( NormValueRangesIntf valueRanges){
		
	}
	/**
	 * 
	 * this has to take place in SomTransformer, because we need the reference to the variables. 
	 * 
	 * ... and adopting the out label name of the latest copy operator in the source stack
	 * it looks for the transformation = stack item, that has the property outputColumnId = transformGuid
	 * @param dataTableObj 
	 * 					 
	 * @param targetColGuid
	 * @param adjLabel
	 * @return 
	 */
	public boolean setOutcolumnLabelForLastWriter( int target, TransformationStack tstack, String targetColGuid, String adjLabel ) {
		
		boolean rB=false;
		String idstr, initialOutColumnLabel="";
		DataTable _dataTable;
		
		// which table (of type DataTable)? basic, or normalized
		if (target==0){
			_dataTable = dataTableObj;
		} else{
			_dataTable = somData.getNormalizedDataTable() ;
		}
		
		
		for (int i= tstack.items.size()-1;i>=0;i--){
			
			idstr = tstack.items.get(i).outputColumnId ;
			if (idstr.contentEquals(targetColGuid)){
				
				initialOutColumnLabel = tstack.items.get(i).outputColumnLabel;
				tstack.items.get(i).outputColumnLabel = adjLabel;
				
				rB=true;
				break;
			}
			
		} // i->
		
		if (initialOutColumnLabel.length()>0){
			int ix = _dataTable.getColumnHeaders().indexOf(initialOutColumnLabel) ;
			if (ix>=0){
				_dataTable.getColumnHeaders().set(ix,adjLabel  );
			}
			// TODO: we even need a callback / event that allows us to update the added variables...
			ix = somData.getVariables().getIndexByLabel( initialOutColumnLabel);
			if (ix>=0){
				String str ;
				Variable v ;
				
				v = somData.getVariables().getItem(ix);
				str = v.getLabel() ;
				if (str.contentEquals(initialOutColumnLabel)){
					v.setLabel( adjLabel );
				}
	
			}
		}
		 
		return rB;
	}

	/**
	 * 
	 * providing the raw values here (which is default) or the normalized is controlled by an OPTION !!! 
	 * we have to provide the index of the stack position which we should use as input
	 * @param stDataSourceSTackItem 
	 * 
	 * @param vix
	 * @param tix
	 * @return
	 */
	private ArrayList<Double> getColumnData( StackedTransformation dataSourceStackItem, int vix, int tix , int mode) {
		
		ArrayList<Double>  colvalues = new ArrayList<Double>();
		
		if ((mode<=0) || (dataSourceStackItem==null)){
			if (mode<0){
				colvalues = somData.getDataTable().getColumn(vix).getCellValues() ;
			}else{
				colvalues = somData.getNormalizedDataTable().getColumn(vix).getCellValues() ;
			}
		}else{
			colvalues = dataSourceStackItem.outData;
		}
		
		return colvalues ;
	}


	/**
	 * TODO: NOT READY; no ABBR. so far !!!
	 * 
	 * @param varStrings
	 * @return
	 */
	private String createLabelForSpriteDerivedVariable(String[] varStrings) {
		
		String newLabel = "",dLevelIndicationStr;
		int ix,dmax = 0;
		int exptotalLength = 5;
		
		String[] prepVarStrings = new String[varStrings.length];
		
		// we have to check the stack for similar name... etc.
		
		
		
		// we need to get the max derivation level of the provided parts
		// also determine the expected total length
		
		for (int i=0;i<varStrings.length;i++){
	
			prepVarStrings[i] = varStrings[i] ;
		} // all provided parts
		
	
		// then we reduce the names to a shorter version: suffixes, for each part as defined by capital letters
		
		for (int i=0;i<prepVarStrings.length;i++){
	
		}
		
		// 
		dmax++;
		
		dLevelIndicationStr = "d"+dmax+"_" ;
		newLabel = dLevelIndicationStr;
		
		// TODO XXX  abc124 create a new variable 
		//						d1b_Anzahl_Mitarbeiter_Gründungsdatum_c, ->  d1b_AnzMit_Gründc 
		//						d1_Anzahl_Mitarbeiter_Kunde_seit_c       ->  d1_AnzMit_Kundseitc
		//                
		for (int i=0;i<prepVarStrings.length;i++){
			
			newLabel = newLabel + prepVarStrings[i];
			if (i<prepVarStrings.length-1){
				newLabel = newLabel + "_" ;
			}
		} // all provided parts
	
		// loop: if this label already exists, we have to modify it inserting a char-enm (a,b,c)
		//       just before the "_" , e.g. as d2b_ , from "dLevelIndicationStr"
		
		String dIndicatstr = dLevelIndicationStr ;
		boolean varNameConflict = true;
		int z=0;
		while (varNameConflict){
			
			ix = transformationModel.getIndexByLabel(newLabel);
			
			varNameConflict = ix>=0;
			if (varNameConflict){
				z++;
				int p = dIndicatstr.length() ;
				newLabel = newLabel.substring(p, newLabel.length()) ;
				
				dIndicatstr = ((char)(z+97))+"_" ;
				dIndicatstr = dLevelIndicationStr.replace("_", dIndicatstr);
				newLabel = dIndicatstr + newLabel;
			}
		}
			
			
		return newLabel;
	}


	class SomTransformerInitialization{
		
		boolean done = false;
		int rowCount = 0;
		int colCount = 0;
		
		long timeStamp =0;
		
		public SomTransformerInitialization(){
			
		}
		
		public void setInitialized( boolean flag, int rowcnt, int colcnt){
			
			if (flag){
				timeStamp = System.currentTimeMillis() ;
				done = true;
				rowCount = rowcnt ; 
				colCount = colcnt ;
			}
		}

		public boolean isDone() {
			return done;
		}

		public void setDone(boolean done) {
			this.done = done;
		}

		public int getRowCount() {
			return rowCount;
		}

		public void setRowCount(int rowCount) {
			this.rowCount = rowCount;
		}

		public int getColCount() {
			return colCount;
		}

		public void setColCount(int colCount) {
			this.colCount = colCount;
		}

		public long getTimeStamp() {
			return timeStamp;
		}

		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}
	}


	public FileOrganizer getFileorg() {
		return fileorg;
	}


	public String getLastErrorMsg() {
		return lastErrorMsg;
	}

	public void clearErrorMsg() {
		lastErrorMsg = "";
	}


	public int getDerivationLevel() {
		return derivationLevel;
	}


	public void setDerivationLevel(int dLevel) {
		derivationLevel = dLevel;
	}


	public void incDerivationLevel() {
		derivationLevel++;
	}


	public int getRevision() {
		return revision;
	}


	public void setRevision(int revision) {
		this.revision = revision;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	@Override
	public SomTransformer getSelfReference() {
		return this;
	}

	// ========= from SomTransformerClientIntf ================================
	
	@Override
	public void introduceTransformation() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void introduceTransformations(String filename) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addObservations(ArrayList<String> colHeaders, ArrayList values) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void reFreshCalculation() {
		// TODO Auto-generated method stub
		
	}
	 
}


 
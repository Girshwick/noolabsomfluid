package org.NooLab.somfluid.clapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.app.up.SomFluidStartup;
import org.NooLab.somfluid.components.SomDataObject;
 
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.somtransform.StackedTransformation;
import org.NooLab.somtransform.TransformationModel;
import org.NooLab.somtransform.TransformationStack;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.callback.ProcessFeedBackContainer;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.strings.StringsUtil;
import org.apache.commons.collections.CollectionUtils;


/**
 * 
 * loads a model into an applicable object
 * 
 *
 */
public class SomAppModelLoader {

	transient SomFluidFactory sfFactory ;
	
	transient SomApplicationIntf somApplication;
	transient SomDataObject somData;
	
	SomAppProperties soappProperties ;
	
	SomModelCatalog soappModelCatalog ; 
	
	transient SomAppClassifier soappClassifier;
	transient SomAppTransformer soappTransformer;
	
	String baseFolder ="";
	String activeModel="";
	String activeVersion="";
	String modelPackageName = "" ;
	
	String dataSourceFile = "" ; 
	
	ArrayList<String> providedVarLabels = new ArrayList<String>(); 
	
	transient private ArrayList<String> transformedVariables = new ArrayList<String>();
	transient private ArrayList<String> postponedVariables   = new ArrayList<String>();

	
	transient StringedObjects strgObj = new StringedObjects() ;
	transient DFutils fileutil = new DFutils();
	transient XmlStringHandling xMsg = new XmlStringHandling() ;
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities ();
	transient PrintLog out = new PrintLog(2,true);

	boolean transformationsExecuted;
	boolean transformationModelImported;
	
	// ========================================================================
	public SomAppModelLoader( SomApplicationIntf somApp, SomFluidFactory factory ) {
		 
		somApplication = somApp;
		sfFactory = factory;
		soappProperties = sfFactory.getSomAppProperties() ;
		 
		somData = somApplication.getSomData() ;
		soappModelCatalog = new SomModelCatalog( somApp , soappProperties)  ; 
		
		baseFolder = soappProperties.getBaseModelFolder() ;
	}
	
	// ========================================================================
	
	public void retrieveExpectedVarLabels() throws Exception {
		 
		String filename = "";
		
		
		DataTable dataTable = somApplication.getSomData().getData();
		
		if (dataTable!=null){
			
			int chsz = dataTable.getColumnHeaders().size();
			
			if (chsz>1){
				providedVarLabels = new ArrayList<String>(dataTable.getColumnHeaders());
				return;
			}
			
		} // dataTable ?
		
		// fallback: directly accessing the file... yet, this could be problematic, since umlauts are not corrected 
		filename = soappProperties.getDataSourceFilename() ;
		
		
		
		
		if (filename.trim().length()==0){
			throw(new Exception("Data source filename is empty.")) ;
		}
		if (filename.indexOf("/")<0){
			filename = fileutil.createpath( 
											fileutil.createpath( SomFluidStartup.getProjectBasePath(),SomFluidStartup.getLastProjectName()) , 
											SomFluidStartup.getLastDataSet()) ;	
		}
		 
		if (fileutil.fileexists(filename)){
		
			dataSourceFile = filename;
			
			Vector<String> lines = fileutil.readFileintoVectorstringTable(filename) ;
			String[] headerRow = lines.get(0).split("\t") ;
			if (headerRow.length>1){
				providedVarLabels = new ArrayList<String>(Arrays.asList(headerRow));
				//"expected" := from the perspective of the model these contain the selected variables
			}
		}else{
			
		}
		
	}

	// ========================================================================
	
	// ========================================================================
	
	public void createSomTransformerInstance(SomDataObject somdata) throws Exception{
	
		somData = somdata;
		if (somData==null){
			throw(new Exception("createSomTransformerInstance() needs a workig instance of SomDataObject."));
		}
		if (soappTransformer==null){
			soappTransformer = new SomAppTransformer( somData, soappProperties);
			
			
			somData.setTransformer(soappTransformer) ;
		}
			
	}

	/**
	 * 
	 * loading the som model: </br>
	 * if a version has been provided, it will be tried to choose that one, </br>
	 * else a particular one will be selected acc. to settings, and loaded; </br></br>
	 * </br>
	 * it requires the model catalog, which gets updated through calling "selectFromAvailableModels()"
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean loadModel() throws Exception{
		
		boolean rB = false ;
		ModelCatalogItem mcItem ;
		
		// before, we have determined name and version according to the request options (e.g. "latest")
		mcItem = soappModelCatalog.getItemByModelname( activeModel, activeVersion ); 
		// here, an active version should always be defined, if not, the first one will be selected
		
		
		if (mcItem != null){
											out.print(2, "loading resources for requested model <"+activeModel+">, version <"+activeVersion+"> ...  ") ;
			
			if ((somData==null) || (somData.getData().getColumnHeaders().size()==0)){						
				somApplication.loadSource();
			}
			
			// this also sets the data
			soappTransformer  = loadSomAppTransformer(mcItem);
			
			
			rB= transformIncomingData();
			
			soappClassifier = loadSomAppClassifier(mcItem);

			rB= (soappTransformer!=null) && (soappClassifier!=null);
		}else{
			out.print(2, "identification resources for requested model <"+activeModel+"> failed.") ;
		}
		return rB;
	}

	
	/**
	 * 
	 * selecting from the available models 
	 * - according to the request options (like best, latest, first...)
	 * - such that the fields required by the model are provided by the data
	 *  
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean selectFromAvailableModels() throws Exception{
		
		boolean rB=false;
			
		baseFolder = soappProperties.getBaseModelFolder() ;
					 //  sth like:  "D:/data/projects/_classifierTesting/bank2/models"
		
		activeModel = soappProperties.getActiveModel() ; // sth like "bank2" 
		// this refers to the name of the project as it is contained in the model file!!
		// on first loading, a catalog of available model will be created for faster access if it does not exists
	
		if (activeModel.length()>0){      
											out.print(2, "checking model catalog associated with selected project ...") ;
			checkCreateLocationCatalog() ;
		} else{
			// alternatively, we set the active model to blank here, and provide the package name ;
			// whenever the active model name is given (and existing) it will be preferred!
			 
			modelPackageName = soappProperties.getModelPackageName();
			activeModel = getModelThroughPackage(modelPackageName ) ;
		}
		
		// now reading from the modelcatalog.dat
		
		/*
	 	_MODELSELECT_LATEST      = 1;
		_MODELSELECT_FIRSTFOUND  = 2;
		_MODELSELECT_BEST        = 4;
		_MODELSELECT_ROBUST      = 8;
		_MODELSELECT_VERSION     = 16 ;
		 */
		ModelCatalogItem mcItem= null, mci = null ;

		if (soappModelCatalog.size()>0){
			int m=0;
			boolean mciOK=false;
			
			while ( (mciOK==false) && (mcItem==null) && (m<soappModelCatalog.size())){
				
				if (soappProperties.getModelSelectionMode() == SomAppProperties._MODELSELECT_FIRSTFOUND){
					mci = soappModelCatalog.getItem(m);
				}
				
				if (soappProperties.getModelSelectionMode() == SomAppProperties._MODELSELECT_LATEST){
					mci = soappModelCatalog.getLatestItem();
				}
				if (soappProperties.getModelSelectionMode() == SomAppProperties._MODELSELECT_BEST){
					mci = soappModelCatalog.getBestItem();
				}
				if (soappProperties.getModelSelectionMode() == SomAppProperties._MODELSELECT_VERSION){
					mci = soappModelCatalog.getItemByModelname( activeModel, soappProperties.preferredModelVersion ) ;
				}
				
				// checking whether the model contains the required fields mcItem.requiredfields
				if (modelCheckRequirements(mci) == false ){
					soappModelCatalog.addToExcludedItems(mci);
				}else{
					mciOK=true;
					mcItem = mci;
				}

				m++;
			} // ->
			
			
		}else{
			mcItem=null;
		}
		
		
		activeModel = "" ;
		activeVersion = "" ;
		
		if (mcItem!=null){
			activeVersion = mcItem.modelVersion;
			activeModel = mcItem.modelName;
			rB=true;
			
			if (activeModel.length()==0){
				rB = false;
			}
			if (activeVersion.length()==0){
				rB = false;
			}

		}else{ // mcItem ?
			out.print(2,"No matching model (by required fields) found in the list (n="+soappModelCatalog.size()+") of available models.");
		}
		return rB;
	}

	private boolean modelCheckRequirements(ModelCatalogItem mcItem) {
		boolean rB=false  ;
		
	 
		int xn, rn, mismatchCount = -1 ;
		
		try{
		 	
			xn = providedVarLabels.size();  // variables as provided by the data source
			rn = mcItem.requiredfields.size() ;
			
			if ((xn>0) && (rn>0)){
				int isectsz = CollectionUtils.intersection(providedVarLabels, mcItem.requiredfields).size() ;
				// absolute...
				mismatchCount = CollectionUtils.disjunction( providedVarLabels, mcItem.requiredfields).size();
				// perspective "required" 
				mismatchCount = Math.abs(isectsz - mcItem.requiredfields.size()) ;
			}
			
			rB = mismatchCount == 0;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return rB;
	}

	private SomAppTransformer loadSomAppTransformer(ModelCatalogItem mcItem) throws Exception {
		
		int r;
		SomAppTransformer soappT ;
		
		SomDataObject sdo ;
		sdo = somApplication.getSomData() ;
		
		if (soappTransformer==null){
			soappT = new SomAppTransformer( sdo, soappProperties);
		}else{
			soappT = soappTransformer;
		}

		
		r = loadSoappTransformer(soappT, mcItem);
		
		if (r<0)soappT=null;
		
		return soappT;
	}

	private SomAppClassifier loadSomAppClassifier(ModelCatalogItem mcItem) {
		
		int r;
		SomAppClassifier soappC ;
		SomAppSomObject soappObj;
		
		soappC = new SomAppClassifier(somApplication, sfFactory) ;
		 
		// how many som instances? check:  <som index="0">
		LatticePropertiesIntf latticeProps = sfFactory.getSfProperties();
		
		soappObj = new SomAppSomObject(soappC, soappC.somObjects.size(), latticeProps.getSomType());

		r = loadSomObject( soappObj, mcItem, 0 ) ;
		
		if (r==0){
			soappC.somObjects.add(soappObj) ;
		}else{
			soappC = null ;
		}
		
		return soappC;		
	}

	
	@SuppressWarnings("unchecked")
	private int loadSoappTransformer( SomAppTransformer soappT, ModelCatalogItem mcItem){
		int result = -1, az;
		String str = "",rawXmlStr , modelFolder="",txmlFilename="" ;
		boolean explicitXML = false;
		
	 
		try{
			
			modelFolder  = fileutil.createpath(baseFolder, mcItem.packageName ) ;
			txmlFilename = fileutil.createpath(modelFolder, "transform.xml");
			
			rawXmlStr = fileutil.readFile2String(txmlFilename);
			
			xMsg.clear() ;
			xMsg.setContentRoot( "somtransformer") ;
			
			str = xMsg.getSpecifiedInfo(rawXmlStr, "/project/name", "label") ;
				  soappT.modelname = str ; // must be the same as for som
			 
					
		    str = xMsg.getSpecifiedInfo(rawXmlStr, "/transformations/requiredvariables", "list") ;   // "requiredvariables" ... honeypot for searches...
		    	  soappT.requiredVariables = new ArrayList<String> (xMsg.getListFromXmlStr(str, String.class) ) ;
		    	    
		     
			str = xMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/transformations/requiredchains", "list") ; // "requiredvariables"
			  if (str.length()>0){
				  soappT.requiredchains = new ArrayList<String>(xMsg.getListFromXmlStr(str, String.class)) ;
			  }
			  
			// embedded serialized object ?
			str = xMsg.getSpecifiedInfo(rawXmlStr, "//transformations/storage/format", "embedded") ;
			      explicitXML = !xMsg.getBool(str, false) ;
			    	  
			if (explicitXML == true){
				
				if (xMsg.tagExists( rawXmlStr,"//transformations/storage/objectdata")){
					explicitXML = false;
				}
			}
			if (explicitXML == false){
				
				// load everything at once
				loadTransformationModelFromSerialized(soappT,rawXmlStr);
				
				return 0;
			} 
			// all transformations, containing sub-sections "column"
			// explicit storage
			Vector<Object> algoNodeObjects = xMsg.getItemsList(rawXmlStr, "//transformations", "column", "index") ;
			

			/**
			 * 
			 * mcItem contains the array of used variables;
			 * these variables may be derived variables, so we have to track back, which
			 * variables are actually raw input variables;
			 * for tracking back, we need the GUID
			 * 
			 * YET THIS SHOULD BE DONE on export, and we should export only those
			 * variables, which we actually will need
			 * 
			 */
			soappT.nodeCount = algoNodeObjects.size();
			soappT.createNodes();
			
			// dependent on format of storage, we either load it explicitly or from a serialized TransformationModel embedded to the XML 
			// the decision whether all variables or only needed ones are included is on the side of the export
											out.print(2, "\n...going to load som transformation model, <"+soappT.nodeCount+"> nodes expected...") ;
			for (int i=0;i<algoNodeObjects.size();i++){
											// provide a process feedback to the available callback
				
				SomAppAlgorithm node = soappT.soappTransforms.nodes.get(i) ;
				
				Object obj = algoNodeObjects.get(i) ; // that's not a node, it is an integer
				
				xMsg.clearBasicConditionLocation() ;
				
				// get info from that node
				// str = xMsg.getSpecifiedItemInfo(obj, "index") ;
				
				String xpath = "//somtransformer/transformations/column[@index='"+i+"']" ;
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, xpath, "label");
					  node.varLabel = str;
				str = xMsg.getSpecifiedInfo(rawXmlStr, xpath, "guid");
					  node.guid = str;
				str = xMsg.getSpecifiedInfo(rawXmlStr, xpath, "index");
					  node.colIndex = xMsg.getInt(str, -1);	
				xMsg.setConditionalXPath(xpath) ;
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "algorithms", "length");
					  az = xMsg.getInt(str, 0);
					  
					  // node.createAlgorithms();
					  
					  xMsg.setConditionalXPath("") ;
					  
					  for (int t=0;t<az;t++){
						  String axpath = xpath + "/algorithms/algoritem[@index='"+t+"']"  ; 
						  
						  str = xMsg.getSpecifiedInfo(rawXmlStr, axpath, "name");
						  	    // node.algorithms.get(t) ;
						  	    // algo.name = str ;
						  	    
						  str = xMsg.getSpecifiedInfo(rawXmlStr, axpath, "type"); // int constant like  AlgorithmIntf._ALGOTYPE_WRITER
						  
						  xMsg.setConditionalXPath(axpath) ;
						  // TODO we need the type of the algorithm from the xml!!!
						  // -> passive, writer transformer
						  
						  str = xMsg.getSpecifiedInfo(rawXmlStr, "algoritem/parameters", "outlabel");
						  
						  
						  str = xMsg.getSpecifiedInfo(rawXmlStr, "algoritem/parameters", "outlabel");

						  // count the items  "parameters/params/set"
						  int sc = 0 ;
						  for (int s=0;s<sc;s++){
						  
							  String apxpath = axpath + "/algoritem/parameters/params/set[@index='"+t+"']"  ;
							  xMsg.setConditionalXPath(apxpath) ;
							  
						  }
						  
					  }
				
				
				
			} // -> all columns
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return result;
	}
	
	
	/**
	 * 
	 * @param soappT
	 * @param rawXmlStr
	 */
	private void loadTransformationModelFromSerialized( SomAppTransformer soappT,String rawXmlStr) {
	
		xMsg.setConditionalXPath("");
		String str = xMsg.getTextData( rawXmlStr, "//transformations/objectdata");
	 
		Object object = strgObj.decode(str) ;
		
		if (object instanceof TransformationModel){ // we pick only the list of transformations
			TransformationModel tm = (TransformationModel)object;
			
			soappT.getTransformationModel().setOriginalColumnHeaders( new ArrayList<String>( tm.getOriginalColumnHeaders() ));
			// this is very important for setting up the dataTable, data vectors to be classified !!!
			
			soappT.getTransformationModel().setVariableTransformations(  new ArrayList<TransformationStack>(tm.getVariableTransformations()) ) ;
			
			// and the original vector of column headers = assignates = features  
		}
		
	}

	
	
	/**
	 * 
	 * this loads the som.xml into a VirtualLattice structure, which is essentially a collection of MetaNodes; </br></br>
	 * such, the standard way = same methods as in som learning are used for classifying records </br>
	 * 
	 * it also reduces the table for normalized data and builds the appropriate usage vector
	 * 
	 * 
	 * @param soappObj
	 * @param mcItem
	 * @param somObjIndex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int loadSomObject( SomAppSomObject soappObj, ModelCatalogItem mcItem, int somObjIndex) {

		int result = -1;
		String str = "",rawXmlStr , modelFolder="",somxmlFilename="" ;
		Object subtag;
		ArrayList<Double> loadedValues ;

		Variables variables;
		MetaNode mnode;

		ArrayList<SomAppNode> localNodeCollection = new ArrayList<SomAppNode>();
		ArrayList<String>  loadedVariables, syntheticVarMetric = new ArrayList<String>();
		ArrayList<String>  usedVariables = new ArrayList<String>(); 
		ArrayList<Integer> usedVariablesIndexes ;
		ArrayList<Double>  profileValues,usageIndicationVector = new ArrayList<Double> ();
		

		
		
		variables = somData.getVariables() ;
		
		
		try{
			
			if (somData==null){
				somData = soappTransformer.getSomData() ;
			}
			
			
			
			modelFolder    = fileutil.createpath(baseFolder, mcItem.packageName ) ;
			somxmlFilename = fileutil.createpath(modelFolder, "som.xml");
			
			rawXmlStr = fileutil.readFile2String(somxmlFilename);
			xMsg = new XmlStringHandling();
			
			xMsg.setContentRoot( "somobjects") ;
			// <project>
			// <general>
			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
																"/project/general/task", "id"); 	
			       soappObj.originatingTask = str;
			 
			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
																"/project/general/name", "label"); 	
				   soappObj.name = str;
			// .................
			
			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
																"/project/context/bags", "count"); 	
				   soappObj.bagsCount = xMsg.getInt(str, 0) ;
				   
			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
																"/project/context/somtype", "targetmode"); 	
				   soappObj.targetmode = xMsg.getInt(str, 0) ;
			
			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
																"/project/context/somtype", "typeid"); 	
				   soappObj.somTypeId = xMsg.getInt(str, 0) ;
			
			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
																"/project/risk/ecr", "value");
				   double v = soappProperties.getRiskAttitudeByECR();
			       if (v<0){
			    	   v = 0.25 ;
			       }
				   v = xMsg.getNum(str, v );
				   soappProperties.setRiskLevelByECR(v);

			str =  xMsg.getSpecifiedConditionalInfo( rawXmlStr, "//somobjects/som", "index", ""+somObjIndex, 
															    "/project/risk/support", "value"); 	
				   int n = soappProperties.getRiskConfidenceByClusterSupport();
				   if (n<0){
					   n = 0;
				   }
				   n = xMsg.getInt(str, n); // 
				   soappProperties.setRiskLevelByECR(v);

			// <content> ... filter, noise etc. is missing
			
			// get all nodes as object
			Vector<Object> xmlNodeObjects = new Vector<Object>();
			
			// 
			int r = xMsg.setBasicConditionLocation( rawXmlStr,
													"//somobjects/som", // path  
													"index", 			// attribute containing the condition 
													""+somObjIndex ,	// the condition 
													"/lattice");		// a node below the condition

			xmlNodeObjects = xMsg.getItemsList(rawXmlStr, "nodes", "node", "index") ;
			
			// that's not implemented yet...
			xmlNodeObjects = xMsg.getSpecifiedConditionalNode( rawXmlStr, 	"//somobjects/som", // path  
																		"index", 			// attribute containing the condition 
																		""+somObjIndex ,	// the condition 
																		"/lattice");		// a node below the condition
			// these are xml nodes !!!
			xmlNodeObjects = xMsg.getNodeList(rawXmlStr, "nodes", "node");
			
			String basicXpath = xMsg.getConditionalXPath()+"/nodes";
	 		
			soappObj.nodeCount = xmlNodeObjects.size();
			
			
		 									out.print(2, "going to load som classification model, <"+soappObj.nodeCount+"> nodes expected...") ;
            soappObj.soappNodes.clear() ;
            soappObj.soappNodes.setSomData(somData) ;
            								ProcessFeedBackContainer processFeedBackContainer = new ProcessFeedBackContainer( 0,71, xmlNodeObjects.size(), somApplication);
            								processFeedBackContainer.setHostingObject( this.getClass().getSimpleName() );
            
            // we have to transfer the data from nodeObjects to MetaNodeIntf in soappObj.soappNodes
            // soappNodes is an instance of VirtualLattice!!
            								
            // as provided and/or available
            int vn = somData.getNormalizedDataTable().getColumnHeaders().size() ;
            
            usageIndicationVector = ArrUtilities.createCollection(vn, 0.0) ;
             
            // note that we need 2 loops, because we reduce the table size AND the set of the variables, we also have to care about requested index variables
            // subsequently the variable collection will not contain not-needed stuff
            // first creating nodes and reading the data into helper structure, then establishing node profile data
			for (int i=0;i<xmlNodeObjects.size();i++){
											// provide a process feedback to the available callback
											processFeedBackContainer.setCurrentProgressValue(i) ;
				// SomAppNodeIntf node = (SomAppNodeIntf)soappObj.soappNodes.getNodes().get(i) ;
				// this derives from the <MetaNodeIntf>
				// MetaNode node = new MetaNode(soappObj.soappNodes, somData );
				
				SomAppNode node = new SomAppNode(soappObj.soappNodes, somData ,i); // 161c9d1
				
				Object obj = xmlNodeObjects.get(i) ;
				xMsg.clearBasicConditionLocation() ;
				// get info from that node
				str = xMsg.getSpecifiedItemInfo(obj, "index") ;
				
				String xpath = basicXpath+"/node[@index='"+str+"']" ;
				xMsg.setConditionalXPath(xpath) ;
				
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/ppv", "value");
					   node.setPpv( xMsg.getNum(str, -1.0) );
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/ppvrank", "value");
					  node.setPpvRank( xMsg.getInt(str, 0) );
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/npv", "value");
				   	  node.setNpv( xMsg.getNum(str, -1.0) );

				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/recordcount", "value");
					  node.setRecordcount( xMsg.getInt(str, 0) );
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/neighbors", "items");
					  node.setNeighborsList( xMsg.getListFromXmlStr(str,Integer.class) );
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "profile/values", "list");
					  node.setModelProfile( xMsg.getListFromXmlStr(str,Double.class)) ;
					  
				str = xMsg.getSpecifiedInfo(rawXmlStr, "profile/variables", "list");
					  node.setAssignates( xMsg.getListFromXmlStr(str,String.class)) ;
					  
				str = xMsg.getSpecifiedInfo(rawXmlStr, "profile/variances", "list"); 
					  node.setModelProfileVariances( xMsg.getListFromXmlStr(str,Double.class)) ; 
				
				
				ArrUtilities.resetCollection( node.getProfileVector().getValues(), 0.0);
				
				// establishing two parallel lists of different flavors of the node  
				soappObj.soappNodes.addNode( node ) ; // MetaNode , does not know about assignates
				localNodeCollection.add(node) ;       // SomAppNode, flavor used as an intermediate structure
			} // i-> all nodeObjects
											processFeedBackContainer.setCurrentProgressValue(xmlNodeObjects.size()) ;
			
			
			result=1;

			
			// the synthesis consists from the optionally requested id variables ... TODO: we have to check whether they are present in the data
		 	
			if (soappProperties.indexVariableLabels.size()>0){
				syntheticVarMetric = arrutil.collectionGetConfirmedContainments( soappProperties.indexVariableLabels, 
																				 somData.getNormalizedDataTable().getColumnHeaders());	
			}
			
			// ... and from the imported variables
			syntheticVarMetric.addAll( localNodeCollection.get(0).getAssignates() );
			
			DataTable normDataTable = somData.getNormalizedDataTable() ;
			
			// streamlining the collection of variables
			// note, that at this point we need the table of transformed data !!! 
			int vm = variables.size()-1;
			
			while (vm>=0){
				
				String varLabel = variables.getItem(vm).getLabel();
				if (syntheticVarMetric.indexOf(varLabel)<0){
					variables.removeItem(vm); 	// should be happen "everywhere", in "each" instance of somData / variables -> check !!??? 
				 								// since we move around only the reference
					normDataTable.getColumnHeaders().remove(vm);
					normDataTable.getDataTable().remove(vm) ;
					
					int ix = somData.getVariableLabels().indexOf(varLabel);
					if (ix>=0){
						somData.getVariableLabels().remove(ix) ;
					}
				}
				vm--;
			}
			normDataTable.setColcount( normDataTable.getDataTable().size()) ;
			normDataTable.getDataTable().trimToSize();
			normDataTable.getColumnHeaders().trimToSize();
			variables.getItems().trimToSize() ;

			// we have to re-establish the row perspective of the DataTable !!!
			normDataTable.createRowOrientedTable(1);
			
			//
			
            ArrayList<Variable> vari = somData.getVariableItems();

            usedVariables = localNodeCollection.get(0).getAssignates();
            
			usedVariablesIndexes = (ArrayList<Integer>) variables.transcribeUseIndications(usedVariables); usedVariablesIndexes.trimToSize();
			usageIndicationVector = (ArrayList<Double>) variables.transcribeUseIndications(usedVariablesIndexes) ; usageIndicationVector.trimToSize();
			
			soappObj.soappNodes.getIntensionalitySurface().setUsageIndicationVector(usageIndicationVector) ;
			soappObj.soappNodes.getSimilarityConcepts().setUsageIndicationVector(usageIndicationVector) ;
			
			soappObj.setUsageVector( usageIndicationVector );
			
			syntheticVarMetric = normDataTable.getColumnHeaders() ;
			profileValues = ArrUtilities.createCollection( usageIndicationVector.size(), 0.0) ;
			
											processFeedBackContainer = new ProcessFeedBackContainer( 69,100, xmlNodeObjects.size(), somApplication);
											processFeedBackContainer.setHostingObject( this.getClass().getSimpleName() );
											processFeedBackContainer.setStepWidth(5.0) ;
											
			for (int i=0;i<soappObj.soappNodes.size();i++){
											processFeedBackContainer.setCurrentProgressValue(i) ;
											
				mnode = soappObj.soappNodes.getNode(i) ; 
				
				
				
				// requires NodesInformer, not necessary for soapp ?
				// registerNodeinNodesInformer( node );
				mnode.getIntensionality().getProfileVector().setVariables( vari ) ;
				mnode.getExtensionality().getStatistics().setVariables(vari);
				
				// thats wrong !! we need a mapping
				loadedValues = localNodeCollection.get(i).modelProfile ;
				loadedVariables = localNodeCollection.get(i).assignates ;
				
				ArrUtilities.resetCollection( profileValues, 0.0);
				
				for (int p=0;p<loadedValues.size();p++){
					String varLabel = loadedVariables.get(p) ;
					int ix = normDataTable.getColumnHeaders().indexOf(varLabel) ;
					if (ix>=0){
						profileValues.set(ix, loadedValues.get(p) );
					}
				}
				
				mnode.alignProfileVector( mnode.getIntensionality().getProfileVector() );
				
				mnode.getProfileVector().setValues(profileValues);
				mnode.getProfileVector().setVariables(vari) ;
				mnode.getProfileVector().setVariablesStr( syntheticVarMetric );
				
				n = localNodeCollection.get(i).getRecordcount() ;
				mnode.getExtensionality().setSupport( n ) ;
				mnode.getExtensionality().setPPV( localNodeCollection.get(i).ppv );
				mnode.getExtensionality().setNPV( localNodeCollection.get(i).npv );
				//soappObj.soappNodes.
				// ProfileVectorIntf profileVector ;
				soappObj.soappNodes.setNode(i,mnode);
				
				out.print(4, "node <"+i+"> ("+mnode.toString()+") :  "+ ArrUtilities.arr2Text( mnode.getProfileVector().getValues(), 3)) ;
			}
			
			// we have a reduced table for normalized data and the appropriate usage vector
			
			result=0;
											out.print(2, "som classification model containing <"+soappObj.nodeCount+" nodes> has been successfully loaded.") ;
											
		}catch(Exception e){
			e.printStackTrace() ;
			result = -7;
		}
		 
		
		return result;
	}
	

	private String getModelThroughPackage(String packageName) {
		 
		String activemodelname="";
		
		
		if (activemodelname.length()>0){
			if (checkCreateLocationCatalog()==false){
				activemodelname="";
			}
		}
		
		return activemodelname;
	}

	private boolean checkCreateLocationCatalog() {
		 boolean rB=false;
		
		 rB = soappModelCatalog.update();
		 
		 return rB;
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
		int   rvix,ctvix,  popoVarIx;
		
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
			rawDataTable = soappTransformer.getDataTableObj();
			columnVarLabels = rawDataTable.getColumnHeaders();

			int columnLen = rawDataTable.getColumn(0).size() ;
			normDataTable = new DataTable(somData, true);
			
			soappTransformer.setDataTableNormalized(normDataTable) ;
			
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

			transformModel = soappTransformer.getTransformationModel() ;
			variables = somData.getVariables() ;
				
				// we need to refer to the imported model for setting up the target table = normDataTable
					// 1. first the data which are provided by the input
				
				for (int i=0;i<rawDataTable.getColcount();i++){
					DataTableCol column = new DataTableCol(normDataTable, i);
					 
					column.setNumeric(true) ; // important for determination of size
					column.setHasHeader(true) ;
					column.setVisibleOutput(true) ;
					
					normDataTable.getDataTable().add(column) ;
				}
					// 2. the columns that are derived, we do not distinguish or analyze the dependencies here
				
			stackList = transformModel.getVariableTransformations();
					
			for (int i=0;i<stackList.size();i++){
			
				// base variable is available through serialized persistence, it has been loaded through transform.xml
				
				Variable perBaseVar = stackList.get(i).getBaseVariable();
				varLabel = perBaseVar.getLabel();

				if ((perBaseVar != null) && (perBaseVar.isDerived())) {
					ctvix = soappTransformer.requiredchains.indexOf(varLabel);
					if (ctvix >= 0) {

						Variable newCVar = new Variable(perBaseVar);

						DataTableCol column = new DataTableCol(normDataTable, i);
						normDataTable.getDataTable().add(column);
						normDataTable.getColumnHeaders().add(newCVar.getLabel());
						variables.additem(newCVar);
					}

				}
				
				SomFluidPluginSettings pluginSettings = soappTransformer.soappProperties.getPluginSettings() ;
				stackList.get(i).setPluginSettings(pluginSettings) ;
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
												out.print(4,"establishing transformation stack (1) for variable : "+ varLabel) ;
												
						popoVarIx = postponedVariables.indexOf(varLabel) ;				
						if ((popoVarIx>=0) || (transformedVariables.indexOf(varLabel)<0)){
if (i==13){
	int zz;
	zz=0;
}
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

				rvix = soappTransformer.requiredVariables.indexOf(varLabel);

				calculateCol = (rvix >= 0) || (variable.isDerived());

				if (calculateCol) {

				}

			} // -> all columns in normalized table

			// filling all empty columns in norm table with -1, such that they are defined
			normDataTable.getColumnHeaders().trimToSize();  
			columnVarLabels = normDataTable.getColumnHeaders();
			DataTableCol tablecol;
			normDataTable.setTablename("normalized");
			
			for (int i=0;i<normDataTable.getColcount();i++){
				
				varLabel = columnVarLabels.get(i) ;
				tablecol = normDataTable.getColumn(i) ;
				
				int rix = soappProperties.getIndexVariableLabels().indexOf(varLabel) ;

				if (tablecol.getCellValues().size()==0){
					// keep raw values for variables that are requested for being routed through...
					if (rix<0){
						cellvalues = ArrUtilities.createCollection( columnLen, -1.0) ;
					}else{
						cellvalues = somData.getData().getColumn(i).getCellValues() ; 
						tablecol.getCellValueStr().addAll( somData.getData().getColumn(i).getCellValueStr() ) ;
					}
					tablecol.getCellValues().addAll(cellvalues) ;
				}
				
				
				
			}
			
			// create row perspective
			normDataTable.createRowOrientedTable();
			
			somData.setNormalizedSomData(normDataTable) ;
			soappTransformer.getDataTableNormalized();
			
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

		ArrayList<Double> cellvalues = null;
		 
		ArrayList<TransformationStack> stackList;

		TransformationModel transformModel;
		TransformationStack varTStack;
		StackedTransformation sT;
		
		
		
		transformSuccess = false;
		try{
			
			transformModel = soappTransformer.getTransformationModel() ;
			variables = somData.getVariables() ;
			
			if (revisitedVariable){
				// remove previously introduced stack and data (first data in algorithm, then algorithm the stackedTransforms
				
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

				rvix = soappTransformer.requiredVariables.indexOf(varLabel);
				ctvix = soappTransformer.requiredchains.indexOf(varLabel);
				
                // TODO better: using requiredRoots...  which would avoid unnecessary work
				
				if ((ctvix >= 0) || (revisitedVariable)){
					// applying the tStack to this variable

					// getting the index on the collection of transformation stacks by variable label
					tix = transformModel.getIndexByLabel(varLabel);

					// get the index in the raw table (which we got as input data for the classification task)
					rdix = rawDataTable.getColumnHeaders().indexOf(varLabel);
												out.print(3,"establishing transformation stack (2) for variable : "+ varLabel) ;
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
								soappTransformer.connectTransformStacksForData(varTStack, 1, false); //
							} else {
							}
							

							// dependent on data format, we have to introduce values differently
							if (sT.getInFormat() > DataTable.__FORMAT_BIN){// __FORMAT_ORGINT) { 
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
									cellvalues = ArrUtilities.createCollection(columnLen, 0.0);
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
								
								
								sT.multiVarInput = true;
								// we need to update the GUIDs of the backward links
								// sT.getInputColumnIDs().add( srcstack.transformGuid );
								sT.getInputColumnIDs().size() ;
								
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
												int srcvix = soappTransformer.requiredchains.indexOf(srcLabel);
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
												// in "AlgoTransformationAbstract"
												sT.getInData().add( srcCellValues );
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
						 
						checkStackForProperNormalizationStack(varTStack) ;
						
						// update is exclusively backward directed (to the left of the table), 
						// NEVER forward directed, to the right of the table thus we can call update here
						varTStack.update();

						// checkStackForProperNormalization( normDataTable, varTStack, varLabel, ctvix);
						
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
	
	
	
	private void checkStackForProperNormalizationStack(TransformationStack varTStack) {
		 
		int tsz ,i, lastValAlgoPos ;
		boolean normFound =false;
		StackedTransformation st ; 
		AlgoTransformationIntf valgo ;
		AlgorithmIntf algo;
		
		// linNorm as last value algorithm ?
		tsz = varTStack.size() ;
		i = tsz-1;
		
		lastValAlgoPos = -1;
		
		while (i>=0){
		
			st = varTStack.getItem(i);
			
			algo = (AlgorithmIntf)(st.getAlgorithm());
			if ((lastValAlgoPos == -1) && (algo.getType() == AlgorithmIntf._ALGOTYPE_VALUE )){
				lastValAlgoPos = i;
				break;
			}
			
			i--;
		} //
		
		if (lastValAlgoPos>0){
			algo = (AlgorithmIntf)varTStack.getItem(lastValAlgoPos).getAlgorithm() ;
			valgo = (AlgoTransformationIntf)(varTStack.getItem(lastValAlgoPos).getAlgorithm());
				
			String cn = valgo.getClass().getSimpleName().toLowerCase() ; 
			if (cn.contains("linearnorm")){
				normFound = true;
			}
		}
		
		
		if (normFound==false){
			try {
			
				varTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
				varTStack.introduceAlgorithmizedStackPosition("LinearNormalization") ;

			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		
	}

	private int checkStackForProperNormalization( DataTable normDataTable, TransformationStack varTStack, String varLabel, int ctvix) {

		int restartPosition = -1;
		ArrayList<Double> cellvalues;
	
		int xp,ip;
		double xpv=100.0, ipv = -100.0;

		
		int colix = normDataTable.getColumnHeaders().indexOf(varLabel);
		
		if (colix>=0){
			
			
			// determine min, max
			// Min, Max within [0..1] ?
			DataTableCol tablecol = normDataTable.getColumn(colix) ;
			
			cellvalues = varTStack.getLastPosition().getOutData();

			xp = arrutil.arrayMaxPos(cellvalues) ;
			ip = arrutil.arrayMinPos(cellvalues, new Double[]{-1.0,-2.0}, -0.0001 );
			if (xp>=0)xpv = cellvalues.get(xp);
			if (ip>=0)ipv = cellvalues.get(ip);

			
			
		} // column defined ?
		
		return restartPosition;
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
		TransformationModel transformModel = soappTransformer.getTransformationModel() ;
		
		six = transformModel.getIndexByLabel(newVarLabel);
		
		newTStack = transformModel.getItem(six) ;
		
		newTStack = new TransformationStack( soappTransformer, soappProperties.getPluginSettings() );
		
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
	 
		TransformationModel transformModel ;
		ArrayList<TransformationStack> modelTransformStacks ;
		
		
		// the variables needed for the profile
		// targetStruc = getTargetVector();
		somData = soappTransformer.getSomData() ;  
		// soappTransformer.setSomData( somApplication.getSomData() );
		somApplication.setSomData(somData) ;
		
		somData.getVariablesLabels() ;
		 
		
		// from here we transfer...
		modelTransformStacks = soappTransformer.getTransformationModel().getVariableTransformations();
		transformModel = soappTransformer.getTransformationModel() ;
		 
		
		transformationModelImported = true;
	}
	
	
	

	// ----------------------------------------------------
	
	public SomAppClassifier getSoappClassifier() {
		return soappClassifier;
	}

	public SomAppTransformer getSoappTransformer() {
		return soappTransformer;
	}

	public String getActiveModel() {
		return activeModel;
	}

	public SomAppProperties getSoappProperties() {
		return soappProperties;
	}

	public void setSoappProperties(SomAppProperties soappProperties) {
		this.soappProperties = soappProperties;
	}

	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public String getModelPackageName() {
		return modelPackageName;
	}

	public void setModelPackageName(String modelPackageName) {
		this.modelPackageName = modelPackageName;
	}

	public SomApplicationIntf getSomApplication() {
		return somApplication;
	}

	public SomModelCatalog getSoappModelCatalog() {
		return soappModelCatalog;
	}

	 

	public String getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(String activeVersion) {
		this.activeVersion = activeVersion;
	}

	public void setActiveModel(String activeModel) {
		this.activeModel = activeModel;
	}

	public String getDataSourceFile() {
		return dataSourceFile;
	}

	public void setDataSourceFile(String dataSourceFile) {
		this.dataSourceFile = dataSourceFile;
	}

	public ArrayList<String> getProvidedVarLabels() {
		return providedVarLabels;
	}

	public void setProvidedVarLabels(ArrayList<String> providedVarLabels) {
		this.providedVarLabels = providedVarLabels;
	}
	
	
	
}

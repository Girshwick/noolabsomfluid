package org.NooLab.somfluid.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidStartup;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.TransformationModel;
import org.NooLab.somtransform.TransformationStack;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.StringsUtil;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Node;


/**
 * 
 * loads a model into an applicable object
 * 
 *
 */
public class SomAppModelLoader {

	transient SomFluidFactory sfFactory ;
	
	transient SomApplicationIntf somApplication;
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
	
	
	transient StringedObjects strgObj = new StringedObjects() ;
	transient DFutils fileutil = new DFutils();
	transient XmlStringHandling xMsg = new XmlStringHandling() ;
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities ();
	transient PrintLog out = new PrintLog(2,true);
	
	// ========================================================================
	public SomAppModelLoader( SomApplicationIntf somApp, SomFluidFactory factory ) {
		 
		somApplication = somApp;
		sfFactory = factory;
		soappProperties = sfFactory.getSomAppProperties() ;
		 
		
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
			soappClassifier   = loadSomAppClassifier(mcItem);
			
			soappTransformer  = loadSomAppTransformer(mcItem);
			
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

	private SomAppTransformer loadSomAppTransformer(ModelCatalogItem mcItem) {
		
		int r;
		SomAppTransformer soappT ;
		
		SomDataObject sdo ;
		sdo = somApplication.getSomData() ;
		
		soappT = new SomAppTransformer( sdo, soappProperties);
		  
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
		
		soappObj = new SomAppSomObject(soappC, soappC.somObjects.size());

		r = loadSomObject( soappObj, mcItem, 0 ) ;
		
		if (r==0){
			soappC.somObjects.add(soappObj) ;
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

	
	
	
	@SuppressWarnings("unchecked")
	private int loadSomObject( SomAppSomObject soappObj, ModelCatalogItem mcItem, int somObjIndex) {
		int result = -1;
		
		String str = "",rawXmlStr , modelFolder="",somxmlFilename="" ;
		Object subtag;
		
		
		try{
			
			modelFolder    = fileutil.createpath(baseFolder, mcItem.packageName ) ;
			somxmlFilename = fileutil.createpath(modelFolder, "som.xml");
			
			rawXmlStr = fileutil.readFile2String(somxmlFilename);
			
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
			
			// <content> ... filter, noise etc. is missing
			
			// get all nodes as object
			Vector<Object> nodeObjects = new Vector<Object>();
			
			// 
			int r = xMsg.setBasicConditionLocation( rawXmlStr,
													"//somobjects/som", // path  
													"index", 			// attribute containing the condition 
													""+somObjIndex ,	// the condition 
													"/lattice");		// a node below the condition

			nodeObjects = xMsg.getItemsList(rawXmlStr, "nodes", "node", "index") ;
			
			nodeObjects = xMsg.getSpecifiedConditionalNode( rawXmlStr, 	"//somobjects/som", // path  
																		"index", 			// attribute containing the condition 
																		""+somObjIndex ,	// the condition 
																		"/lattice");		// a node below the condition
			
			nodeObjects = xMsg.getNodeList(rawXmlStr, "nodes", "node");
			
			String basicXpath = xMsg.getConditionalXPath()+"/nodes";
	 		
			soappObj.nodeCount = nodeObjects.size();
			soappObj.createNodes();
											out.print(2, "going to load som classification model, <"+soappObj.nodeCount+"> nodes expected...") ;
			for (int i=0;i<nodeObjects.size();i++){
											// provide a process feedback to the available callback
				
				SomAppNodeIntf node = (SomAppNodeIntf)soappObj.soappNodes.getNodes().get(i) ;
				// this derives from the <MetaNodeIntf>
				
				
				Object obj = nodeObjects.get(i) ;
				xMsg.clearBasicConditionLocation() ;
				// get info from that node
				str = xMsg.getSpecifiedItemInfo(obj, "index") ;
				
				String xpath = basicXpath+"/node[@index='"+str+"']" ;
				xMsg.setConditionalXPath(xpath) ;
				
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/ppv", "value");
					   node.setPpv( xMsg.getNum(str, -1.0) );
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/ppvrank", "value");
					  node.setPpvRank( xMsg.getInt(str, 0) );
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
				
			} // i-> all nodeObjects
  
			result=0;
											out.print(2, "som classification model has been successfully loaded.") ;
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

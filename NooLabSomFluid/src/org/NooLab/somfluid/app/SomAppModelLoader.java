package org.NooLab.somfluid.app;

import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.TransformationModel;
import org.NooLab.somtransform.TransformationStack;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.StringsUtil;
import org.w3c.dom.Node;


/**
 * 
 * loads a model into an applicable object
 * 
 *
 */
public class SomAppModelLoader {

	SomApplicationIntf somApplication;
	SomAppProperties soappProperties ;
	
	SomModelCatalog soappModelCatalog ; 
	
	SomAppClassifier soappClassifier;
	SomAppTransformer soappTransformer;
	
	String baseFolder ="";
	String activeModel="";
	String modelPackageName = "" ;
	
	
	transient StringedObjects strgObj = new StringedObjects() ;
	transient DFutils fileutil = new DFutils();
	transient XmlStringHandling xMsg = new XmlStringHandling() ;
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities ();
	transient PrintLog out = new PrintLog(2,true);
	
	// ========================================================================
	public SomAppModelLoader( SomApplicationIntf somApp, SomAppProperties properties ) {
		 
		somApplication = somApp;
		soappProperties = properties ;
		
		soappModelCatalog = new SomModelCatalog( somApp , soappProperties)  ; 
		
		baseFolder = soappProperties.getBaseModelFolder() ;
	}
	
	// ========================================================================
	
	public void loadModel(){
	
		ModelCatalogItem mcItem ;
		
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
		
		if (activeModel.length()==0){
			return;
		}

		mcItem = soappModelCatalog.getItemByModelname( activeModel );
		
		if (mcItem != null){
											out.print(2, "loading resources for requested model <"+activeModel+"> ...  ") ;
			soappClassifier   = loadSomAppClassifier(mcItem);
			
			soappTransformer  = loadSomAppTransformer(mcItem);
			
			
		}else{
			out.print(2, "identification resources for requested model <"+activeModel+"> failed.") ;
		}
	}

	
	
	
	private SomAppTransformer loadSomAppTransformer(ModelCatalogItem mcItem) {
		
		int r;
		SomAppTransformer soappT ;
		
		soappT = new SomAppTransformer();
		  
		r = loadSoappTransformer(soappT, mcItem);
		
		return soappT;
	}

	private SomAppClassifier loadSomAppClassifier(ModelCatalogItem mcItem) {
		
		int r;
		SomAppClassifier soappC ;
		SomAppSomObject soappObj;
		
		
		soappC = new SomAppClassifier() ;
		
		// how many som instances? check:  <som index="0">
		
		soappObj = new SomAppSomObject(soappC, soappC.somObjects.size());

		r = loadSomObject( soappObj, mcItem, 0 ) ;
		
		if (r==0){
			soappC.somObjects.add(soappObj) ;
		}
		
		return soappC;		
	}

	
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
			
			soappT.transformationModel.setOriginalColumnHeaders( new ArrayList<String>( tm.getOriginalColumnHeaders() ));
			// this is very important for setting up the dataTable, data vectors to be classified !!!
			
			soappT.transformationModel.setVariableTransformations(  new ArrayList<TransformationStack>(tm.getVariableTransformations()) ) ;
			
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
				
				SomAppNode node = soappObj.soappNodes.nodes.get(i) ;
				
				Object obj = nodeObjects.get(i) ;
				xMsg.clearBasicConditionLocation() ;
				// get info from that node
				str = xMsg.getSpecifiedItemInfo(obj, "index") ;
				
				String xpath = basicXpath+"/node[@index='"+str+"']" ;
				xMsg.setConditionalXPath(xpath) ;
				
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/ppv", "value");
					  node.ppv = xMsg.getNum(str, -1.0) ;
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/ppvrank", "value");
					  node.ppvRank = xMsg.getInt(str, 0);
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/recordcount", "value");
					  node.recordcount = xMsg.getInt(str, 0) ;
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "description/neighbors", "items");
					  node.neighborsList = xMsg.getListFromXmlStr(str,Integer.class) ;
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "profile/values", "list");
					  node.modelProfile = xMsg.getListFromXmlStr(str,Double.class) ;
				str = xMsg.getSpecifiedInfo(rawXmlStr, "profile/variables", "list");
					  node.assignates = xMsg.getListFromXmlStr(str,String.class) ;
				str = xMsg.getSpecifiedInfo(rawXmlStr, "profile/variances", "list"); 
					  node.modelProfileVariances = xMsg.getListFromXmlStr(str,Double.class) ; 
				
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
	
	
	
}

package org.NooLab.somfluid.clapp;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.strings.StringsUtil;
import org.apache.commons.collections.CollectionUtils;


import com.jamesmurty.utils.XMLBuilder;





/**
 * 
 * 
 * 
 * 
 * 
 *
 */
public class SomModelCatalog implements Serializable{

	private static final long serialVersionUID = -6937656897298307466L;
	

	SomApplicationIntf somApplication;
	
	SomAppProperties soappProperties ;
	
	String rawXmlStr="";
	String baseFolder ="";
	String catalogfilename = "modelcatalog.dat" ;
	
	SomAppModelCatalog  soappModelCatalog;
	
	transient XmlStringHandling xMsg = new XmlStringHandling() ;
	transient SomFluidXMLHelper xEngine = new SomFluidXMLHelper();
	
	transient StringsUtil strgutil = new StringsUtil();
	transient ArrUtilities arrutil = new ArrUtilities ();
	transient DFutils fileutil = new DFutils();
	
	transient private PrintLog out = new PrintLog(2,false);


	
	
	// ========================================================================
	public SomModelCatalog(SomApplicationIntf somApp, SomAppProperties properties) {
		
		somApplication = somApp;
		soappProperties = properties ;
		
		baseFolder = soappProperties.getBaseModelFolder() ;
		soappModelCatalog = new SomAppModelCatalog(somApp,soappProperties)  ;
		
		
	}
	public int size() {
		return soappModelCatalog.size();
	}

	public ModelCatalogItem getItem(int index) {
		return soappModelCatalog.getItem(index);
	}
	
	public ModelCatalogItem getLatestItem() {
		ModelCatalogItem item=null;
		long tival=0;
		
		for (int i=0;i < soappModelCatalog.size();i++){
			item = soappModelCatalog.getItem(i) ;
			if ((tival < item.timevalue) && (soappModelCatalog.getExcludedItems().indexOf(item)<0)){
				tival = item.timevalue;
				item = soappModelCatalog.getItem(i) ;
			}
		}
		return item;
	}
	
	public ModelCatalogItem getBestItem() {
		ModelCatalogItem item=null;
		double sval= 9999999.09;
		
		for (int i=0;i < soappModelCatalog.size();i++){
			item = soappModelCatalog.getItem(i) ;
			if ((sval > item.modelscore) && (item.modelscore>=0) && (soappModelCatalog.getExcludedItems().indexOf(item)<0)){
				sval = item.modelscore;
				item = soappModelCatalog.getItem(i) ;
			}
		}
		return item;
	}
	
 
	// ========================================================================
		
		/*
			<?xml version="1.0" encoding="utf-8"?>
			<sompackages>
				<packages>
				<package name="1" model="bank2">
					<expiry value="0" timestamp=""/>
					<content>
					   <assignates list="">
					</content>
				</package> 
				</packages>
			</sompackages>
	 	*/


	public ModelCatalogItem getItemByModelname(String modelname) {
		
		ModelCatalogItem mci,mcItem = null;
		
		mcItem = soappModelCatalog.getItemByModelname(modelname) ;
		
		return mcItem;
	}
	
	public ModelCatalogItem getItemByModelname(String modelname, String version) {
		
		ModelCatalogItem mci,mcItem = null;
		
		if ((version==null) || (version.length()==0) || (version.contentEquals("*"))){
			mcItem = soappModelCatalog.getItemByModelname(modelname) ;
		}else{
			mcItem = soappModelCatalog.getItemByModelname(modelname,version) ;
		}
		
		return mcItem;
	}
	
	/**
	 * returns entries on the level of project names
	 * @return
	 */
	public ArrayList<String> getAvailableModels(){
		ArrayList<String> mlist = new ArrayList<String>();
		
		mlist = fileutil.listOfSubDirectories( soappProperties.baseModelFolder, "", false) ;
		
		return mlist; 
	}
	
	
	public void addToExcludedItems(ModelCatalogItem item) {
		
		soappModelCatalog.addToExcludedItems(item);
		
	}
	/**
	 * 
	 * first we read the file "modelcatalog.dat" and then we additionally 
	 * scan the directory for further models not contained in the catalog  
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean update() {
		
		boolean rB=false;
		
		String str, catfilename,mLabel ; 
		String domainSpecs="",namedItem;
		Vector<Object> xmlContentItems ;
		ArrayList<String> fieldlabels, requiredfields;
		long timestamp = 0;
		
		
		try {
			catfilename = fileutil.createpath( baseFolder, catalogfilename );
									// through properties, "modelcatalog.dat" ;

			if (fileutil.fileexists(catfilename)==false){
				createEmptyCatalogFile();
				createNewCatalogFile(1); // scanning the directory
				return fileutil.fileexists(catfilename);
			}
			
			xMsg.setContentRoot("sompackages");

			rawXmlStr = fileutil.readFile2String(catfilename);

			
			xmlContentItems = xMsg.getItemsList(rawXmlStr, "//sompackages/packages", "package", "version"); // "version" refers to zip or directory
			
			for (int i=0;i<xmlContentItems.size();i++){
				Object obj = xmlContentItems.get(i) ;
				
				
				try{
					namedItem = (String)obj;	
				}catch(Exception e){
					namedItem = "";
				}
				
				if (namedItem.length()>0){
					// does it exist?
					// as zip -> extract
					
					// as dir, does it contain the files: som.xml, transform.xml
					mLabel = xMsg.getSpecifiedInfo(rawXmlStr, "//sompackages/packages/package", "name", namedItem,"model");
					
					 
					
					if (mLabel.length()>0){
						// does it exist?
						// <content> <assignates list="">
					
						
						
						str = xMsg.getSpecifiedConditionalInfo(rawXmlStr, 	"//sompackages/packages/package",   // path  
																			"name", 				// attribute containing the condition 
																			namedItem ,				// the condition 
																			"/assignates",			// the tag containing the data
																			"list"); 				// the attribute containing the data 
						 	            
						fieldlabels = xMsg.getListFromXmlStr(str, String.class);
						
						

					 
						str = xMsg.getSpecifiedConditionalInfo(rawXmlStr, 	"//sompackages/packages/package",   // path  
																			"name", 				// attribute containing the condition 
																			namedItem ,				// the condition 
																			"/required",			// the tag containing the columns "required" in data to be classified  
																			"list"); 				// the attribute containing the data 
						 	            
						requiredfields = xMsg.getListFromXmlStr(str, String.class);
						 
						
						str = xMsg.getSpecifiedConditionalInfo(rawXmlStr, 	"//sompackages/packages/package",   // path  
																			"name", 				// attribute containing the condition 
																			namedItem ,				// the condition 
																			"/expiry",			    // the tag containing the data
																			"timestamp"); 				// the attribute containing the data 
					          timestamp = xMsg.getTimeLong( str ,0 );
						
						
						// create a catalog item instance
						
						if (fieldlabels.size()>0){

							ModelCatalogItem mcItem = new ModelCatalogItem();
							
							mcItem.fieldlabels = new ArrayList<String>(fieldlabels) ;
							mcItem.requiredfields = new ArrayList<String>(requiredfields) ;
							mcItem.modelName = mLabel;
							mcItem.packageName = namedItem ;
								
							soappModelCatalog.add(mcItem) ;
						}
						
					} // model defined ?
				} // package defined ?
				
			} // i->
			
			
			// str = xMsg.getSpecifiedInfo(rawXmlStr, "//sompackages/packages", "name", "","index");
			
			// now scanning for the existing packages
			// also removes non-existent entries / adding non-registered entries 
			scanDirectory();
			
			// now we can write the updated model catalog file
			
			fileutil.manageBakFile(catfilename, true) ;
			
			createModelCatalogXmlString();
			
			rB = (fileutil.fileexists(catfilename)) ;
			
		} catch (IOException e) {
			e.printStackTrace();
			rB=false;
		}
		return rB;
	}
	

	private void createModelCatalogXmlString() {
		 
		XMLBuilder builder ;
		
		// builder = xEngine.getXmlBuilder("");
		
		createNewCatalogFile(0) ;
		
		return ;
	}
	
	
	private ModelCatalogItem readCatalogItemFromPackage(String pkgname){
		 
		ModelCatalogItem mcItem = null;
		String modelFolder, sfilename,tfilename;
		int r;
		
		modelFolder  = fileutil.createpath(baseFolder, pkgname) ;
		sfilename    = fileutil.createpath(modelFolder, "som.xml"); // TODO: should be procs to constants
		tfilename    = fileutil.createpath(modelFolder, "transform.xml"); 

		if ((fileutil.fileexists(sfilename)) && (fileutil.fileexists(tfilename))){
			mcItem = new ModelCatalogItem();
			
			// reading the som.xml
			r = getSomAppModelCatalogDescription( tfilename, sfilename, mcItem ) ;
			if (r==0){
				mcItem.packageName = pkgname ;
				 
			}
			 
		}
		return mcItem;
	}
	
	@SuppressWarnings("unchecked")
	private int getSomAppModelCatalogDescription( String transformxmlFilename, 
												  String somxmlFilename, 
												  ModelCatalogItem mcItem) {

		int result, ixItem, r;
		XmlStringHandling sxMsg;
		String str,vLabels , rawXmlStr="";
		long timeval;
		ArrayList<String> fieldlabels ;
		Vector<Object> xmlContentItems;

		if (mcItem==null){
			mcItem = new ModelCatalogItem ();
		}else{
			mcItem.fieldlabels = null;
			mcItem.modelName = "" ;
			mcItem.packageName = "" ;
			mcItem.timevalue = 0;
		}
		
		try{
			
			sxMsg = new XmlStringHandling() ;
			sxMsg.setContentRoot("somobjects");
			 
			
			rawXmlStr = fileutil.readFile2String(somxmlFilename).trim();
			
			rawXmlStr = sxMsg.cleanSimple( rawXmlStr );
			
			// for DEBUG test only...
			str = sxMsg.getSpecifiedConditionalInfo( rawXmlStr, 	
													 "//somobjects/som",     	// root path for xpath  
													 "index", 				  	// attribute containing the condition 
													 "0",						// the condition :  <som index="0">
													 "/lattice/description",	// the tag containing the data
													 "nodecount"); 				// the attribute containing the data 

			if (sxMsg.getLastErrorState().length()>0){
				out.printErr(2, sxMsg.getLastErrorState()) ;
				// throw exception
			}
			
			

			str = "" ;
			str = sxMsg.getSpecifiedConditionalInfo( rawXmlStr, 	
													 "//somobjects/som",    
													 "index", 			 
													 "0",				 
													 "/project/general/date",			 
													 "value"); 				 
			    timeval = sxMsg.getTimeLong(str,0) ; // sth like : str = "22/05/2012 06:51:47" ...
			    mcItem.timevalue = timeval ;

			    
			double scorevalue= -1.0;
			str = sxMsg.getSpecifiedConditionalInfo( rawXmlStr, 	
													 "//somobjects/som",   
													 "index", 		 
													 "0",			 
													 "/project/quality/score",			
													 "value"); 				 
			    scorevalue = sxMsg.getNum(str, -1.0) ; 
			    mcItem.modelscore = scorevalue ;

						
			
			r = sxMsg.setBasicConditionLocation( rawXmlStr,
												"//somobjects/som", 
												"index", 			  
												"0" ,			       
												"/lattice");
			/*
			 * any further query will be conditional to this base
			 */
			xmlContentItems = sxMsg.getItemsList(rawXmlStr, "//nodes", "node", "index"); 
			
			
			for (int i=0;i<xmlContentItems.size();i++){
				Object obj = xmlContentItems.get(i) ;
				
				try{
					str = (String)obj;
					ixItem = Integer.parseInt(str) ; 	
				}catch(Exception e){
					ixItem = -1;
					e.printStackTrace();
				}
				
				if (ixItem >= 0){
					vLabels = "";
					// we need a version that delivers the nodes, otherwise the XML query always identifies all node to return one
					vLabels = sxMsg.getSpecifiedConditionalInfo( rawXmlStr, 	
																 "nodes/node",      // path  
																 "index", 			// attribute containing the condition 
																 ""+ixItem,			// the condition 
																 "/profile/variables",			// the tag containing the data
																 "list"); 			// the attribute containing the data 

					
											// out.print(2, vLabels) ;
					fieldlabels = sxMsg.getListFromXmlStr(vLabels, String.class);
					int n = fieldlabels.size()  ; 
					
					mcItem.fieldlabels = fieldlabels;
					 
					if (n>0){
						
						break;
					}
				}
			}
			 
			sxMsg.clearBasicConditionLocation() ;
			
			str = sxMsg.getSpecifiedConditionalInfo(	rawXmlStr, 	
														"//somobjects/som", // path  
														"index", 			// attribute containing the condition 
														"0" ,			    // the condition 
														"/general/name",		// the tag containing the data
														"label"); 			// the attribute containing the data
			 
			mcItem.modelName = str ;
			
			str = fileutil.getParentDir(somxmlFilename);
			str = fileutil.getSimpleName(str);
			mcItem.modelVersion = str;
			
			result =0;
			if ((str==null) || (str.length()==0) || (mcItem.fieldlabels==null) || (mcItem.fieldlabels.size()==0)){
				result = 3;
			}
			
			if (result==0){
				
				XmlStringHandling txMsg = new XmlStringHandling() ;
				
				rawXmlStr = fileutil.readFile2String( transformxmlFilename ).trim();
				rawXmlStr = txMsg.cleanSimple( rawXmlStr );

				txMsg.setContentRoot("somtransformer");

				str = txMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/transformations/requiredvariables", "list") ; // "requiredvariables"
					  if (str.length()>0){
						  mcItem.requiredfields = new ArrayList<String>(txMsg.getListFromXmlStr(str, String.class)) ;
					  }

			 
			}
			
		}catch(Exception e){
			e.printStackTrace();
			result = -7;
		}
		
		return result;
	}
	/**
	 * 
	 *  
	 *  we should scan the directories only, if we have to: </br> </br>
	 *   - model or version not set...  </br>
	 *        if it is set, then we just check whether the </br> 
	 *        requested version is contained in the catalog </br> </br>
	 *   - the directory is not included in the catalog </br>
	 *   - complete re-scan is enforced </br>
	 *    </br>
	 *    
	 *    
	 */
	private void scanDirectory(){
		
		ArrayList<String> subfolders ;
		String pkgname="" ;
		int nd,ix ;
		boolean rB;
		SomAppModelCatalog availableModelCatalog;
		ModelCatalogItem mcItem ;
		
		
		nd = fileutil.enumerateSubDir(baseFolder, "");
		
		subfolders = fileutil.listOfSubDirectories(baseFolder,"",false);
		
		for (int i=0;i<subfolders.size();i++ ){
			pkgname = subfolders.get(i) ; 
			
			// is this package already described in catalog ?
			
			rB = checkCatalogMatch(pkgname) ;
			
			// that's from stored packages:  "som.xml", "transform.xml"
			// its expensive, so we should check whether its necessary!
			mcItem = readCatalogItemFromPackage(pkgname); // into a further structure
			
			// that's from catalog file
			int cin = soappModelCatalog.items.size();
			ix = -1;
			if ( (mcItem!=null) && ( mcItem.packageName.length()>0) && (mcItem.modelName.length()>0)){
				ix = soappModelCatalog.getItemIndexByIDs( mcItem.packageName, mcItem.modelName ) ;
			
			if (ix>=0){
				int dcount = CollectionUtils.disjunction(soappModelCatalog.items.get(ix).fieldlabels , mcItem.fieldlabels).size() ;
					if (dcount == 0) {
						soappModelCatalog.items.get(ix).confirmed = true;
					} else {
						mcItem.confirmed = true;
						soappModelCatalog.add(mcItem);
					}
				} else {
					mcItem.confirmed = true;
					soappModelCatalog.add(mcItem);
				}
			}
			// confirmed
		} // ->
		
		// delete all non-confirmed from the list of mcItems (catalog items)
		int m=soappModelCatalog.items.size()-1;
		int z=0;
		while ((m>=0) && (soappModelCatalog.items.size()>0) && (z<1000)){
			
			mcItem = soappModelCatalog.items.get(m) ;
			if ((mcItem.confirmed==false) || 
				( mcItem.packageName.length()==0) || (mcItem.modelName.length()==0) ){
				soappModelCatalog.items.remove(m) ;
			}
			
			m--; z++;
		} // ->
		nd=0;
	}
	
	
	private boolean checkCatalogMatch(String pkgname) {
		// TODO Auto-generated method stub
		return false;
	}
	private void createNewCatalogFile(int scanFolder) {

		String xstr,fstr,rqfstr,catfilename ; 
		XMLBuilder builder ;
		ModelCatalogItem mcItem ;
		
		builder = xEngine.getXmlBuilder("sompackages") ;
		
		try {
		
			builder = builder.e("packages");
			
			if (scanFolder>=1){
				
				// here, we create a new catalog-file, so we clear the list
				soappModelCatalog.items.clear() ;
				scanDirectory();
				
				if (soappModelCatalog.items.size()==0){
					builder = builder.c("list of available somfluid model packages is empty");
				}

			}
			
			xstr = "";
			
			for (int i=0;i<soappModelCatalog.items.size();i++){
				
				mcItem = soappModelCatalog.items.get(i) ;
				fstr = arrutil.arr2text( mcItem.fieldlabels, ";") ;
				rqfstr = arrutil.arr2text(  mcItem.requiredfields, ";") ;
				
				builder = builder.e("package")
				                          .a("version", mcItem.packageName)
				                          .a("model", mcItem.modelName).c("equals the project name in model building instances") 
				                       .e("expiry").a("value", "0").a("timestamp",""+mcItem.timevalue).up()  // not used so far
				                       .e("content")
				                          .e("assignates").c("the variables as required by the som model, transformation has to provide them") 
				                                  .a("count", ""+mcItem.fieldlabels.size())
				                                  .a("list", fstr).up()
				                          .e("required").c("refers to the columns 'required' in data to be classified ") 
				                                  .a("count", ""+mcItem.requiredfields.size())
				                                  .a("list", rqfstr).up()
				                       .up()
				                 .up();
			} // all catalog items
			
			builder = builder.up() ; // packages
			
			xstr = xEngine.getXmlStr(builder, true) ;
			
			catfilename = fileutil.createpath( baseFolder, catalogfilename );
			fileutil.writeFileSimple(catfilename, xstr);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		xstr="" ;
	}
	
	
	
	private void createEmptyCatalogFile() {
		 
		String xstr,catfilename ; 
		XMLBuilder builder ;
		
		
		builder = xEngine.getXmlBuilder("sompackages") ;
		
		try {
		
			builder.e("packages").c("list of somfluid model packages").up();
			
			
			xstr = xEngine.getXmlStr(builder, true) ;
			
			catfilename = fileutil.createpath( baseFolder, catalogfilename );
			fileutil.writeFileSimple(catfilename, xstr);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}

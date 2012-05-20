package org.NooLab.somfluid.app;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
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


	@SuppressWarnings("unchecked")
	public boolean update() {
		
		boolean rB=false;
		
		String str, catfilename,mLabel ; 
		String domainSpecs="",namedItem;
		Vector<Object> xmlContentItems ;
		ArrayList<String> fieldlabels;
		
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

			
			xmlContentItems = xMsg.getItemsList(rawXmlStr, "//sompackages/packages", "package", "name"); // "name" refers to zip or directory
			
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
						
						// create a catalog item instance
						
						if (fieldlabels.size()>0){

							ModelCatalogItem mcItem = new ModelCatalogItem();
							
							mcItem.fieldlabels = new ArrayList<String>(fieldlabels) ;
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
			r = getSomAppModelCatalogDescription( sfilename, mcItem ) ;
			if (r==0){
				mcItem.packageName = pkgname ;
			}
		}
		return mcItem;
	}
	
	@SuppressWarnings("unchecked")
	private int getSomAppModelCatalogDescription( String somxmlFilename, ModelCatalogItem mcItem) {

		int result, ixItem, r;
		XmlStringHandling sxMsg;
		String str,vLabels ;
		ArrayList<String> fieldlabels ;
		Vector<Object> xmlContentItems;

		if (mcItem==null){
			mcItem = new ModelCatalogItem ();
		}else{
			mcItem.fieldlabels = null;
			mcItem.modelName = "" ;
			mcItem.packageName = "" ;
		}
		
		try{
			
			sxMsg = new XmlStringHandling() ;
			sxMsg.setContentRoot("somobjects");
			 
			
			rawXmlStr = fileutil.readFile2String(somxmlFilename);
			
			// for DEBUG test only...
			str = sxMsg.getSpecifiedConditionalInfo( rawXmlStr, 	
													 "//somobjects/som",   // path  
													 "index", 				// attribute containing the condition 
													 "0",				// the condition 
													 "/lattice/description",			// the tag containing the data
													 "nodecount"); 				// the attribute containing the data 

			if (sxMsg.getLastErrorState().length()>0){
				out.printErr(2, sxMsg.getLastErrorState()) ;
				// throw exception
			}
			r = sxMsg.setBasicConditionLocation( rawXmlStr,
												"//somobjects/som", // path  
												"index", 			  // attribute containing the condition 
												"0" ,			      // the condition 
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

					
					out.print(2, vLabels) ;
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
			result =0;
			if ((str.length()==0) || (mcItem.fieldlabels.size()==0)){
				result = 3;
			}
		}catch(Exception e){
			e.printStackTrace();
			result = -7;
		}
		
		return result;
	}
	
	private void scanDirectory(){
		
		ArrayList<String> subfolders ;
		String pkgname="" ;
		int nd ;
		SomAppModelCatalog availableModelCatalog;
		ModelCatalogItem mcItem ;
		
		
		nd = fileutil.enumerateSubDir(baseFolder, "");
		
		subfolders = fileutil.listOfSubDirectories(baseFolder,"",false);
		
		for (int i=0;i<subfolders.size();i++ ){
			pkgname = subfolders.get(i) ; 
			
			// that's from stored packages
			mcItem = readCatalogItemFromPackage(pkgname); // into a further structure
			
			// that's from catalog file
			int cin = soappModelCatalog.items.size();
			
			int ix = soappModelCatalog.getItemIndexByIDs( mcItem.packageName, mcItem.modelName ) ;
			if (ix>=0){
				int dcount = CollectionUtils.disjunction(soappModelCatalog.items.get(ix).fieldlabels , mcItem.fieldlabels).size() ;
				if (dcount==0){
					soappModelCatalog.items.get(ix).confirmed = true ;
				}else{
					mcItem.confirmed = true ;
					soappModelCatalog.add(mcItem) ;
				}
			}else{
				mcItem.confirmed = true ;
				soappModelCatalog.add(mcItem) ;
			}
			// confirmed
		} // ->
		
		// delete all non-confirmed
		int m=soappModelCatalog.items.size()-1;
		
		while (m>=0){
			
			mcItem = soappModelCatalog.items.get(m) ;
			if (mcItem.confirmed==false){
				soappModelCatalog.items.remove(m) ;
			}
			
			m--;
		} // ->
		nd=0;
	}
	
	
	private void createNewCatalogFile(int scanFolder) {

		String xstr,fstr,catfilename ; 
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
			
			for (int i=0;i<soappModelCatalog.items.size();i++){
				mcItem = soappModelCatalog.items.get(i) ;
				fstr = arrutil.arr2text( mcItem.fieldlabels, ";") ;
				
				builder = builder.e("package")
				                          .a("name", mcItem.packageName)
				                          .a("model", mcItem.modelName) 
				                       .e("expiry").a("value", "0").a("timestamp","").up()  // not used so far
				                       .e("content")
				                          .e("assignates")
				                                  .a("count", ""+mcItem.fieldlabels.size())
				                                  .a("list", fstr).up()
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

	public ModelCatalogItem getItemByModelname(String modelname) {
		
		ModelCatalogItem mci,mcItem = null;
		
		mcItem = soappModelCatalog.getItemByModelname(modelname) ;
		
		return mcItem;
	}
	
	
	
}

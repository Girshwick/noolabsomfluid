package org.NooLab.somfluid.components;

import java.io.IOException;
import java.util.Vector;


import org.NooLab.somfluid.SomFluidPluginSettingsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.strings.StringsUtil;



public class AlgorithmDeclarationsLoader {

	
	SomFluidProperties sfProperties;
	SomFluidPluginSettingsIntf pluginSettings;
	
	
	String builtinAlgoConfigFile ;
	
	private IndexedDistances indications = new IndexedDistances() ;
	
	
	StringsUtil strgutil = new StringsUtil();
	DFutils fileutil = new DFutils();
	
	// ========================================================================
	public AlgorithmDeclarationsLoader(SomFluidProperties sfProps) {

		sfProperties = sfProps;
		
		pluginSettings = sfProperties.getPluginSettings() ;
		
		builtinAlgoConfigFile = sfProperties.getAlgorithmsConfigPath();
		builtinAlgoConfigFile = DFutils.createPath(builtinAlgoConfigFile, "builtinscatalog.xml");

		sfProperties.setAlgoDeclarations(this);
	}
	// ========================================================================
	
	
	
	public void load() throws Exception {
	
		IndexDistance xitem ;
		int ix, act, lastix;
		String rawXmlStr="", jarfilename,pkgname,namedItem, str = "",grouplabel;
		XmlStringHandling xMsg = new XmlStringHandling() ;
		
		Vector<Object> xmlContentItems, algorithmSectionNodes; 

		 
		try {
			
			if (fileutil.fileexists(builtinAlgoConfigFile)){
				rawXmlStr = fileutil.readFile2String(builtinAlgoConfigFile) ;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (xMsg.isXML(rawXmlStr)==false){
			throw(new Exception("Basic declaration file is missing"));
		}
		
		 
		xMsg.setContentRoot("somtransformer") ;
		
		// <structure> <package ->  "org.NooLab.somtransform.algo" 
		
		
		
		xmlContentItems = xMsg.getItemsList(rawXmlStr, "//somtransformer/algorithms", "/algorithm", "name") ;
		// <algorithm name="RunningMean"> <algorithm name="abc">, returns: "RunningMean","abc"   
		lastix=0; ix=0;
		
		for (int i=0;i<xmlContentItems.size();i++){
			
			Object obj = xmlContentItems.get(i) ;
			
			try{
				namedItem = (String)obj;	
			}catch(Exception e){
				namedItem = "";
			}
			
			// <algorithm name="LinearNormalization" index ="3" active="1"/>  

			if (namedItem.length()>0){
				
				str = xMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/algorithms/algorithm", "name", namedItem,"index");
					  if (str.length()>0){
						  ix = Integer.parseInt(str) ;
					  }else{
						  ix=lastix+1;
					  }
					  lastix = ix;
					  
				str = xMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/algorithms/algorithm", "name", namedItem,"active");
				  	  if (str.length()>0){
				  		  act = Integer.parseInt(str) ;
				  	  }else{
				  		  act=lastix+1;
				  	  }

				/*
				 *  we also could accept all algorithms here and control activation through authentication...
				 */
				if (act>0){
					xitem = new IndexDistance( i,ix,act,namedItem) ;	
					getIndications().add(xitem);
				}


			}
				 
			  
		} // 
		
		ix=0;
		
	}



	public IndexedDistances readAdvAutoTransforms() {
		  
		
		IndexedDistances autoTransforms = new IndexedDistances() ;
		IndexDistance xitem ;
		int ix;
		String shortname;
		double prio;
		
		// <advancedauto> <items> <item ....
		

		String rawXmlStr="", xfile="", namedItem, str = "" ;
		XmlStringHandling xMsg = new XmlStringHandling() ;
		
		Vector<Object> xmlContentItems; 

		
		xfile = pluginSettings.getCatalogFileName() ;
		try {
			
			if (fileutil.fileexists(xfile)){
				rawXmlStr = fileutil.readFile2String(xfile) ;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (xMsg.isXML(rawXmlStr)==false){
			return autoTransforms;
		}
		
		 
 		xMsg.setContentRoot("somtransformer") ;
		xmlContentItems = xMsg.getItemsList(rawXmlStr, "//somtransformer/advancedauto/items", "/item", "name") ;
		// <algorithm name="RunningMean"> <algorithm name="abc">, returns: "RunningMean","abc"   
		 
		
		for (int i=0;i<xmlContentItems.size();i++){
			Object obj = xmlContentItems.get(i) ;
			
			try{
				namedItem = (String)obj;	
			}catch(Exception e){
				namedItem = "";
			}
			
			 
			if (namedItem.length()>0){
				
				try{
					prio = 1.0 ;

					// reading the particular item 
					// <item name="AdaptiveLogShift" />
					str = xMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/advancedauto/items/item", "name", namedItem,"priority");
						  if (str.length()>0){
							  // put this to the data section ...
							  if (strgutil.isNumericX(str)){
								  prio = Double.parseDouble(str) ;
							  }
						  }
					shortname = xMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/advancedauto/items/item", "name", namedItem,"abbrev");
					// prepare this as item and add it...
					// first get the global index value... could return -1 if the requested algorithm is not active
					ix = sfProperties.getAlgoDeclarations().getIndications().getIndexByStr(namedItem) ;

					// do not add requests for algorithms which are not yet available, or set as active!!
					if ((ix>=0)&&(sfProperties.getAlgoDeclarations().indications.getItem(ix).getDistance()>0.0)){
						xitem = new IndexDistance( i, ix, namedItem);
						xitem.setDataObject(shortname);
						xitem.setDataObject("..."); // group label
						xitem.addDataObject(prio);
						
						autoTransforms.add(xitem) ;
					}
	
				}catch(Exception e){
					// collect and display in the end
				}
								
			}
				  
		} // all "item" entries in xml section 
		
		
		
		
		return autoTransforms;
	}



	public void setIndications(IndexedDistances indications) {
		this.indications = indications;
	}



	public IndexedDistances getIndications() {
		return indications;
	}

}

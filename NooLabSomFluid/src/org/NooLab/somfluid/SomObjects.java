package org.NooLab.somfluid;
 
import java.util.ArrayList;

 
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
 
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.somtransform.SomTransformer;
 
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;

import com.jamesmurty.utils.XMLBuilder;



/**
 * 
 * this object contains alive SomProcess'es
 * 
 * 
 *
 */
public class SomObjects {

	SomFluidFactory sfFactory ;
	SomFluidProperties sfProperties ;
	
	ArrayList<String> xmlImage = new ArrayList<String>();
	
	ArrayList<ArrayList<String>> xmlImages = new ArrayList<ArrayList<String>>(); 
	
	ArrayList<SomContainer> items = new ArrayList<SomContainer>(); 

	
	transient SomFluidXMLHelper xEngine = new SomFluidXMLHelper();
	transient PrintLog out;
	transient StringsUtil strgutils = new StringsUtil();
	
	
	// ========================================================================
	public SomObjects(SomFluidFactory factory) {
		
		sfFactory = factory ;
		sfProperties = sfFactory.sfProperties ;
		
		
		out = factory.getOut() ;
	}
	// ========================================================================
	

	public void addSom(SomHostIntf somHost, String taskGuid ) {
		SomContainer sc;
	 
		sc = new SomContainer(this, somHost,taskGuid);
		 
		items.add( sc ) ;
	}

	// ------------------------------------------------------------------------
	
	private ArrayList<String> createXmlImage( String xmlstr){
		
		ArrayList<String> xmlimage = new ArrayList<String>() ;;
		String[] xmlstrs; 
		
		if (xmlstr.length()>5){

			xmlstrs = xmlstr.split("\n");
			
			xmlimage = new ArrayList<String>( strgutils.changeArrayStyle(xmlstrs) );
		}
		  
		return xmlimage;
	}


	public int extractSomModels( ){
		
		int somxCount=0;
		XMLBuilder builder, smbuilder;
		ArrayList<String> xmlimage = new ArrayList<String>() ;
		
		
		
		if (items.size()==0){
			return -1;
		}
		
		builder = xEngine.getXmlBuilder( "somobjects" );
		
		for (int i=0;i<items.size();i++){
			// there we collect everything ...
			smbuilder = extractSomModel( i, items.get(i).taskGuid );
			
			builder.importXMLBuilder(smbuilder) ;
			
			
			
			// 
			 
		}
		
		builder=builder.up() ;
		
		String xstr = xEngine.getXmlStr(builder, true);
		xmlimage = createXmlImage( xstr ) ;
		
		xmlImages.add(xmlimage);
		xmlImage = xmlimage;
											String hs1 = ""; if (items.size()>1)hs1="s" ;
											out.print(2,"exporting transformation model"+hs1+" finished.");
		return somxCount ;
	}

	/**
	 * creates an XML image of the SOM such that it can be used for 
	 * 
	 * this should be performed by a SomContainer, which provides cross-object
	 * persistence
	 * 
	 * the SomFluidFactory provides a structure SomObjects, which provides a
	 * collection of containers
	 * 
	 * this will also be involved in the ensembles
	 * 
	 */
	public int extractSomModel( ){
		int sc=0;
		
		XMLBuilder builder=null, smBuilder;
		SomContainer item = items.get(items.size()-1);
		
		try{
			

			builder = xEngine.getXmlBuilder( "somobjects" );
			
			
			
			smBuilder = extractSomModel( 0, item.taskGuid);
			
			builder.importXMLBuilder(smBuilder) ;
			
			builder=builder.up() ;
				
			sc=1;
			
			String xstr = xEngine.getXmlStr(builder, true);
			
			xmlImage = createXmlImage( xstr ) ;
			
			xmlImages.add(xmlImage); 
			
											out.print(2,"exporting transformation model finished.");
		}catch(Exception e){
			e.printStackTrace() ;
			sc=-1;
		}
		
		return sc;
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * 
	 * // there we collect everything of a particular som...
	 * 
	 * @param index
	 * @param taskGuid
	 * @return
	 */
	public XMLBuilder extractSomModel(  int index, String taskGuid){
	
		XMLBuilder builder;
	  
		// ................................................
		
		
		SomContainer item = getItemByTaskGuid(taskGuid);
		
		if (item==null){
			return null ;
		}
		
		// ................................................
		
											out.print(2,"exporting transformation model...");
											
		builder = xEngine.getXmlBuilder( "som" ).a("index", ""+index);

			XMLBuilder sb = item.getSomDescriptionXml(xEngine);
			String xstr = xEngine.getXmlStr(sb, false);
			
			builder = builder.importXMLBuilder( sb );
 
		 
		builder = builder.up();
		
		return builder ;
	}
	
	public ArrayList<String> getXmlImage() {
		if (xmlImage==null){
			xmlImage = new ArrayList<String> ();
		}
		return xmlImage;
	}



	
	public SomContainer getItemByTaskGuid(String guidstr) {
		SomContainer sc=null;
		
		for (int i=0;i<items.size();i++){

			if (items.get(i).taskGuid.contentEquals(guidstr)){
				sc = items.get(i);
				break;
			}
		}
		
		return sc;
	}
	
}

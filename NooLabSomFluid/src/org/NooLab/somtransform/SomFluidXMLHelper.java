package org.NooLab.somtransform;

 

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.xml.XMessageAbs;

import com.jamesmurty.utils.XMLBuilder;


/**
 * 
 * 
 *
 */
public class SomFluidXMLHelper extends XMessageAbs{

	
	
	PrintLog out = new PrintLog(2,false);
	
	public SomFluidXMLHelper( ){
		super();
			
	}
	
	
	
	// ------- facade begin ---------
	public XMLBuilder getXmlBuilder( String XFrame ){
		
		return super.getXmlBuilder(XFrame) ;
	}
	
	public String getXmlStr( XMLBuilder builder, boolean fullXML ){
		return super.getXmlStr(builder, fullXML) ;
	}
	
	
	// ------- facade end   ---------

	public String template( BasicMessageParams basics,  String action){ 
	 
		String xmlstr = "" ;
		XMLBuilder builder ;

		
		try {
 
			
			builder = getXmlBuilder( "messageboard" ).a( "name", "spela").a("role", basics.roleOfSender ); 
			
			builder.e("subscription")
			
					.up() ;
			
			  
			
			xmlstr = getXmlStr(builder, true);
			 
			
		}catch(Exception e){
		}
		
		return xmlstr;
	
	}


	public String _transcodeStackedTransformation(int i, StackedTransformation stTransform) {
		String xstr = "", algoName="",outColumn="";
		
		algoName = stTransform.algorithmName;
		outColumn = stTransform.outputColumnLabel ;
		
		 
		
		return xstr;
	}


	
}	

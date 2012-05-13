package org.NooLab.somfluid.util;

 
import org.NooLab.utilities.xml.XMessageAbs;
import org.NooLab.utilities.xml.XMessageIntf;

import com.jamesmurty.utils.XMLBuilder;




public class XmlStringHandling extends XMessageAbs implements XMessageIntf{
	
	
	public XmlStringHandling(){
		super();
		
	}
	
	 
	
	
	
	
	
	public String template( String action){ 
		 
		String xmlstr = "" ;
		XMLBuilder builder ;

		
		try {
 
			
			builder = getXmlBuilder( "messageboard" ).a( "name", "spela").a("role", "" ); 
			
			builder.e("subscription")
			
					.up() ;
			
			  
			
			xmlstr = getXmlStr(builder, true);
			 
			
		}catch(Exception e){
		}
		
		return xmlstr;
	
	}
	
}
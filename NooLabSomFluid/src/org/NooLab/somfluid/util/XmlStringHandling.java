package org.NooLab.somfluid.util;

 
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

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


	public long getTimeLong(String dtstr, long defaulValue) { // sth like "22/05/2012 06:51:47"
		
		long timevalue = defaulValue;
		SimpleDateFormat formatter ;
		
		try{
			
			if ((dtstr==null) || (dtstr.length()==0)){
				timevalue = defaulValue;
				
			}else{

				formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Date date = (Date) formatter.parse(dtstr);

				// Timestamp ts = new Timestamp(date.getTime());

				timevalue = date.getTime();
			}
			
		}catch(Exception e){
			timevalue = defaulValue;
		}
		
		return timevalue;
	}
 

  


	
}
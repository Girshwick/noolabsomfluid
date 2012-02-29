package org.NooLab.utilities.net;

import java.net.*;
 

public class UrlUtilities {

	public UrlUtilities(){
		
	}
	
	
	public static String getDomainFromPageAddr( String urlString ) throws MalformedURLException{
		// String urlString = "http://www.test.example/path/page.htm";

		URL url;
		String domain="" ;

		try{
			url = new URL(urlString);
			domain = url.getHost();
			
		}catch(Exception e){
			System.err.println("critical error while handling url in getDomainFromPageAddr(): "+urlString+" -> "+e.getMessage() );
			// e.printStackTrace();
			domain="";
		}
		
		return domain;
	}
	
	public static String exchangeHostsInAddress( String urlString, String host, String newHost){
		
		return urlString ;
	}
}

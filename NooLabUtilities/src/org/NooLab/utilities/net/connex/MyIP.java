package org.NooLab.utilities.net.connex;

import org.NooLab.utilities.net.pages.UrlSimpleRead;

public class MyIP {

	static String urlStr = "http://www.myip.ch";
	// will return a page which contains:  "Current IP Address: 84.226.241.126"
	
	
	public MyIP(){
		
	}
	
	public static String get(){
		
		String str , parsedReturnStr="";
		int p1,p2;
		
		str = UrlSimpleRead.retrieve( urlStr , true);
		
		if (str.length()>0){
			p1 = str.indexOf("Current IP Address");
			p2 = str.indexOf("\n", p1);
			if (p2<0){
				p2 = str.length();
			}
			str = str.trim();
			str = str.substring(p1,p2);
			
			if ((p1>0) && (p2>p1)){
				str = (str.split(":")[1]).trim() ; 
				
				p1 = str.indexOf("\n");
				if (p1>2){
					str = str.substring(0,p1);
				}
				parsedReturnStr = str;
			}
			
		}
		
		
		return parsedReturnStr;
	}
	
}

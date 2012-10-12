package org.NooLab.utilities.net.pages;

public class HtmlErrorPages {

	public HtmlErrorPages(){
		
	}

	public static boolean htmlPageIsErrorPage(String htmlStr) {
		boolean rB=false;
		
		if (htmlStr.length()>1000){
			return rB;
		}
		
		if (rB==false){
			rB = htmlStr.contains("404");
		}
		if (rB==false){
			rB = htmlStr.contains("401");
		}
		if (rB==false){
			rB = htmlStr.contains("402");
		}
		if (rB==false){
			rB = htmlStr.contains("403");
		}
		if (rB==false){
			rB = htmlStr.contains("404");
		}
		if (rB==false){
			rB = htmlStr.contains("500");
		}

		
		return false;
	}
	
	
	
}

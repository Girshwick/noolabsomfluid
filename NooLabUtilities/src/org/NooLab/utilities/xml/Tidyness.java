package org.NooLab.utilities.xml;



import java.io.InputStream;

import org.w3c.tidy.*;


public class Tidyness {

	
	public Tidyness(){
		
	}
	
	
	
	
	public void test(){
		Tidy tidy; 
		boolean xhtml = true;
		InputStream inputStream = null ;
		
		tidy = new Tidy();
		
		tidy.setXHTML(xhtml);
		
		tidy.parse(inputStream, System.out);
		
	}
}

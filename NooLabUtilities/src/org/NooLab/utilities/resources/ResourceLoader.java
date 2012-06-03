package org.NooLab.utilities.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;



public class ResourceLoader {

	
	String textResource ="" ;
	
	// ========================================================================
	public ResourceLoader(){
		
	}
	// ========================================================================	
	

	/**
	 * 
	 * @param path
	 */
	public boolean loadTextResource( Class hostclz, String internalFilepath ){

		boolean rB = false;
		InputStream  istr;
		
		// we check INSIDE the jar
		if (internalFilepath.length()>=0){
		
			try {

				ClassLoader classLoader ;

				
				istr = hostclz.getResourceAsStream( internalFilepath );
				istr = hostclz.getResourceAsStream( "org/NooLab/somtransform/resources/builtinscatalog-xml.txt" );
				
			    classLoader = Thread.currentThread().getContextClassLoader();
			    // istr = classLoader.getResourceAsStream("org/NooLab/somtransform/resources/builtinscatalog-xml.txt");
			    // input = classLoader.getResourceAsStream("org/NooLab/somtransform/resources/builtinscatalog-xml");
			    
			    istr = classLoader.getResourceAsStream(internalFilepath);

				
				//classLoader.getResourceAsStream ("your/app/package/config.properties");

				if (istr!=null){
					textResource = convert(istr);
				}
				
				rB=true;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			 
		} 
		return rB;
	}
	
	private String convert( InputStream istr ) {
		
		String outStr = "" ;
		
    	try {

			// read it with BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(istr));

			StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			outStr = sb.toString();

			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return outStr ;
	}


	public String getTextResource() {
		return textResource;
	}


	public void setTextResource(String textResource) {
		this.textResource = textResource;
	}
		
}

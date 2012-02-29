package org.NooLab.utilities.strings;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.NooLab.utilities.files.DFutils;
import org.apache.commons.codec.binary.Hex;


@SuppressWarnings("unused")
public class MD5Content {

	MessageDigest md;
	
	int bufferSize = 2048 ;
	
	DFutils filutil = new DFutils(); 
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public MD5Content() {
		
		try {
			
			md = MessageDigest.getInstance("MD5");
			
		} catch (NoSuchAlgorithmException e) {
			 
			e.printStackTrace();
		}
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 

	
	public String getDigest( String sourceLocator ){
		
		String md5 = "" ;
		String filcontentStr, str; 
		boolean locfile = true;
		
		FileInputStream fis;
		BufferedInputStream bis ;
		DataInputStream dis ;
		
		try {
			
			locfile = ( (sourceLocator.toLowerCase().contains("http")==false) &&
						(sourceLocator.toLowerCase().contains("www")==false) );
				
			if (locfile == false){
				str = sourceLocator;
				File fil = new File(sourceLocator);
				
				if (fil.exists()){
					locfile = true;
				} else{
					// check for correct http address ... by trial
					
				}
			}
			
			if (locfile){
				// filcontentStr = filutil.readFile2String ( sourceLocator ) ;
			
				fis = new FileInputStream( sourceLocator );
			      // Here BufferedInputStream is added for fast reading.
			    bis = new BufferedInputStream(fis);
			    dis = new DataInputStream(bis);
			      
			    md5 = getDigest( dis );
			    
			} else {
				
				md5 = fromURL( sourceLocator ) ;
			}
			
			
		} catch (Exception e) {
			System.out.println("problem caused by source locator : "+sourceLocator);
			e.printStackTrace();
		}
		
		
		return md5 ;
	}
	
	
	private String fromURL( String urlStr) throws NoSuchAlgorithmException, 
								  				  FileNotFoundException,
								  				  IOException {

		String digest;
		InputStream is ;
		URL url ;
		
		
		url = new URL(urlStr ); // "http://www.google.com");
		is = url.openStream();
		
		// we have to filter out everything except text, 
		// especially however, time stamps, which are frequently at hte end of the doc
		 
		digest = getDigest( is );

		return digest ; 

	}

	
	public String getDigestforStr( String str) throws NoSuchAlgorithmException, IOException {
		byte[]  strBytes, digest; 
		String md5_str="";
		
		
		strBytes = str.getBytes();
		
		md.update(strBytes, 0, strBytes.length);
		digest = md.digest();
		md5_str = new String(Hex.encodeHex(digest));
		
		return md5_str;
	}
	
	private String getDigest( InputStream is  ) throws NoSuchAlgorithmException, IOException {
		
		byte[] bytes ;
		byte[] digest ;
		String result ;
		
		md.reset();
		
		bytes = new byte[ bufferSize ];
		int numBytes;
		
		while ((numBytes = is.read(bytes)) != -1) {
			md.update(bytes, 0, numBytes);
		}
		
		digest = md.digest();
		result = new String(Hex.encodeHex(digest));
		
		return result;
	}

}

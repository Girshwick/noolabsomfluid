package org.NooLab.utilities.resources;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.NooLab.utilities.files.DFutils;



public class ResourceLoader {

	
	String textResource ="" ;
	BufferedImage imageResource = null;
	
	// ========================================================================
	public ResourceLoader(){
		
	}
	// ========================================================================	
	

	private InputStream getInputStreamFromResource( Class hostclz, String internalFilepath ){
		InputStream  istr=null;
		
		ClassLoader classLoader ;

		
		istr = hostclz.getResourceAsStream( internalFilepath );
		// istr = hostclz.getResourceAsStream( "org/NooLab/somtransform/resources/builtinscatalog-xml.txt" );
		
	    classLoader = Thread.currentThread().getContextClassLoader();
	    // istr = classLoader.getResourceAsStream("org/NooLab/somtransform/resources/builtinscatalog-xml.txt");
	    // input = classLoader.getResourceAsStream("org/NooLab/somtransform/resources/builtinscatalog-xml");
	    
	    istr = classLoader.getResourceAsStream(internalFilepath);
	    
	    if (istr==null){
	    	classLoader = hostclz.getClassLoader();
	    	istr = classLoader.getResourceAsStream(internalFilepath);
	    }
	    if (istr==null){
	    	istr = hostclz.getResourceAsStream(internalFilepath) ;
	    }
		
		
		return istr;
	}
	
	
	public boolean loadImageResource( Class hostclz, String imgResource) throws IOException{ 
		
		// Image iUrl = Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgResource ));
		InputStream  istr=null;
		boolean rB=false;
		
		istr = getInputStreamFromResource( hostclz, imgResource );
		
		if (istr!=null){
			imageResource = ImageIO.read( istr);
			rB=true;
		}
		return rB;
	}

	public BufferedImage getImageResource() {
		return imageResource;
	}
	
	public String saveToTmpFile() throws IOException {
		// 
		String filename="",fname;
		
		if (imageResource!=null){
			DFutils fileutil = new DFutils();
			
			fname = fileutil.createTempFilename("~itx-", "png");
			

			ByteArrayOutputStream oStream = new ByteArrayOutputStream();
			ImageIO.write(imageResource, "png",oStream);
			
			OutputStream outputStream = new FileOutputStream (fname);
			oStream.writeTo(outputStream);
			
			outputStream.close();
			oStream.close();
			
			if (fileutil.fileexists(fname)){
				filename=fname;
			}
		}
		
		return filename;
	}


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

				//classLoader.getResourceAsStream ("your/app/package/config.properties");
				istr = getInputStreamFromResource( hostclz, internalFilepath );

				if (istr!=null){
					textResource = convert(istr);
					rB=true;
				}
				
				
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
		

	public BufferedImage loadBufferedImageResource( String imgResource) throws IOException{ 
		
		
		File imgFile = new File(imgResource);
		BufferedImage img = ImageIO.read( imgFile );

		//setIconImage(
		
		return img;
		
	}

	public BufferedImage _loadImageResource( String imgResource) throws IOException{ 
		
		Image iUrl = Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgResource ));
		File imgFile = new File(imgResource);
		BufferedImage img = ImageIO.read( imgFile );

		//setIconImage(
		
		return img;
		
	}
}

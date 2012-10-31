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
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.PathFinder;
import org.NooLab.utilities.gui.SystemProperties;
import org.NooLab.utilities.strings.StringsUtil;



/**
 * 
 * usage:  
 *  - (new ResourceLoader(this.getClass())).getImage(resourcePath)
 *  - (new ResourceLoader(this.getClass())).getText(resourcePath)
 * 
 * @author kwa
 *
 */
public class ResourceLoader {

	String textResource = "";
	BufferedImage imageResource = null;
	private String jarFileName;
	private Class hostClass;
	
	
	DFutils fileutil = new DFutils() ;
	
	// ========================================================================
	public ResourceLoader() {

	}
	public ResourceLoader( Class clzz) {
		hostClass = clzz;
	}

	// ========================================================================

	// ========================================================================
	
	// ========================================================================

	public String getTextResource() {
		
		return textResource;
	}

	public String getText(String resourcePath) {
		String rText="" ;
		
		if ((textResource==null) || (textResource.length()==0)){
			if (hostClass!=null){
				boolean hb = loadTextResource(hostClass, resourcePath);
				if (hb){
					rText = this.textResource ;
				}
			}
		}
		return rText;
	}
	
	public Image getImage(String resourcePath) {
		
		Image img=null;
		
		
		if (hostClass==null){
			return null;
		}
		try {
			
			
			boolean hb = loadImageResource(hostClass, resourcePath) ;
			
			if (hb){
				img = imageResource ;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return img;
	}

	/**
	 * 
	 * 
	 * @param clzz         class variable of the main class
	 * @param resourcePath the path within the jar
	 * @param filepath     the path in the folder tree of the OS
	 * @param overwrite    overwrite if it already exists
	 * @return
	 */
	public int externalizeResource( Class clzz, String resourcePath, String filepath, boolean overwrite) {
		// 
		int rB=-1;
		boolean hb;
		
		hb = loadTextResource(clzz, resourcePath);
		
		if (hb){
			String rText = getTextResource();
			if (rText!=null){
				if (fileutil.fileexists(filepath)){
					if (overwrite){
						fileutil.deleteFile(filepath);
					}else{
						return 3;
					}
				}
				String CR="\n";
				if (SystemProperties.isWin()){
					CR="\r\n";
				}
				
				rText = StringsUtil.replaceall(rText,"|||", CR);
				hb = fileutil.writeFileSimple(filepath, rText);
				if (hb){
					rB=0;
				}
			}
		}
		
		return  rB;
	}

	/**
	 * 
	 * 
	 * @param path
	 */
	public boolean loadTextResource(Class hostclz, String internalFilepath) {
	
		boolean rB = false;
		InputStream istr;
	
		// we check INSIDE the jar
		if (internalFilepath.length() >= 0) {
	
			try {
	
				// classLoader.getResourceAsStream
				// ("your/app/package/config.properties");
				istr = getInputStreamFromResource(hostclz, internalFilepath);
	
				if (istr != null) {
					textResource = convert(istr);
					rB = true;
				}
	
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		}
		return rB;
	}

	public String saveToTmpFile() throws IOException {
		//
		String filename = "", fname;
	
		if (imageResource != null) {
			DFutils fileutil = new DFutils();
	
			fname = fileutil.createTempFilename("~itx-", "png");
	
			ByteArrayOutputStream oStream = new ByteArrayOutputStream();
			ImageIO.write(imageResource, "png", oStream);
	
			OutputStream outputStream = new FileOutputStream(fname);
			oStream.writeTo(outputStream);
	
			outputStream.close();
			oStream.close();
	
			if (fileutil.fileexists(fname)) {
				filename = fname;
			}
		}
	
		return filename;
	}

	private InputStream getInputStreamFromResource( Class hostclz, String internalFilepath) {
		
		InputStream istr = null;

		ClassLoader classLoader;

		istr = hostclz.getResourceAsStream(internalFilepath);
		// istr = hostclz.getResourceAsStream(
		// "org/NooLab/somtransform/resources/builtinscatalog-xml.txt" );

		classLoader = Thread.currentThread().getContextClassLoader();
		// istr =
		// classLoader.getResourceAsStream("org/NooLab/somtransform/resources/builtinscatalog-xml.txt");
		// input =
		// classLoader.getResourceAsStream("org/NooLab/somtransform/resources/builtinscatalog-xml");

		istr = classLoader.getResourceAsStream(internalFilepath);

		if (istr == null) {
			classLoader = hostclz.getClassLoader();
			istr = classLoader.getResourceAsStream(internalFilepath);
		}
		if (istr == null) {
			istr = hostclz.getResourceAsStream(internalFilepath);
		}

		return istr;
	}

	public boolean loadImageResource(Class hostclz, String imgResource)
			throws IOException {

		// Image iUrl =
		// Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgResource
		// ));
		InputStream istr = null;
		boolean rB = false;

		istr = getInputStreamFromResource(hostclz, imgResource);

		if (istr != null) {
			imageResource = ImageIO.read(istr);
			rB = true;
		}
		return rB;
	}

	public BufferedImage getImageResource() {
		return imageResource;
	}

	private String convert(InputStream istr) {

		String outStr = "";

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
		return outStr;
	}

	public void setTextResource(String textResource) {
		this.textResource = textResource;
	}

	public BufferedImage loadBufferedImageResource(String imgResource)
			throws IOException {

		File imgFile = new File(imgResource);
		BufferedImage img = ImageIO.read(imgFile);

		// setIconImage(

		return img;

	}

	public BufferedImage _loadImageResource(String imgResource)
			throws IOException {

		Image iUrl = Toolkit.getDefaultToolkit().getImage( getClass().getResource(imgResource));
		File imgFile = new File(imgResource);
		BufferedImage img = ImageIO.read(imgFile);

		// setIconImage(

		return img;

	}

	
	public String getJarFileName() {
		return jarFileName;
	}

	public void setJarFileName(String jarFileName) {
		this.jarFileName = jarFileName;
	}

	public String[] filesInJar( String binpath, Class clzz, String extension) throws IOException{
		
		CodeSource src = clzz.getProtectionDomain().getCodeSource();
		ArrayList<String> list = new ArrayList<String>();
		String[] filesarr = new String[0] ;
		ZipInputStream zip ;
		ZipEntry ze ;
		URL jar;
		
		PathFinder pf  = new PathFinder();
		
		binpath = pf.getAppBinPath(clzz,true);
		// this contains the "jarfilename" if it has been started from that!! 
		
		String jarFilename = "" ;
		jarFilename = binpath; // DFutils.createPath(, jarFileName );
		
		if ((DFutils.folderExists(jarFilename)) || (DFutils.fileExists(jarFilename))){
			JarFile jarFile = new JarFile(jarFilename);
		}else{
			return filesarr;
		}
		
		
		if( src != null ) {
		    // jar = src.getLocation();
			jar = new URL("file://"+jarFilename) ;
		    zip = new ZipInputStream( jar.openStream());
		    ze = null;

		    while( ( ze = zip.getNextEntry() ) != null ) {
		        String entryName = ze.getName();
		        if (entryName.endsWith( extension ) ) {
		            list.add( entryName  );
		        }
		    }

		 }
		 filesarr = list.toArray( new String[ list.size() ] );
		 return filesarr;
	}
	
	
	public ArrayList<String> findFilesInJar( String binpath, Class clzz, String path, String extension) throws IOException {
		
		ArrayList<String> files = new ArrayList<String>();
		
		final String[] parts = path.split("\\Q.jar\\\\E");
		
		if (extension.startsWith(".")==false){
			extension = "."+extension;
		}
			
		PathFinder pf  = new PathFinder();
		
		// String binpath = pf.getAppBinPath(clzz,true);
		String jarFilename ;// = pf.getFullJarFilePath() ;
		jarFilename = DFutils.createPath(binpath, jarFileName ); // e.g. "NooLabiTexxWinApp.jar") ;
		
		// if (parts.length == 2)
		if (DFutils.fileExists(jarFilename)){
			
			// String jarFilename = parts[0] + extension;
			String relativePath = jarFilename.replace(File.separatorChar, '/');
			
			JarFile jarFile = new JarFile(jarFilename);
			final Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				final String entryName = entry.getName();
				if (entryName.startsWith(relativePath)) {
					files.add(entryName.replace('/', File.separatorChar));
				}
			}
		}
		return files;
	}
	
}

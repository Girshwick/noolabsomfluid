package org.NooLab.utilities.files;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class PathFinder {

	private String lastError="";
	URL location ;
	String binpath ="" ;
	private String fullJarFilePath="";
	private Class givenClass;
	// ----------------------------------------------------
	public PathFinder (){
		
		try {
		
			location = new URL("");

		} catch (MalformedURLException e) {}
		
	}
	// ----------------------------------------------------
	
	public String getAppBinPath( Class clzz, boolean useJarAsBase) {
		
		if (clzz==null){
			lastError = "input was null.";
			return "";
		}
		
		givenClass = clzz;
		try{
			
			location = clzz.getProtectionDomain().getCodeSource().getLocation();
			
		}catch(Exception e){
			lastError = "Problem to interpret the command sequence \n"+
						""+clzz.getProtectionDomain().getCodeSource().getLocation()+"\n"+
						""+e.getMessage();
			return "";
		}
		
		if (location!=null){
			String str = location.toString();
			
			binpath = str.substring(6); // clips off the "file:/" prefix
		
			binpath = binpath.replace("\\", "/") ;
		}
		// binpath = binpath + "sjdhfkg12376.jar" ;
		
		if (useJarAsBase==false){
			if (binpath.toLowerCase().endsWith(".jar")){
				int p = binpath.lastIndexOf(".jar") ;
				binpath = binpath.substring(0,p-1);
				
				p = binpath.lastIndexOf("/") ;
				if (p>0){
					binpath = binpath.substring(0,p);
				}
			}
		}
		
		return binpath ;
	}

	public String getFullJarFilePath() {
		String jarfile = "";
		if (fullJarFilePath.length()==0){
			String cn = getMainClassname();
			if ((cn==null) || (cn.length()==0)){
				cn = givenClass.getSimpleName() ;
			}
			
			// now we have to check all files "*.jar" whether it contains a class <cn>
			fullJarFilePath = DFutils.createPath(binpath , jarfile);
		}
		return fullJarFilePath;
	}

	public String getMainClassname(){
		return getMainClassName();
	}
	
	public static String getMainClassName()	{
		
		String mainclassName = "";
		int z=0;
		for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
			String str = entry.getKey();
			if (str.startsWith("JAVA_MAIN_CLASS")) {
				mainclassName = entry.getValue();
				break;
			}
			z++;
		}
		// throw new IllegalStateException("Cannot determine main class.");
		return mainclassName ; 
	}


	public String getLastError() {
		return lastError;
	}


	public void setLastError(String lastError) {
		this.lastError = lastError;
	}


	public URL getLocation() {
		return location;
	}


	public void setLocation(URL location) {
		this.location = location;
	}

}

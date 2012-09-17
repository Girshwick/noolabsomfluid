package org.NooLab.utilities.files;

import java.net.URL;
import java.util.Map;

public class PathFinder {

	public String getAppBinPath( Class clzz, boolean useJarAsBase) {
		
		if (clzz==null){
			return "";
		}
		
		URL location = clzz.getProtectionDomain().getCodeSource().getLocation();
		String str = location.toString();
		String binpath = str.substring(6); // clips off the "file:/" prefix
		
		binpath = binpath.replace("\\", "/") ;
		
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

}

package org.NooLab.utilities.files;

public class PathFinder {

	public String getAppBinPath( Class clzz, boolean useJarAsBase) {
		
		if (clzz==null){
			return "";
		}
		String binpath = clzz.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
		
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

}

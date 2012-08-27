package org.NooLab.utilities.files;

import java.io.File;
import java.util.Properties;

import org.NooLab.utilities.logging.SerialGuid;
import org.NooLab.utilities.net.GUID;



public class StartupProperties {

	
	Properties props = new Properties();
	
	DFutils fileutil = new DFutils();
	
	
	public long retrieveNumGuid( String filename, String propertyName, int instIndex ){
		long numGuid = -1L;
		String binpathPropsFile, binpath;
		
		if (propertyName.length()==0){
			propertyName = "guid" ;
		}
		
		binpath = (new PathFinder()).getAppBinPath() ;
		
		binpathPropsFile = DFutils.createPath(binpath, filename);
		
		if (DFutils.fileExists(binpathPropsFile)){
			// read it
			File file = new File(binpathPropsFile) ;
			
			props = fileutil.readPropertiesByFile( file) ;
			propertyName = propertyName +"." + instIndex  ;
			
			if (props.containsKey(propertyName)){
				
				String numGuidStr = (String) props.get(propertyName) ;
				numGuid = Long.parseLong(numGuidStr ) ;
			}else{
				numGuid=-1;
			}
		}
		
		return numGuid;
	} 
	
	public static String retrieveGuid(String filename, String string) {
		
		
		return "";
	}

	public void createStore(String filename, Properties props){
		
		Properties propst = new Properties();
		
		if (fileutil.fileexists(filename)){
			fileutil.deleteFile(filename);
		}
		File file = new File(filename) ;
		fileutil.writePropertiesByFile(file, props);
		
		propst = fileutil.readPropertiesByFile(file) ;
		int n = propst.size() ;
	}

	/**
	 * note that the filename has to be given as a simple name, not path info!!!
	 * storage folder will be the bin dir of the jar package
	 * 
	 * 
	 * @param filename
	 * @param infotype
	 * @return
	 */
	public Object careForInstanceGuid(String filename, int index, Class infotype) {

		String cn = infotype.getSimpleName().toLowerCase() ;
		long numGuid ;
		boolean store=false;
		String guid ,binpath,binpathPropsFile ;
		String numGuidStr ;
		
		Object obj=null;
		
		Properties  props = new Properties();
		
		
		
		if (cn.contains("long")){
			obj = -1L;
			numGuid = retrieveNumGuid( filename , "numguid", index);
			
			if (numGuid<0){
				numGuid = SerialGuid.numericalValue() ;
				
				numGuidStr = ""+ numGuid ;
				props.put("numguid"+"." + index , numGuidStr) ;
				
				store = true;
			}
			obj= numGuid ;
		}
		if (cn.contains("string")){
			obj= "";
			guid = retrieveGuid( filename , "guid");
			
			if ((guid==null) || (guid.length()==0)){
				guid = GUID.randomvalue() ;
				props.put("guid"+"." + index , guid) ;
				store = true;
			}
			obj = guid;
		}
		
		if ((store) && (obj!=null)){
			
			binpath = (new PathFinder()).getAppBinPath() ;
			binpathPropsFile = DFutils.createPath(binpath, filename);
			
			createStore( binpathPropsFile, props);
		}
		
		
		return obj;
	}

}


















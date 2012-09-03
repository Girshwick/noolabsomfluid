package org.NooLab.somfluid.app.up;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.strings.StringsUtil;


/**
 * 
 * simple class with only a few basic fields to remember
 * 
 *
 */
public class IniProperties implements Serializable{
 
	private static final long serialVersionUID = -4913671119673537645L;
	
	public static final String _iniFileName = "~nooSf-###-ini.properties"; 
	
	public static String fluidSomProjectBasePath = "";
	public static String lastProjectName = ""; 
	public static String dataSource = "";
	public static String appInfo = "";
	
	static String iniFileName = _iniFileName.replace("###", "");
	static String binPath = "";
	static IniProperties ini;

	private static String appId;

	
	
	DFutils fileutil = new DFutils (); 
	
	// ------------------------------------------
	
	public IniProperties(){
		// binPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
	}
	// ------------------------------------------	

	public static IniProperties getIni(){
		ini = new IniProperties();
		return ini;
	}
	
	public static void setFlavor( String appid ){
		ini.appId = appid;
		ini.iniFileName = _iniFileName.replace("###", appId);
		ini.iniFileName = ini.iniFileName.replace("--", "-") ;
	}
	
	public void setflavor(String applicationId) {
		 
		setFlavor(applicationId);
	}

	public String getflavor() {
		 
		return getFlavor();
	}
	public static String getFlavor() {
		 
		return ini.appId;
	}
	
	public static String getFluidSomProjectBasePath() {
		return fluidSomProjectBasePath;
	}

	public static void setFluidSomProjectBasePath(String fluidSomProjectBasePath) {
		IniProperties.fluidSomProjectBasePath = fluidSomProjectBasePath;
		
	}
	
	public static void saveIniProperties(){
		
		(new PropertyWriter(ini)).go();
	}
	
	
	public static void loadIniFile(){
		loadIniFile( binPath );
	}
	
	public static void loadIniFile( String binPath){
		
		java.io.FileInputStream fis;
		java.util.Properties props ;
		String path = "",filename;
		
		props = new java.util.Properties();
		
		path = binPath;
		
		filename = DFutils.createPath(binPath, iniFileName) ;
		
		if (DFutils.fileExists(filename)==false){
			return ;
		}
		File iniFile = new java.io.File( filename ) ;
		
		try {

			fis = new java.io.FileInputStream( iniFile );
			props.load(fis);
			fis.close();
			
			
			fluidSomProjectBasePath = StringsUtil.notNullString( props.getProperty("ProjectBasePath"));
			lastProjectName 		= StringsUtil.notNullString( props.getProperty("LastProjectName"));
			dataSource      		= StringsUtil.notNullString( props.getProperty("DataSource"));
			appInfo      		    = StringsUtil.notNullString( props.getProperty("AppInfo"));
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	
	public static String getLastProjectName() {
		return lastProjectName;
	}

	public static void setLastProjectName(String lastProjectName) {
		IniProperties.lastProjectName = lastProjectName;
	}

	 
	public String getBinPath(){
		return binPath;
	}
	public static IniProperties setBinPath(String appBinPath) {
		 
		ini.binPath = appBinPath ;
		
		return ini;
	}

	public static boolean folderExists( String path) {
		DFutils fileutil = new DFutils (); 
		boolean rB=false;
		if (path==null)path="";
		rB = fileutil.direxists( path ) ;
		
		fileutil = null;
		return rB;
	}
}


class PropertyWriter {
	String str, key, val, _binpath;
	IniProperties iniProps;
	
	public PropertyWriter(IniProperties ini) {
		iniProps = ini;
	}
		
	public void go(){
		
		String filename = "";
		Properties props;
		FileOutputStream fos ;
		
		DFutils fileutil = new DFutils (); 
		
		try {
			_binpath = iniProps.getBinPath();
			if (_binpath==null){
				IniProperties.binPath="";
			}
			filename = DFutils.createPath( IniProperties.binPath, IniProperties.iniFileName);
			
			if (fileutil.fileexists(filename)){
				fileutil.deleteFile(filename) ;
			}
			fos = new FileOutputStream( filename ) ;
			
			props = new Properties();

			props.setProperty("ProjectBasePath", StringsUtil.notNullString( IniProperties.fluidSomProjectBasePath));
			props.setProperty("LastProjectName", StringsUtil.notNullString( IniProperties.lastProjectName));
			props.setProperty("DataSource", StringsUtil.notNullString( IniProperties.dataSource));
			props.setProperty("AppInfo", StringsUtil.notNullString( IniProperties.appInfo));
			      		   
			props.store(fos, null);

			 
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		fileutil=null;
	}

}


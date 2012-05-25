package org.NooLab.somfluid;

import java.io.Serializable;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;



public class SomAppPublishing implements Serializable{

	private static final long serialVersionUID = 8308914250803680720L;
	
	public static final int _LOCATION_FILE  = 1;
	public static final int _LOCATION_FTP   = 10; // not implemented yet...
	public static final int _LOCATION_HTTP  = 12; // not implemented yet...
	public static final int _LOCATION_SRV   = 15; // not implemented yet...
	
	transient SomFluidFactory sfFactory ;
	
	int targetMode = _LOCATION_FILE ;
	
	boolean active =false; 
	String publishingBasepath = "" ;
	String version = "" ;
	String packageName = "";
	
	String versionFolder;
	
	transient DFutils fileutil = new DFutils();
	transient PrintLog out  ;
	
	// ========================================================================	
	public SomAppPublishing( ) {
		 
	}

	public SomAppPublishing( SomFluidFactory factory, boolean active, String basepath, String projectname, String version , int targetmode) {
		this.active = active ; 
		packageName = projectname;
		publishingBasepath = basepath ;
		this.version = version;
		targetMode = targetmode ;
		
		sfFactory = factory;
		out = sfFactory.getOut() ;
		
		if ((active) && (targetmode==_LOCATION_FILE)){
			establishSpace();
		}
	}
	// ========================================================================

	
	protected void establishSpace() {
		
		
	}	
	
	/**
	 * 
	 * @param exportDir the folder where the som modeling instance has exported the som.xml, transform.xml
	 * 
	 * @return
	 */
	public int publishApplicationModel(String exportDir) {
		int result = -1;
		String targetDir , publicationDir="";
		String pmsg="";
		boolean copied;
		
		
		if (fileutil.direxists(exportDir)==false){
			return -3;
		}
		
		if (active==false){
			pmsg = "Publishing was not active, nothing has been published.";
			out.print(2, pmsg);
			return -2;
		}
	 
		
		publicationDir = fileutil.createpath(publishingBasepath, packageName+"/models");
		targetDir = fileutil.createpath( publicationDir, version+"/" );
		
		if (fileutil.direxists(publicationDir)==false){
			return -5;
		}
		
		targetDir = fileutil.createEnumeratedSubDir( targetDir, version, 0, 1000, -3 );  
		// 1000 = maxCount, -3 = remove oldest by date, -2 = remove first by sort
			
		versionFolder = fileutil.getSimpleName(targetDir) ;
			
		try{	 
		
			// copy files
			String source="som.xml", dest="";
			
			dest = fileutil.createpath( targetDir,source);
			source = fileutil.createpath( exportDir,source);
			
			copied = fileutil.copyFile(source, dest) ;
			
			source="transform.xml"; dest="";
			
			dest = fileutil.createpath( targetDir,source);
			source = fileutil.createpath( exportDir,source);
			
			copied = fileutil.copyFile(source, dest) ;
		
				pmsg = 	"Upon request, the model <"+packageName+"> has been published:\n"+
						"  path    : " + fileutil.getParentDir(targetDir) + "\n" +
						"  version : " + versionFolder;
				out.print(2, pmsg) ;
			
			
		}catch(Exception e){
			result = -7;
			out.printErr(2, "publishing of model package failed.");
		}
		
		
		return result;
	}

	
	// ------------------------------------------------------------------------
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getPublishingBasepath() {
		return publishingBasepath;
	}

	public void setPublishingBasepath(String publishingBasepath) {
		this.publishingBasepath = publishingBasepath;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getTargetMode() {
		return targetMode;
	}

	public void setTargetMode(int targetMode) {
		this.targetMode = targetMode;
	}

	public String getVersionFolder() {
		return versionFolder;
	}

	public void setVersionFolder(String versionFolder) {
		this.versionFolder = versionFolder;
	}

	
	
}

package org.NooLab.somfluid;

import java.io.File;

import org.NooLab.somfluid.app.IniProperties;
import org.NooLab.utilities.files.DFutils;




public class ProjectSpaceMaintenance {

	String[] sfSystemProjectFolders = new String[]{ "data",
													"data/raw",
													"export",
													"export/packages",
													"export/results",
													"model",
													"model/obj",
													"model/out",
													"model/som",
													"model/som/auto",
													"model/som/user",
													"model/transform",
													"tmp"
													} ;
	String[] sfSystemFoldersAlgorithmCatalog = new String[]{"texx",
															"texx/plugins/transforms",
															"texx/plugins"
															};
	
	String catalogFolder = "somfluid/plugins";
	String builtinAlgoConfigFile ="";
	
	transient DFutils fileutil = new DFutils();
	private String dataSourceFile;
	
	// ========================================================================
	public ProjectSpaceMaintenance(SomFluidFactory factory){
		
		SomFluidProperties sfProperties = factory.getSfProperties() ;
		builtinAlgoConfigFile = sfProperties.getAlgorithmsConfigPath() ;
	}
	
	public ProjectSpaceMaintenance(){
		
	}
	// ========================================================================	
	
	public int projectSpaceExists() throws Exception {
		return projectSpaceExists();
	}
	
	public int projectSpaceExists(String projectSpaceLabel) throws Exception {
		
		int finalStatus=-1;
		
		if ((projectSpaceLabel==null) || (projectSpaceLabel.length()==0)){
			projectSpaceLabel = SomFluidStartup.getProjectSpaceLabel();
		}
		
		String prjSpaceFolder = SomFluidStartup.getProjectBasePath() + projectSpaceLabel ;
		
		boolean rB = DFutils.folderExists(prjSpaceFolder);
			
		if (rB){
			finalStatus=1;
			System.err.println("The project space <"+ projectSpaceLabel+"> already exists.");
		}
		
	
		
		return finalStatus;
	}


	public void establishCatalogFolder() throws Exception{ 
	
		
		String baseFolder = SomFluidStartup.getProjectBasePath();
		
		if ((baseFolder.length()==0) || (fileutil.direxists(baseFolder)==false)){
			//return;
			throw(new Exception("Project base folder does not exist, please set appropriately."));
		}
		
		baseFolder = fileutil.createpath(baseFolder, "somfluid/");
		
		for (int i=0;i<sfSystemFoldersAlgorithmCatalog.length;i++){
			
			String subfolder = sfSystemFoldersAlgorithmCatalog[i];
			subfolder = fileutil.createpath(baseFolder, subfolder+"/");
		}
		
		// TODO: export built-in resources from 
		//       package: org.NooLab.somtransform.resources
		//       internal file: builtinscatalog-xml -> builtinscatalog.xml
 		
	}
	
	public void completeProjectSpaceDirectories() throws Exception {
		
		String baseFolder = SomFluidStartup.getProjectBasePath();
		
		if ((baseFolder.length()==0) || (fileutil.direxists(baseFolder)==false)){
			//return;
			throw(new Exception("Project base folder does not exist, please set appropriately."));
		}
		
		baseFolder = fileutil.createpath(baseFolder, IniProperties.lastProjectName+"/");
		
		for (int i=0;i<sfSystemProjectFolders.length;i++){
			String subfolder = sfSystemProjectFolders[i];
			subfolder = fileutil.createpath(baseFolder, subfolder+"/");
		}
		
	}

	public void organizeRawProjectData() {
	
		String outpath = SomFluidStartup.getProjectBasePath();
		
		
		outpath = fileutil.createpath(outpath, SomFluidStartup.getLastProjectName() );
	
		SomFluidStartup sfsup = new SomFluidStartup();
		
		
		MsgDialog msgDialog = sfsup.getMsgDialog();
		msgDialog.openingMsg = 	"Now you have to select a data source file, \n"+
								"which will be copied into the project space.\n"+
								"You also may select this file later.";
		
		if (msgDialog.show()==false){
			return;
		}
		
		FileSelectionDialog fselect = sfsup.getFileSelectDialog();
		
		fselect.setMessages("abc");
		String filename = fselect.show();
		
		if (fileutil.fileexists(filename)){
		
			File f = new File(filename);
			String infile = f.getName(); f=null; 
			String outfile =  fileutil.createpath(outpath,infile);
			fileutil.copyFile( filename, outfile) ;
			if (fileutil.fileexists(outfile)){
				dataSourceFile = filename;
			}
		}
		
		
	}

	public void duplicateProject( String projectSpaceLabel, int mode ) {

		
	}


	public String[] getSfSystemProjectFolders() {
		return sfSystemProjectFolders;
	}


	public void setSfSystemProjectFolders(String[] sfSystemProjectFolders) {
		this.sfSystemProjectFolders = sfSystemProjectFolders;
	}


	public String getCatalogFolder() {
		return catalogFolder;
	}


	public void setCatalogFolder(String catalogFolder) {
		this.catalogFolder = catalogFolder;
	}

	public String getDataSourceFile() {
		return dataSourceFile;
	}

}

package org.NooLab.somfluid.app.up;

import java.io.IOException;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.NooLab.utilities.dialog.FileFolderChooser;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.PathFinder;
import org.NooLab.utilities.strings.StringsUtil;

import org.NooLab.utilities.inifile.*;



public class SomFluidStartup {

	
	MsgDialog msgDialog;
	InputDialog textInputDialog;

	
	FileSelectionDialog fileSelectDialog;	
	
	static boolean userbasedBaseFolder ;
	
	static String applicationId;
	static String lastProjectName = "";
	private static String lastDataSet = "";
	
	static IniProperties ini;
	
	// ----------------------------------------------------
	public SomFluidStartup(){
		
	}
	// ----------------------------------------------------
	
	
	private static String readbinpref(String binpath) throws Exception{
	/*
	    app.preference.txt
	    [dev]
		pseudobin=D:/data/iTexx/app/bin	
	 */
	 	
		String inidef;
		String pseudobinpath= binpath;
		
		try {
		
			DFutils fileutil = new DFutils();
		
			String path = fileutil.createpath(binpath, "/");
			path = fileutil.createpath( path, "app.preference.txt");
			
			if (DFutils.fileExists(path)){
				inidef = fileutil.readFile2String( path ) ;
			
				// inidef = fileutil.readFile2String( binpath + "app.preference.txt") ;
		
				// IniStyleContent ini = new IniStyleContent(inidef);
				// sections = ini.getIniStyleSections();

				pseudobinpath = (new IniStyleContent()).fromIniText(inidef).section("dev","pseudobin") ;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			pseudobinpath= binpath;
		}

		return pseudobinpath;
	}
	
	
	public static void setProjectSpaceLabel(){
		IniProperties.lastProjectName = "/";
	}
	
	
	public static void setApplicationID(String appid, Class clzz ) throws Exception {
		userbasedBaseFolder = true;
		setApplicationID(appid, clzz, 0) ;
	}
	
	/**
	 * the class variable sends us to the correct bin.
	 * In order to prevent the question about the base directory, use "absolute=1".
	 * In this case, the folder of the jar file will be taken as the base directory;
	 * </br></br>
	 * Use negative values [-n] for using a directory [n] steps above the folder that contains the jar file,
	 * if you don't want to refer to absolute, preferring the question popping up at start up of the app, 
	 * omit the parameter 
	 */
	public static void setApplicationID(String appid, Class clzz, int absolute) throws Exception {	
		String str;
		
		 
		ini = IniProperties.getIni() ;
		
		String pseudobinpath="" ;
		
		userbasedBaseFolder = false;
		
		str= ini.getflavor();
		
		applicationId = appid.trim();
		
		ini.setflavor(applicationId);
		IniProperties.setFlavor(applicationId);
		
		
		str= ini.getflavor();
		str= IniProperties.getFlavor();
		
		String binpath = (new PathFinder()).getAppBinPath( clzz, false); // false: removes the name of the jar file if present
		
		IniProperties.setBinPath( binpath );
		IniProperties.loadIniFile();
/* 
 * 		
 */
System.out.println("\nStartup binpath "+binpath+"\n");

		pseudobinpath = readbinpref(binpath);
		
		if (binpath.toLowerCase().contains("/java/workspace/")){
			userbasedBaseFolder = true;
		}

		if (userbasedBaseFolder){
			if (DFutils.folderExists( pseudobinpath) ){
				userbasedBaseFolder = false;
				String mainfolder = pseudobinpath;
				mainfolder = DFutils.getParentDir(mainfolder);
				IniProperties.fluidSomProjectBasePath = mainfolder;
				IniProperties.saveIniProperties();
			}
		}
		
		
		
		lastProjectName = IniProperties.lastProjectName;
		
		if (IniProperties.folderExists( IniProperties.fluidSomProjectBasePath ) == false){
			
			if (userbasedBaseFolder){
				// System.out.println("\nuser-based folder selection...\n");
				
				IniProperties.fluidSomProjectBasePath = selectProjectHome();
				IniProperties.saveIniProperties();
			}else{
				System.out.println("\nauto-selection for base folder (a)...\n");
				
			}
		}else{
			if (userbasedBaseFolder==false){
				// System.out.println("\nauto-selection for base folder (b)...\n");
			}
		}
	}


	public static String getApplicationID() {
	
		return applicationId;
	}
	public static String selectProjectHome() throws Exception{
		
		String selectDir;
		String selectedFolder="";
		FileFolderChooser fileDialog ;
		
		String actionMsg =  "In the next dialog, you have to select a folder that \n"+
							"will serve as a base folder for all the projects; \n"+
							"these sub-folders' names are equal to the respective project names. \n";
		
		MsgDialog mDlg = new MsgDialog() ;
		if (mDlg.show(actionMsg)==false){
			throw(new Exception("Action has been cancelled."));
		}

		fileDialog = new FileFolderChooser();
		 
		
		fileDialog.selectFolder(IniProperties.fluidSomProjectBasePath); 
		
		selectDir = fileDialog.getSelectedFolder( true ); 
		
		selectDir = selectDir.trim();
		if (selectDir.endsWith(".")){
			selectDir = selectDir.substring(0,selectDir.length()-1);
		}
		if ((selectDir.length()==0) || (selectDir.contentEquals("/"))){
			selectDir = IniProperties.fluidSomProjectBasePath;
		}
		
		if (selectDir.trim().length()<=1){
			throw(new Exception("Action has been cancelled."));
		}
		
		selectedFolder = StringsUtil.replaceall(selectDir, "\\\\", "/");
		selectedFolder = StringsUtil.replaceall(selectedFolder, "\\", "/");
		selectedFolder = StringsUtil.replaceall(selectedFolder, "//", "/");
		selectedFolder = StringsUtil.replaceall(selectedFolder, "/.", "/");
		selectedFolder = DFutils.createPath(selectedFolder , "/");
		
		IniProperties.fluidSomProjectBasePath = selectedFolder ;
		 
		return selectedFolder;
	}
	
	public static String getProjectBasePath(){
		String path = IniProperties.fluidSomProjectBasePath;
		path = StringsUtil.replaceall(path, "\\", "/");
		if (path.endsWith("/")==false){
			path=path+"/" ;
		}
		IniProperties.fluidSomProjectBasePath = path;
		return path ;
	}
	
	/** provides a coherent storage for results under a common base folder  */
	public static String getLastProjectName() {
		lastProjectName = IniProperties.lastProjectName ;
		return lastProjectName;
	}
	
	public static boolean isDataSetAvailable(){
		boolean rB=false;
		
		String filename = IniProperties.dataSource;;
		if (DFutils.fileExists(filename)==false){
			String path = DFutils.createPath( IniProperties.fluidSomProjectBasePath, IniProperties.lastProjectName);
			path = DFutils.createPath(path,"data/raw");
			filename = DFutils.createPath(path,filename);
			rB = DFutils.fileExists(filename);
		}else{
			rB=true;
		}
		return rB;
	}

	/**   */
	public static String getLastDataSet() {
		
		IniProperties.dataSource = StringsUtil.replaceall(IniProperties.dataSource, "\\", "/") ;
		lastDataSet = IniProperties.dataSource ;
		return lastDataSet;
	}

	public static void setProjectSpaceLabel(String label){
		IniProperties.lastProjectName = label;
	}
	
	public static String getProjectSpaceLabel(){
		return IniProperties.lastProjectName;
	}
	
	public static String getNewProjectSpaceLabel() throws Exception{
		 
		String projectSpace ="";
		InputDialog input = new InputDialog();
		
		input.setOpeningMsg("Please provide the name of the new project (as a single word) : ")
		     .setClosingMsg("You will create the following project space : ")
		     .setFailureMsg("cancelled.") ;
		
		
		projectSpace = input.getText() ;
		
		if (projectSpace.trim().toLowerCase().contentEquals("somfluid") ){
			projectSpace = "" ;
		}
			
		if (projectSpace.length()==0){
			throw(new Exception("No input string provided."));
		}
		return projectSpace;
	}

	
	public static void selectActiveProject()  throws Exception{
		String activeProject="";

		FileFolderChooser fileDialog = new FileFolderChooser();
		
		fileDialog.adjustToBaseFolder(true);
		activeProject = fileDialog.selectFolder( IniProperties.fluidSomProjectBasePath ); // "D:/data/projects/"
		
		activeProject = fileDialog.getSelectedFolder( false ); 
		// false = only the folders simple name, true = full path
		
		if (activeProject.length()>0){
			IniProperties.lastProjectName = activeProject;
			IniProperties.saveIniProperties();
		}else{
			throw(new Exception("Nothing selected."));
		}
	}

	public static String  introduceDataSet() {
		String activeDataFile="";

		try{
			
			if (IniProperties.lastProjectName.length()==0){
				SomFluidStartup.selectActiveProject() ;
			}
			
			String idir = DFutils.createPath( IniProperties.fluidSomProjectBasePath, IniProperties.lastProjectName+"/data/") ;
			
			FileFolderChooser fileDialog = new FileFolderChooser();
			
			activeDataFile = fileDialog.openFile(idir) ;
			
			if ( fileDialog.getFullPathFilename().startsWith(IniProperties.fluidSomProjectBasePath)==false){
				activeDataFile = fileDialog.getFullPathFilename();
			}
			if ( activeDataFile.length()>0)  {
				IniProperties.dataSource = activeDataFile;
				IniProperties.saveIniProperties();
			}
			
			
		}catch(Exception e){
			System.err.println(e.getMessage()) ;
		}
		return activeDataFile;
	}

	public static boolean checkClassifierSetting() {
		boolean rB= true;
		
		
		
		return rB;
	}


	
	public MsgDialog getMsgDialog() {
		return (new MsgDialog());
	}

	public static void showMsg(String msgstr) {
		
		(new MsgDialog()).show(msgstr);
	}
	public InputDialog getTextInputDialog() {
		return (new InputDialog()); 
	}
	

	public String openTextInputDialog(String header,String pretext,String defaultStr) {
		String inputStr="";
		
		InputDialog indlg = new InputDialog();
		indlg.openingMsg = pretext;
		try{
			
			inputStr = indlg.getText();
			
		}catch(Exception e){
			
		}
		
		return inputStr;
	}


	public FileSelectionDialog getFileSelectDialog() {
		return (new FileSelectionDialog(this));
	}


	public static void setLastProjectName(String lastProjectName) {
		SomFluidStartup.lastProjectName = lastProjectName;
	}


	public static boolean isConfigAvailable() {
		boolean rB=false;
        
        String filename = IniProperties.dataSource;
        if (DFutils.fileExists(filename)==false){
                String path ;
                
                path = DFutils.createPath( IniProperties.fluidSomProjectBasePath, IniProperties.lastProjectName);
                path = DFutils.createPath(path,"config/");

                filename = DFutils.createPath(path,filename);
                rB = DFutils.fileExists(filename);
        }else{
                rB=true;
        }
        return rB;
	}

	
} // sfs


 





class InputDialog{
	
	String openingMsg = "text";
	String closingMsg = "done.";
	String failureMsg = "done.";

	public String getText() {
		String answer="";
		
		String str = JOptionPane.showInputDialog(null, openingMsg , "NooLab SomFluid", 1);

		if (str != null) {
			JOptionPane.showMessageDialog(null, closingMsg + str, "NooLab SomFluid", 1);
			answer = str;
		} else {
			// JOptionPane.showMessageDialog(null, "You pressed cancel button.", "NooLab SomFluid", 1);
		}
		return answer;
	}

	public String getOpeningMsg() {
		return openingMsg;
	}

	public InputDialog setOpeningMsg(String openingMsg) {
		this.openingMsg = openingMsg;
		return this;
	}

	public String getClosingMsg() {
		return closingMsg;
	}

	public InputDialog setClosingMsg(String closingMsg) {
		this.closingMsg = closingMsg;
		return this;
	}

	public String getFailureMsg() {
		return failureMsg;
	}

	public InputDialog setFailureMsg(String failureMsg) {
		this.failureMsg = failureMsg;
		return this;
	}
}	



 
	

package org.NooLab.somfluid;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.NooLab.somfluid.app.IniProperties;
import org.NooLab.utilities.dialog.FileFolderChooser;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.PathFinder;
import org.NooLab.utilities.strings.StringsUtil;





public class SomFluidStartup {

	
	MsgDialog msgDialog;
	InputDialog textInputDialog;

	
	FileSelectionDialog fileSelectDialog;	
	
	static String applicationId;
	static String lastProjectName = "";
	private static String lastDataSet = "";
	
	
	// ----------------------------------------------------
	public SomFluidStartup(){
		
	}
	// ----------------------------------------------------
	
	public static void setApplicationID(String appid) throws Exception {
		
		applicationId = appid.trim();
		
		IniProperties.setFlavor(applicationId); 
		IniProperties.setBinPath( (new PathFinder()).getAppBinPath() );
		IniProperties.loadIniFile();
		
		lastProjectName = IniProperties.lastProjectName;
		
		if (IniProperties.folderExists( IniProperties.fluidSomProjectBasePath ) == false){
		 	IniProperties.fluidSomProjectBasePath = selectProjectHome();
		 	IniProperties.saveIniProperties();
		}
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

	public InputDialog getTextInputDialog() {
		return (new InputDialog()); 
	}

	public FileSelectionDialog getFileSelectDialog() {
		return (new FileSelectionDialog(this));
	}

	
} // sfs


class MsgDialog{
	
	String openingMsg = "";
	
	
	public MsgDialog(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	
	public boolean show(String msg) {
		openingMsg = msg;
		return show() ;
	}
	
	public boolean show(){
		boolean rB = false;
		if( JOptionPane.showConfirmDialog(null, openingMsg , 
												"NooLab SomFluid", 
												JOptionPane.OK_CANCEL_OPTION) == 0){
			
			// JOptionPane.showMessageDialog(null, "You clicked on \"Ok\" button", "NooLab SomFluid", 1);
			rB = true;
		}
		return rB;
	}
	public String getOpeningMsg() {
		return openingMsg;
	}
	public void setOpeningMsg(String openingMsg) {
		this.openingMsg = openingMsg;
	}
	
}





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



class FileSelectionDialog{

	FileFolderChooser fileDialog ;
	SomFluidStartup sfs;
	
	
	public FileSelectionDialog( SomFluidStartup sfs){
		this.sfs = sfs ;
		
	}
	
	
	public void setMessages(String string){
		
	}
	
	
	public void init( String basepath ){
		
		fileDialog = new FileFolderChooser();
		
		try {
		
			fileDialog.selectFolder(basepath);

		
		} catch (Exception e) {
			e.printStackTrace();
		} 
		

	}
	public String show() {
		

		String selectDir ; // = fileDialog.getSelectedFolder( true ); 
		
		selectDir = SomFluidStartup.introduceDataSet() ;
		
		return selectDir;
	}

	
		
}
	

package org.NooLab.somfluid;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.NooLab.somfluid.app.IniProperties;
import org.NooLab.utilities.dialog.FileFolderChooser;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.PathFinder;

public class SomFluidStartup {

	
	
	
	static String applicationId;
	static String lastProjectName = "";
	private static String lastDataSet = "";
	
	
	// ----------------------------------------------------
	public SomFluidStartup(){
		
	}
	// ----------------------------------------------------
	
	public static void setApplicationID(String appid) {
		
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


	public static String selectProjectHome() {
		
		String selectedFolder="";
		FileFolderChooser fileDialog ;
		
		// looping = false;
		// this.noLoop();
		
		fileDialog = new FileFolderChooser();
		
		fileDialog.selectFolder(IniProperties.fluidSomProjectBasePath); 
		
		selectedFolder = fileDialog.getSelectedFolder( true ); 
		
		IniProperties.fluidSomProjectBasePath = selectedFolder ;
		// looping = true;
		// this.loop() ;
		return selectedFolder;
	}
	
	/** provides a coherent storage for results under a common base folder  */
	public static String getLastProjectName() {
		lastProjectName = IniProperties.lastProjectName ;
		return lastProjectName;
	}

	/**   */
	public static String getLastDataSet() {
		lastDataSet = IniProperties.dataSource ;
		return lastDataSet;
	}

	 
	public static String getProjectSpaceLabel() throws Exception{
		 
		String projectSpace ="";
		InputDialog input = new InputDialog();
		projectSpace = input.getText() ;
		
		if (projectSpace.length()==0){
			throw(new Exception("No label provided."));
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

		String idir = DFutils.createPath( IniProperties.fluidSomProjectBasePath, IniProperties.lastProjectName+"/data/") ;
		
		FileFolderChooser fileDialog = new FileFolderChooser();
		
		activeDataFile = fileDialog.openFile(idir) ;
		
		if ( activeDataFile.length()>0)  {
			IniProperties.dataSource = activeDataFile;
			IniProperties.saveIniProperties();
		}
		
		return activeDataFile;
	}

}


class InputDialog{

	public String getText() {
		String answer="";
		
		String str = JOptionPane.showInputDialog(null, "Enter some text : ", "NooLab SomFluid", 1);

		if (str != null) {
			JOptionPane.showMessageDialog(null, "You will create the following project space : " + str, "NooLab SomFluid", 1);
			answer = str;
		} else {
			// JOptionPane.showMessageDialog(null, "You pressed cancel button.", "NooLab SomFluid", 1);
		}
		return answer;
	}
}
package org.NooLab.somfluid.app.up;

import org.NooLab.utilities.dialog.FileFolderChooser;

public class FileSelectionDialog{

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
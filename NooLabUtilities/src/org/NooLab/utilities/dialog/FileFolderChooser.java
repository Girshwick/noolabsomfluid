package org.NooLab.utilities.dialog;


import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*; 
 
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.strings.StringsUtil;


 

/**
 * 
 * 
 * 
 *
 */
public class FileFolderChooser { 

	private static final long serialVersionUID = -2517466450233667988L;

	public static final int _FFC_FILE   = 1;
	public static final int _FFC_FOLDER = 2;
	
	String[] fileExtensions = new String[]{"*"} ;
	ArrayList<String> supportedExtensions = new ArrayList<String>();
	 

	String basePath  = "" ;
	String selectedFolder = "" ;
	String fullPathFilename = "" ;
	
	private boolean adjustedPath = false;
	
	StringsUtil strgutil = new StringsUtil();

	private int target = 0;
	
	
	//-----------------------------------------------------
	public FileFolderChooser( int target ){
		this.target = target;
	}

	public FileFolderChooser() {
	}

	//-----------------------------------------------------
	

	/**
	 * if adjustedPath=true, only the folder just one level deeper than 
	 * the base folder will be returned; </br>
	 * 
	 * @param fullpathOption false = only the folders simple name, true = full path
	 * 
	 * @return the resulting name string
	 * 
	 */
	public String getSelectedFolder(boolean fullpathOption ) {
		String foldernamestr = "";
		
		
		
		foldernamestr = selectedFolder ;
		
		if (adjustedPath){
			String fstr = selectedFolder;
			int ds = basePath.length() ;
			
			if (ds>2){
				fstr = fstr.substring(ds,fstr.length()) ;
				fstr = strgutil.trimm(fstr, "/") ;
				int p = fstr.indexOf("/");
				if (p>1){
					fstr = selectedFolder.substring(0,ds+p);
					foldernamestr = fstr ;
					foldernamestr = strgutil.trimm(foldernamestr, "/");
				}
			}
			
			// selectedFolder.
			
		}else{
			
			foldernamestr = selectedFolder ;
		}
		
		if(fullpathOption==false){
			
			if (basePath.length()>0){
				foldernamestr = foldernamestr.replace( basePath, "");
				foldernamestr = strgutil.trimm(foldernamestr, "/");
			}
		}
		 
		return foldernamestr;
	}
	//-----------------------------------------------------
	public void setFileExtensions(String[] extensions){
		
		fileExtensions = extensions;
	}
  
	
	public String selectFolder() throws Exception{
		return selectFolder("");
	}
	
	public String selectFolder(String basedir) throws Exception{
		
		target= _FFC_FOLDER ;
		
		if (basedir.length()>0){
			basedir = StringsUtil.replaceall(basedir, "\\\\", "/");
			basedir = StringsUtil.replaceall(basedir, "\\", "/");
			basedir = StringsUtil.replaceall(basedir, "//", "/");
		}
		basePath = basedir;
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		} catch (Exception e) {
			e.printStackTrace();

		}
			
		if (isMacOS()){
			return macOSXFolderSelection(basedir) ;
		}
		final JFileChooser folderselection = new JFileChooser("Please select a project by its base folder!");
		folderselection.setDialogType(JFileChooser.OPEN_DIALOG);
		folderselection.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
         
        if (basedir.length()>0){
        	String _bd = DFutils.createPath(basedir, "."); // if it does not exist, it will open the user's home dir
        	_bd = basedir;
			File baseFolder = new File(_bd);
			folderselection.setCurrentDirectory(baseFolder);
		}
        
        

        folderselection.addPropertyChangeListener( new PropertyChangeListener() {
            											public void propertyChange(PropertyChangeEvent e) {
            												// if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
            												if( e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
            													final File f = (File) e.getNewValue();
            												}
            											} // propertyChange
        											} // new
            									 );

        folderselection.setVisible(true);
        final int result = folderselection.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = folderselection.getSelectedFile();
            selectedFolder = inputVerzFile.getPath();
            selectedFolder = StringsUtil.replaceall(selectedFolder, "\\", "/");
            selectedFolder = StringsUtil.replaceall(selectedFolder, "\\\\", "/");
            selectedFolder = StringsUtil.replaceall(selectedFolder, "//", "/");

            // System.out.println("selected path : " + selectedFolder);
            
        }
        if (result == JFileChooser.CANCEL_OPTION) {
        	selectedFolder = "" ;
        	throw(new Exception("Folder selection dialog has been cancelled."));
        }
        
        folderselection.setVisible(false);
        
		return selectedFolder;
	}
	
	private String macOSXFolderSelection(String basedir) {
		
		String prompt="" ;
		String fstr="";
		
		Frame frame = new Frame(); 
	    
		FileDialog fd = new FileDialog(frame, prompt, FileDialog.LOAD);
	    
		if ((basedir != null) && (basedir.length()>0)){
	    	  File folder = new File(basedir);
	    	  fd.setDirectory(folder.getParent());
	      }

	      System.setProperty("apple.awt.fileDialogForDirectories", "true");
	    
	      fd.setVisible(true);
	      
	      System.setProperty("apple.awt.fileDialogForDirectories", "false");
	      
	      if (fd.getFile() == null) {
	        return null;
	      }
	      File sf = new File(fd.getDirectory(), fd.getFile());
	      if (sf!=null){
	    	  fstr= sf.getAbsolutePath() ;
	      }
	      return fstr;
	}

	private boolean isMacOS() {
		return System.getProperty("os.name").indexOf("Mac") != -1;
	}
	  
	/*
	      
    
     
	 
	 */
	static class FileChooser extends JFileChooser {

		private static final long serialVersionUID = 7030296989280393660L;

		protected JDialog createDialog(Component parent) throws HeadlessException {
			
			JDialog dlg = super.createDialog(parent);
			parent.setLocation(400, 200) ;
			dlg.setLocation(400, 200);
			return dlg;
		}
	}

	   
	public  String openFile(){
		return openFile("") ;
	}
	public  String openFile(String basedir){	
		
		target= _FFC_FILE ;
		// set system look and feel 
		String selectedFilename ="";
		
		supportedExtensions = new ArrayList<String>(Arrays.asList(fileExtensions));  
		try { 
		  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) { 
		  e.printStackTrace();  
		 
		} 
		 
		// create a file chooser 
		FileChooser fc = new FileChooser(); 
		 
		if (basedir.length()>0){
			File baseFolder = new File(basedir+".");
			fc.setSelectedFile(baseFolder);
			
		}
		
		// in response to a button click: 
		// int returnVal = fc.showOpenDialog(applet); 
		 
	    // fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// fc.setBounds(300, 300, 400, 300) ;
        JDialog jdia = new JDialog();
        // jdia.setLocation(300, 400) ;
        // fc.setLocation(300, 400);
	    int returnVal = fc.showOpenDialog(jdia);
	    // JFileChooser setLocation
		
		if (returnVal == JFileChooser.APPROVE_OPTION) { 
		  File file = fc.getSelectedFile(); 
		
		  // (better to write a function and check for all supported extensions) 
		  selectedFilename = file.getName();
		  fullPathFilename = file.getAbsolutePath() ;
		  
		  fullPathFilename = strgutil.replaceAll(fullPathFilename, "\\\\", "/");
		  fullPathFilename = strgutil.replaceAll(fullPathFilename, "\\", "/");
		  fullPathFilename = strgutil.replaceAll(fullPathFilename, "//", "/");
		  
		  if (filenameIsSupported( selectedFilename)){
			  
		  }else{
			  selectedFilename = "" ;
		  }
		  
		} else { 
		  System.out.println("Open command cancelled by user.");
		  selectedFilename = "" ;
		}
		return selectedFilename;
	}
	
	private boolean filenameIsSupported(String selectedFilename) {
		boolean rB=false;
		
		String ext ="" ;
		
		if ((supportedExtensions.indexOf("*")>=0) || (supportedExtensions.indexOf(".*")>=0) || (supportedExtensions.indexOf("*.*")>=0)){
			rB=true;
		}else{
			int p = selectedFilename.lastIndexOf(".");
			int ps = selectedFilename.lastIndexOf("/");
			if ((p>0) && (ps<p)){
				ext = selectedFilename.substring(p,selectedFilename.length()) ;
			} 
			rB = (supportedExtensions.indexOf("*")>=0);
		}
		
		return rB;
	}
	public void adjustToBaseFolder(boolean flag) {

		adjustedPath = flag;
	}

	public String getFullPathFilename() {
		return fullPathFilename;
	}
	
	/*
	 if (file.getName().endsWith("jpg")) { 
		    // load the image using the given file path
		    PImage img = loadImage(file.getPath()); 
		    if (img != null) { 
		      // size the window and show the image 
		      size(img.width,img.height); 
		      image(img,0,0); 
		    } 
		  } else { 
		    // just print the contents to the console 
		    // note: loadStrings can take a Java File Object too 
		    String lines[] = loadStrings(file); 
		    for (int i = 0; i < lines.length; i++) { 
		      println(lines[i]);  
		    } 
		  } 
	 */
}

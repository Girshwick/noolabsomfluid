package org.NooLab.utilities.files;


import java.io.File;


public class FileListinDir {

	public static final void FileListinDir() {

	}
	public static final String[] get( String drivePath) {
		return get( drivePath, true,true);
	}

	public static final String[] get( String drive, boolean onlyFiles, boolean noRecursion) {
		String[] filenames = new String[0];
		File folder;
		File[] listOfFiles ;
		
		if (drive.contains(":")==false){
			drive= drive+":/" ;
		}
		if (drive.contains("/")==false){
			drive= drive.replace("\\","/");

			drive= drive+"/" ;
		}
		drive= drive.replace("//","/");

		
		folder = new File(drive);
		listOfFiles = folder.listFiles();

		filenames = new String[listOfFiles.length];
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				// System.out.println("File " + listOfFiles[i].getName());
				filenames[i] = listOfFiles[i].getAbsolutePath() ;
			} else if (listOfFiles[i].isDirectory()) {
				// System.out.println("Directory " + listOfFiles[i].getName());
			}
			
		}
		
		return filenames;
	}

}

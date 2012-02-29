package org.NooLab.utilities.files;

import java.io.File;

public class DirTreeWalk {

	public void process( File dirfil){
		// perform stuff here
		
	}
	
	
	// Process all files and directories under dir
	public void visitAllDirsAndFiles(File dir) {
	    process(dir);

	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirsAndFiles(new File(dir, children[i]));
	        }
	    }
	}

	// Process only directories under dir
	public void visitAllDirs(File dir) {
	    if (dir.isDirectory()) {
	        process(dir);

	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirs(new File(dir, children[i]));
	        }
	    }
	}

	// Process only files under dir
	public void visitAllFiles(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllFiles(new File(dir, children[i]));
	        }
	    } else {
	        process(dir);
	    }
	}
	
	
}

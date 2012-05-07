package org.NooLab.utilities.files;

import java.io.File;
import java.io.Serializable;

public class FileDataSource implements Serializable{

	private static final long serialVersionUID = 4626300048807573170L;

	// types of sources 
	static public int SRC_LOCAL = 1;
	static public int SRC_HTTP  = 2;
	static public int SRC_FTP   = 3;
	
	// format
	static public int FORMAT_TEXTTAB   = 0 ;
	static public int FORMAT_TEXTGUESS = 1 ;
	/// ... any other, like Excel... 
	static public int FORMAT_ZIPPED    = 10;
	
	String resourceLocator = "" ;
	
	int srcType=-1;   
	int format = 0 ;  // +10 = zipped ;
	
	String description ;
	
	int recordCount ;
	int columnCount ;
	
	String separator = "\t" ;
	
	boolean resourceExists ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public FileDataSource(){
		
	}
	
	public FileDataSource( String filepath, int srctype){
		srcType = srctype;
		resourceLocator = filepath;
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 

	
	
	
	// getters / setters -----------------------------------------------------
	
	public String getResourceLocator() {
		return resourceLocator;
	}

	public void setFilename(String resourceLocator) {
		this.resourceLocator = resourceLocator;
	}




	public int getSrctype() {
		return srcType;
	}




	public void setSrctype(int srctype) {
		this.srcType = srctype;
	}




	public int getFormat() {
		return format;
	}




	public void setFormat(int format) {
		this.format = format;
	}




	public String getDescription() {
		return description;
	}




	public void setDescription(String description) {
		this.description = description;
	}




	public int getRecordCount() {
		return recordCount;
	}




	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}




	public int getColumnCount() {
		return columnCount;
	}




	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}




	public String getSeparator() {
		return separator;
	}




	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public boolean resourceExists() {
		File fil;
		
		fil = new File(resourceLocator) ;
		
		resourceExists = fil.exists() ;
		
		return resourceExists;
	}

	public int getSrcType() {
		return srcType;
	}

	public void setSrcType(int srcType) {
		this.srcType = srcType;
	}

	public boolean isResourceExists() {
		return resourceExists;
	}

	public void setResourceExists(boolean resourceExists) {
		this.resourceExists = resourceExists;
	}

	public void setResourceLocator(String resourceLocator) {
		this.resourceLocator = resourceLocator;
	}
	
}

package org.NooLab.somfluid.data;

public class TableImportSettings {

	boolean isNumeric;
	boolean chgtoNumeric;
	
	boolean mergebyIndex;   // will be added as additional columns
	boolean mergebyColumns; // will be added as additional rows
	
	boolean replace ;
	
	
	public TableImportSettings(){
		
	}

	// ------------------------------------------------------------------------

	public boolean isNumeric() {
		return isNumeric;
	}


	public void setNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
	}


	public boolean isChgtoNumeric() {
		return chgtoNumeric;
	}


	public void setChgtoNumeric(boolean chgtoNumeric) {
		this.chgtoNumeric = chgtoNumeric;
	}


	public boolean isMergebyIndex() {
		return mergebyIndex;
	}


	public void setMergebyIndex(boolean mergebyIndex) {
		this.mergebyIndex = mergebyIndex;
	}


	public boolean isMergebyColumns() {
		return mergebyColumns;
	}


	public void setMergebyColumns(boolean mergebyColumns) {
		this.mergebyColumns = mergebyColumns;
	}


	public boolean isReplace() {
		return replace;
	}


	public void setReplace(boolean replace) {
		this.replace = replace;
	}
	
	
	
}

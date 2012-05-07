package org.NooLab.somfluid.properties;

import java.io.Serializable;

public class SomBagSettings  implements Serializable{

	
	
	private static final long serialVersionUID = 6913194020084874987L;
	
	int sombagRecordsPerNode ;
	int sombagMaxNodeCount   ;
	int sombagMaxRecordCount ; 
	boolean applySomBags;
	boolean autoSomBags;
	
	// ========================================================================
	public SomBagSettings(ModelingSettings modelingSettings) {
		 
	}
	// ========================================================================	
	

	public int getSombagRecordsPerNode() {
		return sombagRecordsPerNode;
	}

	public void setSombagRecordsPerNode(int sombagRecordsPerNode) {
		this.sombagRecordsPerNode = sombagRecordsPerNode;
	}

	public int getSombagMaxNodeCount() {
		return sombagMaxNodeCount;
	}

	public void setSombagMaxNodeCount(int sombagMaxNodeCount) {
		this.sombagMaxNodeCount = sombagMaxNodeCount;
	}

	public int getSombagMaxRecordCount() {
		return sombagMaxRecordCount;
	}

	public void setSombagMaxRecordCount(int sombagMaxRecordCount) {
		this.sombagMaxRecordCount = sombagMaxRecordCount;
	}

	public boolean isApplySomBags() {
		return applySomBags;
	}
	public boolean getApplySomBags() {
		return applySomBags;
	}
	public void setApplySomBags(boolean flag) {
		applySomBags = flag ;		
	}


	public void setAutoSomBags(boolean flag) {
		autoSomBags = flag;
	}
	public boolean getAutoSomBags() {
		return autoSomBags;
	}
	public boolean isAutoSomBags() {
		return autoSomBags;
	}
}

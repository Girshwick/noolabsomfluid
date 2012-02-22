package org.NooLab.somfluid.properties;

public class SomBagSettings {

	
	int sombagRecordsPerNode ;
	int sombagMaxNodeCount   ;
	int sombagMaxRecordCount ; 
	boolean applySomBags;
	
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
}

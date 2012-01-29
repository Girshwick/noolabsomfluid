package org.NooLab.repulsive.components.data;

public class RetrievalParamSet {


	public int task = -1;
	public int xpos = -1;
	public int ypos = -1 ;
	public int particleIndex;
	
	public double surroundExtent=-1.0;
	public int surroundN = -1;  
	public int selectMode ;
	public boolean autoselect;
	
	public String guid = "";
	public int[] particleIndexes = new int[0] ;
	public double[] cfgparams = new double[0] ;
	
	
	
	public RetrievalParamSet(){
		
	}

	public int getTask() {
		return task;
	}

	public String getGuid() {
		return guid;
	}

	public void setSelectMode(int selectMode) {
		this.selectMode = selectMode;
	}

	public void setAutoselect(boolean autoselect) {
		this.autoselect = autoselect;
	}

	public boolean isAutoselect() {
		return autoselect;
	}
}

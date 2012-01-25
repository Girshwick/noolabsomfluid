package org.NooLab.repulsive.components.data;

public class RetrievalParamSet {


	public int task= -1;
	public int xpos;
	public int ypos ;
	public int particleIndex;
	public int surroundN ;  
	public int selectMode;
	public boolean autoselect;
	
	public String guid="";
	
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

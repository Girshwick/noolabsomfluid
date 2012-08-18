package org.NooLab.field.repulsive.components.data;

import java.io.Serializable;



public class RetrievalParamSet implements Serializable{

	private static final long serialVersionUID = -3655819038833178655L;

	
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

	public int getXpos() {
		return xpos;
	}

	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}

	public void setYpos(int ypos) {
		this.ypos = ypos;
	}

	public int getParticleIndex() {
		return particleIndex;
	}

	public void setParticleIndex(int particleIndex) {
		this.particleIndex = particleIndex;
	}

	public double getSurroundExtent() {
		return surroundExtent;
	}

	public void setSurroundExtent(double surroundExtent) {
		this.surroundExtent = surroundExtent;
	}

	public int getSurroundN() {
		return surroundN;
	}

	public void setSurroundN(int surroundN) {
		this.surroundN = surroundN;
	}

	public int[] getParticleIndexes() {
		return particleIndexes;
	}

	public void setParticleIndexes(int[] particleIndexes) {
		this.particleIndexes = particleIndexes;
	}

	public double[] getCfgparams() {
		return cfgparams;
	}

	public void setCfgparams(double[] cfgparams) {
		this.cfgparams = cfgparams;
	}

	public int getSelectMode() {
		return selectMode;
	}

	public void setTask(int task) {
		this.task = task;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
}

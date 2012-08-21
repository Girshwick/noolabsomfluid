package org.NooLab.field.fixed.components;

import org.NooLab.field.fixed.NodeLink;

public class GridNode {

	private int index = -1;
	
	int x=-1,y=-1   ;
	
	int hasNode = 0 ;
	int hasTunnelLink = -1;  // in case of folds, or stacks of fields
	int nodeIsActive;
	
	
	NodeLink tunnelfoldNodeLink;  
	
	int  particleIndex = -1;
	
	
	// ------------------------------------------------------------------------
	public GridNode(int index){
	
		this.setIndex(index);
	}
	// ------------------------------------------------------------------------	


	public void setIndex(int index) {
		this.index = index;
	}


	public int getIndex() {
		return index;
	}


	public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}


	public int getY() {
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}


	public int getHasNode() {
		return hasNode;
	}


	public void setHasNode(int hasNode) {
		this.hasNode = hasNode;
	}


	public int getNodeIsActive() {
		return nodeIsActive;
	}


	public void setNodeIsActive(int nodeIsActive) {
		this.nodeIsActive = nodeIsActive;
	}
	
	
	
}

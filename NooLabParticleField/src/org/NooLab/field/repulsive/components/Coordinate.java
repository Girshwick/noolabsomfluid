package org.NooLab.field.repulsive.components;

public class Coordinate {

	int particleIndex = -1;
	String name = "";
	
	
	public Coordinate( int index, String name){
		this.particleIndex = index;
		this.name = name;
	}
	
	public int getIndex() {
		return particleIndex;
	}

	public void setIndex(int index) {
		this.particleIndex = index;
	}
}

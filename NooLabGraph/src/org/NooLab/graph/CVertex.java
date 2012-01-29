package org.NooLab.graph;


/**
 * 
 * a convenience class for handling the openjgraph lib
 * 
 * 
 */
public class CVertex {

	
	int index;
	double x;
	double y; 
	String label;
 
	 
	public CVertex(int index, double x, double y, String label) {

		this.index = index ;
		this.x = x ;
		this.y = y ; 
		 
		this.label = label ;
	}



	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

 
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	
}

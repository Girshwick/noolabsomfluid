package org.NooLab.utilities.datatypes;

import java.io.Serializable;
import java.util.Vector;



public   class  Coord2D implements Serializable {
 
	private static final long serialVersionUID = -8958939726338041470L;

	Double x = -1.0 ;
	Double y = -1.0 ;
	 
	 
	
	// the name of the area
	String label = "" ;
	
	// some data (or pointer), which is attached to this area
	transient Object data ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public Coord2D(){
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// some geometry...
	
	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

 

	public double getArea(){
		double result = -1.0;
		
		return result;
	}

	
	 
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	
	
}

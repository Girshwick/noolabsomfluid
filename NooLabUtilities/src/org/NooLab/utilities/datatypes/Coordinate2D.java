package org.NooLab.utilities.datatypes;

import java.io.Serializable;
import java.util.Vector;



public   class  Coordinate2D implements Serializable {
 
	private static final long serialVersionUID = -8958939726338041470L;

	Vector<Double> x = new Vector<Double>();
	Vector<Double> y = new Vector<Double>();
	
	// robust handling on/off
	boolean checkArraySize = true;
	
	// the name of the area
	String label = "" ;
	
	// some data (or pointer), which is attached to this area
	transient Object data ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public Coordinate2D(){
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// some geometry...
	
	public double getArea(){
		double result = -1.0;
		
		return result;
	}

	
	public double getLongAxisLen(){
		double result = -1.0;
		
		return result;
	}

	public double getShortAxisLen(){
		double result = -1.0;
		
		return result;
	}

	public double getConcavityOfPolygon(){
		double result = -1.0;
		
		return result;
	}
	
	 
	public double getCenterofGravity( int index){
		double result = -1.0;
		
		return result;
	}

 
	
	// ------------------------------------------------------------------------
	
	private double getXiorYi(int index, Vector<Double> xy){
		double v = -1.0;
		
		if (checkArraySize){
			if (index < 0) {
				index = 0;
			}
			if (index > x.size() - 1) {
				index = x.size() - 1 ;
			}
		}
		
		if (checkArraySize){
			if ((index>=0) && (index < x.size())){
				v = xy.get(index);
			}
		}else{
			v = xy.get(index);
		}
		
		return v;
	}
	
	public double getXi(int index){
		double v = -1.0;
		 
		v = getXiorYi(index,x);
		
		return v;
	}
	
	public double getYi(int index){
		double v = -1.0;
		 
		v = getXiorYi(index,y);
		
		return v;
	}
	
	
	public Vector<Double> getX() {
		return x;
	}

	public void setX(Vector<Double> x) {
		this.x = x;
	}

	public Vector<Double> getY() {
		return y;
	}

	public void setY(Vector<Double> y) {
		this.y = y;
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

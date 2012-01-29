package org.NooLab.graph;

public class PXY implements PointXYIntf{

	double x,y;
	
	public PXY(double _x, double _y) {
		x = _x;
		y = _y ;
	}

	@Override
	public double getX() {

		return x;
	}

	@Override
	public double getY() {
		 
		return y;
	}

	@Override
	public void setX(double _x) {
		 
		x = _x;
	}

	@Override
	public void setY(double _y) {
		 
		y = _y ;
	}

 
	
	
}

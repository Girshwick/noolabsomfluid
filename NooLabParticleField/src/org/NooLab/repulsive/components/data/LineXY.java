package org.NooLab.repulsive.components.data;



public class LineXY implements Cloneable{


	int index = -1;
	int dimensions = 2;
	
	int[] pointIndexes = new int[2] ;
		
	public  double[] x  = new double[2] ;
	public  double[] y  = new double[2] ;
	public  double[] z  = new double[2] ;
	
	
	public LineXY() {
		 
	}
	
	public LineXY( double x1, double y1, double x2,double y2) {
		
		x = new double[]{x1,x2} ;
		y = new double[]{y1,y2} ;
	}
	public LineXY( LineXY iline) {
		
		if (iline==null){
			return;
		}
		
		index = iline.index;
		dimensions = iline.dimensions;
		 
		pointIndexes = new int[]{iline.pointIndexes[0],iline.pointIndexes[1]} ;

		x = new double[]{iline.x[0],iline.x[1]} ;
		y = new double[]{iline.y[0],iline.y[1]} ;
		z = new double[]{iline.z[0],iline.z[1]} ;
	}
	
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getDimensions() {
		return dimensions;
	}
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}
	public int[] getPointIndexes() {
		return pointIndexes;
	}
	public void setPointIndexes(int[] pointIndexes) {
		this.pointIndexes = pointIndexes;
	}
	public double[] getX() {
		return x;
	}
	public void setX(double[] x) {
		this.x = x;
	}
	public double[] getY() {
		return y;
	}
	public void setY(double[] y) {
		this.y = y;
	}
	public double[] getZ() {
		return z;
	}
	public void setZ(double[] z) {
		this.z = z;
	}
	
	 
}

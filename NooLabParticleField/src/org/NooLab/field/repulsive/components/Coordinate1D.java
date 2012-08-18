package org.NooLab.field.repulsive.components;



import java.util.Comparator;



public class Coordinate1D extends Coordinate implements Comparable<Coordinate1D> {

	double cvalue = 0.0;
	

	public Coordinate1D(double value, int index, String name) {
		super(index,name);
		
		this.cvalue = value;
		
	}

	@Override
	public int compareTo(Coordinate1D ccObj) {
		int result = -3;

		if (ccObj.cvalue < cvalue) {
			result = -1;
		}
		if (ccObj.cvalue == cvalue) {
			result = 0;
		}
		if (ccObj.cvalue > cvalue) {
			result = 1;
		}
		
		return result;
	}

	public double getCvalue() {
		return cvalue;
	}

	public void setCvalue(double cvalue) {
		this.cvalue = cvalue;
	}



}

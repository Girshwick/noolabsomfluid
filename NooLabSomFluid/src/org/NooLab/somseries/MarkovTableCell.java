package org.NooLab.somseries;

public class MarkovTableCell {


	public int index = -1;
	public int dimensions = 2;
	
	public  int x  = 0;
	public  int y  = 0;
	public  int mz = 0;

	public  double probability = 0.0 ;
	
	// ------------------------------------------------------------------------
	public MarkovTableCell() {
	}

	public MarkovTableCell(int _x, int _y) {
		 x = _x;
		 y = _y;
	}

	public MarkovTableCell(int _x, int _y, int _z) {
		 x = _x;
		 y = _y;
	}

	public MarkovTableCell(int _x, int _y, double prob) {
		 x = _x;
		 y = _y;
		 probability = prob;
	}

	// ------------------------------------------------------------------------

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

	public int getMz() {
		return mz;
	}

	public void setMz(int mz) {
		this.mz = mz;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double prob) {
		this.probability = prob;
	}
	
}

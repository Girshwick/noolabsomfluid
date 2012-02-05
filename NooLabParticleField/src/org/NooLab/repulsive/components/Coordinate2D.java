package org.NooLab.repulsive.components;


import java.util.HashMap;
import java.util.Map;

public class Coordinate2D extends 
 									Coordinate 
 						  implements
									Comparable<Coordinate2D> {

	public static final int _COMPARE_X  = 1;
	public static final int _COMPARE_Y  = 2;
	public static final int _COMPARE_XY = 4; // x,Y must be identical
	public static final int _COMPARE_D  = 8; // according to temporary value of distance to a dynamically changing external point

	public double cxValue = 0.0, cyValue = 0.0;
	
	// for parallel processing we need a multi-slot storage device for diatance values
	Map<String,Double> TempDistances = new HashMap<String,Double>();
	public String activeHash="";
	
	/**  
	 * hash for sorting the table entries along "dynamic" (virtual) columns
	 * built first from x then from y, 5digits each, starting with 10000000000  */
	long hashvalue = 10000000000L; 
	
	int compareMode = _COMPARE_XY;

	Compare compare;

	public Coordinate2D(double x, double y, int index, String name) {
		super(index, name);

		cxValue = x;
		cyValue = y;

		createHashValue();
		
			
		// compare = new Compare(this);
	}

	@Override
	public int compareTo(Coordinate2D ccObj) {
		int result = 0;

		result = Compare.go(compareMode, ccObj, hashvalue);

		return result;
	}
	
	public void setDistanceValue( String guidStr, double distanceValue){
		
		TempDistances.put(guidStr, distanceValue);
	}

	public double getDistanceValue( String guidStr){
		double distanceOf = -1.0;
		
		try{
			if (TempDistances.containsKey(guidStr)) {
				distanceOf = TempDistances.get(guidStr);
			}
		}catch(Exception e){
			String str;
			str = e.getMessage(); 
			// System.err.println("critical error in Coordinate2D.getDistanceValue()"+str);
		}
		
		return distanceOf;
	}

	public void removeTempDistanceSlot( String guidStr ){
		
		if (TempDistances.containsKey(guidStr) ){
			TempDistances.remove(guidStr) ;
		}
	}
		
	private void createHashValue(){
		String strx,stry, hashStr ;
		long hc;
		int v;
		
		v = (int)Math.round(cxValue*10);
		hc = 100000L + v;
		strx=""+hc;
		
		v = (int)Math.round(cyValue*10);
		hc = 100000L + v;
		stry=""+hc;
		hashStr = strx+stry;
		
		hashvalue = Long.parseLong(hashStr)  ;
	}
	
	public double[] getXYvalue() {
		double[] c = new double[2];

		c[0] = cxValue;
		c[1] = cyValue;
		return c;
	}
	
	public void setXYvalue(double[] cvalues) {
		cxValue = cvalues[0];
		cyValue = cvalues[1];
	}

	public double getXvalue() {
		return cxValue;
	}

	public void setXvalue(double x) {
		this.cxValue = x;
	}

	public double getYvalue() {
		return cyValue;
	}

	public void setYvalue(double y) {
		this.cyValue = y;
	}

	public int getCompareMode() {
		return compareMode;
	}

	public void setCompareMode(int comparemode) {
		this.compareMode = comparemode;
	}

}


class Compare {

	Coordinate2D parent;
	static double threshold = 0.0001;

	static int compareMode;
	static Coordinate2D ccObj;
	static double x;
	static double y;
	static long hashValue; 
	static double TempDistance;
	
	public Compare(Coordinate2D parent) {
		this.parent = parent;
	}

	public static int go(int comparemode, Coordinate2D ccobj, long hashvalue) {
		int result = 0;
		compareMode = comparemode;
		ccObj = ccobj;
		hashValue = hashvalue;

		if (comparemode == Coordinate2D._COMPARE_XY) {
			result = compareXY();
		}
		if (comparemode == Coordinate2D._COMPARE_X) {
			result = compareX();
		}
		if (comparemode == Coordinate2D._COMPARE_Y) {
			result = compareY();
		}

		return result;
	}

	/**
	 * uses a hash value, that is composed as xxxxx-yyyyy
	 * @return
	 */
	public static int compareXY() {
		int result = 0;

		if ((ccObj.hashvalue > hashValue) ) { // && (ccObj.cyValue < y)
			result = -1;
		}
		if (ccObj.hashvalue == hashValue)  { // && (almostEqual(ccObj.cyValue, y))
			result = 0;
		}
		if ((ccObj.hashvalue < hashValue) ) {
			result = 1;
		}
		return result;
	}

	public static int compareX() {
		int result = 0;

		if ((ccObj.cxValue > x)) {
			result = -1;
		}
		if (ccObj.cxValue == x) {
			result = 0;
		}
		if (ccObj.cxValue < x) {
			result = 1;
		}
		return result;
	}

	public static int compareY() {
		int result = 0;

		if ((ccObj.cxValue > x) && (ccObj.cyValue < y)) {
			result = -1;
		}
		if (ccObj.cyValue == y) {
			result = 0;
		}
		if (ccObj.cyValue < y) {
			result = 1;
		}
		return result;
	}
 

	
	
	private static boolean almostEqual(double v1, double v2) {
		boolean rB = false;

		double diff;

		diff = Math.abs(v1 - v2);

		if (diff < threshold) {
			rB = true;
		}

		return rB;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
} // inner class Compare


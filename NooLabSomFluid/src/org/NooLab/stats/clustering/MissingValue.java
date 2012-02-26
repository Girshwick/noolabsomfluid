package org.NooLab.stats.clustering;


public class MissingValue {
	private static double value = -1.0 ;
	private static boolean active = false ;
	
	public MissingValue(){
		
	}

	public static double getValue() {
		return value;
	}

	public static void setValue(double mvalue) {
		 value = mvalue;
	}

	public static boolean isActive() {
		return active;
	}

	public static void setActive(boolean flag) {
		 active = flag;
	}
	
	
	
}

package org.NooLab.math3.stat;


public class MissingValue implements MissingValueIntf{
	
	private  double value = -1.0 ;
	private  boolean active = false ;
	
	private boolean missingSurrogateValueActive = false;
	
	// the potential contribution to a distance
	private double[] surrogates = new double[]{0.0,0.0,0.11} ;
	
	public MissingValue(){
		
	}

	public double getValue() {
		return value;
	}

	public void setValue(double mvalue) {
		 value = mvalue;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean flag) {
		 active = flag;
	}

	@Override
	public boolean isMissingValue(double v) {
		 
		if (active==false){
			return false;
		}
		return value == v;
	}

	@Override
	public double applyOptionalMissingDistance(double p1, double p2) {
		double surrogate=0.0;
		
		if (missingSurrogateValueActive==false){
			return 0.0;
		}
		if ( (isMissingValue(p1)==false) && isMissingValue(p2)){
			surrogate = surrogates[0] ;
		}
		if (isMissingValue(p1) && (isMissingValue(p2)==false)){
			surrogate = surrogates[1] ;
		}
		if (isMissingValue(p1) && isMissingValue(p2)){
			surrogate = surrogates[2] ;
		}
		
		return surrogate;
	}
	
	
}

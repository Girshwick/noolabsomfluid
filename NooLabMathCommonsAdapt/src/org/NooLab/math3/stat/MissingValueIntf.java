package org.NooLab.math3.stat;

public interface MissingValueIntf {

	public double getValue() ;

	public void setValue(double mvalue) ;

	public boolean isActive() ;
	
	public void setActive(boolean flag) ;

	public boolean isMissingValue(double v);

	public double applyOptionalMissingDistance(double p1, double p2);
	
	
}

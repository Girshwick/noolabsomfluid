package org.NooLab.somsprite;

public class FunctionCohortParameterSet {

	
	String varPLabel = "";
	double lo = -1.0;
	double hi = -1.0;
	int steps = 0;
	String scalingFunc = "";
	public double[] cohortValues;
	
	// ========================================================================
	public FunctionCohortParameterSet(String varPLabel, double lo, double hi, int steps, String scalingFunc) {

		this.varPLabel = varPLabel  ;
		this.lo = lo  ;
		this.hi = hi  ;
		this.steps =  steps ;
		this.scalingFunc = scalingFunc  ;
	}
	// ========================================================================
	

	public String getVarPLabel() {
		return varPLabel;
	}

	public void setVarPLabel(String varPLabel) {
		this.varPLabel = varPLabel;
	}

	public double getLo() {
		return lo;
	}

	public void setLo(double lo) {
		this.lo = lo;
	}

	public double getHi() {
		return hi;
	}

	public void setHi(double hi) {
		this.hi = hi;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public String getScalingFunc() {
		return scalingFunc;
	}

	public void setScalingFunc(String scalingFunc) {
		this.scalingFunc = scalingFunc;
	}
	
	
	
}

package org.NooLab.somfluid.util;


public class FDescription{
	
	double[] cfParams ;
	double weightedDeviation ;
	double[] deviations ;
	double[] minima ;
	double[] maxima ;
	private String name;
	
	public FDescription(){
		
	}

	public double[] getCfParams() {
		return cfParams;
	}

	public void setCfParams(double[] cfParams) {
		this.cfParams = cfParams;
	}

	public double getWeightedDeviation() {
		return weightedDeviation;
	}

	public void setWeightedDeviation(double weightedDeviation) {
		this.weightedDeviation = weightedDeviation;
	}

	public double[] getDeviations() {
		return deviations;
	}

	public void setDeviations(double[] devs) {
		this.deviations = devs;
	}

	public double[] getMinima() {
		return minima;
	}

	public void setMinima(double[] minima) {
		this.minima = minima;
	}

	public double[] getMaxima() {
		return maxima;
	}

	public void setMaxima(double[] maxima) {
		this.maxima = maxima;
	}

	public FDescription setName(String rqname) {
		name = rqname;
		return this;
	}

	public String getName() {
		return name;
	}
	
	
}



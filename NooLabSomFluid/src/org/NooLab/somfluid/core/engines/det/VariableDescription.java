package org.NooLab.somfluid.core.engines.det;

import java.util.Vector;

import org.NooLab.somfluid.util.DescriptiveStatistics;




public class VariableDescription {
	  
	int index ;
	String label ;
	boolean activeTV ;
	boolean potentialTV ;
	
	Vector<Double> targetgroups ;
	
	boolean blacklisted ;
	boolean whitelisted ;

	double  weight ;

	DescriptiveStatistics stats = new DescriptiveStatistics() ;
	
	
	public VariableDescription(){
		
	}
	
	
	public String getLabel(){
		return label;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public boolean isActiveTV() {
		return activeTV;
	}


	public void setActiveTV(boolean activeTV) {
		this.activeTV = activeTV;
	}


	public boolean isPotentialTV() {
		return potentialTV;
	}


	public void setPotentialTV(boolean potentialTV) {
		this.potentialTV = potentialTV;
	}


	public Vector<Double> getTargetgroups() {
		return targetgroups;
	}


	public void setTargetgroups(Vector<Double> targetgroups) {
		this.targetgroups = targetgroups;
	}


	public boolean isBlacklisted() {
		return blacklisted;
	}


	public void setBlacklisted(boolean blacklisted) {
		this.blacklisted = blacklisted;
	}


	public boolean isWhitelisted() {
		return whitelisted;
	}


	public void setWhitelisted(boolean whitelisted) {
		this.whitelisted = whitelisted;
	}


	public double getWeight() {
		return weight;
	}


	public void setWeight(double weight) {
		this.weight = weight;
	}


	public DescriptiveStatistics getStats() {
		return stats;
	}


	public void setStats(DescriptiveStatistics stats) {
		this.stats = stats.clone();
	}


	public void setLabel(String label) {
		this.label = label;
	}
	
	
}

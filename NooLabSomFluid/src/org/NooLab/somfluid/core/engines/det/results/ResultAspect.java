package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;



public class ResultAspect implements Serializable{

	private static final long serialVersionUID = -6165009890666557863L;

	public int observationClassType;

	public int classType;
	public double ppv;
	public double npv;
	public int sizeOfClass;
	public double relativeSizeOfClass;

	public ArrayList<String> profileVariablesStr;

	public ArrayList<Double> profileValues;

	public double similarity;

	 
	// ------------------------------------------------------------------------
	public ResultAspect(){
		
	}
	// ------------------------------------------------------------------------
	
	
	public int getClassType() {
		return classType;
	}

	public void setClassType(int classType) {
		this.classType = classType;
	}

	public int getObservationClassType() {
		return observationClassType;
	}


	public void setObservationClassType(int observationClassType) {
		this.observationClassType = observationClassType;
	}


	public double getPpv() {
		return ppv;
	}

	public void setPpv(double ppv) {
		this.ppv = ppv;
	}

	public double getNpv() {
		return npv;
	}

	public void setNpv(double npv) {
		this.npv = npv;
	}

	public int getSizeOfClass() {
		return sizeOfClass;
	}

	public void setSizeOfClass(int sizeOfClass) {
		this.sizeOfClass = sizeOfClass;
	}

	public double getRelativeSizeOfClass() {
		return relativeSizeOfClass;
	}

	public void setRelativeSizeOfClass(double relativeSizeOfClass) {
		this.relativeSizeOfClass = relativeSizeOfClass;
	}


	public ArrayList<String> getProfileVariablesStr() {
		return profileVariablesStr;
	}


	public void setProfileVariablesStr(ArrayList<String> profileVariablesStr) {
		this.profileVariablesStr = profileVariablesStr;
	}


	public ArrayList<Double> getProfileValues() {
		return profileValues;
	}


	public void setProfileValues(ArrayList<Double> profileValues) {
		this.profileValues = profileValues;
	}


	public double getSimilarity() {
		return similarity;
	}


	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
}

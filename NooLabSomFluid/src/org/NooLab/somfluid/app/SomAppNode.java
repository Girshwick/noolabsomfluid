package org.NooLab.somfluid.app;

import java.util.ArrayList;



public class SomAppNode {

	int index = -1;
	
	// <description>
	double ppv = -1 ;
	int ppvRank = -1 ;
	int recordcount = 0; 
	
	ArrayList<Integer> neighborsList = new ArrayList<Integer> ();
	// neighbors items="1;2;10" />	
	
	// <profile>
	// <values
	ArrayList<Double> modelProfile = new ArrayList<Double>(); 

	// <variances
	ArrayList<Double> modelProfileVariances = new ArrayList<Double>();
	
	// variables 
	ArrayList<String> assignates = new ArrayList<String> ();
	
	//-------------------------------------------------------------------------
	public SomAppNode(int ix){
		index = ix;
	}
	//-------------------------------------------------------------------------

	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getPpv() {
		return ppv;
	}

	public void setPpv(double ppv) {
		this.ppv = ppv;
	}

	public int getPpvRank() {
		return ppvRank;
	}

	public void setPpvRank(int ppvRank) {
		this.ppvRank = ppvRank;
	}

	public int getRecordcount() {
		return recordcount;
	}

	public void setRecordcount(int recordcount) {
		this.recordcount = recordcount;
	}

	public ArrayList<Integer> getNeighborsList() {
		return neighborsList;
	}

	public void setNeighborsList(ArrayList<Integer> neighborsList) {
		this.neighborsList = neighborsList;
	}

	public ArrayList<Double> getModelProfile() {
		return modelProfile;
	}

	public void setModelProfile(ArrayList<Double> modelProfile) {
		this.modelProfile = modelProfile;
	}

	public ArrayList<Double> getModelProfileVariances() {
		return modelProfileVariances;
	}

	public void setModelProfileVariances(ArrayList<Double> modelProfileVariances) {
		this.modelProfileVariances = modelProfileVariances;
	}

	public ArrayList<String> getAssignates() {
		return assignates;
	}

	public void setAssignates(ArrayList<String> assignates) {
		this.assignates = assignates;
	}
	
	
	
}

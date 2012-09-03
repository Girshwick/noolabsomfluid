package org.NooLab.somfluid.clapp;

import java.util.ArrayList;

import org.NooLab.somfluid.core.nodes.MetaNodeIntf;






public interface SomAppNodeIntf extends MetaNodeIntf{

	public double getPpv();
	public void setPpv(double value);
	
	public void setNpv(double numVal) ; 
	public double getNpv();
	
	public int getPpvRank();
	public void setPpvRank(int value);
	
	public int getRecordcount();
	public void setRecordcount(int value) ;
	
	public ArrayList<Integer>  getNeighborsList();
	public void setNeighborsList(ArrayList<Integer> ilist);
	
	public ArrayList<Double>  getModelProfile();
	public void setModelProfile( ArrayList<Double> pvalues);
	
	public ArrayList<String> getAssignates();
	public void setAssignates(ArrayList<String> labels);
	
	public ArrayList<Double> getModelProfileVariances();
	public void setModelProfileVariances(ArrayList<Double> vvalues);
	
	
}

package org.NooLab.somfluid.data;

import java.io.Serializable;
import java.util.ArrayList;
 

public class Variable implements Serializable{
	
	private static final long serialVersionUID = 4793294807647874956L;

	private int index ; 

	private double minimum;
	private double maximum;
	private double weight;
	private String label;
	private boolean isTV=false;
	private boolean isID=false;
	private boolean Used=false;
	private boolean IDnotnormalized=false ;
	
	private int     mvCount;
	private double  median;
	private boolean isDerived=false;
	private long    derivationID = -1L;
	
	
	// such, the variable can move itself, and inform all other variables about changed indexes!
	ArrayList<Variable> parentCollection ;

	private boolean isTVcandidate;

	private boolean isIndexcandidate;
	
	// ========================================================================
	public Variable(){
		
	}
	// ========================================================================	
	
	public int getIndex(){
		return index  ;
	}
	public void setIndex( int ix){
		index = ix ;
	}
	
	public ArrayList<Variable> getParentCollection() {
		return parentCollection;
	}


	public void setParentCollection(ArrayList<Variable> parentCollection) {
		this.parentCollection = parentCollection;
	}
	
	
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	public double getMinimum() {
		return minimum;
	}
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	public double getMaximum() {
		return maximum;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public double getWeight() {
		return weight;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public void setTV(boolean isTV) {
		this.isTV = isTV;
	}
	public boolean isTVcandidate() {
		return isTVcandidate;
	}

	public boolean isIndexcandidate() {
		return isIndexcandidate;
	}

	public boolean isTV() {
		return isTV;
	}
	public void setTVcandidate(boolean flag ) {
		 
		isTVcandidate = flag;
	}

	public void setID(boolean isID) {
		this.isID = isID;
	}
	public boolean isID() {
		return isID;
	}
	public void setUsed(boolean used) {
		Used = used;
	}
	public boolean isUsed() {
		return Used;
	}


	public boolean getIDnotnormalized() {
		return IDnotnormalized;
	}


	public void setIDnotnormalized(boolean iDnotnormalized) {
		IDnotnormalized = iDnotnormalized;
	}


	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getMvCount() {
		return mvCount;
	}

	public void setMvCount(int mvCount) {
		this.mvCount = mvCount;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public boolean isDerived() {
		return isDerived;
	}

	public void setDerived(boolean isDerived) {
		this.isDerived = isDerived;
	}

	public long getDerivationID() {
		return derivationID;
	}

	public void setDerivationID(long derivationID) {
		this.derivationID = derivationID;
	}

	public void setIndexcandidate(boolean flag) {
		
		isIndexcandidate = flag ;
	}


}

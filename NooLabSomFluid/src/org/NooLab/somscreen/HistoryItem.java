package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexedDistances;



public class HistoryItem implements Serializable{

	private static final long serialVersionUID = 4906839929113092835L;
	
	int index = -1;
	int step  = -1;
	
	double score = 1000.0 ;
	
	int[] variableIndexes = new int[0] ;
	
	int truePositives=-1,trueNegatives=-1, falsePositives=-1, falseNegatives=-1 ;
	
	double tpRate,tnRate, fpRate, fnRate, ppv, npv, sensitivity, specificity ;
	double rocAuC, rocSTP;

	double risk;

	// for organizing the output
	transient IndexedDistances catalogFields ;

	int loopix;
	
	// ------------------------------------------------------------------------
	public HistoryItem(){
		
		 
	}
	// ------------------------------------------------------------------------

	public void setCatalogFields( IndexedDistances cixds){
		catalogFields = cixds;
	}
	
	public ArrayList<Object> getValueByCatalogPointers(){
		ArrayList<Object> objvalues = new ArrayList<Object>();
		
		return objvalues;
	}

	public Object getValueByCatalogPointer( int fieldindex){
		
		return null;
	}
	public Object getValueByCatalogPointer( String fieldLabel){
	
		return null;
	}
	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int[] getVariableIndexes() {
		return variableIndexes;
	}

	public void setVariableIndexes(int[] variableIndexes) {
		this.variableIndexes = variableIndexes;
	}

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}

	public int getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}

	public double getFpRate() {
		return fpRate;
	}

	public void setFpRate(double fpRate) {
		this.fpRate = fpRate;
	}

	public double getFnRate() {
		return fnRate;
	}

	public void setFnRate(double fnRate) {
		this.fnRate = fnRate;
	}

	public double getTnRate() {
		return tnRate;
	}



	public void setTnRate(double tnRate) {
		this.tnRate = tnRate;
	}



	public double getTpRate() {
		return tpRate;
	}

	public void setTpRate(double tpRate) {
		this.tpRate = tpRate;
	}



	public int getTrueNegatives() {
		return trueNegatives;
	}



	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}



	public double getRocAuC() {
		return rocAuC;
	}



	public void setRocAuC(double rocAuC) {
		this.rocAuC = rocAuC;
	}



	public double getRisk() {
		return risk;
	}



	public void setRisk(double risk) {
		this.risk = risk;
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

	public double getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}

	public double getSpecificity() {
		return specificity;
	}

	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	public double getRocSTP() {
		return rocSTP;
	}

	public void setRocSTP(double rocSTP) {
		this.rocSTP = rocSTP;
	}

	public int getLoopix() {
		return loopix;
	}

	public void setLoopix(int loopix) {
		this.loopix = loopix;
	}
	
	
	
}

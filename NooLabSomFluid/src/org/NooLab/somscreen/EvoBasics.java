package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;

public class EvoBasics implements Serializable{

	private static final long serialVersionUID = 5254385568805654924L;

	public static final int _QUANT_LOW = 1;
	public static final int _QUANT_TOP = 2;
	public static final int _QUANT_MED = 3;
	public static final int _QUANT_CTR = 5;

	
	
	
	ArrayList<Double> evolutionaryWeights = new ArrayList<Double>() ; 
	ArrayList<Integer> evolutionaryCounts = new ArrayList<Integer>() ; 
	
	ArrayList<String> knownVariables = new ArrayList<String>(); 
	
	EvoTasks evoTasks;

	int bestModelHistoryIndex;
	
	
	
	
	// ========================================================================
	public EvoBasics(){
		
	}
	private void initializeEvoTaskList() {
		
		evoTasks = new EvoTasks(knownVariables) ;
	}
	// ========================================================================

	
	public void incEvolutionaryCount(int index) {
		int ec = evolutionaryCounts.get(index) ;
		ec++;
		evolutionaryCounts.set(index, ec);
	}
	
	public void setSelectionparameters(){
		
	}
	
	public ArrayList<Integer> getQuantilByWeight(double quantile, double loWeightLimit, double hiWeightLimit) {
		
		return getQuantilByWeight( quantile, loWeightLimit, hiWeightLimit, false);
	}
	public ArrayList<Integer> getQuantilByWeight( double quantile, 
												  double loWeightLimit, double hiWeightLimit, 
												  boolean allowExtension) {
		
		IndexDistance ixd;
		IndexedDistances ixds = new IndexedDistances();
		ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
		double ew, minew=1.01, maxew=-0.1 ;
		
		
		if (loWeightLimit<0)loWeightLimit=0.0;
		if (hiWeightLimit>1.0)hiWeightLimit=1.0;
		if (hiWeightLimit < loWeightLimit)hiWeightLimit = loWeightLimit+0.000001;
		
		
		for (int i=0;i<this.evolutionaryWeights.size();i++){
			ew = evolutionaryWeights.get(i);
			if (minew<ew)minew=ew ;
			if (maxew>ew)maxew=ew ;
				
			if ((ew>=loWeightLimit) && (ew<= hiWeightLimit)){
				String varLabel = this.knownVariables.get(i);
				ixd = new IndexDistance(i,ew,varLabel); 
			}
		}
		
		if ((allowExtension) && (ixds.size()==0) && (maxew<loWeightLimit)){
			selectedIndexes = getQuantilByWeight( quantile, loWeightLimit, 1.0, false);	
		} else {

			ixds.sort(-1);

			for (int i = 0; i < ixds.size(); i++) {
				selectedIndexes.add(ixds.getItem(i).getIndex());
			}

		}
		return selectedIndexes;
	}
	
	public void getQuantilByWeight(double quantile ) {
		getQuantilByWeight(quantile, _QUANT_TOP);
	}

	public void getQuantilByWeight(double quantile , int mode) {
		
	}
	public void getQuantilByWeight(double quantile, double center ) {
		
	}

	
	
	public ArrayList<Double> getEvolutionaryWeights() {
		return evolutionaryWeights;
	}

	public void setEvolutionaryWeights(ArrayList<Double> evolutionaryWeights) {
		this.evolutionaryWeights = evolutionaryWeights;
	}

	public ArrayList<Integer> getEvolutionaryCounts() {
		return evolutionaryCounts;
	}

	public void setEvolutionaryCounts(ArrayList<Integer> evolutionaryCounts) {
		this.evolutionaryCounts = evolutionaryCounts;
	}

	public ArrayList<String> getKnownVariables() {
		return knownVariables;
	}

	public void setKnownVariables(ArrayList<String> variablesStr) {
		this.knownVariables = variablesStr;
		
		initializeEvoTaskList();
	}
	
	
	/**
	 * @return the bestModelHistoryIndex
	 */
	public int getBestModelHistoryIndex() {
		return bestModelHistoryIndex;
	}
	/**
	 * @param bestModelHistoryIndex the bestModelHistoryIndex to set
	 */
	public void setBestModelHistoryIndex(int bestModelHistoryIndex) {
		this.bestModelHistoryIndex = bestModelHistoryIndex;
	}
	public EvoTasks getEvoTasks() {
		return evoTasks;
	}
	 
	public void setEvoTasks( EvoTasks evt) {
		this.evoTasks = evt;
	}
	
	
	
}

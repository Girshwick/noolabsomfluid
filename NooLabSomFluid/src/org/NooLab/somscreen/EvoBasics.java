package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;

public class EvoBasics implements Serializable{

	private static final long serialVersionUID = 5254385568805654924L;
	
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

package org.NooLab.somfluid.app;

import java.util.ArrayList;

public class SomAppSomObject {

	int index = -1;
	
	
	// <project>
	// <general>
	String name = "";
	String originatingTask = "";
	
	// <context>
	boolean nested = false;
	
	int targetmode = -1;
	int somTypeId  = -1;
	int bagsCount  = 0 ;
	
	
	// <content>
	ArrayList<String> filter = new ArrayList<String>(); // not used 
	int noiseActive = -1;
	
	// <lattice>
	// description 
	int nodeCount = 0;
	int learnedFromRecordsCount = 0;
	
	// <nodes>
	SomAppNodes soappNodes; 
	
	
	// ========================================================================
	public SomAppSomObject( SomAppClassifier soappC, int ix) {
		 
		soappNodes = new SomAppNodes(this) ; 
		index = ix;
	}
	
	// ========================================================================	

	public void createNodes() {
		// TODO Auto-generated method stub
		
		for (int i=0;i<nodeCount;i++){
			SomAppNode node = new SomAppNode(i);
			soappNodes.nodes.add( node );
		}
		
	}
	
	public SomAppNode getNodeByIndex( int index ){
		
		return soappNodes.nodes.get(index) ;
	}
	
	// ... other "get by" ...
	
	
	
	
	// ----------------------------------------------------

	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getOriginatingTask() {
		return originatingTask;
	}


	public void setOriginatingTask(String originatingTask) {
		this.originatingTask = originatingTask;
	}


	public boolean isNested() {
		return nested;
	}


	public void setNested(boolean nested) {
		this.nested = nested;
	}


	public int getTargetmode() {
		return targetmode;
	}


	public void setTargetmode(int targetmode) {
		this.targetmode = targetmode;
	}


	public int getSomTypeId() {
		return somTypeId;
	}


	public void setSomTypeId(int somTypeId) {
		this.somTypeId = somTypeId;
	}


	public int getBagsCount() {
		return bagsCount;
	}


	public void setBagsCount(int bagsCount) {
		this.bagsCount = bagsCount;
	}


	public ArrayList<String> getFilter() {
		return filter;
	}


	public void setFilter(ArrayList<String> filter) {
		this.filter = filter;
	}


	public int getNoiseActive() {
		return noiseActive;
	}


	public void setNoiseActive(int noiseActive) {
		this.noiseActive = noiseActive;
	}


	public int getNodeCount() {
		return nodeCount;
	}


	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}


	public int getLearnedFromRecordsCount() {
		return learnedFromRecordsCount;
	}


	public void setLearnedFromRecordsCount(int learnedFromRecordsCount) {
		this.learnedFromRecordsCount = learnedFromRecordsCount;
	}


	public SomAppNodes getSoappNodes() {
		return soappNodes;
	}


	public void setSoappNodes(SomAppNodes soappNodes) {
		this.soappNodes = soappNodes;
	}
	

}

















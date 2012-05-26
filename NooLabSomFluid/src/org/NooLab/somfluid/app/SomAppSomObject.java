package org.NooLab.somfluid.app;

import java.util.ArrayList;

import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.utilities.datatypes.IndexedDistances;

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
	// SomAppNodes soappNodes;
	VirtualLattice soappNodes;

	// structures
	LatticePropertiesIntf latticeProps;
	
	SomAppClassifier soappClassifier;
	
	private ArrayList<Double> usageVector; 
	
	
	// ========================================================================
	public SomAppSomObject( SomAppClassifier soappC, int ix) {
		 
		soappClassifier = soappC;
	// 	soappNodes = new SomAppNodes(this) ;
		soappNodes = new VirtualLattice( soappClassifier, latticeProps,0 ) ;
								// soappClassifier as SomProcessIntf, 
		index = ix;
	}
	
	// ========================================================================	

	public void setUsageVector(ArrayList<Double> usagevector) {
		 
		// distribute around nodes ?
		usageVector = new ArrayList<Double>(usagevector);
	}

	public void createNodes() {
 		
		for (int i=0;i<nodeCount;i++){
			SomAppNode node = new SomAppNode(soappNodes,soappClassifier.somData, i);
			
			// soappNodes.getNodes().add( node );
		}
		
	}
	
	public IndexedDistances getWinnerNodes(ArrayList<Double> values) {
		IndexedDistances winnerNodes = new IndexedDistances();
	
	 
		
		
		return winnerNodes;
	}

	public MetaNode getNodeByIndex( int index ){
		
		return soappNodes.getNodes().get(index) ;
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


	public VirtualLattice getSoappNodes() {
		return soappNodes;
	}


	public void setSoappNodes(VirtualLattice nodeCollection) {
		soappNodes = nodeCollection;
	}

	public ArrayList<Double> getUsageVector() {
		return usageVector;
	}
	

}

















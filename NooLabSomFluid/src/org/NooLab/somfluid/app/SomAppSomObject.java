package org.NooLab.somfluid.app;

import java.util.ArrayList;

import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.engines.det.ProfileVectorMatcher;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;

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
	VirtualLattice soappNodes=null;

	// structures
	LatticePropertiesIntf latticeProps;
	
	SomAppClassifier soappClassifier;
	
	private ArrayList<Double> usageVector; 
	
	
	PrintLog out;
	
	// ========================================================================
	public SomAppSomObject( SomAppClassifier soappC, int ix) {
		 
		soappClassifier = soappC;
	// 	soappNodes = new SomAppNodes(this) ;
		soappNodes = new VirtualLattice( soappClassifier, latticeProps,0 ) ;
								// soappClassifier as SomProcessIntf, 
		index = ix;
		out = soappClassifier.out ;
	}
	
	// ========================================================================	

	public void setUsageVector(ArrayList<Double> usagevector) {
		 
		// distribute around nodes ?
		if (usagevector!=null){
			usageVector = new ArrayList<Double>(usagevector);
		}
	}

	public void _createNodes() {
 		
		ArrayList<String> varLabels = new ArrayList<String>();
		
		
		if( soappNodes==null){
			soappNodes = new VirtualLattice( soappClassifier, latticeProps,0 );
		}
		// if (soappNodes.)
		
		// soappNodes has to receive the definition of the profiles before defining the nodes
		// soappNodes.spreadVariableSettings();
		soappNodes.setAveragePhysicalDistance(10.0); // should be taken from loaded model (missing there!)
		
		for (int i=0;i<nodeCount;i++){
			SomAppNode node = new SomAppNode(soappNodes,soappClassifier.somData, i);
			
			// requires NodesInformer, not necessary for soapp ?
			// registerNodeinNodesInformer( node );
			
			ArrayList<Variable> vari = soappClassifier.somData.getVariableItems();

			// ProfileVectorIntf pv =
			
			node.getIntensionality().getProfileVector().setVariables( vari ) ;
			node.getExtensionality().getStatistics().setVariables(vari);

			
		} // -> to requested node count
		
	}
	
	// ... other "get by" ...
	
	
	
	
	// ----------------------------------------------------
	
	 

	public IndexedDistances getWinnerNodes(ArrayList<Double> profilevalues) {
		IndexedDistances winnerNodes = new IndexedDistances();
		IndexDistance ixd ;
		double distance;
		ProfileVectorMatcher bmuSearch;
		ArrayList<Integer> boundingIndexList = new ArrayList<Integer>();
		ArrayList<MetaNode> nodeCollection = new ArrayList<MetaNode>(); 

		// get the similarity for all nodes and put it to the ixds structure

		// ProfileVectorMatcher
		// ... = somLattice.getNode(srcNodeIndex).getIntensionality().getProfileVector().getValues();

		bmuSearch = new ProfileVectorMatcher( 0,out);
		bmuSearch.setNodeCollection( soappNodes.getNodes() ).setParameters(profilevalues, 10, boundingIndexList);
		
		bmuSearch.linkNodeCollection( soappNodes.getNodes());
		
		bmuSearch.createListOfMatchingUnits(1);
		
		winnerNodes.addAll( bmuSearch.getList(10) );
		
		// the list has already been sorted by bmuSearch such that the first item is the one with the smallest distance
		 
		 
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
		
		if (usageVector==null){
			usageVector = new  ArrayList<Double>(); // avoid null, change to empty
		}
		return usageVector;
	}
	

}

















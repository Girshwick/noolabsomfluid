package org.NooLab.somfluid.app;

import java.util.ArrayList;

import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.nodes.AbstractMetaNode;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;




/**
 * 
 * 
 * 
 * 
 *
 */
public class SomAppNode 
							extends 
										MetaNode 
							implements 
										MetaNodeIntf,
										SomAppNodeIntf{

	int index = -1;
	
	// <description>
	double ppv = -1.0 ;
	double npv = -1.0 ;
	
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
	public SomAppNode( VirtualLattice  nodeCollection , DataSourceIntf somData , int nodindex ){
		super(nodeCollection, somData) ;
		index = nodindex;
	}
	//-------------------------------------------------------------------------

	
	public SomAppNode(VirtualLattice soappNodes, SomDataObject somData) {
		super(soappNodes, somData);
	}


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

	public void setNpv(double numVal) {
		npv = numVal;
		
	}
	
	@Override
	public double getNpv() {
		return npv;
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
	
	// ========================================================================
	

	@Override
	public ArrayList<Integer> getExtensionRecordsIndexValues() {
		return getExtensionRecordsIndexValues(); 
	}
	
	@Override
	public int size() {

		return this.getExtensionality().getCount();
	}
	
	

	@Override
	public IntensionalitySurfaceIntf importIntensionalitySurface() {

		return getVirtualLattice().distributeIntensionalitySurface() ;
	}
	
	@Override
	public IntensionalitySurfaceIntf importIntensionalitySurface(long serialID) {
		 
		return getVirtualLattice().distributeIntensionalitySurface(serialID) ;
	}
	
	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics() {		
		return getVirtualLattice().distributeExtensionalityDynamics();
	}
	
	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics(long serialID) {

		return getVirtualLattice().distributeExtensionalityDynamics(serialID);
	}
	
	@Override
	public SimilarityIntf importSimilarityConcepts() {
		return getVirtualLattice().distributeSimilarityConcept() ;
	}

	@Override
	public SimilarityIntf importSimilarityConcepts(long serialID) {
		return getVirtualLattice().distributeSimilarityConcept(serialID) ;
	}


	
	
	
	@Override
	public <T> T getInfoFromNode( Class<T> theClass, int infoID) throws IllegalAccessException,
																		InstantiationException {
		 
		return null;
	}
	
	@Override
	public void setContentSensitiveInfluence(boolean flag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void adjustProfile( ArrayList<Double> datarecord, int nodeIndex, double learningrate,
							   double influence, double sizeFactor, int i) {
	 
		
	}
	
	@Override
	public void insertDataAndAdjust(ArrayList<Double> dataNewRecord, int recordIndexInTable, int ithWinner,
			double learningrate, int fillingLimitForMeanStyle) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void cleanInitializationByUsageVector(ArrayList<Double> usagevector) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeDataAndAdjust(int recordIndexInTable, double learningrate) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ArrayList<Integer> exportDataFromNode(int countOfRecords, int quality, boolean removeExports) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<Integer> exportDataFromNode(double smallestPortion, double largestPortion, int quality,
			boolean removeExports) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void importDataByIndex(ArrayList<Integer> recordIndexes) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ArrayList<Double> getTargetVariableValues() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity(long serialID) {
		// TODO Auto-generated method stub
		return null;
	}


	
	// ========================================================================
	
	
	
	
}

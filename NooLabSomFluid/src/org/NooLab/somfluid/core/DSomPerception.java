package org.NooLab.somfluid.core;

import java.util.ArrayList;

 
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.DSomDataPerceptionAbstract;
import org.NooLab.somfluid.core.engines.det.results.ValidationSet;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;



public class DSomPerception extends 
									  DSomDataPerceptionAbstract{

	// available through parent class:
	// SomDataObject  somData,	VirtualLattice somLattice;
	
	ArrayList<Integer> boundingIndexList = new ArrayList<Integer>();
	
	ArrayList<IndexDistanceIntf> bmus ;
	
	int bmuListSize = 1;
	
	// ========================================================================
	public DSomPerception( DSom dsom ) {
		super(dsom);
		 
	}
	// ========================================================================
	
	
	public void classifyRecord( int dataRowIndex, ArrayList<Double> profilevalues ){
		if (bmus!=null)bmus.clear() ;
		
		int mppLevel = sfProperties.getMultiProcessingLevel() ;
		bmus = getBestMatchingNodes( dataRowIndex,
								     profilevalues, 
								     bmuListSize, 
									 boundingIndexList, mppLevel) ;
		 
	}


	public ArrayList<IndexDistanceIntf> getBmus() {
		return bmus;
	}


	public void setBmuListSize(int n) {
		bmuListSize = n;
	}
	
	
	
}

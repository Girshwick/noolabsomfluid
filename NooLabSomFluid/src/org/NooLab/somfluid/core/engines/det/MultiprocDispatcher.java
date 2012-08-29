package org.NooLab.somfluid.core.engines.det;

import java.util.SortedMap;
import java.util.TreeMap;

import org.NooLab.somfluid.lattice.VirtualLattice;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;


/**
 * 
 * this class maintains dispatching lists
 * - which node of a SOM is processed on which processor core
 * - which metrices are checked in evo-devo on which machine 
 * 
 * for each processor there is a dedicated class 
 * 
 *
 */
public class MultiprocDispatcher {

	DSom dSom;
	private VirtualLattice somLattice;
	
	SortedMap<Integer,Integer>  NodeCoreMap = new TreeMap<Integer,Integer>();
	
	// ========================================================================
	public MultiprocDispatcher(DSom dsom) {
 
		dSom = dsom;
		
		somLattice = dSom.getSomLattice() ;
		
	}
	// ========================================================================
	
	
	
}

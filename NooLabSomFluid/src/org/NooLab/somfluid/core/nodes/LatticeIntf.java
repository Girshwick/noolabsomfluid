package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;

import org.NooLab.somfluid.components.IndexedDistances;

public interface LatticeIntf {

	
	public ArrayList<IndexedDistances> getNeighborhoodNodes( int index );
}

package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;

import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.components.data.IndexDistanceIntf;
import org.NooLab.somfluid.components.IndexedDistances;

public interface LatticeIntf {

	
	public ArrayList<IndexDistanceIntf> getNeighborhoodNodes( int index , int nodeCount );
}

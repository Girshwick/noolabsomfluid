package org.NooLab.somfluid.core.engines.det;

import org.NooLab.utilities.logging.PrintLog;

public class DSomCore {

	
	PrintLog out;
	
	public DSomCore(DSom dSom) {
		 
		// ... dSom.somData;
		out = dSom.out;
		
		out.print(2, "requesting neighborhood for <11> from particle field via Som-Lattice");
		dSom.somLattice.getNeighborhoodNodes(11) ; out.delay(200);
		dSom.somLattice.getNeighborhoodNodes(15) ;
	}

	
	
}

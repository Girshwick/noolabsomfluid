package org.NooLab.somfluid.astor.query;

import org.NooLab.somfluid.components.SomQueryTargetIntf;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.nodes.LatticeIntf;


/**
 * 
 * TODO: LATER (with stabilized interface) this whole package has to be moved
 *       into its own project!!!
 * 
 * @author kwa
 *
 */
public class SomQueryFactory {

	/** the som... actually, SomQuery does not know whether it is a lattice , 
	 *  a bundle of them, sheafs, a fluid or a gas...
	 */
	SomQueryTargetIntf somLattice ;
	 
	SomQuery somQuery;
	
	
	
	
	
	// ========================================================================
	public SomQueryFactory(){
		
	}
	// ========================================================================

	public static SomQueryIntf getInstance( SomQueryTargetIntf somlattice) {
	
		SomQuery somQuery;
		SomQueryFactory sqf = new SomQueryFactory();
		
		sqf.prepare(somlattice);
		
		// overlapping contexts...
		somQuery = sqf.getSomQuery() ;
		
		return (SomQueryIntf) somQuery;
	}
	
	private SomQuery getSomQuery() {
		return somQuery;
	}
	
	private void prepare(SomQueryTargetIntf somlattice) {

		somLattice = somlattice;
		
		somQuery = new SomQuery( this, somlattice );
		
	}

	
	
	
	
	
}

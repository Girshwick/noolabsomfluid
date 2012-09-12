package org.NooLab.somfluid.lattice.grow;


/**
 * 
 * organizes growth by attachment
 * 
 * new nodes are not "empty" or "ignorant", they get instantiated by the information 
 * of the nodes nearby, followed by a re-location of some of the data
 * 
 * In a torus, it is less an attachment than it is an insertion.
 * 
 *
 * Note, that in the fixed grid, always 5 columns get inserted/attached.
 * 
 * This takes into account 2 issues:
 *  - the possibility of a local densification by splitting nodes, without much efforts
 *  - still keeping grid paths open such that nodes can move around without collision 
 *  
 * Basically, this separates the "logical perspective" from a "physical substrate", much like
 * the fully fluid particle Grid, but at the same time being faster.
 * 
 * Note that the nodes themselves never know anything about their coordinates!!!
 *
 *
 * This class also organizes the pullulating, that s the local growth out of the sheet into 
 * the 3rd dimension, yielding a fractal dimensionality for the grid
 * 
 * This could be thought of as a local, embedded prefetch
 *
 */
public class LatticeGrowth {

	
	// ========================================================================
	public LatticeGrowth(){
		
	}
	// ========================================================================	
	
	
	
}

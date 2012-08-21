package org.NooLab.field.interfaces;

import org.NooLab.field.repulsive.intf.ParticleDataHandlingIntf;

public interface FieldSelectionIntf {
	
	

	public int getSelectionSize() ;
	
	/** setting the number of particles included in a selection, only if <140 it will follow 
	 * exactly the hexagonal pattern */
	public void setSelectionSize(int n) ;
	
	/** 
	 * increase/decrease of the area that will be selected ;
	 * modes: 1= by "layers" (min=1), (not implemented: 2=by physical distance in % (min= avg distance between particles))
	 */
	public void selectionSizeDecrease(int mode, double amount);
	public void selectionSizeIncrease(int mode, double amount);


	
	
	/** adds a particle at a random location (but the random mode can be set to uniform, or gaussian) */
	public int addParticles( int count);
	
	/** adds one particle at a particular location */
	public int addParticles( int x, int y );
	
	/** adds a bunch of particles at the defined particular locations */
	public int addParticles( int x[], int[] y );
	
	
	/** splits a selected (by "index") particle into 2 */
	public int splitParticle( int index, ParticleDataHandlingIntf pdataHandler );
	
	/** merges at most 7 particles in a single step that are close to each other, in other words, where the
	 *  the distance is less than 1.3 * average distance; </br>
	 *  mergeTargetIndex is the index of the particle "into" which the others will be
	 *  merged (this is relevant for distance criterion) 
	 */
	public String mergeParticles( int mergeTargetIndex, int[] indexes) ;
	public String mergeParticles( int mergeTargetIndex, int swallowedIndex) ;
	
	/** remove the particle with index "index"  */
	public void deleteParticle( int index );

	/**  type=1 -> absolute, type=2 -> relative; movement will be clipped (without reflection) 
	 *   at the borders of the area if there are borders (e.g. in toroidal topology there are no borders) */
	public void moveParticle( int particleIndex, int type , double xParam, double yParam );

	public int getNumberOfParticles() ;
	

	// -------------------------------------

	/** getting the particle which is closest to the provided coordinates, results return via event */
	public String selectParticleAt(int xpos, int ypos, boolean autoselect);

	
	/** getting a list of index values that point to the particles which build a circular surround located
	 * around the particle closest to the provided coordinates;
	 * alternatively, if the index is known, the index can be used directly (which saves a bit of efforts) 
	 * 
	 *  selectMode=1 : selecting closest "surroundN" items;
	 *  selectMode=2 : selecting closest items within a radius of surroundN   
	 * */
	public String getSurround( int xpos, int ypos , int selectMode, int surroundN, boolean autoselect) ;
	
	/** in SOM, usually has identified a node as a winner and wants to know the list of nodes in its 
	 *  vicinity given a particular radius, while the maximum of the radius remains stable for a certain period of time */
	public String getSurround( int index , int selectMode, int surroundN, boolean autoselect);


	
	

}

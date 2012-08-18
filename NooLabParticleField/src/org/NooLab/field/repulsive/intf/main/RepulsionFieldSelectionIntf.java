package org.NooLab.field.repulsive.intf.main;

import java.util.ArrayList;

import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.field.repulsive.intf.particles.GraphParticlesIntf;



public interface RepulsionFieldSelectionIntf {
	
	

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

	
	public GraphParticlesIntf getGraphParticles();

	/**
	 * calculating the grid perspective , which is a 
	 * regular and rectangular grid in absolute coordinates;
	 * such it is much more easier to retrieve particles;
	 * 
	 * if the grid perspective has been calculated, or is available it will be
	 * used with priority against the particle-based selection procedures
	 * 
	 * Else, a client of the RepulsionField needs NOT not to store the particles;
	 * 
	 * the grid perspective does NOT influence any of the behaviors of the particles,
	 * it is always calculated after freezing the layout, in parallel to 
	 * calculating the SurroundBuffer 
	 * 
	 * the grid perspectives resolution is about a 1/3 of the minimal distance between particles
	 */
	public void provideGridPerspective() ;
	
	// -------------------------------------

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
	
	
	/** deleting the pointers to all data objects  */
	public void clearData(int index);		
	
	/** transfers the pointers (they are of type "long"! ) from one particle to another */
	public void transferData( int fromParticleIndex, int toParticleIndex);
	
	/**
	 * in case of a SOM, usually 1 particle is just 1 node; actually, the particle does not know anything
	 * to what kind of entities the data object index refers to (it may point to a list of heterogenous objects)
	 * yet, it is possible here to collect several nodes in a single particle 
	 * 
	 * @param particleIndex
	 * @param dataPointer
	 */
	public void insertDataPointer( int particleIndex, long dataPointer);

	public void removeDataPointer( int particleIndex, long dataPointer);
	
	// -------------------------------------

	/** getting the particle which is closest to the provided coordinates, results return via event */
	public String selectParticleAt(int xpos, int ypos, boolean autoselect);

	
	/** getting a list of index values that point to the particles which build a circular surround located
	 * around the particle closest to the provided coordinates;
	 * alternatively, if the index is known, the index can be used directly (which saves a bit of efforts) */
	public String getSurround( int xpos, int ypos , int selectMode, boolean autoselect) ;
	
	/** in SOM, usually has identified a node as a winner and wants to know the list of nodes in its 
	 *  vicinity given a particular radius, while the maximum of the radius remains stable for a certain period of time */
	public String getSurround( int index , int selectMode, boolean autoselect);

	
	/**   */
	public String getParticlesOfFiguratedSet( int figure, Object indexes, double thickness, double endPointRatio, boolean autoselect );
	
	public String getParticlesOfFiguratedSet( int figure, ArrayList<PointXY> points, double thickness, double endPointRatio, boolean autoselect );


	
}

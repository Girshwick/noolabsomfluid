package org.NooLab.field.repulsive.intf.main;

import java.util.ArrayList;

import org.NooLab.field.interfaces.FieldSelectionIntf;
import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.field.repulsive.intf.particles.GraphParticlesIntf;



public interface RepulsionFieldSelectionIntf extends FieldSelectionIntf{
	
	

	
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


	
	/**   */
	public String getParticlesOfFiguratedSet( int figure, Object indexes, double thickness, double endPointRatio, boolean autoselect );
	
	public String getParticlesOfFiguratedSet( int figure, ArrayList<PointXY> points, double thickness, double endPointRatio, boolean autoselect );


	
}

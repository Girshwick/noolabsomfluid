package org.NooLab.repulsive.intf.main;

 

import java.util.ArrayList;

import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.data.AreaPoint;
import org.NooLab.repulsive.components.data.PointXY;
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;


/**
 * 
 * see {@link RepulsionFieldCore}
 * 
 *
 */
public interface RepulsionFieldIntf extends RepulsionFieldBasicIntf{
	
	public static final int __SELECTION_FIGURE_CONVEXHULL = 1;
	public static final int __SELECTION_FIGURE_MST = 5;
	 
	
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

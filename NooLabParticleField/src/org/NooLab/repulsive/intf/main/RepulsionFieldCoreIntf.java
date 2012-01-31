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
public interface RepulsionFieldCoreIntf extends RepulsionFieldBasicIntf{
	
	  
	 
	// -------------------------------------

	/** getting a list of index values that point to the particles which build a circular surround located
	 * around the particle closest to the provided coordinates;
	 * alternatively, if the index is known, the index can be used directly (which saves a bit of efforts) */
	public String getSurround( int xpos, int ypos , int selectMode, boolean autoselect) ;
	
	/** in SOM, usually has identified a node as a winner and wants to know the list of nodes in its 
	 *  vicinity given a particular radius, while the maximum of the radius remains stable for a certain period of time */
	public String getSurround( int index , int selectMode, boolean autoselect);

	/** getting the particle which is closest to the provided coordinates, results return via event */
	public String selectParticleAt(int xpos, int ypos, boolean autoselect);
	
	
	// -------------------------------------
	
	/**
	 * calculates the Minimal Spannning Tree as an array of lines (type LineXY)
	 */
	public String getMinimumSpanningTree( ArrayList<PointXY> points, boolean autoselect) ; 
	
	/** considers the area of an ellipse where the thickness is the length of the minor axis and the distance 
	 *  between two points in the MST forms the major axis;
	 *  endpointRatio=0 -> points are on the hull of the eclipse (focal points are UNequal to support points);
	 *  endpointRatio=1 -> points are interpreted as focal points
	 */
	public String getParticlesAroundMST( int[] indexes, double thickness, double endPointRatio, boolean autoselect) ;

	public String getParticlesAroundMST( ArrayList<PointXY> points, double thickness, double endPointRatio, boolean autoselect) ;

	public String getParticlesWithinConvexHull( ArrayList<PointXY> points, double thickness , int topology, boolean autoselect) ;

	public String getParticlesWithinConvexHull( int[] indexes, double thickness , int topology, boolean autoselect) ;
	 
	
	
	
	
}

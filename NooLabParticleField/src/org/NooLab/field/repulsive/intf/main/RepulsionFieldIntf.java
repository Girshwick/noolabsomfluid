package org.NooLab.field.repulsive.intf.main;

 

import java.util.ArrayList;

import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.data.AreaPoint;
import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;


/**
 * 
 * see {@link RepulsionFieldCore}
 * 
 *
 */
public interface RepulsionFieldIntf extends RepulsionFieldSelectionIntf, RepulsionFieldBasicIntf{
	
	public static final int __SELECTION_FIGURE_CONVEXHULL = 1;
	public static final int __SELECTION_FIGURE_MST = 5;
	 
 
	

	public void setFieldIsRandom(boolean flag);



	/** call without parameters deactivates the box-mode for selections */
	public void setConstrainingBox();
	
	/** deactivates a box even for the next setting, or activates it if one has been defined */
	public void setConstrainingBox(boolean flag);


	/** defines the box */
	public void setConstrainingBox(double x1, int x2, double y1, double y2);



	public void setSelectionShape( int shapeId, double param1, double param2 );
    				   // in case of _ELLIPSE : double angle,  double minorAxisScale
                       // in case of _RECT    : double x-side, double y-side
					   // in case of _STRING  : double length, disregarded

	public void setSelectionShape(int shapeId);



	public void close();


	
	
}

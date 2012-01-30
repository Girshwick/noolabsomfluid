package org.NooLab.repulsive.intf.particles;

import org.NooLab.repulsive.particles.PColor;



public interface GraphParticleIntf {
 
	public void setMainColor(PColor mainColor) ;
	
	public PColor getDisplayedColor();
	
	public double getX() ;

	public double getY() ;

	public double getZ() ;

	public double getRadius() ;

	public int getBehavior() ;

	public int getCharge() ;

	public void unselect();


	
	
}

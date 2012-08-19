package org.NooLab.field.repulsive.intf.particles;

import org.NooLab.field.repulsive.particles.PColor;



public interface GraphParticleIntf {
 
	public void setMainColor(PColor mainColor) ;
	
	public PColor getDisplayedColor();
	
	public void setDisplayedColor( int[] displayedColors);
	
	public double getX() ;

	public double getY() ;

	public double getZ() ;

	public double getRadius() ;

	public int getBehavior() ;

	public int getCharge() ;

	public void unselect();


	
	
}

package org.NooLab.field.interfaces;

import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;



public interface PhysicalGridFieldIntf {

	int getNumberOfParticles();

	void close();



	public int getWidth() ;

	public void setWidth(int width) ;

	public int getHeight() ;

	public void setHeight(int height) ;

	public boolean isInitComplete() ;

	public void setInitComplete(boolean initComplete) ;

	public ParticlesIntf getParticles();

	double getAverageDistanceBetweenParticles();

	void setSelectionSize(int surroundN);

	String getSurround(int particleindex, int i, boolean b);

	boolean getInitComplete();
	
	
	
	
	
}

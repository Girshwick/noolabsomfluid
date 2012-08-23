package org.NooLab.field.interfaces;

import org.NooLab.field.repulsive.components.data.SurroundResults;


// import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;



public interface PhysicalGridFieldIntf {

	int getNumberOfParticles();

	void close();



	

	public int getWidth() ;

	public void setWidth(int width) ;

	public int getHeight() ;

	public void setHeight(int height) ;

	public boolean isInitComplete() ;

	public void setInitComplete(boolean initComplete) ;

	// RepFieldParticlesIntf -> instantiation dependent on parameter
	public Object getParticles();

	double getAverageDistanceBetweenParticles();

	void setSelectionSize(int surroundN);

	/** int index, int selectMode, boolean autoselect */
	String getSurround(int particleindex, int selectMode, int surroundN, boolean autoselect);

	boolean getInitComplete();

	 
	public void registerEventMessaging( Object eventSinkObj ) ;
	
	
	
	
}

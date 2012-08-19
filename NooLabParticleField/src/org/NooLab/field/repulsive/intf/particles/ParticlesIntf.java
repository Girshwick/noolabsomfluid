package org.NooLab.field.repulsive.intf.particles;

import org.NooLab.field.repulsive.particles.Particle;


public interface ParticlesIntf extends ParticlesBaseIntf{

	
	 
	public Particle get(int index) ;


	public double getDensity() ;

	public double getAverageDistance() ;
	
}

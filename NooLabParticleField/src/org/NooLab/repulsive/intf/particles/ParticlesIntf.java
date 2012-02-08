package org.NooLab.repulsive.intf.particles;

import org.NooLab.repulsive.particles.Particle;


public interface ParticlesIntf extends ParticlesBaseIntf{

	
	 
	public Particle get(int index) ;


	public double getDensity() ;

	public double getAverageDistance() ;
	
}

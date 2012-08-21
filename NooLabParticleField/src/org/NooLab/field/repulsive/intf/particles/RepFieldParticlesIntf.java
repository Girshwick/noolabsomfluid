package org.NooLab.field.repulsive.intf.particles;

import org.NooLab.field.FieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;


public interface RepFieldParticlesIntf extends FieldParticlesIntf {

	
	 
	public RepulsionFieldParticle get(int index) ;


	public double getDensity() ;

	public double getAverageDistance() ;
	
}

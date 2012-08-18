package org.NooLab.field.repulsive.intf.particles;

import org.NooLab.field.repulsive.particles.Particle;


public interface ParticlesBaseIntf {
	
	public int size();

	public void add(Particle p);
 
	public void remove(int index);
	
}

package org.NooLab.repulsive.intf.particles;

import org.NooLab.repulsive.particles.Particle;


public interface ParticlesBaseIntf {
	
	public int size();

	public void add(Particle p);
 
	public void remove(int index);
	
}

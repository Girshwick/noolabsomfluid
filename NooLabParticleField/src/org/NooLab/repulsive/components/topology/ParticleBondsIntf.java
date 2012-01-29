package org.NooLab.repulsive.components.topology;


/**
 * 
 * can be used to define adhesion or selective communication
 */
public interface ParticleBondsIntf {
	
	public void setMaxDistanceToParticleIndex( int index, double maxDistance);
	public void setMinDistanceToParticleIndex( int index, double minDistance);
	public void setMinDistanceToAnyParticle( double minDistance);
	
	public double getRepulsion() ;
	public void setRepulsion(double repulsion) ;
	
}

package org.NooLab.repulsive.components.topo;

import java.util.ArrayList;

import org.NooLab.repulsive.particles.Particle;




public class ParticleLinkages {
	
	ArrayList<ParticleLinkage> particleLinkages = new ArrayList<ParticleLinkage>();
	Particle parentParticle ;
	
	public ParticleLinkages( Particle p){
		parentParticle = p;
		
	}

	public ArrayList<ParticleLinkage> getParticleLinkages() {
		return particleLinkages;
	}
	
}

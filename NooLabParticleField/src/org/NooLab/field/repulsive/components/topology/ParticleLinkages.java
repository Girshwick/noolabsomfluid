package org.NooLab.field.repulsive.components.topology;

import java.util.ArrayList;

import org.NooLab.field.FieldParticleIntf;
// import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;




public class ParticleLinkages {
	
	ArrayList<ParticleLinkage> particleLinkages = new ArrayList<ParticleLinkage>();
	FieldParticleIntf parentParticle ;
	
	public ParticleLinkages( FieldParticleIntf p){
		parentParticle = p;
		
	}

	public ArrayList<ParticleLinkage> getParticleLinkages() {
		return particleLinkages;
	}
	
}

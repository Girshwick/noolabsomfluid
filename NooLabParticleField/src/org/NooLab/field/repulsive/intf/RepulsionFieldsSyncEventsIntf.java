package org.NooLab.field.repulsive.intf;

import org.NooLab.field.repulsive.particles.Particle;



public interface RepulsionFieldsSyncEventsIntf {

	public void onAddingParticle( Object observable, Particle p, int index );
	
	public void onDeletingParticle( Object observable, Particle p, int index );
}

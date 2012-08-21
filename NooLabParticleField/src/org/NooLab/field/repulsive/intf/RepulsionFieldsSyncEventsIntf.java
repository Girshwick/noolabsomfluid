package org.NooLab.field.repulsive.intf;

import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;



public interface RepulsionFieldsSyncEventsIntf {

	public void onAddingParticle( Object observable, RepulsionFieldParticle p, int index );
	
	public void onDeletingParticle( Object observable, RepulsionFieldParticle p, int index );
}

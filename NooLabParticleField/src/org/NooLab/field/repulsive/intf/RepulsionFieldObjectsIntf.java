package org.NooLab.field.repulsive.intf;



import org.NooLab.field.repulsive.components.Neighborhood;
import org.NooLab.field.repulsive.components.SurroundBuffers;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldCoreIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;



public interface RepulsionFieldObjectsIntf {

	public Neighborhood getNeighborhood();
	
	public SurroundBuffers getSurroundBuffers();
	
	public RepulsionFieldCoreIntf getCoreInstance();
	
	public RepulsionFieldIntf getFacadeInstance();
	 
}

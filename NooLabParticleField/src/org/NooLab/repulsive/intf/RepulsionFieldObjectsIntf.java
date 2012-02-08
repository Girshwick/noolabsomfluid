package org.NooLab.repulsive.intf;



import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.SurroundBuffers;
import org.NooLab.repulsive.intf.main.RepulsionFieldCoreIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;



public interface RepulsionFieldObjectsIntf {

	public Neighborhood getNeighborhood();
	
	public SurroundBuffers getSurroundBuffers();
	
	public RepulsionFieldCoreIntf getCoreInstance();
	
	public RepulsionFieldIntf getFacadeInstance();
	 
}

package org.NooLab.somfluid.core.engines.det;

import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.data.ModelingSettings;
import org.NooLab.utilities.logging.PrintLog;








/**
 * 
 * This class organizes as a container for a SOM that is suitable for
 * target oriented modeling;
 * 
 * it uses table data and finds a repesentation for it that can be used
 * as/for classification.
 * 
 * a SOM is made from a collection of nodes, each node is described by a
 * weight-vector, and each node contains pointers to records, i.e. it does
 * NOT contain real data, only pointers to them, which are simply the 
 * record ID. This record ID points to the data table
 * 
 * Most important, the nodes are not just simply passive extensional containers as
 * in standard SOM implementation.
 * In contrast Nodes in the SomFluid are MetaNodes. In the simplest case, they behave 
 * as passive extensional nodes; yet they are better conceived as an "arrow" in
 * the sense of category theory, which provides a lot more flexibility and extensibility,
 * e.g. for branching, nesting etc. = growing and differentiation 
 * 
 */
public class DSom implements DSomIntf{

	
	DSomProperties dsProperties;
	
	ModelingSettings  modelingSettings ;
	
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	
	SomDataObject somData;
	VirtualLattice somLattice;

	// SomFluidTask 
	SomFluidMonoTaskIntf somTask; // is of monoflavor here
	
	DSomCore dSomCore;
	
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public DSom( SomFluidFactory factory ,SomDataObject sdo, VirtualLattice somlattice, SomFluidTask sfTask ) {
		 
		sfFactory = factory;	
		sfProperties = factory.getSfProperties();
		somData = sdo;
		somLattice = somlattice ;
		out = sdo.getOut() ;
		
		
		modelingSettings = sfProperties.getModelingSettings() ;
		
		String activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ;
	}
	

	private void init(){
		
	}
	// ------------------------------------------------------------------------	

	public void performTargetedModeling() {
		 
		dSomCore = new DSomCore(this) ;
	}
	
	
	 
	
	
}

/*
  
  	
		som = new DSomCore(); 
		
		// get object reference to settings object 
		som.setModelingSettings( modelingSettings ) ; 			
		
		// some helper objects, and the facultative reference to the PApplet 
		som.setGeneralSettings( generalSettings ) ; 			

		
		
		// provide reference to data object
		som.setSomDataObject(somDOB);
		
		
		
		// starting the calculations
		som.executeSOM() ;
		

  
 
 */

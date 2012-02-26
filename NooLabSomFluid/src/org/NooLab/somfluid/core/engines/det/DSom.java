package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;
import java.util.Random;


import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.results.SomTargetResults;
import org.NooLab.somfluid.core.engines.det.results.SomValidation;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.logging.PrintLog;

 

/**
 * 
 * TODO: we need a SampleOrganizer class (SomBag?) before this dsom, in order
 *       to be able to apply hard cuts for sampling data;
 *       such the DSom would see only a part of the world, without realizing
 *       that it is only a part of it
 *       
 *        
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
	
	SomFluid somFluidParent;
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	
	SomDataObject somData;
	VirtualLattice somLattice;

	SomFluidMonoTaskIntf somTask; // is of mono-flavor here
	
	DSomCore dSomCore;

	public BmuBuffer bmuBuffer;
	
	// this gets assigned in DSomCore, sine we also  need the actual record sample 
	// (besides the total number of records))
	SomTargetResults somResults;
	
	
	String activeTvLabel ;
	
	
	PrintLog out;

	public boolean loweredPriority = false ;

	Random random;

	
	// ------------------------------------------------------------------------
	public DSom( SomFluid sfParent , SomDataObject sdo, VirtualLattice somlattice, SomFluidTask sfTask ) {
		 
		somFluidParent = sfParent;
		//sfFactory = factory;	
		sfProperties = somFluidParent.getSfProperties();
		somData = sdo;
		somLattice = somlattice ;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		
		random = new Random();
		random.setSeed(3579) ;
		
		
		bmuBuffer = new BmuBuffer(this, somData.getRecordCount() ) ;
		
		activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ;
		
		out = sdo.getOut() ;
		
	}
	

	private void init(){
		
	}
	// ------------------------------------------------------------------------	

	public void performTargetedModeling() {
		 
		dSomCore = new DSomCore(this) ;
		 
		// will run the som in a dedicated thread
		dSomCore.perform() ;
		 
	}



	public int getTargetVariableColumn() {
		// dependent on modelingSettings
		
		return 0;
	}


	public boolean getUserbreak() {
		// will be set via Observer or callback
		return false;
	}

	/** size of the Lattice as count of nodes  */
	public int getSize() {

		return somLattice.size();
	}


	public Random getRandom() {
		return random;
	}


	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}


	public SomFluid getSomFluidParent() {
		return somFluidParent;
	}


	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}


	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}


	public SomDataObject getSomData() {
		return somData;
	}


	public VirtualLattice getSomLattice() {
		return somLattice;
	}


	public PrintLog getOut() {
		return out;
	}

 
	
	
	 
	
	
}

 

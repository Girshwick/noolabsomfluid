package org.NooLab.somfluid.astor;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;



/**
 * 
 * This take the same role as the ModelOptimizer
 * 
 * the idea is to keep most of the infrastructure, just the loops provided by
 * ModelOptimizer are abandoned, do not take place.
 * 
 * Another issue that is important right from the beginning is persistence;
 * 
 * Data handling is very different to ModelOptimizer
 * 
 * If the count of nodes is beyond a certain threshold, say 150 or so, the
 * search for the best matching unit BMU will be organized completely different
 * In this case, 5..8..10 sustained processes are created, which run a queue.
 * The nodes are assigned in a fixed manner ( needs update in case of growth)
 * A particular record then is provided to each of these processes, and each of them
 * creates its own list of BMU, which then are matched  
 *  
 * A second issue is the missing target variable.
 * The validation is not based on TV. 
 *  
 * 
 * modeling needs internal statistics, such that variance is controlled 
 * additionally, we use variance of variances as a target target variable for intermittent optimization
 * 
 * the focus here is on incremental learning, that is, a proper update service
 * primary data source is a database
 * 
 * 
 * 
 * 
 * also, a second version will be made available for further processing, which is a "crystallization"
 * based upon adhoc multi-criteria optimization:
 *    given an initial SOM, a threshold is chosen dynamically, such that 
 *    - the distance between clusters is in a certain range
 *    - clusters comprise at least 5(+) nodes
 *    - the contrast (variance of diff) between clusters and valleys is larger than those between nodes 
 * 
 * This class is just an app-like wrapper, organizing references, processes and classes
 * 
 * 
 * 
 */
public class SomAssociativeStorage implements SomProcessIntf {

	SomFluid somFluid ;
	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
	
	// using observer in its own thread, is part of SomDataObj
	SomDataStreamer somDataStreamer ; 
	SomDataObject somDataObj;
		
	
	SomProcessIntf somProcess;
	
	SomTransformer somTransformer;
	
	SomFluidProperties sfProperties ;
	
	DSom dSom ;
	
	 
	
	
	PrintLog out = new PrintLog(2,true);
	
	// ========================================================================
	public SomAssociativeStorage( SomFluid somfluid, SomFluidFactory factory,
								  SomFluidProperties properties) {
		
		somFluid = somfluid ;
		sfFactory = factory ;
		sfProperties = properties;
		
		prepareAstor() ;
	}
	// ========================================================================
	
	// care for data connection, incoming data link : we are learning incrementally !!
	public void prepareAstor(){
		
		// properties contains DB properties
		somDataObj = SomDataObject.openSomData(sfProperties);
		// now activate listener, router and receiver
		
		somDataObj.establishDataLinkage();
	}
	
	public void perform(){
		// this starts the process
		// no direct call here!! but see:
		SomTargetedModeling stm ;		
		
		// dSom.getSomData should deliver an interface to the object, not the object itself...
		// somlattice.setSomData(dSom.getSomData()) ;
	
		try{
			

			VirtualLattice astorSomLattice = new VirtualLattice( this, null, -5 );
			
			// somDataObj here does not provide data, but only a reference to the streaming source
			// yet, in the beginning there have to be at least 100 records in order to
			// initialize the SomTransformer
			dSom = new DSom( this, somDataObj, astorSomLattice, sfTask );
			
			dSom.performAstor();
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public SomDataObject getSomDataObject() {
		
		return null;
	}
	@Override
	public String getNeighborhoodNodes(int index, int surroundN) {
		
		return null;
	}
	@Override
	public SomFluidAppGeneralPropertiesIntf getSfProperties() {
		
		return null;
	}
	@Override
	public LatticePropertiesIntf getLatticeProperties() {
		
		return null;
	}
	@Override
	public VirtualLattice getSomLattice() {
		
		return null;
	}
	@Override
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) {
		
		return null;
	}
	@Override
	public ArrayList<Integer> getUsedVariablesIndices() {
		
		return null;
	}
	@Override
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables) {
		
		
	}
	@Override
	public void clear() {
		
		
	}
	
}

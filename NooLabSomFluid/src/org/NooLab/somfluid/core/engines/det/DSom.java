package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;


import org.NooLab.field.FieldIntf;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;

import org.NooLab.somfluid.clapp.SomValidation;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.*;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.engines.det.results.SomTargetResults;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
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
public class DSom extends Observable implements DSomIntf{

	
	DSomProperties dsProperties;
	
	ModelingSettings  modelingSettings ;
	
	// SomFluid somFluidParent;
	SomFluidFactory sfFactory;
	SomFluidAppGeneralPropertiesIntf sfProperties;
	
	SomDataObject somData;
	VirtualLattice somLattice;

	SomFluidMonoTaskIntf somTask; // is of mono-flavor here
	
	MultiprocDispatcher multiprocDispatcher ;
	
	//SomTargetedModeling somTargetedModeling;  
	
	private DSomCore dSomCore;

	public BmuBuffer bmuBuffer;
	
	// this gets assigned in DSomCore, since we also  need the actual record sample 
	// (besides the total number of records))
	
	
	
	String activeTvLabel ="";
	int activeTvIndex = -1;
	boolean volatileSampling=false;
	

	public boolean loweredPriority = false ;

	Random random;

	private int callerStatus = 0;

	public boolean inProcessWait = false;

	SomProcessIntf somProcessParent;

	public boolean resultsRequested=true;

	private String instanceGuid;
	
	
	transient PrintLog out;
	// ------------------------------------------------------------------------
	public DSom( SomProcessIntf processParent, SomDataObject sdo, VirtualLattice somlattice, SomFluidTask sftask ) {

		
		this.addObserver( (Observer) processParent );
		
		somProcessParent = processParent;
		sfProperties = somProcessParent.getSfProperties();
		sfFactory = sfProperties.getSfFactory() ;
		
		somTask = sftask;
		somData = sdo;
		somLattice = somlattice ;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		
		random = sfFactory.getRandom();
		random.setSeed(3579) ;
		
		multiprocDispatcher = new MultiprocDispatcher( this ) ;
		
		bmuBuffer = new BmuBuffer(this, somData.getRecordCount() ) ;
		activeTvLabel = "";
		
		// is it some kind of targeted modeling ? -> care for TV
		if (sfProperties.getSomType() == FieldIntf._SOMTYPE_MONO ){
			// it SomType (mono, or prob) NOT instance type!
			
			activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ;
			if (activeTvLabel.length()>0){
				
				activeTvIndex = somData.getVariables().getIndexByLabel( activeTvLabel ) ;
				if ((activeTvLabel.contains("*")) && (activeTvIndex>=0)){
					activeTvLabel = somData.getVariables().getItem(activeTvIndex).getLabel() ;
				}
				if (activeTvIndex>=0){
					somData.getVariables().setTvColumnIndex(activeTvIndex) ;
				}else{
					
					
						activeTvIndex = somData.getVariables().getTvColumnIndex() ;
						activeTvLabel = somData.getVariables().getItem(activeTvIndex).getLabel() ; 
					 
				}
				
				try {
					if (sfProperties.getSomType() < FieldIntf._INSTANCE_TYPE_ASTOR ){
						somData.getVariables().addTargetedVariableByLabel(activeTvLabel ) ;
					}
					
				} catch (Exception e) {
				
					e.printStackTrace();
				}
			}
			
		} // is it some kind of targeted modeling ?
		
		out = sdo.getOut() ;
		
	}
	

	public DSom(DSom dSomIn) {

		
	}


	private void init(){
		
	}
	
	public void close(){
		
		if (dSomCore!=null) dSomCore.close() ;
		
		System.gc() ;

		dSomCore = null;
	}
	
	
	// ------------------------------------------------------------------------	

	public void performModeling() throws Exception {
		 
		int r=-1;
		try{
			
			dSomCore = new DSomCore(this) ;
			r=0;
			
		}catch(Exception e){
			e.printStackTrace();
			throw(new Exception("Critical error while initializing dSomCore, stopping."));
		}
		
		
		// will run the som in a dedicated thread
		r = dSomCore.perform() ;
		
			
		if (r<0){
			throw(new Exception("Critical error in dSomCore (r="+r+"), stopping."));
		}
		
		 
		
	}

	
	public  void performAstor() throws Exception  {
		int r;
		
		
		dSomCore = new DSomCore(this) ;
		r = dSomCore.perform() ;
			// calls performAstor() on somtype = somprob
		
	}
	
	
	public void onCoreProcessCompleted(int resultCode){
		ModelProperties modelProperties ;
		
		if (dSomCore.somResults!=null){ // SomTargetResults@1a6acb9
			modelProperties = new ModelProperties( (ModelProperties) dSomCore.somResults.getModelProperties() ) ;
		}else{
			modelProperties = new ModelProperties();
		}
		

		// dSomCore is not the owner of the lattice !!
		dSomCore.close();
		dSomCore = null;

		System.gc();

			modelProperties.dSomGuid = getGuid();
		
			// is sent to ???
			setChanged();
			this.notifyObservers(modelProperties);
		
		
	}

	// ========================================================================
	
	
	public int getTargetVariableColumn() throws Exception{
		// dependent on modelingSettings
		int varix ,varix2;
		String varstr;
		
		if (activeTvIndex>0){
			return activeTvIndex ;
		}
		varix  = getSomLattice().getNode(0).getSimilarity().getIndexTargetVariable() ;
		varstr = this.modelingSettings.getActiveTvLabel() ;
		varix2 = somData.getVariables().getIndexByLabel(varstr) ;
		
		if (varix!=varix2){
			throw(new Exception("target variable settings have been found to be inconsistent."));
		}
		activeTvIndex = varix ;
		return varix  ;
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


	public SomProcessIntf getSomProcessParent() {
		return somProcessParent;
	}


	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}


	public SomFluidAppGeneralPropertiesIntf getSfProperties() {
		return sfProperties;
	}


	public SomDataObject getSomData() {
		return somData;
	}


	public VirtualLattice getSomLattice() {
		return somLattice;
	}


	public DSomCore getdSomCore() {
		return dSomCore;
	}


	public PrintLog getOut() {
		return out;
	}

/*
	public void setEmbeddingInstance(SomTargetedModeling targetedModeling) {
		 
		somTargetedModeling  = targetedModeling;
	}
	public SomTargetedModeling getEmbeddingInstance() {
		 
		return somTargetedModeling;
	}
*/

	public void setCallerStatus(int callerState) {
		callerStatus = callerState ;
	}


	public int getCallerStatus() {
		return callerStatus;
	}


	/**
	 * @return the inProcessWait
	 */
	public boolean isInProcessWait() {
		return inProcessWait;
	}


	/**
	 * @param inProcessWait the inProcessWait to set
	 */
	public void setInProcessWait(boolean inProcessWait) {
		this.inProcessWait = inProcessWait;
	}


	public String getGuid() {
		return instanceGuid;
	}

	public void setGuid(String instanceGuid) {
		this.instanceGuid = instanceGuid;
	}


	public SomFluidMonoTaskIntf getSomTask() {
		return  somTask;
	}

 
	
	
	 
	
	
}

 

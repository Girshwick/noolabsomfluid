package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.FieldParticleIntf;
import org.NooLab.field.fixed.components.FixedFieldParticlesIntf;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.*;
import org.NooLab.somfluid.env.data.* ;

import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.engines.det.results.*;
import org.NooLab.somfluid.core.engines.det.adv.SomBags;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.env.communication.*;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.tasks.SomFluidTask;


import org.NooLab.somscreen.linear.SimpleExplorationClustering;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.timing.DelayFor;



/**
 * see for event listeners here:
 * 
 * http://www.javaworld.com/javaworld/jw-03-1999/jw-03-toolbox.html?page=6
 * http://stackoverflow.com/questions/1658702/how-do-i-make-a-class-extend-observable-when-it-has-extended-another-class-too
 * 
 * 
 * 
 *
 */
public class SomTargetedModeling    implements  
	 								 		    SomProcessIntf ,
	 								 		    Observer,
	 								 		    // events from particle field, namely selections!
	 								 		    RepulsionFieldEventsIntf,
	 								 		    FixedNodeFieldEventsIntf{

	String name = "";
	long numericID = 0L;
	
	SomFluidFactory sfFactory ;
	//SomFluid somFluid;
	SomHostIntf somHost;
	
	SomFluidProperties sfProperties; 
	ModelingSettings modelingSettings;
	
	SomDataObject somDataObject;
	SomTransformer transformer;
	
	SomFluidTask sfTask;
	
	PhysicalGridFieldIntf particleField ;
	LatticePropertiesIntf latticeProperties;
	
	/** 
	 * VirtualLattice is essentially an ArrayList of &lt;MetaNodeIntf&gt;
	 * ?? we never can call the routines of the MetaNode directly, we always have
	 * to to use an event mechanism  
	 */
	VirtualLattice somLattice ; 
	
	NodesInformer nodesinformer;
	
	SomBags somBags; 
	DSom dSom ;
	
	DataFileReceptorIntf dataReceptor;
	
	 
	int callerStatus = 0;
	
	StringedObjects sob = new StringedObjects();
	PrintLog out = new PrintLog(2, true);
	private boolean isCompleted=false;
 
	
	
	// ========================================================================
	public SomTargetedModeling( SomHostIntf somhost,
								SomFluidFactory factory, 
								SomFluidProperties sfproperties, 
								SomFluidTask sftask ,
								long numericid ) {
		
		sfFactory = factory;
		somHost = somhost  ;
		sfProperties = sfproperties  ; 
		
		somDataObject = somHost.getSomDataObj() ;
		
		modelingSettings = sfProperties.getModelingSettings() ;
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		this.numericID = numericid;
		// this should be a copy, in order to allow parallel processing on the same machine
		// TODO: somLattice = new VirtualLattice(latticeNodes) ;
		 
		if (sfFactory.getOut()!=null){
			out = null;
			out = sfFactory.getOut() ; 
		}
		
		sfTask = sftask  ;
		callerStatus = sfTask.getCallerStatus() ;
		
		nodesinformer = new NodesInformer ();
		
		basicInitialization( factory );
		
		completingInitialization();
		
	}
	// ========================================================================
	
	// cloning
	public SomTargetedModeling( SomTargetedModeling targetModeling, boolean newLattice, int callerState ) {
		 
		callerStatus = callerState;
		
		sfProperties = targetModeling.sfProperties  ; 
		somDataObject = targetModeling.somDataObject ;
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		basicInitialization( sfFactory );
		
		if (newLattice==false){
			somLattice = targetModeling.somLattice ;
			somLattice.reInitNodeData() ;
			somLattice.setSomData(somDataObject);
		}else{
			 
			
			somLattice = new VirtualLattice( this, latticeProperties ,-5 );
		}
		// sfPropertiesDefaults.setGridType( SomFluidFactory._SOM_GRIDTYPE_FIXED ) ;
		// sfPropertiesDefaults.setGridType( SomFluidFactory._SOM_GRIDTYPE_FIXED ) ;
		
		somLattice.setFieldIsDynamic( sfProperties.getSomGridType() == FieldIntf._SOM_GRIDTYPE_FLUID ) ;
		sfTask = targetModeling.sfTask  ;
		
 
	}
 
	

 

	private void basicInitialization(SomFluidFactory factory){
		
		sfFactory = factory;
		
		sfProperties = sfFactory.getSfProperties() ;
		sfFactory.establishPhysicalFieldMessaging( this ); 
		// <<<<<<<<< onSelectionRequestCompleted() very important for routing result to the SomLattice/VirtualLattice !!!
		// also for associative storage, where we do NOT have a targeted modeling
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		// only if requested by the properties ....
		somBags = new SomBags(this, sfProperties) ;
		 
		out.setPrefix("[SomFluid-main]");
		out.setShowTimeStamp(true);
	}


	protected void completingInitialization(  ){
		
		
		if (somLattice!=null){
			somLattice.clear();
			somLattice.close();
			somLattice=null;	
		}
		
		
		particleField = somHost.getSomFluid().getParticleField( ) ;
		
		// re-arranging the endpoint for notifications
		sfFactory.establishPhysicalFieldMessaging( this); // RepulsionFieldEventsIntf eventSink
		
		// do all into a container: LatticePreparation
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		LatticePreparation latticePreparation = new LatticePreparation( this, somHost,sfProperties);
		latticePreparation.setNodesInformer( nodesinformer ) ;
		
		somLattice = latticePreparation.getLattice();

		/////////////////////////////////<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		/*
		somLattice = latticePreparation.getLattice();
		 
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		somLattice = new VirtualLattice(this,latticeProperties,(int) (100+numericID));
		  
		somLattice.setFieldIsDynamic( sfProperties.getSomGridType() == FieldIntf._SOM_GRIDTYPE_FLUID ) ;
	
		// initStructures( somLattice );
	
		boolean displayNodeCount = sfTask.getCounter()<=0;
		createVirtualLattice( somLattice, particleField, initialNodeCount , displayNodeCount);
		*/ 
	}
	/**  
	 * 
	 * connecting the SOM nodes to the particles collection
	 * 
	 * Note that the particles in the field are just containers! They are not identical to nodes.
	 * Hence, our nodes need to be attached to the particles  
	 */
	@SuppressWarnings("unchecked")
	private void createVirtualLattice( VirtualLattice somLattice, PhysicalGridFieldIntf particleField, int initialNodeCount, boolean showNodeCount) {
		
		MetaNode mnode;
		long idbase;
		FieldParticleIntf particle=null;
		
		somDataObject = somHost.getSomDataObj() ;
		 									if ((showNodeCount) || (somHost.getSfTask().getCounter()<=3)){ 
		 										out.print(2, "creating the logical som lattice for "+initialNodeCount+" nodes...");
		 									}
		for (int i=0;i<initialNodeCount;i++){
			
			mnode = new MetaNode( somLattice, somDataObject ,i ); 
			somLattice.addNode(mnode) ;
			
											out.print(4,"Node <"+i+">, serial = "+mnode.getSerialID());
			try{
				// requires NodesInformer
				nodesinformer.registerNodeinNodesInformer( mnode );
				 
				int somtype = sfProperties.getSomGridType();
				// the same as somLattice.getLatticeProperties().getSomType()
				
				ArrayList<Variable> v=null;
				
				if ( somtype == FieldIntf._SOMTYPE_MONO){
					v = somDataObject.getVariableItems() ;	
				}else{
					v = somDataObject.getVariableItemsReference();
				}
				v = somDataObject.getVariableItemsReference();
				
				// ArrayList<Variable> vari = somDataObject.getVariableItems();
				ProfileVectorIntf pv = mnode.getIntensionality().getProfileVector();
				// pv.setVariables( vari ) ;
				pv.setVariables( v ) ;
				
				mnode.getExtensionality().getStatistics().setVariables(v);
				
				ArrayList<Double> values = ArrUtilities.createCollection( v.size(), 0.5) ;
				pv.setValues(values);
				
				
				Object obj = particleField.getParticles();
				 
				if (somtype == FieldIntf._SOM_GRIDTYPE_FLUID){
					particle = ((RepFieldParticlesIntf)obj).get(i);
				}
				if (somtype == FieldIntf._SOM_GRIDTYPE_FIXED){
					particle = (( FixedFieldParticlesIntf)obj).get(i);
				}
				
				if (particle!=null){
					particle.setIndexOfDataObject( mnode.getSerialID() );
				}
				
			}catch(Exception e){
				e.printStackTrace() ;
			}
			 
		}
		 
		double d = particleField.getAverageDistanceBetweenParticles();
		somLattice.setAveragePhysicalDistance(d);
		
											out.print(3, "logical som lattice created.");
		// 
	}


	protected void initializeNodesWithData(){
		
		// "by index" refers to 
		// this.notifyNodeByIndex(1, new NodeTask( NodeTask._TASK_SETDATA, variablesSetupDef, null) );
		// this.notifyNodeBySerial( somLattice.getNode(5).getSerialID(), new NodeTask( NodeTask._TASK_SETDATA, new String("123-"+i)) );
	
		// TODO: we may perform a PCA, thus deriving a weight vector (NOT: profile vector!!!)
		//       that would prepare the SOM into the direction of the main dimensions
		int n=0;
		n=1;
		
	}

	protected void initializeNodeWithRandomValues( int nodeindex ){
		
		Variables variables = somDataObject.getVariables() ;
		ArrayList<String> uv = variables.getLabelsForVariablesList(variables) ;	
		
    	somLattice.getNode(nodeindex).onDefiningFeatureSet( (Object)sob.encode( (Object)uv ),null);
    	somLattice.getNode(nodeindex).onDefiningTargetVar( (Object)sob.encode( (Object)variables.getActiveTargetVariable()) );
    	
    	// we cold provide a mode for asymmetry, or later, a vector of preferred values
    	somLattice.getNode(nodeindex).onRequestForRandomInit(null); 
		
	}
	
	protected void initializeNodesWithRandomvalues( Variables vars){
	
		NodeTask task;
		 
		String guid; 
		LatticeFutureVisor latticeFutureVisor;
		
		
		
		
		if ((vars==null) || (vars.size()<=1)){
			return;
		}
		if (somLattice==null){
			return;
		}
											int outlevel=2;
											if ((LogControl.Level>=2) && (somHost.getSfTask().getCounter()>3)){
												outlevel=3;
											}
											out.print(outlevel, "loading data definitions to Som-Lattice (vector size: "+vars.size()+")..."); 
		
											
		// initialize feature vectors: ALL variables, WITHOUT id, tv !!!
		// if we won't use all variables, the size of the vectors won't match (intensionality.profilevector.values)
		System.gc() ;									
		ArrayList<String> uv = vars.getLabelsForVariablesList(vars) ;			
	    int n = somLattice.getNodes().size() ;
	    
	    NodeTask initTask = new NodeTask( NodeTask._TASK_RNDINIT  );
	    
	    // in order to obtain suitable profiles for initialization, we could calculate a simple Knn++ for 4 clusters, 
	    // using PCA variables and including TV into clustering, then providing those 4 cluster profiles to the lattice.
	    // from there it would be taken by the nodes dependent on their relative position, and mixed with ranom values
	    
	    int cc = somDataObject.getNormalizedDataTable().getColcount();
	    int rc = somDataObject.getNormalizedDataTable().getRowcount();
	    // 
	    // PCA pca; is in: import org.NooLab.somscreen.linear.PCA;
	    SimpleExplorationClustering sec = new SimpleExplorationClustering( somDataObject.getNormalizedDataTable() ) ;
	    sec.perform() ;
	    ArrayList<Double> pvalues = sec.getProfiles();
	    ArrayList<String> pvars = sec.getProfileVariables();
	    somLattice.setPreferredInitializationValues( pvars,pvalues ); // nothing happens so far ... 
	    
	    // TODO: this could be done in parallel 
	    
	    									out.print(2, "initializing nodes (n="+n+")...");
	    for (int i=0;i<n;i++){
	    									if ((i>500) || ( (n*cc*rc)>5000000)){
	    										out.printprc(2, i, n, n/5, "");
	    									}
	    	
	    	initializeNodeWithRandomValues(i) ;
	    }
	    
	    n=0;
		/* later: a listening mechanism
		
		latticeFutureVisor = new LatticeFutureVisor(somLattice,  NodeTask._TASK_SETVAR );
		 
											out.print(4, "before task sending...");
		task = new NodeTask( NodeTask._TASK_SETVAR, (Object)sob.encode( (Object)uv ));
		// do it for all nodes
		notifyAllNodes( task );
											out.print(4, "returned from task sending...  -> now waiting");
		latticeFutureVisor.waitFor(); // it will wait for completion of "_TASK_SETVAR", for all nodes since we did not define a particular one		
	
	
											out.print(4, "continue, next task...");
		// set target variable ... TODO other messages about dynamic configuration : blacklist, whitelist, sim function 
		task = new NodeTask( NodeTask._TASK_SETTV, (Object)sob.encode( (Object)vars.getActiveTargetVariable()) );
		// do it for all nodes
		notifyAllNodes( task );
		
		 
											out.print(3, "loading data definitions done.");
											out.print(3, "initializing nodes...");
											
		latticeFutureVisor = new LatticeFutureVisor(somLattice,  NodeTask._TASK_RNDINIT );
		
		
		// ATTENTION: we have to wait !!! The informer immediately returns, then the init is sent before the node initialized!
		// the need for waiting until a process is completed is quite rare, and should occur only in the startup phase,
		// even on loading data into the SOM (learning) there should be no need to wait
		task = new NodeTask( NodeTask._TASK_RNDINIT  );
		notifyAllNodes( task );
		
		latticeFutureVisor.waitFor(); out.delay(100); 
											out.print(3, "initialization of SomLattice done (vector size: "+uv.size()+"). ");
											
		*/						
	}


	@SuppressWarnings("unchecked")
	public void prepare( ArrayList<Integer> usedVariables) throws Exception{
		
		int ix;
		String tvlabel = null;
		ArrayList<Double> usageVector =new ArrayList<Double>();
		
		Variables variables = somDataObject.getVariables() ;
		Variables vars = new Variables();
		Variable v;
		
		if (usedVariables.size()==0){
			
		}
		for (int i=0;i<variables.size();i++){
			if (i>=usageVector.size())usageVector.add(0.0) ;
		}

		
		if (variables.getUsageIndicationVector().size()==0){
			for (int i=0;i<variables.size();i++){
				variables.getUsageIndicationVector().add(0.0) ;
			}
		}else{
			// TODO all variables except black, ID, IDs, and TVs except selected one
		}
		
		
		
		// index
		if (variables.getIdVariables().size()>0){
			v = variables.getIdVariables().get(0) ;
			vars.additem( v );
			if (variables.getIdColumnIndex()<0){
				ix = variables.getIndexByLabel( v.getLabel());
				variables.setIdColumnIndex(ix) ;
			}
		}
		

		// target
		v = variables.getTargetVariable() ;
		if (v!=null){
			tvlabel = v.getLabel() ;
		}
		if (v==null){ // setActiveTvLabel("*TV") 
			
			tvlabel = modelingSettings.getActiveTvLabel() ;
			double uv = 1.0;
			 
			ix = variables.getIndexByLabel(tvlabel) ;
			
			if ((ix<0) && (tvlabel.indexOf("*")>=0)){
				 // search the first matching one
			}else{
				// might indeed not exist at all...
				if (ix>=0){
					v = variables.getItem(ix) ;
					tvlabel = v.getLabel();
					variables.setTargetVariable(v) ;
				
					uv = 1.0 ;
					
				}
			}
			if (ix>=0){
				usageVector.set(ix, uv);
			}
		}
		if ((v != null) && (v.getLabel().length()>0)){
			vars.additem( v );
		}
		

		 
		if (usedVariables.size()>0){
			// the desired variables
			for (int i = 0; i < usedVariables.size(); i++) {
				ix = usedVariables.get(i) ;
				if (ix>=0){
					vars.additem(variables.getItem(ix));
					variables.getUsageIndicationVector().set(ix, 1.0);
					usageVector.set(ix, 1.0);
				}else{
					
				}
				
			}
		}else{
			for (int i = 0; i < somDataObject.getActiveVariables().size(); i++) {
				v = somDataObject.getActiveVariables().getItem(i);
				vars.additem(v );
				String label = v.getLabel() ;
				if ((v.isID()==false) && (somDataObject.getVariables().getBlacklistLabels().indexOf(label)<0)){
					usageVector.set(i, 1.0);
				}
			}
		}
		ArrayList<Integer> useIndexes = (ArrayList<Integer>)variables.transcribeUseIndications(usageVector);  
		
		variables.setInitialUsageVector( variables.deriveVariableSelection(useIndexes, 0) ) ;
		
		init(variables);
		
		if (somLattice.getNodes().get(0).getExtensionality().getCount()>0){
			somLattice.clear();
		}
		
		ArrayList<Variable> varin, vari = somDataObject.getVariableItems();
		ProfileVectorIntf pv = somLattice.getNode(0).getIntensionality().getProfileVector();
		varin = pv.getVariables() ;

		if (variables.getTargetVariable()==null){
			// change modeling mode !!
			throw(new Exception("Requested modeling mode was <targeted>, but no target variable has been found."));
		}
		
		
											out.print(3, "lattice going to be configured : "+somLattice.toString());
		somLattice.getSimilarityConcepts().setUsageIndicationVector(usageVector) ;
		somLattice.getSimilarityConcepts().setIndexTargetVariable( variables.getIndexByLabel(tvlabel) );
		somLattice.getSimilarityConcepts().setIndexIdColumn( variables.getIdColumnIndex() ) ;
		
		somLattice.setSomData(somDataObject) ; 
		somLattice.spreadVariableSettings(); // informs all nodes about the usevector
		// somLattice.getSimilarityConcepts().setUsageIndicationVector(usageVector) ;
		//  node.getSimilarity.usageIndicationVector is wrong, hence profile.getValues() is also wrong 
	}


	public void setSource( int index) {
		
		
	}
	 

	public SomTargetedModeling init( Variables _vars ){
		Variables vars = null ;
		
		String activeTvLabel;
		// now, the somDataObject knows about the DataTable
		// if there is some data in SomDataObject, it will be loaded into nodes
		
		// we have to remove empty columns, blacklisted columns, columns that are excluded dynamically 
		// by criteria like derivation level 
		// somDataObject.determineActiveVariables(); // not yet functional.... else, it should be called BEFORE this init()...
		
		
		ClassificationSettings cf = sfProperties.getModelingSettings().getClassifySettings() ;
		int targetMode = cf.getTargetMode();
		
		if ( targetMode==ClassificationSettings._TARGETMODE_MULTI ) {
			if ((cf.getTargetGroupDefinition().length==0) || (cf.getTargetGroupDefinition()[0].length==0) ||
				(cf.getAutomaticTargetGroupDefinition()) ){

				somDataObject.inferTargetGroups( sfProperties.getModelingSettings() );
			}
		}

		if ((_vars==null) || (_vars.size()<=1)){
			vars = somDataObject.getVariables() ;
			vars.getBlacklist().clear();
		} else{
			vars = somDataObject.getVariables() ;
			//_vars;
		}
		
		
		initializeNodesWithRandomvalues( vars ); // adopting feature vectors, not yet the data of course
		 
		// initializeNodesWithData(); // nothing there so far, could be stuffed with top-4 of a linear/ized description 
		// we would use a method primingNodesCollection() 
		// to which we would stuff a small number of profiles, that are supposed to separate quite well
		// e.g. based on PCA & K-Means, KNN principal
		
		activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ; // "TV"
		// TargetVariable  targetVariable;
		if (_vars.getIndexByLabel(activeTvLabel)<0){
			activeTvLabel = "";
			_vars.setTargetVariable(null);
		}
		
		return this;
	}
 


	public void clear() {
		                   					out.print(5, "stm-clear (1)...");
		nodesinformer.stopMsgSrv(); // nodesinformer
											out.print(5, "stm-clear (2)...");
		somLattice.reInitNodeData() ;
		somLattice.close();
											out.print(5, "stm-clear (3)...");
		// somLattice=null;
		
		(new DelayFor()).period(100);
		dSom.close() ;
		dSom = null;
											out.print(5, "stm-clear (4)...");
		
		
		System.gc();
		(new DelayFor()).period(200);
	}


	public String perform( int callerState ) throws Exception {
		  
		VirtualLattice somlattice;
		
		
		
		
		sfTask.setCallerStatus(callerState);
		
		// no bagging (is a performed as a different task), just standard normal som-ing 
		dSom = new DSom( this, somDataObject, somLattice, sfTask );
		
		String dsomGuid = GUID.randomvalue() ;
		
		dSom.setGuid(dsomGuid);
		
											String str = "Som is running, identifier: "+dsomGuid;
											out.print(3, "Som running is going to be prepared...") ;
		
		somlattice = dSom.getSomLattice();
											out.print(3, "dSom is working on lattice : "+somlattice.toString()) ;
		// dSom.setEmbeddingInstance( this ) ;

		dSom.setCallerStatus(callerStatus) ;
		
		 
		ArrayList<Double> usevector ;

						// this is globally constant, hence we should not do that... we need flexibility for screenings
						usevector = dSom.getSomData().getVariables().getUsageIndicationVector();
		
		usevector = somlattice.getSimilarityConcepts().getUsageIndicationVector();
		
		if (dSom.activeTvIndex>=usevector.size()){
			usevector.add(-2.0);
		}else{
			usevector.set(dSom.activeTvIndex,-2.0);
		}
		
		somlattice.getSimilarityConcepts().setUsageIndicationVector( usevector ) ;
		
		
		somlattice.getSimilarityConcepts().setIndexTargetVariable(dSom.activeTvIndex);
		
		int ix = dSom.getSomData().getVariables().getIdColumnIndex();
		if (ix>=0){
			somlattice.getSimilarityConcepts().setIndexIdColumn(ix) ;
		}
		
		somlattice.setSomData(dSom.getSomData()) ;
		// settings to be spreaded will be taken from : similarityConcepts.getUsageIndicationVector()
		somlattice.spreadVariableSettings();
		 
		// check initialization of profiles
		MetaNode node0 = somlattice.getNode(0) ;
		if (node0.getProfileVector().getValues().size() <=1) {
			// should not happen... initialization of SomLattice failed?
		}
		
		if (sfProperties.getModelingSettings().getSomBagSettings().getApplySomBags()){
			
			// we create bags according to parameters, each bag will run a DSom then...
			// results will be collected by SomBag, and meta-results will be evaluated also there
			somBags.createBags( dSom ); // will put the bags as samples into object "dataSampler{}"
			
			somBags.runBags();

		} else{
			if (callerState>0){
				dSom.inProcessWait = true ;
			}
											out.print(3, "Som is running...") ;
			dSom.performModeling();
		}
		 
		return dsomGuid;
	}
	
	
	@SuppressWarnings("unused")
	@Override
	public void update(Observable dsomInstance, Object results) {
		// 
		DSom dsom ;
		SomTargetResults stm ;
				
		dsom = (DSom) dsomInstance;
		String dsomGuid = dsom.getGuid();
		
		ModelProperties mprops = (ModelProperties) results ;
		
		mprops.task = sfTask;
		
		isCompleted=true;
		somHost.onTargetedModelingCompleted(mprops);
	}


	/**
	 * @param somDataObject the somDataObject to set
	 */
	public void setSomDataObject(SomDataObject somDataObject) {
		this.somDataObject = somDataObject;
	}


	/**
	 * @return the somBags
	 */
	public SomBags getSomBags() {
		return somBags;
	}


	/**
	 * @return the dSom
	 */
	public DSom getdSom() {
		return dSom;
	}
	
	public SomTransformer getTransformer() {
		return transformer;
	}
	
	 

		
	@Override
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) {
		ArrayList<Double> uv = somLattice.getSimilarityConcepts().getUsageIndicationVector();
		
		if (inclTV==false){
			for (int i=0;i<uv.size();i++){
				if (uv.get(i)<0){
					uv.set(i, 0.0);
				}
			}
		}
		if (inclTV==true){
			int tvix = somLattice.getSimilarityConcepts().getIndexTargetVariable() ;
			if ((tvix>0) && (tvix<uv.size())){
				uv.set(tvix, -2.0) ;
			}
		}

		return uv;
	}


	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer> getUsedVariablesIndices() {
		
		ArrayList<Integer>  usedVariablesIndices = new ArrayList<Integer>();
		ArrayList<Double> uv= new ArrayList<Double>();
		
		if (somLattice!=null){
			uv = somLattice.getSimilarityConcepts().getUsageIndicationVector() ;
		}
		
		usedVariablesIndices = (ArrayList<Integer>) somDataObject.getVariables().transcribeUseIndications(uv) ;
		return usedVariablesIndices;
	}


	@Override
	public void setUsedVariablesIndices( ArrayList<Integer> usedVariables) {
		 
		
	}


	public Variables setUsedVariablesIndicatorVector( ArrayList<Double> impUsagevector) {

		Variables vars,selectedVariables;
		  
		ArrayList<String> blacklistLabels = new ArrayList<String>(); 
		ArrayList<String> initialUsageVector = new ArrayList<String>(); 
		
		ArrayList<Integer> usedVariables;
		
		vars = somDataObject.getActiveVariables() ; // all vars
		
		// we have to refer to the original blacklist, which we probably extend
		// is of type ArrayList<Variable> originalBlacklist
		
		// we create a deep copy, leaving the source unchanged
		selectedVariables = new Variables( vars );
		
		for (int i=0;i<impUsagevector.size();i++){
			if (impUsagevector.get(i)>0){
				// selectedVariables.additem( vars.getItem(i) );
				initialUsageVector.add( vars.getItem(i).getLabel() ) ;
			}else{
				blacklistLabels.add( vars.getItem(i).getLabel() ) ;
			}
		}
		 
		selectedVariables.setBlacklistLabels(blacklistLabels) ;
		
		selectedVariables.setInitialUsageVector( initialUsageVector ) ;
		
		somDataObject.getVariables().setInitialUsageVector(initialUsageVector) ;
		
		return selectedVariables;
	}
	
	
	public String getNeighborhoodNodes( int nodeindex, int surroundN ) {
		int particleindex=nodeindex;
		
		// we need a map that translates between nodes and particles
		
		particleField.setSelectionSize( surroundN ) ;
		
		// asking for the surrounding, -> before start set the selection radius == new API function
		String guid = particleField.getSurround( particleindex, 1, surroundN, true); 
		
		// will immediately return, the selection will be sent 
		// through event callback to "onSelectionRequestCompleted()" below
		
 		return  guid;
	}
	
	@Override
	public void statusMessage(String msg) {
		
	}

	public LatticePropertiesIntf getLatticeProperties() {
		return latticeProperties;
	}


	/**
	 * @return the somLattice
	 */
	public VirtualLattice getSomLattice() {
		return somLattice;
	}


	@Override
	public boolean getInitComplete() {
		// TODO Auto-generated method stub
		return false;
	}


	/**
	 * @return the sfProperties
	 */
	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}


	/**
	 * @return the somDataObject
	 */
	public SomDataObject getSomDataObject() {
		return somDataObject;
	}


	
	public void _onLayoutCompleted(int flag) {
		
		out.print(2,"Layout of particle field has been completed.");
	}


	@Override
	public void onCalculationsCompleted() {
	
		if (sfFactory.getPhysicalFieldStarted()==0){
			out.print(2,"Calculations in particle field have been completed.");
			out.print(1,"... please wait ...");
		}
		sfFactory.setPhysicalFieldStarted(1);
		
		sfFactory.getFieldFactory().setInitComplete(true);
	}


	@Override
	public void onSelectionRequestCompleted( Object resultsObj ) {
		
		
		SurroundResults results, clonedResults;
		String str ;
		int[] particleIndexes;
		
		results = (SurroundResults)resultsObj;  
		
		this.somHost.selectionEventRouter(results,somLattice);
		
		 
	}


	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		
	}


	@Override
	public void onActionAccepted(int action, int state, Object param) {
		
	}


	public void setSerialID(long serial) {
		numericID = serial;
	}

	public long getSerialID() {
		return numericID;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}


	@Override
	public void onLayoutCompleted(int flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeChangeEvent(ExtensionalityDynamicsIntf extensionality, long result) {
		
		// for registration and handling : fork immediately into container !!
		long nodeID, uuid = extensionality.getNodeNumGuid();
		uuid   = extensionality.getNodeSerial();
		
		/* it is also sent to implementations of SomProcessIntf :
		 * 	  - SomDataObject
		 *    - SomAstor
		 */
		
	}
}

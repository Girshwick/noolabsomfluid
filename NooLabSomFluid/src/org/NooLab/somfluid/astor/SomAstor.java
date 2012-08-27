package org.NooLab.somfluid.astor;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.itexx.storage.DataStreamProvider;
import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.LatticePreparation;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.somfluid.env.communication.NodesInformer;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somscreen.linear.SimpleExplorationClustering;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;


/**
 * 
 * This takes the same role as the class "SomTargetedModeling" (which is used for targeted modeling)
 * 
 * 
 * 
 *
 */
public class SomAstor 
						extends
						            Observable
						implements  
								    Observer,
								    Runnable,
									SomProcessIntf ,  
									FixedNodeFieldEventsIntf {

	private SomFluidProperties sfProperties;
	private SomFluidTask sfTask;
	private SomFluidFactory sfFactory;
	private SomHostIntf somHost;
	private SomDataObject somDataObj;
	
	private DSom dSom;
	
	ModelingSettings modset ;
	
	PhysicalGridFieldIntf particleField ;
	private LatticePropertiesIntf latticeProperties;
	private VirtualLattice astorLattice;
	
	private NodesInformer nodesinformer;
	
	private boolean isRunning;

	Thread astorThrd;
	
	
	StringedObjects sob = new StringedObjects();
	PrintLog out;
	private boolean userBreak;
	private boolean streamReceptorSwitchedOn = false ;
	private int delay = 1000 ;
	private int registeredChanges;
	private int changesThreshold=0;


	ArrayList<Long> changedNodes = new ArrayList<Long> ();
	
	// ========================================================================
	public SomAstor( SomHostIntf somhost, 
					 SomFluidFactory sfFactory,
					 SomFluidProperties sfProperties, 
					 SomFluidTask sfTask, 
					 long serialID) throws Exception {

		this.sfProperties = sfProperties;
		this.sfTask = sfTask;
		this.sfFactory = sfFactory;
		this.somHost = somhost;
		 
		somDataObj = somHost.getSomDataObj() ;
		modset = sfProperties.getModelingSettings();
		
		out = somhost.getSomFluid().getOut() ;
		
		prepareAstor();
	}
	
	
	@Override
	public void clear() {
		 
		isRunning = false;
		delay=10 ;
		
		
	}
	// ========================================================================	

	
	public void prepareAstor() throws Exception{
		
		
		if (somDataObj==null){
			throw(new Exception("The data object provided to Astor was empty.\n"));
		}
		
		
		
		
		// now activate listener, router and receiver
		
		somDataObj.establishDataLinkage();
		
		
		nodesinformer = new NodesInformer ();
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		LatticePreparation latticePreparation = new LatticePreparation( this, somHost,sfProperties);
		latticePreparation.setNodesInformer( nodesinformer ) ;
		
		astorLattice = latticePreparation.getLattice();
		
		
		particleField = somHost.getSomFluid().getParticleField( ) ;
		particleField.registerEventMessaging(this) ;
		 
		astorThrd = new Thread(this,"astorThrd") ;
	}
	
	

	@SuppressWarnings("unchecked")
	public void prepare( ArrayList<Integer> usedVariables) throws Exception{
		
		int ix;
		
		ArrayList<String> fields = new ArrayList<String>();
		
		ArrayList<Double> usageVector =new ArrayList<Double>();
		
		Variables variables = somDataObj.getVariables() ;
		Variables vars = new Variables();
		Variable v;
		
		
		if (sfProperties.getSourceType() == DataStreamProviderIntf._DSP_SOURCE_DB){
			TexxDataBaseSettingsIntf dbs ;
			
			dbs = sfProperties.getDatabaseSettings();
			fields = dbs.getTableFields();
			//dbAccessDefinition
		} // DB?
		
		
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
			for (int i = 0; i < somDataObj.getActiveVariables().size(); i++) {
				v = somDataObj.getActiveVariables().getItem(i);
				vars.additem(v );
				String label = v.getLabel() ;
				if ((v.isID()==false) && (somDataObj.getVariables().getBlacklistLabels().indexOf(label)<0)){
					usageVector.set(i, 1.0);
				}
			}
		}
		ArrayList<Integer> useIndexes = (ArrayList<Integer>)variables.transcribeUseIndications(usageVector);  
		
		variables.setInitialUsageVector( variables.deriveVariableSelection(useIndexes, 0) ) ;
		
		init(variables);
		
		if (astorLattice.getNodes().get(0).getExtensionality().getCount()>0){
			astorLattice.clear();
		}
		
		ArrayList<Variable> varin, vari = somDataObj.getVariableItems();
		ProfileVectorIntf pv = astorLattice.getNode(0).getIntensionality().getProfileVector();
		varin = pv.getVariables() ;

		if (variables.getTargetVariable()!=null){
			// inform about modeling mode !!
			String tvstr = variables.getTargetVariable().getLabel();
			out.print(2, "Requested modeling mode was <astor>, the provided target variable <"+tvstr+"> will be excluded from the list of active fields.");
			
			vars = modset.getVariables();
			vars.addBlacklistLabel( tvstr);
			variables.setTargetVariable(null) ;
			// TODO: also all tv candidates
		}
		
		
											out.print(3, "lattice going to be configured : "+astorLattice.toString());
		astorLattice.getSimilarityConcepts().setUsageIndicationVector(usageVector) ;
		// astorLattice.getSimilarityConcepts().setIndexTargetVariable( variables.getIndexByLabel(tvlabel) );
		astorLattice.getSimilarityConcepts().setIndexIdColumn( variables.getIdColumnIndex() ) ;
		
		astorLattice.setSomData(somDataObj) ; 
		astorLattice.spreadVariableSettings(); // informs all nodes about the usevector

	}

 
	private void init(Variables variables) {

		initializeNodesWithRandomvalues( variables ); 
	}


	protected void initializeNodeWithRandomValues( int nodeindex ){
		
		Variables variables = somDataObj.getVariables() ;
		ArrayList<String> uv = variables.getLabelsForVariablesList(variables) ;	
		
    	astorLattice.getNode(nodeindex).onDefiningFeatureSet( (Object)sob.encode( (Object)uv ),null);
    	
    	
    	// we cold provide a mode for asymmetry, or later, a vector of preferred values
    	astorLattice.getNode(nodeindex).onRequestForRandomInit(null); 
		
	}
	
	protected void initializeNodesWithRandomvalues( Variables vars){
	
		NodeTask task;
		 
		String guid; 
		LatticeFutureVisor latticeFutureVisor;
		
		
		
		
		if ((vars==null) || (vars.size()<=1)){
			return;
		}
		if (astorLattice==null){
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
	    int n = astorLattice.getNodes().size() ;
	    
	    NodeTask initTask = new NodeTask( NodeTask._TASK_RNDINIT  );
	    
	    // in order to obtain suitable profiles for initialization, we could calculate a simple Knn++ for 4 clusters, 
	    // using PCA variables and including TV into clustering, then providing those 4 cluster profiles to the lattice.
	    // from there it would be taken by the nodes dependent on their relative position, and mixed with ranom values
	    
	    int cc = somDataObj.getNormalizedDataTable().getColcount();
	    int rc = somDataObj.getNormalizedDataTable().getRowcount();
	    // 
	    // PCA pca; is in: import org.NooLab.somscreen.linear.PCA;
	    SimpleExplorationClustering sec = new SimpleExplorationClustering( somDataObj.getNormalizedDataTable() ) ;
	    sec.perform() ;
	    ArrayList<Double> pvalues = sec.getProfiles();
	    ArrayList<String> pvars = sec.getProfileVariables();
	    astorLattice.setPreferredInitializationValues( pvars,pvalues ); // nothing happens so far ... 
	    
	    // TODO: this could be done in parallel 
	    
	    									out.print(2, "initializing nodes (n="+n+")...");
	    for (int i=0;i<n;i++){
	    									if ((i>500) || ( (n*cc*rc)>5000000)){
	    										out.printprc(2, i, n, n/10, "");
	    									}
	    	
	    	initializeNodeWithRandomValues(i) ;
	    }
	    									out.printprc(2, n, n, n/5, "");	    
	    n=0;
		 					
	}

	private void perform() throws Exception {
		
		dSom = new DSom( this, somDataObj, astorLattice, sfTask );
		
		dSom.performAstor();
	}
	
	@Override
	public void update(Observable senderObj, Object msg) {
		 
		out.printErr(2, "Observer-update msg received in SomAstor, \n"+
				        "  sender = "+latticeProperties.toString()+ "\n"+
				        "  msg    = "+msg.toString())  ;
		
		if (senderObj instanceof DSom){
			
		}
		if (senderObj instanceof DataStreamProvider){
			
		}
		
	}


	public String start() {
		
		String guid  = GUID.randomvalue() ;
	
		isRunning = true;
		
		// start in thread and return
		astorThrd.start();
		
		return guid;
	}
	
	
	public boolean isRunning() {
		 
		return isRunning;
	}
	
	@Override
	public void run() {
		  
		try {
			perform();
			
			
		} catch (Exception e) {
			
			isRunning = false;
			e.printStackTrace();
		}
		
		// start the stream receptor via observer
		streamReceptorSwitchedOn  = true;
		
		IndexedDistances ixds = astorLattice.getNodeSizes(true);
		int n = (int)ixds.getItem(0).getDistance() ;
		out.print(2, "largest node (ix:"+ixds.getItem(0).getIndex()+") = "+n) ;

		//
		out.print(2, "\n\nAstor SOM is in stream receiver mode now.");
		while ((isRunning==true) && (userBreak==false)){
			
			
			PrintLog.Delay(delay);
		}
		out.printErr(2, "");
	}
	
	@Override
	public SomDataObject getSomDataObject() {
		
		return this.somDataObj ;
	}

	@Override
	public String getNeighborhoodNodes(int nodeindex, int surroundN) {
		//
		
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
	public SomFluidAppGeneralPropertiesIntf getSfProperties() {
		
		return sfProperties;
	}

	@Override
	public LatticePropertiesIntf getLatticeProperties() {
		
		return latticeProperties;
	}

	@Override
	public VirtualLattice getSomLattice() {
		
		return astorLattice;
	}

	@Override
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) {

		ArrayList<Double> uv = astorLattice.getSimilarityConcepts().getUsageIndicationVector();
		
		if (inclTV==false){
			for (int i=0;i<uv.size();i++){
				if (uv.get(i)<0){
					uv.set(i, 0.0);
				}
			}
		}
		return uv;
	}

	@Override
	public ArrayList<Integer> getUsedVariablesIndices() {

		ArrayList<Integer>  usedVariablesIndices = new ArrayList<Integer>();
		ArrayList<Double> uv= new ArrayList<Double>();
		
		if (astorLattice!=null){
			uv = astorLattice.getSimilarityConcepts().getUsageIndicationVector() ;
		}
		
		usedVariablesIndices = (ArrayList<Integer>) somDataObj.getVariables().transcribeUseIndications(uv) ;
		return usedVariablesIndices;
	}

	@Override
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void nodeChangeEvent(ExtensionalityDynamicsIntf extensionality, int result) {
		// called via interface from "ExtensionalityDynamics", which is part of the node and holds the indexes of the digested records
		
		/* it is also sent to actually referenced implementations of SomProcessIntf :
		 * 	  - SomTargetedModeling
		 *    - SomDataObject
		 *    - SomAppClassifier
		 */
		
		// result is the index value of the record in the datatable (according to the index column)
		// for registration and handling : fork immediately into container !!
		long nodeID, uuid ;
		uuid    = extensionality.getNodeNumGuid();
		nodeID  = extensionality.getNodeSerial();
		
		// we collect the number of changes in order to decide about a trigger
		
		changedNodes.add(uuid); // important: using the GUID, not the serial.... the lattice maintains a map from NodeGuid to the node's Object reference
		registeredChanges++;
		
		if ((registeredChanges>200) && (registeredChanges>changesThreshold)){
			
			// copy to a clone
			// ArrayList<Integer> changedNodes 
			
			// informing inner class "SomChangeEventObserver" in "SomAstorNodeContent" 
			// registering via astorNodeContent.registerObservedSomProcess( somAstor ) takes
			// place in "SomAssociativeStorage"
			setChanged();
			this.notifyObservers(changedNodes.clone());  // just for code reference: 138709
			
			registeredChanges=0;
			changedNodes.clear(); 
		}
	}


	@Override
	public void onSelectionRequestCompleted(Object results) {
	 
		this.somHost.selectionEventRouter((SurroundResults) results,astorLattice);
	}

	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		 
		
	}

	@Override
	public void onActionAccepted(int action, int state, Object param) {
		 
		
	}

	@Override
	public void statusMessage(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculationsCompleted() {
		
		out.printErr(2, "SomAstor received event <onCalculationsCompleted>") ;
	}


	public boolean isUserBreak() {
		return userBreak;
	}


	public void setUserBreak(boolean userBreak) {
		this.userBreak = userBreak;
	}


	public int getRegisteredChanges() {
		return registeredChanges;
	}


	public void setRegisteredChanges(int registeredChanges) {
		this.registeredChanges = registeredChanges;
	}


	public void setChangesThreshold(int limit) {
		
		changesThreshold = limit;
	}

}

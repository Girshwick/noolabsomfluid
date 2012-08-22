package org.NooLab.somfluid.astor;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
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
import org.NooLab.somscreen.linear.SimpleExplorationClustering;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;


/**
 * 
 * This takes the same role as the class "SomTargetedModeling", which is used for targeted modeling
 * 
 * 
 * 
 *
 */
public class SomAstor 
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
	
	private LatticePropertiesIntf latticeProperties;
	private NodesInformer nodesinformer;
	private VirtualLattice somLattice;
	private boolean isRunning;

	Thread astorThrd;
	
	
	StringedObjects sob = new StringedObjects();
	PrintLog out;
	
	// ========================================================================
	public SomAstor( SomHostIntf somhost, SomFluidFactory sfFactory,
					 SomFluidProperties sfProperties, SomFluidTask sfTask, long serialID) throws Exception {

		this.sfProperties = sfProperties;
		this.sfTask = sfTask;
		this.sfFactory = sfFactory;
		this.somHost = somhost;
		 
		somDataObj = somHost.getSomDataObj() ;
		
		out = somhost.getSomFluid().getOut() ;
		
		prepareAstor();
	}
	
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	// ========================================================================	

	
	public void prepareAstor() throws Exception{
		
		// properties contains DB properties
		somDataObj = SomDataObject.openSomData(sfProperties);
		// now activate listener, router and receiver
		
		if (somDataObj==null){
			somDataObj = SomDataObject.loadSomData(sfProperties);
		}
		somDataObj.establishDataLinkage();
		nodesinformer = new NodesInformer ();
		
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		LatticePreparation latticePreparation = new LatticePreparation( this, somHost,sfProperties);
		latticePreparation.setNodesInformer( nodesinformer ) ;
		
		somLattice = latticePreparation.getLattice();
		
		astorThrd = new Thread(this,"astorThrd") ;
	}
	
	

	@SuppressWarnings("unchecked")
	public void prepare( ArrayList<Integer> usedVariables) throws Exception{
		
		int ix;
		String tvlabel = null;
		ArrayList<Double> usageVector =new ArrayList<Double>();
		
		Variables variables = somDataObj.getVariables() ;
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
		
		if (somLattice.getNodes().get(0).getExtensionality().getCount()>0){
			somLattice.clear();
		}
		
		ArrayList<Variable> varin, vari = somDataObj.getVariableItems();
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
		
		somLattice.setSomData(somDataObj) ; 
		somLattice.spreadVariableSettings(); // informs all nodes about the usevector

	}

 
	private void init(Variables variables) {

		initializeNodesWithRandomvalues( variables ); 
	}


	protected void initializeNodeWithRandomValues( int nodeindex ){
		
		Variables variables = somDataObj.getVariables() ;
		ArrayList<String> uv = variables.getLabelsForVariablesList(variables) ;	
		
    	somLattice.getNode(nodeindex).onDefiningFeatureSet( (Object)sob.encode( (Object)uv ),null);
    	
    	
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
	    
	    int cc = somDataObj.getNormalizedDataTable().getColcount();
	    int rc = somDataObj.getNormalizedDataTable().getRowcount();
	    // 
	    // PCA pca; is in: import org.NooLab.somscreen.linear.PCA;
	    SimpleExplorationClustering sec = new SimpleExplorationClustering( somDataObj.getNormalizedDataTable() ) ;
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
		 					
	}

	private void perform() throws Exception {
		
		dSom = new DSom( this, somDataObj, somLattice, sfTask );
		
		dSom.performAstor();
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
		
		while (isRunning==true){
			
			PrintLog.Delay(1000);
		}
	}
	
	@Override
	public void onSelectionRequestCompleted(Object results) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActionAccepted(int action, int state, Object param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void statusMessage(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculationsCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SomDataObject getSomDataObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNeighborhoodNodes(int index, int surroundN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SomFluidAppGeneralPropertiesIntf getSfProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LatticePropertiesIntf getLatticeProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualLattice getSomLattice() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> getUsedVariablesIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Observable dsomInstance, Object results) {
		 
		
	}
	

}

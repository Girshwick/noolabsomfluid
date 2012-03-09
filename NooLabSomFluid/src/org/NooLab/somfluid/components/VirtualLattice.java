package org.NooLab.somfluid.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import java.util.Random;



import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.components.data.IndexDistanceIntf;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivity;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamics;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurface;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.similarity.Similarity;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.nodes.LatticeIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.env.communication.LatticeFutureVisorIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.ArrUtilities;
 
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.math.array.StatisticSample;




public class VirtualLattice implements LatticeIntf{

	public static final double __DEFAULT_NODE_INIT_STDEV = 0.16;

	LatticePropertiesIntf latticeProperties;

	ArrayList<MetaNode> nodes = new ArrayList<MetaNode>();
	
	Map<Long, Integer> nodeIndexMap = new TreeMap<Long, Integer>() ;
	
	SomFluid somFluidParent;
	
	SomDataObject somData;
	
	OpenLatticeFutures openLatticeFutures = new OpenLatticeFutures();
	
	ArrayList<SurroundResults> selectionResultsQueue = new ArrayList<SurroundResults>(); 
	SelectionResultsQueueDigester selectionResultsQueueDigester;
	
	Map<String, Object> selectionResultsQueryMap = new HashMap<String, Object>()  ;
	
	private ModelProperties  modelProperties; 
	
	// ------------------------------------------
	// initialize imports of external structural components
	// the import objects will act as simple factories and instantiate the classes
	// important: no semantics in constructing them = no parameters for a general interface
	
	/*
	 * these components refer to the lattice as a whole ! not to the nodes...
	 * i.e., they reflect the lattice AS a node,;
	 * the nodes inside this lattice have their own instances, since these components store values individually
	 */
	SimilarityIntf similarityConcepts = new Similarity();  
	IntensionalitySurfaceIntf intensionalitySurface = new IntensionalitySurface();
	ExtensionalityDynamicsIntf extensionalityDynamics ;
	MetaNodeConnectivityIntf nodeConnectivity = new MetaNodeConnectivity() ;
	
	// ------------------------------------------
	
	int latticeQuery = 0;
	
	// ------------------------------------------
	
	int surroundError=0;
	
	StatisticSample statsSampler;
	Random rndInstance = new Random();
	
	ArrUtilities arrutil = new ArrUtilities (); 
	PrintLog out ;

	private double averagePhysicalDistance = 1.0;

	public boolean bmuBufferActivated = false;

	private double initialRandomDivergence = 0.2;
	private double stDevForNodeInitialization = __DEFAULT_NODE_INIT_STDEV ;
	
	
	// ========================================================================
	public VirtualLattice(SomFluid parent, LatticePropertiesIntf latticeProps){
	 
		latticeProperties = latticeProps;
		somFluidParent = parent;
		
		somData = somFluidParent.getSomDataObject() ; 
		
		extensionalityDynamics = new ExtensionalityDynamics(somData) ; 
		
		// ..........................................................
		out = parent.getOut();
		
		selectionResultsQueueDigester = new SelectionResultsQueueDigester() ;
		out.delay(60);
		
		/*
		 * the properties of the lattice if used/interpreted as a model
		 */
		modelProperties = new ModelProperties() ; 
		
		int seed = 9357;
		rndInstance.setSeed( seed );
		statsSampler = new StatisticSample( seed ) ;
		
		
	}
	// ========================================================================

	
	/**
	 * 
	 * this method returns a list of particles within the diameter that is defined at a given stage of learning;
	 * nothing is done so far about the lateral control mechanism
	 * 
	 * the updating callback from ParticleField arrive in SomFluid, 
	 * hence we have to switch into the reverse facade
	 * 
	 */
	@Override
	public ArrayList<IndexDistanceIntf> getNeighborhoodNodes( int index, int nodeCount ) {
		
		ArrayList<IndexDistanceIntf> selectedNodes = new ArrayList<IndexDistanceIntf>();
		
	
		/*
		 * ONLY FOR DEBUG TO PREVENT PARALLEL CALLS
		 */
			if (latticeQuery>0){
				return selectedNodes;
			}
			latticeQuery = 1;
		/*
		 * 
		 */
		
		// forking the request immediately to its own name space  
		selectedNodes = (new ParticleSelectionQuery()).getNodes( index, (int) (nodeCount*1.3) );
		
		 
		return selectedNodes;
	}
	
	// ..........................................
	
	/**
	 * queries can take very different amounts of time, thus we need a GUID identifier for correct returns 
	 *  
	 * 
	 */
	class ParticleSelectionQuery{

		SurroundResults results;
		String queryGuid  ;
		
		public ParticleSelectionQuery() {
			 
		}

		public ArrayList<IndexDistanceIntf> getNodes(int index, int surroundN) {
			 
			ArrayList<IndexDistanceIntf> particlesIntf = new ArrayList<IndexDistanceIntf>();
			ArrayList<IndexDistance> particles = new ArrayList<IndexDistance>();
			
			// TODO define selection size, otherwise the field will take the default !!!
			// this call returns immediately, providing the GUID as issued by the RepulsionField
			queryGuid = somFluidParent.getNeighborhoodNodes( index ,surroundN);  // XXX
			
			// putting this to a map <guid,null>, the matching result object will contain the same Guid
			selectionResultsQueryMap.put(queryGuid,null) ;
			
			// now waiting here
			int z=0;
			while ((z<2000) && (selectionResultsQueryMap.get(queryGuid)==null)){ // (z<300) && // activate for NON _DEBUG abc124
				minidelay(10); 
				z++;
			}
			
			if (selectionResultsQueryMap.containsKey(queryGuid)){
				results = (SurroundResults) selectionResultsQueryMap.get(queryGuid);
			}else{
				// create an empty dummy
				return particlesIntf;
			}
			
			if ((results!=null) && (results.getParticlesAsIndexedDistances()!=null)){
				particles = results.getParticlesAsIndexedDistances();
				particlesIntf = new ArrayList<IndexDistanceIntf>( particles );
			}else{
				out.printErr(3, "retrieval of surround for index <"+index+"> was unexpectedly empty.");
				surroundError++;
			}
			
			results = null;
			latticeQuery = 0;
			return particlesIntf;
		}
	
		@SuppressWarnings("static-access")
		public void minidelay(int nanos){
			try {
				Thread.currentThread().yield();
				Thread.currentThread().sleep(0,nanos);
			} catch (Exception e) {}
		}
	} // inner class ParticleSelectionQuery
	
	// ..........................................
	
	
	// ------------------------------------------------------------------------
	public int size(){
		return nodes.size();
	}
	public void clear(){
		nodes.clear();
	}
	
	public MetaNode getNodeByNumId( long nodeID ){
		
		MetaNode node= null, _node;
		
		// TODO: use a TreeMap for this instead...
		for (int i=0;i<nodes.size();i++){
			_node = nodes.get(i);
			if (_node.getNumID()==nodeID){
				node = _node;
				break;
			}
		}
		
		return node;
	}

	public MetaNode getNodeBySerialId( long serid ){
		
		MetaNode node= null, _node;
		
		// TODO: use a TreeMap for this instead...
		for (int i=0;i<nodes.size();i++){
			_node = nodes.get(i);
			if (_node.getSerialID()==serid){
				node = _node;
				break;
			}
		}
		
		return node;
	}

	
	public MetaNode getNode( int index ){
		return nodes.get(index) ;
	}
	
	public ArrayList<MetaNode> getNodes() {
		return nodes;
	}


	public void indexOf( MetaNode node){
		nodes.indexOf(node);
	}
	
	public int indexOfSerial( long serialID ){
		// it is indeed monotonely increasing, though not starting with 1 and not without gaps;
		// -> We can check the middle position of the array, recursively
		// or we use a map.
		
		int nix = -1;
		
		if ((nodeIndexMap!=null) && (nodeIndexMap.containsKey(serialID))){
			nix = nodeIndexMap.get(serialID) ;
		}
		
		return nix;
	}
	
	public void addNode( MetaNode node){
		
		node.setLatticeProperties(latticeProperties);
		nodes.add(node) ;
	}
	
	public void removeNode( int index ){
		nodes.remove(index) ;
	}
	public void removeNodes( int[] index ){
		
	}
	public void removeNodesBeyondIndex( int index ){
		
		for (int i=index;i<nodes.size();i++){
			nodes.remove(i) ;
		}
	}
	public void removeNodes( ArrayList<Integer> index ){
		
	}
	 
	public void setNode(){
		
	}


	public int getLatticeQuery() {
		return latticeQuery;
	}


	public Random getRndInstance() {
		return rndInstance;
	}


	public void setAveragePhysicalDistance(double dValue) {
		//  
		averagePhysicalDistance = dValue;
	}
	public double getAveragePhysicalDistance() {
		return averagePhysicalDistance;
	}
	public SimilarityIntf getSimilarityConcepts() {
		return similarityConcepts;
	}


	public int getSurroundError() {
		return surroundError;
	}


	public void setSurroundError(int surroundError) {
		this.surroundError = surroundError;
	}


	public PrintLog getOut() {
		return out;
	}


	private MetaNode getBySerial(long nodeSerialID){
		MetaNode node=null;
		
		// TODO
		return node;
	}
	
						// registering call back LatticeFutureVisor 
	public String openLatticeFuture( LatticeFutureVisorIntf visor , int taskId, long nodeSerialID) {
		
		String guid = GUID.randomvalue() ;
		/*
		ArrayList<LatticeFutureVisorIntf>  superVisors 
		
		 */
		LatticeFuture f = new LatticeFuture(guid, nodeSerialID );
		
		openLatticeFutures.add(f);
		
		getBySerial(nodeSerialID).takeLatticeFuture(guid,taskId);
		
		return guid;
	}
	
	public String openLatticeFuture( LatticeFutureVisorIntf visor , int taskId ) {

		String guid = GUID.randomvalue() ;
		
		LatticeFuture f = new LatticeFuture(guid, nodes.size() );
		
		// now we have to link the particular callback LatticeFutureVisorIntf (its a dedicated object)
		// to this LatticeFuture object (which stores the parameters for counting down)
		f.registerCallbackInterests(visor) ;
		
		openLatticeFutures.add(f);
		
											out.print(4, "opening a new Lattice Future for all nodes, guid = "+guid);  
		
		// this guid is issued by the lattice, which knows about it
		// the lattice will provide this guid to all nodes, each of which maintain a FiFo list of such GUIDs
		// once they have finished, the send a signal to the lattice, which counts down -1 for each node,
		// if the count down arrives at 0, it will release the event to here
		
		for (int i=0;i<this.nodes.size();i++){
			nodes.get(i).takeLatticeFuture(guid, taskId);
		}
		
		return guid;
	}


	public void nodeInformsAboutCompletedTask(String guid) {
		// count down for the appropriate future, need to be in its own small object space, $
		// since the calls are unpredictable and parallel
		
		new LatticeFutureCountDown(guid); 

		
	}

	class LatticeFutureCountDown{
		
		public LatticeFutureCountDown(String guid){
			
			LatticeFuture f;
			
						 
			
			f = openLatticeFutures.getByGuid(guid);
			
			if (f==null){
				out.printErr(2, "   --- --- --- LatticeFutureCountDown, guid not found: "+guid);
				out.print(3, "   --- --- --- length of guid list : "+(openLatticeFutures.items.size())+" ,  "+openLatticeFutures.getItemsStr());
				return;
			}
			f.nCount = f.nCount - 1 ;
											out.print(4, "task completion count down (LatticeFuture), n="+f.nCount+" , guid: "+guid );
			if (f.nCount<=0){
				// call back to the "LatticeFutureVisor" object
				f.sendCompletionEvent();
				// openLatticeFutures.removeByGuid(guid) ;
			}
		}
		

	}

	// the event arrives in SomFluid, which is calling this method here
	public void digestParticleSelection( SurroundResults results ) {
		// SurroundResults contains particle indexes, distances to request center, and the request GUID !
		
		String str;
		// str = arrutil.arr2text( results.getParticleIndexes() ) ;
		// out.print(2,"results returned to virtual lattice, "+str);
		
		new ParticleSelectionDispatcher( results );
		 
	}
	
	public ArrayList<SurroundResults> getSelectionResultsQueue() {
		return selectionResultsQueue;
	}

	public LatticePropertiesIntf getLatticeProperties() {
		return latticeProperties;
	}


	public void setLatticeProperties(LatticePropertiesIntf latticeProperties) {
		this.latticeProperties = latticeProperties;
	}


	public ModelProperties  getModelProperties() {
		return modelProperties;
	}


	public void setModelProperties(ModelProperties modelProperties) {
		this.modelProperties = modelProperties;
	}


	public void stop(){
		selectionResultsQueueDigester.isRunning = false;
	}
	public boolean selectionResultsQueueDigesterAlive(){
		boolean hb = (selectionResultsQueueDigester!=null) && (selectionResultsQueueDigester.isRunning) ; 
		       	
		if (hb){
			hb = (selectionResultsQueueDigester.vslSelectionDigest.isAlive()) ;
		}
		
		return hb ;
	}
	public void startSelectionResultsQueueDigester(){
		selectionResultsQueueDigester = new SelectionResultsQueueDigester() ;
	}
	/**
	 * 
	 * this class is the backbone for the acceptance of messages issued by the RepulsionField about
	 * the selected indexes of nodes
	 * 
	 */
	class SelectionResultsQueueDigester implements Runnable{

		boolean isRunning =false, isWorking=false;
		
		Thread vslSelectionDigest;
		
		SurroundResults _results ;
		
		public SelectionResultsQueueDigester(){
		
			vslSelectionDigest = new Thread (this,"vslSelectionDigest");
			vslSelectionDigest.start() ;
		}
		@Override
		public void run() {
			isRunning = true;
			int dt;
			isWorking=false;
			
			selectionResultsQueue = new ArrayList<SurroundResults>();
			
			try{
				while (isRunning){
					
					if (selectionResultsQueue.size()>0){
						out.print(5, "... selections waiting : "+selectionResultsQueue.size());
					}
					if (isWorking==false){
						isWorking = true;
						
						try{
							
							if (selectionResultsQueue.size()>0){
								_results = selectionResultsQueue.get(0) ;
								
								digestParticleSelection(_results) ;
								
								selectionResultsQueue.remove(0) ;
							}
						}catch(Exception e){}
						
						isWorking = false;
					} // isWorking ?
					
					if (selectionResultsQueue.size()==0){
						dt = 2 ;
					}else{
						dt = 0 ;
					}
					out.delay(dt);
				}// -> isRunning?
				
			}catch(Exception e){
				
			}
			
		}
		
	}
	
	class ParticleSelectionDispatcher{

		String guidStr;
		
		public ParticleSelectionDispatcher(SurroundResults results) {
			
			guidStr = results.getGuid();
			
											out.print(5, "   - - - lattice is now checking match for results by guid="+guidStr);
			
			if (selectionResultsQueryMap.containsKey(guidStr)){
				// we may even introduce a local selection buffer here, too !
				selectionResultsQueryMap.put(guidStr, results);
				// the map serves as a queue for named items !
											out.print(5, "   - - - ...match found!");
			}else{
				out.printErr(4, "   - - - lattice could not qualify results-guid = "+guidStr);	
			}
		}
		
	}
	 
	// ========================================================================
	
	public SimilarityIntf distributeSimilarityConcept() {
		 
		return similarityConcepts;
	}

	public SimilarityIntf distributeSimilarityConcept( long serialID ) {

		IntensionalitySurfaceIntf intensionSurf = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			similarityConcepts = nodes.get(nodeIndex).getSimilarity();
		}
		if (similarityConcepts==null){
			similarityConcepts =  new Similarity() ;
		}
		
		return similarityConcepts;
		 
	}

	public IntensionalitySurfaceIntf distributeIntensionalitySurface() {
		 
		
		return intensionalitySurface;
		
	}

	public IntensionalitySurfaceIntf distributeIntensionalitySurface( long serialID ) {
		
		IntensionalitySurfaceIntf intensionSurf = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			intensionSurf = nodes.get(nodeIndex).getIntensionality();
		}
		if (intensionSurf==null){
			intensionSurf = new IntensionalitySurface();
		}
		
		return intensionSurf;
		
	}

	
	public ExtensionalityDynamicsIntf distributeExtensionalityDynamics() {

		return extensionalityDynamics;
	}

	public ExtensionalityDynamicsIntf distributeExtensionalityDynamics(long serialID) {
		ExtensionalityDynamicsIntf extensiony = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			extensiony = nodes.get(nodeIndex).getExtensionality() ;
		}
		if (extensiony==null){
			extensiony = new ExtensionalityDynamics(somData);
		}
		
		return extensiony;
	}

	public MetaNodeConnectivityIntf distributeNodeConnectivity() {

		return nodeConnectivity;
	}

	public MetaNodeConnectivityIntf distributeNodeConnectivity(long serialID) {

		MetaNodeConnectivityIntf nodeconnexy = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			nodeconnexy = nodes.get(nodeIndex).getMetaNodeConnex() ;
		}
		if (nodeconnexy==null){
			nodeconnexy = new MetaNodeConnectivity();
		}
		
		return nodeconnexy;
	}


	public void activateNodes() {
		//
		for (int i=0;i<nodes.size();i++){
			nodes.get(i).setActivation(1);
		}
		
	}

	/**
	 * 
	 * this calculates the similarity of all records to profile 
	 * 
	 */
	public void calculateInternals() {
		 
		for (int i=0;i<this.nodes.size();i++){
			nodes.get(i).evaluateExtensions() ;
			
		}// i-> all nodes
		
		
		
	}


	public double getInitialRandomDivergence() {
		return initialRandomDivergence;
	}

	public void setInitialRandomDivergence(double initialRndDiv) {
		this.initialRandomDivergence = initialRndDiv;
	}


	public double getStDevForNodeInitialization() {
		return stDevForNodeInitialization;
	}


	public void setStDevForNodeInitialization(double stDevForNodeInitialization) {
		this.stDevForNodeInitialization = stDevForNodeInitialization;
	}


	public StatisticSample getRndSamplerInstance() {
		return statsSampler;
	}

	
	
		
}












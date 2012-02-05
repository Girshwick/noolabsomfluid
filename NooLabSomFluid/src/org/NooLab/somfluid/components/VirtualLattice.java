package org.NooLab.somfluid.components;

import java.util.ArrayList;

import java.util.Random;



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
import org.NooLab.somfluid.core.nodes.LatticeIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.env.communication.LatticeFutureVisorIntf;
import org.NooLab.utilities.ArrUtilities;
 
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;




public class VirtualLattice implements LatticeIntf{

	private ArrayList<MetaNode> nodes = new ArrayList<MetaNode>();
	
	LatticePropertiesIntf latticeProperties;

	
	SomFluid somFluidParent;
	
	SomDataObject somData;
	
	OpenLatticeFutures openLatticeFutures = new OpenLatticeFutures();
	
	Random rndInstance = new Random();
	
	// ------------------------------------------
	// initialize imports of external structural components
	// the import objects will act as simple factories and instantiate the classes
	// important: no semantics in constructing them = no parameters for a general interface
	SimilarityIntf similarityConcepts = new Similarity();  
	IntensionalitySurfaceIntf intensionalitySurface = new IntensionalitySurface();
	ExtensionalityDynamicsIntf extensionalityDynamics ;
	MetaNodeConnectivityIntf nodeConnectivity = new MetaNodeConnectivity() ;
	// ------------------------------------------
	
	ArrUtilities arrutil = new ArrUtilities (); 
	PrintLog out ;
	
	// ========================================================================
	public VirtualLattice(SomFluid parent, LatticePropertiesIntf latticeProps){
	 
		latticeProperties = latticeProps;
		somFluidParent = parent;
		
		somData = somFluidParent.getSomDataObject() ; 
		extensionalityDynamics = new ExtensionalityDynamics(somData) ;
		 
		rndInstance.setSeed(1234);
		
		out = parent.getOut();
		
	}
	
	/**
	 * 
	 * this method returns a list of particles within a the diameter that is defined at a given stage of learning;
	 * nothing is done so far about the lateral control mechanism
	 * 
	 */
	@Override
	public ArrayList<IndexedDistances> getNeighborhoodNodes( int index ) {
		// the updating callback from ParticleField arrive in SomFluid, hence we have to switch into the reverse facade
		
		somFluidParent.getNeighborhoodNodes( index );
	
		
		
		
		return null;
	}
	// ========================================================================
	
	
	
	
	
	// ------------------------------------------------------------------------
	public int size(){
		return nodes.size();
	}
	public void clear(){
		nodes.clear();
	}
	public MetaNodeIntf getNode( int index ){
		return nodes.get(index) ;
	}
	
	public void indexOf( MetaNode node){
		nodes.indexOf(node);
	}
	
	public void indexOfSerial( long serialID ){
		// it is indeed monotonely increasing, though not starting with 1 and not without gaps;
		// -> We can check the middle position of the array, recursively
		// or we use a map.
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


	public Random getRndInstance() {
		return rndInstance;
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
				out.print(2, "   --- --- --- length of guid list : "+(openLatticeFutures.items.size())+" ,  "+openLatticeFutures.getItemsStr());
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

	public void digestParticleSelection(SurroundResults results) {
		// SurroundResults contains particle indexes, distances to request center, and the request GUID !
		
		String str;
		str = arrutil.arr2text( results.getParticleIndexes() ) ;
		out.print(2,"results returned to virtual lattice, "+str);
	}
	
	// ========================================================================
	
	public SimilarityIntf distributeSimilarityConcept() {
		 
		return similarityConcepts;
	}

	public IntensionalitySurfaceIntf distributeIntensionalitySurface() {
		 
		return intensionalitySurface;
	}
	public ExtensionalityDynamicsIntf distributeExtensionalityDynamics() {

		return extensionalityDynamics;
	}

	public MetaNodeConnectivityIntf distributeNodeConnectivity() {

		return nodeConnectivity;
	}
	
	  
		

	

	
	
}

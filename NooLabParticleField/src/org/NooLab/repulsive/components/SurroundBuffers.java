package org.NooLab.repulsive.components;

import java.util.ArrayList;

import org.NooLab.chord.CompletionEventMessageCallIntf;
import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;
import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldIntf;
import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.components.infra.PhysicsDigester;
import org.NooLab.repulsive.components.topo.Surround;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.repulsive.particles.ParticlesIntf;
import org.NooLab.utilities.logging.PrintLog;




/**
 * 
 * SurroundBuffers updates all particles in a continuously running background process
 * while the buffer is essentialy independent and not contained as an object in the particles,
 * each particle contains a pointer to its buffer;  
 * the buffer itself knows whether it is available or not., thus, we can profit even if only part
 * of the whole population has an updated buffer ;
 * the surroundbuffer objects also maintain the last known frozen coordinate,  
 * 
 * 
 *  
 * 
 */
public class SurroundBuffers implements Runnable{

	ArrayList<SurroundBuffer>  bufferItems = new ArrayList<SurroundBuffer>(); 
	
	Neighborhood neighborhood;
	RepulsionField parentField;
	LimitedNeighborhoodUpdate limitedAreaUpdate;
	
	boolean bufferingSwitchedOff=false;
	boolean switchedOFF = false;
	int updating=0;
	
	boolean updateIsPending=false;
	boolean fieldIsStable=false ;
	int startingIndex=0;
	boolean surroundSizeChanged=false;
	int selectionSize=0 ;
	
	Plane xyPlane;
	ParticlesIntf particles; 
	
	// ----------------------------------
	
	SurroundCalcDigester multicalc ;
	
	
	// ----------------------------------
	
	boolean anythingChanged=true;
	boolean isRunning=false;
	Thread sbsThrd;
	PrintLog out;
	
	public SurroundBuffers( RepulsionField parentfield, PrintLog outprn){
	
		parentField = parentfield;
		 
		particles = parentField.getParticles() ;
		
		limitedAreaUpdate = parentField.getLimitedAreaUpdate() ;
		out = outprn;
		init();
	}
	
	// this creates a collection that runs PARALLEL to the particles collection -> care about deletion, adding !! 
	private void init(){
		
		for (int i=0;i<particles.size();i++){
			introduceParticle(i);
		}

		sbsThrd = new Thread (this,"sbsThrd") ;
		bufferingSwitchedOff = true;
	}
	
	public void introduceParticles(){
		for (int i=0;i<particles.size();i++){
			introduceParticle(i);
		}
	}

	public void introduceParticle(int particleIndex){
		SurroundBuffer sb ;
		
		sb = new SurroundBuffer( particleIndex , this);
		bufferItems.add(sb) ;
		
		particles.get(particleIndex).registerBufferReference(sb);
		
	}
	
	
	/**
	 * call this after initial calculation of the field has been completed for the first time
	 */
	public void start(){
		try{
			if (isRunning==false){
				// retrieving the selection size just on first start... such we can detect 
				// change of selection size on the global level
				selectionSize = parentField.getSelectionSize() ;
				
				bufferingSwitchedOff = true;
			
				sbsThrd.start() ;
			} 
		}catch(Exception e){
			
		}
		
	}
	
	public void stop() {
		isRunning = false;
		anythingChanged = false;
		switchedOFF = true;
	}
	
	public void setToPause( int flag) {

		switchedOFF = flag>=1;
		
	}

	public void update(){
		// update the buffers only if the requested size is larger than the current buffers  
		if (selectionSize < parentField.getSelectionSize()){
			anythingChanged = true;
		}
	}
	// ========================================================================
	
	@Override
	public void run() {
		isRunning=true;
		boolean isWorking=false;
		try{
			
			while (isRunning){
				
				if ((isWorking==false) && (fieldIsStable)){
					isWorking=true;
					
					if ((anythingChanged) && (switchedOFF==false)){
						if (parentField.isMultiProc()){
							//performParallel(); 
							// parallel mode returns completely weird surrounds,
							// ... sth in neighborhood is not thread safe...
							perform();
						}else{
							//performParallel();
							perform();
						}
					}
					
					isWorking=false;
				}// isWorking?
				int z=0;
				while ((fieldIsStable) && (z<100)){
					out.delay(100);
				}
			}// ->
			
		}catch(Exception e){
			
		}
	}
	
	private void performFor( int index ){
		
		int pointIndex;
		Particle particle;
		int[] surrounding;
		double[] surroundDistances;
		SurroundBuffer sb;
		Surround surround ;
		
		
		particle = particles.get(index);
		if ((particle==null) || (switchedOFF) || (particle.isFrozen())){
			return;
		}
		
		boolean accessForUpdate = true;
		if ((limitedAreaUpdate!=null) && (limitedAreaUpdate.centerpointsOfChangedRegion.size()>0)){
			pointIndex = limitedAreaUpdate.checkParticle( particle ) ;
			accessForUpdate = pointIndex>=0;
			limitedAreaUpdate.remove(pointIndex) ;
		}
		
		if (accessForUpdate == false){
			return;
		}
		// getting the indexes of the surrounding particles
		surround = new Surround(parentField);
		
		surrounding = surround.getGeometricSurround( index, selectionSize, Surround._CIRCLE);
		surroundDistances = surround.getParticleDistances();

		// retrieving the reference of the buffer that is attached to
		// the Particle of index i
		sb = particle.getSurroundBuffer();
		if (sb == null) {
			particle.registerBufferReference(new SurroundBuffer( index, this));
			sb = particle.getSurroundBuffer();
		}
		sb.clear();
		if (surrounding != null) {
			// updating that buffer element
			sb.index = index;
			sb.importSurrounding(surrounding, surroundDistances);
			sb.parent = this;
		}
		sb.index = index;
		
		if (index>bufferItems.size()-1){
			bufferItems.add(sb);
		}else{
			bufferItems.set(index, sb);
		}
		surround = null;
	}
	
	
	private void performParallel(){
	
		
	
		if (selectionSize <= 0) {
			return;
		}
		try {
			out.print(3, "updating the surround buffers...");
			bufferingSwitchedOff = true;
			surroundSizeChanged = selectionSize != parentField.getSelectionSize();
			selectionSize = parentField.getSelectionSize();
	
			if (startingIndex >= particles.size() - 1) {
				startingIndex = 0;
			}
			// i = Math.max(0, startingIndex);
			anythingChanged=false; // blocking repeated call
			
			if (multicalc==null){
				out.print(3, "multi processing digester has been started ...");
				multicalc = new SurroundCalcDigester(this);
			} 
			
			int threadcount = 1 ; //  NeighborHood is not yet thread-safe...
			multicalc.baseDataChanged = true;
			
			multicalc.doParallelSurroundCalc( particles, threadcount);
	
			out.print(3, "updating the surround buffers (p-mode:"+threadcount+") has been completed");
			
			anythingChanged=false;
	
			bufferingSwitchedOff = false;
		} catch (Exception e) {
			anythingChanged=true;
		}
	}

	/**
	 * 
	 * we simply determine the set of selected neighbors for all particles 
	 * 
	 */
	private void perform(){
		 
		int i=0;
		 
		if ((selectionSize<=0) || (switchedOFF)){
			return;
		}
		try{
											out.print(3,"updating the surround buffers...");
		bufferingSwitchedOff = true;
		surroundSizeChanged = selectionSize != parentField.getSelectionSize() ;
		selectionSize = parentField.getSelectionSize() ;
		updating=1;
		
		if (startingIndex>=particles.size()-1){startingIndex=0;}
		i = Math.max( 0,startingIndex) ;
		
			// "fieldIsStable" mirrors frozen state
			while ((i < particles.size()) && (fieldIsStable)) {

				performFor( i );
				
				i++;
			}

			if (i < particles.size() - 2) {
				out.print(2,"updating the surround buffers has been interrupted (at index pos. "+i+" of "+particles.size()+")");
				startingIndex = Math.max(0, i - 1);

			} else {
				out.print(2, "updating the surround buffers has been completed");
				anythingChanged = false;
				startingIndex = 0;
				selectionSize = parentField.getSelectionSize();
				updating=0;
			}
		} catch (Exception e) {
			out.printErr(2, "\ncritical error in sbs.perform() on index = " + i
					+ "\n");
			e.printStackTrace();
		} finally {
			bufferingSwitchedOff = false;
		}
	}
	
	
	private void clearBuffers(){
		
		for (int i=0;i<bufferItems.size();i++){
			if (bufferItems.get(i).parent==null){
				bufferItems.get(i).parent=this;
			}
			bufferItems.get(i).clear(i) ;
		}
	}

	public void registerNeighborhood(Neighborhood nb){
		neighborhood = nb;
		xyPlane = neighborhood.getXyPlane();
		
		particles = parentField.getParticles();
	}
	
	// ========================================================================
	
	public void setFieldFrozenMessage( boolean flag){
		
		fieldIsStable = flag;
		 
		out.print(4,"SurroundBuffers received message about frozen state, frozen="+flag);
		
		if (flag==false){
			anythingChanged=true;
			clearBuffers();
		}
		if (flag==true){
			fieldIsStable = true; 
		}
	}
	
	
	
	
	// ========================================================================
	
	public void importIndexDistance( int particleIndex, ArrayList<IndexDistance> indexedDistances){
		
		SurroundBuffer sb;
		
		sb = particles.get(particleIndex).getSurroundBuffer();
		 
		sb.clear();
		if ((indexedDistances != null) && (indexedDistances.size()>0)) {
			// updating that buffer element
			sb.index = particleIndex;
			sb.importSurrounding(indexedDistances);
			
			sb.parent = this;
		}
		
	}
	
	public void importBufferItems(	ArrayList<SurroundBuffer> sbs){
	
		
	}
	
	public void clear(){
		SurroundBuffer sb;
		
		for (int i=0;i<bufferItems.size();i++){
			sb = bufferItems.get(i) ;
			sb.clear(i) ;
		}
		bufferItems.clear();
	}
	
	 
	public void setParticlesIndexList( int particleIndex ){
		
	}
	public void setParticlesDistanceList( int particleIndex ){
		
	}

	public boolean bufferIsAvailable(int index, int surroundSize) {
		boolean rB=false;
		SurroundBuffer sb;
		Particle particle;
		
		rB = (selectionSize >= surroundSize);
		if (rB){
			rB=false;
			
			if ((index < bufferItems.size()) &&(index>=0)){
				sb = bufferItems.get(index);
				if (sb.index == index) {
					particle = particles.get(index);
					if (particle.getSurroundBuffer() == sb) {
						if (sb.indexes.length >= surroundSize) {
							rB = true;
						}
					}
				} else {
					// msg about mismatch of indices
				}
			}// bufferItems.size ?
		}
		
		return rB;
	}

	public ArrayList<IndexDistance> exportBuffer(int index, int surroundSize, String guidStr){
	
		ArrayList<IndexDistance> indexedDistances = new ArrayList<IndexDistance>() ;
		int ix;
		double dv;
		SurroundBuffer sb;
		IndexDistance ixDist;
		
		sb = bufferItems.get(index) ;
		
		out.print(3, "SurroundBuffers(), exporting buffer into <indexedDistance> ...  ");
		
		int k = sb.indexes.length ;
		if (k>parentField.getSelectionSize() ){
			k=parentField.getSelectionSize() ;
		}
		for (int i=0;i<k;i++){
			
			ix = sb.indexes[i];
			dv = sb.distances[i];
			ixDist = new IndexDistance(ix,dv, guidStr);
			
			indexedDistances.add(ixDist) ;
		}
		
		out.print(3, "SurroundBuffers(), exporting buffer into <indexedDistance> completed.  ");
		return indexedDistances;
	}

 
	public void informAboutNecessaryUpdate( int[] indexes ) {
		// ArrayList<Integer> triggeredItems;
		
		// triggeredItems = (ArrayList<Integer>) indexes.clone() ;
		if (indexes==null){
			return;
		}
		
		for (int i=0;i<indexes.length;i++){
			
		}// i->
	}

	public int getStartingIndex() {
		return startingIndex;
	}

	public void setStartingIndex(int startingIndex) {
		this.startingIndex = startingIndex;
	}

	public boolean getBufferingSwitchedOff() {
		return bufferingSwitchedOff;
	}

	public void setBufferingSwitchedOff(boolean flag) {
		this.bufferingSwitchedOff = flag;
	}
	
	

	public int getUpdating() {
		return updating;
	}



	public boolean updateIsPending() {
		 
		return updateIsPending;
	}



	public void setUpdateIsPending(boolean updateIsPending) {
		this.updateIsPending = updateIsPending;
	}



	public class SurroundCalcDigester implements IndexedItemsCallbackIntf{
		 
		MultiDigester digester=null ;
		 
		//Vector<String>  rowText ;
		ParticlesIntf pparticles;
		boolean baseDataChanged=false;
		boolean activated=false;
		
		SurroundBuffers sbs;
		
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
		public SurroundCalcDigester( SurroundBuffers parent ){
			
			this.sbs = parent ;
			
		}
		
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
		
		public void doParallelSurroundCalc(  ParticlesIntf particles, int threadcount){ 
			 
			
			// providing also right now the callback address (=this class)
			// the interface contains just ONE routine: perform()
			if ((digester==null) || (baseDataChanged) ){
				
				
				digester = new MultiDigester(threadcount, (IndexedItemsCallbackIntf)this ) ;
				digester.setPriority(7);
				
				  
				this.pparticles = particles;
			 
				digester.prepareItemSubSets( pparticles.size(),0 );
			 
			}else{
				 
				digester.reset();
			}

			
			digester.execute() ;
			activated=true;
			
		}
		
		 
		 
		// this will be called back, the multi-threaded digester selects an id, which is called from within one of the threads
		// the processID is just for fun... (and supervision)
		public void perform( int processID, int id ) {
 
			performFor( id );
			
			// out.print(2, "spp("+processID+") - id : "+id);
			
			if (fieldIsStable==false){
				digester.stopAll();
			}
		} 
		
		public void setBaseDataChanged(boolean flag) {
			this.baseDataChanged = flag;
		}

		public boolean isActivated() {
			return activated;
		}

	} // inner class Digester



	
}

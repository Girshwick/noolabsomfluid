package org.NooLab.repulsive.components;

import java.util.ArrayList;


import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;
import org.NooLab.repulsive.RepulsionFieldCore;
 
import org.NooLab.repulsive.components.topology.Surround;
import org.NooLab.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.datatypes.IndexDistance;
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

	String parentName = "";
	
	// RepulsionFieldCore parentField;
	RepulsionFieldBasicIntf parentField; 
	// RepulsionFieldSelectionIntf parentField;

	Neighborhood neighborhood;
	LimitedNeighborhoodUpdate limitedAreaUpdate;
	
	SurroundExtension surroundExtension;
	
	SurroundRetrievalObserverIntf  parentFieldObserver ;
	
	// preventing collisions between requests to the facade and its updating
	int bufferPositionBeingUpdated = -1;
	
	boolean bufferingSwitchedOff=false;
	boolean switchedOFF = false;
	int updating=0;
	boolean allBuffersUpdating;
	
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
	public PrintLog out;
	
	
	public SurroundBuffers( RepulsionFieldCore parentfield, Particles particles, PrintLog outprn){
	
		parentField = parentfield;
		 
		
		parentFieldObserver = ((SurroundRetrievalObserverIntf)parentField );

		this.particles = particles;
		
		limitedAreaUpdate = parentfield.getLimitedAreaUpdate() ;
		
		surroundExtension = new SurroundExtension(this) ;
		
		out = outprn;
		init();
	}
	
	public SurroundBuffers(RepulsionFieldIntf rfield, PrintLog outprn) {
		parentField = rfield;
		parentFieldObserver = ((SurroundRetrievalObserverIntf)parentField );
		out = outprn;
		
		surroundExtension = new SurroundExtension(this) ;
		
		sbsThrd = new Thread (this,"sbsThrd") ;
		bufferingSwitchedOff = true;
	}
	public SurroundBuffers(RepulsionFieldCore rfcore, PrintLog outprn) {
		 
		parentField = (RepulsionFieldIntf)rfcore;
		 
		parentFieldObserver = ((SurroundRetrievalObserverIntf)parentField );
		out = outprn;
		
		sbsThrd = new Thread (this,"sbsThrd") ;
		bufferingSwitchedOff = true;
	}

	// this creates a collection that runs PARALLEL to the particles collection -> care about deletion, adding !! 
	private void init(){
		return;
		/*
		if (particles != null){
			for (int i = 0; i < particles.size(); i++) {
				introduceParticle(i);
			}
		}
		sbsThrd = new Thread (this,"sbsThrd") ;
		bufferingSwitchedOff = true;
		*/
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
		
		// particles.get(particleIndex).registerBufferReference(sb);
		
	}
	
	public void clearSurroundExtension(){
		
	}
	
	/**
	 * this list needs to be cleared upon full update 
	 * 
	 * @param i
	 * @param p
	 */
	public void updateSurroundExtension(int i, Particle p) {
		
		Surrex surrex = new Surrex(i,p); 
		surroundExtension.add( surrex );
											out.print(2,"updateSurroundExtension(), now n="+surroundExtension.size());
	}
	
	/** 
	 * the list of surroundExtension items should be rather short in most cases
	 * 
	 * @param index
	 * @param ixDist
	 * @return
	 */
	public ArrayList<IndexDistance> getIxDisFromSurroundExtension( int index, double maxDistance, ArrayList<IndexDistance> ixDist, String guidStr){
		ArrayList<IndexDistance> extendedIxDist= new ArrayList<IndexDistance>();
		Surrex sx;
		double x0,y0,x,y, ds;
		boolean hb=false;
		IndexDistance ixD;
		
											out.print(4,"getIxDisFromSurroundExtension(), now checking for particle <"+index+">");
		try{
		
			x0 = particles.get(index).x;
			y0 = particles.get(index).y;
			maxDistance = maxDistance*maxDistance; // avoid taking the sqrt
			
			// check for all particles that are stored in surroundExtension
			for (int i=0;i<surroundExtension.size();i++){
				sx = surroundExtension.getItem(i);
				// TODO this sx already contained in  ixDist ?
				hb=false;
				for (int k=0;k<ixDist.size();k++){
					hb = (ixDist.get(k).getIndex()==sx.index);
					if (hb)break;
				}
				if (hb){
					continue;
				}
				
				x = sx.particle.x;
				y = sx.particle.y;
				ds = (x-x0)*(x-x0) + (y-y0)*(y-y0) ;
				if ((ds<maxDistance) && (sx.index!=index)){
					ixD = new IndexDistance( sx.index, Math.sqrt(ds), guidStr);
					extendedIxDist.add(ixD) ;
											out.print(3,"getIxDisFromSurroundExtension(), particle <"+sx+"> added to the extension.");
				}
				
			}// i->
			
		}catch(Exception e){
			
		}
		
		return extendedIxDist;
	}

	
	/**
	 * this is being mostly used in case a buffer was not available and then it has has been directly retrieved;
	 * to avoid repeated direct retrieval, we store now the indexed distances to the relevant buffers
	 * 
	 * @param index
	 * @param indexedDistances
	 * @param surroundSize
	 * @param guidStr
	 */
	public void importToBuffer( int index, ArrayList<IndexDistance> indexedDistances, int surroundSize, String guidStr) {

		int n;
		SurroundBuffer sb;

		try{
			// get the buffer item we have to feed...
			sb = bufferItems.get(index) ;
			
			n = indexedDistances.size() ;
			
			sb.indexes = new int[n];
			sb.distances = new double[n] ;
			
			for (int i=0;i<n;i++){
				
				sb.indexes[i]   = indexedDistances.get(i).getIndex() ;
				sb.distances[i] = indexedDistances.get(i).getDistance() ;
			}
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}
	
	
	
	public ArrayList<IndexDistance> exportBuffer(int index, int surroundSize, String guidStr){
	
		ArrayList<IndexDistance> indexedDistances = new ArrayList<IndexDistance>() ;
		ArrayList<IndexDistance> ixsex;
		
		int ix;
		double dv, dvMax = -1;
		boolean takeIt;
		SurroundBuffer sb;
		IndexDistance ixDist;
		
		sb = bufferItems.get(index) ;
		
											out.print(3, "SurroundBuffers(), exporting buffer into <indexedDistance> ...  ");
		
		int k = sb.indexes.length ;
		if (k>parentField.getSelectionSize() ){
			k=parentField.getSelectionSize() ;
		}
		
		if (index==641){
			// ix=0;
		}
		for (int i=0;i<k;i++){
			
			ix = sb.indexes[i];
			dv = sb.distances[i];
			
			
			// is this particle marked as "being inActive, about to be deleted... ?
			
			takeIt = particles.get(ix).getIsAlive()>0; // would be <0 if scheduled for being deleted
			
			//
			if (takeIt){
				
				// determine the maximum distance:
				// the buffer contains a particular surround, along with the distances of any of the items to 
				// the particle in the center
				// so we need the max distance as a criteria for filtering the surround extensions.
				if (dvMax<dv){
					dvMax=dv;
				}
				
				//
				ixDist = new IndexDistance(ix,dv, guidStr);
				indexedDistances.add(ixDist) ;
			}
		}
		
		ixsex = getIxDisFromSurroundExtension( index,dvMax,indexedDistances,guidStr );
		if ((ixsex!=null) && (ixsex.size()>0)){
			indexedDistances.addAll( ixsex );
		}
		out.print(3, "SurroundBuffers(), exporting buffer into <indexedDistance> completed.  ");
		return indexedDistances;
	}

	// ========================================================================
	
	public void importIndexDistance( int particleIndex, ArrayList<IndexDistance> indexedDistances){
		
		SurroundBuffer sb;
		
		// sb = particles.get(particleIndex).getSurroundBuffer();
		 
		// sb.clear();
		if ((indexedDistances != null) && (indexedDistances.size()>0)) {
			// updating that buffer element
			// sb.index = particleIndex;
			// sb.importSurrounding(indexedDistances);
			
			// sb.parent = this;
		}
		
	}

	public void importBufferItems(	ArrayList<SurroundBuffer> sbs){
	
		
	}

	/**
	 * call this after initial calculation of the field has been completed for the first time;
	 * 
	 * it is called by:
	 * freezeLayout() in RepulsionFieldCore()
	 * 
	 * 
	 */
	public void start(){
		try{
			if (isRunning==false){
				// retrieving the selection size just on first start... such we can detect 
				// change of selection size on the global level
				selectionSize = parentField.getSelectionSize() ;
				
				bufferingSwitchedOff = true;
			
				// sbsThrd.start() ;
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
				
				if ((isWorking==false) && (this.fieldIsStable)){
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
					out.delay(100); z++;
				}
				out.delay(5);
			}// ->
			
		}catch(Exception e){
			
		}
	}
	
	/**
	 * 
	 * this is being called only from the facade layer...
	 * 
	 * Before calling this, the particles have been synchronized already, so indexes are parallel
	 * even after adding/deleting
	 * 
	 * @param targetParticles the particles which we have to update
	 * @param sourceBuffers
	 */
	public void updateFromSurroundBuffers( Particles targetParticles, SurroundBuffers sourceBuffers, int transferDataFlag ) {
		/* 
		int index;
		Particle particle;
		SurroundBuffer srcSB, targetSB ;
		
		if (sourceBuffers==null){
			return;
		}
		
		int i=0;
		while (i<targetParticles.size()){
			
			index = i;
			
			if (i>sourceBuffers.bufferItems.size()-1){
				targetParticles.remove(i);
				i--;
				continue;
			}
			bufferPositionBeingUpdated = index;
			particle = targetParticles.get(index) ;
			
			// boundingBox[]

			// targetSB = particle.getSurroundBuffer();
			// srcSB = sourceBuffers.bufferItems.get(index);

			
			if (transferDataFlag>=1){
				
				// retrieving the reference of the buffer that is attached to
				// the Particle of index i
				 
				if (targetSB == null) {
					// particle.registerBufferReference(new SurroundBuffer( index, this));
					// targetSB = particle.getSurroundBuffer();
				}
				 
				targetSB.importSurrounding( srcSB.indexes, srcSB.distances );
				
				
			} // transferDataFlag ?
			
			targetSB.index = index;
			targetSB.parent = this;
			
			 
			if (index>bufferItems.size()-1){
				bufferItems.add(targetSB);
			}else{
				bufferItems.set(index, targetSB);
			} 
			
			i++;
		} // i-> all particles, all positions
	
		bufferPositionBeingUpdated = -1;
		*/
	}

	private int performFor( int index ) throws Exception{
		int resultState = -1;
		
		int pointIndex;
		Particle particle;
		int[] surrounding;
		double[] surroundDistances;
		SurroundBuffer sb;
		Surround surround ;
		int k;
		
		
		if (index==641){
			k=0;
		}
		
		particle = particles.get(index);
		if ((particle==null) || (switchedOFF) || (particle.isFrozen())){
			
			return 1;
		}
		
		resultState = 5;
		boolean accessForUpdate = true;
		
		if ((limitedAreaUpdate!=null) && (limitedAreaUpdate.centerpointsOfChangedRegion.size()>0)){
			
			pointIndex = limitedAreaUpdate.checkParticle( particle ) ;
			accessForUpdate = pointIndex>=0;
			limitedAreaUpdate.remove(pointIndex) ;
		}
		
		if (accessForUpdate == false){
			return -3;
		}
		// getting the indexes of the surrounding particles
		// surround = new Surround(parentField);
		surround = new Surround(parentField, this);
		 
		
		surrounding = surround.getGeometricSurround( index, selectionSize, Surround._CIRCLE);
		surroundDistances = surround.getParticleDistances();
							if (surrounding.length==0){
								out.printErr(3, "surround.getGeometricSurround() unexpectedly returned a 0-surround for index <"+index+">...") ;
							}
							if (surrounding.length!=selectionSize){
								// this regularly happens in large maps if we have a border topology, 
								// then due to the criterion of the limiting radius
								// we accept a reduction down to 34% (corners in a rectangle), if we still have >80 nodes in the vicinity 
								out.printErr(4, "surround.getGeometricSurround() unexpectedly returned a buffer of wrong length for index <"+index+"> : "+
												surrounding.length+" instead of "+selectionSize+"   ...") ;
							}
		// retrieving the reference of the buffer that is attached to
		// the Particle of index i
											resultState = 7;
		/*								
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
			sb.size = sb.indexes.length;
		}else{
			return -4;
		}
		sb.index = index;
		*/
		if (index>bufferItems.size()-1){
			// bufferItems.add(sb);
		}else{
			// bufferItems.set(index, sb);
		}
		resultState = 0;
		surround = null;
		return resultState ;
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
		 
		int i=0,r, err=1;
		 
		if ((selectionSize<=0) || (switchedOFF)){
			return;
		}
		try{
											out.print(3,"updating the surround buffers..."); err = 2;
			bufferingSwitchedOff = true;
			surroundSizeChanged = selectionSize != parentField.getSelectionSize();
			
			selectionSize = parentField.getSelectionSize();
			updating = 1;

			int xysz = neighborhood.getXyPlane().size() ;
			
			
			
			if (startingIndex >= particles.size() - 1) {
				startingIndex = 0;
			}
			i = Math.max(0, startingIndex);

			// "fieldIsStable" mirrors frozen state
			if (fieldIsStable) {
				while ((i < particles.size())) {
											err = 3;
					try {
											if ((i==1) || (i>501) && (i%50==0)){
												out.print(3,"performing buffer creation (size:"+selectionSize+") for particle index <"+i+"> ");
											}
						r = performFor(i);
						
							if (r!=0){
								out.printErr(2, "surround buffer not correctly set up for particle index <"+i+"> ...");
							}
						
						
						i++;

					} catch (Exception e) {
						out.printErr(1,"critical problem (a) while creating surround buffer for particle <"+ i + ">");
						i++;
					}
				}
			}
											err = 4;
			if (i < particles.size() - 2) {
											out.printErr(1, "updating the surround buffers has been interrupted (at index pos. "+ i + 
															" of " + particles.size()+ 
															"), fieldIsStable? -> " + fieldIsStable);
				startingIndex = Math.max(0, i - 1);

				selectionSize = parentField.getSelectionSize();

			} else {
											err = 6;
				out.print(2, "updating the surround buffers has been completed ");
				anythingChanged = false;
				startingIndex = 0;
											err = 7;
				selectionSize = parentField.getSelectionSize();
				updating = 0;
											err = 8;

				err = 10;
				parentFieldObserver.onSurroundBufferUpdateCompletion( parentField.getName(), particles.size());
				
			}
			
			// TODO check again for all particles the selection size abc124
			
			for (int k=0;k<particles.size();k++){
				
				int sbaState = bufferIsOfState( k, selectionSize );
				if (sbaState<5){
					out.printErr(3, "creating surround buffer for particle <"+k+"> failed (state="+sbaState+")");
				}
			}
			
		} catch (Exception e) {
			out.printErr(2, "\ncritical error (err:" + err + ") in sbs.perform() on index = " + i + "\n");
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

	public int getActualBufferSize(int index) {
		int sz = -1;
		SurroundBuffer sb;
		 
		if (bufferItems!=null){
			sb = bufferItems.get(index);
			sz = sb.indexes.length;
		}
		
		return sz;
	}
	
	public boolean bufferIsAvailable(int index, int surroundSize) {
		boolean rB=false;
		
		rB = bufferIsOfState(index, surroundSize) >=5;
		
		return rB;
	}
	
	public int bufferIsOfState(int index, int surroundSize) {
		boolean rB, sizeOK;
		int stateResult;
		SurroundBuffer sb;
		Particle particle;
		
		stateResult=1;
		rB = (selectionSize >= surroundSize);
		if (rB){
			stateResult=2;
			rB=false;
			
			if ((index < bufferItems.size()) &&(index>=0)){
				stateResult=3;
				sb = bufferItems.get(index);
				if (sb.index == index) {
					stateResult = 4;
					particle = particles.get(index);
					/*
					if (particle.getSurroundBuffer() == sb) {
						
						sizeOK = (sb.indexes.length >= surroundSize); 
						if (sizeOK==false){
							
							if (surroundSize >20){
								sizeOK = (sb.indexes.length >= (int)((double)surroundSize/2.5) );
							}
							
						}
							
						if (sizeOK){
							stateResult=5;
							rB = true;
						}else{
							out.print(4, "surround buffer for particle index <"+index+">is too small (x:"+surroundSize+", o:"+sb.indexes.length+")");
							
						}
					}
					*/
				} else {
					// msg about mismatch of indices
				}
			}// bufferItems.size ?
		}
		
		return stateResult;
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

	public void setAllBuffersUpdating(boolean flag) {
		
		allBuffersUpdating = flag;
	}

	public boolean isAllBuffersUpdating() {
		return allBuffersUpdating;
	}

	public boolean updateIsPending() {
		 
		return updateIsPending;
	}



	public void setUpdateIsPending(boolean updateIsPending) {
		this.updateIsPending = updateIsPending;
	}

	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	public void setParentName(String name) {
		
		parentName = name ;
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
 
			try{
			
				performFor( id );
				
			}catch(Exception e){
				out.printErr(1, "critical problem (b) while creating surround buffer for particle <"+id+">");
			}
			
			
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

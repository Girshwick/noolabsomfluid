package org.NooLab.repulsive;
 
import java.util.ArrayList;


import org.NooLab.repulsive.components.FacadeUpdater;

import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.ParticleAction;
import org.NooLab.repulsive.components.RepulsionFieldProperties;
import org.NooLab.repulsive.components.SurroundBuffers;
import org.NooLab.repulsive.components.SurroundRetrieval;
import org.NooLab.repulsive.components.data.PointXY;
import org.NooLab.repulsive.components.data.SurroundResults;

import org.NooLab.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.repulsive.intf.RepulsionFieldObjectsIntf;
import org.NooLab.repulsive.intf.RepulsionFieldsSyncEventsIntf;
import org.NooLab.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldCoreIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;



public class RepulsionField implements 	Runnable, 
// 										the public interface for usage of the RepulsionField
										RepulsionFieldIntf,
										// the internal interface for linking objects
										RepulsionFieldObjectsIntf,
										// the facade implements the same messaging event sinks as the use layer  
										RepulsionFieldEventsIntf,
										// the facade may receive dedicated messages (prob. subclassing...?)
										RepulsionFieldsSyncEventsIntf,
										// callback to method that organizes the handling of data (upon split, merge)
										ParticleDataHandlingIntf,
										// messages related to the retrieval of a surround upon a request issued by the use layer 
										SurroundRetrievalObserverIntf {
	
	public static final int _INIT_LAYOUT_RANDOM  = 1;
	public static final int _INIT_LAYOUT_REGULAR = 5;
	public static int[] _OUT_SELECTCOLOR = new int[]{255,10,10}; 

	public static final String versionStr = "v1.00.000beta";
	
	// ----------------------------------------------------
	
	public static String _RFUSERDIR = "";
	
	
	
	RepulsionFieldCore coreInstance ;
	
	Neighborhood neighborhood;
	SurroundBuffers surroundBuffers; 
	
	Particles particles ;
	Particles queuedCoreParticles;
	
	SurroundRetrieval surroundRetrieval;
	ParticleAction particleAction;
	
	RepulsionFieldProperties rfProperties;
	
	RepulsionFieldFactory repulsionFieldFactory;
	RepulsionFieldEventsIntf eventsReceptor;
	
	ArrayList<String> availableResults = new ArrayList<String>(); 
	
	boolean isFacadeReadyToUse=false;
	
	ArrUtilities arrutil = new ArrUtilities();
	public PrintLog out = new PrintLog(2,true);
	
	// ========================================================================
	protected RepulsionField(RepulsionFieldFactory rff){
		
		coreInstance = RepulsionFieldCore.create() ; 
		
		repulsionFieldFactory = rff;
		
		coreInstance.setFactoryReference(repulsionFieldFactory);
		
		coreInstance.registerEventMessaging( this ) ;
		 
		
		// of course, we should NOT refer to the coreInstance, the whole trick of the 
		// facade is just to use it as a valuable context! 
		surroundBuffers = new SurroundBuffers( this, out);
		surroundBuffers.setParentName(this.getClass().getSimpleName()) ;
		
		 
		
		out.setPrefix("[RF]");
	}
	// ========================================================================
	
	 
	/**
	 * updating the particles in the facade from the particles in the core; </br>
	 * note that everything is transferred "by-value", no references are passed! (objects are re-created) </br>
	 * this is triggered by the completion of the rebuilding of SurroundBuffers, which is the last
	 * step of recalculating the field. </br> </br>
	 * Once the structures of the facade are updated, requests provide the same results as a query of the 
	 * core would yield.
	 * 
	 * Just for understanding: the facade does NOT contain ANY mechanism for any kind of autonomous re-calculation,
	 * hence, the facade is really simple. </br>
	 * 
	 * All the objects provide mechanisms for updating and cloning. 
	 * Since the access is pure reading, each of them uses the "Cord's" MultiDigester for parallel processes. </br>
	 * 
	 *  While the update of the facade is running, requests for changing the collection of particles are buffered in a waiting queues here in the facade
	 *  before passing them to the core
	 * 
	 */
	private void updatingBuffersFromCore(){
		updatingBuffersFromCore(-1);
	}
	
	private void updatingBuffersFromCore( int index){	
		FacadeUpdater facade; 
		boolean finished =false;
		
		// this opens a new thread and waits for its completion
		// the first time we call this, "new particles" is performed using the particles of the core layer as templates
		// such, we effectively create a clone of the particles object, where also the particles (and any of the
		// objects included therein) are cloned!
		facade = new FacadeUpdater( this, particles,surroundBuffers,neighborhood, coreInstance) ;
		
		
		// here we have to set a flag, causing selection processes to wait!!
		// on the level of the facade, NOT deeper (we will replace them)
		
		surroundBuffers.setBufferingSwitchedOff(true);
		surroundBuffers.setFieldFrozenMessage(false); // setting flag variable "fieldIsStable"
		surroundBuffers.setAllBuffersUpdating(true);
		
		neighborhood = facade.getNeighborhood() ;

		surroundBuffers = facade.getSurroundBuffers() ;
		
		
		if (index<=0){
			surroundBuffers.clearSurroundExtension();
		}
		
		if (index<=0){	
			finished = facade.go();
		}else{
			finished = facade.go(index);
		}
		if (finished){
			particles = facade.getParticles() ;
		}
		
		surroundBuffers = facade.getSyncedSurroundBuffers(particles) ;
		
		surroundBuffers.setAllBuffersUpdating(false);
		surroundBuffers.setBufferingSwitchedOff(false);
		surroundBuffers.setFieldFrozenMessage(true);
		// unset the don't touch flag...
	
	}
	
	/**
	 * 
	 * This is being called upon receiving a messageb about an add-event in the core. </br>
	 * this inserts the same count of particles as will be done in the core layer;</br>
	 * yet, NO positional update will be performed, it just creates the item in the respective fields, such
	 * that the use-layer can address it;
	 * 
	 * As soon as the core layer has inserted all the requested items and has updated the spatial configuration,
	 * the particles of the respective index will be arranged spatially according to the core the data
	 * 
	 * @param x
	 * @param y
	 */
	private void createFacadeParticle( Particle coreParticle){
		
		FacadeUpdater facade; 
		Particle p;
		int count=0;
		// facade = new FacadeUpdater( this, newParticleIndex, particles,surroundBuffers,neighborhood, coreInstance) ;
	
		// count = x.length;
		
		for (int i=0;i<count;i++){
			
			// p = new Particle( queuedCoreParticles[] );
 
			/*
			particles.getItems().add(p);

			surroundBuffers.introduceParticle(particles.size()-1);
			
			neighborhood.update( particles.size()+i,x[i],y[i], p.radius);
			*/
		}
		
	}
	
	@Override
	public void onAddingParticle(Object observable, Particle p, int index) {
		 
		out.print(3, "facade received msg in onAddingParticle() ... ");
		updatingBuffersFromCore( index );
	}
	

	@Override
	public void onDeletingParticle(Object observable, Particle p, int index) {
		// 
		
	}
	
	// ========================================================================

	@Override
	public void init(int nbrParticles) {
		// 
		coreInstance.init( nbrParticles);
	}
	


	@Override
	public void init(int nbrParticles, double energy,double repulsion, double deceleration) {
		// 
		coreInstance.init( nbrParticles,  energy, repulsion,  deceleration) ;
	}

	@Override
	public void init(String command) {
		// 
		coreInstance.init( command) ;
	}

	public RepulsionFieldCore createCoreInstance(RepulsionFieldFactory rff){
	
		RepulsionFieldCore _coreInstance = RepulsionFieldCore.create() ; 
	
		_coreInstance.setFactoryReference(repulsionFieldFactory);
		
		return _coreInstance;
	}


	@Override
	public void registerEventMessaging(Object eventObj){
	
		RepulsionFieldEventsIntf eventsreceptor;
	
		eventsreceptor = (RepulsionFieldEventsIntf)eventObj;
		coreInstance.registerEventMessaging( this ) ;
		
		this.eventsReceptor = eventsreceptor ;
	}

	// .... RepulsionFieldObjectsIntf .........................................
	
	@Override
	public Neighborhood getNeighborhood() {
		// 
		return neighborhood ;
	}


	@Override
	public SurroundBuffers getSurroundBuffers() {
		// 
		return surroundBuffers;
	}


	@Override
	public RepulsionFieldCoreIntf getCoreInstance() {
		// 
		return coreInstance;
	}

	@Override
	public RepulsionFieldIntf getFacadeInstance() {
		// 
		return this;
	}
	// ........................................................................
	
	
	@Override
	public void setName(String name) {
		 
		coreInstance.setName(name) ;
	}
	
	@Override
	public String getName() {
		
		return coreInstance.name ;
	}
	

	@Override
	public String getVersionStr() {
		
		return coreInstance.getVersionStr();
	}
	
	
	// ========================================================================

	@Override
	public String  selectParticleAt(int xpos, int ypos, boolean autoselect) {
		
		String guidStr="";
		int pix;
		
		if (neighborhood==null){
			return "";
		}
		// for getting the results we should NOT refer to the core, of course !!
		surroundRetrieval = new SurroundRetrieval( this,  (SurroundRetrievalObserverIntf)this) ;
		
		pix = surroundRetrieval.addRetrieval( xpos, ypos, autoselect);
		
		guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_PARTICLE);
		 
		availableResults.add(guidStr);
		
		String str = Thread.currentThread().getStackTrace()[1].getMethodName();
		// 	caller: GetStacktrace()[1]
		
		return guidStr;
		
	}


	@Override
	public String getSurround(int xpos, int ypos, int selectMode, boolean autoselect) {
		// EXCEPT startup, NOT handled by passing through, served from objects of the facade 
		
		int surroundN;
											out.print(4,"getSurround(x,y), ready to use ? -> "+isReadyToUse());

		int z=0;
		while ((isReadyToUse()==false) && (surroundBuffers.isAllBuffersUpdating()) && (z<100)){
			out.delay(10); z++;
		}

		if (isReadyToUse()){
			
			String guidStr="";
			int pix;
			 
			if (surroundRetrieval==null){
				surroundRetrieval = new SurroundRetrieval( this,  (SurroundRetrievalObserverIntf)this) ;
			}
			
			surroundN = coreInstance.getSelectionSize() ;	
			
			pix = surroundRetrieval.addRetrieval( xpos, ypos, surroundN, selectMode, autoselect);
			
			guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_C);
			  
			 
			return guidStr;
		} 
		return "";
	}

	@Override
	public String getSurround(int index, int selectMode, boolean autoselect) {


		String guidStr="";
		int pix;
		int surroundN=7;

											out.print(2,"getSurround(index), ready to use ? -> "+isReadyToUse());
		int z=0;
		while (( isReadyToUse()==false)  && (surroundBuffers.isAllBuffersUpdating()) && (z<100)){
			out.delay(10); z++;
		}
		
		if ((isReadyToUse()) && (particles.get(index).getIsAlive()>0 )){
			
			surroundN = coreInstance.getSelectionSize();

			if (surroundRetrieval==null){
				surroundRetrieval = new SurroundRetrieval( this,  (SurroundRetrievalObserverIntf)this) ;
			}

			pix = surroundRetrieval.addRetrieval(index, surroundN, selectMode, autoselect);

			guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_X);
		}
		return "";
		 
	}
 
	
	@Override
	public String getParticlesOfFiguratedSet( int figure, Object objIndexes, 
											  double thickness, double endPointRatio, boolean autoselect) {
		
		String guidStr="";

		int[] indexes = arrutil.importObjectedIntArr( objIndexes) ;
		 
		
		int z=0;
		while (( isReadyToUse()==false)  && (surroundBuffers.isAllBuffersUpdating()) && (z<100)){
			out.delay(10); z++;
		}
		
		if (figure == RepulsionFieldIntf.__SELECTION_FIGURE_MST){
			 
			// should not be performed by the coreIntance of course... 
			guidStr = getParticlesAroundMST(indexes, thickness, endPointRatio, autoselect);
		}
		
		if (figure == RepulsionFieldIntf.__SELECTION_FIGURE_CONVEXHULL){
			guidStr = getParticlesWithinConvexHull(indexes, thickness, (int)endPointRatio, autoselect);
		}
		
		return guidStr;
	}

	@Override
	public String getParticlesOfFiguratedSet( int figure, ArrayList<PointXY> points, 
											  double thickness, double endPointRatio, boolean autoselect) {
		// 
		// ArrayList<PointXY> points;
		ArrayList<Integer> indexesL = new ArrayList<Integer>(); 
		String guidStr ;
		
		for (int i=0;i<points.size();i++){
			guidStr = selectParticleAt( (int)points.get(i).x , (int)points.get(i).y, false) ;
			// autoselect = false -> no information to client !
		} // i->
		// wait, and check completion of the map, that is being created in result sink
		
		return null;
	}

	
	public String getParticlesAroundMST( int[] indexes, double thickness,  double endPointRatio, boolean autoselect) {
		//
		String guidStr = "";
		int pix;
	
		
		if (surroundRetrieval==null){
			surroundRetrieval = new SurroundRetrieval( this,  (SurroundRetrievalObserverIntf)this) ;
		}

		// pix then will contain the indx to a slot in the collecton of
		// "paramSets"
		
		pix = surroundRetrieval.addRetrieval(indexes, thickness, endPointRatio, autoselect);
	
		guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_MST);
	
		return guidStr;
	}


	public String getParticlesWithinConvexHull(int[] indexes, double thickness, int topology, boolean autoselect) {
		
		String guidStr="";
		int pix;
		
		if (surroundRetrieval==null){
			surroundRetrieval = new SurroundRetrieval( this,  (SurroundRetrievalObserverIntf)this) ;
		}

		// pix then will contain the indx to a slot in the collecton of "paramSets"
		pix = surroundRetrieval.addRetrieval( indexes,  thickness, topology, autoselect);
		
		guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_SURROUND_CXHULL);
		 
		return guidStr;
	}


	@Override
	public int getSelectionSize() {
		 
		return coreInstance.getSelectionSize() ;
	}

	@Override
	public void setSelectionSize(int n) {
		 
		coreInstance.setSelectionSize(n);
	}

	@Override
	public void selectionSizeDecrease(int mode, double amount) {
		
		coreInstance.selectionSizeDecrease(mode, amount) ;
	}

	@Override
	public void selectionSizeIncrease(int mode, double amount) {
	 
		coreInstance.selectionSizeIncrease( mode,  amount);
	}

	@Override
	public void setHexagonSizedSelection(boolean hexagonSizedSelection) {
		//  
		coreInstance.setHexagonSizedSelection(hexagonSizedSelection); 
	}

	@Override
	public void setShapeOfSelection() {
		// 
		coreInstance.setShapeOfSelection() ;
	}

	@Override
	public ParticlesIntf getParticles() {
		 
		if (particles==null){
			return coreInstance.getParticles();
		}else{
			return particles;
		}
			
	}
	

	@Override
	public GraphParticlesIntf getGraphParticles() {
		 
		return (GraphParticlesIntf) coreInstance.getGraphParticles();
	}

	@Override
	public int addParticles(int count) {
		// 
		for (int i=0;i<count;i++){
			addParticles(-1,-1); out.delay(10);
		}
		
 		return coreInstance.addParticles(count) ;
	}

	@Override
	public int addParticles(int x, int y) {
		String gstr;
		int newParticleIndex;
											out.print(4, "dispatching <add> ...");		
        newParticleIndex = coreInstance.addParticles(x, y) ;
        									out.print(4, "back from dispatcher for <add>, particle with index "+newParticleIndex+" created in core ...");
       	
		return newParticleIndex; 
	}

	@Override
	public int addParticles(int[] x, int[] y) {
		 
		return coreInstance.addParticles( x, y) ;
	}

	@Override
	public int splitParticle(int index, ParticleDataHandlingIntf pdataHandler) {
		int newParticleIndex;
		Particle particle;
		
		particle = particles.get(index) ;
		
		addParticles( (int)particle.x+4, (int)particle.y+4) ; 
		
		newParticleIndex = coreInstance.splitParticle(index, pdataHandler) ;
		
		return newParticleIndex;
	}

	@Override
	public String mergeParticles(int mergeTargetIndex, int[] indexes) {
		// 
		return coreInstance.mergeParticles(mergeTargetIndex, indexes) ;
	}

	@Override
	public String mergeParticles(int mergeTargetIndex, int swallowedIndex) {
		//  
		return coreInstance.mergeParticles(mergeTargetIndex, swallowedIndex) ;
	}

	@Override
	public void deleteParticle(int index) {
		// 1. make it logically invisible;
		// particles.get(index).setIsAlive(-2);
		// particles.get(index).setActive(false) ;
		// 2. make it physically invisible;
		particles.get(index).x = 3 * (coreInstance.areaWidth) ;
		particles.remove(index) ;
		// <0 = scheduled for being deleted
		// it will not be returned in any surround
		coreInstance.deleteParticle(index) ;
		
		
		return ;
	}

	@Override
	public void moveParticle(int particleIndex, int type, double xParam, double yParam) {
		double newX, newY;
		Particle particle;
		
		particle = particles.get(particleIndex) ;
		
		if (type>=2){
			newX = particle.x + xParam ;
			newY = particle.y + yParam ;
		} else{
			newX = xParam ;
			newY = yParam ;
		}
		int nbm = coreInstance.getBorderMode();
		double[] spatialpos = neighborhood.adjustSpatialPositionsToBorderSettings( newX,newY, particle.radius, nbm);
		particle.x = spatialpos[0];
		particle.y = spatialpos[1];
		
		coreInstance.moveParticle( particleIndex, type, xParam, yParam);
	}

	@Override
	public int getNumberOfParticles() {
		 
		return coreInstance.getNumberOfParticles() ;
	}

	@Override
	public void clearData(int index) {
		// 
		coreInstance.clearData(index) ;
	}

	@Override
	public void transferData(int fromParticleIndex, int toParticleIndex) {
		// 
		coreInstance.transferData(fromParticleIndex, toParticleIndex);
	}

	@Override
	public void insertDataPointer(int particleIndex, long dataPointer) {
		// 
		coreInstance.insertDataPointer( particleIndex, dataPointer);
	}

	@Override
	public void removeDataPointer(int particleIndex, long dataPointer) {
		// 
		coreInstance.removeDataPointer(particleIndex, dataPointer) ;
	}

	@Override
	public double getRepulsion() {
		 
		return coreInstance.getRepulsion() ;
	}

	@Override
	public void setRepulsion(double repulsion) {
		// 
		coreInstance.setRepulsion(repulsion) ;
	}

	@Override
	public double getDeceleration() {
		 
		return coreInstance.getDeceleration() ;
	}

	@Override
	public void setDeceleration(double deceleration) {
		// 
		coreInstance.setDeceleration(deceleration);
	}

	@Override
	public double getEnergy() {
		 
		return coreInstance.getEnergy() ;
	}

	@Override
	public void setEnergy(double energy) {
		// 
		coreInstance.setEnergy(energy) ;
	}

	@Override
	public int getDelayedOnsetMillis() {
		 
		return coreInstance.getDelayedOnsetMillis() ;
	}

	@Override
	public void setDelayedOnset(int delayedOnsetMillis) {
		// 
		coreInstance.setDelayedOnset(delayedOnsetMillis) ;
	}

	@Override
	public void useParallelProcesses(int flag) {
		// 
		coreInstance.useParallelProcesses(flag);
	}


	@Override
	public boolean isMultiProc() {
		return coreInstance.isMultiProc();
	}


	@Override
	public void setMultiProc(boolean flag) {
		coreInstance.setMultiProc(flag);
		
	}
	
	@Override
	public void setFreezingAllowed(boolean freezingAllowed) {
		// 
		coreInstance.setFreezingAllowed(freezingAllowed);
	}

	@Override
	public void setAreaSize(int width, int height) {
		// 
		coreInstance.setAreaSize(width, height);
	}

	@Override
	public int[] getAreaSize() {
		 
		return coreInstance.getAreaSize() ;
	}

	
	@Override
	public void setMaxDensityDeviationPercent(double value) {
		coreInstance.setMaxDensityDeviationPercent(value);
	}
	
	@Override
	public void setDynamics(int nbrParticles, double energy, double repulsion, double deceleration) {
		coreInstance.setDynamics( nbrParticles, energy, repulsion, deceleration) ;
	}
	@Override
	public void setBorderMode(int bordermode) {
		coreInstance.setBorderMode(bordermode);
	}
	@Override
	public void update() {
		// 
		coreInstance.update();
	}

	@Override
	public void setStepsLimit(int steps) {
		// 
		coreInstance.setStepsLimit(steps);
	}

	@Override
	public void interrupt() {
		// 
		coreInstance.interrupt();
	}

	@Override
	public void mobilityDecrease() {
		// 
		coreInstance.mobilityDecrease();
	}

	@Override
	public void mobilityIncrease() {
		// 
		coreInstance.mobilityIncrease();
	}

	@Override
	public void releaseShakeIt(int intensity) {
		// 
		coreInstance.releaseShakeIt( intensity);
	}

	@Override
	public void setAdaptiveBehavior(boolean flag) {
		// 
		coreInstance.setAdaptiveBehavior(flag);
	}

	@Override
	public boolean isCompleted() {
		 
		return coreInstance.isCompleted() ;
	}

	@Override
	public boolean isUpdateFinished() {
		 
		return coreInstance.isUpdateFinished() ;
	}

	@Override
	public boolean isReadyToUse () {
		return isFacadeReadyToUse ;
		
	}
	
	@Override
	public void setColorSize(boolean differentsize, boolean differentcolor) {
		//  
		coreInstance.setColorSize( differentsize, differentcolor);
	}

	 

	@Override
	public void setInitialLayoutMode(int initLayoutRegular) {
		//  
		coreInstance.setInitialLayoutMode(initLayoutRegular);
	}

	@Override
	public void importField() {
	  
		coreInstance.importField();
	}

	@Override
	public void importField(String filename) {
		
		coreInstance.importField(filename);
	}

	@Override
	public void importCoordinates(String filename) {

		coreInstance.importCoordinates(filename);
	}

	@Override
	public void exportCoordinates(String filename) {
		 
		coreInstance.exportCoordinates(filename);
	}

	@Override
	public void storeRepulsionField() {
		 
		coreInstance.storeRepulsionField() ;
	}
	
	// public FieldStorageContainer getFieldStorageContainer() ;
	// this for making the RepulsionField Serializable

	@Override
	public void storeRepulsionField(String filename) {
		 
		coreInstance.storeRepulsionField(filename);
	} 

	public static String getVersionstr() {
		return versionStr;
	}


	// ========================================================================
	@Override
	public void run() {
		 
		
	}

	
	
	// ========================================================================
	
	@Override
	public void onLayoutCompleted(int flag) {
		//  
		
	}
	@Override
	public void onSelectionRequestCompleted(Object results) {
		//  
		out.print(2, "event msg routing in repulsionField...");
		eventsReceptor.onSelectionRequestCompleted(results);
	}
	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		//  
		eventsReceptor.onAreaSizeChanged( observable, width, height);
	}
	@Override
	public void onActionAccepted(int action, int state, Object param) {
		//  
		
	}
	@Override
	public void statusMessage(String msg) {
		 
		eventsReceptor.statusMessage(msg) ;
	}
	@Override
	public void onCalculationsCompleted() {
		// 
		out.print(3, "event observed by onCalculationsCompleted() in RF facade... ") ;
		
		updatingBuffersFromCore(); 
		
		eventsReceptor.onCalculationsCompleted();
		
		isFacadeReadyToUse=true; // will be set only HERE to true, after the first fixation 
	}
	
	
	// ========================================================================

	@Override                        
	public void surroundRetrievalUpdate( SurroundRetrieval Observable, String guid) {

		out.print(3, "result returned to field from SurroundRetrieval(), result id = "+guid );
		
		try{
			

			// now we can retrieve the results
			SurroundResults result = (SurroundResults) Observable.getResultsByGuid(guid);
			 
			if (result == null) {
				return;
			}

			result.arrutil = arrutil;
			
			if (result.getParamSet().getTask() >= SurroundRetrieval._TASK_SURROUND_C) {
				particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
			}

			if (result.getParamSet().getTask() <= SurroundRetrieval._TASK_PARTICLE) {
				result.setParticleIndexes( new int[] { (int) result.particleIndex });
				particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
				
				if (result.getParamSet().getTask() <= SurroundRetrieval._TASK_PARTICLE) {
					particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
				}
			}

			if (result.getParamSet().getTask() == SurroundRetrieval._TASK_SURROUND_MST){
				particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
			}
			if ((eventsReceptor != null)  ){
				eventsReceptor.onSelectionRequestCompleted(result);
			}
			
	
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onSurroundBufferUpdateCompletion(String name, int size) {
		 // neglect this here
		
	}
	@Override
	public int getSurroundBuffersUpdateCounter() {
		// neglect this here
		return -1;
	}
	@Override
	public void handlingDataOnParticleSplit(Object observable,
			Particles particles, int originix, int pullulix) {
		// TODO Auto-generated method stub
		
	}


	


	


	


	 



 

 

}

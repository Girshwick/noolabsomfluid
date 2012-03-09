package org.NooLab.repulsive;
 
import java.util.ArrayList;


import org.NooLab.repulsive.components.FacadeUpdater;

import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.ParticleAction;
import org.NooLab.repulsive.components.RepulsionFieldProperties;
import org.NooLab.repulsive.components.SelectionConstraints;
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
import org.NooLab.repulsive.intf.main.RepulsionFieldSelectionIntf;
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * change log for 1.02
 * - previous buffer mechanism has been dropped and replaced by an "active area mechanism", which
 *   uses a dynamic high-resolution regular grid.
 *   
 *   this allows for constant fast access with low memory profile
 * 
 * 
 * 
 */
public class RepulsionField implements 	Runnable, 
										RepulsionFieldSelectionIntf,
										// the public interface for usage of the RepulsionField
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

	public static final String versionStr = "v1.02.000beta";
	
	// ----------------------------------------------------
	
	public static String _RFUSERDIR = "";
	
	
	RepulsionField rField; // variable for itself, for referring to it from within inner classes
	RepulsionFieldCore coreInstance ;
	
	Neighborhood neighborhood;
	//SurroundBuffers surroundBuffers; 
	
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
		// surroundBuffers = new SurroundBuffers( this, out);
		// surroundBuffers.setParentName(this.getClass().getSimpleName()) ;
		
		rField = this;
		
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
	
	public void updatingBuffersFromCore( int index){	
		FacadeUpdater facade; 
		boolean finished =false;
		
		coreInstance.updateNeighborhood( );
		
		// this opens a new thread and waits for its completion
		// the first time we call this, "new particles" is performed using the particles of the core layer as templates
		// such, we effectively create a clone of the particles object, where also the particles (and any of the
		// objects included therein) are cloned!
		facade = new FacadeUpdater( this, particles,neighborhood, coreInstance) ;// surroundBuffers,
		
		 
		
		// here we have to set a flag, causing selection processes to wait!!
		// on the level of the facade, NOT deeper (we will replace them)
		/*
		surroundBuffers.setBufferingSwitchedOff(true);
		surroundBuffers.setFieldFrozenMessage(false); // setting flag variable "fieldIsStable"
		surroundBuffers.setAllBuffersUpdating(true);
		*/
		// neighborhood = facade.getNeighborhood() ;

		
		
		// surroundBuffers = facade.getSurroundBuffers() ;
		
		/*
		if (index<=0){
			// surroundBuffers.clearSurroundExtension();
		}
		*/
		if (index<=0){	
			finished = facade.go();
		}else{
			finished = facade.go(index);
		}
		if (finished){
			particles = facade.getParticles() ;
		}
		
		/*
		surroundBuffers = facade.getSyncedSurroundBuffers(particles) ;
		
		surroundBuffers.setAllBuffersUpdating(false);
		surroundBuffers.setBufferingSwitchedOff(false);
		surroundBuffers.setFieldFrozenMessage(true);
		// unset the don't touch flag...
		*/
	}
	
	/**
	 * 
	 * This is being called upon receiving a message about an add-event in the core. </br>
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
	@SuppressWarnings("unused")
	private void createFacadeParticle( Particle coreParticle){
		
		FacadeUpdater facade; 
		Particle p;
		int count=0;
		// facade = new FacadeUpdater( this, newParticleIndex, particles,surroundBuffers,neighborhood, coreInstance) ;
	
		// count = x.length;
		
		for (int i=0;i<count;i++){
			
			// p = new Particle( queuedCoreParticles[] );
 
			 
			// particles.getItems().add(p);

			/*
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
		return null; // surroundBuffers;
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
	
	public void activateGridOptimizer(boolean flag){
		coreInstance.activateGridOptimizer(flag);
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



	class SurroundRetrievalHandler{
		String guidStr="";
		int pix;
		int surroundN=7;
		
		int index;
		int xpos; 
		int ypos;
		int selectMode;
		boolean autoselect;

		public SurroundRetrievalHandler(  double xpos, double ypos, boolean autoselect){
			
			surroundN = coreInstance.selectionSize ;
			this.xpos= (int) xpos;
			this.ypos= (int) ypos;
			 
			this.autoselect = autoselect;
		}

		public SurroundRetrievalHandler(  int xpos, int ypos, int selectMode, boolean autoselect){
			surroundN = coreInstance.selectionSize ;
			this.xpos= xpos;
			this.ypos= ypos;
			this.selectMode = selectMode;
			this.autoselect = autoselect;
		}
		public SurroundRetrievalHandler(  int index, int selectMode, boolean autoselect){
			surroundN = coreInstance.selectionSize ;
			this.index= index;
			this.selectMode = selectMode;
			this.autoselect = autoselect;
		}
	
		
		public String go(int task){

			if (task==1){
				return _selectParticleAt();
			}
			if (task==2){
				return _getSurroundforLocation();
			}
			if (task==3){
				return _getSurroundforIndex();
			}
			
			return "" ;
		}
		
		private String _selectParticleAt(){
			

			String guidStr="";
			int pix;
			int z=0;
			while ((z<50) && (coreInstance.particleGridIsUpdating) && coreInstance.particleGrid.isGridInactive()){
				z++;
				out.delay(10);
			}
			 
			// for getting the results we should NOT refer to the core, of course !!
			surroundRetrieval = new SurroundRetrieval( rField, coreInstance.particleGrid,  (SurroundRetrievalObserverIntf)rField) ; 
			
			pix = surroundRetrieval.addRetrieval( xpos, ypos, autoselect);
			
			// this will directly return, the retrieval then happens in another thread, who will signal
			// its results through a callback
			guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_PARTICLE);
			 
			availableResults.add(guidStr);
			
			String str = Thread.currentThread().getStackTrace()[1].getMethodName();
			// 	caller: GetStacktrace()[1]
			
			return guidStr;
			
		}
 
		
		
		private String _getSurroundforLocation(){
			

			int surroundN;
												out.print(4,"getSurround(x,y), ready to use ? -> "+isReadyToUse());

			int z=0;
			while ((isReadyToUse()==false) && (z<100)){ // && (surroundBuffers.isAllBuffersUpdating()
				out.delay(10); z++;
			}

			z=0;
			while ((z<50) && (coreInstance.particleGridIsUpdating) && coreInstance.particleGrid.isGridInactive()){
				z++; out.delay(10);
			}

			if (isReadyToUse()){
				
				String guidStr="";
				int pix;
				 
				if (surroundRetrieval==null){
					surroundRetrieval = new SurroundRetrieval( rField, coreInstance.particleGrid,  (SurroundRetrievalObserverIntf)rField) ;
				}else{  surroundRetrieval.setParticleGrid( coreInstance.particleGrid );
				}
				surroundRetrieval.setSelectionConstraints( coreInstance.selectionConstraints );
				
				surroundN = coreInstance.getSelectionSize() ;	
				
				pix = surroundRetrieval.addRetrieval( xpos, ypos, surroundN, selectMode, autoselect);
				
				guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_C);
				  
				 
				return guidStr;
			} 
			return "";
		}
		
		private String _getSurroundforIndex(){

			String guidStr="";
			int pix;
			int surroundN=7;

												out.print(3,"getSurround("+index+"), ready to use ? -> "+isReadyToUse());
			int z=0;
			while (( isReadyToUse()==false) && (z<100)){ //  && (surroundBuffers.isAllBuffersUpdating()
				out.delay(10); z++;
			}
			
			z=0;
			while ((z<50) && (coreInstance.particleGridIsUpdating) && coreInstance.particleGrid.isGridInactive()){
				z++; out.delay(10);
			}
			
			if ((isReadyToUse()) && (particles.get(index).getIsAlive()>0 )){
				
				surroundN = coreInstance.getSelectionSize();

				if (surroundRetrieval==null){
					out.print(3,"getSurround(), NEW SurroundRetrieval");
					surroundRetrieval = new SurroundRetrieval( rField, coreInstance.particleGrid,  (SurroundRetrievalObserverIntf)rField) ;
				}

				surroundRetrieval.setSelectionConstraints( coreInstance.selectionConstraints );
				
				pix = surroundRetrieval.addRetrieval(index, surroundN, selectMode, autoselect);

				guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_X);
				return guidStr;
			}
			return "";
		}
		
	} // inner class SurroundRetrievalHandler
	
	

	@Override
	public String  selectParticleAt(int xpos, int ypos, boolean autoselect) {
		/*
		 * the SurroundRetrievalHandler{} object provides an immediate fork in order to achieve thread safety;
		 * there, an instance of SurroundRetrieval{} will be created, then a task defined and
		 * the surroundRetrieval{} being started as a dedicated and decoupled process.
		 * Once that process has been finished, it sends the results to the surface layer by
		 * means of a message (through an interface mediated callback)... the handling
		 * of that message ALSO has to be stateless = thread-safe by using an acceptance class!!!
		 * (FIFO message queue for serializing parallel/overlapping requests) 
		 */  
		return (new SurroundRetrievalHandler( (double)xpos, (double)ypos, autoselect)).go(1);
	}

	@Override
	public String getSurround(int xpos, int ypos, int selectMode, boolean autoselect) {
		// EXCEPT startup, NOT handled by passing through, served from objects of the facade 
		
		return (new SurroundRetrievalHandler( xpos, ypos, selectMode, autoselect)).go(2);
	}
	
	@Override
	public String getSurround( int index, int selectMode, boolean autoselect) {

		return (new SurroundRetrievalHandler( index, selectMode, autoselect)).go(3);
	}
	
	@Override
	public String getParticlesOfFiguratedSet( int figure, Object objIndexes, 
											  double thickness, double endPointRatio, boolean autoselect) {
		
		String guidStr="";

		int[] indexes = arrutil.importObjectedIntArr( objIndexes) ;
		 
		
		int z=0;
		while (( isReadyToUse()==false) && (z<100)){ //  && (surroundBuffers.isAllBuffersUpdating()
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
			surroundRetrieval = new SurroundRetrieval( this, coreInstance.particleGrid,  (SurroundRetrievalObserverIntf)this) ;
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
			surroundRetrieval = new SurroundRetrieval( this, coreInstance.particleGrid, (SurroundRetrievalObserverIntf)this) ;
		}

		// pix then will contain the index to a slot in the collecton of "paramSets"
		pix = surroundRetrieval.addRetrieval( indexes,  thickness, topology, autoselect);
		
		guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_SURROUND_CXHULL);
		 
		return guidStr;
	}


	@Override
	public void provideGridPerspective() {
		// creates the particleGrid, which is used internally to create the list (indexes) of 
		// particles that are arranged around a coordinate
		
	}


	@Override
	public int getSelectionSize() {
		 
		return coreInstance.getSelectionSize() ;
	}

	@Override
	public void setSelectionSize(int n) {
		 
		coreInstance.applySelectionSizeRestrictions();
		
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

	public void setSelectionSizeRestriction(int selszLimit) {
		//  
		boolean flag;
		
		flag = selszLimit>11;
		
		rfProperties = repulsionFieldFactory.rfProperties ;
		this.rfProperties.setRestrictSelectionSize( flag);
		rfProperties.setSelectionSizeRestriction(selszLimit);
		
		coreInstance.rfProperties.setRestrictSelectionSize( flag);
		coreInstance.rfProperties.setSelectionSizeRestriction(selszLimit);
		
		coreInstance.setRestrictSelectionSize( flag);
		coreInstance.applySelectionSizeRestrictions();
		
		
	}
	

	public int getSelectionSizeRestriction() {
		int selsz;
		rfProperties = repulsionFieldFactory.rfProperties ;
		
		selsz = rfProperties.getSelectionSizeRestriction() ;
		
		return selsz ;
	}
	 

	@Override
	public void setConstrainingBox() {

		coreInstance.selectionConstraints.deactivate();
	}


	@Override
	public void setConstrainingBox(boolean flag) {

		if (flag==false){
			coreInstance.selectionConstraints.deactivate();
		}else{
			coreInstance.selectionConstraints.activateBox();
		}
		
	}


	@Override
	public void setConstrainingBox(double x1, int x2, double y1, double y2) {

		coreInstance.selectionConstraints.defineBox(x1, x2, y1, y2);
	}


	@Override
	public void setSelectionShape(int shapeId, double param1, double param2) {
		 
		coreInstance.selectionConstraints.setSelectionShape( shapeId, param1, param2);
	}


	@Override
	public void setSelectionShape(int shapeId) {
		coreInstance.selectionConstraints.setSelectionShape( shapeId );
		
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
			// addParticles(-1,-1); out.delay(10);
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
		double[] spatialpos = coreInstance.spatialGeomCalc.adjustSpatialPositionsToBorderSettings( newX,newY, particle.radius, nbm);
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
		
		coreInstance.useParallelProcesses(flag);
		coreInstance.setMultiProc( (flag>=1) );
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
	public void setDefaultDensity(double dvalue) {
		
		coreInstance.setDefaultDensity(dvalue);
	}


	@Override
	public void setDefaultDensity(double avgDensity, int nodeCount) {
	
		coreInstance.setDefaultDensity(avgDensity , nodeCount);
	}


	@Override
	public void setAreaSizeAuto(int nodecounttarget) {
		
		coreInstance.setAreaSizeAuto(nodecounttarget);
		setAreaSize( coreInstance.getAreaWidth(),coreInstance.areaHeight);
		
	}
	

	@Override
	public void setAreaSizeMin() {
		coreInstance.setAreaSizeMin();
		setAreaSize( coreInstance.getAreaWidth(),coreInstance.areaHeight);
	}
	
	@Override
	public void setMaxDensityDeviationPercent(double value) {
		coreInstance.setMaxDensityDeviationPercent(value);
	}
	
	@Override
	public double getAverageDistanceBetweenParticles() {
		 
		return coreInstance.averageDistance ;
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
	public void setFieldIsRandom(boolean flag) {
		 
		
		coreInstance.setFieldIsRandom(flag);
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
		coreInstance.releaseShakeIt( intensity,-1);
	}
	@Override
	public void releaseShakeIt(int intensity, int maxTime) {
		// 
		coreInstance.releaseShakeIt( intensity, maxTime);
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
			
			if (particles==null){
				return;
			}
			// now we can retrieve the results
			SurroundResults result = (SurroundResults) Observable.getResultsByGuid(guid);
			 
			if (result == null) {
				return;
			}

			result.arrutil = arrutil;
			
			if (result.getParamSet().getTask() >= SurroundRetrieval._TASK_SURROUND_C) {
				// particles.selectSurround( result.getParticleIndexes(), result.getParamSet().isAutoselect());
			}

			if (result.getParamSet().getTask() <= SurroundRetrieval._TASK_PARTICLE) {
				result.setParticleIndexes( new int[] { (int) result.particleIndex });
				// particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
				
				if (result.getParamSet().getTask() <= SurroundRetrieval._TASK_PARTICLE) {
					// particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
				}
			}

			if (result.getParamSet().getTask() == SurroundRetrieval._TASK_SURROUND_MST){
				// particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
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
	public void handlingDataOnParticleSplit( Object observable,
											 Particles particles, int originix, int pullulix) {
		
	}
 

 

}

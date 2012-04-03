package org.NooLab.somfluid;

import java.util.ArrayList;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
 
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;

import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;

import org.NooLab.somfluid.properties.* ;
import org.NooLab.somfluid.components.* ;
import org.NooLab.somfluid.core.application.*;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
 
import org.NooLab.somfluid.env.data.*;
import org.NooLab.somtransform.SomTransformer;



/*

	When someone says, "This is really tricky code," I hear them say, "This is really bad code."
	- Steve McConnell
	
	Thus, no tricks are included here.
*/

/**
 * 
 * SomFluid is starting the learning process and is  
 * running a supervising process about the state and the activity of the network
 * 
 * SomFluid does not possess any capabilities for graphical output.
 * The graphics is a separate module hosted by the SomFactory, to which the Fluid connects via port sending;
 * format of exchange is standardized, such the displayed info can be switched easily 
 * 
 * 
 * 
 */
public class SomFluid 
                      implements Runnable,
								 SomFluidIntf,
								 
								 SomSupervisionIntf ,
								 RepulsionFieldEventsIntf
								 {
	
	
	

	SomFluidProperties sfProperties;
	SomFluidFactory sfFactory ;
	
	
	
	// SomDataObject somDataObject;
	ArrayList<SomDataObject> somDataObjects = new ArrayList<SomDataObject>();
	
	SomTasks somTasks;
	
	
	SomFluid sf ;
 	
	SomApplication somApplication;
	
	boolean isActivated=false, isInitialized=false;
	boolean processIsRunning=false;
	Thread sfThread;
	
	StringedObjects sob = new StringedObjects();
	PrintLog out = new PrintLog(2, true);
	private boolean processIsActivated=false;
	private RepulsionFieldIntf particleField;
	private boolean userBreak;
	
	
	// ------------------------------------------------------------------------
	protected SomFluid( SomFluidFactory factory){
		
		sfThread = new Thread (this,"sfThread" );
		somTasks = new SomTasks( sfFactory ) ;
		
		sfFactory = factory;
		sfProperties = sfFactory.getSfProperties() ;
		
		sf = this;
		
		prepareParticlesField( (RepulsionFieldEventsIntf)this);
	}
	
	 
	
	
	// ========================================================================
	
	 
	public void prepareParticlesField( RepulsionFieldEventsIntf eventSink ){
		
		int initialNodeCount = sfProperties.getInitialNodeCount();

		out.print(2, "creating the physical node field for " + initialNodeCount
				+ " particles...");

		// we need to harvest the events here!
		if ((particleField != null) && (particleField.getNumberOfParticles() != initialNodeCount)) {
			particleField.close();
			particleField = null;
		}
		if (particleField == null) {
			
			// sfFactory.establishPhysicalFieldMessaging( this); // RepulsionFieldEventsIntf eventSink
			particleField = sfFactory.createPhysicalField( eventSink, initialNodeCount);
			// this one extra!
			// establishPhysicalFieldMessaging( RepulsionFieldEventsIntf eventSink)
		}

	}
	
 




	/**
	 * start this through message queue and task process,  
	 * 
	 * @param sfTask
	 */
	private void performTargetedModeling( SomFluidTask sfTask ) {
		// since we need this also for evo-optimization, we put it to a small class  
		// there we use a different constructor (taking a copy from this basic instance...)
		 
		SimpleSingleModel simo ;
		ModelingSettings modset = sfProperties.modelingSettings ;
			
		sfTask.setSomHost(null) ;
		somDataObjects.clear();
		
		simo = new SimpleSingleModel(this, sfTask, sfFactory );
		
		sfTask.setDescription("performTargetedModeling()") ;
		
		simo.prepareDataObject() ;
		simo.setInitialVariableSelection( modset.getInitialVariableSelection() ) ;
		
		simo.perform();
		 
		// handling requests about persistence: saving/sending model, results
	}
	

	
	private void performModelOptimizcreener(SomFluidTask sfTask) {
		ModelOptimizer moz ;
		 
		somDataObjects.clear();
		
		sfTask.setDescription("ModelOptimizer()") ;
		sfTask.setSomHost(null) ;
		
		moz = new ModelOptimizer( this , sfTask, sfFactory );
		
		moz.perform();
		
		/*
		 * // put results into modelProperties object (item of a list of such)
		
		// put(dsomGuid, modelProperties);
		
		 */
		// handling requests about persistence: saving/sending model, results
	}
	

	private void performAssociativeStorage(SomFluidTask sfTask) {
		 
		
		new SomAssociativeStorage( this, sfFactory, sfProperties, createSomDataObject() ); 
	}

	// ========================================================================
	public String addTask(SomFluidTask somFluidTask) {
		 
		somFluidTask.guidID = GUID.randomvalue() ;
		somTasks.add(somFluidTask);
		
		
		out.print(2, "...now there are "+somTasks.size()+" tasks in the SomFluid-queue...") ;
		out.delay(100) ;
		
		isActivated = true;
		
		return somFluidTask.guidID  ;
	}


	@Override
	public void onTaskCompleted( SomFluidTask sfTask ) {
		 
		out.printErr(1, "\nSomFluid task has been completed, returning instance : "+ sfTask.getDescription()+", "+sfTask.getSomHost().toString().replace("org.NooLab.somfluid.", "")+"\n" );
		
	}




	@Override
	public void start() {
		if (processIsActivated){
			return;
		}
		processIsActivated=true;
		if(processIsRunning==false){
			sfThread.start();
			isActivated = true;
		}
	}

	
	@Override
	public void run() {
		
		boolean isWorking = false;
		SomFluidTask sfTask = null;
		
		processIsRunning=true;
		
		try{
			
			while (processIsRunning){
				
				// out.print(2,"task dispatching process is running (tasks:"+somTasks.size()+") ");
				
				if ((isWorking==false) && (isActivated)){//) && (isInitialized)){
					isWorking=true;
											out.print(5,"...tdp (2)") ;
					if ((somTasks!=null) && (somTasks.size()>0)){
										    out.print(5,"...tdp (3)") ;
						sfTask = somTasks.getItem(0) ;
						
						out.print(2,"working on task, id = "+sfTask.guidID);
						TaskDispatcher td = new TaskDispatcher(sfTask);
											out.print(5,"...tdp (4)") ;
						if (td.isWorking==false){
											out.print(5,"...tdp (91)") ;
							isWorking=false;
						}
					}else{
						isWorking=false;
					}
					
					
				}
				
				if ((isWorking) && (somTasks.size()>0) && (somTasks.getItem(0).isCompleted())){
											out.print(5,"...tdp (6)") ;
					out.print(2,"task ("+somTasks.getItem(0).guidID+") has been completed.\n");
					somTasks.remove(0);
					isWorking=false;
				}
				out.delay(500);
			}
			
											out.print(5,"...tdp (-1)") ;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// ..........................................
	class TaskDispatcher{

		private boolean isWorking = false;

		public TaskDispatcher(SomFluidTask sfTask) {
			 
			
			try{
				
				// dependent on task we invoke different methods and worker classes
				if (sfTask.somType == SomFluidProperties._SOMTYPE_MONO){
					
					// accessing the perssistent file,
					// it may be an external file containing raw data, or
					// if sth exists an already prepared one
					if (sfTask.workingMode == SomFluidTask._SOM_WORKMODE_FILE){ // ==default
						// r = sfFactory.loadSource();
					}
					
					if (sfTask.workingMode == SomFluidTask._SOM_WORKMODE_PIKETT){ 
						// goto standby mode for this task
						sfTask.isStandbyActive = true;
					}
					
					if (sfTask.getSpelaLevel()<=1){
					// preparing the data, at least transforming and normalizing it
					// is embedded into the SomDataObject, where it is called by
					// importDataTable()
					// (of course, it can be called separately too,
					
						performTargetedModeling( sfTask );
						this.isWorking=true;
					}
					if (sfTask.getSpelaLevel()==2){
						performModelOptimizcreener(sfTask) ;
						this.isWorking=true;
					}
				}else{
					sfFactory.openSource(); // just prepares access, includes connection to dir, db
					performAssociativeStorage( sfTask );
					this.isWorking=true;
				}
				
				
			}catch(Exception e){
				e.printStackTrace();
				this.isWorking=false;
			}
			
		}


		


		
		
	} // inner class TaskDispatcher
	 

	// ========================================================================


	
	public SomDataObject createSomDataObject() {
		SomDataObject _somDataObject;
		
		
		_somDataObject = new SomDataObject(sfProperties) ;
		
		_somDataObject.setFactory(sfFactory);
		_somDataObject.setOut(out);
		 
		_somDataObject.prepare();
		
		somDataObjects.add(_somDataObject) ;
		
		_somDataObject.setIndex(somDataObjects.size()-1);
		
		return _somDataObject;
	}

	// ========================================================================
	
	
	
	public SomDataObject loadSource( String srcname ) throws Exception{
		
		SomDataObject somDataObject;
		int result=-1;
		String srcName ="";
		String loadedsrc ;
		//  
		srcName = srcname;
		if ((srcName.length()==0) || (DFutils.fileExists(srcName)==false)){
			srcName = sfProperties.getDataSrcFilename();
		}
		
		
		// check whether there is already an SDO
		/*
		if (somDataObjects==null){
			somDataObjects = new ArrayList<SomDataObject>();
		}
		for (int i=0;i<somDataObjects.size();i++){
			somDataObject = somDataObjects.get(i) ;
			loadedsrc = somDataObject.getDataReceptor().getLoadedFileName() ;
			if (loadedsrc.contentEquals(srcname)){
				// return _somDataObject;
			}
			somDataObjects.get(0).clear();
		}
		
		*/
		
		somDataObject = createSomDataObject() ;
	
		
		SomTransformer transformer = new SomTransformer( somDataObject );

		somDataObject.setTransformer(transformer) ;
		
		DataReceptor dataReceptor = new DataReceptor( sfProperties, somDataObject );
		
		dataReceptor.loadFromFile(srcName);

		somDataObject.setDataReceptor(dataReceptor);

		somDataObject.acquireInitialVariableSelection();

		return somDataObject;

	}
 

	public SomDataObject getSomDataObject(int index) {
		SomDataObject _somDataObject=null;
		if ((index>=0) && (index<somDataObjects.size())){
			_somDataObject = somDataObjects.get(index) ;
		}
		
		return _somDataObject;
	}

 
	
	// --- Events from RepulsionField / physical particle field ---------------
	
	

	/**
	 * @return the somDataObjects
	 */
	public ArrayList<SomDataObject> getSomDataObjects() {
		return somDataObjects;
	}




	/**
	 * @param somDataObjects the somDataObjects to set
	 */
	public void setSomDataObjects(ArrayList<SomDataObject> somDataObjects) {
		this.somDataObjects = somDataObjects;
	}




	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	// ,
  	// these are public, but clients anyway have access only through the factory,
	// and the factory provides only the usage interface
	public SomAppValidationIntf getSomValidationInstance(){
		if (somApplication==null){
			somApplication = new SomApplication();
		}
		return (SomAppValidationIntf)somApplication ;
	}
	public SomAppUsageIntf getSomUsageInstance(){
		if (somApplication==null){
			somApplication = new SomApplication();
		}
		return (SomAppUsageIntf)somApplication ;
	}

	// ------------------------------------------------------------------------
	
	public PrintLog getOut() {
		return out;
	}

	public void registerDataReceptor(DataFileReceptorIntf datareceptor ) {
		somDataObjects.get(0).registerDataReceptor(datareceptor );
				
	}




	/**
	 * @return the particleField
	 */
	public RepulsionFieldIntf getParticleField() {
		return particleField;
	}


	// ------------------------------------------------------------------------
	// callbacks from RepulsionField 

	@Override
	public void onLayoutCompleted(int flag) {
		
	}




	@Override
	public void onSelectionRequestCompleted(Object results) {
		
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
		
		if (sfFactory.getPhysicalFieldStarted()==0){
			out.print(2,"Calculations (SomFluid as event mgr) in particle field have been completed.");
			
		}
		sfFactory.setPhysicalFieldStarted(1);
		
		sfFactory.getFieldFactory().setInitComplete(true);
		
	}

	@Override
	public boolean getInitComplete() {
		 
		return false;
	}
	// ------------------------------------------------------------------------

	public void setUserbreak(boolean flag) {
		userBreak = flag;
	}
	public boolean getUserbreak() {
		
		return userBreak;
	}
 
	
	// ------------------------------------------------------------------------
	
	
	
}

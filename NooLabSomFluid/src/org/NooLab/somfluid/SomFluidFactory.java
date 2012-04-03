package org.NooLab.somfluid;

 
import java.util.ArrayList;
import java.util.Random;
 
 
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.application.SomAppUsageIntf;
import org.NooLab.somfluid.core.application.SomAppValidationIntf;
 
import org.NooLab.somfluid.env.communication.GlueClientAdaptor;
import org.NooLab.somfluid.env.communication.GlueBindings;
import org.NooLab.somfluid.env.communication.LatticeProperties;
import org.NooLab.somfluid.env.data.DataFileReceptorIntf;
import org.NooLab.somfluid.env.data.DataReceptor;
import org.NooLab.somfluid.storage.SomPersistence;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;

/**
 * 
 * 
 * 
 * 
 * 
 */
public class SomFluidFactory  implements 	// 
											SomFluidFactoryClientIntf{

	
	public static final int _INSTANCE_TYPE_SOM       = 1;
	public static final int _INSTANCE_TYPE_SPRITE    = 2;
	public static final int _INSTANCE_TYPE_OPTIMIZER = 3;
	public static final int _INSTANCE_TYPE_TRANSFORM = 4;
	
	public static final int _GLUE_MODULE_ENV_NONE    = 0;
	public static final int _GLUE_MODULE_ENV_CLIENT  = 1;
	
	
	private RepulsionFieldIntf physicalField ;
	private PhysicalFieldFactory fieldFactory;  
	RepulsionFieldEventsIntf somEventSink =null ;
	
	int physicalFieldStarted = 0;
	
	public SomFluidProperties sfProperties;
	SomFluidTask somFluidTask = null;
	int somType = -1;
	
	SomFluidFactory factory;
	int factoryMode;
	SomFluid somFluidModule;
	SomPersistence persistence;
	
	//
	
	DataFileReceptorIntf dataReceptor;
	
	
	int instanceType   = -1;
	int glueModuleMode = 0;
	
	GlueClientAdaptor glueClient ;
	GlueBindings glueBindings;
	
	PrintLog out = new PrintLog(2, true);
	private Random random;
	
	// ------------------------------------------------------------------------
	private SomFluidFactory(SomFluidProperties props, int factorymode){
		 
		// factorymode -> providing different interfaces active = producing, or passive
		
		// ************* iwll be solved differently
		// if (glueModuleMode == SomFluidFactory._GLUE_MODULE_ENV_CLIENT){ 
		// setting up "wireless" = file-free connectivity by the Glue messaging system
		// glueConnection = sfProperties.getGlueConnection( GlueConnection._GLUEX_DUAL ); 
		// *************
		
		if (props.getMessagingActive()){
			glueBindings = new GlueBindings( this, props);
			/* this is purely physical, 
			 	- no logics, 
				- no schemes for interpreting, or composing messages
				- no interfaces
			*/
			glueClient = new GlueClientAdaptor( glueBindings, props) ;
			glueClient.start();
			
		}
		
		sfProperties = props;
		factoryMode = factorymode;
		
		factory = this;
		sfProperties.setFactoryParent( this );
		
		
		// creating the Factory for the RepulsionField, ..it will not be created right now,
		// only upon "completingInitialization()"
		fieldFactory = new PhysicalFieldFactory() ;
		
		persistence = new SomPersistence(sfProperties) ;
		
		// we need to create the SomFluid object in a rudimentary form right now,
		// because we need it as a event sink for the "PhysicalFieldFactory()"
		somFluidModule = new SomFluid( this );
		 
		random = new Random();
		random.setSeed(192837465) ;
		
		out.setPrefix("[SomFluid-factory]");
	}
	
	public static SomFluidFactory get(SomFluidProperties props){
		
		
		
		return new SomFluidFactory( props,1 );
	}
	 
	public static SomFluidFactoryClientIntf get(){
		// we return a restricted interface ... which offers just the construction of some objects,
		// BUT NOT of the SomFluid !!!
		
		SomFluidProperties sfprops = new SomFluidProperties ();
		return  (SomFluidFactoryClientIntf)(new SomFluidFactory( sfprops,3 ));
	}

	/**
	 * returns a factory that can be used to access a SOM for classifying data, but NOT to run modeling 
	 * @return
	 */
	public static SomFluidFactoryClassifierIntf getSomClassService(){

		SomFluidProperties sfprops = new SomFluidProperties ();
		return  (SomFluidFactoryClassifierIntf)(new SomFluidFactory( sfprops,5 ));

	}
	
	
	public SomAppUsageIntf getSomApplication() {
		
		return somFluidModule.getSomUsageInstance();
	}

	public SomAppValidationIntf getSomValidator() {
		return somFluidModule.getSomValidationInstance();
	}

	protected SomFluidIntf getSomFluid( ){
		
		
		if (somFluidModule==null){
			 
			somFluidModule = new SomFluid( this);
			 
		}
		
		
		somFluidModule.start(); //just starts the loop that will accept the tasks
		
		return somFluidModule ;
	}
	
	 
	// ------------------------------------------------------------------------
	
	public RepulsionFieldIntf createPhysicalField( RepulsionFieldEventsIntf eventSink, int initialNodeCount) { // 
		RepulsionFieldIntf physicalField;
		
		out.print(2, "starting the physical particles field... ") ;  
		
		if (eventSink!=null){
			somEventSink = eventSink;
		}
		
		physicalField = fieldFactory.createPhysicalField( this,somEventSink, initialNodeCount ); // somEventSink,
		
		while (physicalFieldStarted<=0){
			out.delay(10);
		}
		return physicalField;
	}
 
	public void establishPhysicalFieldMessaging( RepulsionFieldEventsIntf eventSink){
		somEventSink = eventSink;
		if (fieldFactory!=null){
			fieldFactory.defineEventMessagingEndpoint(eventSink) ;
		}
	}
	
	public int getPhysicalFieldStarted() {
		return physicalFieldStarted;
	}

	/**
	 * @param physicalFieldStarted the physicalFieldStarted to set
	 */
	public void setPhysicalFieldStarted(int physicalFieldStarted) {
		this.physicalFieldStarted = physicalFieldStarted;
	}

	public PhysicalFieldFactory getFieldFactory() {
		return fieldFactory;
	}

	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	public DataFileReceptorIntf getDataReceptor() {
		return dataReceptor;
	}

	

	/**
	 * 
	 */
	private void loadSomFluid(){
		
		// first properties
		
		// then the appropriate SomFluid Container: _SOMTYPE_MONO=1 , or _SOMTYPE_PROB=2
		
	}

	private void saveSomFluid(){
		
		// first properties
		
		// then the appropriate SomFluid Container: _SOMTYPE_MONO=1 , or _SOMTYPE_PROB=2
		
	}



	public SomDataDescriptor getSourceDescriptor() {
		return getSourceDescriptor(0) ; 
	}
	
	public SomDataDescriptor getSourceDescriptor(int index) {
		SomDataObject _somDataObject;
		_somDataObject	= somFluidModule.getSomDataObject(index) ;
		SomDataDescriptor sdd = _somDataObject.getSomDataDescriptor();
		return sdd;
	}

		
	public void addModelingTargetByVariable(String tvLabel) {
		
		ArrayList<String> tvc = sfProperties.getModelingSettings().getTargetVariableCandidates();
		if (tvc.indexOf(tvLabel)<0){
			tvc.add(tvLabel);
		}
		if (tvc.size()==1){
			sfProperties.getModelingSettings().setActiveTvLabel( tvLabel );
		}
	}
	public void setModelingTargetByVariable(String tvLabel) {
		// 
		ArrayList<String> tvc = sfProperties.getModelingSettings().getTargetVariableCandidates();
		sfProperties.getModelingSettings().setActiveTvLabel( tvLabel );
		if (tvc.indexOf(tvLabel)<0){
			tvc.add(tvLabel);
		}
	}
	
	public  <T> Object createTask() {
		// SomFluidTask
		return createTask("");
	}
	
	// optionally, we give it a name
	@SuppressWarnings("unchecked")
	public <T> Object  createTask( String guidId) {
		SomFluidMonoTaskIntf mono;
		SomFluidProbTaskIntf prob;
		
		int stype = sfProperties.getSomType() ;
		
		if (stype == SomFluidProperties._SOMTYPE_MONO){
			mono = (SomFluidMonoTaskIntf)(new SomFluidTask(guidId,SomFluidProperties._SOMTYPE_MONO));
			return mono;
		}else{
			prob = (SomFluidProbTaskIntf)(new SomFluidTask(guidId,SomFluidProperties._SOMTYPE_PROB));
			return prob;
		}
		 
	}

	public static boolean implementsInterface(Object object, Class interf){
	    return interf.isInstance(object);
	    // this does not support perspectives
	}

	
	/** 
	 * takes the task and produces the SOM, usually, if not set otherwise, 
	 * it also will start the process
	 * @return 
	 */
	public  void produce( Object sfTask ) {
	
		SomFluidTask somFluidTask ;
		
		// First caring about the data, using the transformer module
		
		
		
		// now heading towards the SomFluid
		somFluidTask = (SomFluidTask)sfTask;
		getSomFluid();
		
		if (somFluidTask.getStartMode() == 1){
			
			// somFluidModule.start();   // nothing happens without providing a task
			somFluidModule.addTask(  somFluidTask );
			
		}else{
			if (somFluidTask.getStartMode() >=100){
				// set a delayed start in a dedicated process
				InternalSomFluidModuleStarter _starter = new InternalSomFluidModuleStarter( somFluidTask ); 
			}
		}
	}

	class InternalSomFluidModuleStarter implements Runnable {
		
		Thread sfmStarter;
		int _delay;
		SomFluidTask somFluidTask;
		
		public InternalSomFluidModuleStarter( SomFluidTask sft ){
		
			somFluidTask = sft; 
			_delay = somFluidTask.getStartMode() ;
			
			sfmStarter = new Thread(this,"sfmStarter") ;
			sfmStarter.start() ;
		}

		@Override
		public void run() {
			 
			out.delay(_delay) ;
			somFluidModule.start();   // nothing happens without providing a task
			somFluidModule.addTask( somFluidTask );
		}
	}
	/**
	 *  it will create a spin-off, almost (!) as a copy (except more or less small random variations),
	 *  if it is the first offspring, or as a weighted cross-over with a rather similar one 
	 */
	public void replicate() {
		// 
		
	}

	
	// ------------------------------------------------------------------------
	
	@Override
	public SomFluidRequestPackageIntf createRequestPackage() {
		
		SomFluidRequestPackage rqPackage ;
		
		rqPackage = new SomFluidRequestPackage(this) ; 
		
		return (SomFluidRequestPackageIntf) rqPackage;
	}

	
	// ------------------------------------------------------------------------
	
	public int getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(int instanceType) {
		this.instanceType = instanceType;
	}

	public int getglueModuleMode() {
		return glueModuleMode;
	}

	public void setglueModuleMode(int gluemode) {
		this.glueModuleMode = gluemode;
	}



	public void openSource() {
		// just prepares access, includes connection to dir, db
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the out
	 */
	public PrintLog getOut() {
		return out;
	}

	public Random getRandom() {

		return random;
	}


	
	
	
}

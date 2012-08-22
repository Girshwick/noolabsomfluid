package org.NooLab.somfluid;

 
import java.util.ArrayList;
import java.util.Random;
 
 
import org.NooLab.field.FieldIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.somfluid.app.IniProperties;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.app.SomAppUsageIntf;
import org.NooLab.somfluid.app.SomAppValidationIntf;
import org.NooLab.somfluid.app.SomApplicationEventIntf;
 
 
import org.NooLab.somfluid.components.*;
 
import org.NooLab.somfluid.env.communication.GlueClientAdaptor;
import org.NooLab.somfluid.env.communication.GlueBindings;
import org.NooLab.somfluid.env.data.DataFileReceptorIntf;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.storage.SomPersistence;
import org.NooLab.somtransform.SomTransformerClientIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.resources.ResourceLoader;

/**
 * 
 * required heap size for the package is 500Mb
 * use the following parameters to start the JVM !!!
 * 
 * -Xmx640m
 * -XX:+ExplicitGCInvokesConcurrent
 * -XX:+UseConcMarkSweepGC
 * 
 * the "GC" parameters are important & needed for concurrent garbage collection
 * see package utilities.vm for creating shell-specific startup scripts
 * 
 *  
 * 
 * in Eclipse, there are 2 possibilities 
 * 1- go to Windows > preferences > Java > installed JREs : select one > Edit > default VM arguments  "-Xmx640m"  
 * 2- run > run configurations : select the application/applet > arguments > VM arguments : "-Xmx640m"
 * 
 */
public class SomFluidFactory  implements 	// 
											SomFluidFactoryClientIntf,
											SomAppFactoryClientIntf,
											SomFluidFactoryClassifierIntf{
	
	
	public static final int _GLUE_MODULE_ENV_NONE     = 0;
	public static final int _GLUE_MODULE_ENV_CLIENT   = 1;
	
	
	PhysicalFieldFactory fieldFactory;  
	RepulsionFieldEventsIntf somEventSink =null ;
	
	int physicalFieldStarted = 0;
	
	SomFluidProperties sfProperties;
	SomAppProperties somAppProperties;
	
	SomFluidTask somFluidTask = null;
	int somType = -1;
	
	SomFluidFactory factory;
	int factoryMode;
	SomFluid somFluidModule;
	 
	SomPersistence persistence;
	
	//
	
	
	DataFileReceptorIntf dataReceptor;
	SomProcessControl somProcessControl ;
	SomObjects somObjects;
	SomApplicationEventIntf appInformer;
	
	
	int instanceType   = -1;
	int preparePhysicalParticlesField;

	int glueModuleMode = 0;
	GlueClientAdaptor glueClient ;
	GlueBindings glueBindings;
	
	DFutils fileutil = new DFutils();
	PrintLog out = new PrintLog(2, true, "[SomFluid-factory]"); 
	private Random random;
	
		
	// ------------------------------------------------------------------------
	
	private SomFluidFactory(SomFluidProperties props, int factorymode){
		 
		// factorymode -> providing different interfaces active = producing, or passive
		
		// ************* will be solved differently
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
		
		somProcessControl = new SomProcessControl(this); 
		
		somObjects = new SomObjects(factory) ;
		
		try {
			(new AlgorithmDeclarationsLoader(sfProperties)).load() ;
			
		} catch (Exception e) {
			out.printErr(1, e.getMessage()+"... trying to recover...");
			
			try {
				// last minute check for resources
				if (checkAlgorithmsConfigPath()) {

					(new AlgorithmDeclarationsLoader(sfProperties)).load();

				} else {
					out.printErr(1, e.getMessage() + "attempt to recover failed, thus exiting...");
					System.exit(-7);
				}
				
			} catch (Exception e1) {
				out.printErr(1, e.getMessage() + "attempt to recover failed, thus exiting...");
				System.exit(-7);
			}
			String algorithmsConfigPath = sfProperties.getAlgorithmsConfigPath();
			out.printErr(1, "The attempt to recover succeeded, folder for declaration files is now : "+algorithmsConfigPath);
			
		}
		
		if ((factorymode==FieldIntf._INSTANCE_TYPE_OPTIMIZER) ||
			(factorymode==FieldIntf._INSTANCE_TYPE_SOM)){
			
			preparePhysicalParticlesField = 1;
		}
		// we need to create the SomFluid object in a rudimentary form right now,
		// because we need it as a event sink for the "PhysicalFieldFactory()"
		
		somFluidModule = new SomFluid( this );
		
		prepareApplicationPublishing();
		
		random = new Random();
		random.setSeed(192837465) ;
		
		out.setPrefix("[SomFluid-factory]");
	}
	
	 

	private void prepareApplicationPublishing() {
		 
		OutputSettings outs = sfProperties.getOutputSettings() ;
		
		boolean appubb = ((factoryMode==FieldIntf._INSTANCE_TYPE_OPTIMIZER) ||
						  (factoryMode==FieldIntf._INSTANCE_TYPE_SOM)) ;

		// we only need a publishing dir if we are SOM or OPTIMIZER...
		
		if ((somAppProperties==null) && (appubb) && 
			(sfProperties.getSomType()==2) &&  // _SOMTYPE_MONO (not: _SOMTYPE_PROB !) 
			(outs.getAppPublishing().isActive()) ){
				
			String path = outs.getAppPublishing().getPublishingBasepath().trim() ;
			if ((fileutil.direxists(path)==false) ){
				if ((path.length()==0) || (path.indexOf("/")<0)){
					path = sfProperties.getFileOrganizer().getProjectBaseDir() ;
					path = fileutil.createpath( path, sfProperties.getFileOrganizer().getProjectDirName() );
					path = fileutil.createpath( path, "export/application/");
				}
			}
			outs.setAppPublishing( new SomAppPublishing( this, true, 
															path, // "D:/data/projects/_classifierTesting", 
															// name of package and dedicated version string
															sfProperties.getPersistenceSettings().getProjectName(), "",        
															SomAppPublishing._LOCATION_FILE) );

		}
		
	}



	public SomFluidFactory(SomAppProperties appProperties) {
	 
		// glue stuff ...
		int k;
		k=0;
		try {
			
			 
			factoryMode = FieldIntf._INSTANCE_TYPE_CLASSIFIER ;
			sfProperties = appProperties.getPropertiesConnection() ;
 			somAppProperties = appProperties;
 			
			factory = this;
			sfProperties.setFactoryParent( this );
			appProperties.setFactoryParent( this );
			 
			
			
			// needed: a common interface for this aspect
			(new AlgorithmDeclarationsLoader(sfProperties)).load() ;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-7);
		}
		
		
		random = new Random();
		random.setSeed(192837465) ;
		
		out.setPrefix("[SomFluid-appfactory]");
	}

	public static SomFluidFactory get(SomFluidProperties props, int factoryForInstance){
		return new SomFluidFactory( props,factoryForInstance );
	}

	public static SomFluidFactory get(SomFluidProperties props){
		
		return new SomFluidFactory( props,1 );
	}
	 
	// ------------------------------------------------------------------------
	/**
	 * using a particular properties class dedicated to the application of models, this
	 * method delivers a suitable flavor of the factory
	 * 
	 * @param clappProperties
	 * @return
	 */
	public static SomAppFactoryClientIntf get(SomAppProperties clappProperties) {
		 
		return (new SomFluidFactory(clappProperties));
	}



	// ------------------------------------------------------------------------

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
	public SomAppUsageIntf getSomApplication() {
		
		return somFluidModule.getSomUsageInstance();
	}

	public SomAppValidationIntf getSomValidator() {
		return somFluidModule.getSomValidationInstance();
	}

	
	
	public SomApplicationIntf createSomApplication( SomAppProperties properties){
		 
		return (new SomApplication( this, properties)) ;
	}
	
	@Override
	public String runSomApplication() {
		 
		return null;
	}


	public SomTransformerClientIntf getSomTransformer() {

		
		return null;
	}
	
	public SomTransformerClientIntf createSomTransformerClient() {
		
		preparePhysicalParticlesField = 0;
		
		if (somFluidModule==null){
			 
			somFluidModule = new SomFluid(this);
			
		}
		return somFluidModule.getSoappTransformer();
	}
	

	protected SomFluidIntf getSomFluid(){ // int preparePhysicalParticlesField
		
		
		if (somFluidModule==null){
			 
			somFluidModule = new SomFluid( this);
			 
		}
		
		
		somFluidModule.start(); //just starts the loop that will accept the tasks
		
		return somFluidModule ;
	}
	
	 
	// ------------------------------------------------------------------------

	 
	public static String loadStartupTrace(int instanceTypeSom) throws Exception{
		
		String userdir, cfgTraceFile,traceInfo="";
		DFutils fileutil = new DFutils();
		
		try{

			userdir = fileutil.getUserDir();
			
			cfgTraceFile = fileutil.createpath(userdir, "somfluid-"+instanceTypeSom+".startup") ;
			
			traceInfo = fileutil.readFile2String( cfgTraceFile ); 
			
			fileutil = null;
			
		}catch(Exception e){
			
		}
		
		return traceInfo;
	}

	public void saveStartupTrace(int instanceTypeSom, String traceInfo) {
		
		String userdir, cfgTraceFile;
		
		userdir = fileutil.getUserDir();
		
		cfgTraceFile = fileutil.createpath(userdir, "somfluid-"+instanceTypeSom+".startup") ;
		
		
		fileutil.writeFileSimple(cfgTraceFile,traceInfo); 
		
	}

	/**
	 * 
	 * @param projectname simply the name without path information
	 */
	public Object loadTaskTrace(String projectname) throws Exception{
		// 
		String dir="",fileName="";
		int stype = sfProperties.getSomType();

		FileOrganizer fileorg = new FileOrganizer ();
		fileorg.setPropertiesBase(sfProperties);
		dir = fileorg.getObjectStoreDir();

		dir = DFutils.createPath( dir, "task/"+stype+"/");
		
		DFutils.reduceFileFolderList( dir,1,20) ;
		
		fileName = DFutils.createPath( dir, SomFluidTask._TRACEFILE);
		
		
		// now loading the desired properties into a new object;
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		Object taskObj = storageDevice.loadStoredObject( fileName) ;

		return taskObj;
	
	}

	/**
	 * 
	 * gets automatically called when a task is getting produced by the factory
	 * @param sfTask
	 */
	public void saveTaskTrace( Object sfTask  ) {
	
		String dir="",fileName="";
		int stype = sfProperties.getSomType();

		FileOrganizer fileorg = new FileOrganizer ();
	 
		if (somAppProperties!=null){
			fileorg.setPropertiesBase(somAppProperties);
		}else{
			fileorg.setPropertiesBase(sfProperties);
		}
	 
		
		dir = fileorg.getObjectStoreDir();

		dir = DFutils.createPath( dir, "task/"+stype+"/");
		fileName = DFutils.createPath( dir, SomFluidTask._TRACEFILE);
		
		
		
		// now loading the desired properties into a new object;
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		storageDevice.storeObject( sfTask, fileName) ;
		
		DFutils.reduceFileFolderList( dir,1,".trace",20) ;
	}

	public PhysicalGridFieldIntf createPhysicalField( RepulsionFieldEventsIntf eventSink, int initialNodeCount) { // 
		// RepulsionFieldIntf physicalField;
		PhysicalGridFieldIntf physicalField;
		
		out.setPrefix("[SomFluid-factory]") ;
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
	
	public void setMessagePort( SomApplicationEventIntf msgCallbackIntf ) {
		 
		appInformer = msgCallbackIntf;
	}



	public int getPhysicalFieldStarted() {
		return physicalFieldStarted;
	}

	/**
	 * @param physicalFieldStarted the physicalFieldStarted to set
	 */
	public void setPhysicalFieldStarted(int startedFlag) {
		physicalFieldStarted = startedFlag;
	}

	public PhysicalFieldFactory getFieldFactory() {
		return fieldFactory;
	}

	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	public SomAppProperties getSomAppProperties() {
		return somAppProperties;
	}



	public DataFileReceptorIntf getDataReceptor() {
		return dataReceptor;
	}

	

	public SomProcessControlIntf getSomProcessControl() {
		return somProcessControl;
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

		
	public SomObjects getSomObjects() {
		return somObjects;
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

	public <T> Object  createTask( String guidId) {
		
		SomFluidMonoTaskIntf mono;
		SomFluidProbTaskIntf prob;
		
		int stype = sfProperties.getSomType() ;
		
		if (stype == FieldIntf._SOMTYPE_MONO){
			mono = (SomFluidMonoTaskIntf)(new SomFluidTask(guidId, FieldIntf._SOMTYPE_MONO));
			return mono;
		}else{
			prob = (SomFluidProbTaskIntf)(new SomFluidTask(guidId, FieldIntf._SOMTYPE_PROB));
			return prob;
		}
		 
	}

	
	public <T> Object createTask(int instancetype) {
		 
		return createTask(  instancetype, "");
	}
	
	public <T> Object createTask(int instancetype, String guidId) {
		String descr="The requested type of instance to be created is unknown, no task has been created." ;
		
		SomFluidClassTaskIntf somclass=null;
		
		if (instancetype == FieldIntf._INSTANCE_TYPE_CLASSIFIER ){
			
			somclass = (SomFluidClassTaskIntf) (new SomFluidTask( guidId, SomFluidTask._TASK_CLASSIFICATION ));
			descr="A task has been created, type = <TASK_CLASSIFICATION>";
		}
		if (instancetype == FieldIntf._INSTANCE_TYPE_OPTIMIZER ){
			
			somclass = (SomFluidClassTaskIntf) (new SomFluidTask( guidId, SomFluidTask._TASK_MODELING ));
			descr="A task has been created, type = <_TASK_MODELING>";
		}
		
		if (instancetype == FieldIntf._INSTANCE_TYPE_ASTOR ){
			
			somclass = (SomFluidClassTaskIntf) (new SomFluidTask( guidId, SomFluidTask._TASK_SOMSTORAGEFIELD ));
			descr="A task has been created, type = <_TASK_ASTOR>";
		}
		 
		
		out.print(2, descr) ;
		return somclass;
	}


	// ------------------------------------------------------------------------
	public static boolean implementsInterface(Object object, Class<?> interf){
	    return interf.isInstance(object);
	    // this does not support perspectives
	}

	
 
	
	/** 
	 * takes the task and produces the SOM, usually, if not set otherwise, 
	 * it also will start the process
	 * @return 
	 * @throws Exception  
	 */
	public  void produce( Object sfTask ) throws Exception {
	
		SomFluidTask somFluidTask ;
		
				
		saveTaskTrace(sfTask);
		
		// First caring about the data, using the transformer module
		
		
		// now heading towards the SomFluid
		somFluidTask = (SomFluidTask)sfTask;
		 
		preparePhysicalParticlesField = 1;
		if (somFluidTask.taskType.toLowerCase().startsWith("c")){
			preparePhysicalParticlesField = 0;
		}
		
		// creating the main module and starting the task loop
		getSomFluid(); // obs__ (preparePhysicalParticlesField)
		 
		
		if (somFluidTask.getStartMode() == 1){
			
			// somFluidModule.start();   // nothing happens without providing a task
			somFluidModule.addTask( somFluidTask );
			
		}else{
			if (somFluidTask.getStartMode() >=100){
				// set a delayed start in a dedicated process
				InternalSomFluidModuleStarter _starter = new InternalSomFluidModuleStarter( somFluidTask ); 
			}
		}
	}

	private boolean checkAlgorithmsConfigPath() throws Exception {
		
		boolean rB=false;
		String outpath="";
		// loading "builtinscatalog.xml" which is necessary for global indexing and activation of built-in algorithms
		String algorithmsConfigPath  = sfProperties.getAlgorithmsConfigPath(); 
		String bic = fileutil.createpath(algorithmsConfigPath , "builtinscatalog.xml"); 
			
			
		if ((fileutil.direxists(algorithmsConfigPath )==false) || (fileutil.fileexists(bic)==false)){
			ProjectSpaceMaintenance psm = new ProjectSpaceMaintenance();
			
			ResourceLoader rsrcLoader = new ResourceLoader();
			rB = rsrcLoader.loadTextResource( this.getClass(), "org/NooLab/somtransform/resources/builtinscatalog-xml") ;
			if (rB){
				
				// create a directory structure, create the file and adjust the settings
				if (fileutil.direxists(algorithmsConfigPath )){
					outpath = algorithmsConfigPath;
				}else{
					psm = new ProjectSpaceMaintenance();
					psm.establishCatalogFolder();
				}
				
				String filename = fileutil.createpath( outpath,"builtinscatalog.xml" );
				
				fileutil.writeFileSimple(filename, rsrcLoader.getTextResource()) ;
				rB = fileutil.fileexists(filename) ;
			}
		}
		
		return rB;
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

	
	public void interrupt(){
		somFluidModule.setUserbreak(true);
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

	public void publishReport(String xmlReportStr) {
		// 
		
	}
 
	public SomAppValidationIntf createSomApplicationForValidation() {
		return  (SomAppValidationIntf)(new SomApplicationDsom());
	}



	public void setSomApplication(SomApplication soapp) {
	 
		
	}



	public static int createProjectSpace(String psLabel) {
		int projectSpaceOk = -1;
		
		try{
		
			// create directories, catalog files from resources 
			// dir structure is also from resources
			
			
			
			// ... and ask for a data file to copy into the project space
			
			
			
		}catch(Exception e){
			
		}
		
		
		
		return projectSpaceOk;
	}



	public static void duplicateProject(int mode) {
		//  SomFluidStartup && IniProperties provide the folder and prjLabel
		ProjectSpaceMaintenance psm = new ProjectSpaceMaintenance();
		psm.duplicateProject( SomFluidStartup.getProjectSpaceLabel() , mode) ;
	}
 
	 

	public static boolean projectSpaceExists(String psLabel) {
		boolean rB= true;
		try {
		
			ProjectSpaceMaintenance psm = new ProjectSpaceMaintenance();
			rB = psm.projectSpaceExists( psLabel ) >= 0;
			
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		return rB;
	}



	public static void completeProjectSpace() throws Exception {
		 
		 // SomFluidFactory.projectSpaceExists() 
		String pslabel = IniProperties.lastProjectName;
		
		ProjectSpaceMaintenance psm = new ProjectSpaceMaintenance();
		psm.completeProjectSpaceDirectories() ;

	}



	public static boolean organizeRawProjectData() {
		//
		String dataSourceFile = "" ;
		boolean rB=false;
		
		ProjectSpaceMaintenance psm = new ProjectSpaceMaintenance();
		psm.organizeRawProjectData();
		
		dataSourceFile = psm.getDataSourceFile();
		
		rB = DFutils.fileExists(dataSourceFile);
		
		if (rB){
			IniProperties.dataSource = dataSourceFile;
		}
		return rB;
	}




	
	
	
}

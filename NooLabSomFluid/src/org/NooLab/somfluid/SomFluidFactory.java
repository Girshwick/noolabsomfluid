package org.NooLab.somfluid;

 
import java.util.ArrayList;


import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.somfluid.components.PhysicalFieldFactory;
import org.NooLab.somfluid.components.SomTransformer;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.env.communication.LatticeProperties;
import org.NooLab.somfluid.env.data.DataFileReceptorIntf;
import org.NooLab.somfluid.env.data.DataReceptor;
import org.NooLab.somfluid.storage.SomPersistence;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;

public class SomFluidFactory {

	
	private RepulsionFieldIntf physicalField ;
	private PhysicalFieldFactory fieldFactory;  
	int physicalFieldStarted = 0;
	
	SomFluidProperties sfProperties;
	SomFluidTask somFluidTask = null;
	int somType = -1;
	
	SomFluid somFluidModule;
	SomPersistence persistence;
	LatticeProperties latticeProperties;
	
	
	DataFileReceptorIntf dataReceptor;
	SomTransformer transformer;
	
	PrintLog out = new PrintLog(2, true);
	
	// ------------------------------------------------------------------------
	private SomFluidFactory(SomFluidProperties props){
		
		sfProperties = props;
		
		sfProperties.setFactoryParent( this );
		
		persistence = new SomPersistence(sfProperties) ;
		
		// we need to create the SomFluid object in a rudimentary form right now,
		// because we need it as a event sink for the "PhysicalFieldFactory()"
		somFluidModule = new SomFluid( this );
		
		// creating the Factory for the RepulsionField, ..it will not be created right now,
		// only upon "completingInitialization()"
		fieldFactory = new PhysicalFieldFactory() ;
				
		dataReceptor = new DataReceptor( sfProperties, somFluidModule.somDataObject );
		transformer = new SomTransformer( this, somFluidModule.somDataObject );
		
		out.setPrefix("[SomFluid-factory]");
	}
	
	public static SomFluidFactory get(SomFluidProperties props){
		
		return new SomFluidFactory( props );
	}
	 
	
	protected SomFluidIntf getSomFluid(  ){
		
		long serialID=0;
		if (somFluidModule==null){
			
			serialID = SerialGuid.numericalValue();
			
			somFluidModule = new SomFluid( this, serialID);
			 
		}
		if (somFluidModule.getSerialID()<=0){
			
			serialID = SerialGuid.numericalValue();
			  
			somFluidModule.completingInitialization( serialID );
		}
		
		
		
		return somFluidModule ;
	}
	
	 
	// ------------------------------------------------------------------------
	
	public RepulsionFieldIntf createPhysicalField( RepulsionFieldEventsIntf somEventSink, int initialNodeCount) {
		
		out.print(2, "starting the physical particles field... ") ;  
		
		physicalField = fieldFactory.createPhysicalField( somEventSink, initialNodeCount );
		
		while (physicalFieldStarted<=0){
			out.delay(10);
		}
		return physicalField;
	}

	public RepulsionFieldIntf getPhysicalField() {
		return physicalField;
	}

 
	
	public int getPhysicalFieldStarted() {
		return physicalFieldStarted;
	}

	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	public DataFileReceptorIntf getDataReceptor() {
		return dataReceptor;
	}

	public SomTransformer getTransformer() {
		return transformer;
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

	public void loadSource() {
		
		dataReceptor.loadFromFile( sfProperties.getDataSrcFilename() );
		
		// now, the somDataObject knows about the DataTable
		// if there is some data in SomDataObject, it will be loaded into nodes
		
		somFluidModule.initializeNodesWithData(); // adopting feature vectors, not yet the data of course
		
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
	
		SomFluidTask somFluidfTask = (SomFluidTask)sfTask;
		getSomFluid();
		somFluidModule.start();   // nothing happens without providing a task
		

		
		somFluidModule.addTask(  somFluidfTask );
	}

	/**
	 *  it will create a spin-off, almost (!) as a copy (except more or less small random variations),
	 *  if it is the first offspring, or as a weighted cross-over with a rather similar one 
	 */
	public void replicate() {
		// 
		
	}
	


	
	
	
}

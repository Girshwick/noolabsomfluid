package org.NooLab.somfluid;

import java.io.Serializable;

import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.results.SomResultDigesterIntf;
import org.NooLab.utilities.net.GUID;


public class SomFluidTask 	implements 
										Serializable,
										SomFluidMonoTaskIntf,
										SomFluidProbTaskIntf,
										SomFluidClassTaskIntf,
										SomFluidTransformTaskIntf{

	private static final long serialVersionUID = -532876083346213662L;

	public static final String _TASK_CLASSIFICATION  = "C";
	public static final String _TASK_MODELING        = "M";
	public static final String _TASK_SOMSTORAGEFIELD = "S";
	 

	public static final int _SOM_WORKMODE_FILE   = 1;
	public static final int _SOM_WORKMODE_PIKETT = 2; // either by dir source or glue


	public static final String _TRACEFILE = "SomFluidTask.trace";



	public int workingMode = _SOM_WORKMODE_FILE;
	
	long opentime=0, closetime=-1;
	
	String guidID="";
	
	String description="" ;
	
	int startingMode = 1;
	
	int callerStatus = 0;
	 
	String transportedGuid="";
	
	int somType;
	String taskType;

	

	private int spelaLevel;

	private int numberOfRuns;

	private int derivatesDepth;

	SomHostIntf somHost=null;

	private boolean noHostInforming = false;

	transient private SomResultDigesterIntf somResultHandler;

	private int resumeMode=0;

	boolean isExported;
	long exportTime = 0;
	boolean isCompleted=false;
	int taskDispatched=0;
	boolean isStandbyActive =false;

	private int counter=-1;

	private boolean activateDataStreamReceptor;
 
	 
	
	// ========================================================================
	protected SomFluidTask(String guidId, int somType){
		
		guidID = guidId;
		if (guidID.length()==0){
			guidID = GUID.randomvalue();
		}
		
		this.somType = somType;
		if (somType > 0){
			taskType = "M";  
		}
		opentime = System.currentTimeMillis();
	}
	// ========================================================================


	
	public SomFluidTask(SomFluidTask inTask) {
		
		guidID = inTask.guidID;
		somType = inTask.somType;
		
		spelaLevel = inTask.spelaLevel ;
		numberOfRuns = inTask.numberOfRuns ;
		derivatesDepth = inTask.derivatesDepth ;
		taskType = inTask.taskType;
		opentime = System.currentTimeMillis();
		
		
		description = inTask.description ;
		
		
		startingMode = inTask.startingMode;
		callerStatus = inTask.callerStatus;
		transportedGuid = inTask.transportedGuid ;
		 
		 
		somHost = inTask.somHost ;
		noHostInforming = inTask.noHostInforming;
		somResultHandler = inTask.somResultHandler;
		resumeMode = inTask.resumeMode;
		isExported = inTask.isExported;
		
		isStandbyActive = inTask.isStandbyActive;
	}



	public SomFluidTask(String guid , String scid) {
		 
		if (scid.toLowerCase().startsWith("c")){
			opentime = System.currentTimeMillis();
			guidID = guid;
			taskType = scid; // there is "C"=classifier "S"=associative storage and "M"=modeling ;
		}
	}



	public boolean isCompleted() {
		// 
		return isCompleted;
	}

	public void setCompleted(boolean flag) {
		
		isCompleted = flag;
		
		if (isCompleted){
			closetime = System.currentTimeMillis() ;
		}
	}


	public String getGuidID() {
		return guidID;
	}


	@Override
	public void setContinuity(int level, int numberOfRuns) {
		//  
		spelaLevel = level;
		this.numberOfRuns = numberOfRuns ;
	}

 

	@Override
	public void setContinuity(int level, int derivatesDepth, int numberOfRuns) {

		spelaLevel = level;
		this.numberOfRuns = numberOfRuns ;
		this.derivatesDepth = derivatesDepth;
	}



	@Override
	public void setStartMode(int startingmode) {
		
		startingMode = startingmode;
	}



	@Override
	public int getStartMode() {
		
		return startingMode;
	}
	

	// -----------------------------------
 


	@Override
	public void setResultsReceptor(SomFluidResultsIntf deliveryTarget) {
		// ?
		
	}

	
	@Override
	public String getGuid() {
		return guidID;
	}

	
	public int getCallerStatus() {
		return callerStatus;
	}


	public void setCallerStatus(int callerStatus) {
		this.callerStatus = callerStatus;
	}


	/**
	 * @return the workingMode
	 */
	public int getWorkingMode() {
		return workingMode;
	}


	/**
	 * @param workingMode the workingMode to set
	 */
	public void setWorkingMode(int workingMode) {
		this.workingMode = workingMode;
	}



	/**
	 * @return the startingMode
	 */
	public int getStartingMode() {
		return startingMode;
	}



	/**
	 * @param startingMode the startingMode to set
	 */
	public void setStartingMode(int startingMode) {
		this.startingMode = startingMode;
	}



	/**
	 * @return the somType
	 */
	public int getSomType() {
		return somType;
	}



	/**
	 * @param somType the somType to set
	 */
	public void setSomType(int somType) {
		this.somType = somType;
	}



	/**
	 * @return the isStandbyActive
	 */
	public boolean isStandbyActive() {
		return isStandbyActive;
	}



	/**
	 * @param isStandbyActive the isStandbyActive to set
	 */
	public void setStandbyActive(boolean isStandbyActive) {
		this.isStandbyActive = isStandbyActive;
	}



	/**
	 * @return the spelaLevel
	 */
	public int getSpelaLevel() {
		return spelaLevel;
	}



	/**
	 * @param spelaLevel the spelaLevel to set
	 */
	public void setSpelaLevel(int spelaLevel) {
		this.spelaLevel = spelaLevel;
	}



 


	/**
	 * @return the numberOfRuns
	 */
	public int getNumberOfRuns() {
		return numberOfRuns;
	}



	/**
	 * @param numberOfRuns the numberOfRuns to set
	 */
	public void setNumberOfRuns(int numberOfRuns) {
		this.numberOfRuns = numberOfRuns;
	}



	/**
	 * @param guidID the guidID to set
	 */
	public void setGuidID(String guidID) {
		this.guidID = guidID;
	}



	public long getOpentime() {
		return opentime;
	}



	public void setOpentime(long opentime) {
		this.opentime = opentime;
	}



	public long getClosetime() {
		return closetime;
	}



	public void setClosetime(long closetime) {
		this.closetime = closetime;
	}



	public String getTaskType() {
		return taskType;
	}



	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}



	/**
	 * @return the derivatesDepth
	 */
	public int getDerivatesDepth() {
		return derivatesDepth;
	}



	/**
	 * @param derivatesDepth the derivatesDepth to set
	 */
	public void setDerivatesDepth(int derivatesDepth) {
		this.derivatesDepth = derivatesDepth;
	}



	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}



	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}



	/**
	 * @return the somHost
	 */
	public SomHostIntf getSomHost() {
		return somHost;
	}



	/**
	 * @param somHost the somHost to set
	 */
	public void setSomHost(SomHostIntf somHost) {
		this.somHost = somHost;
	}



	public void setNoHostInforming(boolean flag) {
		noHostInforming = flag;
	}



	/**
	 * @return the noHostInforming
	 */
	public boolean isNoHostInforming() {
		return noHostInforming;
	}



	public void setSomResultDigester( SomResultDigesterIntf resultHandler) {
		 
		somResultHandler = resultHandler;
	}
 
	public SomResultDigesterIntf getSomResultHandler() {
		return somResultHandler;
	}

 
	public void setSomResultHandler(SomResultDigesterIntf resultHandler) {
		this.somResultHandler = resultHandler;
	}



	@Override
	public void setResumeMode(int modeOnOff) {
		resumeMode = modeOnOff;
	}
 
	public int getResumeMode() {
		return resumeMode;
	}



	public boolean isExported() {
		return isExported;
	}

	public void setExported(boolean flag) {
		isExported = flag;
		exportTime = System.currentTimeMillis();
	}



	public long getExportTime() {
		return exportTime;
	}

	public void setExportTime(long timestamp) {
		exportTime = timestamp;
	}

	public static boolean taskIsClassification(String typeid){
		return typeid.toLowerCase().contentEquals( _TASK_CLASSIFICATION.toLowerCase()) ;
	}
	public static boolean taskIsModeling(String typeid){
		return typeid.toLowerCase().contentEquals( _TASK_MODELING.toLowerCase()) ;
	}
	public static boolean taskIsSomStorageFields(String typeid){
		return typeid.toLowerCase().contentEquals( _TASK_SOMSTORAGEFIELD.toLowerCase()) ;
	}



	public String getTransportedGuid() {
		return transportedGuid;
	}



	public void setTransportedGuid(String transportedGuid) {
		this.transportedGuid = transportedGuid;
	}



	public int getTaskDispatched() {
		return taskDispatched;
	}



	public void setTaskDispatched(int taskDispatched) {
		this.taskDispatched = taskDispatched;
	}



	public void setCounter(int value) {
		
		counter = value;
	}



	public int getCounter() {
		return counter;
	}



	public static boolean taskIsModelOptimizer(SomFluidTask sfTask) {

		boolean rB=false;
		
		if ((sfTask!=null) && (sfTask.somHost!=null)){
			String cn = sfTask.somHost.getClass().getSimpleName() ;
			rB = cn.toLowerCase().contains("modeloptimizer");
		}
		
		return rB;
	}


	
	public static boolean taskIsSomApplication(SomFluidTask sfTask) {
		boolean rB=false;
		
		if ((sfTask!=null) && (sfTask.somHost!=null)){
			String cn = sfTask.somHost.getClass().getSimpleName() ;
			rB = cn.toLowerCase().contains("somapplication");
		}
		
		return rB;
	}



	public boolean activateDataStreamReceptor() {
		 
		return activateDataStreamReceptor;
	}
	public boolean isActivateDataStreamReceptor() {
		return activateDataStreamReceptor;
	}
	
	public void setActivateDataStreamReceptor(boolean flag) {
		activateDataStreamReceptor = flag;
	}

	@Override
	public boolean activatedDataStreamReceptor() {
		return activateDataStreamReceptor ;
		
	}

	@Override
	public void activateDataStreamReceptor(boolean flag) {
		activateDataStreamReceptor = flag;
		
	}
	 
}

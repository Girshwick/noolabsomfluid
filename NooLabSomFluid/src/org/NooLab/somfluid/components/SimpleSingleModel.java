package org.NooLab.somfluid.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.somfluid.*;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.engines.det.results.SomTargetResults;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.tasks.SomFluidTask;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;



/**
 * 
 * 
 * 
 * 
 */
public class SimpleSingleModel implements SomHostIntf, Serializable{

	private static final long serialVersionUID = 2574222962733833955L;

	SomFluidTask sfTask ;

	transient SomFluid somFluid;
	transient SomFluidFactory sfFactory ;	
	transient SomDataObject somDataObj ;
	
	transient SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	
	transient SomProcessIntf somProcess;
	
	ModelProperties results;
	
	boolean saveOnCompletion = false;
	
	transient PrintLog out ;
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();

	private int lastState;
	
	// ========================================================================  
	public SimpleSingleModel( SomFluid somfluid, 
							  SomFluidTask sftask,
							  SomFluidFactory factory) {
		 
		somFluid = somfluid;
		sfTask = sftask ;
		sfFactory = factory;
		
		
		
		sfProperties = sfFactory.getSfProperties() ;
		modelingSettings = sfFactory.getSfProperties().getModelingSettings();
		
		sfTask.setCompleted(false);
		
		if (sfTask.getSomHost()==null){
			sfTask.setSomHost(this) ;
		}
		
		out = sfFactory.getOut() ;
	}
	// ========================================================================
	
	public void setInitialVariableSelection(ArrayList<String>  vs){
		
		//ArrayList<Double> initialUsageIndicator = new ArrayList<Double>();
		
		if (somDataObj==null){
			return;
		}
		Variables variables = somDataObj.getVariables() ;
		String label;
		
		
		for (int i=0;i<vs.size();i++){
			label = vs.get(i);
			int ix = variables.getIndexByLabel(label);
			usedVariables.add(ix) ;
		}
		Collections.sort(usedVariables); 
		usedVariables.trimToSize();
		
	}


	public void prepareDataObject(){
		
		try {
		
			if (sfTask.getResumeMode()>=1){
				
			}
			somDataObj = somFluid.loadSource("");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setDataObject( SomDataObject sdo){
		somDataObj = sdo ;
	}
	
	
	public void perform() {
 
		SomTargetedModeling targetedModeling=null;
		long serialID=0;
		serialID = SerialGuid.numericalValue();


		try{
			out.delay(50);

			
			
			sfTask.setCallerStatus(0) ;
			
			targetedModeling = new SomTargetedModeling( this, sfFactory, sfProperties, sfTask, serialID);
			
			somProcess = targetedModeling;
			
			targetedModeling.setSomDataObject(somDataObj);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try {

			targetedModeling.prepare(usedVariables); //

			lastState = 2;
		} catch (Exception e) {
			
			out.printErr(1, "A critical inconsistency has been detected while preparing the som: "+e.getMessage() ) ;
			e.printStackTrace();
			lastState = -7;
			return ;
		}
// VirtualLattice _somLattice = this.somProcess.getSomLattice() ;
// out.printErr(2, "lattice 5 "+_somLattice.toString());

		try {
			String guid = targetedModeling.perform(0);
			
			lastState = 0;
		} catch (Exception e) {
			lastState = -7;
			if (LogControl.Level >=3){
				e.printStackTrace();
			}else{
				out.printErr(1, e.getMessage());
			}
			 
		}
		

// _somLattice = this.somProcess.getSomLattice() ;
// out.printErr(2, "lattice 6"+_somLattice.toString());

		if (sfTask.isNoHostInforming()==false){
			somFluid.onTaskCompleted( sfTask );
		}
			

		
	}
  
	public static SimpleSingleModel load( SomFluidProperties sfProperties ){
		
		SimpleSingleModel simo=null;
		
		String xstr="", filepath , vstr="", filename = "";
		PersistenceSettings ps;
		 
		FileOrganizer fileorg = new FileOrganizer(); 
		 
		fileorg.setPropertiesBase( sfProperties );
		
		ps = fileorg.getPersistenceSettings() ;
		DFutils fileutil = fileorg.getFileutil();
		 
		 
		filename = ps.getProjectName()+"-somobj" + vstr + fileorg.getFileExtension( FileOrganizer._SOMMODELER ) ;
		filepath = fileutil.createpath( fileorg.getSomStoreDir() , filename);
		
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		fileorg.careForArchive( FileOrganizer._SOMMODELER, filepath );
		
		simo= (SimpleSingleModel)storageDevice.loadStoredObject( filepath) ;
		
		return simo;
	}
	
	public int save() {
	
	
		int result=-1;
		String xstr="", filepath , vstr="", filename = "";
		PersistenceSettings ps;
		 
		FileOrganizer fileorg = somDataObj.transformer.getFileorg() ;
		
		ps = fileorg.getPersistenceSettings() ;
		DFutils fileutil = fileorg.getFileutil();
		 
		 
		filename = ps.getProjectName()+"-somobj" + vstr + fileorg.getFileExtension( FileOrganizer._SOMMODELER ) ;
		filepath = fileutil.createpath( fileorg.getSomStoreDir() , filename);
		
		ContainerStorageDevice storageDevice ;
		storageDevice = new ContainerStorageDevice();
		
		fileorg.careForArchive( FileOrganizer._SOMMODELER, filepath );
		
		storageDevice.storeObject( this , filepath) ;
		
		if (fileutil.fileexists(filepath)==false){
			result=-3;
		}else{
			result =0;
		}
		return result;
		
	}
	@Override
	public ModelProperties getSomResults() {
		return results;
	}

	/**
	 * so far a simple callback, not an event (but should..)
	 */
	@Override
	public void onTargetedModelingCompleted(ModelProperties modpropsResults) {
		
			
			results = modpropsResults ;
							out.print(3, "results received by main instance (<SimpleSingleModel>) for Som ("+results.dSomGuid+")\n");	
			double tps = results.getTrainingSample().getTpSingularity();
			// dealing with persistence of model and results
			
			if (saveOnCompletion){
				save();
			}
			results.task.setCompleted(true);
			 
	}


	@Override
	public SomFluid getSomFluid() {
		 
		return somFluid;
	}

	@Override
	public SomFluidTask getSfTask() {
		return sfTask;
	}

	@Override
	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}

	@Override
	public SomDataObject getSomDataObj() {
		return  somDataObj;
	}
   
	@Override
	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	public SomProcessIntf getSomProcess() {
		return somProcess;
	}

	 

	public int getLastState() {
		return lastState;
	}

	@Override
	public String getOutResultsAsXml(boolean asHtmlTable) {
		String xmlstr="";
		
		return xmlstr;
	}

	/**
	 * @return the results
	 */
	public ModelProperties getResults() {
		return results;
	}

	public void setResults(ModelProperties results) {
		this.results = results;
	}

	public boolean isSaveOnCompletion() {
		return saveOnCompletion;
	}

	public void setSaveOnCompletion(boolean flag) {
		saveOnCompletion = flag;
	}

	public void setSfTask(SomFluidTask sfTask) {
		this.sfTask = sfTask;
	}

	public void setSfProperties(SomFluidProperties sfProperties) {
		this.sfProperties = sfProperties;
	}

	public ArrayList<Integer> getUsedVariables() {
		return usedVariables;
	}

	public void setUsedVariables(ArrayList<Integer> usedVariables) {
		this.usedVariables = usedVariables;
	}

	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}

	public void setModelingSettings(ModelingSettings modelingSettings) {
		this.modelingSettings = modelingSettings;
	}

	@Override
	public void selectionEventRouter(SurroundResults results, VirtualLattice somLattice) {
		 
		somLattice.handlingRoutedSelectionEvent(results);
	}
 
	

}

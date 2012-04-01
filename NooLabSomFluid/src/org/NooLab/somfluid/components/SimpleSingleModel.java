package org.NooLab.somfluid.components;

import java.util.ArrayList;
import java.util.Collections;


import org.NooLab.somfluid.*;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.engines.det.results.SomTargetResults;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;




public class SimpleSingleModel implements SomHostIntf{

	SomFluid somFluid;
	SomFluidTask sfTask ;
	SomFluidFactory sfFactory ;
	
	SomDataObject somDataObj ;
	
	SomFluidProperties sfProperties ;
	ModelingSettings modelingSettings;
	
	SomProcessIntf somProcess;
	
	ModelProperties results;
	
	PrintLog out ;
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();
	
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
		
		out = sfFactory.getOut() ;
	}
	// ========================================================================
	
	public void setInitialVariableSelection(ArrayList<String>  vs){
		
		ArrayList<Double> initialUsageIndicator = new ArrayList<Double>();
		
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
		Collections.sort(usedVariables); usedVariables.trimToSize();
	}


	public void prepareDataObject(){
		
		try {
		
			somDataObj = somFluid.loadSource("");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setDataObject( SomDataObject sdo){
		somDataObj = sdo ;
	}
	
	
	public void perform() {
 

		long serialID=0;
		serialID = SerialGuid.numericalValue();


		try{
			

			SomTargetedModeling targetedModeling;
			
			sfTask.setCallerStatus(0) ;
			
			targetedModeling = new SomTargetedModeling( this, sfFactory, sfProperties, sfTask, serialID);
			
			somProcess = targetedModeling;
			
			targetedModeling.setSomDataObject(somDataObj);
			 
			targetedModeling.prepare( usedVariables ); // [10, 11, 12, 13, 15]
			 
			String guid = targetedModeling.perform(0);
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
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
							out.print(2, "results received by main instance for Som ("+results.dSomGuid+")\n");	
			double tps = results.getTrainingSample().getTpSingularity();
			// dealing with persostence of model and results
			
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
 
	

}

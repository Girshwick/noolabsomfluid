package org.NooLab.somfluid.core.engines.det.results;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomResultsPersistence;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.utilities.logging.PrintLog;



public abstract class SomResultHandlerAbs  implements SomResultDigesterIntf{

	SomHostIntf somHost;
	SomResultsPersistence resultsPersistence ;
	SomFluid somFluid;
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	ModelingSettings modelingSettings;
	PersistenceSettings persistenceSettings;
	
	ModelProperties somResults;
	
	PrintLog out;
	
	// ========================================================================
	public SomResultHandlerAbs(SomHostIntf somhost, SomFluidFactory factory){
		
		somHost = somhost;
		somFluid = somhost.getSomFluid() ;
		sfFactory = factory;
		
		sfProperties = somFluid.getSfProperties();
		modelingSettings = sfProperties.getModelingSettings() ;
		
		persistenceSettings = sfProperties.getPersistenceSettings();
		
		out = somFluid.getOut() ;
	}
	// ========================================================================
	
	@Override
	public void getResults() {

		somResults = somHost.getResults() ;
		return  ;
	}

	@Override
	abstract public void handlingResults() ;
	
	
	
	
}

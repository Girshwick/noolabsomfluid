package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomResultsPersistence;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.utilities.logging.PrintLog;



public abstract class SomResultHandlerAbs  implements SomResultDigesterIntf,Serializable{

	private static final long serialVersionUID = 1060737476097032451L;

	transient SomHostIntf somHost;
	SomResultsPersistence resultsPersistence ;
	transient SomFluid somFluid;
	transient SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	ModelingSettings modelingSettings;
	PersistenceSettings persistenceSettings;
	
	ModelProperties somResults;
	
	transient PrintLog out;
	
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

	public SomResultsPersistence getResultsPersistence() {
		return resultsPersistence;
	}

	public void setResultsPersistence(SomResultsPersistence resultsPersistence) {
		this.resultsPersistence = resultsPersistence;
	}

	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	public void setSfProperties(SomFluidProperties sfProperties) {
		this.sfProperties = sfProperties;
	}

	public PersistenceSettings getPersistenceSettings() {
		return persistenceSettings;
	}

	public void setPersistenceSettings(PersistenceSettings persistenceSettings) {
		this.persistenceSettings = persistenceSettings;
	}

	public ModelProperties getSomResults() {
		return somResults;
	}

	public void setSomResults(ModelProperties somResults) {
		this.somResults = somResults;
	}
	
	
	
	
}

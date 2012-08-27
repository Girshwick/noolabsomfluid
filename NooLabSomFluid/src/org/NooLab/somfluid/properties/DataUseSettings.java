package org.NooLab.somfluid.properties;

import java.io.Serializable;

import org.NooLab.somfluid.SomFluidProperties;

import com.jamesmurty.utils.XMLBuilder;


public class DataUseSettings implements Serializable{

	private static final long serialVersionUID = -3235606223479031348L;

	int simumlationMode =  SomFluidProperties._SIM_NONE ;
	int simulationSize = 0;
	
	// ========================================================================
	public DataUseSettings(){
		
	}
	// ========================================================================


	public void setSimulationMode(int simulationmode, double... params) {
		simumlationMode = simulationmode;		
	}


	public void setSimulationSize(int simulationsize) {
		simulationSize = simulationsize ;
	}


	public int getSimumlationMode() {
		return simumlationMode;
	}


	public void setSimumlationMode(int simumlationMode) {
		this.simumlationMode = simumlationMode;
	}


	public int getSimulationSize() {
		return simulationSize;
	}


	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

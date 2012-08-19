package org.NooLab.field.repulsive;

import org.NooLab.field.repulsive.components.RepulsionFieldProperties;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.utilities.logging.LogControl;

import tester.RequestTester;




public class RepulsionFieldFactory {

	RepulsionField repulsionField;
	
	RepulsionFieldProperties rfProperties;
	
	RepulsionFieldFactory rfFactory;
	
	RequestTester rqTester;
	
	boolean isFacadeReadyToUse=false;
	
	// ========================================================================
	public RepulsionFieldFactory(){
		init();
	}
	
	public RepulsionFieldFactory( String command , int LogControlLevel){
		LogControl.Level = LogControlLevel;
		baseinit( command); 
	}
	public RepulsionFieldFactory( String command ){
		baseinit( command);
	}
	
	private void baseinit( String command){
		init();
		
		// test:RQ=100
		if ((command.contains("test")) && (command.contains("RQ"))){
			String dvs = command.split("=")[1];
			int dv = Integer.parseInt(dvs) ;
			
			if ((dv>0)&&(dv<10)){
				dv = 10;
			}
			if (dv>0){
				rqTester = new RequestTester(repulsionField, dv);
			}
		}
		
	}
	private void init(){
		rfProperties = new RepulsionFieldProperties() ;
		rfFactory = this;
		produce(rfProperties);
	}
		
	public RepulsionFieldFactory(RepulsionFieldProperties rfProperties){
		 
		 produce(rfProperties);
	}
	 
	
	private void produce(RepulsionFieldProperties rfProperties){

		repulsionField = new RepulsionField(rfFactory);
		
	}
	// ========================================================================
	
	
	public RepulsionFieldFactory getFactory(){
		
		return this;
	}
	
	public RepulsionField  getRepulsionField(){
		
		return repulsionField ;
	}

	public RepulsionFieldProperties getRfProperties() {
		return rfProperties;
	}

	public RequestTester getRqTester() {
		return rqTester;
	}

	public RepulsionFieldFactory getRfFactory() {
		return rfFactory;
	}

	public void setRepulsionField(RepulsionFieldIntf repulsionField) {
		this.repulsionField = (RepulsionField) repulsionField;
	}

	public boolean isFacadeReadyToUse() {
		return isFacadeReadyToUse;
	}

	public void setFacadeReadyToUse(boolean isFacadeReadyToUse) {
		this.isFacadeReadyToUse = isFacadeReadyToUse;
	}
}

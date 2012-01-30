package org.NooLab.repulsive;

import org.NooLab.repulsive.components.RepulsionFieldProperties;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;

import tester.RequestTester;




public class RepulsionFieldFactory {

	RepulsionField repulsionField;
	
	RepulsionFieldProperties rfProperties;
	
	RepulsionFieldFactory rfFactory;
	
	RequestTester rqTester;
	
	// ========================================================================
	public RepulsionFieldFactory(){
		init();
	}
	
	public RepulsionFieldFactory( String command ){
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
}

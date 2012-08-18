package org.NooLab.field.repulsive.components;

import org.NooLab.field.repulsive.RepulsionFieldCore;




/**
 * 
 * class for handling parameters about selection, such as 
 * - virtual boxes (constraint for selecting)
 * - shapes
 * 
 */
public class SelectionConstraints implements Cloneable{

	RepulsionFieldCore rfCore ;
	
	boolean initialActivation = false;
	boolean isActive=false;
	
	public int currentShapeId = -1; // ParticleGrid._CIRCLE ;
	public double shapeParam1 = -1.0;

	public double shapeParam2 = -1.0;
	
	SBox box = new SBox();
	
	// ========================================================================	
	public SelectionConstraints( RepulsionFieldCore rfc) {

		rfCore = rfc;
		
	}
	// ========================================================================

	public boolean isActive(){
		return isActive;
	}
	public void deactivate() {
		initialActivation = false ;
		isActive = false;
	}

	/** activates a box if present, or sets the next definition immediately to active */
	public void activateBox() {
		
		initialActivation = true;
		
		if (currentShapeId>0){
			isActive = true;
		}
	}

	public void defineBox(double x1, double x2, double y1, double y2) {
		 
		box.x1 = x1;
		box.x2 = x2;
		box.y1 = y1;
		box.y2 = y2;
	}

	public double[] getBoxParams(){
		double [] xp = new double[4];
		
		xp[0] = box.x1;
		xp[1] = box.x2;
		xp[2] = box.y1;
		xp[3] = box.y2;
		
		return xp;
	}
	
	public SBox getBox(){
		return box;
	}
	
	public void setSelectionShape(int shapeId, double param1, double param2) {
		
		if ((shapeId< ParticleGrid._STRING) || (shapeId>ParticleGrid._ELLIPSE )){
			shapeId = ParticleGrid._CIRCLE;
		}
		currentShapeId = shapeId;
		
		shapeParam1 = param1;
		shapeParam2 = param2;
		
		if (initialActivation){
			isActive = true;
		}
	}

	public boolean setSelectionShape(int shapeId) {
		boolean hb=false ;
		
		int lastshape = currentShapeId ; 
		currentShapeId = shapeId ;
		
		if ((shapeId<ParticleGrid._STRING) || (shapeId>ParticleGrid._ELLIPSE)){
			return false;
		}
		// check for param defaults...
		if (shapeId==ParticleGrid._ELLIPSE){
			if ((shapeParam1>0) && (shapeParam2>0)){
				hb=true;
			}
		}
		if (shapeId==ParticleGrid._RECT){
			if ((shapeParam1>0) && (shapeParam2>0)){
				hb=true;
			}
			
		}
		if (shapeId==ParticleGrid._STRING){
			if ( shapeParam1>0 ){
				hb=true;
			}
			
		}
		
		if (shapeId==ParticleGrid._CIRCLE){ // uses max count
			hb =true;
		}
		
		if (hb){
			isActive = true;
		}
		
		return true;
	}
	
	
	class SBox{
		public double x1, x2, y1, y2 ;
		
	}


	public RepulsionFieldCore getRfCore() {
		return rfCore;
	}

	public void setRfCore(RepulsionFieldCore rfCore) {
		this.rfCore = rfCore;
	}

	public boolean isInitialActivation() {
		return initialActivation;
	}

	public void setInitialActivation(boolean initialActivation) {
		this.initialActivation = initialActivation;
	}

	public int getCurrentShapeId() {
		return currentShapeId;
	}

	public void setCurrentShapeId(int currentShapeId) {
		this.currentShapeId = currentShapeId;
	}

	public double getShapeParam1() {
		return shapeParam1;
	}

	public void setShapeParam1(double shapeParam1) {
		this.shapeParam1 = shapeParam1;
	}

	public double getShapeParam2() {
		return shapeParam2;
	}

	public void setShapeParam2(double shapeParam2) {
		this.shapeParam2 = shapeParam2;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setBox(SBox box) {
		this.box = box;
	}
	

}

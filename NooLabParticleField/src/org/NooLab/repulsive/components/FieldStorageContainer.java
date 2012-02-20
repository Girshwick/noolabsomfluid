package org.NooLab.repulsive.components;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;




public class FieldStorageContainer implements Serializable{

	private static final long serialVersionUID = -213752946252268334L;

	

	ArrayList<Particle> particleStore = new ArrayList<Particle> (); 
	ArrayList<SurroundBuffer> surroundBufferItems = new ArrayList<SurroundBuffer>() ; // 
	FieldAreaProperties areaProperties = new FieldAreaProperties();
	
	transient Storage parentStore ;
	transient RepulsionFieldCore rfield;
	
	
	public FieldStorageContainer( Storage parent){
		
		parentStore = parent;
		rfield = parentStore.rfield;
	}
	
	public void setFieldReference( RepulsionFieldCore rf ){
		rfield = rf ;
	}
	
	public void setAreaProperties(){
		
	}
		
	public void acquireParticles( Particles particles){
		Particle particle;
		try{
			
			for (int i=0;i<particles.size();i++){
				
				particle = particles.get(i) ;
				
				particleStore.add(particle) ;
				
				// surroundBufferItems.add( particle.getSurroundBuffer() ) ;
				
			} // i-> all particles
			
			areaProperties.areaHeight = rfield.getAreaHeight();
			areaProperties.areaWidth = rfield.getAreaWidth();
			
			areaProperties.deceleration = rfield.getDeceleration();
			
			areaProperties.energy = rfield.getEnergy();
			areaProperties.repulsion = rfield.getRepulsion();
			areaProperties.kRadiusFactor = rfield.getkRadiusFactor();
			areaProperties.name = rfield.getName();
			areaProperties.numberOfParticles = rfield.getNumberOfParticles();
			areaProperties.threadcount = rfield.getThreadcount();
			areaProperties.delayedOnsetMillis = rfield.getDelayedOnsetMillis();
			  
			 
			areaProperties.multiProc = rfield.isMultiProc() ;
			areaProperties.sizefactor = rfield.getSizefactor() ;
			areaProperties.freezingAllowed = rfield.isFreezingAllowed() ;
			areaProperties.stepsLimit = rfield.getStepsLimit() ;
			areaProperties.selectionSize = rfield.getSelectionSize() ;
			areaProperties.useOfSamplesForStatistics = rfield.isUseOfSamplesForStatistics() ;
			areaProperties.colormode = rfield.getColormode() ;
			areaProperties.averageDistance = rfield.getAverageDistance() ; 
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	

	public ArrayList<Particle> getParticleStore() {
		return particleStore;
	}

	public void setParticleStore(ArrayList<Particle> particleStore) {
		this.particleStore = particleStore;
	}

	public ArrayList<SurroundBuffer> getIndexedDistances() {
		return surroundBufferItems;
	}

	public void setIndexedDistances(ArrayList<SurroundBuffer> sbi) {
		this.surroundBufferItems = sbi;
	}

	public FieldAreaProperties getAreaProperties() {
		return areaProperties;
	}

	public void setAreaProperties(FieldAreaProperties areaProperties) {
		this.areaProperties = areaProperties;
	}
}

class FieldAreaProperties implements Serializable{
	
	private static final long serialVersionUID = 5576012965405759095L;
	
	String name ;
	
	int areaWidth;
	int areaHeight ;
	int numberOfParticles ;
	
	double deceleration ;
	double energy ;
	double repulsion ;
	double kRadiusFactor ;
	
	int threadcount ;
	int delayedOnsetMillis ;	 
	
	boolean multiProc;
	double sizefactor;
	boolean freezingAllowed;
	int stepsLimit;
	int selectionSize;
	boolean useOfSamplesForStatistics;
	int colormode;

	double averageDistance;

	// ========================================================================
	public FieldAreaProperties(){
		
	}
	// ========================================================================
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getAreaWidth() {
		return areaWidth;
	}

	public void setAreaWidth(int areaWidth) {
		this.areaWidth = areaWidth;
	}

	public int getAreaHeight() {
		return areaHeight;
	}

	public void setAreaHeight(int areaHeight) {
		this.areaHeight = areaHeight;
	}

	public double getDeceleration() {
		return deceleration;
	}

	public void setDeceleration(double deceleration) {
		this.deceleration = deceleration;
	}

	public int getDelayedOnsetMillis() {
		return delayedOnsetMillis;
	}

	public void setDelayedOnsetMillis(int delayedOnsetMillis) {
		this.delayedOnsetMillis = delayedOnsetMillis;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getRepulsion() {
		return repulsion;
	}

	public void setRepulsion(double repulsion) {
		this.repulsion = repulsion;
	}

	public double getkRadiusFactor() {
		return kRadiusFactor;
	}

	public void setkRadiusFactor(double kRadiusFactor) {
		this.kRadiusFactor = kRadiusFactor;
	}

	public int getNumberOfParticles() {
		return numberOfParticles;
	}

	public void setNumberOfParticles(int numberOfParticles) {
		this.numberOfParticles = numberOfParticles;
	}

	public double getAverageDistance() {
		return averageDistance;
	}

	public void setAverageDistance(double averageDistance) {
		this.averageDistance = averageDistance;
	}

	public int getThreadcount() {
		return threadcount;
	}

	public void setThreadcount(int threadcount) {
		this.threadcount = threadcount;
	}

	public boolean isMultiProc() {
		return multiProc;
	}

	public void setMultiProc(boolean multiProc) {
		this.multiProc = multiProc;
	}

	public double getSizefactor() {
		return sizefactor;
	}

	public void setSizefactor(double sizefactor) {
		this.sizefactor = sizefactor;
	}

	public boolean isFreezingAllowed() {
		return freezingAllowed;
	}

	public void setFreezingAllowed(boolean freezingAllowed) {
		this.freezingAllowed = freezingAllowed;
	}

	public int getStepsLimit() {
		return stepsLimit;
	}

	public void setStepsLimit(int stepsLimit) {
		this.stepsLimit = stepsLimit;
	}

	public int getSelectionSize() {
		return selectionSize;
	}

	public void setSelectionSize(int selectionSize) {
		this.selectionSize = selectionSize;
	}

	public boolean isUseOfSamplesForStatistics() {
		return useOfSamplesForStatistics;
	}

	public void setUseOfSamplesForStatistics(boolean useOfSamplesForStatistics) {
		this.useOfSamplesForStatistics = useOfSamplesForStatistics;
	}

	public int getColormode() {
		return colormode;
	}

	public void setColormode(int colormode) {
		this.colormode = colormode;
	}
	
}
	
 















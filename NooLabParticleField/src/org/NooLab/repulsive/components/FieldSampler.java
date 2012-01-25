package org.NooLab.repulsive.components;

import org.NooLab.repulsive.RepulsionField;

import org.NooLab.repulsive.components.topo.Surround;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.repulsive.particles.ParticlesIntf;
import org.NooLab.utilities.logging.PrintLog;
 


/**
 * 
 * this class can create samples from the volatile field of particles.
 * 
 * Sampling is done using 2 mechanisms:
 * 1. by an instance of the RepulsionField itself, with only 5 particles,
 *    which gets instantiated in the beginning, and the location of the "bubbles" gets recorded
 * 2. by adaptive sampling within the five bubbles
 *    such that we always calculate around 300 bubbles
 * 
 * the sample has to be recalculated only if the number of particles changed by more than 20..30%
 * 
 *
 */
public class FieldSampler{
	
	RepulsionField parentField;
	
	SamplingField samplingField;
	
	boolean samplesAvailable = false;
	
	int sampleSize = 300;
	int sampleCount = 5;
	
	int indexOfFocusedParticle = -1;
	
	double aspectratio, densityPerAcre;
	int[] sample  = new int[0];
	
	Particles sampledParticles , fieldParticles;
	Particles samplePatchCenterParticles ;
	
	PrintLog out;
	 
	
	public FieldSampler(RepulsionField parent){
		
		parentField = parent;
		
		// as long as our sample is not available, we will return the original set
		fieldParticles = parentField.particles;
		
		out = parentField.out;
		
		samplingField = parentField.getSamplingField();
	}
	
	
	public Particles getParticlesList(){
		
		Particles nth_sample = fieldParticles;
		
		// determinePatches();
		
		return nth_sample;
	}
	
	public int[] determineSamplePatchesItems( int totalExpectedCount){
		
		int index =1, surroundN=20;
		int actualCount=0;
		Surround surround ;
		
		int[] patchSelection=null,particleIndexes =null;
		
		
		surround = new Surround(parentField);
		
		for (int i=0;i<5;i++){
		
			
		}
		
		patchSelection = surround.getGeometricSurround(index ,surroundN, Surround._CIRCLE );
		
		combine(); // via arrays copy or utilites
		
		
		return particleIndexes;
	}
	
	private void combine(){
		
	}

	public void calculateDensity(){
		densityPerAcre = parentField.getDensityPerAcre() ;

		out.print(3, "N=" + fieldParticles.size() + " ,  densityPerAcre = "+ densityPerAcre + "");
		
		if (densityPerAcre > 50) {
			// recalculate new size of area, keeping the proportions
			aspectratio = parentField.getAreaWidth()/ parentField.getAreaHeight();

		}
	}

	/** importing the particles that represent the sample patches */ 
	public void setSampleRoots( ParticlesIntf particles ) {
		int n,w,h;
		double r,x,y ;
		Particle particle;
		
		if (samplePatchCenterParticles!=null){
			if (samplePatchCenterParticles.size()>0){
				samplePatchCenterParticles.clear();
			}
			samplePatchCenterParticles = null;
		}
		
		samplePatchCenterParticles = new Particles(parentField, particles.size());  
		
		n = particles.size();
		w = parentField.getSamplingField().getWidth() ;
		h = parentField.getSamplingField().getHeight() ;
		
		for (int i=0;i<n;i++){
			x = Math.round(particles.get(i).x);
			y = Math.round(particles.get(i).y);
			r = Math.round(particles.get(i).radius);
			
			particle = new Particle( w,h, parentField.getkRadiusFactor(), n,parentField.getRepulsion(),1.0,1);
			samplePatchCenterParticles.add(particle) ;
			
		} // i->
		
		// we need it only in the beginning (or later, if the size of the whole area changes, but then we will re-instantiate it) 
		if (samplingField!=null){
			if (samplingField.srepulsionField != null){
				samplingField.srepulsionField.stopFieldThread() ;
				out.delay(100);
				samplingField =null ;
			}
		} // samplingField != null ?
		samplesAvailable = true;
	}


	public Particles getSamplePatchCenterParticles() {
		return samplePatchCenterParticles;
	}

	public void setSamplesAvailable(boolean flag) {
		samplesAvailable = flag;
	}
	
	public boolean getSamplesAvailable() {
		 
		return samplesAvailable ;
	}

	/**
	 * performs a simple check on coordinates and radius
	 * 
	 */
	public boolean particleIsInSample(Particle testParticle) {
		
		boolean rB=false;
		Particle sparticle;
		double radius, dx ,xd,yd;
	 
		
		for (int i=0;i<samplePatchCenterParticles.size();i++){
			sparticle = samplePatchCenterParticles.get(i) ;
			
			 
			radius = sparticle.radius ;
			
			xd = testParticle.x - sparticle.x;
			yd = testParticle.y - sparticle.y ;
			dx = Math.sqrt( (xd*xd ) + (yd*yd) );
			
			if (dx<radius){
				rB=true;
				break;
			}
			
		} // i->
		
		return rB;
	}
	
	
	
} // inner class FieldSample




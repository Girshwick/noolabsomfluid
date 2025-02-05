package org.NooLab.field.repulsive.particles;

import java.util.ArrayList;

import org.NooLab.field.FieldHostIntf;
import org.NooLab.field.FieldParticleIntf;
import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.components.SurroundBuffers;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.field.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;

 

public class RepulsionFieldParticles implements RepFieldParticlesIntf , GraphParticlesIntf{ 
	// not correct yet, we have to maintain 
	
	String parentName = "" ; // name of the field which is hosting this collection of particles
	
	ArrayList<RepulsionFieldParticle> items = new ArrayList<RepulsionFieldParticle>();
	
	ParticlePropertiesIntf particleProperties;
	
	RepulsionFieldBasicIntf field;
	FieldHostIntf parentField;
	
	int nbrParticles;
	double averageDistance=0.0, densityPerAcre=0.0 ;

	private double defaultRadius;
	
	
	public RepulsionFieldParticles( RepulsionFieldBasicIntf field , FieldHostIntf parent){ // , int nbrParticles
		this.field = field;
		// this.nbrParticles = nbrParticles;
		parentField = parent;
	}
	
	/**
	 * this basically creates a clone of an population of particles, including cloning all of its items.
	 * @param surroundBuffers 
	 * @param particles
	 */
	public RepulsionFieldParticles(RepulsionFieldParticles templateParticles,FieldHostIntf parent) { // , SurroundBuffers sbs
		
		RepulsionFieldParticle p;
		
		parentField = parent;
		particleProperties = templateParticles.particleProperties ;
		int k;
		
		for (int i=0;i<templateParticles.size();i++){
			
			if (i==641){
				k=0;
			}
			p = new RepulsionFieldParticle( templateParticles.size(),i, templateParticles.get(i),parentField );// , sbs
			
			// p.isAlive = templateParticles.get(i).isAlive; // will be set to -1 if it is scheduled to be deleted
			items.add(p);
		}
	}
	
	

	/**
	 * 
	 * usually, srcParticles should come from the core-layer
	 * 
	 * @param srcParticles
	 * @param sbs
	 * @param beyondIndex
	 */
	public void updateByParticles(RepulsionFieldParticles srcParticles,  int beyondIndex) { // SurroundBuffers sbs,
		RepulsionFieldParticle srcP, p;
	 
		
		int fn,pn ,startix;
		
		pn = srcParticles.size();
		fn = items.size();
		
		if (fn<pn){
			
		}
		
		if ((beyondIndex<=0) || (beyondIndex>srcParticles.size())){
			startix=0;
		}else{
			startix = beyondIndex;
		}
		
		for (int i=startix;i<srcParticles.size();i++){
			
			srcP = srcParticles.get(i) ;
			
			if (i>items.size()-1){ 
				// mirroring the add operation in the core 
				p = new RepulsionFieldParticle( items.size(), i ,srcP,parentField); // , sbs
				p.setDataAsCloneOf( srcP );
				items.add(p) ;
				
				if (beyondIndex>0){
					// sbs.getNeighborhood().update(i, p.x, p.y, p.radius);
					// sbs.updateSurroundExtension( i,p );
				}
			}else{
			
				p = items.get(i) ;
				p.setDataAsCloneOf( srcP );
			}
			 
		} // i->
		if (items.size()>srcParticles.size()){
			
		}
	}

	public void setParentName(String name) {
		parentName = name;
	}

	public void clear(){
		items.clear();
		particleProperties = null;
		field = null;
		nbrParticles = -1;
	}
	
	public int size(){
		nbrParticles = items.size();
		return items.size();
	}

	public void add(RepulsionFieldParticle p){
		items.add(p);
		nbrParticles = items.size() ;
	}

	@Override
	public void add(FieldParticleIntf p) {
		 
		items.add((RepulsionFieldParticle) p);
		nbrParticles = items.size() ;
	}

	public RepulsionFieldParticle get(int index){
		if ((items!=null) && (index>=0) && (index<items.size())){
			nbrParticles = items.size();
			
			return items.get(index);
		}else{
			return null;
		}
			
	}
	
	public void remove(int index){
		RepulsionFieldParticle p;
		
		p = items.get(index) ;
		// p.surroundBuffer.clear(index); // informs its neighbors
		
		// p.surroundBuffer = null;
		
		items.remove(index);
		nbrParticles = items.size() ;
	}
	

	public RepulsionFieldParticle getByProperty(int index){
		return items.get(index);
	}

	public ArrayList<RepulsionFieldParticle> getItems() {
		return items;
	}

	public void setItems(ArrayList<RepulsionFieldParticle> items) {
		this.items = items;
	}

	public int getNbrParticles() {
		return nbrParticles;
	}

	public void setNbrParticles(int nbrParticles) {
		this.nbrParticles = nbrParticles;
	}

	public ParticlePropertiesIntf getParticleProperties() {
		return particleProperties;
	}	
	
	public void selectSurround( int[] particleIndexes, boolean autoselect){
		int ix;

		nbrParticles = items.size();
		
		if (particleIndexes==null){
			return;
		}
		if (autoselect){ // do this by multi-digester
			for (int i=0;i<size();i++){
				get(i).setSelected(0) ;
				get(i).resetColor();
			}
			for (int i=0;i<particleIndexes.length;i++){
				ix  = particleIndexes[i] ;
				
				if ((ix>=0) && (ix< size())){ 
					get(ix).setSelected(1) ;
					// default coloring
					get(ix).setColor( RepulsionField._OUT_SELECTCOLOR[0],RepulsionField._OUT_SELECTCOLOR[1],RepulsionField._OUT_SELECTCOLOR[2]) ;
					// the color should be set via callback, which is homed in the Facade ("RepulsionField")
					// or 
					
				}
			} // i->
		} // autoselect ?
		
		ix=0; 
	}

	public void setField(RepulsionFieldIntf _field) {
		
		field = _field;
	}

	public void setAverageDistance(double avgDistance) {
	 
		averageDistance = avgDistance;
	}

	
	
	public void setAverageDensity(double density) {
		 
		densityPerAcre = density;
	}

	public double getDensity() {
		return densityPerAcre;
	}

	public void setDensity(double density) {
		this.densityPerAcre = density;
	}

	public double getAverageDistance() {
		return averageDistance;
	}

	public void setDefaultRadius(double radval) {
		 
		defaultRadius = radval ;
	}

	public double getDefaultRadius() {
		return defaultRadius;
	}

	public RepulsionFieldBasicIntf getField() {
		return field;
	}

	public void setField(RepulsionFieldBasicIntf field) {
		this.field = field;
	}

	public double getDensityPerAcre() {
		return densityPerAcre;
	}

	public void setDensityPerAcre(double densityPerAcre) {
		this.densityPerAcre = densityPerAcre;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParticleProperties(ParticlePropertiesIntf particleProperties) {
		this.particleProperties = particleProperties;
	}
}

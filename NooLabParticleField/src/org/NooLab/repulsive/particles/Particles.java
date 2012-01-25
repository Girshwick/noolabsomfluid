package org.NooLab.repulsive.particles;

import java.util.ArrayList;

import org.NooLab.repulsive.RepulsionFieldIntf;

 

public class Particles implements ParticlesIntf {
	
	
	ArrayList<Particle> items = new ArrayList<Particle>();
	
	ParticlePropertiesIntf particleProperties;
	
	RepulsionFieldIntf field;
	
	int nbrParticles;
	
	
	
	public Particles( RepulsionFieldIntf field, int nbrParticles ){
		this.field = field;
		this.nbrParticles = nbrParticles;
		
		
	}
	
	public int size(){
		return items.size();
	}
	
	public void clear(){
		items.clear();
		particleProperties = null;
		field = null;
		nbrParticles = -1;
	}
	
	public void add(Particle p){
		items.add(p);
	}

	public Particle get(int index){
		if ((items!=null) && (index>=0) && (index<items.size())){
			return items.get(index);
		}else{
			return null;
		}
			
	}
	
	public void remove(int index){
		Particle p;
		
		p = items.get(index) ;
		p.surroundBuffer.clear(index); // informs its neighbors
		
		p.surroundBuffer = null;
		items.remove(index);
	}
	

	public Particle getByProperty(int index){
		return items.get(index);
	}

	public ArrayList<Particle> getItems() {
		return items;
	}

	public void setItems(ArrayList<Particle> items) {
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
}

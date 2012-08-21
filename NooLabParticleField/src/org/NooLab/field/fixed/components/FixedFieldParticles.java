package org.NooLab.field.fixed.components;

import java.util.ArrayList;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.FieldParticleIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;


public class FixedFieldParticles implements FixedFieldParticlesIntf{

	
	
	ArrayList<FixedFieldParticle> items = new ArrayList<FixedFieldParticle>();
	
	
	
	
	@Override
	public int size() {
		
		return items.size();
	}

	
	//public void add(FixedFieldParticle p) {
	public void add(FieldParticleIntf p) {
		
		FixedFieldParticle particle = (FixedFieldParticle)p;
		items.add(particle) ;
	}

	@Override
	public void remove(int index) {
		 
		items.remove(index) ;
	}

	public ArrayList<FixedFieldParticle> getItems() {
		return items;
	}
	@Override
	public FixedFieldParticle get(int index) {
		
		return items.get(index);
	}

	@Override
	public double getDensity() {
		
		return 0;
	}

	@Override
	public double getAverageDistance( FieldIntf field) {
		double rad=-1;
		
		if (items.size()<=0){
			rad = -1;
		}else{
			
			rad = ((double)field.getAreaHeight() * (double)field.getAreaWidth())/((double)items.size());
			
		}
		
		return rad;
	}



}

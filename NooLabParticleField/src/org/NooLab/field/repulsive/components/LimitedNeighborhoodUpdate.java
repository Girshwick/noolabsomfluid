package org.NooLab.field.repulsive.components;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.*; 

import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.data.AreaPoint;
import org.NooLab.field.repulsive.components.data.FieldPoint;
import org.NooLab.field.repulsive.components.topology.Surround;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticles;
 

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.ArrUtilities;


/**
 * 
 * this class is for updates that work only on the neighborhood of a change;
 * 
 * the need for intersection bookkeeping requires a dedicated class
 * 
 * 
 * StringUtils.join([Collection<>].iterator(), ",")
 */
public class LimitedNeighborhoodUpdate {

	ArrayList<AreaPoint> centerpointsOfChangedRegion = new ArrayList<AreaPoint>(); 
	
	RepulsionFieldCore rf;

	Neighborhood neighborhood;
	SurroundBuffers surroundBuffers;
	
	double surroundFactor = 3.4 ;
	
	
	ArrUtilities arrutil = new ArrUtilities ();
	PrintLog out;

	public LimitedNeighborhoodUpdate( RepulsionFieldCore parent, PrintLog outprn) {
		
		rf = parent ;
		neighborhood = rf.getNeighborhood() ;
		
		out = outprn;
	}
	
	
	public void addRegion( double x, double y){
		AreaPoint apoint = new AreaPoint();
		apoint.x = x ;
		apoint.y = y ;
		centerpointsOfChangedRegion.add(apoint);
	}
	
	/**   */
	public int checkParticle(RepulsionFieldParticle particle) {
		
		boolean rB = true;
		int pointIndex = -1;
		
		double x,y,x1,y1,x2,y2, avgDistance = rf.getAverageDistance() ;

		for (int i=0;i<centerpointsOfChangedRegion.size();i++){
			// care for TORUS BORDER !!!
			
			x = centerpointsOfChangedRegion.get(i).x ;
			y = centerpointsOfChangedRegion.get(i).y ;
			
			x1 = x - avgDistance * surroundFactor; 
			y1 = y - avgDistance * surroundFactor;
			x2 = x + avgDistance * surroundFactor;
			y2 = y + avgDistance * surroundFactor;
			
			rB = (particle.x >= x1) && (particle.x <= x2) && 
				 (particle.y >= y1) && (particle.y <= y2) ; 
			if (rB){
				pointIndex =i;
				break;
			}
		}// i-> all center points
		
		return pointIndex ;
	}

	public void clear(){
		centerpointsOfChangedRegion.clear() ;
	}

	


	public double getSurroundFactor() {
		return surroundFactor;
	}

	public void setSurroundFactor(double surroundFactor) {
		this.surroundFactor = surroundFactor;
	}

	public void remove(int pointIndex) {
		
		if ((pointIndex>=0) && (pointIndex<centerpointsOfChangedRegion.size())){
			centerpointsOfChangedRegion.remove(pointIndex);
		}
		
	}


	
	 
	 
	 

	
}

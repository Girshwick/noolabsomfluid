package org.NooLab.repulsive.components.topology;

import java.util.ArrayList;

import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.SurroundBuffers;

import org.NooLab.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;



/**
 * 
 * this class is a facade to the neighborhood class;
 * Surround provides methods to check the surrounding of a particle, or of a coordinate;
 * 
 *   it may return single index values that point to the collection of particles,
 *   or an array of such indices, which would be sorted according to the distance
 *   to the requested entity
 * 
 */
public class Surround {

	public final static int _STRING = 1; // like in a violine, or in a model of atomic bonds ("bar-like electron clouds")
	public final static int _RECT   = 3;
	public final static int _CIRCLE = 4;
	
	
	RepulsionFieldBasicIntf parentField;
	
	Neighborhood neighborhood;
	SurroundBuffers surroundBuffers;
	Particles particles;
	
	String requestGuid="";
	
	int[] particleIndexes ;      
	double[] particleDistances ;  // note that the first element is 0 if we called by index
	
	ArrayList<IndexDistance> indexedDistance;
	
	PrintLog out;
	
	
	// ========================================================================
	public Surround(RepulsionFieldCore parent){
		 
		 
		neighborhood = parent.getNeighborhood() ;
		
		parentField = (RepulsionFieldIntf)parent ;
		
		surroundBuffers = parent.getSurroundBuffers();
		
		particles = parent.particles;
				
		out = parent.out;
		
		// out.print(2, "border mode in new surround : "+ neighborhood.getBorderMode() );
	}
	
	
	public Surround(RepulsionFieldBasicIntf rfParent, SurroundBuffers sbs){
		
		parentField = rfParent ;
		
		surroundBuffers = sbs ;
		 
		neighborhood = sbs.getNeighborhood() ;
 		
		particles = (Particles) parentField.getParticles() ;
		 
		out = new PrintLog(2,false);
	}
	// ========================================================================
	
	
	public int[] getGeometricSurround(int xpos, int ypos, int surroundN, int shape) {
		int index=-1;
		double x,y,dx=-1.0;
		long stime, ctime;
		
		
		index = getParticleAt( xpos, ypos, -1);
		
		stime= System.currentTimeMillis();
		
											out.print(3, "now retrieving surround for particle of index = "+index+"...") ;
		
		getGeometricSurround(index, surroundN, shape);
		
		// adjust value in item 0 of particleDistances
		 
		if ((particleIndexes!=null) &&(particleIndexes.length > 0)) {
			index = particleIndexes[0];
			x = particles.get(index).x;
			y = particles.get(index).y;

			dx = Math.sqrt(((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos)));
			particleDistances[0] = dx;
			
			out.print(4, "retrieving surround finished.") ; 
			ctime= System.currentTimeMillis();
			out.print(3, "a group of "+particleIndexes.length+" particles has been retrieved in "+(ctime-stime)+" ms");

		} else{
			out.print(2, "Unexpectedly, nothing found.");
			if (particleIndexes==null){
				particleIndexes = new int[0];
				particleDistances = new double[0] ;
			}
		}
		return particleIndexes;
	}
	
	
	synchronized public String getProcessGuid(){
		String guidStr = GUID.randomvalue() ;
		return guidStr;
	}
	
	// we return the list of indexes directly, the distances may be retrieved 
	// from the object ba a dedicated get()  
	// 
	public int[] getGeometricSurround(int index, int surroundN, int shape) {
		
		int n,ki,z,k;
		double dvMax = -1,dv; 
		int dvMaxix = -1;
		String guidStr = getProcessGuid() ;
		
		// this we may call in a dedicated process (with max parallel instances)
		
		if (requestGuid.length()>0){
			guidStr = requestGuid;
		}
		
		// out.print(2, "Surround(): border mode in getGeometricSurround : "+ neighborhood.getBorderMode() );
		
		try{
			
			

			// TODO: to make this thread safe, it must be called as an inner class !!!
			indexedDistance = neighborhood.getItemsOfSurround( index, surroundN, particles.size(), guidStr) ;
			
			
			boolean sav = ((surroundBuffers!=null) && (surroundBuffers.bufferIsAvailable( index, surroundN)));
			boolean ixDistAssembled = (sav==true) ;
			 
			//if (ixDistAssembled==false)
			{      
				
				if (indexedDistance != null) {
					

					out.print(4, "transposing results from <indexedDistance> to <particleIndexes> ...  ");
					dvMaxix=-1;dvMax=-1;
					// if we have added a particle, we may find 1 or more additional items, which we have to filter out
					// TODO: this just find one overcount... however, there could me many
					ki = indexedDistance.size();
					if (ki>parentField.getSelectionSize()   ){
						k=ki;
						// we find the index(es) with maximum
						for (int i = 0; i < k; i++) {
							dv = indexedDistance.get(i).getDistance();
							if (dvMax<dv){
								dvMax=dv;
								dvMaxix =i;
							}
						}
						
					}
					// we return to the correct size of particles in the surround
					k=parentField.getSelectionSize(); n=k;
					if (k>indexedDistance.size()){
						n=indexedDistance.size();
					}
					particleIndexes = new int[n];
					particleDistances = new double[n];
					for (int i = 0; i < n; i++) { particleIndexes[i] = -1;}
					
					n=0; z=0; 
					for (int i = 0; i < ki; i++) {
						
						if ((i!=dvMaxix) && (i<indexedDistance.size())){
							index = indexedDistance.get(i).getIndex();
							particleIndexes[z] = index;
							particleDistances[z] = indexedDistance.get(i).getDistance();
							z++;
						}
						// 
					}

					n = particleIndexes.length;
					
					
					out.print(4, "transposing results completed. ");
				}
			}
			
			
			
		}catch(Exception e){
			String str ="";
			
			e.printStackTrace();
			particleIndexes = new int[0] ;
		}
		
		return particleIndexes;
	}
	
	
	/** with radius parameter, this returns the closest item to the provided x,y ,
	 * which also lies within the radius */
	public int getParticleAt( int xpos, int ypos, double radius ){
		 
		int particleIndex=-1;
		String guid = getProcessGuid() ;
		long stime, ctime;
		 
		try{
			stime= System.currentTimeMillis();
			
			//boolean sav = ((surroundBuffers!=null) && (surroundBuffers.bufferIsAvailable( index, surroundN)));
			
			
			particleIndex = neighborhood.getItemsCloseTo( xpos, ypos);
			// TODO check for the radius	  
			
			if ((particles==null) || (particleIndex<0) || (particleIndex>=particles.size()) || (particles.get(particleIndex).getIsAlive()<0)){
				String str="";
				if (particles!=null){ str = " ,  particles n="+ particles.size();}
				out.print(2, "problem in <getParticleAt()>, particleIndex="+particleIndex+", particles oK? ->"+(particles!=null)+" "+str);
				particleIndex = -2;
			}
			//
			ctime= System.currentTimeMillis();
			out.print(4, "particle (index="+particleIndex+",x="+xpos+",y="+ypos+") has been retrieved in "+(ctime-stime)+" ms");
			
		} catch(Exception e){
			String str = "Critical error in context <" + parentField.getName()+">";
			out.printErr(2, str);
			e.printStackTrace();
		}
		 
		return particleIndex;
		
	}
	
	/** without radius parameter, this returns just the closest item to the provided x,y */
	public int getParticleAt( int xpos, int ypos ){
		
		return getParticleAt( xpos, ypos ,-1);
			
    }

	public int[] getParticleIndexes() {
		return particleIndexes;
	}

	public ArrayList<IndexDistance> getIndexedDistance() {
		return indexedDistance;
	}

	public double[] getParticleDistances() {
		return particleDistances;
	}



	public void setRequestGuid(String requestguid) {
		requestGuid = requestguid;
	}
	
	
}













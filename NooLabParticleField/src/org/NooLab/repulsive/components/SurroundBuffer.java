package org.NooLab.repulsive.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.NooLab.utilities.datatypes.IndexDistance;

 

/**
 * 
 * 
 *
 *  we do NOT save IndexDistance as a bi-array, instead we maintain different, but
 *  parallel lists for index and distance
 *  
 *
 */
public class SurroundBuffer implements Serializable{

	private static final long serialVersionUID = 1985617276902884583L;
	
	int index;
	int size;
	
	// we use ArrayList instead of int[] since it is much more flexible
	int[] indexes = new int[0];        //= new ArrayList<Integer>();
	double[] distances = new double[0] ; // = new ArrayList<Double>();
	
	transient SurroundBuffers parent ;
	
	// ------------------------------------------------------------------------
	public SurroundBuffer(int index, SurroundBuffers parent){
		this.index = index ;
		this.parent = parent ;
	}
	// ------------------------------------------------------------------------
	
	public void clear( ) {
		indexes = new int[0];
		distances = new double[0];
		
	}
	/**
	 * e.g. called on removing a particle
	 */
	public void clear( int index) {
		// TODO 
		// send a message to the affected items , that they need to update
		
		indexes = new int[0];
		distances = new double[0];
		
		if (parent==null){
			return;
		}
		if (index<parent.bufferItems.size()){
			// parent.bufferItems.remove(index) ; // ????
			parent.informAboutNecessaryUpdate(indexes); // ArrayList<Integer> 
		}
	}

	public SurroundBuffers getBuffersContext(){
		return parent;
	}
	
	public void importSurrounding(int[] particleIndexes) {
		 
		indexes   = Arrays.copyOf( particleIndexes, particleIndexes.length);
		// new ArrayList (Arrays.asList(particleIndexes ) ) ;
		this.distances = null;
	}

	 
	public void importSurrounding(int[] particleIndexes, double[] distances) {
	  
		indexes   = Arrays.copyOf( particleIndexes, particleIndexes.length);
		this.distances   = Arrays.copyOf( distances, distances.length);
		
	}
	
	/**
	 * this provides a transation between two completely different data structures:
	 * indexedDistances :(int, double) -> int[] double[]
	 * 
	 * @param indexedDistances
	 */
	public void importSurrounding(ArrayList<IndexDistance> indexedDistances){
		double d;
		int ix;
		 
		 
		IndexDistance iDist;
		int n = indexedDistances.size();
		indexes = new int[n] ;
		distances = new double[n] ;
		
		for (int i=0;i<n;i++){
			iDist = indexedDistances.get(i);
			indexes[i] = iDist.getIndex() ;
			distances[i] = iDist.getDistance() ;
		}
		
		  
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int[] getIndexes() {
		return indexes;
	}

	public void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}

	public double[] getDistances() {
		return distances;
	}

	public void setDistances(double[] distances) {
		this.distances = distances;
	}

	public SurroundBuffers getParent() {
		return parent;
	}
	
	

	
}

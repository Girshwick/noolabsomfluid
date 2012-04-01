package org.NooLab.repulsive.components;

import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexDistance;

 
 
 
public class IndexListRetriever {

	ParticleGrid pgrid;
	
	int shapeId = ParticleGrid._CIRCLE ;
	
	// ========================================================================
	public IndexListRetriever( ParticleGrid pg){
		pgrid = pg ;
	}
	// ========================================================================
	
	
	public IndexListRetriever setShape( int shape ){
		
		if (shape== ParticleGrid._ELLIPSE) shape = ParticleGrid._CIRCLE ;
		
		shapeId = shape ;
		return this;
	}

	// e.g. for ellipse
	public IndexListRetriever setShape( int shape, double param1, double param2 ){
		
		shapeId = shape ;
		return this;
	}

	
	/**
	 * 
	 * TODO: to make this thread-safe, the method calls should just act as wrappers for objects in their own thread that do the job  
	 * 
	 * @param particleIndex
	 * @param radius
	 * @return
	 */
	public int[] getIndexesFromNeighboorhood( int particleIndex , double radius){
		
		GetIndexListThroughParticleGrid indexLister ;
		              // pgrid
		indexLister = new GetIndexListThroughParticleGrid( pgrid, particleIndex , radius) ;
		indexLister.shapeId = shapeId ;
		
		return indexLister.retrieve();
	}
	

	public int[] getIndexesFromNeighboorhood( double x, double y, int n ){
		return (new GetIndexListThroughParticleGrid( pgrid,x, y, n )).retrieve();
	}
	
	public int[] getIndexesFromNeighboorhood( double x, double y, double radius ){

			return (new GetIndexListThroughParticleGrid( pgrid,x, y, radius )).retrieve();
	}

	public int[] getIndexesFromNeighboorhood( int c, int r){
		return (new GetIndexListThroughParticleGrid( pgrid, c,r)).retrieve();
	}
	
	// --------------------------------------------------------------
	 
	 
	public ArrayList<IndexDistance> getIndexedDistancesFromNeighboorhood( int particleIndex , double radius){
		
		GetIndexListThroughParticleGrid indexLister ;
		
		indexLister = new GetIndexListThroughParticleGrid( pgrid, particleIndex , radius) ;
		indexLister.shapeId = shapeId ;
		
		return indexLister.retrieveIndexedDistances();
	}

	public ArrayList<IndexDistance> getIndexedDistancesFromNeighboorhood( double x, double y, int n ){
		GetIndexListThroughParticleGrid indexLister ;
		
		indexLister = new GetIndexListThroughParticleGrid( pgrid, x, y, n) ;
		indexLister.shapeId = shapeId ;
		
		return indexLister.retrieveIndexedDistances();
	 
	}
	
	public ArrayList<IndexDistance> getIndexedDistancesFromNeighboorhood( double x, double y, double radius ){
		GetIndexListThroughParticleGrid indexLister ;
		
		indexLister = new GetIndexListThroughParticleGrid( pgrid, x, y, radius) ;
		indexLister.shapeId = shapeId ;
		
		return indexLister.retrieveIndexedDistances();
 
	}

	public ArrayList<IndexDistance> getIndexedDistancesFromNeighboorhood( int c, int r){
		return (new GetIndexListThroughParticleGrid( pgrid, c,r)).retrieveIndexedDistances();
	}

	public IndexListRetriever setConstraints( SelectionConstraints selectconstraints) {
		
		pgrid.selectionConstraints = selectconstraints ;
		
		return this;
	}
	
	
}

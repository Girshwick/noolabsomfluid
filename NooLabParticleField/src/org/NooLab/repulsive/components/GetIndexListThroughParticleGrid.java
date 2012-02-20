package org.NooLab.repulsive.components;

import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.graph.TreeLinesIntf;
import org.NooLab.repulsive.components.data.IndexDistance;

public class GetIndexListThroughParticleGrid {

	ParticleGrid pgrid;
	
	ArrayList<IndexDistance> indexedDistances = new ArrayList<IndexDistance>();
	
	int particleIndex ;
	double radius;
	int count = -1 ;
	
	int particlesCount;
	
	int shapeId = ParticleGrid._CIRCLE ; 
	
	double maxDist = -1.0 ;
	
	SpatialGeomCalc spatialGeomCalc;
	
	// ====================================================================
	public GetIndexListThroughParticleGrid( ParticleGrid pg, int particleIndex , double radius){
		pgrid = pg;
		this.particleIndex = particleIndex ;
		this.radius = radius ;
		init();
	}
	
	public GetIndexListThroughParticleGrid( ParticleGrid pg,double x, double y, double radius ){
		pgrid = pg;
		particleIndex = pgrid.getIndexNear(x,y ) ;
		this.radius = radius ;
		init();
	}
	public GetIndexListThroughParticleGrid( ParticleGrid pg,double x, double y, int count ){
		
		pgrid = pg;
		particleIndex = pgrid.getIndexNear(x,y ) ;
		
		this.count = count;
		radius = estimateRequiredRadius(count);
		init();
	}
	
	public GetIndexListThroughParticleGrid( ParticleGrid pg,int c, int r, double radius){
		
		pgrid = pg;
		particleIndex = pgrid.getIndexNear( c, r ) ;
		
		this.radius = radius ;
		init();
	}
	
	public GetIndexListThroughParticleGrid( ParticleGrid pg,int c, int r, int count){
		
		pgrid = pg;
		this.particleIndex = pgrid.getIndexNear( c, r ) ;
		
		this.count = count;
		radius = estimateRequiredRadius(count);	
		init();
	}
	// ====================================================================

	private void init(){
		int w,h;
		w = pgrid.rfCore.getAreaWidth() ;
		h = pgrid.rfCore.getAreaHeight() ;
		spatialGeomCalc = new SpatialGeomCalc(w,h, pgrid.rfCore.getBorderMode() ); 
	}
	
	private void postProcessIndexesList(){
		
		int n;
		double p1,p2;
		
		if (count>0){
			n = count;
		}else{
			n = indexedDistances.size() ;
		}
		if (count>1){ 
			sortIxDist();
		}
		
		if (pgrid.selectionConstraints.isActive()){
			if (pgrid.selectionConstraints.currentShapeId == ParticleGrid._ELLIPSE) {
				p1 = pgrid.selectionConstraints.shapeParam1 ;
				p2 = pgrid.selectionConstraints.shapeParam2 ;
				
				// deselect particles outside the ellipse
				// AbstractCoverage
			}
		}
		
		
	}
	
	private TreeLinesIntf getMajorAxisInCircle(){
		
		return null;
	}
	private void calculateEllipse(){
		
		TreeLinesIntf treelines = getMajorAxisInCircle();
		/*
		
		applying jGeom
		
		 
		1. calculate distant points on circumference line:
		     - max distance in indexedDistances, angle calculation from middle point 
		     - getIndexNearLocation() , 2x
		     - determine line = major axis
		     - define length of minor axis as the half
		     
		2. calculate ellipsis for those axis
		     
		3. get coverage
		
		*/
	}
	
	// --------------------------------------------------------------------
	private double estimateRequiredRadius( int n ){
		double estRadius = 100.0, avgDist;
		int ab;
		
		particlesCount = pgrid.rfCore.particles.size();
		avgDist = pgrid.rfCore.getAverageDistance() ;
		
		// w = pgrid.rfCore.getAreaWidth() ;
		// h = pgrid.rfCore.getAreaHeight() ;
		
		// r = pgrid.grid.length ;
		// c = pgrid.grid[0].length ;
		
		// average degree of filling of the grid[][] array
		
		ab = (int) (1.2 * pgrid.radiusCorrectionFactor * (Math.sqrt(avgDist)+avgDist)/3.0 * Math.round(Math.sqrt( n + 1)) );
		
		estRadius = 1.4 * (double)ab;
		
		return estRadius;
	
	}		
	 
	private void sortIxDist(){
		boolean done = false;
		double ixd2,ixd1 ;
		IndexDistance ixdist0;
		
		while (done==false){
			done=true;
			
			for (int i=0;i<indexedDistances.size()-1;i++){
				
				ixd1 = indexedDistances.get(i).getDistance() ;
				ixd2 = indexedDistances.get(i+1).getDistance() ;
				
				if (ixd2 < ixd1){
					done=false;
					// not changing the list via remove, but just juggling the objects
					ixdist0 = indexedDistances.get(i);
					indexedDistances.set(i, indexedDistances.get(i+1));
					indexedDistances.set(i+1,ixdist0);
				}
				 
			}// i->
			
		} // done? ->
	}
	
	private int[] getIndexValuesFromIndexedDistances(){
		int[] neighbors = new int[0];
	 
		int n;
		 
		n = indexedDistances.size() ;
		
		pgrid.rfCore.out.print(4, "primary sice of selection n = "+n);
		try{

			if ((n>1) && (count<indexedDistances.size())){
				while (indexedDistances.size()>count){
					indexedDistances.remove( indexedDistances.size()-1) ; 
				}
				 
			}

		}catch(Exception e){}
		
		neighbors = pgrid.extractIndexesFromIndexedDistances(indexedDistances);
		
		return neighbors;
	}
	
	private void updateIndexedDistances( IndexDistance ixdist ){
		boolean hb;
		
		if (indexedDistances.size()==0){

			indexedDistances.add(ixdist) ;
			maxDist = ixdist.getDistance() ;
			
		}else{
			
			if (ixdist.getDistance() < indexedDistances.get(0).getDistance()){
				indexedDistances.add(0,ixdist) ;
			}else{
				if (ixdist.getDistance() < indexedDistances.get(indexedDistances.size()-1).getDistance()){
					indexedDistances.add(indexedDistances.size()-2,ixdist) ;
				}else{
					hb = true;
					if ( (count>0) && (indexedDistances.size()>count) && (maxDist<ixdist.getDistance())){
						hb = false;
					}
							
					if (hb){
						indexedDistances.add(ixdist) ;
						if (maxDist < ixdist.getDistance()){
							maxDist = ixdist.getDistance() ;
						}

					}
				}
			}
		}
	
	}

	private void performScreening( int rs,int re,int cs,int ce, double _px, double _py, Vector<Integer> nbv){
		
		double effDist,dx,dy   ;
		int ix;
		IndexDistance ixdist;
		
		
		
		for (int i=rs;i<re;i++){
			for (int j=cs;j<ce;j++){
				ix = pgrid.grid[i][j] ;
				if (ix<0){
				
				}
				if ((ix>=0) && (pgrid.rfCore.particles.get(ix)==null)){
					ix=-3;
					pgrid.grid[i][j] = -1;
				}
				if (ix>=0){
					// calculating effective distance
					effDist = -1;
				
					dx = spatialGeomCalc.getLinearDistanceX( _px, pgrid.rfCore.particles.get(ix).x) ;
					dy = spatialGeomCalc.getLinearDistanceY( _py, pgrid.rfCore.particles.get(ix).y) ;
				
					effDist = Math.sqrt( dx*dx + dy*dy) ;
					
					if (effDist<=radius){
						if (nbv.indexOf(ix)<0){
							nbv.add(ix) ;
							ixdist = new IndexDistance(ix,effDist,"") ;
							// insert sorted...
							updateIndexedDistances( ixdist );
						}
					}
					int nix = indexedDistances.size() ;
				}
			} // j->all col in patch
		} // i-> all rows in patch
		 
	}

	private int[] retrieveListOfParticles( int c, int r, double _px, double _py){

		int[] neighbors = new int[0];
		int  dcr,cs,rs ,ce,re,cx,rx, cxx=0 , rxx=0;
		
		Vector<Integer> nbv = new Vector<Integer>(); 
		
		boolean selectionSizeOk=false;
		
		indexedDistances.clear() ;
		
		if (pgrid.selectionConstraints==null){
			pgrid.selectionConstraints = new SelectionConstraints(pgrid.rfCore) ;
		}
		// int cc = this.count ;
		// double res = pgrid.resolution;
		
		selectionSizeOk = false;
		int zz=0;
		
		while (selectionSizeOk==false){
			dcr =  (int) (Math.round( radius / pgrid.resolution)+1) ;
		
		
			cs = c - dcr-1 ; 
			rs = r - dcr-1 ; 
			ce = c + dcr+1 ;
			re = r + dcr+1 ;
			// bordermode !!!
		
			if ((count<=3) || ( pgrid.rfCore.getBorderMode() == ParticleGrid.__BORDER_ALL)){  // standard rectangle
				if (cs<0)cs=0;
				if (rs<0)rs=0;
				
				if (ce>pgrid.grid[0].length-1) ce = pgrid.grid[0].length-1;
				if (re>pgrid.grid.length-1)    re = pgrid.grid.length-1;
			} // __BORDER_ALL 
			
			if ((count>3) && ( pgrid.rfCore.getBorderMode() == ParticleGrid.__BORDER_NONE)){  // torus
				
				cx = -1;
				rx = -1;
				
				if (cs<0){
					cx = pgrid.grid[0].length-1 + cs;
					cs=0;
					cxx = pgrid.grid[0].length - 1 ;
					rxx = pgrid.grid.length - 1 ;
				}
				if (rs<0){
					rx = pgrid.grid.length-1 + rs;
					rs=0;
					cxx = pgrid.grid[0].length - 1 ;
					rxx = pgrid.grid.length - 1 ;
				}
				
				if (ce>pgrid.grid[0].length-1){
					cx=0; 
					cxx = ce - pgrid.grid[0].length ;
					ce = pgrid.grid[0].length-1;
				}
				if (re>pgrid.grid.length-1){
					rx=0; 
					rxx = re - pgrid.grid.length ;
					re = pgrid.grid.length-1;
				}
				
				
				
				// .........
				if ((rx >= 0) && (cx >= 0)) {
					performScreening(rx, rxx, cx, cxx, _px, _py,nbv);
					performScreening(rs, re, cx, cxx, _px, _py,nbv);
					performScreening(rx, rxx, cs, ce, _px, _py,nbv);
				} else {
					if ((rx >= 0) && (cx < 0)) {
						performScreening(rx, rxx, cs, ce, _px, _py,nbv);
					} else {
						if ((rx < 0) && (cx >= 0)) {
							performScreening(rs, re, cx, cxx, _px, _py,nbv);
						}
					}
				}

			} // __BORDER_NONE
		
			
			performScreening(rs,re,cs,ce,_px,_py,nbv);
			// count is target size,  
			if ((count>3) && (count *0.85 > indexedDistances.size())){
				// adjust radius ;
				// dcr*2*10 ~
				double cr = (double)(count *0.85)/(double)indexedDistances.size();
				pgrid.resolution = pgrid.rfCore.getMinimalDistance()/2.3;
				radius = estimateRequiredRadius( (int)(count * cr) );
				dcr =  (int) (Math.round( radius / pgrid.resolution)+1) ;
				if (zz>3){
					break;
				}
				nbv.clear() ;
				zz++;
			}else{
				selectionSizeOk=true; 
				break;
			}
		} // selection size ok ?
		// sorting, selecting a shape
		postProcessIndexesList() ;
		
		
		if (indexedDistances.size()>0){
			neighbors = getIndexValuesFromIndexedDistances();
		}
		count = indexedDistances.size();
		
		nbv.clear() ;
		return neighbors;
	}
	
	
	public int  retrieveParticleForCoordinate(double cx, double cy) {
		 
		int pix = -1;
		int c,r ;
		int[] neighbors = new int[0];
		
		// int dcr =  (int) (Math.round(radius / pgrid.resolution)+1) ;
		
		if (radius<0){
			
			radius = 3.0 * pgrid.rfCore.getAverageDistance() ;
		}
		count = 3 ;
		particlesCount = 3;
		
		c = (int)(((double)(cx*1.0))/pgrid.resolution); 
		r = (int)(((double)(cy*1.0))/pgrid.resolution);
		
		neighbors = retrieveListOfParticles( c,r, cx,cy) ;
		
		int z=0; pix=0;
		while ((z<particlesCount) && (pix==0)){
			if (neighbors.length > 0) {
				pix = neighbors[0+z];
			}
			z++;
		}
		return pix;
	}

	public int[] retrieve(){
		

		int[] neighbors = new int[0];
		
		
		int c=0,r=0,ix;
		double _px,_py ;
		
		
		 
		
		try{
			 
			if (particleIndex>=0){
				r = pgrid.rowMap.position.get(particleIndex ) ;
				c = pgrid.colMaps[r].position.get(particleIndex ) ;
			
				ix = pgrid.getIndexNear( c,r ) ;
				
				_px = pgrid.rfCore.particles.get(particleIndex).x ;
				_py = pgrid.rfCore.particles.get(particleIndex).y ;
				
				neighbors = retrieveListOfParticles( c,r, _px, _py);
				
			} else {
				
				_px = c * pgrid.resolution;
				_py = r * pgrid.resolution;
				
				ix = retrieveParticleForCoordinate(_px, _py);
			 
				if (ix>=0){
					particleIndex = ix;
					neighbors = retrieve() ;
				}
			}
			
			if ( ((double)neighbors.length)<0.98*((double)count)){
				pgrid.radiusCorrectionFactor = ((double) count)/((double)neighbors.length );
				pgrid.rfCore.out.print(4, "selection was too small ( actual:"+neighbors.length+", requested:"+count+")");
				pgrid.rfCore.out.print(4, "radius will be corrected next time by factor rcf = "+ pgrid.radiusCorrectionFactor);
			}
			
			pgrid.rfCore.out.print(3, "actual selection count n="+neighbors.length);
		}catch(Exception e){
		}
		
		return neighbors;
	}
	
	public ArrayList<IndexDistance> retrieveIndexedDistances(){
		
		retrieve();
		
		return indexedDistances ;
	}
	
	public ArrayList<IndexDistance> getIndexedDistances(){
		return indexedDistances ;
	}
}

package org.NooLab.field.repulsive.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.NooLab.field.FieldGridSubstrateIntf;
import org.NooLab.field.FieldIntf;
import org.NooLab.field.fixed.FixedField;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.data.RetrievalParamSet;
import org.NooLab.field.repulsive.components.topology.CoverageByBars;
import org.NooLab.graph.TreeLinesIntf;

 
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.strings.ArrUtilities;



/**
 * 
 * this is a sister component for "GridSubstrate"
 * 
 * 
 *
 */
public class ParticleGrid implements FluidFieldGridSubstrateIntf{

	
	//RepulsionFieldCore rfCore ;
	// FieldIntf field ;
	RepulsionFieldCore field;
	private RepulsionFieldCore rfCore;
	private FixedField fixedField;
	
	// the grid contains a pointer to the particle index
	int[][] grid ; 
	
	
	/** the map contains tuples ( particle index, row index in grid[][] )  */
	PositionMap rowMap = null ;
	
	PositionMap[] colMaps ;
	
	double resolution = 30.0 ;

	private boolean gridIsInactive = true ;
	
	double radiusCorrectionFactor = 1.0 ;
	
	int updateCounter=0;
	
	SelectionConstraints selectionConstraints ; 
	
	ArrUtilities arrutil = new ArrUtilities();
	
	
	// ========================================================================
	public ParticleGrid( RepulsionFieldCore rfc ) {
		rfCore = rfc; 
		field = rfc ;
	}
	
 
	// ========================================================================
	
	public void cloneDataSectionsFrom( ParticleGrid pg ){
		
		rowMap = pg.rowMap.clonePositionMap() ;
		
		colMaps = new PositionMap[pg.colMaps.length] ;
		
		for (int i=0;i<colMaps.length;i++){
			colMaps[i] = pg.colMaps[i].clonePositionMap() ;
		}
		//System.arraycopy( pg.colMaps, 0, colMaps ,0, pg.colMaps.length);
		// (Object src,int srcPos, Object dest, int destPos, int length) 
		
		resolution = pg.resolution ;

		radiusCorrectionFactor = pg.radiusCorrectionFactor ;
		
		selectionConstraints = pg.selectionConstraints ;
		
		grid = new int[pg.grid.length][pg.grid[0].length] ;
		
		for (int i=0;i<pg.grid.length;i++){
			System.arraycopy( pg.grid[i] , 0, grid[i],0, pg.grid[i].length);
		}
		
	}

	public int[] extractIndexesFromIndexedDistances( ArrayList<IndexDistance> ixDists ){
		
		int[] neighbors = new int[0];
		int n; 
		// particles.get(ix).getIsAlive()>0;
		
		n = ixDists.size() ;
		neighbors = new int[n];
		
		for (int i=0;i<n;i++){
			neighbors[i] = ixDists.get(i).getIndex() ;
		}
		 
		
		return neighbors;
	}
	
	public void update() {
		double minDistance = -1.0,x,y;
		int ncol,nrow,c,r;
		
		minDistance = field.getMinimalDistance() ;
		if ((minDistance<0) || (minDistance>999999999.9)){
			// rfCore.out.print(2, "ParticleGrid, update not possible...") ;
			return ;
		}
		
		if (rowMap!=null){
			rowMap.position.clear() ;
			rowMap.position=null;
		}
		rowMap = new PositionMap();
		
		resolution = minDistance/2.3;
		
		ncol = (int) (Math.round(field.getAreaWidth()/resolution) +1);
		nrow = (int) (Math.round(field.getAreaHeight()/resolution) +1);
		
		// rfCore.out.print(4, "ParticleGrid, updating r,c: "+nrow+", "+ncol) ;
		
		grid = new int[nrow][ncol] ; // x,y
		colMaps = new PositionMap[nrow];
		
		for (int i=0;i<nrow ;i++){
			colMaps[i] = new PositionMap();
			for (int j=0;j<ncol;j++){
				grid[i][j] = -1;
			}
		}
		
		c=0;
		for (int i=0;i<field.getParticles().size();i++){
			
			x = field.getParticles().get(i).getX() ;
			y = field.getParticles().get(i).getY() ;
			
			c = (int) Math.round(x/resolution) ;
 			r = (int) Math.round(y/resolution) ;
 			
 			if (grid[r][c]>=0){
 				// rfCore.out.printErr(2, "grid position <"+c+","+r+"> already taken by index "+grid[c][r]+", will be overwritten by index "+i+"! ");
 			}
			grid[r][c] = i;
			
			rowMap.position.put(i, r) ;
			colMaps[r].position.put(i,c);
		} // i-> all particles
		c=0;
		
		 
	}
	
	public void addSingleParticle( double xpos, double ypos, int newIndexValue ){
		int ixNear,ix , rowIx, colIx, c,r ;
		// 1. get particle next to it
		
		ixNear = this.getIndexNear(xpos, ypos) ;
		
		rowIx = rowMap.getPosition().get(ixNear) ;
		colIx = colMaps[rowIx].getPosition().get(ixNear);
		
		// 2. get grid[][] entry
		ix = grid[rowIx][colIx] ;
		
		// 3. calculate entry for new position
		c = (int) Math.round((double)xpos/resolution) ;
		r = (int) Math.round((double)ypos/resolution) ;
		
		ix = grid[r][c] ;
		
		int z=0,rx,cx;
		rx = r; cx=c;
		while ((ix>=0) && (z<10)){
			z++;
			if (z%4==0){
				cx=cx+ z%3;
			}else{
				if (z%3==0){
					rx = rx+z%2 ;
				}else{
					if (z%2==0){
						cx++;
					}else{
						rx++;
					}
				}
			}
			ix = grid[rx][cx] ;
		}

		if (ix>=0){
			return;
		}
		r = rx; c=cx;
		
		
		// insert it next ; 
		grid[r][c] = newIndexValue ;
		rowMap.position.put( newIndexValue, r );
		colMaps[r].position.put( newIndexValue,c );
	}

	public void remove(int particleindex) {
		int ixNear,ix , rowIx, colIx, c,r ;
		// 1. get particle next to it
		 
		rowIx = rowMap.getPosition().get(particleindex) ;
		colIx = colMaps[rowIx].getPosition().get(particleindex); 
	
		ix = grid[rowIx][colIx] ;
		grid[rowIx][colIx] = -1;
		            
	}
	
	
	// --------------------------------------------------------------
	
	public int getIndexNearLocation( int xpos, int ypos ){
		double x, y;
		
		x = (double)(1.0* xpos) ;
		y = (double)(1.0* ypos) ;

		return getIndexNear( x, y );
	}
	
	public int getIndexNear( double x, double y ){
		int c,r,resultPos = -1;
		
		c = (int) Math.round(x/resolution) ;
		r = (int) Math.round(y/resolution) ;
			
		resultPos = getIndexNear( c,r ) ;
		
		return resultPos;
	}
	
	public int getIndexNear( int c, int r ){
		
		int ix, dc=0,dr=0,dd=0, z,resultPos = -1;
		int[] candidates;
		
		
		ix = grid[r][c] ;
		if (ix<0){
			// this does not work .... better; calculate a box and get indexed distances
			z=0;
			ix = (new GetIndexListThroughParticleGrid( this, -1, 2*field.getAverageDistance() )).retrieveParticleForCoordinate( c*resolution, r*resolution);
	 
		}
		resultPos = ix;
		
		return resultPos;
	}
	
	public IndexListRetriever getIndexListRetriever(){
		
		IndexListRetriever ixLister = new IndexListRetriever( this );
		return ixLister ;
	} 
	
	
	// ........................................................................
	public void deactivate() {
		 gridIsInactive=true;
	}
	public boolean isGridInactive() {
		return gridIsInactive;
	}
	
	public void reactivate(){
		gridIsInactive=false;
	}

	public void clear() {
		 
		
		
		grid = new int[0][0] ;
		/** the map contains tuples ( particle index, row index in grid[][] )  */
		rowMap.position.clear();
		
		for (int i=0;i<colMaps.length;i++){
			colMaps[i].position.clear();
			colMaps[i] = null;
		}
		colMaps = new PositionMap[0] ;
		
	}

	public int incUpdateCounter(int period) {
		
		if (updateCounter>1001)updateCounter=1;
		
		updateCounter++;
		return updateCounter%period;
	}

	public RepulsionFieldCore getRfCore() {
		return rfCore;
	}

	public FixedField getFixedField() {
		return fixedField;
	}

	public FieldIntf getField() {
		return field;
	}

	@Override
	public SelectionConstraints getSelectionConstraints() {
		
		return selectionConstraints;
	}

	@Override
	public double getRadiusCorrectionFactor() {
		
		return radiusCorrectionFactor;
	}

	@Override
	public int[][] getGrid() {
		
		return grid;
	}

	@Override
	public void setSelectionConstraints(SelectionConstraints selectconstraints) {
		
		selectionConstraints = selectconstraints ;
	}

	@Override
	public double getResolution() {
		// 
		return resolution;
	}

	@Override
	public void setRadiusCorrectionFactor(double value) {
		// 
		radiusCorrectionFactor  =value;
	}

	@Override
	public void setResolution(double value) {
		
		resolution = value;
	}

	@Override
	public Object[] getColMaps() {
		
		return colMaps;
	}

	@Override
	public Object getRowMap() {
		
		return rowMap;
	}

	@Override
	public int getColMapsPositions(int r, int particleIndex) {
		
		return colMaps[r].getPosition().get(particleIndex) ;
	}

	@Override
	public int getRowMapPosition(int particleIndex) {
		
		return rowMap.position.get(particleIndex);
	}


	
	
	
}




class PositionMap implements Cloneable{
	
	public Map<Integer,Integer> position;
	
	public PositionMap(){
		position = new TreeMap<Integer,Integer>();
	}
	
	public PositionMap clonePositionMap(){
		PositionMap pmap=null;
		try {
			
			pmap = (PositionMap) this.clone();
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pmap;
	}

	public Map<Integer, Integer> getPosition() {
		return position;
	}

	public void setPosition(Map<Integer, Integer> position) {
		this.position = position;
	}
	
	
	
}

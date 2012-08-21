package org.NooLab.field.fixed.components;

import java.util.ArrayList;

import org.NooLab.field.FieldGridSubstrateIntf;
import org.NooLab.field.repulsive.components.ParticleGrid;
import org.NooLab.field.repulsive.components.SelectionConstraints;
import org.NooLab.field.repulsive.components.SpatialGeomCalc;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;

 
 
 
public class IndexListRetriever {

	// ParticleGrid pgrid;
	FixedFieldGridSubstrateIntf fgrid;
	
	int shapeId = FieldGridSubstrateIntf._CIRCLE ;
	
	PrintLog out = new PrintLog(2,false);
	// ========================================================================
	public IndexListRetriever( FixedFieldGridSubstrateIntf fixedgridsubstrate ){
		fgrid = fixedgridsubstrate ;
	}
	// ========================================================================
	
	
	public IndexListRetriever setShape( int shape ){
		
		if (shape== FieldGridSubstrateIntf._ELLIPSE) shape = FieldGridSubstrateIntf._CIRCLE ;
		
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
		
		
		// return indexLister.retrieve();
		return null;
	}
	

	public int[] getIndexesFromNeighboorhood( double x, double y, int n ){
		return null ;
	}
	
	public int[] getIndexesFromNeighboorhood( double x, double y, double radius ){

			return null;
	}

	public int[] getIndexesFromNeighboorhood( int c, int r){
		return null;
	}
	
	// --------------------------------------------------------------
	 
	 
	public ArrayList<IndexDistance> getIndexedDistancesFromNeighboorhood( int particleIndex , double radius){
		
		return null;
	}

 


	/**
	 * since each particle knows about its position, these x,y coordinates refer to the substrate grid !!
	 * 
	 * @param x
	 * @param y
	 * @param surroundN
	 * @return
	 */
	public IndexedDistances getIndexedDistancesFromNeighboorhood( int x, int y, int surroundN ){

		int plc,plic,xd,xc,yc,ax=0,ax1,ay1,ay=0,dd,lc,colln=0;
		int res = (int)fgrid.getResolution() ;
		boolean xok, yok;
		double dix,diy ;
		
		GridNode gridnode ;
		IndexDistance ixd;
		IndexedDistances ixds = new IndexedDistances();
		
		SpatialGeomCalc geom ;
		
		
		
		int layercount = 1 ; // dependent on requested number of nodes, we have to visit 1+ "onion shells"
							 // if we run with borders, we have to increase this number by +1 + (L*0.7) as a lower threshold
		
		layercount = calculateLayerCount( surroundN, -1.0) ;
		
		GridNode[][] grid = fgrid.getGrid() ;
		// fgrid.
		xc = (int)x;
		yc = (int)y;
		
		dd=0; // take random direction here
		// d runs from 0..7,
		lc=0;
		plc=0; // physical layers on the substrate, it is n-times more !!
		
		
		try{
			
			geom = new SpatialGeomCalc( grid[0].length, // width
										grid.length, 	// height
										ParticleGrid.__BORDER_NONE); // TODO : where is the variable ????
													
			while (ixds.size()<surroundN){
				lc++; // next layer
				plc  = (int)(lc * fgrid.getResolution()) ; // resolution
				plic = (int)((lc-1) * fgrid.getResolution()) ; 
				
				/* all combinations of dx,dy [-1 +1]
				   of more general dy,dy  [-lc, +lc] without 
				   - those already met
				   - those outside of optional borders 
				  and with translation of torus topology
				  
				  Note, that we have to visit all physical gridnodes and test them 
				  whether they contain a som node
				   
				 */
				
				for (int dx= -plc;dx<plc;dx++){
					
					if (Math.abs(dx)<= (plic) ){
						// continue;
					}
					ax1 = xc+dx;
					// is it allowed ?
					xok = true;
					// test for borders, already visited etc.
					ax = adjustForBorders(ax1,grid[0].length,0); // returns -3 if out of range
					xok = (ax>=0) && (ax<grid[0].length) && (ixds.size()<surroundN);
					if (xok){
						// already seen?
						
					}
					
					if (xok){
						for (int dy= -plc;dy<plc;dy++){
							if (Math.abs(dx)<= (plic) ){
								// continue;
							}
							ay1 = yc + dy;
							yok = true;
							// test for borders, already visited etc.
							ay = adjustForBorders(ay1,grid.length,1); // returns -3 if out of range
							yok = (ay>=0)  && (ixds.size()<surroundN);
							if (yok){
								yok = ((ay==yc) && (ax==xc))==false;	
							}
							
							if (yok){
								// already seen
out.print(4, "ax, ay : "+ax+", "+ay);
if (ay>=29){
	ax=ax+1-1;
}
								gridnode = grid[ay][ax] ;
								if (gridnode==null){
									ax=ax+1-1; continue;
								}
								if ((gridnode!=null) && (gridnode.hasNode>0 ) && (gridnode.nodeIsActive>0) && (gridnode.particleIndex>=0) ){
									int pix = gridnode.particleIndex;
									
									int ix = ixds.indexOfIndex(pix) ;
									if (ix<0){
										// calculating the distance while respecting the border mode...
										dix = geom.getLinearDistanceX(xc,ax);
										diy = geom.getLinearDistanceY(yc,ay);
										
										double dist = Math.sqrt( (dix)*(dix) + (diy)*(diy));
										
										ixd = new IndexDistance(pix, dist, "");
										
										ixds.add(ixd) ;
									} // already contained ?
								} // gridnode ?
									
							} // yok ?
						} // dy->
					} // xok ?
				} // dx->
				
			} // -> collected items number ?
				
			
			
		}catch(Exception e){
			ax=ax+1-1; ay=ay+1-1;
			e.printStackTrace();
		}
		ax=0;
		ixds.sort(1) ;
		
		return ixds;
	}


	
	
	/**
	 * 
	 * @param cc coordinate
	 * @param limitvalue max position for coordinate
	 * @param direction we work only with rectangular sheets or fields, thus d = [0=x,1=y]
	 * @return  -3 if out of range
	 */
	private int adjustForBorders(int cc, int limitvalue, int direction) {
		int adjcc = cc;
		// 
		// get border settings: all, none, left, ....
		
		// default = none
		
		if (cc>=limitvalue){
			adjcc = cc-limitvalue;
		}else{
			if (cc<0){
				adjcc = limitvalue - Math.abs(cc);
			}
		}
		
		
		return adjcc;
	}
	
	
	private int calculateLayerCount(int surroundN, double distanceToBorder) {
		
		int cn=0, lc=0;
		
		while (cn<surroundN){
			lc++;
			cn = (lc+2)*(lc+2)-1;
		}
		
		return lc;
	}
	
	
	public ArrayList<IndexDistance> getIndexedDistancesFromNeighboorhood( int c, int r){
		return null;
	}

	public IndexListRetriever setConstraints( SelectionConstraints selectconstraints) {
		
		fgrid.setSelectionConstraints( selectconstraints );
		
		return this;
	}
	
	
}

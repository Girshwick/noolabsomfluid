package org.NooLab.field.fixed.components;

import java.util.ArrayList;

import org.NooLab.field.FieldGridSubstrateIntf;
import org.NooLab.field.FieldIntf;
import org.NooLab.field.fixed.FixedField;
import org.NooLab.field.fixed.FixedFieldFactory;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.SelectionConstraints;
import org.NooLab.utilities.datatypes.IndexDistance;



/**
 * 
 * 
 * 	the substrate is sth like the Glia in brain tissue...
 *	
 *	it defines the accessible physical space for associative nodes
 *  Not all physical locations in the GridSubstrate are accessible for neurons
 *  
 *	technically, it is the sister class of ParticleGrid, which does 
 *  the "same" things for fluid grids
 * 
 *
 */
public class GridSubstrate implements FixedFieldGridSubstrateIntf{


	FixedField fixedField;
	FixedFieldFactory ffFactory ;
	
	GridNode[][] gridnodes ;
	
	ArrayList<GridNode> activeNodes = new ArrayList<GridNode>(); 
	
	private int[] factorization = new int[0];
	double aspectRatio = 1.0 ;
	double averageDistance = 1.0 ;
	
	private int gridResolutionFactor = 5 ; // n-1 empty "strings" between nodes

	private int nbrParticles=-1;

	private int actualNumberOfNodes  = -1;
	private double resolution  = 5;
	private double radiusCorrectionFactor  =1.0 ;
	private SelectionConstraints selectionConstraints;
	
	
	
	
	// ========================================================================
	public GridSubstrate(FixedField fixedfield, int nbrParticles){
		
		int w,h ;
		
		fixedField = fixedfield;
		ffFactory = fixedField.getFfFactory() ;
		
		this.nbrParticles = nbrParticles;
		
		setFactorization(calculateWidthHeightFactors(1,nbrParticles));
		w = getFactorization()[2] +(getGridResolutionFactor()-1) ;
		h = getFactorization()[3] +(getGridResolutionFactor()-1) ;
		
		gridnodes = new GridNode[w][h] ;
		
		preparePopulating();
		
		fixedField.setAreaWidth(w) ;
		fixedField.setAreaHeight(h) ;
		fixedField.setAreaSize(w, h) ;
		
		w=0;
	}
	// ========================================================================	

	private void preparePopulating() {
		int w,h,p;
		
		p=0;
		for (int x=0;x<=getFactorization()[2];x++){
			for (int y=0;y<=getFactorization()[3];y++){
				w = x % getGridResolutionFactor() ;
				h = y % getGridResolutionFactor() ;
				if ((w==0) && (h==0)){
					if (p<nbrParticles){
						GridNode g = new GridNode(p);
						g.hasNode = 1;
						g.nodeIsActive = 0;
						g.particleIndex = p;
						g.x = x ;
						g.y = y ;
						
						gridnodes[x][y] = g ;
						p++;
						actualNumberOfNodes = p;
						
						activeNodes.add(g) ;
						
					} // ?
				} // ?
			} // ->
		} // ->
		p=0;
	}

	
	private int[] calculateWidthHeightFactors( int layout, int nodeCountTarget){
		int[] wh = new int[4];
		int w=0,h=0,ww,hh;
		int newsize1, newsize2;
		double d1, d2;
		
		
		if (layout==1){ // SQUARE
			w = (int) (Math.sqrt(nodeCountTarget) * ((1.0 + aspectRatio) / 2.0) * averageDistance);
			h = (int) (Math.sqrt(nodeCountTarget) / ((1.0 + aspectRatio) / 2.0) * averageDistance);

			// correct w and/or h if necessary, correction according to layout

			
			
			while (w * h < nodeCountTarget) {
				ww = w;
				hh = h;
				newsize1 = (w + 1) * h;
				d1 = (nodeCountTarget - newsize1);
				newsize2 = (w) * (h + 1);
				d2 = (nodeCountTarget - newsize2);
				
				if ( ((d1>0) && (d2>0)) || ((d1<=0) && (d2<=0))){
					if (Math.abs(d1) < Math.abs(d2)) {
						w++;
					} else {
						h++;
					}
					if (d1<=0){
						break;
					}
				}
				if ((d1<=0) && (d2>0)){
					w++; break;
				}
				if ((d2<=0) && (d1>0)){
					h++; break;
				}
				
			} // ->
		} // layout==1 ?
		
		if (layout==2){ // HEX
			w = (int) (Math.sqrt(nodeCountTarget) * ((1.0 + aspectRatio) / 2.0) * averageDistance);
			h = (int) (Math.sqrt(nodeCountTarget) / ((1.0 + aspectRatio) / 2.0) * averageDistance);
			/* e.g.
					
			  		30 x 30
					30 / 5  = 6, -> 7 particles
					7 6 7 6 7 6 7 = 28 + 18 = 46
			 */
		}
		ww=w;
		hh=h;
		// define the grid
		if (getGridResolutionFactor()>1){
			// (grf-1) inserts between 2 items, total: (grf-1)*(w-1)
			ww= w + (w-1)* (getGridResolutionFactor()-1) - 1; 
			hh= h + (h-1)* (getGridResolutionFactor()-1) - 1;
		}
		wh[0]=w;
		wh[1]=h;
		wh[2]=ww;
		wh[3]=hh;
		return wh;
	}



	
	public ArrayList<GridNode> getListOfActiveNodes() {

		if ((activeNodes==null) || (activeNodes.size()==0)){
			
			// GridNode[][] gridnodes ; nbrParticles
		}
		
		return activeNodes;
	}

	public void createParticles() {
		
		GridNode g;
		
		
		for (int n=0;n<activeNodes.size();n++){
		
			g = activeNodes.get(n) ;
		
			fixedField.createParticleForGridNode(g);
			
		}// n->
		
	}
	
	public IndexListRetriever getIndexListRetriever(){
		
		IndexListRetriever ixLister = new IndexListRetriever( this );
		return ixLister ;
	} 

	
	// ------------------------------------------------------------------------

	public void setGridResolutionFactor(int gridResolutionFactor) {
		this.gridResolutionFactor = gridResolutionFactor;
	}

	public int getGridResolutionFactor() {
		return gridResolutionFactor;
	}

	public void setFactorization(int[] factorization) {
		this.factorization = factorization;
	}

	public int[] getFactorization() {
		return factorization;
	}


	@Override
	public FieldIntf getField() {
		
		return fixedField; 
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
	public int getIndexNear(double x, double y) {
		
		return -1;
	}

	
	@Override
	public int[] extractIndexesFromIndexedDistances(ArrayList<IndexDistance> ixDists) {
		
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

	@Override
	public GridNode[][] getGrid() {
	
		 
		return gridnodes;
	}
 

	@Override
	public void setSelectionConstraints(SelectionConstraints selectionConstraints) {
		
	}

	@Override
	public double getResolution() {
		return resolution;
	}

	@Override
	public void setRadiusCorrectionFactor(double value) {
		radiusCorrectionFactor = value;
	}

	@Override
	public void setResolution(double value) {
		
		resolution = value;
	}


}

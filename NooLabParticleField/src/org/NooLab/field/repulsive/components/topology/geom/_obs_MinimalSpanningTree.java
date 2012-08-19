package org.NooLab.field.repulsive.components.topology.geom;



import java.util.Arrays;

import org.NooLab.field.repulsive.components.data.LineXY;
import org.NooLab.field.repulsive.components.data.PointXY;

/**
 * 
 * this class is very important for implicit higher-order associativity of SOMs.
 * 
 * The support points of the MST are some clusters, that are selected based on
 * some criteria; The dynamic and quasi-METAPHORICAL association then is created
 * by the nodes that are activated as a coverage indeuced by the MST between
 * those support points;
 * 
 * such, the MST creates an almost virtual connectivity, that is highly dynamic
 * and purely dependent on the eveolution of the SOM, without ANY influence of
 * coded proerties of the SOM
 * 
 */
public class _obs_MinimalSpanningTree {
	
	private int QuadVal = 1; // the index type
	private int TreeVal = 1; // the tree type

	private PointXY[] points ;    // the terminals
	private PointXY[] linePoints; // the INDICES of the points that build the line
	private PointXY[] edges; // the INDICES of the points that build the line
	

	public _obs_MinimalSpanningTree( PointXY[] spoints ) {
		
		points = Arrays.copyOf(spoints,spoints.length);
		
		linePoints = new PointXY[points.length];
		calculateTree();
	}

	public LineXY[] getTreeLines() {

		LineXY[] treeLines = new LineXY[0];
		int z=0;
		try{

			if ((linePoints!=null) &&(linePoints.length>=2)){
				treeLines = new  LineXY[ linePoints.length-1];
				// all != null ? 
				for (int i=0;i<linePoints.length-1;i++){
					if (linePoints[i]!=null)z++;
				}
				treeLines [0] = new LineXY();
				for (int i=0;i<z-1;i++){
					
					
					if (linePoints[i+1]!=null){
						treeLines [i] = new LineXY();
						
						treeLines[i].x[0] = linePoints[i].x;
						treeLines[i].y[0] = linePoints[i].y;
						treeLines[i].x[1] = linePoints[i+1].x;
						treeLines[i].y[1] = linePoints[i+1].y;
					}else{
						z=0;
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace() ;
		}
		return treeLines;
	}

	
	/**
	 * 
	 Function name: minst Description: compute a minimum spanning tree using
	 * Prim's algorithm with "dumb" heaps.
	 */
	private void calculateTree() {
		int n ; // the current number of terminals
		int dist[], neigh[], closest, minDist, d,k;
		// m is the adjacency matrix
		boolean m[][]; // the minimum spanning tree edges
		
		n = points.length ;

		m = new boolean[n][n];
		dist = new int[n];
		neigh = new int[n];
	
		// initialize data structures
		for (int i = 0; i < n; i++) {
			dist[i] = distance(points[0].x, points[0].y, points[i].x, points[i].y);
			neigh[i] = 0;
			for (int j = 0; j < n; j++) {
				m[i][j] = false;
			}
		}
	
		// find terminal closest to current partial tree
		for (int i = 1; i < n; i++) {
			closest = -1;
			minDist = Integer.MAX_VALUE;
			for (int j = 1; j < n; j++) {
				if ((dist[j] != 0) && (dist[j] < minDist)) {
					closest = j;
					minDist = dist[j];
				}
			}
	
			// set an edge from it to its nearest neighbor
			m[neigh[closest]][closest] = true;
			m[closest][neigh[closest]] = true;
	
			// update nearest distances to current partial tree
			for (int j = 1; j < n; j++) {
				d = distance( points[j].x, points[j].y, points[closest].x, points[closest].y);
				if (d < dist[j]) {
					dist[j] = d;
					neigh[j] = closest;
				}
			}
		}
	
		// update the edges arrey
		k = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (m[i][j]) {
					linePoints[k] = new PointXY();
					linePoints[k].x = points[i].x;
					linePoints[k].y = points[i].y;
					 
					k++;
				}
			}
		}
		
		k=0; 
	} // calculateTree

	
	/***********************************************************************
	 * Function name: distance Description: Euclidean distance between two
	 * points (x1,y1) and (x2,y2)
	 ***********************************************************************/
	private int distance(double x1, double y1, double x2, double y2) {
		return ((int) Math.round(Math.sqrt((double) (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))));
	} // distance()
	
	

}

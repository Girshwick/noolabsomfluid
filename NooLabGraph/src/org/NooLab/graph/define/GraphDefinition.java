package org.NooLab.graph.define;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.NooLab.graph.CVertex;
import org.NooLab.graph.PointXYIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.VertexImpl;
import org.NooLab.graph.commons.WeightedGraphIntf;




public class GraphDefinition {

	WeightedGraphIntf initialGraph;
	
    ArrayList<CVertex> cVertices = new ArrayList<CVertex>();
	
    Map<Integer,Object>vertexMap = new HashMap<Integer,Object>();
    
    
	public GraphDefinition(WeightedGraphIntf initialgraph) {
		initialGraph = initialgraph;
	}
	
	

	// ------------------------------------------------------------------------
	
	
	public void addXYPointsAsVertices( ArrayList<PointXYIntf> points){
		
		double[][] distances;
		int n = points.size() ;
		double d;
		
		distances = new double[n][n];

		for (int i=0;i<points.size();i++){
			
			addVertex( i, points.get(i).getX(), points.get(i).getY()) ;
		}
		
			
		for (int i=0;i<points.size();i++){
			
			
			for (int j=i+1;j<points.size();j++){
				d = distance( points.get(i), points.get(j));
				
				distances[i][j] = d;
				distances[j][i] = d;

				connectByPosition( i, j, d ) ;
			}// j ->
		} // i -> 
		
		d=0;
	}
	
	private double distance( PointXYIntf p1, PointXYIntf p2){
		
		double dx, dy;
		
		dx = p1.getX() - p2.getX();
		dy = p1.getY() - p2.getY();
		
		double d = Math.sqrt((double) (dx*dx) + (dy*dy));
		return d;
	}
	
	// ..........................................
	
	/**
	 * index is referring to the context of the caller;
	 * the return value is the index of the vertex in the Graph object
	 * 
	 */
	public int addVertex( int index, double x, double y){
		return addVertex( index, x, y, "v"+initialGraph.getVerticesCount());
	}
	
	public int addVertex( int index, double x, double y,   String label){
		Vertex	vertex ;
		CVertex cv;
		int vIndex = -1 ;
		
		try{
			
			vertex	= new VertexImpl( label );
			cv = new CVertex( index, x, y,  label);
			
			vIndex = cVertices.size();
			cVertices.add( cv);
			
			vertex.setIndex(vIndex) ;
			initialGraph.add( vertex );
			vertexMap.put( vIndex, vertex);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return vIndex ;
	}
	
	// ..........................................
	
	/**
	 * 
	 */
	public void connectByGraphIndex( int gRootIndex, int[] gIndices, double[] weights ){
		
		// , double weight
		// initialGraph.addEdge( v1, v2, 1.0 );
		// initialGraph.addEdge( v12, v7, 5.0 );
		// vertexMap.put( vIndex, vertex);
		
		int ix;
		Vertex v0, vi ;
		
		if ((gIndices.length==0) || (gIndices.length!=weights.length)){
			return;
		}
		
		try{
			v0 = (Vertex) vertexMap.get(gRootIndex);
			for (int i=0;i<gIndices.length;i++){
				ix = gIndices[i] ;
				
				if (vertexMap.containsKey(ix)){
					vi = (Vertex) vertexMap.get(ix);
					initialGraph.addEdge( v0, vi, weights[i] );
				}
			}
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		
	}
	
	/** just inserting an edge between 2 vertices */
	public void connectByPosition( int gRootIndex, int gIndex, double weight ){
		
		connectByGraphIndex( gRootIndex, new int[]{gIndex}, new double[] {weight} ) ;
		
	}



	public ArrayList<CVertex> getcVertices() {
		return cVertices;
	}



	public Map<Integer, Object> getVertexMap() {
		return vertexMap;
	}


}

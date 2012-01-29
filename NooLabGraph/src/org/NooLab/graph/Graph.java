package org.NooLab.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.NooLab.graph.commons.EdgeIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.WeightedGraphIntf;
import org.NooLab.graph.commons.WeightedGraph;
import org.NooLab.graph.define.GraphDefinition;

/**
 * 
 * 
 * This package is almost completely based on OpenJGraph !!
 * 
 * we just provided some small wrappers for our purposes, in order to
 * provide more direct access, e.g. for building a graph from points in xy plane
 * (performed by the GraphDefinition class)
 * 
 * 
 * 
 * TODO: conditions, dynamic weights, MultiDigester etc.
 * 
 */


public class Graph {

	String versionStr ="v0.92.001" ;
	
	WeightedGraphIntf	initialGraph = new WeightedGraph();
	WeightedGraphIntf	mst = null ;
	
	ArrayList<EdgeIntf> edges = new ArrayList<EdgeIntf>();
	 
    Map<Integer,Object> vertexMap = new HashMap<Integer,Object>();
	
    GraphDefinition definition = new GraphDefinition(initialGraph); 
    
    
	// ------------------------------------------------------------------------    
	public Graph(){
		
	}

		
	// ..........................................
	
	 
	public void deriveMinimumSpanningTree(){ 
		
		deriveMinimumSpanningTree(0,1);
	
	}
	
	public void deriveMinimumSpanningTree(int mstIndex){
		deriveMinimumSpanningTree(0,0);
	}
	
	@SuppressWarnings("unchecked")
	private void deriveMinimumSpanningTree(int mstIndex, int allInOne){
		// based on SampleMinimumSpanningTree
		 
		 
		 
		 
		 
		List<Vertex> mstVertices;
		
		 
		mst = initialGraph.minimumSpanningTree();
		
		mstVertices = mst.getConnectedSets() ; // 1. n ; 2.get(0) if just 1 subtree
 		
		if (allInOne>=1){
			edges = (ArrayList<EdgeIntf>) mst.getEdges();
		
		}else{
			// for spanning forests
			for (int i=0;i<mstVertices.size();i++){
				
				WeightedGraphIntf _mst = (WeightedGraphIntf) mstVertices.get(i) ;
				
				edges = (ArrayList<EdgeIntf>) _mst.getEdges();
				
			} // i->
		}
	
		// int n = edges.size();
		 
	} // getMinimumSpanningTree
	
	
	// ========================================================================

	
	public GraphDefinition getDefinition() {
		return definition;
	}


	public ArrayList<EdgeIntf> getEdges() {
		return edges;
	}


	public WeightedGraphIntf getInitialGraph() {
		return initialGraph;
	}


	public WeightedGraphIntf getMst() {
		return mst;
	}


	public ArrayList<CVertex> getcVertices() {
		return definition.getcVertices();
	}


	public Map<Integer, Object> getVertexMap() {
		return vertexMap;
	}


	public ArrayList<Vertex>  getVertices( int degree ){
		return null;
	}


	private PointXYIntf composePxyFromVertex( EdgeIntf edge, int p){
		
		PointXYIntf point=null;
		CVertex cv;
		Vertex v ;
		int ix;
		
		try{

			if (p==0){
				v = edge.getVertexA() ;
			}else{
				v = edge.getVertexB() ;
			}

			if (v!=null){
				ix = v.getIndex();
				
				if (ix<definition.getcVertices().size()){
		 			cv = definition.getcVertices().get(ix);
					point = new PXY(cv.x, cv.y);
				}
			}
		}catch(Exception e){
			
		}
		
		return point ;
	}
	
	public TreeLinesIntf translateTreeToLines() {
		
		// contains a list "items" that is defined as ArrayList<PPointXYIntf>   
		TreeLinesIntf treelines = new TreeLines();
		PointXYIntf pointA, pointB;
		EdgeIntf edge; 
		PPointXYIntf linePoints ;
		
		for (int k=0;k<edges.size();k++){
			edge = edges.get(k) ;
			
			pointA = composePxyFromVertex(edge, 0);
			pointB = composePxyFromVertex(edge, 1); 
			
			linePoints = new PPXY( new PointXYIntf[]{pointA, pointB} );
			treelines.addItem(linePoints) ;
		} 
		

		return treelines;
	}


	public String getVersionStr() {
		return versionStr;
	}
 
}



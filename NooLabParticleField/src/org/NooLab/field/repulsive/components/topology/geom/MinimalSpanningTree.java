package org.NooLab.field.repulsive.components.topology.geom;

import java.util.ArrayList;
 

import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.graph.Graph;
import org.NooLab.graph.PXY;
import org.NooLab.graph.PointXYIntf;
import org.NooLab.graph.TreeLinesIntf;



public class MinimalSpanningTree {

	Graph graph = new Graph();
	ArrayList<PointXYIntf> points = new ArrayList<PointXYIntf>(); 
	 
	TreeLinesIntf treeLines ; // they are of type  ArrayList<PointXYIntf[]> == a list of xy-pairs of values 
	
	 
	public MinimalSpanningTree( PointXY[] spoints ){
	
		takePoints( spoints) ;
		treeLines = calculateTree();
	}

	private void takePoints( PointXY[] spoints){
		
		PXY pXY;
		
		for (int i=0;i<spoints.length;i++){
			pXY = new PXY( spoints[i].x, spoints[i].y);
			
			points.add(pXY );
		}
		
		graph.getDefinition().addXYPointsAsVertices(points);
	}
	 
	private TreeLinesIntf calculateTree() {
		
		graph.deriveMinimumSpanningTree();
		return treeLines = graph.translateTreeToLines(); 
	}

	public ArrayList<PointXYIntf> getPoints() {
		return points;
	}

	public TreeLinesIntf getTreeLines() {
		return treeLines;
	}
	
}



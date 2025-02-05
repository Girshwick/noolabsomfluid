package examples;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.VertexImpl;
import org.NooLab.graph.commons.WeightedGraphIntf;
import org.NooLab.graph.commons.WeightedGraph;

/**
 * A sample application demonstrating the shortest path spanning tree algorithm.
 *
 * The weighted graph created here is taken from the book "Algorithms"
 * by Robert Sedgewick, 1988.
 *
 *
 * @author  Jesus M. Salvo Jr.
 */
public class SampleShortestPath {
  public static void main( String args[] ) throws Exception {

    WeightedGraphIntf	wgraph;
    Vertex	v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13;

    wgraph = new WeightedGraph();

    v1 = new VertexImpl( "A" );
    v2 = new VertexImpl( "B" );
    v3 = new VertexImpl( "C" );
    v4 = new VertexImpl( "D" );
    v5 = new VertexImpl( "E" );
    v6 = new VertexImpl( "F" );
    v7 = new VertexImpl( "G" );
    v8 = new VertexImpl( "H" );
    v9 = new VertexImpl( "I" );
    v10 = new VertexImpl( "J" );
    v11 = new VertexImpl( "K" );
    v12 = new VertexImpl( "L" );
    v13 = new VertexImpl( "M" );

    wgraph.add( v1 );
    wgraph.add( v2 );
    wgraph.add( v3 );
    wgraph.add( v4 );
    wgraph.add( v5 );
    wgraph.add( v6 );
    wgraph.add( v7 );
    wgraph.add( v8 );
    wgraph.add( v9 );
    wgraph.add( v10 );
    wgraph.add( v11 );
    wgraph.add( v12 );
    wgraph.add( v13 );

    wgraph.addEdge( v1, v2, 1.0 );
    wgraph.addEdge( v1, v6, 2.0 );
    wgraph.addEdge( v1, v7, 6.0 );
    wgraph.addEdge( v2, v3, 1.0 );
    wgraph.addEdge( v2, v4, 2.0 );
    wgraph.addEdge( v2, v5, 4.0 );
    wgraph.addEdge( v3, v5, 4.0 );
    wgraph.addEdge( v4, v5, 2.0 );
    wgraph.addEdge( v4, v6, 1.0 );
    wgraph.addEdge( v6, v5, 2.0 );
    wgraph.addEdge( v7, v5, 1.0 );
    wgraph.addEdge( v7, v8, 3.0 );
    wgraph.addEdge( v8, v9, 2.0 );
    wgraph.addEdge( v9, v11, 1.0 );
    wgraph.addEdge( v11, v10, 1.0 );
    wgraph.addEdge( v10, v12, 3.0 );
    wgraph.addEdge( v10, v13, 2.0 );
    wgraph.addEdge( v7, v10, 1.0 );
    wgraph.addEdge( v12, v13, 1.0 );
    wgraph.addEdge( v12, v7, 5.0 );
    wgraph.addEdge( v12, v5, 4.0 );
    wgraph.addEdge( v12, v6, 2.0 );

    System.out.println( "Weighted Graph:" );
    System.out.println( wgraph );
    System.out.println();
    System.out.println( "Shortest Path Spanning Tree for A: " + wgraph.shortestPath( v1 ));
    System.out.println( "Shortest Path Spanning Tree for G: " + wgraph.shortestPath( v7 ));
    System.out.println( "Shortest Path Spanning Tree for M: " + wgraph.shortestPath( v13 ));
  }
}

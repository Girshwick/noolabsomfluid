package org.NooLab.graph.commons;

/**
 * The factory for creating Vertices and Edges in a <tt>WeightedGraphImpl</tt> class.
 *
 * @author  Jesus M. Salvo jr.
 */

public class WeightedGraphFactory implements OpenJGraphFactory {

    public WeightedGraphFactory() {}

    public Vertex createVertex() {
        return new VertexImpl( "New Vertex" );
    }

    public EdgeIntf createEdge( Vertex v1, Vertex v2 ) {
        return new WeightedEdgeImpl( v1, v2, 0 );
    }
}
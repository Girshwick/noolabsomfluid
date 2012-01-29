package org.NooLab.graph.commons;

/**
 * The factory for creating Vertices and Edges in a <tt>GraphImpl</tt> class.
 *
 * @author  Jesus M. Salvo jr.
 */

public class TreeImplFactory implements OpenJGraphFactory {

    public TreeImplFactory() {}

    public Vertex createVertex() {
        return new VertexImpl( "New Vertex" );
    }

    public EdgeIntf createEdge( Vertex v1, Vertex v2 ) {
        return new Edge( v1, v2 );
    }
}
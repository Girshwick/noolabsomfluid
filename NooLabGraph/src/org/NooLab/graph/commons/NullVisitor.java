package org.NooLab.graph.commons;

/**
 * A visitor that always return true when visiting.
 *
 * @author  Jesus M. Salvo Jr.
 */

public class NullVisitor implements Visitor {

    public boolean visit( Vertex vertexToVisit ){
        return true;
    }
}


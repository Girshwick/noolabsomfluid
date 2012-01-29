package org.NooLab.graph.xml;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.EdgeIntf;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.visual.VisualGraph;

/**
 * @author  Jesus M. Salvo Jr.
 */

public class GraphToXMLEventGeneratorImpl implements GraphToXMLEventGenerator {

    ArrayList   handlers;


    public GraphToXMLEventGeneratorImpl() {
        this.handlers = new ArrayList();
    }

    public void addHandler( GraphToXMLHandler handlerToAdd ) {
        this.handlers.add( handlerToAdd );
    }

    public void removeHandler( GraphToXMLHandler handlerToRemove ) {
        this.handlers.remove( handlerToRemove );
    }

    /**
     * Serialize the Graph using the specified XMLSerializer
     */
    public void serialize( OpenJGraphIntf graph ) throws Exception {
        TreeSet     edgeSet = new TreeSet( new EdgeComparator() );
        Iterator    vertexIterator = graph.getVerticesIterator();
        Iterator    edgeIterator;
        Vertex      vertex;
        EdgeIntf        edge;

        this.notifyStartSerialize( graph );

        // Serialize the node elements
        while( vertexIterator.hasNext() ) {
            vertex = (Vertex) vertexIterator.next();
            this.notifySerializeVertex( vertex );

            // Store the edges of the vertices into the set,
            // guaranteeing uniqueness
            edgeSet.addAll( graph.getEdges( vertex ) );
        }

        // Serialize the edge elements
        edgeIterator = edgeSet.iterator();
        while( edgeIterator.hasNext() ) {
            edge = (EdgeIntf) edgeIterator.next();
            this.notifySerializeEdge( edge );
        }

        this.notifyEndSerialize();
    }

    public void serialize( VisualGraph vGraph ) throws Exception {
        OpenJGraphIntf       graph = vGraph.getGraph();
        TreeSet     edgeSet = new TreeSet( new EdgeComparator() );
        Iterator    vertexIterator = graph.getVerticesIterator();
        Iterator    edgeIterator;
        Vertex      vertex;
        EdgeIntf        edge;

        this.notifyStartSerialize( vGraph );

        // Serialize the node elements
        while( vertexIterator.hasNext() ) {
            vertex = (Vertex) vertexIterator.next();
            this.notifySerializeVertex( vertex );

            // Store the edges of the vertices into the set,
            // guaranteeing uniqueness
            edgeSet.addAll( graph.getEdges( vertex ) );
        }

        // Serialize the edge elements
        edgeIterator = edgeSet.iterator();
        while( edgeIterator.hasNext() ) {
            edge = (EdgeIntf) edgeIterator.next();
            this.notifySerializeEdge( edge );
        }

        this.notifyEndSerialize();
    }

    public void notifyStartSerialize( OpenJGraphIntf graph ) throws Exception {
        Iterator    iterator = this.handlers.iterator();
        GraphToXMLHandler   handler;

        while( iterator.hasNext()) {
            handler = (GraphToXMLHandler) iterator.next();
            handler.startSerialize( graph );
        }
    }

    public void notifyStartSerialize( VisualGraph vGraph ) throws Exception {
        Iterator    iterator = this.handlers.iterator();
        GraphToXMLHandler   handler;

        while( iterator.hasNext()) {
            handler = (GraphToXMLHandler) iterator.next();
            handler.startSerialize( vGraph );
        }
    }


    public void notifySerializeVertex( Vertex vertex ) throws Exception {
        Iterator    iterator = this.handlers.iterator();
        GraphToXMLHandler   handler;

        while( iterator.hasNext()) {
            handler = (GraphToXMLHandler) iterator.next();
            handler.serializeVertex( vertex );
            handler.endSerializeVertex( vertex );
        }
    }

    public void notifySerializeEdge( EdgeIntf edge ) throws Exception {
        Iterator    iterator = this.handlers.iterator();
        GraphToXMLHandler   handler;

        while( iterator.hasNext()) {
            handler = (GraphToXMLHandler) iterator.next();
            handler.serializeEdge( edge );
            handler.endSerializeEdge( edge );
        }
    }

    public void notifyEndSerialize() throws Exception {
        Iterator    iterator = this.handlers.iterator();
        GraphToXMLHandler   handler;

        while( iterator.hasNext()) {
            handler = (GraphToXMLHandler) iterator.next();
            handler.endSerialize();
        }
    }


}

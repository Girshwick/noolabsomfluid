package org.NooLab.graph.xml;

import org.w3c.dom.*;
import org.NooLab.graph.*;
import org.NooLab.graph.commons.EdgeIntf;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.visual.VisualGraph;
import org.apache.xml.serialize.XMLSerializer;
import java.io.*;

/**
 * An interface to serialize Graphs to XML.
 *
 * @author  Jesus M. Salvo Jr.
 */

public interface GraphToXMLEventGenerator {

    public void addHandler( GraphToXMLHandler handlerToAdd );

    public void removeHandler( GraphToXMLHandler handlerToRemove );

    /**
     * Method to be implemented by subclasses to serialize a Graph
     */
    public void serialize( OpenJGraphIntf graph ) throws Exception;

    public void serialize( VisualGraph vGraph ) throws Exception;

    public void notifyStartSerialize( OpenJGraphIntf graph ) throws Exception;

    public void notifyStartSerialize( VisualGraph vGraph ) throws Exception;

    public void notifySerializeVertex( Vertex vertex ) throws Exception;

    public void notifySerializeEdge( EdgeIntf edge ) throws Exception;

    public void notifyEndSerialize() throws Exception;
}

package examples;

import java.io.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.VertexImpl;
import org.NooLab.graph.commons.WeightedGraphIntf;
import org.NooLab.graph.commons.WeightedGraph;
import org.NooLab.graph.xml.*;
import org.apache.xml.serialize.XMLSerializer;

/**
 * A sample application demonstrating serialization of a Graph
 * to XGMML via DOM and SAX events.
 *
 * @author      Jesus M. Salvo Jr.
 */

public class SampleGraphXGMMLSerialization {

    GraphToXMLEventGenerator    eventGenerator;
    XGMMLReader         xgmmlReader;
    XMLSerializer       xmlSerializer;

    FileWriter          writer;
    FileReader          reader;

    public SampleGraphXGMMLSerialization( String fileName ) throws Exception {
        String  domFileName = fileName + "-dom";
        String  saxFileName = fileName + "-sax";

        System.out.println();
        System.out.println( "Serializing graph to " + fileName + "-dom via DOM..." );
        this.writer = new FileWriter( domFileName );
        this.xmlSerializer = new XMLSerializer();
        this.xmlSerializer.setOutputCharStream( this.writer );
        this.eventGenerator = new GraphToXMLEventGeneratorImpl();
        // Replace this DOM handler with a custom one if desired
        this.eventGenerator.addHandler( new GraphToXGMMLDOMHandler( this.eventGenerator, this.xmlSerializer ));
        this.eventGenerator.serialize( this.initGraph() );
        this.writer.flush();
        this.writer.close();

        System.out.println( "Reading XGMML that was serialized via DOM...." );
        this.reader = new FileReader( domFileName );
        // Replace this XGMMLContentHandler with a custom one if desired
        XMLToGraphHandler xmlHandler = new XGMMLContentHandler();
        XMLToGraphReader reader = new XMLToGraphReader( this.reader, xmlHandler );
        reader.parse();
        System.out.println( xmlHandler.getGraph() );

        System.out.println();
        System.out.println( "Serializing graph to " + fileName + "-sax via SAX..." );
        this.writer = new FileWriter( saxFileName );
        this.xmlSerializer = new XMLSerializer();
        this.xmlSerializer.setOutputCharStream( this.writer );
        this.eventGenerator = new GraphToXMLEventGeneratorImpl();
        // Replace this SAX handler with a custom one if desired
        this.eventGenerator.addHandler( new GraphToXGMMLSAXHandler( this.eventGenerator, this.xmlSerializer ));
        this.eventGenerator.serialize( this.initGraph() );
        this.writer.flush();
        this.writer.close();

        System.out.println( "Reading XGMML that was serialized via SAX...." );
        this.reader = new FileReader( saxFileName );
        // Replace this XGMMLContentHandler with a custom one if desired
        xmlHandler = new XGMMLContentHandler();
        reader = new XMLToGraphReader( this.reader, xmlHandler );
        reader.parse();
        System.out.println( xmlHandler.getGraph() );
    }

    private OpenJGraphIntf initGraph() throws Exception {
        WeightedGraphIntf   wgraph = new WeightedGraph();
        Vertex	v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13;

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

        return wgraph;
    }

    public static void main( String[] args ) throws Exception {
        if( args.length < 1 ) {
            System.out.println( "Usage: SampleGraphXGMMLSerialization { output.xml }" );
        }
        else {
            SampleGraphXGMMLSerialization app = new SampleGraphXGMMLSerialization( args[0] );
        }
    }
}

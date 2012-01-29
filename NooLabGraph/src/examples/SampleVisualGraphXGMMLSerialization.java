package examples;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.VertexImpl;
import org.NooLab.graph.commons.WeightedGraphIntf;
import org.NooLab.graph.commons.WeightedGraph;
import org.NooLab.graph.visual.*;
import org.NooLab.graph.visual.layout.*;
import org.NooLab.graph.xml.*;
import org.apache.xml.serialize.XMLSerializer;


/**
 * @author  Jesus M. Salvo Jr.
 */

public class SampleVisualGraphXGMMLSerialization extends JFrame {

    WeightedGraphIntf	wgraph;
    WeightedGraphIntf   subGraph;
    VisualGraph     vgraph;
    Vertex	v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13;

    public SampleVisualGraphXGMMLSerialization( String fileName ) throws Exception {
        GraphEditor         editor;
        GraphLayoutManager  layoutManager;

        // Initialise the weighted graph
        this.initGraph();

        // Get a VisualGraph
        editor = new GraphEditor();
        editor.setGraph( wgraph );
        vgraph = editor.getVisualGraph();

        // Create an instance of an alternate paitner for the edges
        AlternateVisualEdgePainter newEdgePainter = new AlternateVisualEdgePainter();

        // Use the alternate painter only for a subgraph of the weighted graph,
        // in particular for the minimum spanning tree of the weighted graph
        subGraph = wgraph.minimumSpanningTree();
        vgraph.emphasizeSubGraph( subGraph,
            null, null, null, newEdgePainter );

        System.out.println( "Weighted Graph:" );
        System.out.println( wgraph );
        System.out.println();

        System.out.println( "Minimum Spanning Tree: " );
        System.out.println( subGraph );

        // Force the layout of the vertices to be similar to that in the book
        this.forceLayout( vgraph );

        // Initialise a layout manager, though not really part of this eample
        layoutManager = new StraightLineLayout( editor.getVisualGraph() );
        editor.setGraphLayoutManager( layoutManager );

        // Make it all visible
        this.getContentPane().setLayout( new GridLayout(1,2));
        this.getContentPane().add( editor );

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = new Dimension( screenSize.width - 80, screenSize.height - 80 );

        this.setSize( frameSize );
        this.setLocation((int)(screenSize.getWidth() - frameSize.getWidth()) / 2, (int)(screenSize.getHeight() - frameSize.getHeight()) / 2);

        // Terminate the application when the window closes
        this.addWindowListener(
            new VisualGraphXGMMLSerializationWindowAdapter( this.vgraph, fileName ) );
    }

    private void initGraph() throws Exception {
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
    }

    private void forceLayout( VisualGraph vgraph ) {
        vgraph.getVisualVertex( v1 ).setLocation( 50, 50 );
        vgraph.getVisualVertex( v2 ).setLocation( 150, 150 );
        vgraph.getVisualVertex( v3 ).setLocation( 250, 150 );
        vgraph.getVisualVertex( v4 ).setLocation( 150, 250 );
        vgraph.getVisualVertex( v5 ).setLocation( 250, 250 );
        vgraph.getVisualVertex( v6 ).setLocation( 50, 350 );

        vgraph.getVisualVertex( v7 ).setLocation( 350, 150 );

        vgraph.getVisualVertex( v8 ).setLocation( 450, 50 );
        vgraph.getVisualVertex( v9 ).setLocation( 550, 50 );
        vgraph.getVisualVertex( v10 ).setLocation( 450, 250 );
        vgraph.getVisualVertex( v11 ).setLocation( 550, 250 );
        vgraph.getVisualVertex( v12 ).setLocation( 450, 350 );
        vgraph.getVisualVertex( v13 ).setLocation( 550, 350 );
    }

    public static void main( String[] args ) throws Exception {
        if( args.length < 1 ) {
            System.out.println( "Usage: SampleVisualGraphXGMMLSerialization { output.xml }" );
        }
        else {
            SampleVisualGraphXGMMLSerialization app = new SampleVisualGraphXGMMLSerialization( args[0] );
            app.setTitle( "Sample Visual Graph XGMML Serialization" );
            app.setVisible( true );
        }
    }

}


/**
 * WindowAdapter such that when the window closes, the VisualGraph is serialized to a file.
 */
class VisualGraphXGMMLSerializationWindowAdapter extends WindowAdapter {

    VisualGraph vGraph;
    String      fileName;

    GraphToXMLEventGenerator   generator;
    XGMMLReader         xgmmlReader;
    XMLSerializer       xmlSerializer;

    FileWriter          writer;
    FileReader          reader;

    public VisualGraphXGMMLSerializationWindowAdapter( VisualGraph vGraph, String fileName ) {
        this.vGraph = vGraph;
        this.fileName = fileName;
    }

    public void windowClosing( WindowEvent e ) {
        try {
            System.out.println();
            System.out.println( "Serializing visual graph to " + this.fileName + " via DOM..." );
            this.writer = new FileWriter( fileName );
            this.xmlSerializer = new XMLSerializer();
            this.xmlSerializer.setOutputCharStream( this.writer );
            // Replace this DOM serializer with a custom one if desired
            this.generator = new GraphToXMLEventGeneratorImpl();
            this.generator.addHandler( new GraphToXGMMLSAXHandler( this.generator, this.xmlSerializer ));
            this.generator.serialize( this.vGraph );
            this.writer.flush();
            this.writer.close();
        }
        catch( Exception ex ) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}

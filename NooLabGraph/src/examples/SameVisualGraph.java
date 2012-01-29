package examples;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.DirectedGraphImpl;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.VertexImpl;
import org.NooLab.graph.visual.*;
import org.NooLab.graph.visual.layout.*;


public class SameVisualGraph extends JFrame {
  Vertex        v1, v2, v3, v4;
  OpenJGraphIntf         graph;
  VisualGraph	vg1, vg2;
  GraphEditor	gedit1, gedit2;

  public SameVisualGraph() throws Exception {
    graph = new DirectedGraphImpl();
    vg1 = new VisualGraph();
    vg2 = new VisualGraph();
    gedit1 = new GraphEditor();
    gedit2 = new GraphEditor();

    v1 = new VertexImpl( "1" );
    v2 = new VertexImpl( "2" );
    v3 = new VertexImpl( "3" );
    v4 = new VertexImpl( "4" );

    graph.add( v1 );
    graph.add( v2 );
    graph.add( v3 );
    graph.add( v4 );

    graph.addEdge( v1, v2 );
    graph.addEdge( v2, v3 );
    graph.addEdge( v3, v4 );
    graph.addEdge( v4, v1 );

    vg1.setGraph( graph );
    vg2.setGraph( graph );
    gedit1.setVisualGraph( vg1 );
    gedit2.setVisualGraph( vg1 );

    this.getContentPane().setLayout( new GridLayout(1,2));
    this.getContentPane().add( gedit1 );
    this.getContentPane().add( gedit2 );

    vg1.setGraphLayoutManager( new StraightLineLayout( vg1 ) );

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
      });

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = new Dimension( screenSize.width - 80, screenSize.height - 80 );

    this.setSize( frameSize );
    this.setLocation((int)(screenSize.getWidth() - frameSize.getWidth()) / 2, (int)(screenSize.getHeight() - frameSize.getHeight()) / 2);
  }

  public static void main(String[] args) throws Exception {
    SameVisualGraph frame = new SameVisualGraph();
    frame.setTitle("SameVisualGraph");
    frame.setVisible(true);
  }
}



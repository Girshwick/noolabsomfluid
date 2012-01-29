package org.NooLab.graph.visual;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.visual.layout.*;
import org.apache.log4j.Category;

/**
 * GraphEditor encapsulates GraphScrollPane so that a toolbar is provided
 * for the user to add and remove vertices and edges dynamically.
 *
 * @author		Jesus M. Salvo Jr.
 */
public class GraphEditor extends JPanel {
  /**
    * The LayoutManager used by GraphEditor is by default BorderLayout.
    */
  BorderLayout			borderlayout;

  /**
    * The object representing the toolbar used by GraphEditor.
    */
  GraphToolBar			toolbar;

  /**
    * The GraphScrollPane object encapsulated by GraphEditor.
    */
  GraphScrollPane		graphscrollpane;

  JSplitPane        splitpane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );

  /**
    * Creates a GraphEditor object without a specified GraphScrollPane
    * object to draw.
    */
  public GraphEditor() {
    super();
    this.graphscrollpane = new GraphScrollPane();
    this.borderlayout = new BorderLayout();
    this.toolbar = new GraphToolBar( this );

    doGraphEditorLayout();
  }

  /**
    * Creates a GraphEditor object with a given GraphScrollPane
    * object to draw.
    *
    * @param		graphscrollpane	GraphScrollPane object whose vertices and edges will be drawn.
    */
  public GraphEditor( GraphScrollPane graphscrollpane ) {
    super();
    this.graphscrollpane = graphscrollpane;
    this.borderlayout = new BorderLayout();
    this.toolbar = new GraphToolBar( this );

    doGraphEditorLayout();
  }

  /**
    * Creates a GraphEditor object with a given VisualGraph to draw
    *
    * @param		vgraph	VisualGraph object whose vertices and edges will be drawn.
    */
  public GraphEditor( VisualGraph vgraph ) {
    super();
    this.graphscrollpane = new GraphScrollPane();
    this.graphscrollpane.setVisualGraph( vgraph );
    this.borderlayout = new BorderLayout();
    this.toolbar = new GraphToolBar( this );

    doGraphEditorLayout();
  }

  /**
    * Creates a GraphEditor object with a given Graph
    *
    * @param		graph		Graph object whose vertices and edges will be drawn.
    */
  public GraphEditor( OpenJGraphIntf graph ) {
    super();

    VisualGraph		vgraph;

    this.graphscrollpane = new GraphScrollPane();
    vgraph = new VisualGraph( );
    vgraph.setGraph( graph );
    this.graphscrollpane.setVisualGraph( vgraph );
    this.borderlayout = new BorderLayout();
    this.toolbar = new GraphToolBar( this );

    doGraphEditorLayout();
  }

  /**
    * Lays out the components so that the toolbar is on the top. Do not
    * call this method more than once.
    */
  private void doGraphEditorLayout() {
    this.splitpane.setRightComponent( this.graphscrollpane );
    this.splitpane.setOneTouchExpandable( true );

    this.setLayout( this.borderlayout );

    this.add( this.toolbar, BorderLayout.NORTH );
    this.add( this.splitpane );
  }

  public void setLeftPanel( Component component ) {
    this.splitpane.setLeftComponent( component );
  }

  /**
    * Returns the Graph object that is encapsulated in GraphEditor.
    *
    * @return	Graph object encapsulated by GraphEditor.
    */
  public OpenJGraphIntf getGraph(){
    return this.graphscrollpane.getVisualGraph().getGraph();
  }

  /**
    * Returns the VisualGraph object that is encapsulated in GraphEditor.
    *
    * @return	VisualGraph object encapsulated by GraphEditor.
    */
  public VisualGraph getVisualGraph(){
    return this.graphscrollpane.getVisualGraph();
  }

  /**
    * Sets the new VisualGraph object that is encapsulated by GraphEditor.
    *
    * @param		vg		VisualGraph object that will be encapsulated by GraphEditor
    */
  public void setVisualGraph( VisualGraph vg ){
    this.graphscrollpane.setVisualGraph( vg );
  }

  /**
    * Sets the new Graph object that is encapsulated by GraphEditor.
    *
    * @param		graph		Graph object that will be encapsulated by GraphEditor
    */
  public void setGraph( OpenJGraphIntf graph ){
    this.graphscrollpane.getVisualGraph().setGraph( graph );
  }

  public void paint( Graphics g ) {
    super.paint( g );
  }

  /**
    * Sets the mode of operation to NORMAL_MODE. This is simply a wrapper
    * method around GraphScrollPane's setNormalMode() method
    *
    * @see		org.NooLab.graph.visual.GraphScrollPane#setNormalMode()
   */
  protected void setNormalMode() {
    this.graphscrollpane.setNormalMode();
    //this.graphscrollpane.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ));
  }

  /**
    * Sets the mode of operation to VERTEX_MODE. This is simply a wrapper
    * method around GraphScrollPane's setVertexMode() method
    *
    * @see		org.NooLab.graph.visual.GraphScrollPane#setVertexMode()
   */
  protected void setVertexMode() {
    this.graphscrollpane.setVertexMode();
    //this.graphscrollpane.setCursor( new Cursor( Cursor.HAND_CURSOR ));
  }

  /**
    * Sets the mode of operation to EDGE_MODE. This is simply a wrapper
    * method around GraphScrollPane's setEdgeMode() method
    *
    * @see		org.NooLab.graph.visual.GraphScrollPane#setEdgeMode()
   */
  protected void setEdgeMode() {
    this.graphscrollpane.setEdgeMode();
    //this.graphscrollpane.setCursor( new Cursor( Cursor.CROSSHAIR_CURSOR ));
  }

  /**
    * Sets the layout manager to use to layout the vertices of the graph.
    *
    * @param  layoutmanager   An object implementing the GraphLayoutManager interface.
    */
  public void setGraphLayoutManager( GraphLayoutManager layoutmanager ) {
    this.graphscrollpane.setGraphLayoutManager( layoutmanager );
  }

}



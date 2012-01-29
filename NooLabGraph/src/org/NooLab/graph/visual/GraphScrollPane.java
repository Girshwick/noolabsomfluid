package org.NooLab.graph.visual;

import javax.swing.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.visual.layout.*;

import java.awt.*;

/**
 * GraphScrollPane encapsulates GraphPanelSizeable so that the
 * visual representation of the graph has scrollbars to allow the user
 * to view other portions of the graph that are not in the direct view of
 * the viewport of GraphScrollPane.
 *
 * @author		Jesus M. Salvo Jr.
 */
public class GraphScrollPane extends JScrollPane {
  /**
   * The GraphPanel that GraphScrollPane encapsulates
   */
  GraphPanel	gpanel;

  /**
   * Creates a GraphScrollPane object and initializes the scrollbars and the
   * GraphPanelSizeable which it encapsulates. As a note,
   * the scorllbars only appear if the policy rules are set to ***_ALWAYS
   * in the call to the JScrollPane's constructor method. The scrollbars
   * would not appear if we call the constructor method with the policy
   * rules set to other than ***_ALWAYS, even if we subsequently set
   * the policy rules to ***_ALWAYS later on.
   *
   */
  public GraphScrollPane() {
    super(
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
    this.gpanel = new GraphPanel();
    this.gpanel.gpcontainer = this;
    this.GraphScrollPane_init();
  }

  private void GraphScrollPane_init(){
    JViewport		viewport;

    viewport = new JViewport( );
    viewport.setView( this.gpanel );
    this.setViewport( viewport );
  }

  /**
   * Returns the VisualGraph that is encapsulated within
   * GraphScrollPane.GraphPanelSizeable. This is simply
   * a wrapper method around gpanel's getVisualGraph() method
   *
   * @return	This VisualGraph that is encapsulated.
   */
  public VisualGraph getVisualGraph() {
    return this.gpanel.getVisualGraph();
  }

  /**
    * Sets the VisualGraph that is encapsulated within
    * GraphScrollPane.GraphPanelSizeable. Calling this will
    * automatically repaint the contents of the pane. This is simply
    * a wrapper method around gpanel's setVisualGraph() method.
    *
    * @param vg	The visual graph to be encapsulated and drawn.
   */
  public void setVisualGraph( VisualGraph vg ) {
    this.gpanel.setVisualGraph( vg, this );
  }

  /**
    * Sets the Graph that is encapsulated within
    * GraphScrollPane.GraphPanelSizeable. Calling this will
    * automatically repaint the contents of the pane. This is simply
    * a wrapper method around gpanel's setGraph() method.
    *
    * @param g	The graph to be encapsulated and drawn.
   */
  public void setGraph( OpenJGraphIntf g ) {
    this.gpanel.setGraph( g, this );
  }

  /**
    * Sets the mode of operation to NORMAL_MODE. This is simply a wrapper
    * method around gpanel's setNormalMode() method
    *
   */
  public void setNormalMode() {
    this.gpanel.setNormalMode();
  }

  /**
    * Sets the mode of operation to VERTEX_MODE. This is simply a wrapper
    * method around gpanel's setVertexMode() method
    *
   */
  public void setVertexMode() {
    this.gpanel.setVertexMode();
  }

  /**
    * Sets the mode of operation to EDGE_MODE. This is simply a wrapper
    * method around gpanel's setEdgeMode() method
    *
   */
  public void setEdgeMode() {
    this.gpanel.setEdgeMode();
  }

  /**
    * Sets the layout manager to use to layout the vertices of the graph.
    *
    * @param  layoutmanager   An object implementing the GraphLayoutManager interface.
    */
  public void setGraphLayoutManager( GraphLayoutManager layoutmanager ) {
    this.gpanel.setGraphLayoutManager( layoutmanager );
  }

}


package org.NooLab.graph.visual;

import java.awt.event.*;
import java.awt.*;
import java.io.*;

/**
 * Abstract state object of GraphPanel, applying the state design pattern.
 *
 * @author  Jesus M. Salvo Jr.
 */

public abstract class GraphPanelState implements Serializable {
  /**
   * The GraphPanel object that has the specified state.
   */
  GraphPanel  gpanel;

  /**
   * Creates a GraphPanelState object for the specified GraphPanel object.
   */
  GraphPanelState( GraphPanel gpanel ) {
    this.gpanel = gpanel;
  }

  public abstract void mousePressed( MouseEvent e );
  public abstract void mouseReleased( MouseEvent e );
  public abstract void mouseDragged( MouseEvent e );
  public abstract void mouseEntered( MouseEvent e );
  public abstract void mouseExited( MouseEvent e );
  public abstract void mouseClicked( MouseEvent e );
  public abstract void mouseMoved( MouseEvent e );
  public abstract void paint( Graphics2D ged );
}


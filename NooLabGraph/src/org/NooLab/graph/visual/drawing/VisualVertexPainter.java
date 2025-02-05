package org.NooLab.graph.visual.drawing;

import java.awt.*;
import java.awt.geom.*;

import org.NooLab.graph.*;
import org.NooLab.graph.visual.*;

/**
 * An interface for drawing a <tt>VisualVertex</tt>.
 *
 * @author Jesus M. Salvo Jr.
 */

public interface VisualVertexPainter extends Painter {

  /**
   * Paints the outline of the <tt>VisualVertex</tt>
   */
  public void paintOutline( VisualGraphComponent component, Graphics2D g2d );

  /**
   * Paints the <tt>VisualVertex</tt>'s fill color.
   */
  public void paintFill( VisualGraphComponent component, Graphics2D g2d );

  /**
   * Paints the text of the <tt>VisualVertex</tt>.
   */
  public void paintText( VisualGraphComponent component, Graphics2D g2d );

  /**
   * Rescales the drawing based on either the font or text of
   * the <tt>VisualVertex</tt> being changed. Thus, this method is
   * usually called when an end-user changes such properties.
   */
  public void rescale( VisualVertex vv );
}
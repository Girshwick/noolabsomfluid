package org.NooLab.graph.visual.drawing;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.NooLab.graph.*;
import org.NooLab.graph.visual.*;

/**
 * An implementation of <tt>VisualVertexPainter</tt> that draws
 * the <tt>VisualVertex</tt> based on its attributes.
 *
 * @author Jesus M. Salvo Jr.
 */

public class VisualVertexPainterImpl implements VisualVertexPainter {

  /**
    * Distance from the top and bottom edge of the bounds VisualVertex to the ascent
    * of the first line or the descent of the last line, respectively.
    */
  private double  topBottomMargin = 5;

  /**
    * Distance from the top and bottom edge of the bounds of the VisualVertex to the
    * of the first character and the last character of each line, respectively.
    */
  private double  leftRightMargin = 5;

  /**
   * FontMetrics used by the Painter so that we do not always create an instance
   * of Panel() just to get a FontMetrics
   */
  private transient FontMetrics fontMetrics;

  /**
   * Reference to a newline so that we do not create this String all the time.
   */
  private static final String newLine = "\n";


  public VisualVertexPainterImpl() {}

  /**
   * Draw the <tt>VisualVertex</tt> with the specified 2D graphics context.
   * Each call to this method will draw the fill color, the outline color,
   * and the string inside the shape, in that order.
   * <p>
   * The contract is that calling this <tt>paint</tt> method will call
   * the other methods in this interface.
   *
   * @param vv      The <tt>VisualVertex</tt> to be painted.
   * @param	g2d		The Graphics2D graphics context object used to draw
   * the VisualVertex.
   */
  public void paint( VisualGraphComponent vg, Graphics2D g2d ) {

    // Draw the node's fill color
    this.paintFill( vg, g2d );

    // Draw the node's outline
    this.paintOutline( vg, g2d );

    // Finally, paint the node's display text to describe the node
    this.paintText( vg, g2d );
  }

  /**
   * Paints the outline of the <tt>VisualVertex</tt>
   */
  public void paintOutline( VisualGraphComponent component, Graphics2D g2d ) {
    VisualVertex    vv = ( VisualVertex ) component;
    g2d.setColor( vv.getOutlinecolor() );
    g2d.draw( vv.getGeneralPath() );
  }

  /**
   * Paints the <tt>VisualVertex</tt>'s fill color.
   */
  public void paintFill( VisualGraphComponent component, Graphics2D g2d ) {
    VisualVertex    vv = ( VisualVertex ) component;
    g2d.setColor( vv.getFillcolor() );
    g2d.fill( vv.getGeneralPath() );
  }

  /**
   * Paints the text of the <tt>VisualVertex</tt>.
   */
  public void paintText( VisualGraphComponent component, Graphics2D g2d )
  {
    VisualVertex      vv = ( VisualVertex ) component;
    StringTokenizer   strTokenizer;
    int               line = 1;
    int               lineHeight;
    Rectangle2D.Float bounds;

    if( this.fontMetrics == null ) {
        this.fontMetrics = new Panel().getFontMetrics( vv.getFont() );
    }
    lineHeight = this.fontMetrics.getHeight();

    bounds = (Rectangle2D.Float) vv.getGeneralPath().getBounds2D();

    g2d.setFont( vv.getFont() );
    g2d.setColor( vv.getFontcolor() );
    strTokenizer = new StringTokenizer( vv.getText(), VisualVertexPainterImpl.newLine );
    while( strTokenizer.hasMoreTokens() ) {
      g2d.drawString( strTokenizer.nextToken(),
        (float)( bounds.x + this.leftRightMargin + 1 ),
        (float)( bounds.y + this.topBottomMargin + lineHeight * line - 2 ));
      line++;
    }
  }

  /**
   * Rescales the VisualVertex. It determines the height of the text to be painted
   * and adjusts the size of the GeneralPath so that the entire text fits in
   * the VisualVertex.
   */
  public void rescale( VisualVertex vv) {
    GeneralPath       drawPath = vv.getGeneralPath();
    StringTokenizer   strTokenizer;
    AffineTransform   transform = new AffineTransform();
    Rectangle2D       originalLocation;
    double            scalex, scaley;
    int               lineHeight;
    int               height = 0, width, maxWidth = 0;

    if( this.fontMetrics == null ) {
        this.fontMetrics = new Panel().getFontMetrics( vv.getFont() );
    }
    lineHeight = this.fontMetrics.getHeight();

    // Since there is no setSize() method (or something similar)
    // for the class GeneralPath, we will transform the shape by
    // scaling it. Because scaling will update the origin of the
    // GeneralPath, we need to save the original location before proceeding.
    originalLocation = drawPath.getBounds2D();
    strTokenizer = new StringTokenizer( vv.getText(), VisualVertexPainterImpl.newLine );
    while( strTokenizer.hasMoreTokens() ) {
      height += lineHeight;
      width = fontMetrics.stringWidth( strTokenizer.nextToken() );
      maxWidth = width > maxWidth ? width : maxWidth;
    }

    // Now scale the GeneralPath, effectively "resizing" it.
    scalex = ( maxWidth + this.leftRightMargin * 2 ) / drawPath.getBounds2D().getWidth();
    scaley = ( height + this.topBottomMargin * 2 ) / drawPath.getBounds2D().getHeight();
    transform.scale( scalex, scaley );

    // We have to draw the GeneralPath before the scaling takes effect.
    drawPath.transform( transform );

    // Set the shape back to its original location.
    // setToTranslation() is used to remove the transformation or scaling.
    // Otherwise, the shape will be scaled twice.
    transform.setToTranslation(
      originalLocation.getMinX() - drawPath.getBounds2D().getMinX(),
      originalLocation.getMinY() - drawPath.getBounds2D().getMinY());

    // Draw again.
    drawPath.transform( transform );
  }
}
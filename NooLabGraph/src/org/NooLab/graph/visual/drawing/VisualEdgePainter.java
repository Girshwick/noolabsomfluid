package org.NooLab.graph.visual.drawing;

import java.awt.*;

/**
 * An interface for drawing a <tt>VisualEdge</tt>.
 *
 * @author Jesus M. Salvo Jr.
 */

public interface VisualEdgePainter extends Painter {

    /**
     * Paints the text of the <tt>VisualEdge</tt>.
     */
    public void paintText( Graphics2D g2d, Font font, Color fontColor,
        String text, float x, float y );

}
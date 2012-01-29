package org.NooLab.graph.visual.drawing;

import java.awt.*;
import java.awt.geom.*;

import org.NooLab.geom.*;
import org.NooLab.graph.*;
import org.NooLab.graph.visual.*;

/**
 * A weak implementation of the VisualWeightedEdgePainter interface.
 *
 * @author Jesus M. Salvo Jr.
 */

class VisualWeightedEdgePainterWeakImpl implements VisualWeightedEdgePainter {

    public VisualWeightedEdgePainterWeakImpl() {}

    /**
     * Empty method implemetation that does nothing. This method should never
     * be called or delegated to for whatever reason.
     */
    public void paint( VisualGraphComponent component, Graphics2D g2d ) {}

    /**
     * Empty method implemetation that does nothing. This method should never
     * be called or delegated to for whatever reason.
     */
    public void paintText( Graphics2D g2d, Font font, Color fontColor,
        String text, float x, float y ) {}

}
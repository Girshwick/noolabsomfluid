package org.NooLab.graph.visual.drawing;

import java.io.Serializable;

import org.NooLab.graph.visual.VisualGraphComponent;

/**
 * An interface defining a factory that returns a <tt>Painter</tt>
 * for a specified <tt>VisualGraphComponent</tt>.
 *
 * @author  Jesus M. Salvo Jr.
 */

public interface PainterFactory extends Serializable {
    public Painter getPainter( VisualGraphComponent component );
}
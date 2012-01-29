package org.NooLab.graph.visual;

import java.awt.event.*;
import java.awt.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.Vertex;

/**
 * State object that represents the vertex mode in a GraphPanel.
 * Vertex mode being the ability to interactively add a vertex into a graph.
 *
 * @author  Jesus M. Salvo Jr.
 */
public class GraphPanelVertexState extends GraphPanelState {

    /**
     * Creates a GraphPanelVertexState object for the specified GraphPanel object.
     */
    public GraphPanelVertexState( GraphPanel gpanel ) {
        super( gpanel );
    }

    /**
     * Creates a new vertex on the specified coordinate.
     */
    public void mousePressed( MouseEvent e ) {
        // Create a new vertex and set its location
        // to the coordinates of the mouse
        VisualGraph         vg;
        Vertex              newvertex;

        // Create a new vertex
        vg = gpanel.getVisualGraph();
        newvertex = vg.getGraph().getGraphFactory().createVertex();

        // Add the vertex to the graph
        try {
            vg.add( newvertex );
        }
        catch( Exception ex ) {
            ex.printStackTrace();
        }

        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        // Do this before adding the new vertex onto the graph
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseEvent( e );
        };

        // Set the location of the visual representation of the vertex
        // to the coordinates of the mouse
        vg.getVisualVertex( newvertex ).setLocation( e.getX(), e.getY());
    }

    /**
     * Do nothing for this state.
     */
    public void mouseDragged( MouseEvent e ) {
        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseMotionEvent( e );
        };
    }

    /**
     * Do nothing for this state.
     */
    public void mouseReleased( MouseEvent e ) {
        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseEvent( e );
        };
    }

    /**
     * Notifies the VisualGraphComponent, if any, of the mouse event.
     */
    public void mouseEntered( MouseEvent e ) {
        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseEvent( e );
        };
    }

    /**
     * Notifies the VisualGraphComponent, if any, of the mouse event.
     */
    public void mouseExited( MouseEvent e ) {
        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseEvent( e );
        };
    }

    /**
     * Notifies the VisualGraphComponent, if any, of the mouse event.
     */
    public void mouseClicked( MouseEvent e ) {
        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseEvent( e );
        };
    }

    /**
     * Notifies the VisualGraphComponent, if any, of the mouse event.
     */
    public void mouseMoved( MouseEvent e ) {
        VisualGraphComponent    vVertex = this.gpanel.getVisualGraph().getNode( e.getX(), e.getY() );
        VisualGraphComponent    vEdge = this.gpanel.getVisualGraph().getVisualEdge( e.getX(), e.getY() );
        VisualGraphComponent    component;

        // Notify the VisualGraphComponent of the event
        component = vVertex != null ? vVertex :
            ( vEdge != null ? vEdge : null );
        if ( component != null ) {
            component.processMouseMotionEvent( e );
        };
    }

    /**
     * Just call VisualGraph().paint()
     */
    public void paint( Graphics2D g2d ){
        this.gpanel.getVisualGraph().paint( g2d );
    }
}


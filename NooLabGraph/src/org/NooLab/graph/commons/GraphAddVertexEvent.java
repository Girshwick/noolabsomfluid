package org.NooLab.graph.commons;

import java.util.*;

/**
 * This event is used to notify interested parties that a Vertex object
 * has been added to a Graph object.
 *
 * @author		Jesus M. Salvo Jr.
 */
public class GraphAddVertexEvent extends EventObject {
  /**
    * The Vertex object that was added to a Graph object
    */
  Vertex	vertex;

  /**
    * Creates a GraphAddVertexEvent object indicating the source of the event
    * and the Vertex that was added to a Graph object
    *
    * @param	source		source of the event. This is usually the Graph object
    * where the Vertex object was added.
    * @param	newvertex	Vertex object that was added to a Graph object
    */
  public GraphAddVertexEvent( Object source, Vertex newvertex ) {
    super( source );
    this.vertex = newvertex;
  }

  /**
    * Returns the Vertex object that was added to a Graph object
    *
    * @return		The Vertex object added
    */
  public Vertex getVertex( ) {
    return this.vertex;
  }
}

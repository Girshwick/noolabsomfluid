package org.NooLab.graph.commons;

import java.util.*;

/**
 * This event is used to notify interested parties that an Edge object
 * has been added to a Graph object.
 *
 * @author		Jesus M. Salvo Jr.
 */
public class GraphAddEdgeEvent extends EventObject {
  /**
    * The Edge object that was added to a Graph object
    */
  EdgeIntf	edge;

  /**
    * Creates a GraphAddEdgeEvent object indicating the source of the event
    * and the Edge that was added to a Graph object
    *
    * @param	source		source of the event. This is usually the Graph object
    * where the Edge object was added.
    * @param	newedge		Edge object that was added to a Graph object
    */
  public GraphAddEdgeEvent( Object source, EdgeIntf newedge ) {
    super( source );
    this.edge = newedge;
  }

  /**
    * Returns the Edge object that was added to a Graph object
    *
    * @return		The Edge object added
    */
  public EdgeIntf getEdge( ) {
    return this.edge;
  }
}

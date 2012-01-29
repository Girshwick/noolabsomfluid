package org.NooLab.graph.commons;

/**
 * Represents a directed edge in a graph.
 *
 * @author		Jesus M. Salvo Jr.
 */
public class DirectedEdgeImpl extends Edge implements DirectedEdge {

  /**
   * The direction of the edge. The implementation of the DirectedEdge
   * interface is delegated to this object.
   */
  DirectedEdgeWeakImpl    directionOfEdge;

  /**
    * Creates an DirectedEdgeImpl object whose origin and destination vertices
    * are specified by the method parameters.
    *
    * @see		Vertex
    */
  public DirectedEdgeImpl( Vertex sourceVertex, Vertex sinkVertex ){
    super( sourceVertex, sinkVertex );
    directionOfEdge = new DirectedEdgeWeakImpl( sourceVertex, sinkVertex );
  }

  /**
   * Returns the source Vertex of the edge.
   *
   * @return  The source Vertex.
   */
  public Vertex getSource() {
    return this.directionOfEdge.getSource();
  }

  /**
   * Returns the sink Vertex of the edge.
   *
   * @return  The sink Vertex.
   */
  public Vertex getSink() {
    return this.directionOfEdge.getSink();
  }

  /**
   * Returns the direction of the Edge
   *
   */
  public int getDirection() {
    return this.directionOfEdge.getDirection();
  }
  
  /**
    * Returns a String representation of the Edge, using the
    * the toString() methods of Vertex.
    *
    * @return	The String representation of the Edge
    * @see		Vertex
    */
  public String toString(){
    return this.directionOfEdge.getSource().toString() + "->" + this.directionOfEdge.getSink().toString();
  }

  /**
    * Creates a clone of this Edge. This calls the Edge constructor,
    * thereby creating a new instance of Edge. However, the vertices
    * in both endpoints of the Edge are not cloned.
    *
    * @return  A clone of an instance of Edge. 	
    */
  protected Object clone(){
    return new DirectedEdgeImpl( vertexA, vertexB );
  }

}


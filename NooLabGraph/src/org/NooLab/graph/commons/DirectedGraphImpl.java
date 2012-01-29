package org.NooLab.graph.commons;

import java.util.*;

import org.NooLab.graph.algorithm.*;

/**
 * A directed Graph where edges have a specified direction. Edges in this
 * graph are therefore instances of DirectedEdge.
 *
 * @author		Jesus M. Salvo Jr.
 */

public class DirectedGraphImpl extends OpenJGraph implements DirectedGraph {
  /**
   * Delegate object to handle the implementation of the DirectedGraph interface.
   */
  DirectedGraphWeakImpl   graphDirectionDelegate;

  /**
    * Creates a new instance of an empty directed Graph. The default
    * GraphTraversal object is an instance of DepthFirstDirectedGraphTraversal,
    * a depth-first traversal respecting the direction of edges.
    */
  public DirectedGraphImpl( ){
    super();
    this.factory = new DirectedGraphImplFactory();
    this.traversal = new DepthFirstDirectedGraphTraversal( this );
    this.graphDirectionDelegate = new DirectedGraphWeakImpl( this );
  }

  /**
    * Returns the outgoing EdgeSets of the Graph. Each element in the return List
    * is of type EdgeSet. The index of a Vertex in the vertices List
    * matches the Vertex's EdgeSet index in the outgoingEdges List.
    *
    * @return List containing the outgoing EdgeSets in the Graph. This simply
    * returns this.adjacentEdges.
    */
  protected List getOutgoingEdges() {
    return this.graphDirectionDelegate.getOutgoingEdges();
  }

  /**
    * Returns the incoming EdgeSets of the Graph. Each element in the return List
    * is of type EdgeSet. The index of a Vertex in the vertices List
    * matches the Vertex's EdgeSet index in the incomingEdges List.
    *
    * @return	List containing the incoming EdgeSets in the Graph. This simply
    * returns this.adjacentEdges.
    */
  protected List getIncomingEdges() {
    return this.graphDirectionDelegate.getIncomingEdges();
  }

  /**
    * Returns the outgoing edges of a particular Vertex in the Graph.
    *
    * @param		v		Vertex you want to determine its outgoing edges.
    * @return	  List of outgoing edges of the specified Vertex.
    */
  public List getOutgoingEdges( Vertex v ) {
    return this.graphDirectionDelegate.getOutgoingEdges( v );
  }

  /**
    * Returns the incoming edges of a particular Vertex in the Graph.
    *
    * @param		v		Vertex you want to determine its incoming edges.
    * @return	  List of incoming edges of the specified Vertex.
    */
  public List getIncomingEdges( Vertex v ) {
    return this.graphDirectionDelegate.getIncomingEdges( v );
  }

  /**
    * Returns the vertices that are adjacent to a specified Vertex
    * where the Edge is outgoing from the specified Vertex to the adjacent vertex.
    *
    * @param		v		Vertex you want to determine its outgoing adjacent vertices.
    * @return	  List of outgoing vertices adjacent to the specified Vertex.
    */
  public List getOutgoingAdjacentVertices( Vertex v ) {
    return this.graphDirectionDelegate.getOutgoingAdjacentVertices( v );
  }

  /**
    * Returns the vertices that are adjacent to a specified Vertex
    * where the Edge is incoming from the specified Vertex to the adjacent vertex.
    *
    * @param		v		Vertex you want to determine its incoming adjacent vertices.
    * @return	  List of incoming vertices adjacent to the specified Vertex.
    */
  public List getIncomingAdjacentVertices( Vertex v ) {
    return this.graphDirectionDelegate.getIncomingAdjacentVertices( v );
  }

  /**
    * Returns an Edge in the Graph whose origin is fromVertex and destination is toVertex.
    * If there is more than one Edge that has the same origin and destination in the Graph,
    * the first matching Edge is returned.
    *
    * @param		fromVertex		Vertex that is the origin of the directed Edge
    * @param		toVertex		Vertex that is the destination of the directed Edge
    * @return	Edge whose origin is fromVertex and destination is toVertex
    * @see			org.NooLab.graph.commons.EdgeIntf
    */
  public DirectedEdge getEdge( Vertex fromvertex, Vertex tovertex ) {
    return this.graphDirectionDelegate.getEdge( fromvertex, tovertex );
  }

  /**
    * Adds a Vertex into the Graph. This will also create a new entry
    * in the edges List and add the newly added Vertex to its own
    * connected set, thereby adding a new List in the connectedSet List.
    * Finally, all GraphAddVertexListeners are informed of the event that a
    * Vertex has been added to the Graph.
    *
    * @param  newvertex   The Vertex object to be added to the graph.
    */
  public void add( Vertex newvertex ) throws Exception {
    // Whenever a new vertex is added, we also need to create
    // a blank adjacenct edge list for the new vertex
    this.graphDirectionDelegate.add( newvertex );

    // Call the ancestor add() so that listeners are notified.
    super.add( newvertex );
  }

  /**
   * Factory method implementation that creates an instance of a DirectedEdge.
   * This is now merely a shortcut to <tt>factory.crateEdge()</tt>.
   *
   * @param   v1    One endpoint of the vertex
   * @param   v2    The other endpoint of the vertex
   */
  public EdgeIntf createEdge( Vertex v1, Vertex v2 ) {
    return this.factory.createEdge( v1, v2 );
  }

  /**
    * Adds an Edge into the Graph. This first creates a new instance of Edge
    * before calling the overloaded method addEdge( Edge ).
    *
    * @param		fromVertex		Vertex that will be the source of the Edge
    * @param		toVertex		  Vertex that will be the sink of the Edge
    * @return   The Edge object that was created and added to the Graph.
    */
  public EdgeIntf addEdge( Vertex v1, Vertex v2 ) throws Exception{
    EdgeIntf        edge;

    // Let the ancestor create and add the edge
    edge = super.addEdge( v1, v2 );

    // Add the edge as outgoing edge v1 and an incoming edge of v2
    this.graphDirectionDelegate.addEdge( (DirectedEdge) edge );

    return edge;
  }

  /**
    * Adds an Edge into the Graph. The vertices of the Edge need not be
    * existing in the Graph for this method to work properly.
    * The vertices in both ends of the Edge are merged into one connected set,
    * thereby possibly decreasing the number of Lists in the coonectedSet List.
    * Finally, all GraphAddEdgeListeners are informed of the event that a
    * Edge has been added to the Graph.
    * <p>
    * In the event that any one of the vertices are not existing in the Graph,
    * they are added to the Graph.
    * <p>
    * <b>Note:</b> It is the caller's responsibility to make sure that the
    * type of Edge being added is an DirectedEdgeImpl.
    *
    * @param	e   The edge to be added to the Graph.
    */
  public void addEdge( EdgeIntf edge ) throws Exception {

    // Let the ancestor add the edge
    super.addEdge( edge );

    // Add the edge as outgoing edge v1 and an incoming edge of v2
    this.graphDirectionDelegate.addEdge( (DirectedEdge) edge );
  }


  /**
    * Removes an Edge from the Graph.
    *
    * @param		edge		Edge to be removed from the Graph
    */
  public void removeEdge( EdgeIntf edge ) throws Exception {
    // Remove the edge as an incoming and outgoing edge of the vertices
    // at both ends of the edge.
    this.graphDirectionDelegate.removeEdge( (DirectedEdge) edge );

    // Finally, tell the ancestor to remove the edge.
    super.removeEdge( edge );
  }

  /**
    * Removes a Vertex from the Graph.
    *
    * @param		vertex	Vertex to be removed
    */
  public void remove( Vertex vertex ) throws Exception {
    int   index = this.vertices.indexOf( vertex );

    // Call the ancestor method to remove the vertex
    super.remove( vertex );

    // Remove the adjacent edges entry of the vertex.
    this.graphDirectionDelegate.remove( index );
  }

  /**
   * Determines if there is a path from Vertex fromVertex to Vertex toVertex.
   * This will not return true if the only path has at least one Edge pointing
   * in the opposite direction of the path.
   *
   * @param		fromVertex		starting Vertex for the path
   * @param		toVertex			ending Vertex for the path
   * @return	true if there is a path from Vertex to toVertex. false otherwise.
   */
  public boolean isPath( Vertex fromVertex, Vertex toVertex ){
    return this.graphDirectionDelegate.isPath( fromVertex, toVertex );
  }

  /**
   * Determines if there is a cycle from Vertex fromVertex. A cycle occurs
   * when there is a path from the specified Vertex back to itself,
   * taking into consideration that direction of the Edges along the path.
   * This simply calls isPath(), where both parameters are the same Vertex.
   *
   * @param		fromVertex		Vertex to be tested for a cycle path.
   * @return	true if there is a cycle path from fromVertex to itself.
   */
  public boolean isCycle( Vertex fromVertex ){
    return this.graphDirectionDelegate.isCycle( fromVertex );
  }

  /**
   * Returns a String representation of the Graph. The string returned is of the form:
   * super.toString() + "\n" +
   *   "Incoming Edges: " + this.incomingEdges.toString() + "\n" +
   *   "Outgoing Edges: " + this.outgoingEdges.toString();
   *
   * @return	String representation of the Graph
   */
  public String toString() {
    return super.toString() + "\n" +
      "Incoming Edges: " + this.graphDirectionDelegate.getIncomingEdges().toString() + "\n" +
      "Outgoing Edges: " + this.graphDirectionDelegate.getOutgoingEdges().toString();
  }

}


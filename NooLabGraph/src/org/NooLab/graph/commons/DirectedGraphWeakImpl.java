package org.NooLab.graph.commons;

import java.util.*;

import org.NooLab.graph.algorithm.*;

/**
 * A weak implementation of the DirectedGraph interface.
 *
 * @author		Jesus M. Salvo Jr.
 */

class DirectedGraphWeakImpl implements DirectedGraph {
  /**
   * The GraphImpl object which has delegated the DirectedGraph interface implementation
   * to this object - DirectedGraphWeakImpl.
   */
  OpenJGraph graph;

  /**
    * List of outgoing edges in the graph. The index of a Vertex in the vertices List
    * matches the Vertex's outgoing edges index in the List outgoingEdges.
    *
    */
  List  outgoingEdges;

  /**
    * List of incoming edges in the graph. The index of a Vertex in the vertices List
    * matches the Vertex's incoming edges index in the List incomingEdges.
    *
    */
  List  incomingEdges;

  /**
    * Creates a new instance of DirectedGraphWeakImpl.
    */
  public DirectedGraphWeakImpl( OpenJGraph graph){
    outgoingEdges = new ArrayList( 10 );
    incomingEdges = new ArrayList( 10 );
    this.graph = graph;
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
    return outgoingEdges;
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
    return incomingEdges;
  }

  /**
    * Returns the outgoing edges of a particular Vertex in the Graph.
    *
    * @param		v		Vertex you want to determine its outgoing edges.
    * @return	  List of outgoing edges of the specified Vertex.
    */
  public List getOutgoingEdges( Vertex v ) {
    int indexVertex;

    indexVertex = this.graph.vertices.indexOf( v );
    return( (List) this.outgoingEdges.get( indexVertex ));
  }

  /**
    * Returns the incoming edges of a particular Vertex in the Graph.
    *
    * @param		v		Vertex you want to determine its incoming edges.
    * @return	  List of incoming edges of the specified Vertex.
    */
  public List getIncomingEdges( Vertex v ) {
    int indexVertex;

    indexVertex = this.graph.vertices.indexOf( v );
    return( (List) this.incomingEdges.get( indexVertex ));
  }

  /**
    * Returns the vertices that are adjacent to a specified Vertex,
    * respecting the direction of the Edge from the specified Vertex.
    *
    * @param		v		Vertex you want to determine its outgoing adjacent vertices.
    * @param        outGoing    If true, method will return outgoing adjacent vertices.
    *                           If false, method will return incoming adjacent vertices.
    * @return	  List of outgoing / incoming vertices adjacent to the specified Vertex.
    */
  private List getAdjacentVertices( Vertex v, boolean outGoing ) {
    List        adjacentVertices = new ArrayList( 10 );
    List        incidentEdges;
    Iterator    iterator;
    EdgeIntf        edge;
    Vertex      oppositeVertex;

    if( outGoing )
        incidentEdges = this.getOutgoingEdges( v );
    else
        incidentEdges = this.getIncomingEdges( v );

    iterator = incidentEdges.iterator();
    while( iterator.hasNext() ) {
      edge = (EdgeIntf) iterator.next();
      oppositeVertex = edge.getOppositeVertex( v );
      if( oppositeVertex != null )
        adjacentVertices.add( oppositeVertex );
    }

    return adjacentVertices;
  }

  /**
    * Returns the vertices that are adjacent to a specified Vertex
    * where the Edge is outgoing from the specified Vertex to the adjacent vertex.
    *
    * @param		v		Vertex you want to determine its outgoing adjacent vertices.
    * @return	  List of outgoing vertices adjacent to the specified Vertex.
    */
  public List getOutgoingAdjacentVertices( Vertex v ) {
    return this.getAdjacentVertices( v, true );
  }

  /**
    * Returns the vertices that are adjacent to a specified Vertex
    * where the Edge is incoming from the specified Vertex to the adjacent vertex.
    *
    * @param		v		Vertex you want to determine its incoming adjacent vertices.
    * @return	  List of incoming vertices adjacent to the specified Vertex.
    */
  public List getIncomingAdjacentVertices( Vertex v ) {
    return this.getAdjacentVertices( v, false );
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
    List        outIncidentEdges;
    Iterator	iterator;
    DirectedEdge		edge;

    // Get the adjacent edge set of the from vertex
    outIncidentEdges = this.getOutgoingEdges( fromvertex );

    // Find the edge where the direction is to the tovertex
    iterator = outIncidentEdges.iterator();
    while( iterator.hasNext()) {
      edge = (DirectedEdge) iterator.next();
      if( edge.getSink() == tovertex ) {
        // Edge is found.
        iterator = null;
        return edge;
      }
    }
    return null;
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
    List  visited = new ArrayList( 10 );

    this.graph.getTraversal().traverse( fromVertex, visited, new StopAtVisitor( toVertex ));
    if( toVertex == (Vertex) visited.get( visited.size() - 1 ) )
      return true;
    else
      return false;
  }

  /**
   * Determines if there is a cycle from Vertex fromVertex. A cycle occurs
   * when there is a path from the specified Vertex back to itself,
   * taking into consideration that direction of the Edges along the path.
   *
   * @param		fromVertex		Vertex to be tested for a cycle path.
   * @return	true if there is a cycle path from fromVertex to itself.
   */
  public boolean isCycle( Vertex fromVertex ){
    List            outedges = this.getOutgoingEdges( fromVertex );
    Iterator        iterator = outedges.iterator();
    DirectedEdge    dedge;
    Vertex          adjacentVertex;

    // For each outgoing edge of the vertex ...
    while( iterator.hasNext() ){
      dedge = (DirectedEdge) iterator.next();
      // ... get the opposite vertex
      adjacentVertex = dedge.getOppositeVertex( fromVertex );
      // .. and check if there is a path from the opposite vertex back to the vertex
      if( this.isPath( adjacentVertex, fromVertex ))
        // There is a cycle
        return true;
    }

    // No cycle
    return false;
  }

  // --- NON-DELEGATION METHODS
  /**
    * Adds a new List in both the incoming and outgoing List of Edges.
    * The two new vectors added will hold the new vertex's incoming and
    * outgoing edges.
    *
    * @param  newvertex   The Vertex object to be added to the graph.
    */
  public void add( Vertex newvertex ){
    // Whenever a new vertex is added, we also need to create
    // a blank adjacenct edge list for the new vertex
    outgoingEdges.add( new ArrayList( 10 ));
    incomingEdges.add( new ArrayList( 10 ));
  }

  /**
    * Removes the vertex's vectors of incoming and outgoing edges..
    *
    * @param		index	index of Vertex to be removed
    */
  protected void remove( int  index ) {
    // Remove the adjacent edges entry of the vertex
    this.outgoingEdges.remove( index );
    this.incomingEdges.remove( index );
  }

  /**
    * Adds the Edge created as an outgoing edge of one vertex and
    * as an incoming edge of the other vertex.
    *
    * @param		fromVertex		Vertex that will be the source of the Edge
    * @param		toVertex		Vertex that will be the sink of the Edge
    */
  protected void addEdge( DirectedEdge dedge ){
    List        v1outIncidentEdges;
    List        v2inIncidentEdges;
    EdgeIntf        edge;

    // Now get the vector of outgoing edge of v1
    v1outIncidentEdges = this.getOutgoingEdges( dedge.getSource() );
    // ... and the vector of incoming edge of v2
    v2inIncidentEdges = this.getIncomingEdges( dedge.getSink() );

    // Add the edge as an outgoing edge of v1 and as an incoming edge of v2
    v1outIncidentEdges.add( dedge );
    v2inIncidentEdges.add( dedge );
  }

  /**
    * Removes the Edge as the incoming and outgoing edge of the vertices
    * at the ends of the Edge.
    *
    * @param		edge		Edge to be removed from the Graph
    */
  protected void removeEdge( DirectedEdge dedge ) {
    Vertex	fromvertex;
    Vertex	tovertex;
    List    outIncidentEdges;
    List    inIncidentEdges;

    // Get source and sink vertices of edge
    fromvertex = dedge.getSource();
    tovertex = dedge.getSink();

    // Get the vector of outgoing edge of the source and the
    // vector of incoming edges of the sink.
    outIncidentEdges = this.getOutgoingEdges( fromvertex );
    inIncidentEdges = this.getIncomingEdges( tovertex );

    // Remove the edge from the source's outgoing edges
    outIncidentEdges.remove( dedge );
    // Remove the edge from the sink's incoming edges
    inIncidentEdges.remove( dedge );
  }

  /**
   * Empty method implementation. This method should never
   * be called or delegated to for whatever reason.
   */
  public void setGraphFactory( OpenJGraphFactory factory ) {}

  /**
   * Empty method implementation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public OpenJGraphFactory getGraphFactory() { return null; }

  /**
   * Empty method implemetation that returns 0. This method should never
   * be called or delegated to for whatever reason.
   */
  public int getVerticesCount() { return 0; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void remove( Vertex v ) {}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public Iterator getVerticesIterator() { return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public List cloneVertices() { return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public EdgeIntf createEdge( Vertex v1, Vertex v2 ) { return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public EdgeIntf addEdge( Vertex v1, Vertex v2 ) { return null; }

  /**
   * Empty method implemetation that does nothing. This method should never
   * be called or delegated to for whatever reason.
   */
  public void addEdge( EdgeIntf edge ) { return; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void removeEdge( EdgeIntf e ) {}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void removeEdges( Vertex v ){}

  /**
   * Empty method implemetation that returns 0. This method should never
   * be called or delegated to for whatever reason.
   */
  public int getDegree(){ return 0; }

  /**
   * Empty method implemetation that returns 0. This method should never
   * be called or delegated to for whatever reason.
   */
  public int getDegree( Vertex v ){ return 0; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public Set getVertices( int degree ){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public List getEdges( Vertex v ){ return null; }

  @Override
public List<EdgeIntf> getEdges() {
	// TODO Auto-generated method stub
	return null;
}

/**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public List getAdjacentVertices( Vertex v ){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public HashSet getAdjacentVertices( List vertices ){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public List getConnectedSets(){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public List getConnectedSet( Vertex v ){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void mergeconnectedSet( Vertex v1, Vertex v2 ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public List traverse( Vertex startat ){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public GraphTraversal getTraversal(){ return null; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void setTraversal( GraphTraversal traversal ){}

  /**
   * Empty method implemetation that returns false. This method should never
   * be called or delegated to for whatever reason.
   */
  public boolean isConnected( Vertex v1, Vertex v2 ){ return false; }

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void addGraphAddVertexListener( GraphAddVertexListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void addGraphAddEdgeListener( GraphAddEdgeListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void addGraphRemoveEdgeListener( GraphRemoveEdgeListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void addGraphRemoveVertexListener( GraphRemoveVertexListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void removeGraphAddVertexListener( GraphAddVertexListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void removeGraphAddEdgeListener( GraphAddEdgeListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void removeGraphRemoveEdgeListener( GraphRemoveEdgeListener listener ){}

  /**
   * Empty method implemetation that returns null. This method should never
   * be called or delegated to for whatever reason.
   */
  public void removeGraphRemoveVertexListener( GraphRemoveVertexListener listener ){}
}




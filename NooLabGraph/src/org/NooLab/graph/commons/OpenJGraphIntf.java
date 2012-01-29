package org.NooLab.graph.commons;

import java.util.*;
import java.io.*;

import org.NooLab.graph.algorithm.*;

/**
 * An interface for Graphs.
 */
public interface OpenJGraphIntf extends Serializable {

  // ------------------- Informational methods
  /**
    * Returns the number of vertices in the graph
    *
    * @return	The number of vertices in the graph.
    */
  public int getVerticesCount();

  // ------------------- Factory setup
  /**
   * Returns the factory that will be responsible for creating Vertices
   * and Edges in a Graph.
   */
  public OpenJGraphFactory getGraphFactory();

  /**
   * Sets the factory that will be responsible for creating Vertices
   * and Edges in a Graph.
   */
  public void setGraphFactory( OpenJGraphFactory factory );

  // ------------------- Vertex manipulation
  /**
    * Adds a Vertex into the Graph. This will also create a new entry
    * in the edges List and add the newly added Vertex to its own
    * connected set, thereby adding a new List in the connectedSet List.
    * Finally, all GraphAddVertexListeners are informed of the event that a
    * Vertex has been added to the Graph.
    *
    * @param		v		Vertex to be added to the Graph
    */
  public void add( Vertex v ) throws Exception;

  /**
   * Removes the specified Edge from the Graph.
   *
   * @param   edge    The Edge object to be removed.
   */
  public void remove( Vertex v ) throws Exception;

  /**
   * Returns an iterator that iterates through the graph's vertices.
   *
   * @return  An iterator of List vertices.
   */
  public Iterator getVerticesIterator();

  /**
   * Returns a clone of the List of vertices.
   *
   * @return  A clone of the List of vertices.
   */
  public List cloneVertices();

  // ----------------------- Edge manipulation
  /**
   * Method to create the proper type of Edge class.
   *
   * @param   v1    One endpoint of the edge
   * @param   v2    Other endpoint of the edge
   *
   * @deprecated    As of 0.9.0, this is replaced by <tt>GraphFactory.createEdge()</tt>.
   * Future releases will have this method removed.
   */
  public EdgeIntf createEdge( Vertex v1, Vertex v2 );

  /**
    * Adds an Edge into the Graph. The vertices of the Edge must already
    * be existing in the Graph for this method to work properly.
    * The vertices in both ends of the Edge are merged into one connected set,
    * thereby possibly decreasing the number of Lists in the coonectedSet List.
    * Finally, all GraphAddEdgeListeners are informed of the event that a
    * Edge has been added to the Graph.
    *
    * @param		v1	One endpoint of the edge
    * @param    v2  Other endpoint of the edge
    * @return   The Edge object created and added to the Graph.
    */
  public EdgeIntf addEdge( Vertex v1, Vertex v2 ) throws Exception;


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
    * type of Edge being added to the Graph matches the Graph. For example,
    * only a DirectedEdge must be added to a DirectedGraph.
    *
    * @param	e   The edge to be added to the Graph.
    */
  public void addEdge( EdgeIntf e ) throws Exception;

  /**
   * Removes the specified Edge from the Graph.
   *
   * @param   e    The Edge object to be removed.
   */
  public void removeEdge( EdgeIntf e ) throws Exception;

  /**
    * Removes incident Edges of a Vertex. The Edges removed are those whose
    * either endpoints has the specified vertex. This method is usually
    * called just prior to removing a Vertex from a Graph.
    *
    * @param		v	Vertex whose Edges are to be removed
    */
  public void removeEdges( Vertex v ) throws Exception;

  // --------------------------- Degree methods
  /**
   * Returns the degree of the graph, which is simply the highest degree
   * of all the graph's vertices.
   *
   * @return  An int indicating the degree of the graph.
   */
  public int getDegree();

  /**
   * Returns the degree of the vertex, which is simply the number of edges
   * of the vertex.
   *
   * @return  The degree of the vertex.
   */
  public int getDegree( Vertex v );

  /**
   * Returns all vertices with the specified degree.
   *
   * @param   degree    The degree of the vertex to be returned.
   * @return  A collection of vertices with the above specified degree.
   */
  public Set getVertices( int degree );

  // ---------------------- Adjacency methods
  /**
   * Returns a List of edges of the specified vertex.
   *
   * @param   v   The vertex whose edges we want returned
   * @return  A List of Edges that are incident edges of the specified vertex.
   */
  public List getEdges( Vertex v );

  public List<EdgeIntf> getEdges();
  
  
  /**
    * Returns the vertices adjacent to the specified vertex.
    *
    * @param		v		The Vertex you want to determine its adjacent vertices.
    * @return	  List of vertices adjacent to the specified vertex v.
    */
  public List getAdjacentVertices( Vertex v );

  /**
    * Returns the vertices adjacent to all the vertices in the given collection.
    *
    * @param		vertices		List of Vertex where each vertex in the returned Set
    *                               must be adjacent to.
    * @return	  Set of vertices adjacent to all the vertices in the supplied List.
    */
  public HashSet getAdjacentVertices( List vertices );

  // ------------------------ Connected set methods
  /**
    * Returns the connected sets in the Graph. Each List in the return List
    * is a List of vertices that are connected to each other, regardless of the
    * direction of the Edge conneting them together.
    *
    * @return		List of List of connected vertices.
    */
  public List getConnectedSets();

  /**
    * Returns the connected set to which the specified vertex belongs.
    *
    * @param		v		Vertex to which you want the connected set returned.
    * @return		List of connected vertices where the specified vertex belongs.
    */
  public List getConnectedSet( Vertex v );

  /**
    * Merges the connected sets to which Vertex v1 and Vertex v2 belongs, if
    * they are not yet connected. This ma result in decreasing the number of Lists
    * in the connectedSet List.
    *
    * @param		v1		Vertex whose connected set you want merged
    * with the connected set of Vertex v2.
    * @param		v2		Vertex whose connected set you want merged
    * with the connected set of Vertex v1.
    */
  public void mergeconnectedSet( Vertex v1, Vertex v2 );

  // ------------------------ Traversal methods
  /**
   *  Traverses the Graph starting at startat Vertex by performing a
   *  depth-first traversal. The vertices traversed from startat
   *  are stored in Visited. Only the connected
   *  components to which startat belongs to will be traversed.
   *
   *	@param	startat	 The Vertex to which you want to start the traversal.
   */
  public List traverse( Vertex startat );

  /**
   * Gets the traversal algorithm used by the Graph.
   *
   * @return  GraphTraversal object performing traversal for the Graph.
   */
  public GraphTraversal getTraversal();

  /**
   * Sets the graph traversal algorithm to be used
   *
   * @param traversal   A concrete implementation of the GraphTraversal object.
   */
  public void setTraversal( GraphTraversal traversal );

  // ------------------------ connectivity methods
  /**
   * Determines if two vertices are connected
   *
   * @param		v1		  starting Vertex for the path
   * @param		v2			ending Vertex for the path
   * @return	true if v1 and v2 are connected.
   */
  public boolean isConnected( Vertex v1, Vertex v2 );

  // ------------------------ Listener methods
  /**
    * Adds a GraphAddVertexListener to the Graph's internal List of
    * GraphAddVertexListeners so that when a new Vertex is added,
    * all registered GraphAddVertedListeners are notified of the event.
    *
    * @param		listener		GraphAddVertexListener you want registered
    * or be notified when a new Vertex is added
    * @see			org.NooLab.graph.commons.GraphAddVertexListener
    * @see			#removeGraphAddVertexListener( GraphAddVertexListener )
    */
  public void addGraphAddVertexListener( GraphAddVertexListener listener );

  /**
    * Adds a GraphAddEdgeListener to the Graph's internal List of
    * GraphAddEdgeListeners so that when a new Edge is added,
    * all registered GraphAddEdgeListeners are notified of the event.
    *
    * @param		listener		GraphAddEdgeListener you want registered
    * or be notified when a new Edge is added
    * @see			org.NooLab.graph.commons.GraphAddEdgeListener
    * @see			#removeGraphAddEdgeListener( GraphAddEdgeListener )
    */
  public void addGraphAddEdgeListener( GraphAddEdgeListener listener );

  /**
    * Adds a GraphRemoveEdgeListener to the Graph's internal List of
    * GraphRemoveEdgeListeners so that when an Edge is removed,
    * all registered GraphRemoveEdgeListeners are notified of the event.
    *
    * @param		listener		GraphRemoveEdgeListener you want registered
    * or be notified when an Edge is removed
    * @see			org.NooLab.graph.commons.GraphRemoveEdgeListener
    * @see			#removeGraphRemoveEdgeListener( GraphRemoveEdgeListener )
    */
  public void addGraphRemoveEdgeListener( GraphRemoveEdgeListener listener );

  /**
    * Adds a GraphRemoveVertexListener to the Graph's internal List of
    * GraphRemoveVertexListeners so that when a Vertex is removed,
    * all registered GraphRemoveVertexListeners are notified of the event.
    *
    * @param		listener		GraphRemoveVertexListener you want registered
    * or be notified when a Vertex is removed
    * @see			org.NooLab.graph.commons.GraphRemoveVertexListener
    * @see			#removeGraphRemoveVertexListener( GraphRemoveVertexListener )
    */
  public void addGraphRemoveVertexListener( GraphRemoveVertexListener listener );

  /**
    * Removes a GraphAddVertexListener from the Graph's internal List of
    * GraphAddVertexListeners.
    *
    * @param		listener		GraphAddVertexListener you no longer want registered
    * or be notified when a Vertex is added
    * @see			org.NooLab.graph.commons.GraphAddVertexListener
    * @see			#addGraphAddVertexListener( GraphAddVertexListener )
    */
  public void removeGraphAddVertexListener( GraphAddVertexListener listener );

  /**
    * Removes a GraphAddEdgeListener from the Graph's internal List of
    * GraphAddEdgeListeners.
    *
    * @param		listener		GraphAddEdgeListener you no longer want registered
    * or be notified when an Edge is added
    * @see			org.NooLab.graph.commons.GraphAddEdgeListener
    * @see			#addGraphAddEdgeListener( GraphAddEdgeListener )
    */
  public void removeGraphAddEdgeListener( GraphAddEdgeListener listener );

  /**
    * Removes a GraphRemoveEdgeListener from the Graph's internal List of
    * GraphRemoveEdgeListeners.
    *
    * @param		listener		GraphRemoveEdgeListener you no longer want registered
    * or be notified when an Edge is removed
    * @see			org.NooLab.graph.commons.GraphRemoveEdgeListener
    * @see			#addGraphRemoveEdgeListener( GraphRemoveEdgeListener )
    */
  public void removeGraphRemoveEdgeListener( GraphRemoveEdgeListener listener );

  /**
    * Removes a GraphRemoveVertexListener from the Graph's internal List of
    * GraphRemoveVertexListeners.
    *
    * @param		listener		GraphRemoveVertexListener you no longer want registered
    * or be notified when a Vertex is removed
    * @see			org.NooLab.graph.commons.GraphRemoveVertexListener
    * @see			#addGraphRemoveVertexListener( GraphRemoveVertexListener )
    */
  public void removeGraphRemoveVertexListener( GraphRemoveVertexListener listener );

}


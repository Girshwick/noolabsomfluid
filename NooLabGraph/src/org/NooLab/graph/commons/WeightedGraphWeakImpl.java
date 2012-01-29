package org.NooLab.graph.commons;

import java.util.*;

import org.NooLab.graph.algorithm.*;

/**
 * A weak implementation of the WeighedGraph interface. This is used internally
 * to implement a weighted graph without having an actual graph.
 *
 * @author  Jesus M. Salvo Jr.
 */
class WeightedGraphWeakImpl implements WeightedGraphIntf {
  /**
   * The GraphImpl object which has delegated the WeightedGraph interface implementation
   * to this object - WeightedGraphWeakImpl.
   */
  OpenJGraph   graph;

  /**
   * The MinimumSpanningTreeAlgorithm object to which the minimum spanning tree
   * is delegated to.
   */
  MinimumSpanningTreeAlgorithm    minimumSpanningTreeAlgorithm;

  /**
   * The ShortestPathAlgorithm object to which the shortest spanning tree
   * is delegated to.
   */
  ShortestPathAlgorithm           shortestPathAlgorithm;

  /**
   * Creates a new instance of WeightedGraphWeakImpl.
   */
  public WeightedGraphWeakImpl( OpenJGraph graph, MinimumSpanningTreeAlgorithm minspantreealgo, ShortestPathAlgorithm shortestpathalgo  ) {
    this.graph = graph;
    this.minimumSpanningTreeAlgorithm = minspantreealgo;
    this.shortestPathAlgorithm = shortestpathalgo;
  }

  /**
   * Delegate method to add a WeightedEdge with a specified weight
   * into the WeightedGraph. The default addEdge( v1, v2 ) will add a
   * WeightedEdge with zero weight, after which you can call setWeight()
   * to specify the weight.
   *
   * @return  The WeightedEdge that has been added.
   */
  public WeightedEdge addEdge( Vertex v1, Vertex v2, double weight ) throws Exception{
    WeightedEdge  edge = (WeightedEdge) graph.addEdge( v1, v2 );

    edge.setWeight( weight );
    return edge;
  }

  /**
   * Delegate method to add a WeightedEdge into the WeightedGraph.
   *
   * @param edge  Must be an instance of WeightedEdge that will be added.
   */
  public void addEdge( EdgeIntf edge ) throws Exception {
    graph.addEdge( (WeightedEdge) edge );
  }

  /**
   * Sets the algorithm used to determine the minimum spanning tree.
   */
  public void setMinimumSpanningTreeAlgorithm( MinimumSpanningTreeAlgorithm algo ) {
    this.minimumSpanningTreeAlgorithm = algo;
  }

  /**
   * Sets the algorithm used to determine the shortest path spanning tree.
   */
  public void setShortestPathAlgorithm( ShortestPathAlgorithm algo ) {
    this.shortestPathAlgorithm = algo;
  }

  /**
   * Determines the Vertex that is 'closest' to the Vertex specified.
   * The definition of the closest vertex in this context is a
   * vertex that is directly adjacent to Vertex v where the edge
   * has the least weight. There maybe more than adjacent vertex that is
   * connected to the specified vertex with the same edge weight. in which
   * case there is no guarantee which among the equally close vertices
   * are returned.
   *
   * @return  The Vertex closes to Vertex v.
   */
  public Vertex getClosest( Vertex v ) {
    // If the vertex has no edges, return null
    if( this.graph.getEdges( v ).size() == 0 )
      return null;

    // Specify a comparator to sort the adjacent edges by their weights
    TreeSet   set = new TreeSet(
      new Comparator() {
        public int compare( Object obj1, Object obj2 ) {
          WeightedEdge edge1 = (WeightedEdge) obj1;
          WeightedEdge edge2 = (WeightedEdge) obj2;

          if( edge1.getWeight() < edge2.getWeight() )
            return -1;
          else if( edge1.getWeight() > edge2.getWeight() )
            return 1;
          else
            return 0;
        }

        public boolean equals( Object obj ) {
          return obj.equals( this );
        }

      });

    set.addAll( this.graph.getEdges( v ));
    EdgeIntf e = (EdgeIntf) set.first();
    return e.getOppositeVertex( v );
  }

  /**
   * Determine a minimum spanning tree for the weighted graph.
   * There is no guarantee that the same method call will result in
   * the same result, as long as it satisifies the property of
   * a minimum spanning tree.
   *
   * @return  Subgraph connecting all the Vertices such that the sum
   * of the weights of the Edges is at least as small as the sum of the weights of
   * any other collection of Edges connecting all the Vertices.
   */
  public WeightedGraphIntf minimumSpanningTree() {
    return  this.minimumSpanningTreeAlgorithm.minimumSpanningTree();
  }

  /**
   * Determine a shortest path spanning tree for the weighted graph.
   * Shortest path spanning tree need not be unique. Therefore, there is
   * no guarantee that calling this method twice for the same weighted
   * graph will return exactly the same shortest path spanning tree,
   * unless there is only one shortest path spanning tree.
   * <p>
   * Also note that the graph returned by this method is a new instance
   * of WeightedGraph. However, its vertices and edges will be the same instance
   * as those of this WeightedGraph. Therefore, <b>do not</b> modify the contents
   * of the returned <tt>WeightedGraph</tt> such that any of its vertices or edges
   * are removed.
   *
   * @param vertex  The Vertex in the weighted graph that we want to get
   * the shortest paths to all other vertices.
   * @return  Shortest spanning tree subgraph from the vertex
   * parameter to all other vertices that are in the same connected set
   * as the vertex.
   */
  public WeightedGraphIntf shortestPath( Vertex vertex ) {
    return this.shortestPathAlgorithm.shortestPath( vertex );
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
  public void add( Vertex v ) {}

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
package org.NooLab.graph.commons;



import org.NooLab.graph.algorithm.*;
import org.NooLab.grphutil.*;

/**
 * An implementation of the WeightedGraph interface where all edges in the graph
 * have a weight.
 */

public class WeightedGraph extends OpenJGraph implements WeightedGraphIntf {

	private static final long serialVersionUID = 8621467961808277579L;

	
	/**
	 * Delegate object to handle the WeightedGraph interface.
	 */
	WeightedGraphWeakImpl graphWeightDelegate;

	/**
	 * Creates a new instance of WeightedGraphImpl. Default algorithm for
	 * minimum spanning tree will use Kruskal's method
	 * (MinimumSpanningTreeKruskalAlgorithm).
	 */
	public WeightedGraph() {
		super();
		MinimumSpanningTreeKruskalAlgorithm mstKruskal ;
		ShortestPathDijkstraAlgorithm shortestDijkstra ;
		
		this.factory = new WeightedGraphFactory();
		
		mstKruskal = new MinimumSpanningTreeKruskalAlgorithm(this) ;
		shortestDijkstra = new ShortestPathDijkstraAlgorithm(this, new HeapNodeComparator(-1));
		
		this.graphWeightDelegate = new WeightedGraphWeakImpl( this, mstKruskal, shortestDijkstra );
	}

	/**
	 * Factory method implementation that creates an instance of a WeightedEdge.
	 * This is now merely a shortcut to <tt>factory.crateEdge()</tt>.
	 * 
	 * @param v1
	 *            One endpoint of the vertex
	 * @param v2
	 *            The other endpoint of the vertex
	 */
	public EdgeIntf createEdge(Vertex v1, Vertex v2) {
		return this.factory.createEdge(v1, v2);
	}

	/**
	 * Convenience method to add a WeightedEdge with a specified weight into the
	 * WeightedGraph. The default addEdge( v1, v2 ) will add a WeightedEdge with
	 * zero weight, after which you can call setWeight() to specify the weight.
	 * 
	 * @return The WeightedEdge that has been added.
	 */
	public WeightedEdge addEdge(Vertex v1, Vertex v2, double weight)
			throws Exception {
		return this.graphWeightDelegate.addEdge(v1, v2, weight);
	}

	/**
	 * Determines the Vertex that is 'closest' to the Vertex specified. The
	 * definition of the closest vertex in this context is a vertex that is
	 * directly adjacent to Vertex v where the edge has the least weight.
	 * 
	 * @return The Vertex closes to Vertex v.
	 */
	public Vertex getClosest(Vertex v) {
		return this.graphWeightDelegate.getClosest(v);
	}

	/**
	 * Determine a minimum spanning tree for the weighted graph. There is no
	 * guarantee that the same method call will result in the same result, as
	 * long as it satisifies the property of a minimum spanning tree.
	 * 
	 * @return Subgraph connecting all the Vertices such that the sum of the
	 *         weights of the Edges is at least as small as the sum of the
	 *         weights of any other collection of Edges connecting all the
	 *         Vertices.
	 */
	public WeightedGraphIntf minimumSpanningTree() {
		return this.graphWeightDelegate.minimumSpanningTree();
	}

	/**
	 * Determine a shortest path spanning tree for the weighted graph. Shortest
	 * path spanning tree need not be unique. Therefore, there is no guarantee
	 * that calling this method twice for the same weighted graph will return
	 * exactly the same shortest path spanning tree, unless there is only one
	 * shortest path spanning tree.
	 * <p>
	 * Also note that the graph returned by this method is a new instance of
	 * WeightedGraph. However, its vertices and edges will be the same instance
	 * as those of this WeightedGraph. Therefore, <b>do not</b> modify the
	 * contents of the returned <tt>WeightedGraph</tt> such that any of its
	 * vertices or edges are removed.
	 * 
	 * @param vertex
	 *            The Vertex in the weighted graph that we want to get the
	 *            shortest paths to all other vertices.
	 * @return Shortest spanning subgtaph from the vertex parameter to all other
	 *         vertices that are in the same connected set as the vertex.
	 */
	public WeightedGraphIntf shortestPath(Vertex vertex) {
		return this.graphWeightDelegate.shortestPath(vertex);
	}

	/**
	 * Sets the algorithm used to determine the minimum spanning tree.
	 */
	public void setMinimumSpanningTreeAlgorithm(
			MinimumSpanningTreeAlgorithm algo) {
		this.graphWeightDelegate.setMinimumSpanningTreeAlgorithm(algo);
	}

	/**
	 * Sets the algorithm used to determine the shortest path spanning tree.
	 */
	public void setShortestPathAlgorithm(ShortestPathAlgorithm algo) {
		this.graphWeightDelegate.setShortestPathAlgorithm(algo);
	}

}
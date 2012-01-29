package org.NooLab.graph.algorithm;

import java.util.*;
import java.io.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.Visitor;

/**
 * Abstract class for an algorithm implementing graph traversal.
 * Classes implementing the Graph interface uses the Strategy
 * pattern to allow different implementations of the graph traversal
 * algorithm to be used.
 *
 * Concrete implementations of this class must never modify the Graph itself.
 */

public abstract class GraphTraversal implements Serializable {
  static final public int   TERMINATEDBYVISITOR = -1;
  static final public int   OK = 1;

  /**
   * The Graph on which graph traversal will be performed.
   */
  OpenJGraphIntf   graph;

  public GraphTraversal( OpenJGraphIntf graph ) {
    this.graph = graph;
  }

  /**
   * Abstract traversal method to be implemented by subclasses.
   *
   * @param startat The vertex from which traversal will start.
   * @param visitor Visitor object controlling if and when traversal will stop,
   *                apart from having visited all the vertices.
   * @param visited A List of vertices that has been visited in sequence by the traversal
   */
  public abstract int traverse( Vertex startat, List visited, Visitor visitor );

  /**
   * Abstract traversal method to be implemented by subclasses.
   *
   * @param startat The vertex from which traversal will start.
   * @return  A VList of vertices that has been visited in sequence by the traversal
   */
  public abstract List traverse( Vertex startat );

  /**
   * Abstract traversal method to be implemented by subclasses.
   *
   * @param startat The vertex from which traversal will start.
   * @param visitor Visitor object controlling if and when traversal will stop,
   *                apart from having visited all the vertices.
   * @return  A List of vertices that has been visited in sequence by the traversal
   */
  public abstract List traverse( Vertex startat, Visitor visitor );
}
package org.NooLab.graph.algorithm;

import java.util.*;
import java.io.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.WeightedGraphIntf;

/**
 * Abstract class for an algorithm implementing the minimum spanning tree.
 * Classes implementing the WeightedGraph interface uses the Strategy
 * pattern to allow different implementations of the minimum spanning tree
 * algorithm to be used.
 *
 * Concrete implementations of this class must never modify WeightedGraph.
 *
 * @author  Jesus M. Salvo Jr.
 */

public abstract class MinimumSpanningTreeAlgorithm implements Serializable {

  /**
   * The WeightedGraph object that the algorithm uses to determine
   * the minimum spanning tree.
   */
  WeightedGraphIntf   wgraph;

  public MinimumSpanningTreeAlgorithm( WeightedGraphIntf wgraph ) {
    this.wgraph = wgraph;
  }

  /**
   * Abstract method to be implemented by subclasses.
   */
  public abstract WeightedGraphIntf minimumSpanningTree();
}
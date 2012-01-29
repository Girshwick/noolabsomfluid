package org.NooLab.graph.commons;

/**
 * Represents a weighted edge in a graph.
 *
 * @author		Jesus M. Salvo Jr.
 */
public interface WeightedEdge extends EdgeIntf{

  /**
   * Returns the weight of the edge.
   */
  public double getWeight();

  /**
   * Sets the weight of the edge.
   * 
   * @param   weight    The new weight of the edge
   */
  public void setWeight( double weight );
}
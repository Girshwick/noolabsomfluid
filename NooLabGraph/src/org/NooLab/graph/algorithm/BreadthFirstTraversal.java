package org.NooLab.graph.algorithm;

import java.util.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.NullVisitor;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.Visitor;
import org.NooLab.grphutil.*;
import org.NooLab.grphutil.Queue;


/**
 * A concrete subclass of GraphTraversal that uses breadth-first search
 * in traversing a graph. Note that the traverse() method will only
 * traverse the connected set to which the Vertex the traversal will start at belongs.
 *
 * @author  Jesus M. Salvo Jr.
 */

public class BreadthFirstTraversal extends GraphTraversal {
 
	private static final long serialVersionUID = 6726188155431365164L;
Queue queue;

  /**
   * Creates a BreadthFirstTraversal object
   */
  public BreadthFirstTraversal( OpenJGraphIntf graph ) {
    super( graph );
    this.queue = new Queue();
  }

  public int traverse(Vertex startat, List visited, Visitor visitor) {
    Vertex  next;
    Vertex  adjacent;
    List    adjacentVertices;
    Iterator  iterator;

    // Put the starting vertex in the queue
    this.queue.put( startat );

    try {
      do {
        // Get the next vertex in the queue and add it to the visited
        next = (Vertex) this.queue.get();
        visited.add( next );

        // Exit if the visitor tells us so
        if( !visitor.visit( next ))
          return TERMINATEDBYVISITOR;

        // Get all of its adjacent vertices and put them in the queue
        // only if it has not been visited and it has not been queued
        adjacentVertices = this.graph.getAdjacentVertices( next );
        iterator = adjacentVertices.iterator();
        while( iterator.hasNext()) {
          adjacent = (Vertex) iterator.next();
          if( !visited.contains( adjacent ) && !queue.isQueued( adjacent )) {
            this.queue.put( adjacent );
          }
        }

      } while( !this.queue.isEmpty() );
    }
    // This should not happen, but catch it anyway as it is required,
    // but do nothing.
    catch( EmptyQueueException e ) {}
    finally {
      return OK;
    }
  }

  public List traverse(Vertex startat, Visitor visitor) {
    List    visited = new ArrayList( 10 );

    this.traverse( startat, visited, visitor );
    return visited;
  }

  public List traverse(Vertex startat) {
    List    visited = new ArrayList( 10 );

    this.traverse( startat, visited, new NullVisitor() );
    return visited;
  }
}
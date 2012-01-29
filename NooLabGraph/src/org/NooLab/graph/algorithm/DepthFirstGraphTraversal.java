package org.NooLab.graph.algorithm;

import java.util.*;

import org.NooLab.graph.*;
import org.NooLab.graph.commons.OpenJGraphIntf;
import org.NooLab.graph.commons.NullVisitor;
import org.NooLab.graph.commons.Vertex;
import org.NooLab.graph.commons.Visitor;

/**
 * A concrete subclass of GraphTraversal that uses depth-first search
 * in traversing a graph. Note that the traverse() method will only
 * traverse the connected set to which the Vertex the traversal will start at belongs.
 *
 * @author  Jesus M. Salvo Jr.
 */

public class DepthFirstGraphTraversal extends GraphTraversal {
  Stack   stack;

  /**
   * Creates a DepthFirstGraphTraversal object.
   */
  public DepthFirstGraphTraversal( OpenJGraphIntf graph ) {
    super( graph );
    this.stack = new Stack();
  }

  public int traverse( Vertex startat, List visited, Visitor visitor ) {
    Vertex  next;
    Vertex  adjacent;
    List    adjacentVertices;
    Iterator  iterator;

    // Push the starting vertex onto the stack
    this.stack.push( startat );

    do {
      // Get the next vertex in the queue and add it to the visited
      next = (Vertex) this.stack.pop();
      visited.add( next );

      // Exit if the visitor tells us so
      if( !visitor.visit( next ))
        return TERMINATEDBYVISITOR;

      // Get all of its adjacent vertices and push them onto the stack
      // only if it has not been visited and it has not been stacked
      adjacentVertices = graph.getAdjacentVertices( next );
      iterator = adjacentVertices.iterator();
      while( iterator.hasNext()) {
        adjacent = (Vertex) iterator.next();
        if( !visited.contains( adjacent ) && !this.stack.contains( adjacent )) {
          this.stack.push( adjacent );
        }
      }

    } while( !this.stack.isEmpty() );
    return OK;
  }

  public List traverse( Vertex startat ) {
    return this.traverse( startat, new NullVisitor());
  }

  public List traverse( Vertex startat, Visitor visitor ) {
    List  visited = new ArrayList( 10 );

    this.traverse( startat, visited, visitor );
    return visited;
  }
}
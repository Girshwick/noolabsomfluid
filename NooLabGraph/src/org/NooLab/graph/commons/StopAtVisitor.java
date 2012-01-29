package org.NooLab.graph.commons;

/**
 * A <tt>Visitor</tt> that notifies a traversal to stop at a particular <tt>Vertex</tt>.
 *
 * @author Jesus M. Salvo Jr.
 *
 * $Id: StopAtVisitor.java,v 1.1 2001/03/24 08:53:09 jmsalvo Exp $
 */

class StopAtVisitor extends NullVisitor {
  /**
    * Vertex to check for when visiting a vertex
    */
  Vertex	objectToCheck;

  /**
    * Creates a new instance of StopAtVisitor and specifies
    * which Vertex stop
    *
    * @param		objectToCheck		stop at the specified vertex
    */
  StopAtVisitor( Vertex objectToCheck ){
    super();
    this.objectToCheck = objectToCheck;
  }

  /**
    * Override of superclass' visit() method. Compares the Vertex
    * being visited to the Vertex specified in the constructor.
    * If they are the same, return false. Otherwise, return true.
    *
    * @param	objectToVisit		Vertex being visited.
    * @return	false if the Vertex being visited is the same as the
    * Vertex specified in the constructor. True otherwise.
    */
  public boolean visit( Vertex objectToVisit ){
    if( objectToVisit == objectToCheck )
      return false;
    else
      return true;
  }

}

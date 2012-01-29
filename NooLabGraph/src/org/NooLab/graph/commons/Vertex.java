package org.NooLab.graph.commons;

 

/**
 * A vertex in a graph.
 *
 * @author		Jesus M. Salvo Jr.
 * 
 * this part extended by kwa
 */
public interface Vertex extends GraphComponent {
	
	public int getIndex();
	public void setIndex( int ix);
	
	public Object getDataObject();
	public void   setDataObject( Object obj);
	
}

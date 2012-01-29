package org.NooLab.graph;

public interface TreeLinesIntf {


	public int size() ;

	public void clear() ;
	
	public int indexOf( Object obj) ;

	public void addItem( PPointXYIntf p) ;
	
	public PPointXYIntf getItem( int index) ;

	public void setItem( int index, PPointXYIntf p ) ;

	public void removeItem( int index ) ;
	
}

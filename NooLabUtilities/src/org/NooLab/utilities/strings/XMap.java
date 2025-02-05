package org.NooLab.utilities.strings;




/**
 * 
 * 
 * kind of a hash map as a Vector
 * 
 * 
 */
public class XMap {

	public int index ;
	public int position ;
	public String str ; 
	public double value ;
	
	// ------------------------------------------------------------------------
	public XMap( int _index, int _position, String _str ){
		
		index = _index ; 
		position = _position;
		str = _str ;
		
	}
	
	public XMap( int _index, int _position, double _val){
		
		index = _index ; 
		position = _position;
		value = _val ;
		
	}
	
	public XMap( int _index, int _position, String _str, double _val){
		
		index = _index ; 
		position = _position;
		value = _val ;
		str = _str ;
		
	}	
	
	public XMap( int _index, double _val){
		
		index = _index ; 
		value = _val ;
	}
	// ------------------------------------------------------------------------
	
	
	public XMap clear(){
		index = -1 ; 
		position = -1;
		value = 0 ;
		str = "" ;
		return this;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
}

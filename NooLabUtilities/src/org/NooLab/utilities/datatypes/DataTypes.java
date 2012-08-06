package org.NooLab.utilities.datatypes;

public class DataTypes {
	
	public static final int _TYPE_INT       = 1;
	public static final int _TYPE_BOOL      = 2;
	public static final int _TYPE_LONG      = 5;
	public static final int _TYPE_DOUBLE    = 6;
	public static final int _TYPE_FLOAT     = 7;
	public static final int _TYPE_STRING    = 10;
	public static final int _TYPE_ARRPRIM   = 15;
	public static final int _TYPE_ARRUSER   = 16;
	public static final int _TYPE_USERCLASS = 21;
	
	// TODO allow for dynamic definitions of types
	
	public DataTypes(){
	}
	
	public int gettypeid( String classname ){
		return getTypeId(classname);
	}
	
	public static int getTypeId( String classname ){
		int _typeid = -1;
		String str = classname.toLowerCase() ;
		
		if (str.startsWith("int")){
			_typeid = _TYPE_INT ;
		}
		if (str.startsWith("doub")){
			_typeid =  _TYPE_DOUBLE;
		}
		if (str.startsWith("long")){
			_typeid = _TYPE_LONG ;
		}
		if (str.startsWith("string")){
			_typeid = _TYPE_STRING ;
		}
		
		return _typeid ;
	}
	
}

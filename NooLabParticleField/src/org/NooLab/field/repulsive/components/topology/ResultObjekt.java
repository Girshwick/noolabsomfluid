package org.NooLab.field.repulsive.components.topology;

import java.io.Serializable;

public class ResultObjekt implements Serializable{

	static final long serialVersionUID = 1115117354688828288L;
 
	String descriptor="";
	Object obj;
	
	public ResultObjekt(String did, Object obj ){
		descriptor = did;
		this.obj = obj;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	
	
}

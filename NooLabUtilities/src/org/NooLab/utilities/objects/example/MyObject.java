package org.NooLab.utilities.objects.example;


public class MyObject extends MyObjectSuperClass implements AnInterface2{

	@Override
	public void perform(int i) {
		 
		System.out.println( "Object (param:"+i+") created : "+this.toString() );
	}
    //... body of class ... override superclass methods
    //    or implement interface methods
	
	
}


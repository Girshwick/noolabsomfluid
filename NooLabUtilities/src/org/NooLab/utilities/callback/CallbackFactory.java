package org.NooLab.utilities.callback;

import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;

// import com.sun.xml.xsom.impl.scd.Iterators.Map;



public class CallbackFactory {

    private String methodName;
    // private Object scope;
    private Object scope;
    Class cscope;
    
    private static final ConcurrentHashMap<String, Class> BUILT_IN_MAP = new ConcurrentHashMap<String, Class>();
    
    
                          // Object  Class
    public CallbackFactory( Object scope, String methodName ) {
        this.methodName = methodName;
        this.scope = scope;
        cscope = scope.getClass();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void invoke2(Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    	Method method ;
    	Class sclass = null;
    	Class[] pcs ;
    	Object obj;
    	Method[] mm ;
    	 
    	mm = cscope.getMethods() ;
    	mm = ((Class<?>)scope).getClass().getMethods() ;
    	pcs = getParameterClasses(parameters);
    	sclass = scope.getClass();
    	
    	method = sclass.getMethod( methodName, pcs );
    	
    	if (parameters.length==0){
    		method.invoke( (Object)sclass,(Object)parameters );
    	}else{
    		method.invoke( (Object)sclass, (Object)parameters );	
    	}
        
        
        return ;
    }

    

    public Object invoke(Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    	Method method = null; 
    	Object obj ;
    	Class<?>[] pcs, pts;
    	Class<?> pt;
    	Class<?> sclass ;
    	Method[] mm ;
    	Method m;
    	int p,pc;
    	String str;
    	boolean  hb;
    	Type[] ct;
    	
    	
    	pcs = getParameterClasses(parameters);
    	
    	// the problem is, that pcs contains sth like "int", while it should contain "class java.lang.Integer" ...
    	// thus it does not find it... we have have to loop through it and correct it
    	sclass = scope.getClass();
    	mm = sclass.getMethods() ;
    	
    	try{
    		// astonishingly, this fails sometimes, ....
    		method = sclass.getMethod( methodName, pcs );
    		
    	}catch(NoSuchMethodException e){
    		// so we check whether we find something
    		p=-1;
    		for (int i=0;i<mm.length;i++){
    			m = mm[i];
    			
    			str = m.getName() ;
    			pts = m.getParameterTypes();
    			ct = m.getGenericParameterTypes() ;
    			
    			if (str.contentEquals(methodName)){
    				hb = checkSignature(pcs, pts);
    				if (hb){
    					p=i; 
    					break;
    				}
    			}
    		}
    		if (p>=0){
    			method = mm[p] ;
    		}
    		
    	}
    	
    	obj = method.invoke(scope, parameters);
    	return obj;
    }

    private boolean checkSignature( Class<?>[] providedParams, Class<?>[] pts){
    	boolean rb=true;
    	Class<?> provpar;
    	Class<?> ptype;
    	String name ;
    	Class dc = null,c2;
    	// providedParams: e.g. [class java.lang.Integer, class java.lang.String]
    	TypeVariable[] tvs = null ;
    	int n;
    	boolean hb;
    	
    	if (providedParams.length != pts.length){
    		return false;
    	}
    	
    	for (int i=0;i<providedParams.length;i++){
    		provpar = providedParams[i] ;
    		ptype = pts[i];
    		  
    		name = ptype.getName() ;
    		  
    		if (provpar.equals(ptype)==false){
    			hb = false;
    		}else{
    			hb = true;
    		}
    		if ((hb==false) && (name.contains("java")==false)){
    			try {
					  
					name = provpar.getName();
					Class clazz = Class.forName(name);
					
					Object typedClass;
					typedClass = clazz.getField("TYPE").get(null); // class java.lang.Integer -> int
					 
					if (typedClass.equals(ptype)){
						hb = true;
					}
					 
				} catch (Exception e) {
				}
    		}
    		
    		if (hb==false){
    			rb=false;
    		}
    	} // i-> all comparisons
    	
    	return rb;
    }
    
    
    

    static {
        for ( Class c : new Class[]{ void.class, boolean.class, byte.class, char.class,  
                					short.class, int.class, float.class, double.class, long.class})
              BUILT_IN_MAP.put(c.getName(), c);
    }

    public static Class forName(String name) throws ClassNotFoundException {
        Class c = BUILT_IN_MAP.get(name);
        if (c == null)
            // assumes you have only one class loader!
            BUILT_IN_MAP.put(name, c = Class.forName(name));
        return c;
    }

    
    private Class[] getParameterClasses(Object... parameters) {
    	Class c,c2;
    	Class[] classes = null ;
    	String cname;
    	
		try {
			classes = new Class[parameters.length];

			for (int i = 0; i < classes.length; i++) {
				c = parameters[i].getClass();

				cname = c.getName();

				c2 = forName(cname);

				c2 = parameters[i].getClass();
				c2 = c2.getDeclaringClass();

				classes[i] = c;
			}
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
        return classes;
    }
}


/*
the original

public class CallBack {
    private String methodName;
    private Object scope;

    public CallBack(Object scope, String methodName) {
        this.methodName = methodName;
        this.scope = scope;
    }

    public Object invoke(Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = scope.getClass().getMethod(methodName, getParameterClasses(parameters));
        return method.invoke(scope, parameters);
    }

    private Class[] getParameterClasses(Object... parameters) {
        Class[] classes = new Class[parameters.length];
        for (int i=0; i < classes.length; i++) {
            classes[i] = parameters[i].getClass();
        }
        return classes;
    }
}


*/
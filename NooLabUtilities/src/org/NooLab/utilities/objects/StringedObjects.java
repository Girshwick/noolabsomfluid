package org.NooLab.utilities.objects;

 
import java.io.*;
import java.lang.ClassNotFoundException;

 
import org.apache.commons.codec.binary.Base64;

/** 
 * 
 * This class serializes and deserializes objects using Base64;
 * note, that the objects need to be fully serializable
 * 
 * for encoding objects to XML,
 * see also:  http://xstream.codehaus.org/tutorial.html
 * using xstream (which is based on reflection) getters and setters are not necessary...
 * 
 * 
 * String xml = xstream.toXML(joe);
 * Person newJoe = (Person)xstream.fromXML(xml);	
 * 
 */
public class StringedObjects {
 
	Base64 base64 = new Base64();
	
	String stringedObject ="" ;
	Object decodedObject ;
	
	public String lastError="";
	
	
	public StringedObjects(){
    	
    }
           

	public void decode(){
		decodedObject = decode( stringedObject ) ;
	}
	
    public Object decode( String serialObjStr ){
    	Object obj=null;
    	
    	try{
    		obj = fromString( serialObjStr );
    	
    	}catch(IOException e){
    		lastError = e.getMessage() ;
    	} catch( ClassNotFoundException e){
    		lastError = e.getMessage() ;
    	}
    	return obj;
    }
    
    
    public void encode(){
    	stringedObject = encode(decodedObject);
    }
    /** Write the object to a Base64 string. */
	public String encode( Object obj ){
		
		String str="";
		ByteArrayOutputStream baos ;
		ObjectOutputStream oos ;
		
		try {
			
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			// oos.close();
			oos.flush() ;
			
			if (baos.size()>1){
				str = new String( base64.encode( baos.toByteArray() ) );
			}else{
				str = "";
			}
			
			oos.close();
			baos.close() ;
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
	    return str;
	}


	/** Read the object from Base64 string. */
    private Object fromString( String s ) throws IOException ,
                                                        ClassNotFoundException {
    			 
        byte [] data =  base64.decode( s );
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }
    
    
    public void setSerializedObject(String serializedObject) {
		this.stringedObject = serializedObject;
	}


	public String getSerializedObject() {
		return stringedObject;
	}


	public Object getDecodedObject() {
		return decodedObject;
	}


	public void setDecodedObject(Object decodedObject) {
		this.decodedObject = decodedObject;
	}


	public String getStringedObject() {
		return stringedObject;
	}


	public void setStringedObject(String stringedObject) {
		this.stringedObject = stringedObject;
	}    
}
 

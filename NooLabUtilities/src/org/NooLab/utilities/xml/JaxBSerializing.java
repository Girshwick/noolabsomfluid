package org.NooLab.utilities.xml;

import javax.xml.bind.Unmarshaller;


/**
 * 
 * 
 * 
 * @author kwa
 *
 */
public class JaxBSerializing {

	
	public JaxBSerializing(){
		
	}
	
	public void createString( Class clazz){ // like  abc.class
		
	}
	
	public Unmarshaller  createObject( Class clazz){
		
		return null; 
	}
	
	
}

/*

For example:

@XmlRootElement(name="student")
@XmlAccessorType(XmlAccessType.NONE)
class Student {
  @XmlElement(name="name")
  private String name;

  @XmlElement(name="age")
  private int age;

  public Student() {
  }

  public String getName() { return name; }

  public int getAge() { return age; }
}

Then you can use serialize it using JAXB Marshaller:

StringWriter writer = new StringWriter();
JAXBContext context = JAXBContext.newInstance(Student.class);
Marshaller m = context.createMarshaller();
m.marshal(student, writer);

And deserialize it as well by Unmarshelling the input ..

JAXBContext context = JAXBContext.newInstance(Student.class);
Unmarshaller m = context.createUnmarshaller();
return (Student)m.unmarshal(new StringReader(input));

Make sure you look at the JavaDoc I mentioned above since there are many ways to do so.

If you cannot modify your classes, you can still use JAXB (or you can use XStream) Assuming your class is the following:

class Student {
  private String name;
  private int age;

  public Student() {
  }

  public void setName(String name) { this.name = name; }
  public String getName() { return name; }
  public void setAge(int age) { this.age = age; }
  public int getAge() { return age; }
}

You can serialize it by doing:

Student student = new Student();
student.setAge(25);
student.setName('FooBar');
StringWriter writer = new StringWriter();
JAXBContext context = JAXBContext.newInstance(Student.class);
Marshaller m = context.createMarshaller();
m.marshal(new JAXBElement(new QName(Student.class.getSimpleName()), Student.class, student), writer);
System.out.println(writer.toString());


*/
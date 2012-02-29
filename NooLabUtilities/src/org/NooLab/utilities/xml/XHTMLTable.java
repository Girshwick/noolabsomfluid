package org.NooLab.utilities.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;




class XHTMLTable {

	public XHTMLTable(){
		
	}
	
	public void evaluateXhtMLTable(File xmlDocument) {

		String expression;

		XPathFactory factory;
		XPath xPath;
		XPathExpression xPathExpression;

		Element show;

		try {

			factory = XPathFactory.newInstance();
			xPath = factory.newXPath();
			InputSource inputSource = new InputSource(new FileInputStream(
					xmlDocument));

			xPathExpression = xPath.compile("/table[@id='main-content']");

			expression = "/table[@id='main-content']";
			inputSource = new InputSource(new FileInputStream(xmlDocument));
			NodeList shows = (NodeList) xPath.evaluate(expression, inputSource,
					XPathConstants.NODESET);

			for (int i = 0; i < shows.getLength(); i++) {

				show = (Element) shows.item(i);
				System.out.println("The value of show.getTagName(): "
						+ show.getTagName());
				System.out.println("The value of show.getTextContent(): "
						+ show.getTextContent());
			}
		} catch (IOException e) {
		} catch (XPathExpressionException e) {
		}
	}

}
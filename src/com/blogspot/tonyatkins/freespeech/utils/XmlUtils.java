/**
 * Copyright 2012-2015 Upright Software <info@uprightsoftware.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Upright Software ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Upright Software OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.utils;

import java.util.Arrays;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
	public static String convertDomToString(Document doc) {
		StringBuffer buffer = new StringBuffer();
		
		Element docElement = doc.getDocumentElement();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		convertElementToString(docElement,buffer,0);
		
		return buffer.toString();		
	}

	private static void convertElementToString(Node node, StringBuffer buffer, int indentLevel) {
		if (node == null) return;
		char[] padding = new char[indentLevel * 2];
		Arrays.fill(padding, ' ');
		String paddingString = String.valueOf(padding);
		
		buffer.append(paddingString + "<" + node.getNodeName());
		NamedNodeMap attributes =	node.getAttributes();
		if (attributes != null && attributes.getLength() > 0) {
            buffer.append("\n");
			for (int a=0; a<attributes.getLength(); a++) {
				Node attribute = attributes.item(a);
				buffer.append(paddingString + "  " + attribute.getNodeName() + "=\"" + attribute.getNodeValue() + "\" \n");
			}
            buffer.append(paddingString);
		}
        buffer.append(">");

		NodeList childNodes = node.getChildNodes();
		if (childNodes != null && childNodes.getLength() > 0) {
			for (int b=0; b < childNodes.getLength(); b++) {
				Node childNode = childNodes.item(b);
                if (childNode.getNodeType() == Element.TEXT_NODE) {
        			buffer.append(childNode.getTextContent());
                }
                else {
    				convertElementToString(childNode, buffer, indentLevel + 1);
                }
			}
		}

		buffer.append("</" + node.getNodeName() + ">\n");
	}
	
	public static Node getFirstChildElement(Node parentNode, String tag) {
		if (parentNode != null && tag != null) {
			NodeList childNodeList = parentNode.getChildNodes();
			for (int a=0; a<childNodeList.getLength(); a++) {
				Node childNode = childNodeList.item(a);
				if (childNode.getNodeName().toLowerCase().equals(tag.toLowerCase())) {
					return childNode;
				}
			}
		}
		
		return null;
	}

	public static String getNodeValue(Node node, String defaultValue) {
		String nodeValue = getNodeValue(node);
		if (nodeValue != null) return nodeValue;
		
		return defaultValue;
	}
	
	public static String getNodeValue(Node node) {
		if (node == null) return null;
		if (node.getNodeValue() != null) return node.getNodeValue();

		NodeList nodeList = node.getChildNodes();
		for (int a=0; a < nodeList.getLength(); a++) {
			Node childNode = nodeList.item(a);
			if (childNode.getNodeType() == Node.TEXT_NODE && childNode.getNodeValue() != null) {
				return childNode.getNodeValue();
			}
		}
		
		return null;
	}
}

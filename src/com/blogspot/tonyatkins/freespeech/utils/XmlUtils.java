/**
 * Copyright 2008-2013 Clayton Lewis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		
		buffer.append(paddingString + "<" + node.getNodeName() + "\n");
		NamedNodeMap attributes =	node.getAttributes();
		if (attributes != null) {
			for (int a=0; a<attributes.getLength(); a++) {
				Node attribute = attributes.item(a);
				buffer.append(paddingString + "  " + attribute.getNodeName() + "=\"" + attribute.getNodeValue() + "\" \n");
			}
		}
		buffer.append(paddingString + ">\n");

		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength() > 0) {
			for (int b=0; b < childNodes.getLength(); b++) {
				Node childNode = childNodes.item(b);
				convertElementToString(childNode, buffer, indentLevel + 1);
			}
		}
		else {
			buffer.append(paddingString + "  " + node.getTextContent() + "\n");
		}
		
		buffer.append(paddingString + "</" + node.getNodeName() + ">\n");
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

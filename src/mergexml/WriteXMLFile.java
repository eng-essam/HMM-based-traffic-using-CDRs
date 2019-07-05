/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mergexml;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import utils.FromNode;
import utils.ToNode;

/**
 *
 * @author essam
 */
public class WriteXMLFile {

    ArrayList<FromNode> edges;
    String path;

    public WriteXMLFile(String path, ArrayList<FromNode> edges) {
        this.edges = edges;
        this.path = path;
    }

    public void writeProbFile() {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("turn-defs");
            doc.appendChild(rootElement);

            for (FromNode fromNode : edges) {
                Element fromNodeElement = doc.createElement("fromNode");
                rootElement.appendChild(fromNodeElement);

                // set attribute to staff element
                Attr attr = doc.createAttribute("id");
                attr.setValue(fromNode.ID);
                fromNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("x");
                attr.setValue(String.valueOf(fromNode.x));
                fromNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("y");
                attr.setValue(String.valueOf(fromNode.y));
                fromNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("isExt");
                attr.setValue(String.valueOf(fromNode.isExit));
                fromNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("zone");
                attr.setValue(String.valueOf(fromNode.zone));
                fromNodeElement.setAttributeNode(attr);
                
//                System.out.println(fromNode.ID + "\t" + fromNode.x + "\t" + fromNode.y);
                ArrayList<ToNode> toNodesList = fromNode.toedges;
                for (ToNode toNode : toNodesList) {
                    Element toNodeElement = doc.createElement("toNode");
                fromNodeElement.appendChild(toNodeElement);

                // set attribute to staff element
                attr = doc.createAttribute("id");
                attr.setValue(toNode.ID);
                toNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("probability");
                attr.setValue(String.valueOf(toNode.Prob));
                toNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("x");
                attr.setValue(String.valueOf(toNode.x));
                toNodeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("y");
                attr.setValue(String.valueOf(toNode.y));
                toNodeElement.setAttributeNode(attr);
                
//                attr = doc.createAttribute("isExt");
//                attr.setValue(String.valueOf(toNode.isExit));
//                toNodeElement.setAttributeNode(attr);
//                
//                attr = doc.createAttribute("zone");
//                attr.setValue(String.valueOf(toNode.zone));
//                toNodeElement.setAttributeNode(attr);
                
//                    System.out.println("\t" + toNode.ID + "\t" + toNode.Prob + "\t" + toNode.x + "\t" + toNode.y);
                }
            }

            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(this.path));

		// Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
        }

    }
}

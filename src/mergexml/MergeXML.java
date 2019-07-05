/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mergexml;


import java.util.ArrayList;

import utils.FromNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class MergeXML {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String path=args[0];
        String probPath = args[0];
        String edgesPath = args[1];
        String finalXmlPath=args[2];
        
        
        MapDetails details = new MapDetails();
        ArrayList<FromNode> edges  = details.readInitialMap(probPath);
        ArrayList<Vertex> nodesDetails = details.parseEdges(edgesPath);

        System.out.println(nodesDetails.size());
       
        WriteXMLFile writeXMLFile = new WriteXMLFile(finalXmlPath,details.merge(edges, nodesDetails));
        writeXMLFile.writeProbFile();
    }

}

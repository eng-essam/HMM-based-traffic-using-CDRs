/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.util.ArrayList;

import mergexml.NetConstructor;
import utils.Edge;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class WriteVoronoi {

    /**
     * @param args the command line arguments
     *
     *
     * javac -cp .:../diva.jar Voronoi/*.java
     * dir=/home/essam/traffic/Dakar/public
     *
     * java -cp .:../diva.jar \ Voronoi.WriteVoronoi $dir/Dakar.bounded.vor.csv
     * \ $dir/Dakar.bounded.vor.xml $dir/Dakar.xy.dist.xml \
     * $dir/Dakar.xy.new.dist.xml
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String voronoiPath = args[0];
        String edgesPath = args[1];
        String probXml = args[2];
        String newProbXml = args[3];

        /**
         * java -cp .:../diva.jar Voronoi.WriteVoronoi voronoiPath vorXmlPath
         * probXml newProbXml
         */
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        VoronoiConverter converter = new VoronoiConverter();
//        converter.writeVoronoiFile(voronoiPath, vorXmlPath);
//        ArrayList<FromNode> map = converter.DetermineExits(probXml, voronoiPath);
        ArrayList<FromNode> map = converter.determine_edges_cut_zones(probXml, voronoiPath, edges);
//        System.out.println("writing");
        System.out.format("network reading completed with %d node(s)\n", map.size());

        converter.writeProbFile(newProbXml, map);

    }

}

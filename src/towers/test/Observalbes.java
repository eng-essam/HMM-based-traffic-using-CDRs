/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

import java.util.ArrayList;
import java.util.Hashtable;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import towers.ObservablesHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class Observalbes {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String towersPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.towers.csv";
        String vorNeighborsPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.neighborSitesForSite.csv";

        ObservablesHandler obs_handler = new ObservablesHandler();

        Hashtable<Integer, Vertex> towers = obs_handler.readTowers(towersPath);
        Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = obs_handler.readVoronoiNeibors(vorNeighborsPath);

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = obs_handler.construct_zone_graph(towers, voronoi_neighbors);

        String dst = "15";
        String src = "81";
        
        String path = obs_handler.get_weight_path(graph, src, dst);
        System.out.println(path);
        

    }

}

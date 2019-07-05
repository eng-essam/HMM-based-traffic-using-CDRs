/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.jgrapht.DirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import utils.FromNode;
import utils.ToNode;

/**
 *
 * @author essam
 */
public class VConnectivityInspector {

    DirectedGraph<String, DefaultEdge> graph;
    WeightedGraph<String, DefaultWeightedEdge> weighted_graph;
    ConnectivityInspector<String, DefaultEdge> inspector;
    DijkstraShortestPath<String, DefaultEdge> dijkstra;

    public DirectedGraph<String, DefaultEdge> constructMap(ArrayList<FromNode> map) {

        this.graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
            FromNode fromNode = iterator.next();
            graph.addVertex(fromNode.getID());
            ArrayList<ToNode> toNodesList = fromNode.getToedges();
            for (Iterator<ToNode> toIterator = toNodesList.iterator(); toIterator.hasNext();) {
                ToNode toNode = toIterator.next();
                graph.addVertex(toNode.getID());
                graph.addEdge(fromNode.getID(), toNode.getID());
                
//                graph.addEdge(toNode.getID(), fromNode.getID());
            }

        }
//        this.dijkstra = new DijkstraShortestPath<>(graph, "", "");
        this.inspector = new ConnectivityInspector<>(graph);
        return this.graph;
    }

    public double getPathLength(String s, String d) {
        this.dijkstra = new DijkstraShortestPath<>(graph, s, d);
//        return DijkstraShortestPath.findPathBetween(graph, s, d).size();
        return dijkstra.getPathLength();
    }

    public Hashtable<Integer, ArrayList<String>> getRegionExists(ArrayList<FromNode> networkDist) {
        Hashtable<Integer, ArrayList<String>> regionsExists = new Hashtable<>();
        for (Iterator<FromNode> iterator = networkDist.iterator(); iterator.hasNext();) {
            FromNode next = iterator.next();
            int zone = next.getZone();
            if (next.isIsExit()) {
                if (regionsExists.containsKey(zone)) {
//                    System.out.println("find zone");
                    ArrayList<String> exts = regionsExists.get(zone);

                    exts.add(next.getID());
                    regionsExists.replace(zone, exts);
                } else {
//                    System.out.println("not listed zone");
                    ArrayList<String> exts = new ArrayList<>();
                    exts.add(next.getID());
                    regionsExists.put(zone, exts);
                }
            }

        }
        return regionsExists;
    }

    public Hashtable<Integer, ArrayList<String>> getRegionPnts(ArrayList<FromNode> networkDist) {
        Hashtable<Integer, ArrayList<String>> regionsPnt = new Hashtable<>();
        for (Iterator<FromNode> iterator = networkDist.iterator(); iterator.hasNext();) {
            FromNode next = iterator.next();
            int zone = next.getZone();

            if (regionsPnt.containsKey(zone)) {
//                    System.out.println("find zone");
                ArrayList<String> exts = regionsPnt.get(zone);

                exts.add(next.getID());
                regionsPnt.replace(zone, exts);
            } else {
//                    System.out.println("not listed zone");
                ArrayList<String> exts = new ArrayList<>();
                exts.add(next.getID());
                regionsPnt.put(zone, exts);
            }

        }
        return regionsPnt;
    }

    public boolean isConnected(String s, String d) {

        return inspector.pathExists(s, d);
    }

    public Hashtable<Integer, ArrayList<FromNode>> seperateRegions(ArrayList<FromNode> map) {
        Hashtable<Integer, ArrayList<FromNode>> all = new Hashtable<>();
        for (Iterator<FromNode> it = map.iterator(); it.hasNext();) {
            FromNode fromNode = it.next();
            if (all.containsKey(fromNode.getZone())) {
                ArrayList<FromNode> region = all.get(fromNode.getZone());
                region.add(fromNode);
                all.replace(fromNode.getZone(), region);
            } else {
                ArrayList<FromNode> region = new ArrayList<>();
                region.add(fromNode);
                all.put(fromNode.getZone(), region);
            }

        }
        return all;
    }

}

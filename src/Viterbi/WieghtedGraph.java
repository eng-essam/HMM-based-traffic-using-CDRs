/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import utils.FromNode;
import utils.ToNode;

/**
 *
 * @author essam
 */
public class WieghtedGraph {

//    DirectedGraph<String, DefaultEdge> trafficMap;
    SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph;
//    WeightedGraph<String, DefaultWeightedEdge> graph;
//    ConnectivityInspector<String, DefaultEdge> inspector;
    DijkstraShortestPath<String, DefaultEdge> dijkstra;

    /**
     *
     * @param map
     * @return
     */
    public WeightedGraph<String, DefaultWeightedEdge> constructMap(List<FromNode> map) {

//        this.trafficMap = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        this.graph
                = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

//        this.graph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        
        for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
            FromNode fromNode = iterator.next();
            graph.addVertex(fromNode.getID());
            /**
             *
             */
//            trafficMap.addVertex(fromNode.getID());
            ArrayList<ToNode> toNodesList = fromNode.getToedges();
            for (Iterator<ToNode> toIterator = toNodesList.iterator(); toIterator.hasNext();) {
                ToNode toNode = toIterator.next();
                graph.addVertex(toNode.getID());
                DefaultWeightedEdge e1 = null;
                try {
                    e1 = graph.addEdge(fromNode.getID(), toNode.getID());
                } catch (java.lang.IllegalArgumentException e) {
                    System.err.println("Illegal Argument Exception: loops not allowed");
                            
                    continue;
                }
//                if (e1==null) {
//                    e1 = graph.addEdge(toNode.getID(),fromNode.getID());
//                    System.out.println("edge null\t from:\t"+fromNode.getID()+"\tto:\t"+toNode.getID());
//                    continue;
//                }
                graph.setEdgeWeight(e1, eculidean(fromNode.getX(), fromNode.getY(), toNode.getX(), toNode.getY()));

//                DefaultWeightedEdge e2 = graph.addEdge( toNode.getID(), fromNode.getID());
//                graph.setEdgeWeight(e2, eculidean(fromNode.getX(), fromNode.getY(), toNode.getX(), toNode.getY()));
                /**
                 *
                 */
//                trafficMap.addVertex(toNode.getID());
//                trafficMap.addEdge(fromNode.getID(), toNode.getID());
            }

        }
//        this.dijkstra = new DijkstraShortestPath<>(trafficMap, "", "");
//        this.inspector = new ConnectivityInspector<>(trafficMap);
        return this.graph;
    }

    /**
     * Calculate distance between two cartesian points
     *
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @return
     */
    private double eculidean(double x, double y, double x1, double y1) {
        return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
    }

    public double getPathLength(String s, String d) {
        this.dijkstra = new DijkstraShortestPath(graph, s, d);
//        List<DefaultEdge> list = dijkstra.getPathEdgeList();
//        for (Iterator<DefaultEdge> iterator = list.iterator(); iterator.hasNext();) {
//            DefaultEdge next = iterator.next();
//            System.out.println(next.toString());
//            
//        }
        return dijkstra.getPathLength();
    }

    public double getPathLength(String s, String d, double threshold) {
        this.dijkstra = new DijkstraShortestPath(graph, s, d, threshold);
//        List<DefaultEdge> list = dijkstra.getPathEdgeList();
//        for (Iterator<DefaultEdge> iterator = list.iterator(); iterator.hasNext();) {
//            DefaultEdge next = iterator.next();
//            System.out.println(next.toString());
//            
//        }
        return dijkstra.getPathLength();
    }

    /**
     * return the shortest path ...
     * @param s
     * @param d
     * @param threshold
     * @return
     */
    public List<String> getPath(String s, String d, double threshold) {
//    	System.out.println(DijkstraShortestPath.findPathBetween(graph, s, d));
        this.dijkstra = new DijkstraShortestPath(graph, s, d, threshold);
        List<String> path = new ArrayList<>();
        path.add(s);
        List<DefaultEdge> list = dijkstra.getPathEdgeList();
        for (Iterator<DefaultEdge> iterator = list.iterator(); iterator.hasNext();) {
            String trgt = iterator.next().getTarget().toString();
            path.add(trgt);
        }
        return path;
    }

//    public boolean isConnected(String s, String d) {
//
//        return inspector.pathExists(s, d);
//    }

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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import towers.test.PlotTowersDAG;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class ObservablesHandler {

    Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors;
    DirectedGraph<String, DefaultEdge> dgraph;
    ConnectivityInspector<String, DefaultEdge> inspector;
    DijkstraShortestPath<String, DefaultEdge> dijkstra;
    Hashtable<Integer, Vertex> tower;
    String CLM = ",";

    public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> construct_zone_graph(
            Hashtable<Integer, Vertex> towers,
            Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors) {

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoi_neighbors.entrySet()) {
            String twr_id = Integer.toString(entrySet.getKey());
            ArrayList<Integer> twr_nghbrs = entrySet.getValue();
            Vertex twr_id_vertex = towers.get(Integer.parseInt(twr_id));
            /**
             * If the graph doesn't contain this vertex add it
             */
            if (!graph.containsVertex(twr_id)) {
                graph.addVertex(twr_id);
            }

            for (Iterator<Integer> iterator = twr_nghbrs.iterator(); iterator.hasNext();) {
                String to_twr = Integer.toString(iterator.next());
                Vertex to_twr_vertex = towers.get(Integer.parseInt(to_twr));

                /**
                 * If the graph doesn't contain this vertex add it
                 */
                if (!graph.containsVertex(to_twr)) {
                    graph.addVertex(to_twr);
                }

                // add weight
                DefaultWeightedEdge e1 = graph.addEdge(twr_id, to_twr);
                graph.setEdgeWeight(e1, getDistance(twr_id_vertex.getX(), twr_id_vertex.getY(), to_twr_vertex.getX(), to_twr_vertex.getY()));
            }

        }

        return graph;
    }

    public DirectedGraph<String, DefaultEdge> constructMap() {

        this.dgraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoi_neighbors.entrySet()) {
            Integer fromKey = entrySet.getKey();
            ArrayList<Integer> value = entrySet.getValue();
            dgraph.addVertex(String.valueOf(fromKey));
            for (Iterator<Integer> iterator = value.iterator(); iterator.hasNext();) {
                Integer toKey = iterator.next();
                dgraph.addVertex(String.valueOf(toKey));
                dgraph.addEdge(String.valueOf(fromKey), String.valueOf(toKey));
//                trafficMap.addEdge(toNode.getID(), fromNode.getID());
            }
        }
//        this.dijkstra = new DijkstraShortestPath<>(trafficMap, "", "");
        this.inspector = new ConnectivityInspector<>(dgraph);
        return this.dgraph;
    }

    /**
     * Find the shortest path in the
     *
     * @param src
     * @param dst
     * @return Comma separator sequence of observations.
     */
    public String get_weight_path(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph,
            String src, String dst) {
        String path = "";
        List<DefaultWeightedEdge> WeightedEdgePath = DijkstraShortestPath.findPathBetween(graph, src, dst);

        for (DefaultWeightedEdge next : WeightedEdgePath) {
            path += graph.getEdgeSource(next).toString() + CLM;
        }
        path += dst;

        return path;
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
    private double getDistance(double x, double y, double x1, double y1) {
        return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
    }

    public GraphPath<String, DefaultEdge> getPath(String s, String d) {
        this.dijkstra = new DijkstraShortestPath<>(dgraph, s, d);
//        return DijkstraShortestPath.findPathBetween(trafficMap, s, d).size();
        return dijkstra.getPath();
    }

    public ArrayList<String> handleObrSeq(String prv, String cur) {

        ArrayList<String> usrOb = new ArrayList<>();
//        System.out.printf("----> %s, %s\n", prv, cur);
        GraphPath<String, DefaultEdge> path = getPath(prv, cur);
        List<DefaultEdge> edges = path.getEdgeList();
        String src, dest = null;
        for (Iterator<DefaultEdge> iterator = edges.iterator(); iterator.hasNext();) {
            DefaultEdge edge = iterator.next();
            src = dgraph.getEdgeSource(edge);
            dest = dgraph.getEdgeTarget(edge);
//            System.out.printf("%s -> %s,", src,dest);
//            trafficMap.getEdgeTarget(edge);
            usrOb.add(src);

        }
        usrOb.add(dest);
//        System.out.println("");
        return usrOb;
    }

    public Hashtable<Integer, Vertex> readTowers(String path) {
        Hashtable<Integer, Vertex> towers = new Hashtable<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            Vertex pnt = null;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");
                pnt = new Vertex();
                int index = Integer.parseInt(lineSplit[0]);
                pnt.setX(Double.parseDouble(lineSplit[1]));
                pnt.setY(Double.parseDouble(lineSplit[2]));
//                System.out.printf("%f,%f",pnt.x,pnt.y);
                towers.put(index, pnt);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return towers;
    }

    public Hashtable<Integer, ArrayList<Integer>> readVoronoiNeibors(String path) {
        Hashtable<Integer, ArrayList<Integer>> neighbours = new Hashtable<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            int id = 0;

            while ((line = reader.readLine()) != null) {
                ArrayList<Integer> tmp = new ArrayList<>();
                String lineSplit[] = line.split(",");

                if (!line.startsWith(",")) {
                    id = Integer.parseInt(lineSplit[0].trim());
                } else {
                    for (String lineSplit1 : lineSplit) {
                        String toZone = lineSplit1.trim();
                        if (toZone.equals("") || toZone == null) {
                            continue;
                        }
                        tmp.add(Integer.parseInt(toZone));
                    }
                }
                neighbours.put(id, tmp);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return neighbours;
    }

}

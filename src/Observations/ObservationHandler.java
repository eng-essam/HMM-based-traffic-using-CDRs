/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author essam
 */
public class ObservationHandler {

    /**
     * Remove all regions that do not belong to the current area.
     * @param obs
     * @param regs
     * @return 
     */
    public static ArrayList<ArrayList<String>> handleObs(String[] obs, ArrayList<Integer> regs) {
        ArrayList<ArrayList<String>> observations = new ArrayList<>();
        ArrayList<String> subOb = new ArrayList<>();
        for (int i = 0; i < obs.length; i++) {
            int zone = Integer.parseInt(obs[i]);
            
            if (regs.contains(zone)) {
                subOb.add(String.valueOf(zone));
                if (subOb.size()>=25) {
                    observations.add(subOb);
                    subOb = new ArrayList<>();
                }
            } else {
                if (!subOb.isEmpty()) {
                    observations.add(subOb);
                    subOb = new ArrayList<>();
                }
            }

        }
        if (!subOb.isEmpty()) {
            observations.add(subOb);
        }
        return observations;
    }
    /**
     * Remove all regions that do not belong to the current area.
     * @param obs
     * @param regs
     * @return 
     */
    public static ArrayList<ArrayList<String>> handleObs(String[] obs, int towers) {
        ArrayList<ArrayList<String>> observations = new ArrayList<>();
        ArrayList<String> subOb = new ArrayList<>();
        for (int i = 0; i < obs.length; i++) {
            int zone = Integer.parseInt(obs[i]);
            /**
             * 
             */
//            if (regs.contains(zone)) {
            if (zone<=towers) {
                subOb.add(String.valueOf(zone));
            } else {
                if (!subOb.isEmpty()) {
                    observations.add(subOb);
                    subOb = new ArrayList<>();
                }
            }

        }
        if (!subOb.isEmpty()) {
            observations.add(subOb);
        }
        return observations;
    }
    Hashtable<Integer, ArrayList<Integer>> voronoiNeibors;
    DirectedGraph<String, DefaultEdge> trafficMap;

    ConnectivityInspector<String, DefaultEdge> inspector;

    DijkstraShortestPath<String, DefaultEdge> dijkstra;

    public ObservationHandler(Hashtable<Integer, ArrayList<Integer>> voronoiNeibors) {
        this.voronoiNeibors = voronoiNeibors;
        constructMap();
    }

    public DirectedGraph<String, DefaultEdge> constructMap() {

        this.trafficMap = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
            Integer fromKey = entrySet.getKey();
            ArrayList<Integer> value = entrySet.getValue();
            trafficMap.addVertex(String.valueOf(fromKey));
            for (Iterator<Integer> iterator = value.iterator(); iterator.hasNext();) {
                Integer toKey = iterator.next();
                trafficMap.addVertex(String.valueOf(toKey));
                trafficMap.addEdge(String.valueOf(fromKey), String.valueOf(toKey));
//                trafficMap.addEdge(toNode.getID(), fromNode.getID());
            }
        }
//        this.dijkstra = new DijkstraShortestPath<>(trafficMap, "", "");
        this.inspector = new ConnectivityInspector<>(trafficMap);
        return this.trafficMap;
    }

    public GraphPath<String, DefaultEdge> getPath(String s, String d) {
        this.dijkstra = new DijkstraShortestPath<>(trafficMap, s, d);
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
            src = trafficMap.getEdgeSource(edge);
            dest = trafficMap.getEdgeTarget(edge);
//            System.out.printf("%s -> %s,", src,dest);
//            trafficMap.getEdgeTarget(edge);
            usrOb.add(src);

        }
        usrOb.add(dest);
//        System.out.println("");
        return usrOb;
    }
    
    public ArrayList<ArrayList<String>> interpolateObs(String[] obs) {
        ArrayList<ArrayList<String>> all = new ArrayList<>();

        ArrayList<String> fUsrObs = new ArrayList<>();
        String cur = null, prv = null;
        if (obs.length == 0) {
            return null;
        }

//        System.out.println("voronoiNeibors\t"+voronoiNeibors.size());
        for (int i = 0; i < obs.length - 1; i++) {
            cur = obs[i];
//            System.out.println(cur);
            if (voronoiNeibors.containsKey(Integer.parseInt(cur))) {
//                System.out.println("contian cur");
                if (i == 0 || fUsrObs.isEmpty() || voronoiNeibors.get(Integer.parseInt(obs[i - 1])).contains(Integer.parseInt(cur))) {
//                    System.out.println("add");
                    fUsrObs.add(cur);
                } else {
                    if (!fUsrObs.isEmpty()) {
                        all.add(fUsrObs);
                        fUsrObs = new ArrayList<>();
                    }

                    ArrayList<String> tmpUsrOb = handleObrSeq(prv, cur);
                    if (tmpUsrOb.size() > 0) {

                        all.add(tmpUsrOb);
                    }
                }
            } else {
//                System.out.println("in else");
                if (!fUsrObs.isEmpty()) {
                    all.add(fUsrObs);
                    fUsrObs = new ArrayList<>();
                }
//                i++;
            }

//            if (fUsrObs.size() >= 5) {
//                all.add(fUsrObs);
//                fUsrObs = new ArrayList<>();
//            }
        }

        if (!fUsrObs.isEmpty()) {
            all.add(fUsrObs);
            fUsrObs.clear();
        }

        return all;
    }

    public ArrayList<ArrayList<String>> interpolateObs2(String[] obs) {
        ArrayList<ArrayList<String>> all = new ArrayList<>();

        ArrayList<String> fUsrObs = new ArrayList<>();
        String cur = null, prv = null;
        if (obs.length == 0) {
            return null;
        } else if (fUsrObs.isEmpty() && obs.length - 1 > 0) {
            if (!voronoiNeibors.containsKey(Integer.parseInt(obs[0]))) {
                return interpolateObs(Arrays.copyOfRange(obs, 1, obs.length - 1));
            }
            fUsrObs.add(obs[0]);
            cur = prv = obs[0];
        }

        for (int i = 1; i < obs.length - 1; i++) {
            cur = obs[i];
            /**
             * if the current is in the neighbors of the previous add to
             * observations, else get the shortest path from the previous to the
             * current.
             */
            if (!voronoiNeibors.containsKey(Integer.parseInt(cur)) || !voronoiNeibors.containsKey(Integer.parseInt(prv)) || fUsrObs.size() >= 5) {
                if (!fUsrObs.isEmpty()) {
                    all.add(fUsrObs);
                    fUsrObs = new ArrayList<>();
                }
                i++;
                prv = obs[i];
                continue;
            }

            if (voronoiNeibors.get(Integer.parseInt(prv)).contains(Integer.parseInt(cur))) {
                fUsrObs.add(cur);
                prv = cur;
            } else {
                ArrayList<String> tmpUsrOb = handleObrSeq(prv, cur);
                if (tmpUsrOb.size() > 0) {
                    prv = tmpUsrOb.get(tmpUsrOb.size() - 1);
                    fUsrObs.addAll(tmpUsrOb);
                }
            }

        }
        return all;
    }

}

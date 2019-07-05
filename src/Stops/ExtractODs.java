/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Stops;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.FromNode;
import validation.Vit2ODs;

/**
 *
 * @author essam
 */
public class ExtractODs {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        String vitPath = "/home/essam/traffic/SET2/days/";
        String map_path = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.w.vor.xml";

        /**
         * For the complete day timing
         */
        double from = 0.00;
        double to = 24.00 * 60 * 60;

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vitPath));
        Vit2ODs v2od = new Vit2ODs();
        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(map_path);
        Hashtable<Integer, ArrayList<String>> nodes = v2od.generateNodesMap(map);
//        System.out.println(nodes.size()+"\t"+nodes.get(295).size());
        int count = 1;
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            String path = subsetPath.substring(0, subsetPath.lastIndexOf('.') + 1) + "trips.xml";
//                    subsetPath.replace("viterbi.day.", "trips.day.").replace(".CSV.", "_");
            System.out.println(path);
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);

//            v2od.writeTrips(v2od.handleFlow(obs), path, from, to);
//            v2od.writeTripsWDepart(v2od.handleMFlowWDepart(obs), path);
            /**
             * Thu 23 Apr 2015 01:06:49 PM JST Write OD without removing the
             * interpolation parts for the road network segments.
             */
//            v2od.writeTripsWDepart(v2od.handleFlowWDepart(obs), path);
            /**
             * Thu 23 Apr 2015 01:06:49 PM JST write OD without interpolations.
             */
            v2od.writeTripsWDepart(v2od.hFWDepart_zones_edges(obs, nodes), path);
//            break;
        }

    }

}

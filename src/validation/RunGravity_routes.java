/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunGravity_routes {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String vitPath = args[0];
//        String mapPath = args[1];
        String vitPath = "/home/essam/traffic/SET2.1/Viterbi";
        String mapPath = "/home/essam/traffic/Dakar/R_Dakar/Dakar.xy.dist.vor.xml";
        String routes = "/home/essam/traffic/filtered_trips/without_training/tmp_f/tmp/routes";

        final double threshold = 400f;
        final int trip_length = 3;

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);
//        Hashtable<Integer, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vitPath);
//        GravityModel model = new GravityModel();
//        model.handleFlow(obs);
//        model.calcDistances(map);
//        String path = vitPath.substring(0, vitPath.lastIndexOf(".")) + ".gravity.xml";
//        model.computeGOD();
//        model.writeFlow(path);

        int count = 1;
        GravityModel model = new GravityModel();
        Vit2ODs vod = new Vit2ODs();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(routes));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
//            if (fileName.startsWith(".") || !fileName.contains(".trips.")) {
//                continue;
//            }

            ArrayList<String> dua_routes = vod.readRoutes(subsetPath);
            
            String fgodPath = subsetPath.substring(0, subsetPath.lastIndexOf("/") + 1) + "fgod." + threshold + ".threshold."
                    + count++ + "-days.csv";
//            Hashtable<Integer, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);

            model.handleFlow(dua_routes);
            model.calcDistances(map);

            Hashtable<Double, ArrayList<Double>> fgod = model.computeGOD(threshold,false);
            model.writeFGOD(fgodPath, fgod);
            String path = subsetPath.replace(".trips.", ".trips.gravity.");
//                    subsetPath.substring(0, subsetPath.lastIndexOf(".")) + ".gravity.xml";
            model.writeFlow(path);
            fgodPath = subsetPath.substring(0, subsetPath.lastIndexOf("/") + 1) + "avg.fgod." + threshold + ".threshold." + trip_length + ".trip.length.day.csv";
            model.writeAvgGOD(fgodPath, model.avgGOD(fgod));
        }
    }

}

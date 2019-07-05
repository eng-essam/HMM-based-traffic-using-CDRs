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
import java.util.List;
import java.util.Map;

import Observations.ObsTripsBuilder;
import mergexml.MapDetails;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class RunGravity_Zones_Trips {

    public static Hashtable<Double, ArrayList<Double>> limit_flow(Hashtable<Double, ArrayList<Double>> flow, double limit) {
        Hashtable<Double, ArrayList<Double>> limited = new Hashtable<>();
        for (Map.Entry<Double, ArrayList<Double>> entrySet : flow.entrySet()) {
            double key = entrySet.getKey();
            ArrayList<Double> value = entrySet.getValue();
            if (key <= limit) {
                limited.put(key, value);
            }
        }
        return limited;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String trips_path = args[0];
//        String mapPath = args[1];
        String towersPath = args[1];
//        String vitPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Results/without_repeated_obs/updates/Viterbi/transitions-30";
//        String mapPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.vor.xml";
//        String towersPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.towers.csv";

        final double threshold = 1500f;
//        double flow_limit = 4;
        boolean verbose = false;
        boolean intercept = true;
//        final int trip_length = 3;

//        AdaptData adaptor = new AdaptData();
//        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);
        MapDetails details = new MapDetails();
        Hashtable<Integer, Vertex> towers = details.readTowers(towersPath);
//        System.out.println("towers\t"+towers.size());
        GravityModel model = new GravityModel();
//        Hashtable<String, Integer> nodesMap = model.generateNodesMap(map);
        int days_cnt = 0;

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(trips_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".") || fileName.contains("Sunday") /**
                     * || fileName.contains("Friday")
                     */
                    || fileName.contains("Saturday")) {
                continue;
            }

//            String day = subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") + "viterbi.day.".length(), subsetPath.lastIndexOf("."));
//            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);
//            model = new GravityModel();
//            model.setDistances(distances);
//            System.out.println("distances\t"+distances.size());
            List<String[]> routes = DataHandler.read_routes(subsetPath);
            model.handle_zones_Flow(routes);
            days_cnt++;

        }

//        model.mange_zones_flow(nodesMap);
//        Hashtable<String, Hashtable<String, Double>> distances = new Hashtable<>();
//
//        model.setDistances(distances);
        model.calcDistances(towers);
//        model.calcDistances(map);
        /**
         * Avg flow
         */
//        model.average_flow(days_cnt);

        Hashtable<Double, ArrayList<Double>> fgod;

        if (verbose) {
            /**
             * Print verbose flow data
             */
            System.out.println("-------------------------------------------------");
            System.out.println("Number of days" + days_cnt);
            System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "From-zone", "To-zone", "Dist", "Flow", "Outs", "Ins", "GOD");
            fgod = model.computeGOD(threshold, verbose);
            System.out.println("-------------------------------------------------");
        } else {
            fgod = model.computeGOD(threshold, verbose);
        }
        String fgod_csv_path = trips_path + "/data";
        File dir = new File(fgod_csv_path);
        dir.mkdir();

        String fgod_path = fgod_csv_path + "/fgod." + days_cnt + ".days.csv";
        model.writeFGOD(fgod_path, fgod);
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
        fgod_path = fgod_csv_path + "/avg.fgod." + days_cnt + ".days.csv";
        model.writeAvgGOD(fgod_path, avgGOD);
        System.out.printf("Number of days %d\tGravity vs. flow R^2=%f\tAvg Gravity vs. flow R^2=%f\n", days_cnt, model.find_all_linear_regression(fgod, intercept), model.find_avg_linear_regression(avgGOD, intercept));

    }
}

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

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunAvgDailyGravity {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String vitPath = args[0];
//        String mapPath = args[1];
        String vitPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Results/updates/Viterbi/transitions-00/";
        String mapPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.w.vor.xml";
        final double threshold = 1000f;
//        double flow_limit = 4;
//        final int trip_length = 3;
        /**
         * Output flags.
         */
        final boolean intercept = true;
        final boolean verbose = false;

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);

        int days_cnt = 0;
        GravityModel model = new GravityModel();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vitPath));

        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".") || !fileName.contains("viterbi.day.")) {
                continue;
            }
//            System.out.println(subsetPath);
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);
            model.handleFlow(obs);
            days_cnt++;
        }
        model.calcDistances(map);
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
        String fgod_csv_path = vitPath + "/data";
        File dir = new File(fgod_csv_path);
        dir.mkdir();

        String fgod_path = fgod_csv_path + "/fgod.edges." + days_cnt + ".days.csv";
        model.writeFGOD(fgod_path, fgod);
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
        fgod_path = fgod_csv_path + "/avg.fgod.edges." + days_cnt + ".days.csv";
        model.writeAvgGOD(fgod_path, avgGOD);
        System.out.printf("Number of days %d\tGravity vs. flow R^2=%f\tAvg Gravity vs. flow R^2=%f\n", days_cnt, model.find_all_linear_regression(fgod, intercept), model.find_avg_linear_regression(avgGOD, intercept));

    }

}

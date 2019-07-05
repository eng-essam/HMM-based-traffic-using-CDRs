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
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunGravity_without_interpolation {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String vitPath = args[0];
//        String mapPath = args[1];
        String vitPath = "/home/essam/traffic/Dakar/Dakar_edge-200-old/Results/without_repeated_obs/alpha-0.8_beta-.2/transitions-00";
        String mapPath = "/home/essam/traffic/Dakar/Dakar_edge-200-old/Dakar.xy.dist.w.vor.xml";
        final double threshold = 200f;
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

        ArrayList<Hashtable<Double, ArrayList<Double>>> all_fgod = new ArrayList<>();
        ArrayList<Hashtable<Double, ArrayList<Double>>> all_avg_fgod = new ArrayList<>();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vitPath));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".") || !fileName.contains("viterbi.day.")) {
                continue;
            }
            String day = subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") + "viterbi.day.".length(), subsetPath.lastIndexOf("."));
            String fgodPath = subsetPath.substring(0, subsetPath.lastIndexOf("/") + 1) + "fgod." + threshold + ".threshold." + trip_length + ".trip.length.day."
                    + day + ".csv";
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);
            GravityModel model = new GravityModel();
            model.handleFlow(obs);
            model.calcDistances(map);

            Hashtable<Double, ArrayList<Double>> fgod = model.computeGOD(threshold,false);
            all_fgod.add(fgod);
//            model.writeFGOD(fgodPath, fgod);

//            fgodPath = subsetPath.substring(0, subsetPath.lastIndexOf("/") + 1) + "avg.fgod." + threshold + ".threshold." + trip_length + ".trip.length.day."
//                    + day + ".csv";
            Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
            all_avg_fgod.add(avgGOD);
//            model.writeAvgGOD(fgodPath, avgGOD);
//            System.out.println("=================================================");

//            System.out.println("All values\t" + model.find_all_linear_regression(fgod));
//            System.out.println("Average values\t" + model.find_avg_linear_regression(avgGOD));
//            System.out.println("=================================================");
//            String path = subsetPath.replace("viterbi.day.", "gravity.day.");
////                    subsetPath.substring(0, subsetPath.lastIndexOf(".")) + ".gravity.xml";
//            model.writeFlow(path);
        }
        GravityModel model = new GravityModel();
        System.out.printf("%f,%f\n", model.find_all_linear_regression(reduce(all_fgod),false), model.find_avg_linear_regression(reduce(all_avg_fgod),false));
    }

    public static Hashtable<Double, ArrayList<Double>> reduce(ArrayList<Hashtable<Double, ArrayList<Double>>> fgod) {
        Hashtable<Double, ArrayList<Double>> comp = new Hashtable<>();

        for (Iterator<Hashtable<Double, ArrayList<Double>>> iterator = fgod.iterator(); iterator.hasNext();) {
            Hashtable<Double, ArrayList<Double>> next = iterator.next();
            for (Map.Entry<Double, ArrayList<Double>> entrySet : next.entrySet()) {
                Double key = entrySet.getKey();
                ArrayList<Double> value = entrySet.getValue();
                if (comp.containsKey(key)) {
                    ArrayList<Double> comp_val = comp.get(key);
                    comp_val.addAll(value);
                    comp.put(key, comp_val);
                } else {
                    comp.put(key, value);
                }

            }

        }
        return comp;
    }

}

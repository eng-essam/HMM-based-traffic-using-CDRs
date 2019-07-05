/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Density.Density;
import Density.Plot;
import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.NetConstructor;
import utils.Edge;
import utils.StdDraw;

/**
 *
 * @author essam
 */
public class DailyAvgRoutes2Density {

    public static Hashtable<String, Double> get_avg(Hashtable<String, Double> table, int count) {
        Hashtable<String, Double> avg_tab = new Hashtable<>();
        for (Map.Entry<String, Double> entrySet : table.entrySet()) {
            String key = entrySet.getKey();
            double value = entrySet.getValue();
            avg_tab.put(key, value / count);
        }
        return avg_tab;
    }

    public static ArrayList<String> get_edges(String path) {
        Vit2ODs vod = new Vit2ODs();
        ArrayList<String> edges = new ArrayList<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);

            ArrayList<String> unique_edges = vod.get_edges(obs);
            for (Iterator<String> iterator1 = unique_edges.iterator(); iterator1.hasNext();) {
                String edge = iterator1.next();
                if (!edges.contains(edge)) {
                    edges.add(edge);
                }

            }
        }
        return edges;
    }

    public static Hashtable<String, Double> getAccDensities(String path, boolean dua_flag) {
        /**
         * Find accumulated densities of the UE
         */
        Vit2ODs vod = new Vit2ODs();
        RoutesDensity dh = new RoutesDensity();

        Hashtable<String, Double> acc_table = new Hashtable<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
//            System.out.println(fileName);
//            if (fileName.contains("Sunday") || fileName.contains("Saturday")) {
//                continue;
//            }
            Hashtable<String, Double> acc_daily_density;
            if (dua_flag) {
                if (fileName.startsWith(".") || !fileName.endsWith(".xml")) {
                    continue;
                }
                Hashtable<String, Hashtable<String, Double>> daily_density = dh.getRoutesDensity(vod.readRoutes(subsetPath));
                acc_daily_density = dh.accumulate(daily_density);
            } else {
                if (fileName.startsWith(".") || !fileName.contains("viterbi.day.")) {
                    continue;
                }
                Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);
                Hashtable<String, Hashtable<String, Double>> vit_density = dh.getVitDensity(obs);
                acc_daily_density = dh.remove_interpolation(dh.accumulate(vit_density));
            }

            for (Map.Entry<String, Double> entrySet : acc_daily_density.entrySet()) {
                String key = entrySet.getKey();
                Double value = entrySet.getValue();
                if (acc_table.containsKey(key)) {
                    acc_table.replace(key, acc_table.get(key) + value);
                } else {
                    acc_table.put(key, value);
                }

            }

        }
        return acc_table;
    }

    public static double getMax(Hashtable<String, Double> table) {
        double max = Double.MIN_VALUE;
        for (Map.Entry<String, Double> entrySet : table.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            if (value > max) {
                max = value;
            }

        }
        return max;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
//        String home = "/home/essam/traffic/Dakar/Dakar_edge-200/Results/without_repeated_obs/ue/transitions-00/";
//        String routes = "/home/essam/traffic/Dakar/Dakar_edge-200/Results/without_repeated_obs/ue/transitions-00/dua/";
//        String vit_out = "/home/essam/traffic/Dakar/Dakar_edge-200/Results/without_repeated_obs/ue/transitions-00/vit/";
//        String edgesPath = "/home/essam/traffic/Dakar/Dakar_edge-200/edges.xml";
//        String map = "/home/essam/traffic/Dakar/Dakar_edge-200/map";
        
        String home = args[0];
        String routes = args[1];
        String vit_out = args[2];
        String edgesPath = args[3];
        String map = args[4];
        
        int days = 28;
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1619000.93;
        ymax = 1635128.30;
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

        Hashtable<String, Double> dua_acc_den = get_avg(getAccDensities(routes, true), days);
        Hashtable<String, Double> vit_acc_den = get_avg(getAccDensities(vit_out, false), days);

        
        if (false) {
//        System.out.println("Max:\t"+getMax(vit_acc_den)+"\t"+getMax(dua_acc_den));
            /**
             * Plot DUA
             */
            Plot ploter = new Plot(edges, routes + ".png");
            ploter.scale(xmin, ymin, xmax, ymax);
            StdDraw.setPenRadius(0.002);
            Density densityHandler = new Density();
            densityHandler.plot_acc_Density(edges, map, vit_acc_den, home + "/accumulate.vit.png", true, "", "", 500);

            /**
             * Plot Viterbi
             */
            ploter = new Plot(edges, routes + ".png");
            ploter.scale(xmin, ymin, xmax, ymax);
            StdDraw.setPenRadius(0.002);
            densityHandler.plot_acc_Density(edges, map, dua_acc_den, home + "/accumulate.dua.png", true, "", "", 500);
        }
//        System.out.println("dua_acc_den\t" + dua_acc_den.size());
//        System.out.println("vit_acc_den\t" + vit_acc_den.size());

        /**
         * Calculate the average number of vehicles.
         */
        ArrayList<String> unique_edges = get_edges(vit_out);

        double vit_avg = 0, dua_avg = 0, count = 0;
        for (Iterator<String> iterator = unique_edges.iterator(); iterator.hasNext();) {
            String edge = iterator.next();
            if (dua_acc_den.containsKey(edge) && vit_acc_den.containsKey(edge)) {
                vit_avg += vit_acc_den.get(edge);
                dua_avg += dua_acc_den.get(edge);
                count++;
            }

        }

//        System.out.printf("%s,%f\t%s,%f\n", "Vit avg:", vit_avg / count, "DUA avg:", dua_avg / count);
        /**
         * print the edge name , viterbi accumulated number of vehicles, User
         * equilibrium accumulated number of vehicles.
         */
        Hashtable<Double, ArrayList<Double>> densities = new Hashtable<>();

//        System.out.printf("%s,%s,%s\n", "edge_name", "Viterbi_acc_density", "User_equilibrium_acc_densities");
        int skiped = 0;
        for (Iterator<String> iterator = unique_edges.iterator(); iterator.hasNext();) {
            String edge = iterator.next();
            if (dua_acc_den.containsKey(edge) && vit_acc_den.containsKey(edge)) {
                double vit_val = vit_acc_den.get(edge);
                double dua_val = dua_acc_den.get(edge);

                if (densities.containsKey(vit_acc_den.get(edge))) {
                    ArrayList<Double> vals = densities.get(vit_acc_den.get(edge));
                    vals.add(dua_acc_den.get(edge));
                    densities.replace(vit_acc_den.get(edge), vals);

                } else {
                    ArrayList<Double> vals = new ArrayList<>();
                    vals.add(dua_acc_den.get(edge));
                    densities.put(vit_acc_den.get(edge), vals);
                }
            }

        }

//        densityHandler.mark_edges(edges, map, skipped_edges, vit_out + ".skipped.png", true);
        
//        System.out.printf("The number of edges is %f and the number of edges with difference greater the user equilibrium average(%f), while the viterbi average (%f), is:%d", count, dua_avg / count, vit_avg / count, skiped);
        
//        System.out.println(dh.getRSE(dh.accumulate(vit_density), dh.accumulate(dua_density)));
//
        GravityModel model = new GravityModel();
        String path = home + "/avgGOD.csv";
        model.writeFGOD(home + "/all.val.csv", densities);
//        model.writeAvgGOD(path, model.avgGOD(dh.getdensities(dh.accumulate(vit_density), dh.accumulate(dua_density))));
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(densities);
        System.out.printf("\n## %f,%f", model.find_all_linear_regression(densities, true), model.find_avg_linear_regression(avgGOD, true));
        model.writeAvgGOD(path, avgGOD);
    }
}

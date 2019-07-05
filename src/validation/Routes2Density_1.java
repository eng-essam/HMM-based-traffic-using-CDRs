/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

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
public class Routes2Density_1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        String home = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/results";
        String routes = home + "/routes/routes.day.2013-01-08_Tuesday.xml";

//                = "/home/essam/traffic/SET2.3.3/SET2_P02.trips.day.21/SET2_P02.trips._009.rou.xml";
        String vit_out = home + "/viterbi.day.2013-01-08_Tuesday.xml";
//                = "/home/essam/traffic/SET2.3.3/SET2_P02.viterbi.day.21.xml";

        String edgesPath = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/edges.xml";
        String map = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/map";

        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1619000.93;
        ymax = 1635128.30;
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        ArrayList<String> skipped_edges = new ArrayList<>();

        Vit2ODs vod = new Vit2ODs();
        RoutesDensity dh = new RoutesDensity();
        Hashtable<String, Hashtable<String, Double>> dua_density = dh.getRoutesDensity(vod.readRoutes(routes));
        Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_out);

        /**
         * get the list of edges to compare different models by using it.
         */
//        ArrayList<String> unique_edges = vod.get_unique_edges(obs);
        /**
         * find the correlation against all edges.
         */
        ArrayList<String> unique_edges = vod.get_edges(obs);

        Hashtable<String, Hashtable<String, Double>> vit_density = dh.getVitDensity(obs);

        System.out.println("----------------------------");
//        System.out.println(TransBuilder.getError2(vit_density, dua_density));
        Hashtable<String, Double> dua_acc_den = dh.accumulate(dua_density);
        /**
         * get the max
         */
        double max = Double.MIN_VALUE;
        for (Map.Entry<String, Double> entrySet : dua_acc_den.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            if (value > max) {
                max = value;
            }

        }
        System.out.println("max\t" + max);
        /**
         *
         */
        Plot ploter = new Plot(edges, routes + ".png");
        ploter.scale(xmin, ymin, xmax, ymax);
        StdDraw.setPenRadius(0.002);
        Density densityHandler = new Density();
        densityHandler.plot_acc_Density(edges, map, dua_acc_den, routes + ".png", true, "", "", 2000);

        Hashtable<String, Double> vit_acc_den = dh.remove_interpolation(dh.accumulate(vit_density));
        max = Double.MIN_VALUE;
        for (Map.Entry<String, Double> entrySet : vit_acc_den.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            if (value > max) {
                max = value;
            }

        }
        System.out.println("max\t" + max);

        /**
         *
         */
        ploter = new Plot(edges, vit_out + ".png");
        ploter.scale(xmin, ymin, xmax, ymax);
        StdDraw.setPenRadius(0.002);
        densityHandler = new Density();
        densityHandler.plot_acc_Density(edges, map, vit_acc_den, vit_out + ".png", true, "", "", 2000);

        System.out.println("dua_acc_den\t" + dua_acc_den.size());
        System.out.println("vit_acc_den\t" + vit_acc_den.size());

        /**
         * Calculate the average number of vehicles.
         */
        double vit_avg = 0, dua_avg = 0, count = 0;
        for (Iterator<String> iterator = unique_edges.iterator(); iterator.hasNext();) {
            String edge = iterator.next();
            if (dua_acc_den.containsKey(edge) && vit_acc_den.containsKey(edge)) {
                vit_avg += vit_acc_den.get(edge);
                dua_avg += dua_acc_den.get(edge);
                count++;
            }

        }

        System.out.printf("%s,%f\t%s,%f\n", "Vit avg:", vit_avg / count, "DUA avg:", dua_avg / count);
        /**
         * print the edge name , viterbi accumulated number of vehicles, User
         * equilibrium accumulated number of vehicles.
         */
        Hashtable<Double, ArrayList<Double>> densities = new Hashtable<>();

        System.out.printf("%s,%s,%s\n", "edge_name", "Viterbi_acc_density", "User_equilibrium_acc_densities");
        int skiped = 0;
        for (Iterator<String> iterator = unique_edges.iterator(); iterator.hasNext();) {
            String edge = iterator.next();
            if (dua_acc_den.containsKey(edge) && vit_acc_den.containsKey(edge)) {
                double vit_val = vit_acc_den.get(edge);
                double dua_val = dua_acc_den.get(edge);

//                if (Math.abs(vit_val - dua_val) <= 2 * (vit_avg / count)) {
                if (densities.containsKey(vit_acc_den.get(edge))) {
                    ArrayList<Double> vals = densities.get(vit_acc_den.get(edge));
                    vals.add(dua_acc_den.get(edge));
                    densities.replace(vit_acc_den.get(edge), vals);

                } else {
                    ArrayList<Double> vals = new ArrayList<>();
                    vals.add(dua_acc_den.get(edge));
                    densities.put(vit_acc_den.get(edge), vals);
                }
                System.out.printf("%s,%f,%f\n", edge, vit_acc_den.get(edge), dua_acc_den.get(edge));

//                } else {
//                    skipped_edges.add(edge);
//                    skiped++;
//                }
            }

        }

        densityHandler.mark_edges(edges, map, skipped_edges, vit_out + ".skipped.png", true);
        System.out.printf("The number of edges is %f and the number of edges with difference greater the user equilibrium average(%f), while the viterbi average (%f), is:%d", count, dua_avg / count, vit_avg / count, skiped);
//        System.out.println(dh.getRSE(dh.accumulate(vit_density), dh.accumulate(dua_density)));
//
        GravityModel model = new GravityModel();
        String path = home + "/avgGOD.csv";
         model.writeFGOD(home+"/all.val.csv", densities);
//        model.writeAvgGOD(path, model.avgGOD(dh.getdensities(dh.accumulate(vit_density), dh.accumulate(dua_density))));
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(densities);
        System.out.printf("\n## %f,%f\n",model.find_all_linear_regression(densities,false),model.find_avg_linear_regression(avgGOD,false));
        model.writeAvgGOD(path, avgGOD);
    }

}

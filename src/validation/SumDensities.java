/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import Density.Density;
import Observations.Obs;
import Observations.ObsTripsBuilder;
import Viterbi.TransBuilder;

/**
 *
 * @author essam
 */
public class SumDensities {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        sumViterbi();
//        sumRoutes();
        printErrors();
    }

    public static void printErrors() {
        String dir = "/home/essam/traffic/Training_stops/accum/week_1/";
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dir));
        int dayid = 7;
        for (int i = dayid; i < (dayid + (files.size() / 2)); i++) {
            String dua = dir + "routes.density." + i + ".xml";
            String vit = dir + "viterbi.density." + i + ".xml";
            Hashtable<String, Hashtable<String, Double>> dua_density = Density.readDensity(dua);
            Hashtable<String, Hashtable<String, Double>> vit_density = Density.readDensity(vit);
            System.out.println("----------------------------------");
            System.out.println(TransBuilder.getError2(vit_density, dua_density));
        }

    }

    public static void sumRoutes() {
        String dir = "/home/essam/traffic/Training_stops/accum/routes/";
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dir));

        /**
         * Make sure that file are ordered in exact one for comparison
         * purposes...........
         */
        int dayid = 7;
        RoutesDensity dh = new RoutesDensity();
        Vit2ODs vod = new Vit2ODs();
        Hashtable<String, Hashtable<String, Double>> dua_density_0 = new Hashtable<>();
        for (int i = dayid; i < dayid + files.size(); i++) {
            String routes = dir + "routes." + i + ".xml";
            Hashtable<String, Hashtable<String, Double>> dua_density_1 = dh.getRoutesDensity(vod.readRoutes(routes));

            dua_density_0 = dh.sumDensities(dua_density_0, dua_density_1);

            String routes_sum = dir + "routes.density." + i + ".xml";
            Density.writeDensity(dua_density_0, routes_sum);
        }
    }

    public static void sumViterbi() {
        String dir = "/home/essam/traffic/Training_stops/accum/viterbi/";
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dir));

        /**
         * Make sure that file are ordered in exact one for comparison
         * purposes...........
         */
        int dayid = 7;
        RoutesDensity dh = new RoutesDensity();
        Vit2ODs vod = new Vit2ODs();
        Hashtable<String, Hashtable<String, Double>> dua_density_0 = new Hashtable<>();
        for (int i = dayid; i < dayid + files.size(); i++) {
            String vit_out = dir + "SET2_P01.CSV.0_500_th-dist_1000_th-time_60.viterbi.day." + i + ".xml";
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_out);
            Hashtable<String, Hashtable<String, Double>> dua_density_1 = dh.getVitDensity(obs);

            dua_density_0 = dh.sumDensities(dua_density_0, dua_density_1);

            String routes_sum = dir + "viterbi.density." + i + ".xml";
            Density.writeDensity(dua_density_0, routes_sum);
        }

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import AViterbi.Interpolate.Interpolation;
import Voronoi.VoronoiConverter;
import trajectory.POI.POI;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class InterpolateObs {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int stwr = Integer.parseInt(args[0]);
        int etwr = Integer.parseInt(args[1]);
        String dataset_path = args[2];
        String regions_path = args[3];
        String neighbors_path = args[4];
        String towers_path = args[5];
        String output_path = args[6];

//        int stwr=Integer.parseInt(args[0]);
//        int etwr=Integer.parseInt(args[1]);
//        String dataset_path = "/home/essam/traffic/SET2";
//        String regions_path = "/home/essam/traffic/DI/Dakar-2.1/dakar.vor.csv";
//        String neighbors_path = "/home/essam/traffic/DI/Dakar-2.1/dakar.vor.neighborSitesForSite.csv";
//        String towers_path = "/home/essam/traffic/DI/Dakar-2.1/towers.csv";
//        String output_path = "";
        VoronoiConverter converter = new VoronoiConverter();
//        ArrayList<Region> voronoi_regions = converter.readVoronoi(regions_path);
        Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = converter.readVorNeighbors(neighbors_path);
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);

        Interpolation interpolate = new Interpolation(voronoi_neighbors, towers);
        POI poi = new POI();
        ObsTripsBuilder builder = new ObsTripsBuilder();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {

                Path p = Paths.get(subsetPath);
                String file_name = p.getFileName().toString().substring(0, p.getFileName().toString().lastIndexOf("."));

                System.out.println(file_name);
                Hashtable<String, Hashtable<Integer, Obs>> obs = interpolate.interpolate_observations(builder.transposeWUT(builder.remove_repeated(builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stwr, etwr)))));
                String dist = output_path + "/" + file_name + ".xml";
                ObsTripsBuilder.writeObsDUT(obs, dist);

            }
        }

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Density.Plot;
import Observations.ObsTripsBuilder;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.Region;
import utils.StdDraw;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_plot_history {

    /**
     * @param args the command line arguments
     */
    public static double xmin, ymin, xmax, ymax;

    public static void main(String[] args) {
        String towersPath = "/home/essam/traffic/DI/Dakar-2/towers.csv";
        String image_dir = "/home/essam/traffic/DI/Dakar-2/history";
        String edgesPath = "/home/essam/traffic/DI/Dakar-2/edges.interpolated.xml";
//        String map_path = "/home/essam/traffic/DI/Dakar-2/map";
        String vorNeighborsPath = "/home/essam/traffic/DI/Dakar-2/dakar.vor.neighborSitesForSite.csv";
        String vor = "/home/essam/traffic/DI/Dakar-2/dakar.vor.csv";
        String dataset_path = "/home/essam/traffic/SET2/poi";

//        double xmin, ymin, xmax, ymax;
        xmin = 227301.71;
        xmax = 270171.66;
        ymin = 1620398.31;
        ymax = 1648866.88;

        int stwr = 0, etwr = 500, sample_size = 20;

        DataHandler adaptor = new DataHandler();
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towersPath);

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor);
        Hashtable<Integer, ArrayList<Integer>> voronoiNeibors
                = converter.readVorNeighbors(vorNeighborsPath);

        POI poi = new POI();
        Hashtable<Integer, List<Integer>> u_data = ObsTripsBuilder.get_com_usr_sample(poi.unlimited_combine_twr(poi.getObs(dataset_path)), stwr, etwr, sample_size);

        // history colors ...
//        Hashtable<Integer, Color> obsColors = new Hashtable<>();
//        obsColors.put(0, Color.decode("#E6ED13"));
//        obsColors.put(1, Color.decode("#EDCC13"));
//        obsColors.put(2, Color.decode("#ED8713"));
//        obsColors.put(3, Color.decode("#ED5F13"));
//        obsColors.put(4, Color.decode("#ED3013"));
//        obsColors.put(5, Color.decode("#E6ED13"));
//        obsColors.put(6, Color.decode("#B3ED13"));
        //plot a few examples ...
        int i = 0;
        for (Map.Entry<Integer, List<Integer>> entrySet : u_data.entrySet()) {
            Integer usr_id = entrySet.getKey();
            System.out.println("User id:\t" + usr_id);
            List<Integer> obs_lst = entrySet.getValue();
//            Color c = obsColors.get(i);
            String image = image_dir + "/" + usr_id.toString() + ".png";
            plot(edges, voronoiRegions, voronoiNeibors, towers, obs_lst, image);
            
            if (i == 20) {
                break;
            }
            i++;
        }

    }

    private static void plot(ArrayList<Edge> edges,
            ArrayList<Region> voronoiRegions,
            Hashtable<Integer, ArrayList<Integer>> voronoiNeibors,
            Hashtable<Integer, Vertex> towers,
            List<Integer> obs_lst,
            String image) {

        Plot plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();
        plotter.plot_voronoi(voronoiRegions, voronoiNeibors);
        double r = 30;
        StdDraw.setPenColor(Color.ORANGE);
        // issue plot of a  list 
        for (Iterator<Integer> iterator = obs_lst.iterator(); iterator.hasNext();) {
            Vertex t = towers.get(iterator.next());

            StdDraw.filledCircle(t.getX(), t.getY(), r);

        }
        plotter.display_save();
    }

}

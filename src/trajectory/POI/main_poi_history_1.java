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
public class main_poi_history_1 {

    /**
     * @param args the command line arguments
     */
    public static double xmin, ymin, xmax, ymax;

    public static void main(String[] args) {
        String image_dir = "/home/essam/traffic/DI/Dakar-2/history";
        String edgesPath = "/home/essam/traffic/DI/Dakar-2/edges.interpolated.xml";
        String vr_file = "/home/essam/traffic/DI/Dakar-2/senegal.vor.csv";
        String poi_file = "/home/essam/traffic/SET2/poi/poi_sample_log.xml";
        String tower_file = "/home/essam/traffic/Dakar/towers.csv";
        String dataset_path = "/home/essam/traffic/SET2/poi";

        xmin = 227301.71;
        xmax = 270171.66;
        ymin = 1620398.31;
        ymax = 1648866.88;

        int sample_size = 30;

        POI poi = new POI();
        Hashtable<Integer, List<Integer>> obs_tbl = poi.unlimited_combine_twr(poi.getObs(dataset_path));

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(vr_file);

        DataHandler adaptor = new DataHandler();
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(tower_file);

        Scan s = new Scan();
        Hashtable<Integer, List<Cluster>> samples = s.read_xml_poi(poi_file);
        System.out.println("Sample size:\t " + samples.size());
//plot a few examples ...
        int i = 0;
        for (Map.Entry<Integer, List<Cluster>> entrySet : samples.entrySet()) {
            Integer usr_id = entrySet.getKey();
            System.out.println("User: " + usr_id);

            List<Integer> usr_obs = obs_tbl.get(usr_id);
            int[] i_seq = s.convert(usr_obs);
            Hashtable<Integer, Integer> freq = s.get_freq(i_seq);

            List<Cluster> value = entrySet.getValue();
            String image = image_dir + "/" + usr_id.toString() + ".png";
            List<Integer> c_plot = new ArrayList<>();
            for (Iterator<Cluster> iterator = value.iterator(); iterator.hasNext();) {
                Cluster c = iterator.next();
                //debug
//                if (usr_id == 196536) {
//                    c.print();
//                }
                List<Integer> pnts = c.getpoints();
                for (Iterator<Integer> iterator1 = pnts.iterator(); iterator1.hasNext();) {
                    Integer next = iterator1.next();
                    c_plot.add(next);
                }
            }
            //debug
//            if (usr_id == 196536) {
//                System.out.println(c_plot.toString());
//            }

            plot(edges, voronoiRegions, c_plot, towers, freq, image);

            if (i == sample_size) {
                break;
            }
            i++;
        }

    }

    private static void plot(ArrayList<Edge> edges,
            ArrayList<Region> voronoiRegions,
            List<Integer> cluster,
            Hashtable<Integer, Vertex> towers,
            Hashtable<Integer, Integer> freq,
            String image) {

        Plot plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();
        plotter.plot_vor_fill(voronoiRegions, cluster);

        StdDraw.setPenColor(Color.ORANGE);
        double r = 30;
        for (Map.Entry<Integer, Integer> entrySet : freq.entrySet()) {
            Vertex v = towers.get(entrySet.getKey());
            int value = entrySet.getValue();
            
            StdDraw.filledCircle(v.getX(), v.getY(), r);
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(v.getX()+2*r, v.getY()+2*r, String.valueOf(value));
        }
//        plotter.fill_region(voronoiRegions, cluster);
        plotter.display_save();
    }

}

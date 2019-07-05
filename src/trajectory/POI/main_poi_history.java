/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Density.Plot;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import utils.Edge;
import utils.Region;

/**
 *
 * @author essam
 */
public class main_poi_history {

    /**
     * @param args the command line arguments
     */
    public static double xmin, ymin, xmax, ymax;

    public static void main(String[] args) {
        String image_dir = "/home/essam/traffic/DI/Dakar-2/history";
        String edgesPath = "/home/essam/traffic/DI/Dakar-2/edges.interpolated.xml";
        String vr_file = "/home/essam/traffic/DI/Dakar-2/senegal.vor.csv";
        String poi_file = "/home/essam/traffic/SET2/poi/poi_sample_log.xml";

        xmin = 227301.71;
        xmax = 270171.66;
        ymin = 1620398.31;
        ymax = 1648866.88;

        int sample_size = 30;

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(vr_file);

        Scan s = new Scan();

        Hashtable<Integer, List<Cluster>> samples = s.read_xml_poi(poi_file);
        System.out.println("Sample size:\t " + samples.size());
//plot a few examples ...
        int i = 0;
        for (Map.Entry<Integer, List<Cluster>> entrySet : samples.entrySet()) {
            Integer usr_id = entrySet.getKey();
            System.out.println("User: " + usr_id);
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

            plot(edges, voronoiRegions, c_plot, image);

            if (i == sample_size) {
                break;
            }
            i++;
        }

    }

    private static void plot(ArrayList<Edge> edges, 
            ArrayList<Region> voronoiRegions,
            List<Integer> cluster,
            String image) {

        Plot plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();
        plotter.plot_vor_fill(voronoiRegions, cluster);
//        plotter.fill_region(voronoiRegions, cluster);
        plotter.display_save();
    }

}

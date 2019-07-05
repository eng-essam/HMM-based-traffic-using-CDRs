/* * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

import java.util.ArrayList;
import java.util.List;

import Density.Plot;
import mergexml.NetConstructor;
import netconvert.netconverter;
import utils.DataHandler;
import utils.Edge;

/**
 *
 * @author essam
 */
public class main_plot_towers {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String path = "/media/essam/Dell1/traffic/Alex/main_routes/alex.osm";
        String towers_path = "/media/essam/Dell1/traffic/Alex/towers.csv";
        String edges_path = "/media/essam/Dell1/traffic/Alex/main_routes/vodafone/edges.xml";
        String image_path = "/media/essam/Dell1/traffic/Alex/alex_1.png";
        boolean unicolor_map = true;
        boolean shift_coord = false;
        double minlat = 30.8362, minlon = 29.4969, maxlat = 31.3302, maxlon = 30.0916;

        double xymin[], xymax[];
        xymin = DataHandler.proj_coordinates(minlat, minlon);
        xymax = DataHandler.proj_coordinates(maxlat, maxlon);

        netconverter nc = new netconverter(minlat, minlon);
        nc.read_osm(path);

        ArrayList<Edge> edges = new NetConstructor(edges_path).readedges();
        Plot plotter = new Plot(edges, image_path);
//        Plot plotter = new Plot(image_path);
//        plotter.scale(minlat, minlon, maxlat, maxlon);
        plotter.scale(0, 0, (xymax[0] - xymin[0]), (xymax[1] - xymin[1]));
//        nc.plot_map(unicolor_map);
        List<String[]> towers = DataHandler.extract_info(DataHandler.read_csv(towers_path, DataHandler.COMMA_SEP), new int[]{1, 2, 3});
        nc.plot_cell_networks(towers);
        plotter.plotMapEdges();

        plotter.display_save();

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;

import Density.Plot;
import mergexml.NetConstructor;
import utils.Edge;

/**
 *
 * @author essam
 */
public class main_plot_map {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String map_path = "/home/essam/traffic/Dakar/sep_2015/Dakar_edge-200/map";
        String edgesPath = "/home/essam/traffic/Dakar/sep_2015/Dakar_edge-200/edges.xml";
        String image = "/home/essam/traffic/Dakar/sep_2015/Dakar_edge-200/main.png";
        
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;
        
        Plot plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges(edges);
        
        plotter.display_save();
    }
    
}

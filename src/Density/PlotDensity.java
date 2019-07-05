/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.util.ArrayList;
import java.util.Hashtable;

import mergexml.NetConstructor;
import utils.Edge;

/**
 *
 * @author essam
 */
public class PlotDensity {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String edgesPath = args[0];
//        String image = args[1];
//        String density = args[2];
//        int n = 11;
        String edgesPath = "/home/essam/traffic/sep_2015/Dakar_edge-200/edges.xml";
        String map = "/home/essam/traffic/sep_2015/Dakar_edge-200/map";
        String image = "/home/essam/traffic/sep_2015/Dakar_edge-200/density_2013-01-08_Tuesday.png";
        String densityPath = "/home/essam/traffic/sep_2015/Dakar_edge-200/results/alpha-0.7_beta-.3/transitions-00/SET2_P01.CSV.0_300_th-dist_1000_th-time_60.density.day.2013-01-08_Tuesday.xml";
//        String edgesPath = "/home/essam/traffic/sep_2015/Dakar_edge-200/edges.xml";
//        String image = "/home/essam/tmp/all_routes.png";
//        String densityPath = "/home/essam/tmp/SET2_P01.CSV.0_300.denisty.day.19.xml";

        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1619000.93;
        ymax = 1635128.30;

//        complete dakar map
//        xmin = 227761.06;
//        xmax = 250000.96;
//        ymin = 1616000.93;
//        ymax = 1637065.55;

//        xmin = 227761.06;
//        xmax = 270728.96;
//        ymin = 1618439.13;
//        ymax = 1645065.55;
//        
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
//        Plot ploter = new Plot(edges, image);
//        ploter.scale(xmin, ymin, xmax, ymax);
//        ploter.plotMapEdges();
//        ploter.plotMapData(map);
//        StdDraw.setPenRadius(0.002);
//        StdDraw.arc(ymax, ymax, ymax, ymax, ymax);
        Density densityHandler = new Density();
        Hashtable<String, Hashtable<String, Double>> density = Density.readDensity(densityPath);
        
        densityHandler.plotDensity(edges, map, density,xmin, ymin, xmax, ymax, image, true,"tuesday 2013-01-08","",1000);


    }

}

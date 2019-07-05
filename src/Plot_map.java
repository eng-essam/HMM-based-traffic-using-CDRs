
import java.util.ArrayList;

import Density.Plot;
import mergexml.NetConstructor;
import utils.Edge;
import utils.StdDraw;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class Plot_map {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String edgesPath = "/media/essam/Files/traffic/Satellite/Waseda/Map/edges.xml";
        String image = "/media/essam/Files/traffic/Satellite/Waseda/Map/im.jpg";
        double xmin, ymin, xmax, ymax;
        xmin = 0;
        xmax = 381.68;
        ymin = 0;
        ymax = 261.33;
        StdDraw.setCanvasSize(12544, 8960);
        Plot plotter;
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        plotter = new Plot(edges, image);
        
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        StdDraw.setPenRadius(0.01);
        plotter.plotMapEdges();
        plotter.display_save();
        System.exit(0);
    }
    
}

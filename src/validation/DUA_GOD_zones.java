/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.util.ArrayList;
import java.util.Hashtable;

import mergexml.MapDetails;
import utils.DataHandler;
import utils.FromNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class DUA_GOD_zones {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String data_path = "/home/essam/Dropbox/viterbi/TKDD/paper results/equilibrium/without training/";
        String routes_paht = data_path + "Dakar.astar.rou.xml";
        String mapPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.w.vor.xml";
        String towersPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.towers.csv";

        final double threshold = 200f;
        final int trip_length = 3;

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);
        MapDetails details = new MapDetails();
        Hashtable<Integer, Vertex> towers = details.readTowers(towersPath);
//        System.out.println("towers\t"+towers.size());
        GravityModel model = new GravityModel();
        Hashtable<String, Hashtable<String, Double>> distances = new Hashtable<>(model.calcDistances(towers));
        Hashtable<String, Integer> nodesMap = model.generateNodesMap(map);

        Vit2ODs vod = new Vit2ODs();
        ArrayList<String> routes = vod.readRoutes(routes_paht);
        
        
        model.handle_zones_Flow(routes, nodesMap, trip_length);
        model.setDistances(distances);

        Hashtable<Double, ArrayList<Double>> fgod = model.computeGOD(threshold,false);
        model.writeFGOD(data_path + "fgod.csv", fgod);
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
        model.writeAvgGOD(data_path + "avg.fgod.csv", avgGOD);

        System.out.printf("Gravity with zones linear regression with all values %f, and linear regression with avg %f\n", model.find_all_linear_regression(fgod,false), model.find_avg_linear_regression(avgGOD,false));

    }

}

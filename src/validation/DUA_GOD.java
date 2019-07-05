/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.util.ArrayList;
import java.util.Hashtable;

import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class DUA_GOD {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String data_path = "/home/essam/traffic/Dakar/TAPASCologne-0.17.0/";
        String routes_paht = data_path + "cologne.rou.xml";
        String mapPath = "/home/essam/traffic/Dakar/TAPASCologne-0.17.0/cologne.xy.dist.xml";

        final double threshold = 200f;
        final int trip_length = 3;

        Vit2ODs vod = new Vit2ODs();
        ArrayList<String> routes = vod.readRoutes(routes_paht);
        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);

        GravityModel model = new GravityModel();
        model.handleHFlow(routes, trip_length);
        model.calcDistances(map);

        Hashtable<Double, ArrayList<Double>> fgod = model.computeGOD(threshold,false);
        model.writeFGOD(data_path + "fgod.csv", fgod);
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
        model.writeAvgGOD(data_path + "avg.fgod.csv", avgGOD);

        System.out.printf("Gravity with road segments linear regression with all values %f, and linear regression with avg %f\n", model.find_all_linear_regression(fgod,false), model.find_avg_linear_regression(avgGOD,false));

    }

}

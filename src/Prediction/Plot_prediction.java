/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prediction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Density.Density;
import mergexml.NetConstructor;
import utils.Edge;

/**
 *
 * @author essam
 */
public class Plot_prediction {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String file = "/home/essam/traffic/ts_forcasting.csv";
        String out_dir = "/home/essam/traffic/forcasting";
        String edgesPath = "/home/essam/traffic/Dakar/N_Darak/edges.xml";
        String map = "/home/essam/traffic/Dakar/N_Darak/map";
        int max = 6000;
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        Hashtable<String, Hashtable<String, Integer>> accumVeh = read(file);
        System.out.println(accumVeh.size());
        Density density = new Density();
        for (Map.Entry<String, Hashtable<String, Integer>> entry : accumVeh.entrySet()) {
            String string = entry.getKey();
            String image = out_dir + "/" + string + ".png";
            System.out.println(string);
            Hashtable<String, Integer> hashtable = entry.getValue();
            density.plotAccDensity(edges, map, hashtable, image, true, string, "", max);

        }

    }

    public static Hashtable<String, Hashtable<String, Integer>> read(String file) {

        Hashtable<String, Hashtable<String, Integer>> accumVeh = new Hashtable<>();

        String line;
        BufferedReader br;
        boolean hflag = true;
        String headers[] = null;
        try {
            //static file contain the map points
            br = new BufferedReader(new FileReader(file));

            while ((line = br.readLine()) != null) {
                if (hflag) {
                    headers = line.split(",");
                    for (int i = 3; i < headers.length; i++) {
                        headers[i] = headers[i].replace("_Sum", "");

                    }
                    hflag = false;
                    continue;
                }
                if (line.isEmpty()) {
                    continue;
                }
                String[] day_vals = line.split(",");
                String day = day_vals[0] + "-" + day_vals[1] + "-" + day_vals[2];
                System.out.println(day);
                Hashtable<String, Integer> points = new Hashtable<>();

                for (int i = 3; i < day_vals.length - 1; i++) {
                    String tmp = day_vals[i];
//                    System.out.println(tmp);
                    try {
                        points.put(headers[i], (int) Double.parseDouble(tmp));
                    } catch (NumberFormatException numberFormatException) {
                        points.put(headers[i], 0);
                    }
                }
                accumVeh.put(day, points);
            }
            br.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Prediction.Plot_prediction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Prediction.Plot_prediction.class.getName()).log(Level.SEVERE, null, ex);
        }

        return accumVeh;
    }

}

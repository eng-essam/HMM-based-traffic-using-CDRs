/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.NetConstructor;
import utils.Edge;

/**
 *
 * @author essam
 */
public class TimeTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {

        String t1 = "00:00:00";
        String t2 = "24:00:00";
        final int increment = 1;
//        String t3 = "16:15:10";
//        String vit_dir = "/home/essam/traffic/filtered_trips/without.training/day.11";
        String vit_dir =args[0];
        String edgesPath = args[1];
        String map =args[2];
//        String edgesPath = "/home/essam/traffic/Dakar/N_Darak/edges.xml";
//        String map = "/home/essam/traffic/Dakar/N_Darak/map";

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vit_dir));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String vit_day = iterator.next();

            if (!vit_day.contains(".viterbi.day")) {
                continue;
            }
            /**
             * Create directories for the splitted densities
             */
            String dir = vit_day.substring(0, vit_day.lastIndexOf("."));
            if (dir.contains(".csv.")) {
                dir.replace(".csv.", "_");
            } else if (dir.contains(".CSV.")) {
                dir.replace(".CSV.", "_");
            }
            if (new File(dir).mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory already exist");
            }
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(formatter.parse(t1));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(formatter.parse(t2));
            Density density = new Density();

            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);
//        System.out.println(obs.size());
            while (cal2.after(cal1)) {
                String from = cal1.get(Calendar.HOUR_OF_DAY) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);
                Calendar tmp = Calendar.getInstance();;
                String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
                tmp.setTime(formatter.parse(to));
                String ipath = dir + "/density_" + from + "_" + to + ".png";
                String xmlpath = dir + "/density_" + from + "_" + to + ".xml";
//            System.out.println(ipath);
//                System.out.println(from + "," + to);
                Hashtable<String, Hashtable<String, Double>> dtable = density.getHDensity(cal1, tmp, obs);
                density.writeAccumDensity(density.sumIEVehicles(dtable), xmlpath);
                density.plotDensity(edges, map, dtable, ipath, true);
                cal1.add(Calendar.HOUR_OF_DAY, increment);
//                break;
            }
//            break;
        }

    }

}

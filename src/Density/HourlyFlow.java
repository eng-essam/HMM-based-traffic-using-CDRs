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

/**
 *
 * @author essam
 */
public class HourlyFlow {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {

        String t1 = "00:00:00";
        String t2 = "24:00:00";
        final int increment = 1;
//        String t3 = "16:15:10";
//        String vit_dir = "/home/essam/traffic/filtered_trips/with_training";
        String vit_dir =args[0];
        String hd_file_path=args[1];
//        String edgesPath = "/home/essam/traffic/Dakar/N_Darak/edges.xml";
//
//        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        Density density = new Density();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Hashtable<String, Hashtable<String, Integer>> hourlyTable = new Hashtable<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vit_dir));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String vit_day = iterator.next();

            if (!vit_day.contains(".viterbi.day")) {
                continue;
            }

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(formatter.parse(t1));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(formatter.parse(t2));

            Hashtable<String, Hashtable<String, Integer>> dayHoursTable = new Hashtable<>();
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);
//        System.out.println(obs.size());
            while (cal2.after(cal1)) {
                Calendar tmp = Calendar.getInstance();;
                String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
                tmp.setTime(formatter.parse(to));
//           
                Hashtable<String, Hashtable<String, Double>> dtable = density.getHDensity(cal1, tmp, obs);
                dayHoursTable.put(formatter.format(tmp.getTime()), density.sumIEVehicles(dtable));

                cal1.add(Calendar.HOUR_OF_DAY, increment);

            }

            /**
             * Add days densities.
             */
            density.sumHDayDensities(hourlyTable, dayHoursTable);
        }
        density.writeHDstates(hd_file_path, hourlyTable);
    }

}

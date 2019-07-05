/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * java -d64 -XX:+UseG1GC -Xms5g -Xmx5g -XX:MaxGCPauseMillis=500   Density.TimeTester1 $dir/results/transitions-00/tmp/ $dir/edges.xml $dir/map 
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
import java.util.logging.Level;
import java.util.logging.Logger;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.NetConstructor;
import utils.Edge;

class RunnablePloter implements Runnable {

    final static String t1 = "00:00:00";
    final static String t2 = "24:00:00";
    final static int increment = 1;

    private Thread t;
    private String threadName;
    private String vit_day;
    private ArrayList<Edge> edges;
    private String map;
    private double max;
    private String start_date;

    public RunnablePloter(String threadName, String vit_day, ArrayList<Edge> edges, String map, double max, String start_date) {
        this.t = t;
        this.threadName = threadName;
        this.vit_day = vit_day;
        this.edges = edges;
        this.map = map;
        this.max = max;
        this.start_date = start_date;
    }

    @Override
    public void run() {

        try {
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

            DateFormat sdformatter = new SimpleDateFormat("yyyy-mm-dd");

            Calendar scal = Calendar.getInstance();
            scal.setTime(sdformatter.parse(start_date));

//        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
//            String vit_day = iterator.next();
//
//            if (!vit_day.contains(".viterbi.day")) {
//                continue;
//            }
            /**
             * Create directories for the splitted densities
             */
            String dir = vit_day.substring(0, vit_day.lastIndexOf("."));
//            int day = Integer.parseInt(dir.substring(dir.lastIndexOf("."), dir.length()));
//            String month
//            if (dir.contains(".csv.")) {
//                dir.replace(".csv.", "_");
//            } else if (dir.contains(".CSV.")) {
//                dir.replace(".CSV.", "_");
//            }
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
                Calendar tmp = Calendar.getInstance();
                String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
                tmp.setTime(formatter.parse(to));

//            String from_tmp = String.format("%02d", (cal1.get(Calendar.HOUR_OF_DAY))) + ":" + String.format("%02d", cal1.get(Calendar.MINUTE)) + ":" + String.format("%02d", cal1.get(Calendar.SECOND));
//            String to_tmp = String.format("%02d", (cal1.get(Calendar.HOUR_OF_DAY) + increment)) + ":" + String.format("%02d", cal1.get(Calendar.MINUTE)) + ":" + String.format("%02d", cal1.get(Calendar.SECOND));
                String ipath = dir + "/" + start_date + "_" + formatter.format(cal1.getTime()) + "_" + formatter.format(tmp.getTime()) + ".png";

                /**
                 * get day and week:
                 */
//            System.out.pr intln(ipath);
//                System.out.println(from + "," + to);
                Hashtable<String, Hashtable<String, Double>> dtable = density.getHDensity(cal1, tmp, obs);
//                density.writeAccumDensity(density.sumIEVehicles(dtable), xmlpath);
                density.plotIEDensity(edges, map, dtable, ipath, true, start_date, from + " ==> " + to, max);
                cal1.add(Calendar.HOUR_OF_DAY, increment);

//                break;
            }
        } catch (ParseException ex) {
            Logger.getLogger(RunnablePloter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}

/**
 *
 * @author essam
 */
public class TimeTester1 {

    /**
     * @param args the command line arguments
     */
//    final static String t1 = "00:00:00";
//    final static String t2 = "24:00:00";
    final static String t1 = "09:00:00";
    final static String t2 = "10:00:00";
    final static int increment = 1;

    public static double getmax(ArrayList<String> files) throws ParseException {
        Density density = new Density();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        int day = 2; // search in XXfor days only fot the max number of vehicles
        int i = 0;
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

            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);
//        System.out.println(obs.size());
            while (cal2.after(cal1)) {
                String from = cal1.get(Calendar.HOUR_OF_DAY) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);
                Calendar tmp = Calendar.getInstance();;
                String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
                tmp.setTime(formatter.parse(to));
                Hashtable<String, Hashtable<String, Double>> dtable = density.getHDensity(cal1, tmp, obs);
                cal1.add(Calendar.HOUR_OF_DAY, increment);
                density.sumIEVehicles(dtable);
            }
            if (i == day) {
                break;
            }
            i++;
        }
        return density.getMax();
    }

    public static void main(String[] args) throws ParseException {

//        String t3 = "16:15:10";
        String vit_dir = args[0];
        String edgesPath = args[1];
        String map = args[2];

//        String vit_dir = "/media/essam/TOSHIBA EXT/traffic/Dakar/sep_2015/Dakar_edge-200/results/updates/viterbi/alpha-0.1_beta-.9/transitions-00/";
////        String vit_dir =args[0];
//        String edgesPath = "/media/essam/TOSHIBA EXT/traffic/Dakar/sep_2015/Dakar_edge-200/edges.interpolated.xml";
//        String map = "/media/essam/TOSHIBA EXT/traffic/Dakar/sep_2015/Dakar_edge-200/map";
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
//        String start_date = "2013-02-01";

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vit_dir));
        files.sort(null);

        double max_veh = 40;

//        double max_veh = getmax(files);
        System.out.println(max_veh);
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            if (!subsetPath.contains(".viterbi.day")) {
                continue;
            }
            String day = subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") + "viterbi.day.".length(), subsetPath.lastIndexOf("_"));

//            RunnablePloter ploter = new RunnablePloter(day, subsetPath, edges, map, max_veh, day);
//            ploter.start();
            plotHdensities(subsetPath, edges, map, max_veh, day);

        }

//        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
//            String next = iterator.next();
//            System.out.println(next);
//
//        }
    }

    public static void plotHdensities(String vit_day, ArrayList<Edge> edges, String map, double max, String start_date) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        DateFormat sdformatter = new SimpleDateFormat("yyyy-mm-dd");
        DateFormat dayformater = new SimpleDateFormat("EEEE yyyy-mm-dd");

        Calendar scal = Calendar.getInstance();
        scal.setTime(sdformatter.parse(start_date));

//        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
//            String vit_day = iterator.next();
//
//            if (!vit_day.contains(".viterbi.day")) {
//                continue;
//            }
        /**
         * Create directories for the splitted densities
         */
        String dir = vit_day.substring(0, vit_day.lastIndexOf("."));
//            int day = Integer.parseInt(dir.substring(dir.lastIndexOf("."), dir.length()));
//            String month
//            if (dir.contains(".csv.")) {
//                dir.replace(".csv.", "_");
//            } else if (dir.contains(".CSV.")) {
//                dir.replace(".CSV.", "_");
//            }
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
            Calendar tmp = Calendar.getInstance();
            String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
            tmp.setTime(formatter.parse(to));

//            String from_tmp = String.format("%02d", (cal1.get(Calendar.HOUR_OF_DAY))) + ":" + String.format("%02d", cal1.get(Calendar.MINUTE)) + ":" + String.format("%02d", cal1.get(Calendar.SECOND));
//            String to_tmp = String.format("%02d", (cal1.get(Calendar.HOUR_OF_DAY) + increment)) + ":" + String.format("%02d", cal1.get(Calendar.MINUTE)) + ":" + String.format("%02d", cal1.get(Calendar.SECOND));
            String ipath = dir + "/" + start_date + "_" + formatter.format(cal1.getTime()) + "_" + formatter.format(tmp.getTime()) + ".png";
            String xmlpath = dir + "/" + start_date + "_" + formatter.format(cal1.getTime()) + "_" + formatter.format(tmp.getTime()) + ".xml";
            /**
             * get day and week:
             */
//            System.out.pr intln(ipath);
//                System.out.println(from + "," + to);
            Hashtable<String, Hashtable<String, Double>> dtable = density.getHDensity(cal1, tmp, obs);
            density.writeAccumDensity(density.sumIEVehicles(dtable), xmlpath);
            density.plotDensity(edges, map, dtable, ipath, true, dayformater.format(scal.getTime()), from + " ==> " + to, max);
            cal1.add(Calendar.HOUR_OF_DAY, increment);

//                break;
        }

//        scal.add(Calendar.DAY_OF_MONTH, increment);
//        start_date = sdformatter.format(scal.getTime());
//        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.MapDetails;
import utils.DataHandler;
import utils.FromNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class RunHGravity {

    final static String RLM = "/";
    final static String CLM = ",";

    /**
     * generate density map from DAILY Viterbi paths from a specific time to
     * another.
     *
     * @param Calendar fromH
     * @param Calendar toH
     * @param Hashtable<Integer, Hashtable<Integer, Obs>> obs
     *
     * @return
     */
    public static ArrayList<String> getHTrips(Calendar fromH,
            Calendar toH,
            Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {

        Hashtable<String, Hashtable<String, Double>> hd = new Hashtable<>();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        ArrayList<String> seq_hour = new ArrayList<>();
        ArrayList<String> tseq_hour = new ArrayList<>();

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
            Hashtable<Integer, Obs> dailyObs = entrySet.getValue();
            for (Map.Entry<Integer, Obs> dailyObsEntry : dailyObs.entrySet()) {
                Obs observations = dailyObsEntry.getValue();
                String seq = observations.getVitPath();
                String timeStamp = observations.getTimeStamp();

                if (seq.isEmpty()) {
                    continue;
                }
//                System.out.println(seq);
                /**
                 * Initiate daily observation trips
                 */
                String[] vitPnts = new String[]{seq};
                String[] obsTimes = new String[]{timeStamp};
                if (seq.contains(RLM)) {
                    vitPnts = seq.split(RLM);
                    obsTimes = timeStamp.split(RLM);
                }

                /**
                 * generate partial trips (trips based on time stamp and
                 * distances).
                 */
                for (int i = 0; i < vitPnts.length; i++) {
                    /**
                     * If the Viterbi of this trip can not be determine continue
                     * to the next one.
                     */
                    if (vitPnts[i].equals("-")) {
//                        System.out.println(vitPnts[i]);
                        continue;
                    }
//                    else {
//                        System.out.println(vitPnts[i]);
//                    }

                    String[] pnts = vitPnts[i].split(CLM);
                    String[] tstamps = obsTimes[i].split(CLM);

                    String path = "";
                    String time = "";
                    for (int j = 0; j < tstamps.length; j++) {
                        String tstamp = tstamps[j];
                        cal.setTime(formatter.parse(tstamp));
                        /**
                         * If current time stamp in between the require period
                         * add it to the path.
                         */
                        if ((fromH.before(cal) && toH.after(cal))) {
//                            System.out.println("in");
                            time += tstamp + ",";
                            path += pnts[j] + ",";
                        }

                    }
                    /**
                     *
                     */
                    if (!path.isEmpty()) {
                        seq_hour.add(path);
                        tseq_hour.add(time);
//                        System.out.println(time + "\t" + path);
                    }

                }
            }
        }
//        System.out.println(toH.get(Calendar.HOUR_OF_DAY) + ":" + toH.get(Calendar.MINUTE) + ":" + toH.get(Calendar.SECOND) + "\t" + count_time(tseq_hour));
        return seq_hour;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        String t1 = "00:00:00";
        String t2 = "24:00:00";
        final int increment = 6;
        String vitPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Results";
        String mapPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.vor.xml";
        String towersPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.towers.csv";
        final double threshold = 200f;
        final int trip_length = 3;

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);
        MapDetails details = new MapDetails();
        Hashtable<Integer, Vertex> towers = details.readTowers(towersPath);
//        System.out.println("towers\t"+towers.size());
        GravityModel model = new GravityModel();
        Hashtable<String, Hashtable<String, Double>> distances = new Hashtable<>(model.calcDistances(towers));
        Hashtable<String, Integer> nodesMap = model.generateNodesMap(map);

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vitPath));
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
//            if (new File(dir).mkdir()) {
//                System.out.println("Directory created");
//            } else {
//                System.out.println("Directory already exist");
//            }
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(formatter.parse(t1));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(formatter.parse(t2));

            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);
            ArrayList<Hashtable<Double, ArrayList<Double>>> all_fgod = new ArrayList<>();
            ArrayList<Hashtable<Double, ArrayList<Double>>> all_avg_fgod = new ArrayList<>();
//        System.out.println(obs.size());
            while (cal2.after(cal1)) {
                String from = cal1.get(Calendar.HOUR_OF_DAY) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);
                Calendar tmp = Calendar.getInstance();;
                String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
                tmp.setTime(formatter.parse(to));
//                String xmlpath = dir + "/fog_" + from + "_" + to + ".csv";
                ArrayList<String> dtable = getHTrips(cal1, tmp, obs);
                model = new GravityModel();
                model.setDistances(distances);
                model.handle_hourly_zones_Flow(dtable, nodesMap, trip_length);

                Hashtable<Double, ArrayList<Double>> fgod = model.computeGOD(threshold, false);
                all_fgod.add(fgod);
                Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
                all_avg_fgod.add(avgGOD);
//                model.writeFGOD(xmlpath, fgod);
//                xmlpath = dir + "/avg_fog_" + from + "_" + to + ".csv";
//                model.writeAvgGOD(xmlpath, model.avgGOD(fgod));

                cal1.add(Calendar.HOUR_OF_DAY, increment);
            }
            
            System.out.printf("%f,%f\n", model.find_all_linear_regression(reduce(all_fgod),false), model.find_avg_linear_regression(reduce(all_avg_fgod),false));
        }

    }

    public static Hashtable<Double, ArrayList<Double>> reduce(ArrayList<Hashtable<Double, ArrayList<Double>>> fgod) {
        Hashtable<Double, ArrayList<Double>> comp = new Hashtable<>();

        for (Iterator<Hashtable<Double, ArrayList<Double>>> iterator = fgod.iterator(); iterator.hasNext();) {
            Hashtable<Double, ArrayList<Double>> next = iterator.next();
            for (Map.Entry<Double, ArrayList<Double>> entrySet : next.entrySet()) {
                Double key = entrySet.getKey();
                ArrayList<Double> value = entrySet.getValue();
                if (comp.containsKey(key)) {
                    ArrayList<Double> comp_val = comp.get(key);
                    comp_val.addAll(value);
                    comp.put(key, comp_val);
                } else {
                    comp.put(key, value);
                }

            }

        }
        return comp;
    }
}

package Stops;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.Vertex;
import validation.GravityModelStops;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class Stop2 {

    /**
     * Delimeters used in the data representation.
     */
    final static String RLM = "/";
    final static String CLM = ",";

    final static int HOUR = 60;
    /**
     * Map sort parameters.
     */
    public static boolean ASC = true;
    public static boolean DESC = false;
    /**
     * The number of towers invoked.
     */
    final static int stower = 0;
    final static int etower = 300;
    /**
     * Trip detection parameters.
     */
    final static int dist_th = 1000;
    final static int time_th = 60;
    /**
     * minimum distance between O and D for the gravity model.
     */
    final static double threshold = dist_th;
    /**
     * Hourly increment values for hourly interval data.
     */
    final static int increment = 1;

    final static boolean KM = false;
    /**
     * Output flags.
     */
    final static boolean intercept = false;
    final static boolean verbose = true;
    /**
     * Data handling flags.
     */
    final static boolean weekends = true;
    final static boolean algorithm2 = false;
    final static boolean repeated_obs_flag = false;
//        final boolean log = true;

    /**
     *
     * Identify the stop points
     *
     * This is the exact implementation of the algorithm provided in AllAboard
     * paper
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs algorithm1(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        String stops = "";
        String tstops = "";

        /**
         * Buffers:
         * <\n buffer holds sequence of observations that did not meet
         * buffer clearance criterias.>
         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
         */
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> tbuffer = new ArrayList<>();

        double max_distance = 0;
        int time_diff = 0;
        for (int i = 0; i < towers.length; i++) {
            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
            for (int j = 0; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
//                System.out.println("b"+Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }

            }

            buffer.add(towers[i]);
            tbuffer.add(tstamps[i]);

            if (max_distance > dist_th) {

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                java.util.Date sTime = formatter.parse(tbuffer.get(0));
                java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

                if (time_diff > time_th) {
                    /**
                     * Add buffer mode to the stops
                     */
                    int index = modeIndex(buffer);
                    if (index != -1) {
//                        System.out.println("Find stop");
                        if (!stops.isEmpty()) {
                            stops += CLM;
                            tstops += CLM;
                        }
                        stops += buffer.get(index);
                        tstops += tbuffer.get(index);

                    }
                }
//                else {
                buffer = new ArrayList<>();
                tbuffer = new ArrayList<>();
                /**
                 * Reset maximum distances
                 */
                max_distance = 0;
//                }

            }

        }

        if (!buffer.isEmpty()) {
            /**
             * Add buffer mode to the stops
             */
            int index = modeIndex(buffer);
            if (index != -1) {
//                System.out.println("Find from the remaining buffer");
                if (!stops.isEmpty()) {
                    stops += CLM;
                    tstops += CLM;
                }
                stops += buffer.get(index);
                tstops += tbuffer.get(index);

            }

        }

//        System.out.println("stops:\t" + stops);
//        System.out.println("time stamps:\t" + tstops);
        return new Obs(stops, tstops);

    }

    /**
     *
     * This is the exact implementation of the algorithm provided in AllAboard
     * paper
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs algorithm2(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        String stops = "";
        String tstops = "";

        /**
         * Buffers:
         * <\n buffer holds sequence of observations that did not meet
         * buffer clearance criterias.>
         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
         */
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> tbuffer = new ArrayList<>();

        double max_distance = 0;
        int time_diff = 0;
        for (int i = 0; i < towers.length; i++) {
            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
            for (int j = 0; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
//                System.out.println("b"+Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }

            }

            buffer.add(towers[i]);
            tbuffer.add(tstamps[i]);

            if (max_distance > dist_th) {

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                java.util.Date sTime = formatter.parse(tbuffer.get(0));
                java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

                if (time_diff > time_th) {
                    /**
                     * Add buffer mode to the stops
                     */
                    if (!stops.isEmpty()) {
                        stops += CLM;
                        tstops += CLM;
                    }
                    System.out.print(buffer.get(buffer.size() - 1) + " , ");
                    stops += buffer.get(buffer.size() - 1);
                    tstops += tbuffer.get(buffer.size() - 1);

                    /**
                     * Reset buffers.
                     */
                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    /**
                     * Reset maximum distances
                     */
                    max_distance = 0;

                } else {

                    /**
                     * Add the first observation if we reset the buffer
                     */
                    if (stops.isEmpty()) {
                        System.out.print("( " + buffer.get(0) + " )" + " , ");
                        stops += buffer.get(0);
                    }
                    System.out.print(Arrays.toString(buffer.toArray(new String[buffer.size()])) + " , ");

                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    max_distance = 0;
                }

            }

        }

        if (!buffer.isEmpty()) {

            if (!stops.isEmpty()) {
                stops += CLM;
                tstops += CLM;
            }
            System.out.print(buffer.get(buffer.size() - 1));
            stops += buffer.get(buffer.size() - 1);
            tstops += tbuffer.get(buffer.size() - 1);

        }
        System.out.println();
//        System.out.println("stops:\t" + stops);
//        System.out.println("time stamps:\t" + tstops);
        return new Obs(stops, tstops);

    }

    /**
     *
     * This is the exact implementation of the algorithm provided in AllAboard
     * paper
     *
     * In this implementation we are using the last point in the stop sequences
     * to represent the whole history subseq.
     *
     * The number of decoded trajectories using this appoach dropped to 0.35
     * from about 0.60. This drop in the number of decoded trajectories is a
     * natural result to the reduction process of a number of adjacent zones.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs algorithm2_1(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        ArrayList<String> trips = new ArrayList<>();
        ArrayList<String> tstrips = new ArrayList<>();

        String stops = "";
        String tstops = "";

        /**
         * Buffers:
         * <\n buffer holds sequence of observations that did not meet
         * buffer clearance criterias.>
         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
         */
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> tbuffer = new ArrayList<>();

        double max_distance = 0;
        int time_diff = 0;
        for (int i = 0; i < towers.length; i++) {
            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
            for (int j = 0; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
//                System.out.println("b"+Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }

            }

            buffer.add(towers[i]);
            tbuffer.add(tstamps[i]);

            if (max_distance > dist_th) {

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                java.util.Date sTime = formatter.parse(tbuffer.get(0));
                java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

                if (time_diff > time_th) {

                    if (trips.isEmpty()) {
                        trips.add(buffer.get(buffer.size() - 1));
                        tstrips.add(tbuffer.get(buffer.size() - 1));
                    } else {
                        trips.add(buffer.get(buffer.size() - 1));
                        tstrips.add(tbuffer.get(buffer.size() - 1));
                        trips.add(RLM);
                        tstrips.add(RLM);
                        trips.add(buffer.get(buffer.size() - 1));
                        tstrips.add(tbuffer.get(buffer.size() - 1));
                    }
                    /**
                     * Reset buffers.
                     */
                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    /**
                     * Reset maximum distances
                     */
                    max_distance = 0;

                } else {

                    /**
                     * Add the first observation as the origin of the first
                     * trips and the remaining part of the buffer as the
                     * traveling observations, else add the complete buffer
                     * elements as the observation seq of the traveling
                     * observables.
                     */
                    trips.addAll(buffer);
                    tstrips.addAll(tbuffer);

                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    max_distance = 0;
                }

            }

        }

        if (!buffer.isEmpty()) {
            trips.add(buffer.get(buffer.size() - 1));
            tstrips.add(tbuffer.get(buffer.size() - 1));

        }

//        System.out.println("stops:\t" + Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));
//        System.out.println("time stamps:\t" + Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));
        return new Obs(Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""), Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));

    }

    /**
     * Not completed
     *
     * This is the exact implementation of the algorithm provided in AllAboard
     * paper
     *
     * In this implementation we are using the last point in the stop sequences
     * for the destination and the first point in the historical sub-sequence as
     * the origin of the new trip.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs algorithm2_2(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        ArrayList<String> trips = new ArrayList<>();
        ArrayList<String> tstrips = new ArrayList<>();

        /**
         * Buffers:
         * <\n buffer holds sequence of observations that did not meet
         * buffer clearance criterias.>
         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
         */
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> tbuffer = new ArrayList<>();

        double max_distance = 0;
        int time_diff = 0;
        for (int i = 0; i < towers.length; i++) {
            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
            for (int j = 0; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
//                System.out.println("b"+Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }

            }

            buffer.add(towers[i]);
            tbuffer.add(tstamps[i]);

            if (max_distance > dist_th) {

                try {
                    /**
                     * if the time exceeds timing threshold, then check the
                     * distance between towers. If this distance less than the
                     * distance threshold, then previous tower is the end of the
                     * current trip.
                     *
                     */
                    java.util.Date sTime = formatter.parse(tbuffer.get(0));
                    java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
                    cal.setTime(sTime);
                    int hour = cal.get(Calendar.HOUR);
                    int minute = cal.get(Calendar.MINUTE);

                    cal.setTime(eTime);
                    int ehour = cal.get(Calendar.HOUR);
                    int eminute = cal.get(Calendar.MINUTE);

                    time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

                    if (time_diff > time_th) {

                        if (trips.isEmpty()) {
                            trips.add(buffer.get(buffer.size() - 1));
                            tstrips.add(tbuffer.get(buffer.size() - 1));
                        } else {
                            trips.add(buffer.get(0));
                            tstrips.add(tbuffer.get(0));
                            trips.add(RLM);
                            tstrips.add(RLM);
                            trips.add(buffer.get(buffer.size() - 1));
                            tstrips.add(tbuffer.get(buffer.size() - 1));
                        }
                        /**
                         * Reset buffers.
                         */
                        buffer = new ArrayList<>();
                        tbuffer = new ArrayList<>();
                        /**
                         * Reset maximum distances
                         */
                        max_distance = 0;

                    } else {

                        /**
                         * Add the first observation as the origin of the first
                         * trips and the remaining part of the buffer as the
                         * traveling observations, else add the complete buffer
                         * elements as the observation seq of the traveling
                         * observables.
                         */
                        if (trips.isEmpty()) {
                            trips.add(buffer.get(buffer.size() - 1));
                            tstrips.add(tbuffer.get(buffer.size() - 1));
                        }

//                        trips.addAll(buffer);
//                        tstrips.addAll(tbuffer);
                        buffer = new ArrayList<>();
                        tbuffer = new ArrayList<>();
                        max_distance = 0;
                    }
                } catch (ParseException parseException) {
                    System.err.println("ParseException\t" + parseException.getMessage());
                }

            }

        }

        if (!buffer.isEmpty()) {
            trips.add(buffer.get(0));
            tstrips.add(tbuffer.get(0));

        }

//        System.out.println("stops:\t" + Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));
//        System.out.println("time stamps:\t" + Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));
        return new Obs(Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""), Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));

    }

    /**
     * Calculate the Eculidean distance.
     *
     * @param x
     * @param x1
     * @param y
     * @param y1
     * @return
     */
    private static double eculidean(double x, double x1, double y, double y1) {
        return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
    }

    /**
     * avg daily
     *
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {
        String dataset_path = "/home/essam/traffic/SET2/tmp";
        String results = "/home/essam/traffic/stops/";
        String towerPath = "/home/essam/traffic/Dakar/senegal/towers.csv";
//        dist_th = Integer.parseInt(args[0]) * 1000;
//        final double threshold = Integer.parseInt(args[1]) * 1000;
        String data_dir = "Result_";
        int days_cnt = 0;
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towerPath);
        GravityModelStops model = new GravityModelStops();
        model.calcDistances(towers, KM);
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                ObsTripsBuilder builder = new ObsTripsBuilder();
                Hashtable<String, Hashtable<Integer, Obs>> obs_table;

                if (repeated_obs_flag) {
                    obs_table = builder.transposeWUT(builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stower, etower)));
                } else {
                    obs_table = builder.transposeWUT(builder.remove_repeated(builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stower, etower))));
                }
                Hashtable<String, Hashtable<Integer, Obs>> obs_stops;
                if (algorithm2) {
                    obs_stops = Update_obs_table(obs_table, towers, weekends, algorithm2);
                } else {
                    obs_stops = Update_obs_table(obs_table, towers, weekends, algorithm2);
                }

                days_cnt += obs_stops.size();
                model.handle_zones_Flow(obs_stops);

            }

        }

        /**
         * Avg flow
         */
//        model.average_flow(days_cnt);
        Hashtable<Double, ArrayList<Double>> fgod;
        if (repeated_obs_flag) {
            data_dir += "with_repeated_obs_";
        } else {
            data_dir += "without_repeated_obs_";
        }
        if (algorithm2) {
            data_dir += "algorithm2_";
        } else {
            data_dir += "algorithm1_";
        }

        double god_threshold = threshold;
        data_dir += "god_threshold_" + god_threshold;

        if (verbose) {
            /**
             * Print verbose flow data
             */
            System.out.println("-------------------------------------------------");
            System.out.println("Number of days" + days_cnt);
            System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "From-zone", "To-zone", "Dist", "Flow", "Outs", "Ins", "GOD");
            fgod = model.computeGOD(god_threshold, verbose);
            System.out.println("-------------------------------------------------");
        } else {
            fgod = model.computeGOD(god_threshold, verbose);
        }
        results += data_dir + "/";
        String fgod_path = results + "fgod." + days_cnt + ".days.csv";
        model.writeFGOD(fgod_path, fgod);
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
        fgod_path = results + "avg.fgod." + days_cnt + ".days.csv";
        model.writeAvgGOD(fgod_path, avgGOD);
        System.out.printf("Number of days %d\tGravity vs. flow R^2=%f\tAvg Gravity vs. flow R^2=%f\n", days_cnt, model.find_all_linear_regression(fgod, intercept), model.find_avg_linear_regression(avgGOD, intercept));

    }

    /**
     * Get the median index of an array list
     *
     * @param list
     * @return
     */
    public static int modeIndex(ArrayList<String> list) {
        int index = -1;
        /**
         * If the list is empty return -1 as the median index, else if list size
         * is odd, make index it equal size/2+1 else make it equal size/2.
         */
        if (!list.isEmpty()) {
            list.trimToSize();
            int size = list.size();
            if (size % 2 != 0) {
                index = size / 2;
            } else {
                index = size / 2 - 1;
            }
        }
        return index;
    }

    /**
     * find stops of the whole observation table.
     *
     * @param obs_table
     * @param towers_list
     * @return
     * @throws ParseException
     */
    public static Hashtable<String, Hashtable<Integer, Obs>> Update_obs_table(
            Hashtable<String, Hashtable<Integer, Obs>> obs_table,
            Hashtable<Integer, Vertex> towers_list, boolean weekend, boolean trips_flag) throws ParseException {

        Hashtable<String, Hashtable<Integer, Obs>> obs_stops_table = new Hashtable<>();
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
            String day_key = entrySet.getKey();
            if (!weekend) {
                if (day_key.contains("Saturday") || day_key.contains("Sunday")) {
                    continue;
                }
            }
            Hashtable<Integer, Obs> usr_obs = entrySet.getValue();
            Hashtable<Integer, Obs> obs_tmp = new Hashtable<>();
            for (Map.Entry<Integer, Obs> entrySet1 : usr_obs.entrySet()) {
                Integer usr_key = entrySet1.getKey();
                Obs obs_val = entrySet1.getValue();
                String seq = obs_val.getSeq();
                String tstamps = obs_val.getTimeStamp();
                if (trips_flag) {
                    obs_val = algorithm2_2(seq.split(CLM), tstamps.split(CLM), towers_list);
                } else {
                    obs_val = algorithm1(seq.split(CLM), tstamps.split(CLM), towers_list);
                }

                obs_tmp.put(usr_key, obs_val);

            }
            obs_stops_table.put(day_key, obs_tmp);
        }
        return obs_stops_table;
    }
}

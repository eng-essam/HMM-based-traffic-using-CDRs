package Stops;

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
public class Stop1 {

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
    final static int etower = 400;
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
    final static boolean weekends =true;
    final static boolean trips_flag = false;
    final static boolean repeated_obs_flag = false;
//        final boolean log = true;

    /**
     * Buffers:
     * <\n buffer holds sequence of observations that did not meet
     * buffer clearance criterias.>
     * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
     */
    private static ArrayList<String> buffer;

    private static ArrayList<String> tbuffer;

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

    private static double find_max(Hashtable<Integer, Vertex> towersXY) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < buffer.size(); i++) {
            Vertex a = towersXY.get(Integer.parseInt(buffer.get(i)));
            for (int j = i + 1; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max) {
                    max = tmp_distance;
                }
            }
        }
        /**
         * if the current max is still bigger than the distance threshold remove
         * elements.
         */
        if (max > dist_th) {
            buffer.remove(0);
            tbuffer.remove(0);
            find_max(towersXY);
        }
        return max;
    }

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
    public static Obs findStops(String[] towers, String[] tstamps,
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
     * Implementation of the definition 2 in AllAboard.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs findStops_2(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY,
            int dist_th, int time_th) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        ArrayList<String> stops = new ArrayList<>();
        ArrayList<String> tstops = new ArrayList<>();

        /**
         * Buffers:
         * <\n buffer holds sequence of observations that did not meet
         * buffer clearance criterias.>
         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
         */
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> tbuffer = new ArrayList<>();

        buffer.add(towers[0]);
        tbuffer.add(tstamps[0]);

        double max_distance = Double.MIN_VALUE;
        for (int i = 1; i < towers.length; i++) {
            /**
             * Check the complete sequence time difference between starting and
             * end, if this time less than the timing threshold continue to the
             * next sequence else determine the trip.
             */

            Vertex tower = towersXY.get(Integer.parseInt(towers[i]));
            /**
             * if the time exceeds timing threshold, then check the distance
             * between towers. If this distance less than the distance
             * threshold, then previous tower is the end of the current trip.
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

            int diff = Math.abs((ehour - hour) * HOUR + (eminute - minute));
            /**
             * check time difference with time threshold whatever the distance
             * between the starting tower
             */
            if (diff > time_th) {
                /**
                 * Check the max distance, if it distance less than distance
                 * threshold mark the current tower as the start of a new trip.
                 */

                for (int j = 0; j < buffer.size(); j++) {
                    Vertex sTower = towersXY.get(Integer.parseInt(buffer.get(j)));
                    double dist = eculidean(sTower.getX(), tower.getX(), sTower.getY(), tower.getY());
                    if (dist > max_distance) {
                        max_distance = dist;
                    }
                }

                if (max_distance < dist_th) {
                    /**
                     * Add stop.
                     */

                    int index = modeIndex(buffer);
                    if (index != -1) {
                        stops.add(buffer.get(index));
                        tstops.add(tbuffer.get(index));

                    }

                }

                /**
                 * reset buffers.
                 */
                max_distance = Double.MIN_VALUE;
                buffer.clear();
                tbuffer.clear();
                /**
                 * add the last tower.
                 */
                buffer.add(towers[i]);
                tbuffer.add(tstamps[i]);
            } else {
                /**
                 * if time difference is small keep adding towers to the
                 * buffers.
                 */
                buffer.add(towers[i]);
                tbuffer.add(tstamps[i]);

            }
//              
        }

        if (!buffer.isEmpty()) {
            /**
             * Add buffer mode to the stops
             */
            int index = modeIndex(buffer);
            if (index != -1) {
//                System.out.println(Arrays.toString(buffer.toArray(new String[buffer.size()])));

                stops.add(buffer.get(index));
                tstops.add(tbuffer.get(index));

            }

        }

        String stops_str = stops.toString().replace(" ", "").replace("[", "").replace("]", "");
        String tstops_str = tstops.toString().replace(" ", "").replace("[", "").replace("]", "");

        return new Obs(stops_str, tstops_str);

    }

    /**
     *
     * Identify the stop points
     *
     * Gradual clear of the buffer.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs findStops_3(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        String stops = "";
        String tstops = "";

        buffer = new ArrayList<>();
        tbuffer = new ArrayList<>();

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
                        System.out.println("Find stop");
                        if (!stops.isEmpty()) {
                            stops += CLM;
                            tstops += CLM;
                        }
                        stops += buffer.get(index);
                        tstops += tbuffer.get(index);

                    }

                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    /**
                     * Reset maximum distances
                     */
                    max_distance = 0;
                } else {
                    /**
                     * iteratively clear elements from the beginning of the
                     * buffer.
                     */
                    buffer.remove(0);
                    tbuffer.remove(0);
//                    System.out.println("current max:\t" + max_distance);
                    max_distance = find_max(towersXY);
//                    System.out.println("new max:\t" + max_distance);
                }

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
     * Identify trips using stop buffers.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public static Obs findStops_trips(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        String stops = towers[0];
        String tstops = tstamps[0];

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
//                    if (buffer.size() >= trip_length) {
                    if (!stops.isEmpty()) {
                        stops += CLM;
                        tstops += CLM;
                    }
                    /**
                     * Add start and end of the trips to the stop sequences
                     */
                    stops += buffer.get(buffer.size() - 1);
                    tstops += tbuffer.get(tbuffer.size() - 1);

//                    }
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
//            if (buffer.size() >= trip_length) {
            if (!stops.isEmpty()) {
                stops += CLM;
                tstops += CLM;
            }
            /**
             * Add start and end of the trips to the stop sequences
             */
            stops += buffer.get(buffer.size() - 1);
            tstops += tbuffer.get(tbuffer.size() - 1);

//            }
        }

//        System.out.println("stops:\t" + stops);
//        System.out.println("time stamps:\t" + tstops);
        return new Obs(stops, tstops);

    }

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
                String seq = observations.getSeq();
                String timeStamp = observations.getTimeStamp();

                if (seq.isEmpty()) {
                    continue;
                }

                String[] pnts = seq.split(CLM);
                String[] tstamps = timeStamp.split(CLM);

                String path = "";
                String time = "";
                for (int j = 0; j < tstamps.length; j++) {
                    String tstamp = tstamps[j];
                    cal.setTime(formatter.parse(tstamp));
                    /**
                     * If current time stamp in between the require period add
                     * it to the path.
                     */
                    if ((fromH.before(cal) && toH.after(cal))) {
                        if (!path.isEmpty()) {
                            time.concat(CLM);
                            path.concat(CLM);
                        }
                        time.concat(tstamp);
                        path.concat(pnts[j]);
                    }

                }

                if (!path.isEmpty()) {
                    seq_hour.add(path);
                    tseq_hour.add(time);
                }

            }
        }
        return seq_hour;
    }

    /**
     * avg daily
     *
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {
        String dataset_path = "/home/essam/traffic/SET2/tmp";
        String results = "/home/essam/traffic/stops/without_repeated_obs_May-25/";
        String towerPath = "/home/essam/traffic/Dakar/senegal/towers.csv";
//        dist_th = Integer.parseInt(args[0]) * 1000;
//        final double threshold = Integer.parseInt(args[1]) * 1000;

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

                Hashtable<String, Hashtable<Integer, Obs>> obs_stops = Update_obs_table(obs_table, towers, weekends, trips_flag);
                days_cnt += obs_stops.size();
                model.handle_zones_Flow(obs_stops);

            }

        }

        /**
         * Avg flow
         */
//        model.average_flow(days_cnt);

        Hashtable<Double, ArrayList<Double>> fgod;
        
        if (verbose) {
            /**
             * Print verbose flow data
             */
            System.out.println("-------------------------------------------------");
            System.out.println("Number of days" + days_cnt);
            System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "From-zone", "To-zone", "Dist", "Flow", "Outs", "Ins", "GOD");
            fgod = model.computeGOD(threshold, verbose);
            System.out.println("-------------------------------------------------");
        } else {
            fgod = model.computeGOD(threshold, verbose);
        }
        String fgod_path = results + "data/fgod." + days_cnt + ".days.csv";
        model.writeFGOD(fgod_path, fgod);
        Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
        fgod_path = results + "data/avg.fgod." + days_cnt + ".days.csv";
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
     * Split 2 weeks data into daily hash-tables.
     *
     * @param obs
     * @return
     */
    public static ArrayList<Hashtable<String, Hashtable<Integer, Obs>>> split_obs_daily(Hashtable<String, Hashtable<Integer, Obs>> obs) {
        ArrayList<Hashtable<String, Hashtable<Integer, Obs>>> daily_obs = new ArrayList<>();

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<Integer, Obs> value = entrySet.getValue();
            Hashtable<String, Hashtable<Integer, Obs>> tmp = new Hashtable<>();
            tmp.put(key, value);
            daily_obs.add(tmp);
        }
        return daily_obs;
    }

    public static ArrayList<String> trips_hour_interval(Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {
        String t1 = "00:00:00";
        String t2 = "24:00:00";
        ArrayList<String> trips = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(formatter.parse(t1));
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(formatter.parse(t2));

        ArrayList<Hashtable<Double, ArrayList<Double>>> all_fgod = new ArrayList<>();
        ArrayList<Hashtable<Double, ArrayList<Double>>> all_avg_fgod = new ArrayList<>();
        while (cal2.after(cal1)) {
            String from = cal1.get(Calendar.HOUR_OF_DAY) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);
            Calendar tmp = Calendar.getInstance();;
            String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" + cal1.get(Calendar.MINUTE) + ":" + cal1.get(Calendar.SECOND);;
            tmp.setTime(formatter.parse(to));
            ArrayList<String> dtable = getHTrips(cal1, tmp, obs);
            trips.addAll(dtable);
            cal1.add(Calendar.HOUR_OF_DAY, increment);
        }
        return trips;
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
                    obs_val = findStops_trips(seq.split(CLM), tstamps.split(CLM), towers_list);
                } else {
                    obs_val = findStops(seq.split(CLM), tstamps.split(CLM), towers_list);
                }

                obs_tmp.put(usr_key, obs_val);

            }
            obs_stops_table.put(day_key, obs_tmp);
        }
        return obs_stops_table;
    }
}

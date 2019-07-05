
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.Vertex;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class Stop_Detection_0 {

    final static String CLM = ",";

    final static int HOUR = 60;
    final static String RLM = "/";
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
     * Identify the stop points
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
        String trip = "";
        String ttrip = "";
        /**
         * construct observations and time stamps
         */
        String time = "";
        String obs = "";

        double max_distance = 0;
        int time_diff = 0;
        for (int i = 0; i < towers.length; i++) {
            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
            for (int j = 0; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }

            }
//            System.out.println(max_distance);
            if (max_distance > dist_th) {
//                System.out.println(max_distance);
                /**
                 * get time difference
                 */

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                Date sTime = formatter.parse(tstamps[i]);
                Date eTime = formatter.parse(tbuffer.get(0));
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                time_diff = (ehour - hour) * HOUR + (eminute - minute);

                if (time_diff > time_th) {
                    /**
                     * Add buffer mode to the stops
                     */
                    int index = modeIndex(buffer);
                    if (index != -1) {
                        System.out.println(Arrays.toString(buffer.toArray(new String[buffer.size()])));
                        stops.add(buffer.get(index));
                        tstops.add(tbuffer.get(index));
                        if (!trip.isEmpty()) {
                            trip += CLM;
                            ttrip += CLM;
                        }
                        trip += buffer.get(index);
                        ttrip += tbuffer.get(index);
                    }

                    /**
                     * Add trips and time stamps to the observation sequences
                     * then clear trips and time_trips string buffers.
                     *
                     */
//                    System.out.println(Arrays.toString(towers));
//                    System.out.println("Trip:" + trip);
//                    System.out.println("Trip:" + ttrip);
                    if (!trip.isEmpty()) {
                        System.out.println("\\-end");
                        trip += RLM;
                        ttrip += RLM;
                    }
                    time += ttrip;
                    obs += trip;

                    trip = "";
                    ttrip = "";
                } else {
                    for (int j = 0; j < tbuffer.size(); j++) {

                        if (!trip.isEmpty()) {
                            trip += CLM;
                            ttrip += CLM;
                        }
                        trip += buffer.get(j);
                        ttrip += tbuffer.get(j);
                    }
//                    trip += Arrays.toString(buffer.toArray(new String[buffer.size()]));
                }
                buffer = new ArrayList<>();
                tbuffer = new ArrayList<>();
                /**
                 * Reset maximum distances
                 */
                max_distance = 0;

            }
            buffer.add(towers[i]);
            tbuffer.add(tstamps[i]);

        }

        if (!buffer.isEmpty()) {
            /**
             * Add buffer mode to the stops
             */
            int index = modeIndex(buffer);
            if (index != -1) {
                System.out.println(Arrays.toString(buffer.toArray(new String[buffer.size()])));

                stops.add(buffer.get(index));
                tstops.add(tbuffer.get(index));
                if (!trip.isEmpty()) {
                    trip += CLM;
                    ttrip += CLM;
                }
                trip += buffer.get(index);
                ttrip += tbuffer.get(index);
            }
            //Record the last trip
            time += ttrip;
            obs += trip;
        }

        System.out.println(obs + "\n" + time);
        return new Obs(obs, time);

    }

    /**
     * Find Trips as the following: If the time difference greater than temperal
     * threshold, check the distance if it smaller than spatial threshold then
     * the current current moving very slowly or stop at this point, so mark it
     * as the end of the current trip.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     */
    public static Obs findTrips(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY,
            int dist_th, int time_th) {
        /**
         * Marks array contain index of the towers that represent the starting
         * observation of a new trip.
         */
        boolean[] marks = new boolean[tstamps.length];
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        /**
         * add 0 as the start of the first tower in this seq.
         */
//        int mIndex = 0;
//        marks[mIndex++] = 0;

        int current = 0;
        for (int i = 1; i < tstamps.length; i++) {
            try {
                String tstamp = tstamps[i];

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                Date sTime = formatter.parse(tstamps[current]);
                Date eTime = formatter.parse(tstamp);
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                int diff = (ehour - hour) * HOUR + (eminute - minute);
                /**
                 * check time difference with time threshold whatever the
                 * distance between the starting tower
                 */
                if (diff > time_th) {
                    /**
                     * Check distance, if it distance less than distance
                     * threshold mark the current tower as the start of a new
                     * trip.
                     */
                    Vertex sTower = towersXY.get(Integer.parseInt(towers[current]));
                    Vertex tower = towersXY.get(Integer.parseInt(towers[i]));

                    if (eculidean(sTower.getX(), tower.getX(), sTower.getY(), tower.getY()) < dist_th) {
                        /**
                         * Update the trip sequences
                         */
                        marks[i] = true;
                        current = i;
                    }

                }
            } catch (ParseException ex) {
                Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * construct observations and time stamps
         */
        String time = "";
        String obs = "";
        for (int i = 0; i < marks.length; i++) {
            if (marks[i]) {

                time += RLM + tstamps[i];
                obs += RLM + towers[i];
            } else {
                /**
                 * Add comma separators
                 */
                if (!time.isEmpty()) {
                    time += CLM;
                    obs += CLM;
                }
                time += tstamps[i];
                obs += towers[i];
            }

        }
        System.out.println(obs + "\n" + time);
        return new Obs(obs, time);
    }

    public static void main(String[] args) throws ParseException {
        int stower = 0;
        int etower = 500;
        int dist_th = 1000;
        int time_th = 60;
        String towerPath = "/home/essam/traffic/Dakar/N_Darak/Dakar.vor.towers.csv";
        String[] obs = "462,461,454,461,461,461,461,461,461,461,461,461,461,457,457,461,462,462,458,462,462,462,462,462,462,462,462,462,462,462,454,462,462,462,454,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462,462".split(CLM);
        String[] tstamps = "8:10:0,10:40:0,11:20:0,13:20:0,13:20:0,13:20:0,13:40:0,15:50:0,15:50:0,15:50:0,15:50:0,16:0:0,16:20:0,16:50:0,17:0:0,17:10:0,17:10:0,17:10:0,17:20:0,17:50:0,18:30:0,18:30:0,18:40:0,18:40:0,18:40:0,18:40:0,18:40:0,19:0:0,19:0:0,20:20:0,20:20:0,20:20:0,20:30:0,20:30:0,20:30:0,20:40:0,20:50:0,20:50:0,20:50:0,21:0:0,21:0:0,21:10:0,21:10:0,21:20:0,21:30:0,21:30:0,21:30:0,21:40:0,21:40:0,21:50:0,22:10:0,22:10:0,22:40:0,22:50:0,23:0:0,23:10:0,23:40:0,23:50:0,23:50:0,23:50:0".split(CLM);
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towerPath);

        Obs s_out = findStops(obs, tstamps, towers, dist_th, time_th);
        Obs t_out = findTrips(obs, tstamps, towers, dist_th, time_th);
//        System.out.println(out.getSeq());
//        System.out.println(out.getTimeStamp());
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
}

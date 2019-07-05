
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import Observations.Obs;
import utils.DataHandler;
import utils.Vertex;

/*
 * The idea of the high pass is to allow high vilocity, which mean that moving from one tower to another in a short time (The inter-event )
 */
/**
 *
 * @author essam
 */
public class HighPass {

    final static String CLM = ",";

    final static int HOUR = 60;
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

        /**
         * for test
         */
//        buffer.add(towers[0]);
//        tbuffer.add(tstamps[0]);
        double max_distance = 0;
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

                int time_diff = (ehour - hour) * HOUR + (eminute - minute);

                if (time_diff > time_th) {
                    /**
                     * Add buffer mode to the stops
                     */
                    int index = modeIndex(buffer);
                    if (index != -1) {
                        System.out.println(Arrays.toString(buffer.toArray(new String[buffer.size()])));
                        stops.add(buffer.get(index));
                        tstops.add(tbuffer.get(index));
                    }

                } else {
                    /**
                     * Clear Buffers
                     */
                    System.out.println("clear");
                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    /**
                     * Reset maximum distances
                     */
                    max_distance = 0;
                }

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
            }
        }

        /**
         * construct observations and time stamps
         */
        String time = "";
        String obs = "";

        for (int i = 0; i < stops.size(); i++) {
            String s = stops.get(i);
            String t = tstops.get(i);

            if (!time.isEmpty()) {
                time += CLM;
                obs += CLM;
            }
            time += t;
            obs += s;
        }
        return new Obs(obs, time);

    }

    public static void main(String[] args) throws ParseException {
        int stower = 0;
        int etower = 500;
        int dist_th = 1000;
        int time_th = 60;
        String towerPath = "/home/essam/traffic/Dakar/N_Darak/Dakar.vor.towers.csv";
        String[] obs = "291,297,297,297,297,292,292,297,297,297,292,240,297,297,423,423,423,429,281,285,281,273,273".split(CLM);
        String[] tstamps = "9:50:0,10:20:0,11:0:0,11:10:0,11:20:0,11:40:0,11:40:0,12:20:0,12:20:0,13:10:0,13:40:0,13:50:0,14:50:0,15:10:0,16:30:0,16:40:0,16:40:0,18:20:0,21:0:0,21:10:0,23:30:0,23:30:0,23:30:0".split(CLM);
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towerPath);

        Obs out = findStops(obs, tstamps, towers, dist_th, time_th);
        System.out.println(out.getSeq());
        System.out.println(out.getTimeStamp());
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

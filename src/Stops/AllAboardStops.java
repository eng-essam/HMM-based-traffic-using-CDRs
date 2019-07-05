package Stops;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author essam
 */
public class AllAboardStops {

    final static String CLM = ",";
    final static int HOUR = 60;
    final static int dist_th = 3000;
    final static int time_th = 60;

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
    public static String findStops(String[] towers, String[] tstamps,
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
                    System.out.println(buffer.toString() + "\t" + tbuffer.toString() + "\t" + buffer.get(index));
                    if (index != -1) {
                        System.out.println("Find stop");
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
            System.out.println(buffer.toString() + "\t" + tbuffer.toString() + "\t" + buffer.get(index));

            if (index != -1) {
                System.out.println("Find from the remaining buffer");
                if (!stops.isEmpty()) {
                    stops += CLM;
                    tstops += CLM;
                }
                stops += buffer.get(index);
                tstops += tbuffer.get(index);

            }

        }

        System.out.println("stops:\t" + stops);
        System.out.println("time stamps:\t" + tstops);
        return stops;

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
    public static String findStops_3(String[] towers, String[] tstamps,
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
                    System.out.println(buffer.toString() + "\t" + tbuffer.toString() + "\t" + buffer.get(index));

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
            System.out.println(buffer.toString() + "\t" + tbuffer.toString() + "\t" + buffer.get(index));

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

        System.out.println("stops:\t" + stops);
        System.out.println("time stamps:\t" + tstops);
        return stops;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        String towerPath = args[0];
        String towers[] = args[1].split(CLM);
        String tstamps[] = args[2].split(CLM);
        Hashtable<Integer, Vertex> towers_list = readTowers(towerPath);
        System.out.println("------------- AllAboard Algorithm -------------------------------------------------");
        findStops(towers, tstamps, towers_list);

        System.out.println("------------- AllAboard Algorithm with iterative elements removal------------------");
        findStops_3(towers, tstamps, towers_list);

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
     * Read towers list.
     *
     * @param path
     * @return
     */
    public static Hashtable<Integer, Vertex> readTowers(String path) {
        Hashtable<Integer, Vertex> towers = new Hashtable<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            Vertex pnt = null;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(CLM);
                pnt = new Vertex();
                int index = Integer.parseInt(lineSplit[0]);
                pnt.setX(Double.parseDouble(lineSplit[1]));
                pnt.setY(Double.parseDouble(lineSplit[2]));
//                System.out.printf("%f,%f",pnt.x,pnt.y);
                towers.put(index, pnt);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AllAboardStops.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AllAboardStops.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return towers;
    }

}

class Vertex {

    double x;
    double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

}

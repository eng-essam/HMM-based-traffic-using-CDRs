/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class POI {

    final String RLM = "/";
    final String CLM = ",";

    /**
     * Convert observations into vertices
     *
     * @param obs_tbl
     * @param towers
     * @return
     */
    private Hashtable<String, Hashtable<Integer, Obs>> convert_obs(Hashtable<String, Hashtable<Integer, Obs>> obs_tbl, Hashtable<Integer, Vertex> towers) {
        Hashtable<String, Hashtable<Integer, Obs>> con_obs_tbl = new Hashtable<>();
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_tbl.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<Integer, Obs> day_obs_tbl = entrySet.getValue();
            Hashtable<Integer, Obs> t_day_obs_tbl = new Hashtable<>();
            for (Map.Entry<Integer, Obs> day_entry_set : day_obs_tbl.entrySet()) {
                Integer usr_id = day_entry_set.getKey();
                String obs_str[] = day_entry_set.getValue().getSeq().replace(RLM, CLM).split(CLM);
                String t_obs_str = "";
                for (int i = 0; i < obs_str.length; i++) {
                    Vertex v = towers.get(Integer.parseInt(obs_str[i]));
                    if (!t_obs_str.isEmpty()) {
                        t_obs_str += " ";
                    }
                    String t = v.getX() + "," + v.getY();
                    t_obs_str += t;
//                    System.out.println();
                }
                t_day_obs_tbl.put(usr_id, new Obs(t_obs_str, ""));
//                System.out.println(t_obs_str);
            }
            con_obs_tbl.put(key, t_day_obs_tbl);
        }
        return con_obs_tbl;
    }

    /**
     * Calculate distance between two cartesian points
     *
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @return
     */
    private double euclidean(double x, double y, double x1, double y1) {
        return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
    }

    public DoublePoint get_centriod(List<DoublePoint> pnts) {
        DoublePoint p = null;
        double x = 0, y = 0;
        if (pnts.size() == 1) {
            return pnts.get(0);
        }
        //get the geomatric center of a set of points
        for (Iterator<DoublePoint> iterator = pnts.iterator(); iterator.hasNext();) {
            double[] nxt = iterator.next().getPoint();
            x += nxt[0];
            y += nxt[1];
        }
        x /= 2;
        y /= 2;
        double min = Double.MAX_VALUE;
        // get the actual get_centriod by finding the distance from the geometric points and existance towers. 
        for (Iterator<DoublePoint> iterator = pnts.iterator(); iterator.hasNext();) {
            DoublePoint tp = iterator.next();
            double[] nxt = tp.getPoint();
            double dist = euclidean(x, y, nxt[0], nxt[1]);
            if (dist < min) {
                min = dist;
                p = tp;
            }
        }
        return p;

    }

    /**
     * extract only a sample of user observation that fall within a specific
     * range of a towers .
     *
     * @param obs
     * @param stwr
     * @param etwr
     * @param sample_size
     * @return
     */
    public Hashtable<Integer, List<Integer>> get_com_usr_sample(Hashtable<Integer, List<Integer>> obs, int stwr, int etwr) {
        Hashtable<Integer, List<Integer>> sample = new Hashtable<>();
        for (Map.Entry<Integer, List<Integer>> entrySet : obs.entrySet()) {
            Integer key = entrySet.getKey();
            List<Integer> obs_lst = entrySet.getValue();
            boolean flag = true;
            for (Iterator<Integer> iterator = obs_lst.iterator(); iterator.hasNext();) {
                int t = iterator.next();
                if (!(t > stwr && t < etwr)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                // add to the sample ..
                sample.put(key, obs_lst);
            }

        }

        return sample;
    }

    public Hashtable<Integer, List<Cluster<DoublePoint>>> get_poi_clusters(String dataset_path, Hashtable<Integer, Vertex> towers, double theta, int min_pnts) {
        DBSCANClusterer dbscan = new DBSCANClusterer(theta, min_pnts);

        Hashtable<Integer, List<DoublePoint>> u_data = unlimited_combine(getObs(dataset_path), towers);
        Hashtable<Integer, List<Cluster<DoublePoint>>> u_poi = new Hashtable<>();

        for (Map.Entry<Integer, List<DoublePoint>> entrySet : u_data.entrySet()) {
            Integer usr_key = entrySet.getKey();
            List<Cluster<DoublePoint>> cluster = dbscan.cluster(entrySet.getValue());
            u_poi.put(usr_key, cluster);
        }
        return u_poi;
    }

    public Hashtable<String, Hashtable<Integer, Obs>> getObs(String dataset_path) {

        ObsTripsBuilder builder = new ObsTripsBuilder();
        Hashtable<String, Hashtable<Integer, Obs>> total_obs_table = new Hashtable<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                Hashtable<String, Hashtable<Integer, Obs>> obs_table = builder.transposeWUT(builder.remove_handovers(builder.read_obs_data(subsetPath)));
                total_obs_table.putAll(obs_table);
            }
        }

        System.out.println("Dataset size: " + total_obs_table.size());
        return total_obs_table;
    }

    public Hashtable<String, Hashtable<Integer, Obs>> getObs(String dataset_path, int stwr, int etwr) {

        ObsTripsBuilder builder = new ObsTripsBuilder();
        Hashtable<String, Hashtable<Integer, Obs>> total_obs_table = new Hashtable<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                Hashtable<String, Hashtable<Integer, Obs>> obs_table = builder.read_obs_data(subsetPath, stwr, etwr);
                total_obs_table.putAll(obs_table);
            }
        }

//        System.out.println("Dataset size: " + total_obs_table.size());
        return total_obs_table;
    }

    public Hashtable<Integer, String> limited_combine(Hashtable<String, Hashtable<Integer, Obs>> obs_table) throws ParseException {
        Hashtable<Integer, String> udata = new Hashtable<>();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

//        java.util.Date sTime = formatter.parse(tbuffer.get(0));
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
            String day = entrySet.getKey();
            if (day.endsWith("Friday") || day.endsWith("Satureday") || day.endsWith("Sunday")) {
                continue;
            }

            Hashtable<Integer, Obs> users_data = entrySet.getValue();
            for (Map.Entry<Integer, Obs> entrySet1 : users_data.entrySet()) {
                Integer id = entrySet1.getKey();
                String tws[] = entrySet1.getValue().getSeq().split(",");
                String ts[] = entrySet1.getValue().getTimeStamp().split(",");
                String value = "";
                for (int i = 0; i < ts.length; i++) {
                    Date time = formatter.parse(ts[i]);
                    cal.setTime(time);
                    if (cal.get(Calendar.HOUR_OF_DAY) > 6 && cal.get(Calendar.HOUR_OF_DAY) < 22) {
                        if (!value.isEmpty()) {
                            value += ",";
                        }
                        value += tws[i];
                    }

                }

                if (udata.containsKey(id)) {
                    String t;
                    if (udata.get(id).isEmpty()) {
                        t = value;
                    } else {
                        t = udata.get(id) + "," + value;
                    }
                    udata.replace(id, t);
                } else {
                    udata.put(id, value);
                }

            }
        }

        //print 
        for (Map.Entry<Integer, String> entrySet : udata.entrySet()) {
            Integer key = entrySet.getKey();
            String value = entrySet.getValue();
            System.out.println(value);

        }

        return udata;
    }

    /**
     * mirror the towers list to be used to handle the obtained clusters ...
     *
     * @param towers
     * @return
     */
    public Hashtable<DoublePoint, Integer> mirror_towers(Hashtable<Integer, Vertex> towers) {
        Hashtable<DoublePoint, Integer> mt = new Hashtable<>();
        for (Map.Entry<Integer, Vertex> entrySet : towers.entrySet()) {
            Integer key = entrySet.getKey();
            Vertex v = entrySet.getValue();
            mt.put(new DoublePoint(new double[]{v.getX(), v.getY()}), key);

        }
        return mt;
    }

    public Hashtable<Integer, List<DoublePoint>> unlimited_combine(Hashtable<String, Hashtable<Integer, Obs>> obs_table, Hashtable<Integer, Vertex> towers) {
        Hashtable<Integer, List<DoublePoint>> udata = new Hashtable<>();

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
            String day = entrySet.getKey();
            if (day.endsWith("Friday") || day.endsWith("Satureday") || day.endsWith("Sunday")) {
                continue;
            }

            Hashtable<Integer, Obs> users_data = entrySet.getValue();
            for (Map.Entry<Integer, Obs> entrySet1 : users_data.entrySet()) {
                Integer id = entrySet1.getKey();
                String tws[] = entrySet1.getValue().getSeq().split(",");

                // restore user data ...
                List<DoublePoint> u_twrs;
                if (udata.containsKey(id)) {
                    u_twrs = udata.get(id);
                } else {
                    u_twrs = new ArrayList<>();
                }

                for (int i = 0; i < tws.length; i++) {
                    Vertex twr = towers.get(Integer.parseInt(tws[i]));
                    u_twrs.add(new DoublePoint(new double[]{twr.getX(), twr.getY()}));
                }

                // update stored data
                if (udata.containsKey(id)) {
                    udata.replace(id, u_twrs);
                } else {
                    udata.put(id, u_twrs);
                }

            }
        }
        System.out.println("The number of users: " + udata.size());
        return udata;
    }

    public Hashtable<Integer, List<Integer>> unlimited_combine_twr(Hashtable<String, Hashtable<Integer, Obs>> obs_table) {
        Hashtable<Integer, List<Integer>> udata = new Hashtable<>();

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
            String day = entrySet.getKey();
            if (day.endsWith("Friday") || day.endsWith("Satureday") || day.endsWith("Sunday")) {
                continue;
            }

            Hashtable<Integer, Obs> users_data = entrySet.getValue();
            for (Map.Entry<Integer, Obs> entrySet1 : users_data.entrySet()) {
                Integer id = entrySet1.getKey();
                String tws[] = entrySet1.getValue().getSeq().split(",");

                // restore user data ...
                List<Integer> u_twrs;
                if (udata.containsKey(id)) {
                    u_twrs = udata.get(id);
                } else {
                    u_twrs = new ArrayList<>();
                }

                for (int i = 0; i < tws.length; i++) {
                    u_twrs.add(Integer.parseInt(tws[i]));
                }

                // update stored data
                if (udata.containsKey(id)) {
                    udata.replace(id, u_twrs);
                } else {
                    udata.put(id, u_twrs);
                }

            }
        }
        System.out.println("The number of users: " + udata.size());
        return udata;
    }

    public void write_history(Hashtable<Integer, List<Integer>> u_poi, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));

            writer.write("nusers\n");
            writer.write(u_poi.size() + "\n");
            for (Map.Entry<Integer, List<Integer>> entrySet : u_poi.entrySet()) {
                Integer key = entrySet.getKey();
                writer.write("user\n");
                writer.write(key + "\n");
                List<Integer> value = entrySet.getValue();
                writer.write("nvPoints\n" + value.size() + "\n");
                writer.write(value.get(0).toString());
                for (int i = 1; i < value.size(); i++) {
                    Integer get = value.get(i);
                    writer.write("," + value.get(i));
                }
                writer.newLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public void write_obs_coordinates(String dataset_path, Hashtable<Integer, Vertex> towers, String path) {
        Hashtable<String, Hashtable<Integer, Obs>> obs = convert_obs(getObs(dataset_path), towers);
        writeObs_con_towers(obs, path);
    }

    public void write_poi(Hashtable<Integer, List<Cluster<DoublePoint>>> u_poi, Hashtable<DoublePoint, Integer> m_towers, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));

            writer.write("nusers\n");
            writer.write(u_poi.size() + "\n");
            for (Map.Entry<Integer, List<Cluster<DoublePoint>>> entrySet : u_poi.entrySet()) {
                Integer key = entrySet.getKey();
                writer.write("user\n");
                writer.write(key + "\n");
                List<Cluster<DoublePoint>> value = entrySet.getValue();
                writer.write("nclusters\n" + value.size() + "\n");
                for (Iterator<Cluster<DoublePoint>> iterator = value.iterator(); iterator.hasNext();) {
                    List<DoublePoint> pnts = iterator.next().getPoints();
//                    DoublePoint c = get_centriod(pnts);
                    writer.write(m_towers.get(get_centriod(pnts)).toString());
                    for (Iterator<DoublePoint> iterator1 = pnts.iterator(); iterator1.hasNext();) {
                        DoublePoint next = iterator1.next();
                        writer.write("," + m_towers.get(next));
                    }
                    writer.newLine();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public void writeObs_con_towers(Hashtable<String, Hashtable<Integer, Obs>> obs, String path) {

        BufferedWriter writer = null;
        try {

            for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
                String daykey = entrySet.getKey();
                Hashtable<Integer, Obs> value = entrySet.getValue();
                File logFile = new File(path + "/" + daykey + ".csv");
                writer = new BufferedWriter(new FileWriter(logFile));
                writer.write("nSequences\n");
                writer.write(Integer.toString(obs.size()));
                writer.newLine();

                for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
                    Integer usr_id = entrySet1.getKey();
                    writer.write(Integer.toString(usr_id) + " ");
                    String value1 = entrySet1.getValue().getSeq();
                    writer.write(value1);
                    writer.newLine();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }
    
    

}

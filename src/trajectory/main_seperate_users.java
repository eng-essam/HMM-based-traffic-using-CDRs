/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_seperate_users {

    public static double xmin = 228134.41, xmax = 894348.78, ymin = 1366617.79, ymax = 1841188.44, height = 512, width = 768;

    public static Hashtable<Integer, String> combine(Hashtable<String, Hashtable<Integer, Obs>> obs_table) throws ParseException {
        Hashtable<Integer, String> udata = new Hashtable<>();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

//        java.util.Date sTime = formatter.parse(tbuffer.get(0));
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
            String day = entrySet.getKey();
            if (/*day.endsWith("Friday") ||*/ day.endsWith("Satureday") || day.endsWith("Sunday")) {
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
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        String dSPath = "/media/essam/Dell1/traffic/SET2";
        String towerPath = "/media/essam/Dell1/traffic/towers.csv";

        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towerPath);

        ObsTripsBuilder builder = new ObsTripsBuilder();
        Hashtable<String, Hashtable<Integer, Obs>> total_obs_table = new Hashtable<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dSPath));
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
        Hashtable<Integer, String> udata = traj_points(combine(total_obs_table), towers);

        WriteCSVFile(udata, "/media/essam/Dell1/traffic/20_week.csv");
//        builder.writeObsDUT(total_obs_table, "/home/essam/trajectory/t.xml");

    }

    public static double scaleX(double x) {
        return width * (x - xmin) / (xmax - xmin);
    }

    public static double scaleY(double y) {
        return height * (ymax - y) / (ymax - ymin);
    }

    public static Hashtable<Integer, String> traj_points(Hashtable<Integer, String> udata, Hashtable<Integer, Vertex> towers) {
        Hashtable<Integer, String> traj_udata = new Hashtable<>();
        for (Map.Entry<Integer, String> entrySet : udata.entrySet()) {
            Integer key = entrySet.getKey();
            String obs[] = entrySet.getValue().split(",");
            int size =0;
            String tmp = "";
            for (int i = 0; i < obs.length; i++) {
                if (obs[i].isEmpty()) {
                    continue;
                }
                Vertex twv = towers.get(Integer.parseInt(obs[i]));
                if (!tmp.isEmpty()) {
                    tmp += " ";
                }
                tmp += scaleX(twv.getX()) + " " + scaleY(twv.getY());
                size++;
            }
            traj_udata.put(key, size + " " + tmp);
//            System.out.println(key + " " + size + " " + tmp);
        }
        return traj_udata;
    }

    public static void WriteCSVFile(Hashtable<Integer, String> udata, String path) {

        try {
            File file = new File(path);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(Integer.toString(2));
            output.newLine();
            output.write(Integer.toString(udata.size()));
            output.newLine();

            for (Map.Entry<Integer, String> entrySet : udata.entrySet()) {
                Integer key = entrySet.getKey();
                String value = entrySet.getValue();
                output.write(key + " " + value);
                output.newLine();
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

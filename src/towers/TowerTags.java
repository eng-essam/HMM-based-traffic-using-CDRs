/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import towers.test.PlotTowersDAG;

/**
 *
 * @author essam
 */
public class TowerTags {

    public final static double AVERAGE_RADIUS_OF_EARTH = 6371;

    String path;

    public TowerTags(String path) {
        this.path = path;
    }

    public int calculateDistance(double userLat, double userLng,
            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH * c));
    }
    public void generateTags() {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(this.path));

            String line;

            while ((line = reader.readLine()) != null) {
                /**
                 * site_id,arr_id,lon,lat
                 */
                String lineSplit[] = line.split(",");
                System.out.format("<node id=\"%s\" visible=\"true\" "
                        + "version=\"1\" changeset=\"20800199\" "
                        + "timestamp=\"2014-02-26T23:06:07Z\" "
                        + "user=\"essam\" uid=\"1238079\" "
                        + "lat=\"%s\" lon=\"%s\">\n"
                        + "<tag k=\"amenity\" v=\"hospital\"/>\n"
                        + "<tag k=\"name\" v=\"%s\"/>\n"
                        + "</node>\n", lineSplit[0], lineSplit[3], lineSplit[2], lineSplit[0]);

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void getTowers(double ax, double ay, double bx, double by) {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(this.path));

            String line;

            while ((line = reader.readLine()) != null) {
                /**
                 * site_id,arr_id,lon,lat
                 */
                String lineSplit[] = line.split(",");
                double x = Double.parseDouble(lineSplit[3]);
                double y = Double.parseDouble(lineSplit[2]);
//                System.out.format("%s,%s\n",x,y);
                Rectangle2D rect = new Rectangle2D.Double(bx, by, bx-ax, by-ay);
                rect.getX();
                rect.getY();
                if (rect.contains(x, y)) {
                    System.out.format("%s,%s", x, y);
//                    System.out.format("<node id=\"%s\" visible=\"true\" "
//                            + "version=\"1\" changeset=\"20800199\" "
//                            + "timestamp=\"2014-02-26T23:06:07Z\" "
//                            + "user=\"essam\" uid=\"1238079\" "
//                            + "lat=\"%s\" lon=\"%s\">\n"
//                            + "<tag k=\"amenity\" v=\"hospital\"/>\n"
//                            + "<tag k=\"name\" v=\"%s\"/>\n"
//                            + "</node>\n", lineSplit[0], lineSplit[3], lineSplit[2], lineSplit[0]);
                }

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

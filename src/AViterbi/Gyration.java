/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import utils.Vertex;

/**
 *
 * @author essam
 */
public class Gyration {

    /**
     * convert observation string into a list.....
     *
     * @param sobs
     * @return
     */
    public static List<Integer> toList(String sobs) {
        List<Integer> obs = new ArrayList<>();
        if (sobs.contains("/")) {
            sobs.replaceAll("/", ",");
        }
        String s[] = sobs.split(",");
        for (int i = 0; i < s.length; i++) {
            obs.add(Integer.parseInt(s[i]));
        }
        return obs;
    }

    Hashtable<Integer, Vertex> towers;

    public Gyration(Hashtable<Integer, Vertex> towers) {
        this.towers = towers;
    }

    /**
     * Calculate the centroid for a list of points ...
     *
     * @param obs
     * @return
     */
    private Vertex calc_centroid(List<Integer> obs) {

        Vertex centroid = new Vertex(0, 0);

        for (Iterator<Integer> iterator = obs.iterator(); iterator.hasNext();) {
            Integer twr = iterator.next();
            Vertex v = towers.get(twr);
            centroid.setX(centroid.getX() + v.getX());
            centroid.setY(centroid.getY() + v.getY());

        }
        centroid.setX(centroid.getX() / obs.size());
        centroid.setY(centroid.getY() / obs.size());
        return centroid;
    }

    /**
     *
     * @param obs
     * @return
     *
     * Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
     */
    public double calc_rg(List<Integer> obs) {
        Vertex centroid = calc_centroid(obs);
        double sum_diff = 0;
        for (Iterator<Integer> iterator = obs.iterator(); iterator.hasNext();) {
            Integer twr = iterator.next();
            Vertex pi = towers.get(twr);
            sum_diff += diff(centroid, pi);

        }
        return Math.sqrt(sum_diff / obs.size());
    }

    private double diff(Vertex v1, Vertex v2) {
        return Math.pow((v1.getX() - v2.getX()), 2) + Math.pow((v1.getY() - v2.getY()), 2);
    }

    /**
     * convert list of integers to string ...
     *
     * @param l
     * @return
     */
    public String toString(List<Integer> l) {
        if (l.isEmpty()) {
            return "";
        }
        String r = l.get(0).toString();
        for (int i = 1; i < l.size(); i++) {
            r += "," + l.get(i);
        }
        return r;
    }
}

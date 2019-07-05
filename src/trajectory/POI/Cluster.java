/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author essam
 */
public class Cluster<T> {

    private List<T> points;
    private T centriod;
    private int id;
    

    public Cluster() {
        points = new ArrayList<>();
    }

    public void addpoint(T point) {
        points.add(point);
    }

    public boolean contains(T point) {
        return points.contains(point);
    }

    public T get_centroid() {
        return centriod;
    }

    public int getId() {
        return id;
    }

    public List<T> getpoints() {
        return points;
    }

    public void print() {
        System.out.println(centriod.toString());
        System.out.print(points.get(0).toString());
        for (int i = 1; i < points.size(); i++) {
            System.out.print("," + points.get(i).toString());
        }
        System.out.println("");
    }

    public void set_centroid(T point) {
        centriod = point;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        String t = points.get(0).toString();
        for (int i = 1; i < points.size(); i++) {
            t += "," + points.get(i);
        }
        return t;
    }

}

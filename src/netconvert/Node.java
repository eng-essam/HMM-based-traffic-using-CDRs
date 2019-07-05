/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netconvert;

/**
 *
 * @author essam
 */
public class Node {

    protected long id;
    protected float lat;
    protected float lon;
    protected double x;
    protected double y;

    public Node(long id, float lat, float lon, double x, double y) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.x = x;
        this.y = y;
    }

    
    public long getId() {
        return id;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author essam
 */
public class Vertex {

	public String ID;
	public double x;
	public double y;

	public Vertex() {

	}

	public Vertex(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public String getID() {
		return ID;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

}

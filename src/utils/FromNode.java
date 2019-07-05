/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.util.ArrayList;

/**
 *
 * @author essam
 */
public class FromNode {
	public String ID;
	public double x;
	public double y;
	public boolean isExit = false;
	public int zone = -1;
	public String type = "";
	public ArrayList<ToNode> toedges = new ArrayList<>();

	
	public String getID() {
		return ID;
	}

	public ArrayList<ToNode> getToedges() {
		return toedges;
	}

	public String getType() {
		return type;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getZone() {
		return zone;
	}

	public boolean isIsExit() {
		return isExit;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public void setIsExit(boolean isExit) {
		this.isExit = isExit;
	}

	public void setToedges(ArrayList<ToNode> toedges) {
		this.toedges = toedges;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

}

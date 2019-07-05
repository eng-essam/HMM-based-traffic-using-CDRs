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
public class Region {

	public int id;
	public ArrayList<Vertex> vertices = new ArrayList<>();

	public int getId() {
		return id;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setVertices(ArrayList<Vertex> vertices) {
		this.vertices = vertices;
	}

}

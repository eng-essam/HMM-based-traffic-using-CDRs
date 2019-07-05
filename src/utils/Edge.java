/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.awt.geom.Point2D;

/**
 *
 * @author essam
 */
public class Edge {

	public String id;
	public String from_node;
	public String to_node;
	public String shape;
	public String type;

	public Point2D start;
	public Point2D end;

	public Edge() {
	}

	public Edge(String id, String from, String to, String shape) {
		this.id = id;
		this.from_node = from;
		this.to_node = to;
		this.shape = shape;
	}

	public Edge(String id, String from, String to, String shape, String highway_type) {
		this.id = id;
		this.from_node = from;
		this.to_node = to;
		this.shape = shape;
		this.type = highway_type;
	}

	public double[] getCenterPnt() {
		String[] pnts = this.shape.split(DataHandler.SPACE_SEP);
		double[] cntr = new double[2];
		double x = 0, y = 0;
		for (int i = 0; i < pnts.length; i++) {
			String[] pnt0 = pnts[i].split(DataHandler.COMMA_SEP);
			x += Double.parseDouble(pnt0[0]);
			y += Double.parseDouble(pnt0[1]);
		}
		cntr[0] = x / pnts.length;
		cntr[1] = y / pnts.length;

		return cntr;

	}

	public void set_start_end_pnts() {

		String[] pnts = this.shape.split(DataHandler.SPACE_SEP);
		String[] s_pnt_str = pnts[0].split(DataHandler.COMMA_SEP);

		start = new Point2D.Double(Double.parseDouble(s_pnt_str[0]), Double.parseDouble(s_pnt_str[1]));
		s_pnt_str = pnts[pnts.length - 1].split(DataHandler.COMMA_SEP);

		end = new Point2D.Double(Double.parseDouble(s_pnt_str[0]), Double.parseDouble(s_pnt_str[1]));

	}

	public String getFrom() {
		return from_node;
	}

	public String getId() {
		return id;
	}

	public String getShape() {
		return shape;
	}

	public String getTo() {
		return to_node;
	}

	public String getType() {
		return type;
	}

	public void setFrom(String from) {
		this.from_node = from;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public void setTo(String to) {
		this.to_node = to;
	}

	public void setType(String type) {
		this.type = type;
	}

}

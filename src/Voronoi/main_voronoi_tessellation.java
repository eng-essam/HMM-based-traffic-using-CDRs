/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import hoten.geom.Point;
import hoten.geom.Rectangle;
import hoten.voronoi.nodename.as3delaunay.Voronoi;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_voronoi_tessellation {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here
		double minlat = Double.parseDouble(args[0]);
		double minlon = Double.parseDouble(args[1]);
		double maxlat = Double.parseDouble(args[2]);
		double maxlon = Double.parseDouble(args[3]);
		String towers_file = args[4];

		String parent_path = towers_file.substring(0, towers_file.lastIndexOf("/"));
		System.out.println(parent_path);

		String voronoi_path = parent_path + "/voronoi.csv";
		String neighbors_path = parent_path + "/neighbors.csv";

		// Alex coordinates ...
		// double minlat = 30.8362, minlon = 29.4969, maxlat = 31.3302, maxlon =
		// 30.0916;
		// Dakar coordinates ..
		// double minlat = 14.5597, minlon = -17.5616, maxlat = 14.9036, maxlon
		// = -17.1098;
		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		double xmin, ymin, xmax, ymax;
		xmin = 0;
		xmax = xymax[0] - xymin[0];
		ymin = 0;
		ymax = xymax[1] - xymin[1];

		Rectangle r = new Rectangle(xmin, ymin, xmax, ymax);

		ArrayList<Point> points = new ArrayList<>();
		ArrayList<Color> pntColors = new ArrayList();

		Hashtable<Integer, Vertex> twrs = DataHandler.readTowers(towers_file);

		Hashtable<Point, Integer> trans_twrs = new Hashtable<>();
		for (Map.Entry<Integer, Vertex> entrySet : twrs.entrySet()) {
			Integer key = entrySet.getKey();
			Vertex v = entrySet.getValue();
			Point pnt = new Point(v.getX(), v.getY());
			points.add(pnt);
			trans_twrs.put(pnt, key);
			pntColors.add(Color.red);

		}

		Voronoi vor = new Voronoi(points, pntColors, r);
		String vor_str = "";
		for (Iterator<Point> iterator = points.iterator(); iterator.hasNext();) {
			// System.out.println("\t\t" + (numSites--));
			Point pnt = iterator.next();
			int ps = trans_twrs.get(pnt);
			vor_str += ps + ",\n";
			System.out.println(ps + ",");
			ArrayList<Point> region = vor.region(pnt);
			for (Iterator<Point> pntsIt = region.iterator(); pntsIt.hasNext();) {
				Point next = pntsIt.next();
				vor_str += "," + next.x + "," + next.y + "\n";
				System.out.println("," + next.x + "," + next.y);

			}
		}

		write_csv(vor_str, voronoi_path);

		System.out.printf("=======================================\n%s\n=======================================\n",
				"Nieghbours");

		String neighbors_str = "";
		for (Point pnt : points) {
			neighbors_str += trans_twrs.get(pnt) + ",\n";
			System.out.println(trans_twrs.get(pnt) + ",");
			ArrayList<Point> neighbours = vor.neighborSitesForSite(pnt);
			for (Point next : neighbours) {
				neighbors_str += "," + trans_twrs.get(next);
				System.out.print("," + trans_twrs.get(next));
			}
			neighbors_str += "\n";
			System.out.println("");
		}
		write_csv(neighbors_str, neighbors_path);

		System.out.println("Finish");
	}

	public static void write_csv(String data, String path) {
		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(data);
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

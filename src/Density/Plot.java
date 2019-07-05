/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

import utils.Edge;
import utils.GPSTrkPnt;
import utils.Region;
import utils.StdDraw;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class Plot {

	public static double round(double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	ArrayList<Edge> map;
	Hashtable<String, Integer> edgesMap;
	String iPath;

	double xmin, ymin, xmax, ymax;

	public Plot(ArrayList<Edge> map, String iPath) {
		this.map = map;
		this.iPath = iPath;
		indexEdges();
	}

	public Plot(double width, double height, String iPath) {
		StdDraw.DEFAULT_SIZE = (int) (width > height ? width : height);
		StdDraw.width = (int) width;
		StdDraw.height = (int) height;
		this.iPath = iPath;
	};

	public Plot(int isize, ArrayList<Edge> map, String iPath) {
		StdDraw.DEFAULT_SIZE = isize;
		this.map = map;
		this.iPath = iPath;
		indexEdges();
	}

	public Plot(String iPath) {
		this.iPath = iPath;
	}

	public void display_save() {
		StdDraw.show(0);
		StdDraw.save(this.iPath);
		StdDraw.clear();

	}

	public Color getColor(double power) {
		double H = power * 0.8; // Hue (note 0.4 = Green)
		double S = 0.9; // Saturation
		double B = 0.9; // Brightness
		// int val = (int) (H * 255);
		// Color col = new Color(val, val, val);
		// return col;
		return Color.getHSBColor((float) H, (float) S, (float) B);

	}

	private void indexEdges() {
		this.edgesMap = new Hashtable<>();

		for (Iterator<Edge> it = map.iterator(); it.hasNext();) {
			Edge edge = it.next();
			edgesMap.put(edge.getId(), map.indexOf(edge));
		}
	}

	/**
	 * Mark a list of edges
	 * @param edges
	 * @param color
	 * @param pen_radius
	 * @return
	 */
	public boolean mark_map_edges(List<String> edges, Color color,double pen_radius) {
		StdDraw.setPenColor(color);
		StdDraw.setPenRadius(pen_radius);
		for (Iterator<Edge> iterator = map.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			if (edges.contains(edge.id)) {
				String[] splitString = edge.getShape().split("\\s");
				for (int i = 0; i < splitString.length - 1; i++) {
					String[] point1 = splitString[i].split(",");
					String[] point2 = splitString[i + 1].split(",");
					// StdDraw.setPenColor(Color.GREEN);
					StdDraw.line(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]),
							Double.parseDouble(point2[0]), Double.parseDouble(point2[1]));
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param vl
	 * @param c
	 */
	public void plot_gpx_traces(List<GPSTrkPnt> pnts, Color c) {
		StdDraw.setPenColor(c);
		StdDraw.setPenRadius(0.001);
		for (int i = 0; i < pnts.size() - 1; i++) {
			GPSTrkPnt v1 = pnts.get(i);
			GPSTrkPnt v2 = pnts.get(i + 1);
			StdDraw.line(v1.x, v1.y, v2.x, v2.y);
		}

	}
	
	/**
	 * Mark individual edges with specific color for map density
	 *
	 * @param id
	 * @param color
	 */
	public void mark_edge(String id, Color color) {
		StdDraw.setPenRadius(0.002);
		for (Iterator<Edge> iterator = map.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			if (edge.getId().equals(id)) {
				StdDraw.setPenColor(color);
				String[] splitString = edge.getShape().split("\\s");
				for (int i = 0; i < splitString.length - 1; i++) {
					String[] point1 = splitString[i].split(",");
					String[] point2 = splitString[i + 1].split(",");
					StdDraw.line(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]),
							Double.parseDouble(point2[0]), Double.parseDouble(point2[1]));
				}
			}

		}
	}

	/**
	 * Mark individual edges with specific color for map density
	 *
	 * @param id
	 * @param color
	 */
	public void mark_edge(String id, double val, boolean txt, Color color) {
		StdDraw.setPenRadius(0.002);
		double x, y;

		for (Iterator<Edge> iterator = map.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			if (edge.getId().equals(id)) {
				StdDraw.setPenColor(color);
				String[] splitString = edge.getShape().split("\\s");
				for (int i = 0; i < splitString.length - 1; i++) {
					String[] point1 = splitString[i].split(",");
					String[] point2 = splitString[i + 1].split(",");
					StdDraw.line(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]),
							Double.parseDouble(point2[0]), Double.parseDouble(point2[1]));
				}
				if (txt) {
					String[] point1 = splitString[0].split(",");
					String[] point2 = splitString[splitString.length - 1].split(",");
					double x1, x2, y1, y2;
					x1 = Double.parseDouble(point1[0]);
					y1 = Double.parseDouble(point1[1]);
					x2 = Double.parseDouble(point2[0]);
					y2 = Double.parseDouble(point2[1]);
					x = (x1 + x2) / 2;
					y = (y1 + y2) / 2;
					StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 15));
					StdDraw.setPenRadius(0.01);
					StdDraw.setPenColor(Color.BLACK);
					StdDraw.text(x, y, Double.toString(val));
				}
			}

		}
	}

	public void plot_edge(String[] points) {
		StdDraw.setPenRadius(0.002);
		StdDraw.setPenColor(Color.GREEN);
		String[] strt = points[0].split(",");
		String[] end = points[points.length - 1].split(",");
		StdDraw.line(Double.parseDouble(strt[0]), Double.parseDouble(strt[1]), Double.parseDouble(end[0]),
				Double.parseDouble(end[1]));
	}

	public void plot_exts(ArrayList<String> exts) {
		for (Iterator<String> it = exts.iterator(); it.hasNext();) {
			String edge = it.next();
			String[] points = map.get(edgesMap.get(edge)).getShape().split(" ");
			plot_edge(points);
		}
		StdDraw.setPenRadius();
	}

	/**
	 * 
	 * @param vl
	 * @param c
	 */
	public void plot_lines(Vertex[] vl, Color c) {
		StdDraw.setPenColor(c);
		StdDraw.setPenRadius(0.0005);
		for (int i = 0; i < vl.length - 1; i++) {
			Vertex v1 = vl[i];
			Vertex v2 = vl[i + 1];
			StdDraw.line(v1.getX(), v1.getY(), v2.getX(), v2.getY());
		}

	}

	public void plot_towers(Hashtable<Integer, Vertex> towers) {
		StdDraw.setFont(new Font("DS-Digital", Font.PLAIN, 5));
		for (Map.Entry<Integer, Vertex> entry : towers.entrySet()) {
			int key = entry.getKey().intValue();
			Vertex v = entry.getValue();
			StdDraw.setPenColor(Color.YELLOW);
			StdDraw.filledCircle(v.x, v.y, 0.5);
			StdDraw.setPenColor(Color.BLACK);
			StdDraw.textLeft(v.x, v.y, Integer.toString(key));
		}
	}

	/**
	 * Wed Oct 28 12:19:38 JST 2015
	 *
	 * plot voronoi shapes and fill the listed zone with transparent colors ...
	 *
	 * @param voronoiRegions
	 * @param zones
	 */
	public void plot_vor_fill(ArrayList<Region> voronoiRegions, List<Integer> zones) {

		for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}

			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.getVertices();

			double[] xPnts = new double[vertices.size()];
			double[] yPnts = new double[vertices.size()];

			int i = 0;
			for (Vertex vert : vertices) {
				xPnts[i] = vert.getX();
				yPnts[i] = vert.getY();
				i++;
			}
			double cXPnt = DoubleStream.of(xPnts).average().getAsDouble();
			double cYPnt = DoubleStream.of(yPnts).average().getAsDouble();
			--i;
			// StdDraw.setPenRadius(0.3);
			StdDraw.setPenRadius(0.0005);
			StdDraw.setPenColor(Color.BLUE);
			StdDraw.text(cXPnt, cYPnt, String.valueOf(region.getId()));
			StdDraw.polygon(xPnts, yPnts);

			if (zones.contains(region.getId())) {
				Color c = new Color(1.0f, 0.698f, 0.4f, 0.6f);
				StdDraw.setPenColor(c);
				StdDraw.filledPolygon(xPnts, yPnts);
			}

		}
	}

	public void plot_voronoi(ArrayList<Region> voronoiRegions, Hashtable<Integer, ArrayList<Integer>> voronoiNeibors) {

		for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}

			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.getVertices();

			double[] xPnts = new double[vertices.size()];
			double[] yPnts = new double[vertices.size()];

			int i = 0;
			for (Vertex vert : vertices) {
				// StdDraw.filledCircle(vert.x, vert.y, 500);
				// if()

				xPnts[i] = vert.getX();
				yPnts[i] = vert.getY();
				i++;
			}
			double cXPnt = DoubleStream.of(xPnts).average().getAsDouble();
			double cYPnt = DoubleStream.of(yPnts).average().getAsDouble();
			--i;
			// StdDraw.setPenRadius(0.3);
			StdDraw.text(cXPnt, cYPnt, String.valueOf(region.getId()));
			StdDraw.setPenRadius(0.0005);
			if (voronoiNeibors.containsKey(region.getId())) {
				StdDraw.setPenColor(Color.BLUE);
				StdDraw.polygon(xPnts, yPnts);

			} else {
				StdDraw.setPenColor(Color.RED);
				StdDraw.polygon(xPnts, yPnts);
			}
		}
	}

	public void plotClock(int h) {
		// a= PI/2 - h % 12 * PI / 6 - m % 60 * PI / 360
		double angle = Math.PI / 2 - h % 12 * Math.PI / 6;
	}

	public void plotColoredPath(String viterbiPath) {
		if (!viterbiPath.contains(",")) {
			return;
		}
		String[] edges = viterbiPath.split(",");
		ArrayList<Vertex> path = new ArrayList<>();
		StdDraw.setPenColor(Color.GREEN);
		StdDraw.setPenRadius(0.003);
		for (String edge : edges) {
			String[] points = map.get(edgesMap.get(edge)).getShape().split(" ");

			for (int i = 0; i <= points.length - 1; i++) {
				String[] str = points[i].split(",");
				// String[] to_str = points[i+1].split(",");
				Vertex vertex = new Vertex();
				vertex.setX(Double.parseDouble(str[0]));
				vertex.setY(Double.parseDouble(str[1]));

				// StdDraw.line(Double.parseDouble(str[0]),
				// Double.parseDouble(str[1]), Double.parseDouble(to_str[0]),
				// Double.parseDouble(to_str[1]));
				path.add(vertex);
			}

		}
		StdDraw.setPenRadius();
		StdDraw.setPenColor(Color.getHSBColor((float) Math.random(), (float) Math.random(), (float) Math.random()));
		for (int i = 0; i < path.size() - 1; i++) {
			// StdDraw.setPenColor(getColor(Math.random()));
			Vertex from = path.get(i);
			Vertex to = path.get(i + 1);

			StdDraw.line(from.getX(), from.getY(), to.getX(), to.getY());

			// StdDraw.text(from.getX(), from.getY(), "mmmmm");
		}
	}

	public void plotFixGamut(int njump, double max, double xmin, double ymin, double xmax, double ymax) {
		// 0.0 java.awt.Color[230,23,23]
		// 0.1 java.awt.Color[230,122,23]
		// 0.2 java.awt.Color[230,221,23]
		// 0.3 java.awt.Color[139,230,23]
		// 0.4 java.awt.Color[39,230,23]
		// 0.5 java.awt.Color[23,230,106]
		Hashtable<Double, Color> colors = new Hashtable<>();
		colors.put(0.0, new Color(23, 230, 106));
		colors.put(0.1, new Color(39, 230, 23));
		colors.put(0.2, new Color(39, 230, 23));
		colors.put(0.3, new Color(139, 230, 23));
		colors.put(0.4, new Color(139, 230, 23));
		colors.put(0.5, new Color(230, 221, 23));
		colors.put(0.6, new Color(230, 221, 23));
		colors.put(0.7, new Color(230, 122, 23));
		colors.put(0.8, new Color(230, 122, 23));
		colors.put(0.9, new Color(230, 23, 23));
		colors.put(1.0, new Color(230, 23, 23));
		plotHGamut(colors, njump, max, xmin, ymin, xmax, ymax);

	}

	/**
	 * Plot horizonetal the colors map used in plotting traffic intensity
	 *
	 * @param colors
	 * @param max
	 */
	public void plotHGamut(Hashtable<Double, Color> colors, int njump, double max, double xmin, double ymin,
			double xmax, double ymax) {
		/**
		 * Start by sorting keys
		 */

		Vector v = new Vector(colors.keySet());
		Collections.sort(v);
		Iterator it = v.iterator();
		double step = (xmax - xmin) / colors.size();
		double hwidth = step / 2;
		double hhight = (ymax - ymin) / 2;
		double y = ymin + hhight;

		int jump = njump;
		StdDraw.setPenRadius(0.003);
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 50));

		double prev = 0;
		while (it.hasNext()) {

			double element = (double) it.next();
			// System.out.println( + " " + colors.get(element).toString());
			double x = xmin + hwidth;
			StdDraw.setPenColor(colors.get(element));
			// System.out.println(element + "\t" +
			// colors.get(element).toString());
			StdDraw.filledRectangle(x, y, hwidth, hhight);

			// if (!colors.get(element).equals(colors.get(prev))|| prev==0) {
			// if (jump == njump || jump == 0) {
			// StdDraw.setPenRadius(0.002);
			StdDraw.setPenColor(Color.BLACK);
			StdDraw.text(xmin + 2 * hwidth - 200, ymin - 400, String.valueOf((int) (element * max)), 45);
			// jump = njump;
			// prev = element;
			// }
			// jump--;
			xmin += step;
		}

		// StdDraw.setFont(Font.getFont("DS-Digital"));
		// StdDraw.setPenRadius(0.01);
		// StdDraw.text(max+1000, min+2000, to_clock);
	}

	/**
	 * Plot horizonetal the colors map used in plotting traffic intensity
	 *
	 * @param colors
	 * @param max
	 */
	public void plotHGamut(Hashtable<Double, Color> colors, int njump, double max, double xmin, double ymin,
			double xmax, double ymax, String to_clock) {
		/**
		 * Start by sorting keys
		 */

		Vector v = new Vector(colors.keySet());
		Collections.sort(v);
		Iterator it = v.iterator();
		double step = (xmax - xmin) / colors.size();
		double hwidth = step / 2;
		double hhight = (ymax - ymin) / 2;
		double y = ymin + hhight;

		int jump = njump;
		StdDraw.setPenRadius(0.003);
		while (it.hasNext()) {
			double element = (double) it.next();
			// System.out.println( + " " + colors.get(element).toString());
			double x = xmin + hwidth;
			StdDraw.setPenColor(colors.get(element));
			StdDraw.filledRectangle(x, y, hwidth, hhight);
			if (jump == njump || jump == 0) {
				// StdDraw.setPenRadius(0.002);
				StdDraw.setPenColor(Color.BLACK);
				StdDraw.text(xmin - 200, ymin - 200, String.valueOf((int) (element * max)), 45);
				jump = njump;
			}
			jump--;
			xmin += step;
		}

		// StdDraw.setFont(Font.getFont("DS-Digital"));
		// StdDraw.setPenRadius(0.01);
		// StdDraw.text(max + 1000, ymin + 2000, to_clock);
	}

	

	/**
	 * perl -ne 'while(/\s(?:shape)="([^"]+)"/g) { print "$1 " }' \
	 * $dir/Dakar.bounded.net.xml > $dir/map
	 *
	 * @param mapPath
	 * @return
	 */
	public boolean plotMapData(String mapPath) {
		StdDraw.setPenRadius(0.0005);
		String line;
		BufferedReader br;
		StdDraw.setPenColor(Color.GRAY);
		try {
			// static file contain the map points
			br = new BufferedReader(new FileReader(mapPath));

			while ((line = br.readLine()) != null) {

				if (line.isEmpty()) {
					continue;
				}
				String[] splitString = line.split("\\s");
				for (int i = 0; i < splitString.length - 1; i++) {
					String[] point1 = splitString[i].split(",");
					String[] point2 = splitString[i + 1].split(",");
					StdDraw.line(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]),
							Double.parseDouble(point2[0]), Double.parseDouble(point2[1]));
				}

			}
			br.close();

		} catch (FileNotFoundException ex) {
			Logger.getLogger(Plot.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Plot.class.getName()).log(Level.SEVERE, null, ex);
		}
		StdDraw.setPenRadius();
		// System.out.println(mapData.size());
		return true;
	}

	/**
	 * Plot map using edges shapes
	 *
	 * @param edgesPath
	 * @return true
	 */
	public boolean plotMapEdges() {
		StdDraw.setPenColor(Color.GRAY);
		StdDraw.setPenRadius(0.0003);
		for (Iterator<Edge> iterator = map.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			String[] splitString = edge.getShape().split("\\s");
			for (int i = 0; i < splitString.length - 1; i++) {
				String[] point1 = splitString[i].split(",");
				String[] point2 = splitString[i + 1].split(",");
				// StdDraw.setPenColor(Color.GREEN);
				StdDraw.line(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]),
						Double.parseDouble(point2[0]), Double.parseDouble(point2[1]));
			}

		}
		return true;
	}

	/**
	 * Plot map using edges shapes
	 *
	 * @param edgesPath
	 * @return true
	 */
	public boolean plotMapEdges(ArrayList<Edge> map) {
		StdDraw.setPenColor(Color.GREEN);
		for (Iterator<Edge> iterator = map.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			String[] splitString = edge.getShape().split("\\s");
			for (int i = 0; i < splitString.length - 1; i++) {
				String[] point1 = splitString[i].split(",");
				String[] point2 = splitString[i + 1].split(",");
				// StdDraw.setPenColor(Color.GREEN);
				StdDraw.line(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]),
						Double.parseDouble(point2[0]), Double.parseDouble(point2[1]));
			}

		}
		return true;
	}

	public void plotPath(String viterbiPath) {
		if (!viterbiPath.contains(",")) {
			return;
		}
		StdDraw.setPenColor(Color.BLUE);
		// StdDraw.setPenRadius(0.003);
		String[] edges = viterbiPath.split(",");
		ArrayList<Vertex> path = new ArrayList<>();
		for (String edge : edges) {
			String[] points = map.get(edgesMap.get(edge)).getShape().split(" ");

			for (int i = 0; i < points.length - 1; i++) {
				String[] str = points[i].split(",");
				Vertex vertex = new Vertex();
				vertex.setX(Double.parseDouble(str[0]));
				vertex.setY(Double.parseDouble(str[1]));
				path.add(vertex);
			}
		}
		for (int i = 0; i < path.size() - 1; i++) {
			Vertex from = path.get(i);
			Vertex to = path.get(i + 1);
			StdDraw.line(from.getX(), from.getY(), to.getX(), to.getY());
			// StdDraw.text(from.getX(), from.getY(), nodes[i]);

		}
	}

	public void plotPath(String viterbiPath, Color color) {
		if (!viterbiPath.contains(",")) {
			return;
		}

		String[] edges = viterbiPath.split(",");
		ArrayList<Vertex> path = new ArrayList<>();
		StdDraw.setPenColor(Color.GREEN);
		for (String edge : edges) {
			if (!edgesMap.containsKey(edge)) {
				continue;
			}
			String[] points = map.get(edgesMap.get(edge)).getShape().split(" ");
			// mark exit edge selected by viterbi
			// plot_edge(points);
			for (int i = 0; i < points.length - 1; i++) {
				String[] str = points[i].split(",");
				Vertex vertex = new Vertex();
				vertex.setX(Double.parseDouble(str[0]));
				vertex.setY(Double.parseDouble(str[1]));
				// StdDraw.point(vertex.getX(), vertex.getY());
				path.add(vertex);
			}
		}
		StdDraw.setPenRadius(0.0003);
		// path = new ArrayList<>(sort_path(path));
		StdDraw.setPenColor(color);
		for (int i = 0; i < path.size() - 1; i++) {
			// StdDraw.setPenColor(getColor(Math.random()));
			Vertex from = path.get(i);
			Vertex to = path.get(i + 1);
			StdDraw.line(from.getX(), from.getY(), to.getX(), to.getY());

			// StdDraw.text(from.getX(), from.getY(), "mmmmm");
		}
	}

	public void plotPath(String edges[], Color color) {

		ArrayList<Vertex> path = new ArrayList<>();
		StdDraw.setPenColor(Color.GREEN);
		for (String edge : edges) {
			String[] points = map.get(edgesMap.get(edge)).getShape().split(" ");
			plot_edge(points);
			for (int i = 0; i < points.length - 1; i++) {
				String[] str = points[i].split(",");
				Vertex vertex = new Vertex();
				vertex.setX(Double.parseDouble(str[0]));
				vertex.setY(Double.parseDouble(str[1]));
				// StdDraw.point(vertex.getX(), vertex.getY());
				path.add(vertex);
			}
		}
		StdDraw.setPenRadius();
		StdDraw.setPenColor(color);
		for (int i = 0; i < path.size() - 1; i++) {
			// StdDraw.setPenColor(getColor(Math.random()));
			Vertex from = path.get(i);
			Vertex to = path.get(i + 1);
			StdDraw.line(from.getX(), from.getY(), to.getX(), to.getY());

			// StdDraw.text(from.getX(), from.getY(), "mmmmm");
		}
	}

	public void plotPath(String edges[], Color color,double pen_radius) {
		
		ArrayList<Vertex> path = new ArrayList<>();
		StdDraw.setPenColor(Color.GREEN);
		for (String edge : edges) {
			String[] points = map.get(edgesMap.get(edge)).getShape().split(" ");
			// plot_edge(points);
			for (int i = 0; i < points.length - 1; i++) {
				String[] str = points[i].split(",");
				Vertex vertex = new Vertex();
				vertex.setX(Double.parseDouble(str[0]));
				vertex.setY(Double.parseDouble(str[1]));
				// StdDraw.point(vertex.getX(), vertex.getY());
				path.add(vertex);
			}
		}
		StdDraw.setPenColor(color);
		StdDraw.setPenRadius(pen_radius);
		for (int i = 0; i < path.size() - 1; i++) {
			// StdDraw.setPenColor(getColor(Math.random()));
			Vertex from = path.get(i);
			Vertex to = path.get(i + 1);
			StdDraw.line(from.getX(), from.getY(), to.getX(), to.getY());

			// StdDraw.text(from.getX(), from.getY(), "mmmmm");
		}
	}

	
	/**
	 * Wed Oct 28 10:03:08 JST 2015 plot Voronoi regions
	 *
	 * @param voronoiRegions
	 */
	public void plotVor(ArrayList<Region> voronoiRegions) {

		StdDraw.setPenRadius(0.0005);
		StdDraw.setPenColor(Color.BLUE);

		for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}

			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.getVertices();

			double[] xPnts = new double[vertices.size()];
			double[] yPnts = new double[vertices.size()];

			int i = 0;
			for (Vertex vert : vertices) {
				xPnts[i] = vert.getX();
				yPnts[i] = vert.getY();
				i++;
			}
			double cXPnt = DoubleStream.of(xPnts).average().getAsDouble();
			double cYPnt = DoubleStream.of(yPnts).average().getAsDouble();
			--i;
			// StdDraw.setPenRadius(0.3);
			StdDraw.text(cXPnt, cYPnt, String.valueOf(region.getId()));
			StdDraw.polygon(xPnts, yPnts);
		}
	}

	public void scale(double xmin, double ymin, double xmax, double ymax) {
		// System.out.println((int) Math.abs(xmax - xmin)+"\t"+(int)
		// Math.abs(ymax - ymin));
		// StdDraw.setCanvasSize((int) (Math.abs(xmax - xmin)/10), (int)
		// (Math.abs(ymax - ymin)/10));
		StdDraw.setXscale(xmin, xmax);
		StdDraw.setYscale(ymin, ymax);
		StdDraw.show(0);
	}

	/**
	 * Sort path vertices
	 *
	 * @param path
	 * @return
	 */
	private ArrayList<Vertex> sort_path(ArrayList<Vertex> path) {
		ArrayList<Vertex> sorted = new ArrayList<>();
		Vertex a = path.get(0);
		for (int i = 1; i < path.size() - 1; i++) {

			Vertex b = path.get(i);

			if (a.getY() > b.getY()) // a is before b
			{
				sorted.add(a);
				a = new Vertex();
				a = b;
			} else if (a.getX() < b.getX()) // a is before b
			{
				sorted.add(a);
				a = new Vertex();
				a = b;
			} else // b is before a
			{
				sorted.add(b);
			}
			{
			}
		}
		return sorted;
	}

}

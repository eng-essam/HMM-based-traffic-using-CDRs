package sm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import Density.Plot;
import hoten.geom.Point;
import hoten.geom.Rectangle;
import hoten.voronoi.nodename.as3delaunay.Voronoi;
import utils.DataHandler;
import utils.StdDraw;

class node {
	int id;
	double w;
	List<Integer> v = new ArrayList<>();
}

public class main_sm {

	public static void main(String[] args) {
		int num_points = Integer.parseInt(args[0]);
		double xmax = Double.parseDouble(args[1]);
		double ymax = Double.parseDouble(args[2]);
		String point_image_path = args[3];

		List<Point> ps = generate_points(num_points, xmax, ymax);

		Hashtable<Integer, ArrayList<Point>> regions = new Hashtable<>();
		Hashtable<Integer, ArrayList<Integer>> neighbors = new Hashtable<>();
		voronoi_tessellation(ps, xmax, ymax, regions, neighbors);

		double[][] w = weight_matrix(ps);
		System.out.println("Start search ..");
		node n = search_1(ps, neighbors, w);
		plot_points(ps, n, xmax, ymax, point_image_path);

	}

	public static node search(List<Point> ps, Hashtable<Integer, ArrayList<Integer>> neighbors, double[][] w) {
		List<node> nds = new ArrayList<>();
		// initialize search with the first element in the points list ...
		int strt_pnt = 0;
		int count = ps.size() - 2;
		int stps = 0;
		for (int i : neighbors.get(strt_pnt)) {
			node n = new node();
			n.id = i;
			n.v.add(strt_pnt);
			n.w = w[strt_pnt][i];
			nds.add(n);
			stps++;
		}
		// forward search
		count--;
		while (count > 0) {
			List<node> tmp_nds = new ArrayList<>();

			for (node n : nds) {
				ArrayList<Integer> n_nbrs = neighbors.get(n.id);
				for (int ii : n_nbrs) {
					if (!n.v.contains(ii)) {
						// n.v.add(n.id);
						// n.w += w[n.id][ii];
						// n.id = ii;
						// tmp_nds.add(n);
						node nn = new node();
						nn.v.addAll(n.v);
						nn.v.add(n.id);
						nn.w = n.w + w[n.id][ii];
						nn.id = ii;
						tmp_nds.add(nn);
					}
					stps++;
				}
			}
			nds = new ArrayList<>(tmp_nds);
			count--;
		}

		// backward to return only a list of visited nodes with the minimum
		// weight

		// node fn = nds.get(0);
		int index = -1;
		double fw = Double.POSITIVE_INFINITY;
		for (int i = 0; i < nds.size(); i++) {
			// for (node n : nds) {
			node n = nds.get(i);
			// plot_points(ps, n, 1000, 1000, "/home/essam/tmp" + n.id +
			// ".png");
			System.out.println(n.id + "\t" + n.w);
			if (n.w < fw) {
				index = i;
				// fn = new node();
				// fn.v.addAll(n.v);
				fw = n.w;
			}
		}
		System.out.println("steps:\t" + stps);
		return nds.get(index);

	}

	public static node search_1(List<Point> ps, Hashtable<Integer, ArrayList<Integer>> neighbors, double[][] w) {
		List<node> nds = new ArrayList<>();
		// initialize search with the first element in the points list ...
		int strt_pnt = 0;
		int count = ps.size() - 1;
		int stps = 0;
		// for (int i : neighbors.get(strt_pnt)) {
		// node n = new node();
		// n.id = i;
		// n.v.add(strt_pnt);
		// n.w = w[strt_pnt][i];
		// nds.add(n);
		// stps++;
		// }
		for (int i = 0; i < ps.size(); i++) {
//			Point p = ps.get(i);
			node in = new node();
			in.id = i;
			in.w = 0;
			nds.add(in);
		}
		// forward search
		// count--;
		while (count > 0) {
			List<node> tmp_nds = new ArrayList<>();

			for (node n : nds) {
				ArrayList<Integer> n_nbrs = neighbors.get(n.id);
				for (int ii : n_nbrs) {
					if (!n.v.contains(ii)) {
						// n.v.add(n.id);
						// n.w += w[n.id][ii];
						// n.id = ii;
						// tmp_nds.add(n);
						node nn = new node();
						nn.v.addAll(n.v);
						nn.v.add(n.id);
						nn.w = n.w + w[n.id][ii];
						nn.id = ii;
						tmp_nds.add(nn);
					}
					stps++;
				}
			}
			nds = new ArrayList<>(tmp_nds);
			count--;
		}

		// backward to return only a list of visited nodes with the minimum
		// weight

		// node fn = nds.get(0);
		int index = -1;
		double fw = Double.POSITIVE_INFINITY;
		for (int i = 0; i < nds.size(); i++) {
			// for (node n : nds) {
			node n = nds.get(i);
			// plot_points(ps, n, 1000, 1000, "/home/essam/tmp" + n.id +
			// ".png");
			// System.out.println(n.id + "\t" + n.w);
			if (n.w < fw) {
				index = i;
				// fn = new node();
				// fn.v.addAll(n.v);
				fw = n.w;
			}
		}
		System.out.println("steps:\t" + stps);
		return nds.get(index);

	}

	public static List<Point> generate_points(int num, double xmax, double ymax) {
		List<Point> ps = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			ps.add(new Point((Math.random() * xmax), Math.random() * ymax));
		}
		return ps;
	}

	public static void voronoi_tessellation(List<Point> ps, double xmax, double ymax,
			Hashtable<Integer, ArrayList<Point>> regions, Hashtable<Integer, ArrayList<Integer>> neighbors) {
		Rectangle r = new Rectangle(0, 0, xmax, ymax);

		// Point dummy_pnt = new Point(0, 0);

		// dummy central point ..
		// for (Point pnt : ps) {
		// dummy_pnt.x += pnt.x;
		// dummy_pnt.y += pnt.y;
		// }
		// dummy_pnt.x /= ps.size();
		// dummy_pnt.y /= ps.size();
		ArrayList<Point> t_ps = new ArrayList<>(ps);
		// t_ps.add(dummy_pnt);

		Color cs[] = new Color[t_ps.size()];
		Arrays.fill(cs, Color.red);

		Voronoi vor = new Voronoi(t_ps, new ArrayList<>(Arrays.asList(cs)), r);

		// for (Iterator<Point> iterator = ps.iterator(); iterator.hasNext();) {
		// // System.out.println("\t\t" + (numSites--));
		// Point pnt = iterator.next();
		// int id = ps.indexOf(pnt);
		// ArrayList<Point> region = vor.region(pnt);
		// regions.put(id, region);
		// }

		for (Point pnt : ps) {

			int id = ps.indexOf(pnt);
			ArrayList<Point> n_points = vor.neighborSitesForSite(pnt);
			ArrayList<Integer> ns = new ArrayList<>();
			for (Point nPnt : n_points) {
				// if (nPnt.x == dummy_pnt.x && nPnt.y == dummy_pnt.y)
				// continue;
				ns.add(ps.indexOf(nPnt));
			}
			neighbors.put(id, ns);
		}
	}

	public static double[][] weight_matrix(List<Point> ps) {
		int s = ps.size();
		double[][] m = new double[s][s];
		ps.parallelStream().forEach(p -> {
			ps.parallelStream().forEach(tp -> {
				m[ps.indexOf(p)][ps.indexOf(tp)] = DataHandler.euclidean(p.x, p.y, tp.x, tp.y);
			});
		});
		return m;
	}

	public static void plot_points(List<Point> ps, node n, double xmax, double ymax, String path) {
		Plot p = new Plot(path);
		p.scale(0, 0, xmax, ymax);
		for (int i = 0; i < n.v.size() - 1; i++) {
			Point p0 = ps.get(i);
			StdDraw.filledCircle(p0.x, p0.y, 5);
			Point p1 = ps.get(i + 1);
			StdDraw.filledCircle(p1.x, p1.y, 5);

			StdDraw.line(p0.x, p0.y, p1.x, p1.y);
		}
		p.display_save();

	}

}

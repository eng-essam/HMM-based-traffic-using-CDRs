/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.Interpolate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import AViterbi.Gyration;
import Observations.Obs;
import edu.mines.jtk.interp.CubicInterpolator;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class Interpolation {

	Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors;
	Hashtable<Integer, Vertex> towers;
	SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;
	// int stwr = -1;
	// int etwr = -1;

	public Interpolation(Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors, Hashtable<Integer, Vertex> towers) {
		this.voronoi_neighbors = voronoi_neighbors;
		this.towers = towers;

		graph = new SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		construct_graph();
	}

	// public Interpolation(int start_twr, int end_twr, Hashtable<Integer,
	// ArrayList<Integer>> voronoi_neighbors,
	// Hashtable<Integer, Vertex> towers) {
	//
	// this.voronoi_neighbors = voronoi_neighbors;
	// this.towers = towers;
	//// this.stwr = start_twr;
	//// this.etwr = end_twr;
	//
	// graph = new SimpleDirectedWeightedGraph<Integer,
	// DefaultWeightedEdge>(DefaultWeightedEdge.class);
	// construct_graph();
	// }

	private SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> construct_graph() {
		// SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph
		// = new SimpleDirectedWeightedGraph<Integer,
		// DefaultWeightedEdge>(DefaultWeightedEdge.class);
		for (Map.Entry<Integer, Vertex> entry : towers.entrySet()) {
			int from_key = entry.getKey();

			if (!graph.containsVertex(from_key)) {
				graph.addVertex(from_key);
			}

			Vertex from_v = entry.getValue();
			if (voronoi_neighbors.containsKey(from_key)) {
				ArrayList<Integer> vn = voronoi_neighbors.get(from_key);
				for (Iterator<Integer> iterator = vn.iterator(); iterator.hasNext();) {
					int to_key = iterator.next();
					Vertex to_v = towers.get(to_key);
					if (!graph.containsVertex(to_key)) {
						graph.addVertex(to_key);
					}
					// add weight
					DefaultWeightedEdge e1 = graph.addEdge(from_key, to_key);
					graph.setEdgeWeight(e1, DataHandler.euclidean(from_v.getX(), from_v.getY(), to_v.getX(), to_v.getY()));
				}
			}
		}
		return graph;
	}

	/**
	 * get the distance between a line connecting the non-adjacent zones and a
	 * vertex of an adjacent zone ...
	 *
	 * @param t1_v
	 * @param t2_v
	 *
	 * @param t_nv
	 *            adjacent zone vertex ..
	 * @return distance
	 */
	public double distance(Vertex t1_v, Vertex t2_v, Vertex t_nv) {
		double d = 0;
		// slop of the line ..
		double dy = t1_v.getY() - t2_v.getY();
		double dx = t1_v.getX() - t2_v.getX();

		if (dx == 0) {
			return t_nv.getX();
		} else if (dy == 0) {
			return t_nv.getY();
		} else {
			double slop = dy / dx;
			// System.out.println("slop:\t" + slop);
			double eq_const = t2_v.getY() - slop * t2_v.getX();
			// System.out.println("Const:\t" + eq_const);
			d = Math.abs(slop * t_nv.getX() - t_nv.getY() + eq_const) / Math.sqrt(Math.pow(slop, 2) + 1);
			return d;
		}

	}

	/**
	 * Calculate distance between two cartesian points
	 *
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @return
	 */
	public double euclidean(double x, double y, double x1, double y1) {
		return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
	}

	/**
	 * get an approximated length for a curve consists of a set of points lps.
	 * points must be ordered ......
	 *
	 * @param lps
	 * @return
	 */
	public double get_length(Vertex[] lps) {
		double length = 0;
		Vertex lp0 = lps[0];
		for (int i = 1; i < lps.length; i++) {
			Vertex lp1 = lps[i];
			length += euclidean(lp0.getX(), lp0.getY(), lp1.getX(), lp1.getY());
			lp0 = lp1;
		}
		return length;
	}

	/**
	 * Sat Dec 5 21:09:01 EET 2015 hermite cubic piecewise interpolation ..
	 *
	 * @param seq
	 * @return
	 */
	public List<Integer> hermite_interpolator(List<Integer> seq) {
		// System.out.println("Start inter ...");
		List<Integer> interpolated_list = new ArrayList<>();
		// float[] x = new float[seq.size()];
		// float[] y = new float[seq.size()];
		if (seq.size() < 2) {
			System.out.println("Error: The number of towers must be greater than 2");
			System.exit(0);
		}
		// for (int i = 0; i < seq.size(); i++) {
		// int twr = seq.get(i);
		// Vertex twr_v = towers.get(twr);
		// x[i] = (float) twr_v.getX();
		// y[i] = (float) twr_v.getY();
		// System.out.println(x[i] + " , " + y[i]);
		// }
		//
		// CubicInterpolator ci = new
		// CubicInterpolator(CubicInterpolator.Method.MONOTONIC, x, y);

		int cur = seq.get(0);
		interpolated_list.add(cur);
		for (int i = 1; i < seq.size(); i++) {
			int nxt = seq.get(i);
			ArrayList<Integer> cur_n = voronoi_neighbors.get(cur);

			/**
			 * The the neighbors list contain the next tower, add to the
			 * interpolation list and set the current to the next one. Else if,
			 * interpolate missed observations using Hermite piecewise cubic
			 * interpolator with adjacent towers between the current tower and
			 * the next one.
			 *
			 */
			if (!cur_n.contains(nxt)) {

				// } else {
				// subseq_hermite_cubic_interpolator(interpolated_list, ci, cur,
				// nxt);
				boolean flag = subseq_hermite_cubic_interpolator(interpolated_list, cur, nxt);
				// int last = interpolated_list.get(interpolated_list.size() -
				// 1);
				if (!flag) {
					return interpolated_list;
				}
			}
			interpolated_list.add(nxt);
			cur = nxt;
		}

		return interpolated_list;
	}

	/**
	 * add interpolated points to the curve ....
	 *
	 * @param org_crv
	 * @param p
	 * @param indx
	 * @return
	 */
	public Vertex[] insert_point(Vertex[] org_crv, Vertex p, int indx) {
		int sz = org_crv.length + 1;
		Vertex[] mdf_crv = new Vertex[sz];
		for (int i = 0; i < sz; i++) {
			if (i == indx) {
				mdf_crv[i] = p;
				mdf_crv[i + 1] = org_crv[i];
			} else if (i < indx) {
				mdf_crv[i] = org_crv[i];
			} else {
				mdf_crv[i + 1] = org_crv[i];
			}
		}
		return mdf_crv;
	}

	/**
	 * Interpolate observations based on the gyration radius of users ...
	 *
	 * @param obs
	 * @return
	 */
	public Hashtable<String, Hashtable<Integer, Obs>> interpolate_observations(
			Hashtable<String, Hashtable<Integer, Obs>> obs) {
		Gyration gyration = new Gyration(this.towers);
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String day_key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();

			Hashtable<Integer, Obs> new_value = new Hashtable<>();
			for (Map.Entry<Integer, Obs> usr_entryset : value.entrySet()) {
				Integer usr_key = usr_entryset.getKey();
				Obs usr_obs = usr_entryset.getValue();

				String usr_obs_twr = usr_obs.getSeq();
				List<Integer> seq = Gyration.toList(usr_obs_twr);

				if (seq.isEmpty() || seq == null || seq.size() < 2) {
					continue;
				}
				double rg = gyration.calc_rg(seq) / 1000;
				List<Integer> ilist;
				if (rg <= 3) {
					ilist = linear_interpolator(seq);
				} else {
					ilist = hermite_interpolator(seq);
				}
				usr_obs_twr = gyration.toString(ilist);
				usr_obs.setSeq(usr_obs_twr);
				new_value.put(usr_key, usr_obs);
				// value.replace(usr_key, usr_obs);
			}
			obs.replace(day_key, new_value);
		}
		return obs;
	}

	public boolean is_adjacent(int t1, int t2) {
		ArrayList<Integer> t1_list = voronoi_neighbors.get(t1);
		if (t1_list.contains(t2)) {
			return true;
		}
		return false;
	}

	public boolean is_connected(int t1, int t2) {
		ConnectivityInspector<Integer, DefaultWeightedEdge> inspector = new ConnectivityInspector<>(graph);
		return inspector.pathExists(t1, t2);
	}

	public List<Integer> linear_interpolator(List<Integer> seq) {

		List<Integer> m_seq = new ArrayList<>();
		for (int i = 1; i < seq.size();) {
			int crt;
			if (!m_seq.isEmpty()) {
				crt = m_seq.get(m_seq.size() - 1);
			} else {
				crt = seq.get(i - 1);
				m_seq.add(crt);
			}
			int nxt = seq.get(i);

			if (nxt == crt) {
				continue;
			}
			// else {
			// while (!voronoi_neighbors.containsKey(crt) && i < seq.size()) {
			// i++;
			// crt = seq.get(i - 1);
			// }
			// }
			// int nxt = -1;
			// while (!voronoi_neighbors.containsKey(nxt) && i < seq.size()) {
			// nxt = seq.get(i);
			// i++;
			// }

			if (is_adjacent(crt, nxt)) {
				m_seq.add(nxt);
			} else if (is_connected(crt, nxt)) {
				// List<Integer> list = subseq_linear_interpolator_graph(crt,
				// nxt);
				List<Integer> list = subseq_linear_interpolator(crt, nxt);
				// if (list.isEmpty()) {
				// continue;
				// }
				list.remove(0);
				m_seq.addAll(list);
			} else {
				System.err.printf("Broken path between (%d,%d )", crt, nxt);
				return m_seq;
			}
		}
		return m_seq;
	}

	/**
	 * Sat Dec 5 19:52:51 EET 2015 Monotonic x is required, so I'll reduce the
	 * xy arrays to the current and next locations ...
	 *
	 * @param ilist
	 * @param ci
	 * @param cur
	 * @param nxt
	 */
	private void subseq_hermite_cubic_interpolator(List<Integer> ilist, CubicInterpolator ci, int cur, int nxt) {

		/**
		 * interpolated missed observations ...
		 */
		ArrayList<Integer> cur_n = voronoi_neighbors.get(cur);
		Vertex nxt_v = towers.get(nxt);
		while (!cur_n.contains(nxt)) {
			float min = Float.MAX_VALUE;
			Vertex cur_v = towers.get(cur);
			double prev_dist = euclidean(cur_v.getX(), cur_v.getY(), nxt_v.getX(), nxt_v.getY());

			int itwr = -1;
			for (Iterator<Integer> iterator = cur_n.iterator(); iterator.hasNext();) {
				Integer twr = iterator.next();
				Vertex twr_v = towers.get(twr);
				double twr_dist = euclidean(twr_v.getX(), twr_v.getY(), nxt_v.getX(), nxt_v.getY());

				float y = ci.interpolate((float) twr_v.getX());
				float delta_y = Math.abs((float) twr_v.getY() - y);
				/**
				 * check the difference between the original y and interpolated
				 * y, and the direction of insertion of the chosen zones ..
				 */
				if (delta_y < min && twr_dist < prev_dist) {
					min = delta_y;
					itwr = twr;
				}
			}
			// add nearest interpolated zone to the ilist ...
			if (itwr == -1) {
				System.err.printf(
						"Error: Zones (%d,%d) are not adjacent and couldn't find an adjacnet sequence of zones in linear interpolation\n",
						cur, nxt);
				System.exit(0);
			}

			ilist.add(itwr);
			cur = itwr;
			cur_n = voronoi_neighbors.get(cur);
		}

	}

	/**
	 *
	 * @param ilist
	 * @param cur
	 * @param nxt
	 */
	private boolean subseq_hermite_cubic_interpolator(List<Integer> ilist, int cur, int nxt) {

		if (cur == nxt) {
			ilist.add(nxt);
			return true;
		}
		float[] x = new float[2];
		float[] y = new float[2];

		Vertex twr1_v = towers.get(cur);
		Vertex twr2_v = towers.get(nxt);

		x[0] = (float) twr1_v.getX();
		y[0] = (float) twr1_v.getY();

		x[1] = (float) twr2_v.getX();
		y[1] = (float) twr2_v.getY();

		// System.out.println(cur + " , " + nxt);
		// System.out.println(x[0] + " , " + y[0]);
		// System.out.println(x[1] + " , " + y[1]);
		CubicInterpolator ci = new CubicInterpolator(CubicInterpolator.Method.MONOTONIC, x, y);

		/**
		 * interpolated missed observations ...
		 */
		ArrayList<Integer> cur_n = voronoi_neighbors.get(cur);
		Vertex nxt_v = towers.get(nxt);
		while (!cur_n.contains(nxt)) {
			float min = Float.MAX_VALUE;
			Vertex cur_v = towers.get(cur);
			double prev_dist = euclidean(cur_v.getX(), cur_v.getY(), nxt_v.getX(), nxt_v.getY());

			int itwr = -1;
			for (Iterator<Integer> iterator = cur_n.iterator(); iterator.hasNext();) {
				Integer twr = iterator.next();
				// check if one of the neighbor is outside the specified region
				// ..
				if (!towers.containsKey(twr))
					continue;
				// if (etwr != -1 && stwr != -1) {
				// if (twr < stwr || twr > etwr) {
				// continue;
				// }
				// }
				Vertex twr_v = towers.get(twr);
				double twr_dist = euclidean(twr_v.getX(), twr_v.getY(), nxt_v.getX(), nxt_v.getY());

				// float iy = ci.interpolate((float) twr_v.getX());
				float delta_y = Math.abs((float) twr_v.getY() - ci.interpolate((float) twr_v.getX()));
				/**
				 * check the difference between the original y and interpolated
				 * y, and the direction of insertion of the chosen zones ..
				 */
				if (delta_y < min && twr_dist < prev_dist) {
					min = delta_y;
					itwr = twr;
				}
			}
			// add nearest interpolated zone to the ilist ...
			if (itwr == -1) {
				System.err.printf(
						"Broken path between zones (%d,%d) and cann't find an adjacnet sequence of zones in cubic interpolation\n",
						cur, nxt);
				// ilist.add(cur);
				// ilist.add(nxt);
				return false;
				// System.exit(0);
			}

			ilist.add(itwr);
			cur = itwr;
			cur_n = voronoi_neighbors.get(cur);
		}
		return true;
	}

	/**
	 * Linearly interpolate missed observations ..
	 *
	 * @param t1
	 * @param t2
	 * @return
	 */
	private List<Integer> subseq_linear_interpolator(int t1, int t2) {
		// System.out.println("Start inter ...");
		List<Integer> interpolated_list = new ArrayList<>();
		Vertex t1_v = towers.get(t1);
		Vertex t2_v = towers.get(t2);
		int cur = t1;
		// System.out.printf("t2_v:%f, t2_v:%f, t1_v.getX(),
		// t1_v.getY()",t2_v.getX(), t2_v.getY(), t1_v.getX(), t1_v.getY());
		double dist = 0;
		dist = euclidean(t2_v.getX(), t2_v.getY(), t1_v.getX(), t1_v.getY());

		do {
			// System.out.print(cur + ",");
			interpolated_list.add(cur);
			ArrayList<Integer> t1_n = voronoi_neighbors.get(cur);
			if (t1_n.contains(t2)) {
				interpolated_list.add(t2);
				break;
			}
			double v_min = Double.MAX_VALUE;
			double l_min = dist;
			for (Iterator<Integer> iterator = t1_n.iterator(); iterator.hasNext();) {
				int nxt = iterator.next();
				if (interpolated_list.contains(nxt)) {
					continue;
				}

				// check if one of the neighbor is outside the specified region
				// ..
				if (!towers.containsKey(nxt))
					continue;
				// if (etwr != -1 && stwr != -1) {

				// if (nxt < stwr || nxt > etwr) {
				// continue;
				// }
				// }

				Vertex nxt_v = towers.get(nxt);
				double nxt_dist = 0;
				// try {
				nxt_dist = euclidean(t2_v.getX(), t2_v.getY(), nxt_v.getX(), nxt_v.getY());
				// } catch (NullPointerException e) {
				// System.out.printf("towers from %d to %d\n", nxt, t2);
				// continue;
				// }

				double vertical_dist = distance(t1_v, t2_v, nxt_v);
				if (v_min > vertical_dist && l_min > nxt_dist) {
					cur = nxt;
					v_min = vertical_dist;
					dist = nxt_dist;

				}

			}
		} while (cur != t2);
		return interpolated_list;
	}

	public List<Integer> subseq_linear_interpolator_graph(int t1, int t2) {
		List<Integer> ilist = new ArrayList<>();
		List<DefaultWeightedEdge> wp = DijkstraShortestPath.findPathBetween(graph, t1, t2);

		for (DefaultWeightedEdge next : wp) {
			ilist.add(graph.getEdgeSource(next));
		}
		ilist.add(graph.getEdgeTarget(wp.get(wp.size() - 1)));

		return ilist;
	}
}

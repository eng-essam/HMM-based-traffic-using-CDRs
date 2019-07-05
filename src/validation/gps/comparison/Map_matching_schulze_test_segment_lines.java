package validation.gps.comparison;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import Density.Plot;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;
import Viterbi.WieghtedGraph;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.GPSTrkPnt;
import utils.Vertex;

public class Map_matching_schulze_test_segment_lines {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double scale = Double.parseDouble(args[0]);
		double minlat = Double.parseDouble(args[1]);
		double minlon = Double.parseDouble(args[2]);
		double maxlat = Double.parseDouble(args[3]);
		double maxlon = Double.parseDouble(args[4]);
		String edges_file_path = args[5];
		String towers_file_path = args[6];
		String gpx_file_path = args[7];
		String voronoi_file_path = args[8];
		String network_file_path = args[9];
		String image_file_path = args[10];

		float distance_thrshd = 100;
		Hashtable<Integer, Vertex> twrs = DataHandler.readTowers(towers_file_path);
		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();

		Map_matching_schulze mms = new Map_matching_schulze();
		mms.index_edges(edges);
		// read GPX format file ..
		List<List<GPSTrkPnt>> trk_segs_list = mms.read_gpx_data_no_proj(gpx_file_path, scale);
		// map cellular observations ..
		List<List<GPSTrkPnt>> gps_pnt_zones = mms.map_gps_pnt_zones(trk_segs_list, voronoi_file_path);
		WieghtedGraph graph = mms.construct_traffic_graph(network_file_path);

		gps_pnt_zones.stream().forEach(gps_seg -> {
			int index = gps_pnt_zones.indexOf(gps_seg);
			// these skipped segments contains missing end, which cause
			// incomplete
			// trajectories calculated by schulze method. but they return good
			// fit within our method ...
			// if (index != 8 && index != 11 && index != 17) {
			if (index == 0) {
				// find origin/destinations alternatives based on the closest
				// five
				// edges
				// to the cellular towers ....
				Hashtable<Integer, List<String>> close_twrs_edges = mms.get_start_end_egdes(twrs, edges, gps_seg,
						distance_thrshd);

				Plot plotter = new Plot(edges,
						image_file_path.replace(".png", "_" + gps_pnt_zones.indexOf(gps_seg) + ".png"));

				plotter.scale(minlat * scale, minlon * scale, maxlat * scale, maxlon * scale);
				plotter.plotMapEdges();
				plotter.plot_towers(twrs);
				// plot the closest segements
				// close_twrs_edges.entrySet().stream().forEach(e -> {
				// plotter.mark_map_edges(e.getValue(), Color.ORANGE, 0.001);
				// });

				plotter.plot_gpx_traces(gps_seg, Color.RED);

				// find routes using map matching approach proposed by schulze
				// 2015
				double max_search_dist = 1000;

				List<String> route = mms.find_route(graph, close_twrs_edges, gps_seg, max_search_dist);
				// System.out.println(route);
				plotter.mark_map_edges(route, Color.BLUE, 0.001);

				// viterbi

				String obs[] = mms.get_observations(gps_seg);
				String vedges[] = varify_viterbi_path(obs);
				plotter.plotPath(vedges, Color.ORANGE, 0.001);

				// String[] smoothed_vedges =smooth_viterbi_path(graph, vedges);
				// plotter.plotPath(smoothed_vedges, Color.CYAN, 0.001);
				plotter.display_save();

				// segment lines

				double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
				double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

				double xmin, ymin, xmax, ymax;
				xmin = xymin[0];
				xmax = xymax[0];
				ymin = xymin[1];
				ymax = xymax[1];
				plotter = new Plot(edges,
						image_file_path.replace(".png", "_" + gps_pnt_zones.indexOf(gps_seg) + "_pnts.png"));
				plotter.scale(xmin, ymin, xmax, ymax);

				// get points ..
				double increment = 20; // meters
				String l = "";
				double[] ls = new double[3];
				mms.plot_route_pnst(mms.get_gps_points(gps_seg, scale, increment, ls), Color.RED);
				mms.plot_route_pnst(mms.get_vpath_points(vedges, scale, increment, ls), Color.ORANGE);
				mms.plot_route_pnst(mms.get_mm_points(route, scale, increment, ls), Color.BLUE);

				plotter.display_save();
			}
		});

	}

	public static String[] varify_viterbi_path(String[] usrObs) {
		String transPath = "/home/essam/traffic/models/dakar/main/transition.xml";
		String emissionPath = "/home/essam/traffic/models/dakar/main/emission.xml";

		DataHandler adaptor = new DataHandler();

		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
		Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
		ArrayList<String> exts = adaptor.getExts(trans_p);

		String[] states = exts.toArray(new String[exts.size()]);

		Hashtable<String, Double> start = adaptor.getStartProb(trans_p);
		// states = exts.toArray(new String[exts.size()]);
		Viterbi viterbi = new Viterbi();

		Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

		ObsIndex oi = new ObsIndex();
		oi.Initialise(states, emit_p);

		Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs, states, emit_p, si, oi);
		String vit_out = (String) ret[1];
		return vit_out.split(",");
	}

}

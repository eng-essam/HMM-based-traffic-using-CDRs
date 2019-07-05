package validation.gps.comparison;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.DoubleStream;

import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;
import Viterbi.WieghtedGraph;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.GPSTrkPnt;
import utils.Vertex;

/**
 * 
 * @author essam
 * @date Fri 29 Jul 20:06:24 EET 2016
 * 
 *       java -cp
 *       .:../lib/diva.jar:../lib/Java_Delaunay.jar:../lib/jcoord-1.0.jar:../lib
 *       /jgrapht.jar -Xss1g -d64 -XX:+UseG1GC -Xms5g -Xmx5g
 *       -XX:MaxGCPauseMillis=500
 *       validation.gps.comparison.Map_matching_schulze_viterbi_residuals 1000
 *       $dir/edges.xml $dir/towers.csv $dir/1556984.gpx $dir/voronoi.csv
 *       $dir/map.xy.dist.vor.xml $dir/r.csv
 *
 */
public class Map_matching_schulze_viterbi_residuals {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double scale = Double.parseDouble(args[0]);
		double sampling_time_threshold = Double.parseDouble(args[1]);
		String edges_file_path = args[2];
		String towers_file_path = args[3];
		String gpx_file_path = args[4];
		String voronoi_file_path = args[5];
		String network_file_path = args[6];
		String output_file_path = args[7];

		float distance_thrshd = 100;
		double alignment_threshold = 20;
		Hashtable<Integer, Vertex> twrs = DataHandler.readTowers(towers_file_path);
		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();

		Map_matching_schulze mms = new Map_matching_schulze();
		mms.index_edges(edges);
		// read GPX format file ..
		List<List<GPSTrkPnt>> trk_segs_list = mms.read_gpx_data_no_proj(gpx_file_path, scale);
		// map cellular observations ..
		// List<List<GPSTrkPnt>> gps_pnt_zones =
		// mms.map_gps_pnt_zones(trk_segs_list, voronoi_file_path,
		// sampling_time_threshold);
		List<List<GPSTrkPnt>> gps_pnt_zones = mms.map_gps_pnt_zones(trk_segs_list, voronoi_file_path);
		WieghtedGraph graph = mms.construct_traffic_graph(network_file_path);

		List<String> distances = new ArrayList<>();
		List<String> divergences = new ArrayList<>();
		List<String> alignments = new ArrayList<>();

		gps_pnt_zones.stream().forEach(gps_seg -> {
			int index = gps_pnt_zones.indexOf(gps_seg);
			System.out.println("Current track segment is " + index);
			// these skipped segments contains missing end, which cause
			// incomplete. trajectories calculated by schulze method. but they
			// return good
			// fit within our method ...
			if (index != 8 && index != 11 && index != 17) {
				// if (index == 0) {
				// find origin/destinations alternatives based on the closest
				// five
				// edges
				// to the cellular towers ....
				Hashtable<Integer, List<String>> close_twrs_edges = mms.get_start_end_egdes(twrs, edges, gps_seg,
						distance_thrshd);

				// // find routes using map matching approach proposed by
				// schulze 2015
				double max_search_dist = 1000;
				//
				List<String> route = mms.find_route(graph, close_twrs_edges, gps_seg, max_search_dist);

				// viterbi
				String obs[] = mms.get_observations(gps_seg);
				String vedges[] = mms.remove_duplicated_edges(varify_viterbi_path(obs));

				// String vedges[] = varify_viterbi_path(obs);
				// print edges
				// for (int ii = 0; ii < vedges.length; ii++) {
				// System.out.printf("\"%s\",", vedges[ii]);
				// }
				//
				// // String[] smoothed_vedges =smooth_viterbi_path(graph,
				// vedges);

				// get points ..
				double increment = 1; // meters

				double[] ls = new double[3];
				List<Vertex> gps_pnts = mms.get_gps_points(gps_seg, scale, increment, ls);
				// System.out.println("Viterbi\n==================================================");
				List<Vertex> viterbi_pnts = mms.get_vpath_points(vedges, scale, increment, ls);
				// System.out.println("MM_S\n==================================================");
				List<Vertex> mms_pnts = mms.get_mm_points(route, scale, increment, ls);

				distances.add(ls[0] + "," + ls[1] + "," + ls[2]);
				// System.out.println(length);

				double[] viterbi_residuals = mms.georesiduals(gps_pnts, viterbi_pnts, scale);
				// Diveregence is the average alignment distances ...
				double viterbi_div = DoubleStream.of(viterbi_residuals).sum() / viterbi_residuals.length;
				double viterbi_align_ratio = mms.get_alignment_ratio(viterbi_residuals, alignment_threshold);

				mms.write_csv(viterbi_residuals, output_file_path.replace(".csv",
						"_" + index + "_viterbi_residuals_sampling_" + sampling_time_threshold + "_sec.csv"));
				double[] mms_residuals = mms.georesiduals(gps_pnts, mms_pnts, scale);
				double mms_div = DoubleStream.of(mms_residuals).sum() / mms_residuals.length;
				double mms_align_ratio = mms.get_alignment_ratio(mms_residuals, alignment_threshold);

				mms.write_csv(mms_residuals, output_file_path.replace(".csv",
						"_" + index + "_mms_residuals_sampling_" + sampling_time_threshold + "_sec.csv"));

				divergences.add(viterbi_div + "," + mms_div);
				alignments.add(viterbi_align_ratio + "," + mms_align_ratio);
				// System.out.println("Finished ...");
			}
		});

		mms.write_csv(distances, output_file_path.replace(".csv",
				"_travelling_distances_sampling_" + sampling_time_threshold + "_sec.csv"));
		mms.write_csv(divergences,
				output_file_path.replace(".csv", "_divergence_sampling_" + sampling_time_threshold + "_sec.csv"));
		mms.write_csv(alignments,
				output_file_path.replace(".csv", "_alignment_ratio_sampling_" + sampling_time_threshold + "_sec.csv"));
	}

	public static String[] varify_viterbi_path(String[] usrObs) {
		String transPath = "/home/essam/traffic/models/dakar/main_no_proj/transition.xml";
		String emissionPath = "/home/essam/traffic/models/dakar/main_no_proj/emission.xml";

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

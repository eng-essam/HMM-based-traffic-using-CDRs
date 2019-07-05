package validation.gps.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;

public class main_avg_distance_from_roads_to_exts {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double scale = Double.parseDouble(args[0]);
		String edges_file_path = args[1];
		List<String> roads = Arrays.asList("motorway", "motorway_link", "trunk", "trunk_link", "primary",
				"primary_link", "secondary", "secondary_link", "tertiary", "unclassified");

		List<String> allroads = Arrays.asList("residential", "service", "road");

		DataHandler adaptor = new DataHandler();
		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();
		List<Double> dists = new ArrayList<>();

		edges.parallelStream().forEach(fn -> {
			if (allroads.contains(fn.type)) {
				double min = Double.MAX_VALUE;
				double[] fn_cnt = fn.getCenterPnt();
				for (int i = 0; i < edges.size(); i++) {
					Edge nfn = edges.get(i);
					double[] cnt = nfn.getCenterPnt();

					if (roads.contains(nfn.type)) {
						double dist = DataHandler.geodistance(fn_cnt[0] / scale, fn_cnt[1] / scale, cnt[0] / scale,
								cnt[1] / scale, "m");
						if (dist < min)
							min = dist;

					}
				}
				dists.add(min);
			}
		});

		double sum = 0;
		for (double d : dists) {
			sum += d;
		}
		System.out.println(sum / dists.size());

	}

}

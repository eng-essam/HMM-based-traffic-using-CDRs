package netconvert.test;

import java.util.ArrayList;

import Density.Plot;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;

public class main_plot_map_edges {

	public static void main(String[] args) {
		double minlat = Double.parseDouble(args[0]);
		double minlon = Double.parseDouble(args[1]);
		double maxlat = Double.parseDouble(args[2]);
		double maxlon = Double.parseDouble(args[3]);
		String edges_file_path = args[4];
		String image_file_path = args[5];
		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();

		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		double xmin, ymin, xmax, ymax;
		xmin = 0;
		xmax = xymax[0] - xymin[0];
		ymin = 0;
		ymax = xymax[1] - xymin[1];

		/**
		 * Complete Dakar map
		 */
		// xmin = 227761.06;
		// xmax = 270728.96;
		// ymin = 1618439.13;
		// ymax = 1645065.55;
		Plot plotter = new Plot(edges, image_file_path);
		plotter.scale(xmin, ymin, xmax, ymax);
		//// plotter.plotMapData(map_path);
		plotter.plotMapEdges();
		// render output
		plotter.display_save();
	}
	
//	public static void main(String[] args){
//		String osm_file_path = args[0];
//		String image_file_path = args[1];
//		
//		netconverter nc = new netconverter();
//
//        nc.read_osm(osm_file_path);
//        ArrayList<Edge> edges = new ArrayList<>(nc.construct_map_edges());
//       
//	}

}

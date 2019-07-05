package validation.gps.comparison;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Density.Plot;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.GPSTrkPnt;
import utils.StdDraw;

public class main_read_gpx {

	public static void main(String[] args) {
		double minlat = Double.parseDouble(args[0]);
		double minlon = Double.parseDouble(args[1]);
		double maxlat = Double.parseDouble(args[2]);
		double maxlon = Double.parseDouble(args[3]);
		String edges_file_path = args[4];
		String gpx_file_path = args[5];// "/home/essam/traffic/1556984.gpx";
		String image_file_path = args[6];

		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();

		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		double xmin, ymin, xmax, ymax;
		xmin = 0;
		xmax = xymax[0] - xymin[0];
		ymin = 0;
		ymax = xymax[1] - xymin[1];
		List<List<GPSTrkPnt>> trk_segs_list = read_gpx_data(gpx_file_path, xymin[0], xymin[1]);
		Plot plotter = new Plot(edges, image_file_path);
		plotter.scale(xmin, ymin, xmax, ymax);
		plotter.plotMapEdges();
		for (int i = 0; i < trk_segs_list.size() - 1; i++) {

			List<GPSTrkPnt> trk_pnts_list = trk_segs_list.get(i);
			
			
			//// plotter.plotMapData(map_path);
			
//			StdDraw.setPenColor(Color.getHSBColor((float) Math.random(), (float) Math.random(), (float) Math.random()));
			plot_gpx_traces(trk_pnts_list, Color.getHSBColor((float) Math.random(), (float) Math.random(), (float) Math.random()));
			// render output
			
		}
		plotter.display_save();
		
		// int length = 0;
		// for (int i = 0; i < trk_segs_list.size() - 1; i++) {
		//
		// List<GPSTrkPnt> trk_pnts_list = trk_segs_list.get(i);
		// long time_diff =
		// Math.abs(get_time_diff(trk_pnts_list.get(0).timestamp,
		// trk_pnts_list.get(trk_pnts_list.size()-1).timestamp,
		// TimeUnit.MINUTES));
		//
		//// for (int j = 0; j < trk_segs_list.size() - 1; j++) {
		////
		//// long time_diff =
		// Math.abs(get_time_diff(trk_pnts_list.get(j).timestamp,
		//// trk_pnts_list.get(j + 1).timestamp, TimeUnit.SECONDS));
		////// length++;
		// System.out.println(time_diff);
		//// // if ((time_diff >= 210 && length >= 50) || time_diff > 2000) {
		//// // System.out.println(length + "\t" + time_diff);
		//// // length = 0;
		//// // }
		//// }
		//// System.out.println("------------------------------");
		// }

	}

	/**
	 * 
	 * @param vl
	 * @param c
	 */
	public static void plot_gpx_traces(List<GPSTrkPnt> pnts, Color c) {
		StdDraw.setPenColor(c);
		StdDraw.setPenRadius(0.001);
		for (int i = 0; i < pnts.size() - 1; i++) {
			GPSTrkPnt v1 = pnts.get(i);
			GPSTrkPnt v2 = pnts.get(i + 1);
			StdDraw.line(v1.x, v1.y, v2.x, v2.y);
		}

	}

	public static List<List<GPSTrkPnt>> read_gpx_data(String gpx_file_path,double xmin,double ymin) {
		SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<List<GPSTrkPnt>> trk_segs_list = new ArrayList<>();
		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(gpx_file_path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();

			NodeList trk_seg_list = dpDoc.getElementsByTagName("trkseg");
			for (int is = 0; is < trk_seg_list.getLength(); is++) {

				Node trkseg = trk_seg_list.item(is);
				if (trkseg.getNodeType() == Node.ELEMENT_NODE) {
					Element trksegElement = (Element) trkseg;
					NodeList trk_pnt_list = trksegElement.getElementsByTagName("trkpt");

					List<GPSTrkPnt> trk_pnts_list = new ArrayList<>();
					for (int i = 0; i < trk_pnt_list.getLength(); i++) {
						GPSTrkPnt pnt = new GPSTrkPnt();
						Node trkpnt = trk_pnt_list.item(i);
						if (trkpnt.getNodeType() == Node.ELEMENT_NODE) {
							Element trkpntElement = (Element) trkpnt;
							pnt.lat = Double.parseDouble(trkpntElement.getAttribute("lat"));
							pnt.lon = Double.parseDouble(trkpntElement.getAttribute("lon"));

							double xy[]=DataHandler.proj_coordinates(pnt.lat, pnt.lon, xmin, ymin);
							pnt.x=xy[0]+500;
							pnt.y=xy[1]+500;
							
							// eElement.getChildNodes()
							NodeList timestamp_list = trkpntElement.getElementsByTagName("time");
							String date = "XXX";
							for (int j = 0; j < timestamp_list.getLength(); j++) {
								Node time_node = timestamp_list.item(j);

								if (time_node.getNodeType() == Node.ELEMENT_NODE) {
									pnt.timestamp = dtf.parse(
											time_node.getTextContent().replace('T', ' ').replace('Z', ' ').trim());
								}
							}
							// System.out.printf("%f,%f,%s\n", lon, lat, date);
							trk_pnts_list.add(pnt);
						}

					}
					trk_segs_list.add(trk_pnts_list);
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException | ParseException ex) {
			Logger.getLogger(main_read_gpx.class.getName()).log(Level.SEVERE, null, ex);
		}
		return trk_segs_list;
	}

	/**
	 * Get a diff between two dates
	 * 
	 * @param date1
	 *            the oldest date
	 * @param date2
	 *            the newest date
	 * @param timeUnit
	 *            the unit in which you want the diff
	 * @return the diff value, in the provided unit
	 */
	public static long get_time_diff(Date date1, Date date2, TimeUnit time_unit) {
		long diff_millies = date2.getTime() - date1.getTime();
		return time_unit.convert(diff_millies, TimeUnit.MILLISECONDS);
	}
}

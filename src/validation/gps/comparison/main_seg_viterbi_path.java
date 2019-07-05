package validation.gps.comparison;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import Density.Plot;
import utils.DataHandler;
import utils.StdDraw;
import utils.Vertex;

public class main_seg_viterbi_path {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String image_file_path = "/home/essam/line_seg_image.png";

		 double minlat = 14.5597, minlon = -17.5616, maxlat = 14.9036, maxlon
		 = -17.1098;

		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		 double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		double xmin, ymin, xmax, ymax;
		xmin = 0;
		xmax = 2000;
		ymin = 0;
		ymax = 2000;

		Plot plotter = new Plot(image_file_path);
		plotter.scale(xmin, ymin, xmax, ymax);

		List<Vertex> pnts = new ArrayList<>();
//		pnts.add(new Vertex(10, 50));
//		pnts.add(new Vertex(588, 230));
//		pnts.add(new Point2D.Double(720, 1240));
//		pnts.add(new Point2D.Double(1420, 1500));
//		pnts.add(new Point2D.Double(1800, 542));

		Map_matching_schulze mms = new Map_matching_schulze();
		List<Vertex> spnts = mms.sample_routes(pnts, 100);

		StdDraw.setPenRadius(0.01);
		for (int i = 0; i < spnts.size() - 1; i++) {
			Vertex p0 = spnts.get(i);
			Vertex p1 = spnts.get(i + 1);
			StdDraw.line(p0.getX(), p0.getY(), p1.getX(), p1.getY());
		}

		StdDraw.setPenColor(Color.ORANGE);

//		List<Point2D> segs = segment_line(p0, p1, 100);
//		System.out.println(segs.size());

		spnts.stream().forEachOrdered(p -> {
			StdDraw.filledCircle(p.getX(), p.getY(), 10);
			// System.out.println(p.getX() + "," + p.getY());
		});

		plotter.display_save();

	}

	public static List<Point2D> segment_line(Point2D p0, Point2D p1, double increment) {

		List<Point2D> segs = new ArrayList<>();

		// check start of the segment ..
		if (p1.getX() < p0.getX()) {
			Point2D tmp = new Point2D.Double(p0.getX(), p0.getY());
			p0.setLocation(p1.getX(), p1.getY());
			p1.setLocation(tmp.getX(), tmp.getY());
		}

		double dy = p1.getY() - p0.getY();
		double dx = p1.getX() - p0.getX();

		if (dy == 0) {
			// horizontal line
			segs.add(p0);
			for (int i = 1; i <= (dx / increment); i++) {
				segs.add(new Point2D.Double(p0.getX() + i * increment, p0.getY()));
			}
			segs.add(p1);

		} else if (dx == 0) {
			// vertical line
			if (p1.getY() < p0.getY()) {
				Point2D tmp = new Point2D.Double(p0.getX(), p0.getY());
				p0.setLocation(p1.getX(), p1.getY());
				p1.setLocation(tmp.getX(), tmp.getY());
			}

			segs.add(p0);
			for (int i = 1; i <= (dy / increment); i++) {
				segs.add(new Point2D.Double(p0.getX(), p0.getY() + i * increment));
			}
			segs.add(p1);

		} else {
			// normal situations
			double m = (dy / dx);
			double C = p0.getY() - m * p0.getX();
			double length = DataHandler.euclidean(p0.getX(), p0.getY(), p1.getX(), p1.getY());

			segs.add(p0);
			double x = p0.getX(), y = p0.getY();
			for (int i = 1; i <= (length / increment); i++) {
				double A = x;
				double B = y;

				double a = 1 + Math.pow(m, 2);
				double b = 2 * m * (C - B) - 2 * A;
				double c = Math.pow(A, 2) + Math.pow(B, 2) + Math.pow(C, 2) - 2 * B * C - Math.pow(increment, 2);

				System.out.println(m + "\t" + A + "\t" + B + "\t" + a + "\t" + b + "\t" + c);
				double q_rs = Math.sqrt(Math.pow(b, 2) - 4 * a * c);
				System.out.println(q_rs);

				double q_x = (-b + q_rs) / (2 * a);
				double q_x1 = (-b - q_rs) / (2 * a);

				if (q_x > x && q_x < p1.getX()) {
					x = q_x;
				} else {
					x = q_x1;
				}
				y = x * m + C;
				segs.add(new Point2D.Double(x, y));
			}
			segs.add(p1);

		}
		return segs;
	}
}

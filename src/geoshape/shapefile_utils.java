package geoshape;

import java.awt.Color;

import diewald_shapeFile.files.dbf.DBF_File;
import diewald_shapeFile.files.shp.SHP_File;
import diewald_shapeFile.files.shp.shapeTypes.ShpPolygon;
import diewald_shapeFile.files.shx.SHX_File;
import diewald_shapeFile.shapeFile.ShapeFile;
import utils.StdDraw;

public class shapefile_utils {

	public static ShapeFile read_shapefile(String shapefile_dir, String shapefile_name) {
		DBF_File.LOG_INFO = !false;
		DBF_File.LOG_ONLOAD_HEADER = false;
		DBF_File.LOG_ONLOAD_CONTENT = false;

		SHX_File.LOG_INFO = !false;
		SHX_File.LOG_ONLOAD_HEADER = false;
		SHX_File.LOG_ONLOAD_CONTENT = false;

		SHP_File.LOG_INFO = !false;
		SHP_File.LOG_ONLOAD_HEADER = false;
		SHP_File.LOG_ONLOAD_CONTENT = false;
		// LOAD SHAPE FILE (.shp, .shx, .dbf)
		ShapeFile shapefile = null;
		try {
			shapefile = new ShapeFile(shapefile_dir, shapefile_name).READ();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return shapefile;

	}

	public static void plot_shapefile(ShapeFile shapefile) {

		// double[][] bb = shapefile.getSHP_boundingBox();
		// // bb [x, y, z][min,max]
		// double xmin = bb[0][0], xmax = bb[0][1], ymin = bb[1][0], ymax =
		// bb[1][1];
		// p.scale(xmin, ymin, xmax, ymax);
		StdDraw.setPenColor(Color.GRAY);
		int number_of_shapes = shapefile.getSHP_shapeCount();
		for (int i = 0; i < number_of_shapes; i++) {
			ShpPolygon shape = shapefile.getSHP_shape(i);
			double[][][] polygons = shape.getPointsAs3DArray();

			for (double[][] polygon : polygons) {
				double[] xp = new double[polygon.length];
				double[] yp = new double[polygon.length];
				for (int ii = 0; ii < polygon.length; ii++) {
					double[] point = polygon[ii];
					xp[ii] = point[0];
					yp[ii] = point[1];

				}
				StdDraw.polygon(xp, yp);
			}
		}

	}
}

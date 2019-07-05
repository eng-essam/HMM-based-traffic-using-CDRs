
package geoshape;

import Density.Plot;
import diewald_shapeFile.files.dbf.DBF_Field;
import diewald_shapeFile.files.dbf.DBF_File;
import diewald_shapeFile.files.shp.SHP_File;
import diewald_shapeFile.files.shp.shapeTypes.ShpPolygon;
import diewald_shapeFile.files.shp.shapeTypes.ShpShape;
import diewald_shapeFile.files.shx.SHX_File;
import diewald_shapeFile.shapeFile.ShapeFile;
import utils.StdDraw;

public class main_plot_shapefiles {
	public static void main(String[] args) {
		String shapefile_dir = "/media/essam/My Passport/TeleCom Dataset/2015/Caps/CAP_Shapefile";
		String shapefile_name = "CodAvvPostale";
		String image_output_path = "/media/essam/My Passport/TeleCom Dataset/2015/Caps/CAP_Shapefile/CodAvvPostale.png";
		// LOAD SHAPE FILE (.shp, .shx, .dbf)
		ShapeFile shapefile = shapefile_utils.read_shapefile(shapefile_dir, shapefile_name);
		double[][] bb = shapefile.getSHP_boundingBox();
		// bb [x, y, z][min,max]

		double xmin = bb[0][0], xmax = bb[0][1], ymin = bb[1][0], ymax = bb[1][1];
		Plot p = new Plot(image_output_path);
		p.scale(xmin, ymin, xmax, ymax);
		shapefile_utils.plot_shapefile(shapefile);
		p.display_save();
	}

	public static void main_1(String[] args) {
		DBF_File.LOG_INFO = !false;
		DBF_File.LOG_ONLOAD_HEADER = false;
		DBF_File.LOG_ONLOAD_CONTENT = false;

		SHX_File.LOG_INFO = !false;
		SHX_File.LOG_ONLOAD_HEADER = false;
		SHX_File.LOG_ONLOAD_CONTENT = false;

		SHP_File.LOG_INFO = !false;
		SHP_File.LOG_ONLOAD_HEADER = false;
		SHP_File.LOG_ONLOAD_CONTENT = false;

		try {
			String shapefile_dir = "/media/essam/My Passport/TeleCom Dataset/2015/Caps/CAP_Shapefile";
			String shapefile_name = "CodAvvPostale";
			String image_output_path = "/media/essam/My Passport/TeleCom Dataset/2015/Caps/CAP_Shapefile/CodAvvPostale.png";
			// LOAD SHAPE FILE (.shp, .shx, .dbf)
			ShapeFile shapefile = new ShapeFile(shapefile_dir, shapefile_name).READ();

			// TEST: printing some content
			ShpShape.Type shape_type = shapefile.getSHP_shapeType();
			System.out.println("\nshape_type = " + shape_type);

			int number_of_shapes = shapefile.getSHP_shapeCount();
			int number_of_fields = shapefile.getDBF_fieldCount();

			double[][] bb = shapefile.getSHP_boundingBox();
			// bb [x, y, z][min,max]

			double xmin = bb[0][0], xmax = bb[0][1], ymin = bb[1][0], ymax = bb[1][1];
			Plot p = new Plot(image_output_path);
			p.scale(xmin, ymin, xmax, ymax);

			for (int i = 0; i < number_of_shapes; i++) {
				ShpPolygon shape = shapefile.getSHP_shape(i);
				String[] shape_info = shapefile.getDBF_record(i);

				ShpShape.Type type = shape.getShapeType();
				int number_of_vertices = shape.getNumberOfPoints();
				int number_of_polygons = shape.getNumberOfParts();
				int record_number = shape.getRecordNumber();

				// polygons [number of polygons][number of points per
				// polygon][x, y, z, m]
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

				// System.out.printf("\nSHAPE[%2d] - %s\n", i, type);
				// System.out.printf(" (shape-info) record_number = %3d;
				// vertices = %6d; polygons = %2d\n", record_number,
				// number_of_vertices, number_of_polygons);

				for (int j = 0; j < number_of_fields; j++) {
					String data = shape_info[j].trim();
					DBF_Field field = shapefile.getDBF_field(j);
					String field_name = field.getName();
					// System.out.printf(" (dbase-info) [%d] %s = %s", j,
					// field_name, data);
				}
				// System.out.printf("\n");
			}
			p.display_save();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

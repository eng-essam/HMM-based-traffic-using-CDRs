
package geoshape;

import diewald_shapeFile.files.dbf.DBF_Field;
import diewald_shapeFile.files.dbf.DBF_File;
import diewald_shapeFile.files.shp.SHP_File;
import diewald_shapeFile.files.shp.shapeTypes.ShpPolygon;
import diewald_shapeFile.files.shp.shapeTypes.ShpShape;
import diewald_shapeFile.files.shx.SHX_File;
import diewald_shapeFile.shapeFile.ShapeFile;

public class main_shapefiles_reader {

	public static void main(String[] args) {
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
			String path = "/media/essam/My Passport/TeleCom Dataset/2015/Caps/CAP_Shapefile";
			String name = "CodAvvPostale";

			// LOAD SHAPE FILE (.shp, .shx, .dbf)
			ShapeFile shapefile = new ShapeFile(path, name).READ();

			// TEST: printing some content
			ShpShape.Type shape_type = shapefile.getSHP_shapeType();
			System.out.println("\nshape_type = " + shape_type);

			int number_of_shapes = shapefile.getSHP_shapeCount();
			int number_of_fields = shapefile.getDBF_fieldCount();

			double[][] bb = shapefile.getSHP_boundingBox();
			// bb [x, y, z][min,max]

			double xmin = bb[0][0], xmax = bb[0][1], ymin = bb[1][0], ymax = bb[1][1];
			// for (int i = 0; i < bb.length; i++) {
			// for (int j = 0; j < bb[i].length; j++) {
			// System.out.println(bb[i][j]);
			// }
			// }

			for (int i = 0; i < number_of_shapes; i++) {
				ShpPolygon shape = shapefile.getSHP_shape(i);
				String[] shape_info = shapefile.getDBF_record(i);

				ShpShape.Type type = shape.getShapeType();
				int number_of_vertices = shape.getNumberOfPoints();
				int number_of_polygons = shape.getNumberOfParts();
				int record_number = shape.getRecordNumber();

				// polygons [number of points][x,y,z]
				double[][] polygons = shape.getPoints();

				System.out.printf("\nSHAPE[%2d] - %s\n", i, type);
				System.out.printf(" (shape-info) record_number = %3d; vertices = %6d; polygons = %2d\n", record_number,
						number_of_vertices, number_of_polygons);

				for (int j = 0; j < number_of_fields; j++) {
					String data = shape_info[j].trim();
					DBF_Field field = shapefile.getDBF_field(j);
					String field_name = field.getName();
					System.out.printf(" (dbase-info) [%d] %s = %s", j, field_name, data);
				}
				// System.out.printf("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

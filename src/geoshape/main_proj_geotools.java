package geoshape;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class main_proj_geotools {

	public static void main(String[] args) throws IOException {
		String path = "/home/essam/Italy_shapefile/it_1km.prj";
		double lat = 41.133969;
		double lon = 16.76386;

		String wkt = new String(Files.readAllBytes(Paths.get(path)));
		// System.out.println(wkt);

		SpatialReference src = new SpatialReference();
		src.ImportFromProj4("+proj=latlong +datum=WGS84 +no_defs");

		SpatialReference dst = new SpatialReference(wkt);

		CoordinateTransformation ct = CoordinateTransformation.CreateCoordinateTransformation(src, dst);

		double p[] = ct.TransformPoint(lat, lon);
		System.out.print(p[0] + "\t" + p[1]);

	}

}

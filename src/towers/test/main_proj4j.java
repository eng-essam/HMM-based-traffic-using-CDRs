/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

//import org.osgeo.proj4j.CRSFactory;
//import org.osgeo.proj4j.CoordinateReferenceSystem;
//import org.osgeo.proj4j.CoordinateTransform;
//import org.osgeo.proj4j.CoordinateTransformFactory;
//import org.osgeo.proj4j.ProjCoordinate;

/**
 *
 * @author essam
 */
public class main_proj4j {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        String csName = "EPSG:2063";
// 29.44,30.78,30.13,31.32
        double lat = 30.78;
        double lon = 29.44;
        //http://spatialreference.org/ref/epsg/4229/
//        String spatial_ref = "EPSG:4229";
//        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
//        CRSFactory csFactory = new CRSFactory();
//        /*
//  	 * Create {@link CoordinateReferenceSystem} & CoordinateTransformation.
//  	 * Normally this would be carried out once and reused for all transformations
//         */
//        CoordinateReferenceSystem crs = csFactory.createFromName(spatial_ref);
//
//        final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84";
//        CoordinateReferenceSystem WGS84 = csFactory.createFromParameters("WGS84", WGS84_PARAM);
//
//        CoordinateTransform trans = ctFactory.createTransform(WGS84, crs);
//
//        /*
//     * Create input and output points.
//     * These can be constructed once per thread and reused.
//         */
//        ProjCoordinate p = new ProjCoordinate(lon, lat);
//        ProjCoordinate p2 = new ProjCoordinate();
//        /*
//         * Transform point
//         * 384196.61,3951943.96,384578.29,3952205.29
//         3675829.96,3910646.46
//         */
//        double ex1, ey1;
//        ex1 = 3675829.96;
//        ey1 = 3910646.46;
//
//        trans.transform(p, p2);
//        System.out.printf("x:%f\ty:%f\n", p2.x, p2.y);
////        System.out.printf("Delta_x:%f\tDelta_y:%f\n", p2.x - ex1, p2.y - ey1);
//
//        lon = 139.7791;
//        lat = 35.7015;
//        p = new ProjCoordinate(lon, lat);
//        ex1 = 384578.29;
//        ey1 = 3952205.29;
//        trans.transform(p, p2);
//        System.out.printf("x:%f\ty:%f\n", p2.x, p2.y);
//        System.out.printf("Delta_x:%f\tDelta_y:%f\n", p2.x - ex1, p2.y - ey1);
    }

}

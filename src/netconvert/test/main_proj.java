/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netconvert.test;

//import org.osgeo.proj4j.CoordinateReferenceSystem;
//import org.osgeo.proj4j.proj.Projection;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 *
 * @author essam
 */
public class main_proj {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		double lat = 41.0473;
		double lon = 16.6718;

//		 northing (latitude) or easting (longitude)
		 
		 LatLng ll =new LatLng(lat, lon);
		
		 UTMRef utm = ll.toUTMRef();
//		 OSRef u = ll.toOSRef();
		 System.out.println(utm.getEasting()+"\t"+utm.getNorthing());

	}

}

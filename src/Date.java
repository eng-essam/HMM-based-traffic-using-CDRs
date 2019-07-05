
import java.text.ParseException;

import Observations.ObsTripsBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class Date {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
//        String dt = "2013-01-31";  // Start date
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(sdf.parse(dt));
//        
////        c.add(Calendar.DATE, 1);  // number of days to add
////        dt = sdf.format(c.getTime());
////        System.out.println(dt);
//
////        Calendar cal = Calendar.getInstance();
////        cal.setFirstDayOfWeek(Calendar.MONDAY);
//        int rec = cal.get(Calendar.WEEK_OF_YEAR);
//        System.out.println(rec);
        String path = "/home/essam/traffic/SET2/SET2_P01.CSV";
        ObsTripsBuilder builder = new ObsTripsBuilder();
        builder.split_weeky_data(path);

    }

}

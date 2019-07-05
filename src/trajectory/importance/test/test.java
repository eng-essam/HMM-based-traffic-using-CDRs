/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author essam
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        // TODO code application logic here
        String date_str = "2013-02-09 22:20:00";
        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE MMM dd");

        Date d = parserSDF.parse(date_str);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        System.out.println(cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);  
        
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        
//        String dd = dayFormat.format(d);

//        cal.setTime(dd);
        System.out.println(dayOfYear+" " + hourOfDay);

//        int start_usr_id = 0;
//        int end_usr_id = 100000;
//        int threads = 16;
//        int usrs = end_usr_id - start_usr_id;
//        final int chunck = (int) Math.ceil((double) usrs / (double) threads);
//
//        for (int i = 0; i < threads; i++) {
//            final int s_index = start_usr_id + i * chunck;
//            System.out.println(s_index + "\t" + (s_index + chunck));
//        }

    }

}

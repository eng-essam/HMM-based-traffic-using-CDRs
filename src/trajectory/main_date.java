/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author essam
 */
public class main_date {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        // TODO code application logic here
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance(); // creates a new calendar instance
        
        String time = "15:10:0";

        Date dt = formatter.parse(time);
        cal.setTime(dt);
        System.out.println(cal.get(Calendar.HOUR_OF_DAY));

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

/**
 *
 * @author essam
 */
public class main_time_slots_test {

    /**
     * Home hours constants ..
     */
    private final static int START_NIGHT_HOURS = 19;
    private final static int END_NIGHT_HOURS = 7;

    /**
     * Work hours constants ..
     */
    private final static int START_WORK_HOURS = 13;
    private final static int END_WORK_HOURS = 17;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        int hourOfDay=Integer.parseInt(args[0]);
        
        //increment night or day light events ..
        if (hourOfDay >= START_WORK_HOURS && hourOfDay <= END_WORK_HOURS) {
            System.out.println("Work Hour");
        } else if (hourOfDay >= START_NIGHT_HOURS || hourOfDay <= END_NIGHT_HOURS) {
            System.out.println("Home Hour");
        }
    }
    
}

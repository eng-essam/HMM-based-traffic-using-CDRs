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
public class main_bool {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        boolean flag = Boolean.parseBoolean(args[0]);
//        System.out.println(flag);
        if (flag) {
            System.out.println("Get true ...");
        } else {
            System.out.println("Get false value ..");
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

import utils.DataHandler;

/**
 *
 * @author essam
 */
public class main_area {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        double coords[] = {29.4969, 30.8362, 30.0916, 31.3302};

        double[] xymin = DataHandler.proj_coordinates(coords[1], coords[0]);
        double[] xymax = DataHandler.proj_coordinates(coords[3], coords[2]);

        double w = Math.abs(xymax[0] - xymin[0]);
        double h = Math.abs(xymax[1] - xymin[1]);
        System.out.println("Width: " + w + "\thieght: " + h);
    }

}

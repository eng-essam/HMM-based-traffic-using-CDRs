/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import java.util.Arrays;

/**
 *
 * @author essam
 */
public class main_numbers {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        double probability = 0.00000000000045032;
//        String str = String.format("%2.4e",probability);
//        
//        System.out.println(str);
//        double prob = Double.parseDouble(str);
//        System.out.println(1-prob);
        
        double[][] dists = new double[3][3];
        for (int i = 0; i < dists.length; i++) {
            Arrays.fill(dists[i], -1.0);
            
        }
        
        System.out.println(dists[1][1]);
    }
    
}

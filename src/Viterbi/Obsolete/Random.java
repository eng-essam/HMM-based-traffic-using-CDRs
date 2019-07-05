/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author essam
 */
public class Random {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double sum = 0;
        ArrayList<Double> vals = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            double prob = Math.random();
            sum+=prob;
            vals.add(prob);
            System.out.println(prob);
            
        }
        
        for (Iterator<Double> iterator = vals.iterator(); iterator.hasNext();) {
            Double next = iterator.next();
            System.out.printf("%f , %f\n",next,next/sum);
            
        }
    }
    
}

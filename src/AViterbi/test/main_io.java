/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import java.util.Hashtable;
import java.util.Map;

import AViterbi.IO;

/**
 *
 * @author essam
 */
public class main_io {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String states_file = "/home/essam/traffic/DI/Dakar-2.1/dakar-2.states";
        String hmm_file = "/home/essam/traffic/DI/Dakar-2.1/uhmm/dakar_u_07.hmm";
        IO io = new IO();

        String[] states = io.read_states(states_file);

        System.out.println("state 2\t"+states[1]);
        Hashtable<String, Double> strt = new Hashtable<>();
        Hashtable<String, Hashtable<String, Double>> trans_p = new Hashtable<>();
        Hashtable<String, Hashtable<String, Double>> emit_p = new Hashtable<>();

        io.readHMM(states, hmm_file, strt, trans_p, emit_p);

        Hashtable<String, Double> t = trans_p.get(states[1]);
        for (Map.Entry<String, Double> entrySet : t.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            System.out.print(value + " ");
        }

    }

}

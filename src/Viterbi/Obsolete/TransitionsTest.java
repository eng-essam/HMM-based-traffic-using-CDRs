/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Hashtable;

import Viterbi.TransBuilder;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class TransitionsTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String distPath = args[0];
        String vorNghbrPath = args[1];
        String transPath = args[2];
        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> mapList = adaptor.readNetworkDist(distPath);
        System.out.println("map list"+mapList.size());
        TransBuilder constructor = new TransBuilder(mapList,vorNghbrPath);
        Hashtable<String, Hashtable<String, Double>> transitions = constructor.getTransitionProb();
        System.out.println(transitions.size());
        constructor.writeZonesTrans(transitions, transPath);
        
//        for (Map.Entry<String, Hashtable<String, Double>> entrySet : transitions.entrySet()) {
//            String key = entrySet.getKey();
//            System.out.println(key);
//            Hashtable<String, Double> value = entrySet.getValue();
//            for (Map.Entry<String, Double> entrySet1 : value.entrySet()) {
//                String key1 = entrySet1.getKey();
//                Double value1 = entrySet1.getValue();
//                System.out.format("\t%s\t%f\n", key1,value1);
//                
//            }
//            
//        }
    }

}

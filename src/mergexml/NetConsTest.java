/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mergexml;

import java.util.ArrayList;

import utils.Edge;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class NetConsTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String edgesPath = args[0];
        String probPath = args[1];

        NetConstructor constructor = new NetConstructor(edgesPath, probPath);
        ArrayList<Edge> edges = constructor.readedges();
        System.out.println("edges: \t" + edges.size());
        /**
         * Sat 18 Apr 2015 04:35:44 AM JST
         *
         * create a traffic network , with XY coordinates of the center point of
         * the road segments.
         */
        ArrayList<FromNode> Transitions = constructor.construct_traffic_map();
        /**
         * Sat 18 Apr 2015 04:38:37 AM JST 
         * 
         * Old implementation, construct a traffic network with start point of
         * the road segment as the marked points for the transition calculation.
         * I think this badly affect the transition probabilities and the
         * gravity calculated after that.
         */
//        ArrayList<FromNode> Transitions = constructor.constructTransitions();
        
        
//        ArrayList<FromNode> Transitions = constructor.construct_edges_map();
        System.out.println("Transitions: \t" + Transitions.size());
        constructor.writeProbFile(Transitions);

    }

}

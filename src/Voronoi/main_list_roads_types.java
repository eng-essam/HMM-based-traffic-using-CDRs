/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mergexml.NetConstructor;
import utils.Edge;

/**
 *
 * @author essam
 */
public class main_list_roads_types {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String edgesPath = "/media/essam/Dell1/traffic/Alex/All_routes/edges.xml";
//        String edgesPath = "/media/essam/Dell1/traffic/Dakar_1/edges.xml";

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        
        String[] main_roads = {"motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link", "secondary", "secondary_link", "tertiary"};
        List<String> roads = new ArrayList<>(Arrays.asList(main_roads));
        System.out.println(roads.size());
//        List<String> roads = new ArrayList<>();
        for (Iterator<Edge> iterator = edges.iterator(); iterator.hasNext();) {
            Edge e = iterator.next();

            if (!roads.contains(e.getType())) {
                roads.add(e.getType());
            }
        }

        for (Iterator<String> iterator = roads.iterator(); iterator.hasNext();) {
            System.out.print("\"" + iterator.next() + "\",");

        }
        System.out.println("");

    }

}

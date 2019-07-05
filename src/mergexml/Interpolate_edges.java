/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mergexml;

import java.util.ArrayList;

import utils.Edge;

/**
 *
 * @author essam
 */
public class Interpolate_edges {

    /**
     * @param args the command line arguments
     * 
     * java -Xss128m -d64 -XX:+UseG1GC -Xms5g -Xmx5g -XX:MaxGCPauseMillis=500 mergexml.Interpolate_edges $dir/edges.xml 100
     */
    public static void main(String[] args) {
        
        String edgesPath= args[0];
        double threshold = Double.parseDouble(args[1]);
        
        NetConstructor constructor = new NetConstructor(edgesPath);
        ArrayList<Edge> edges = constructor.interpolate_edges(threshold);
        System.out.println("edges: \t"+edges.size());
//        ArrayList<FromNode> Transitions = constructor.constructTransitions();
//        System.out.println("Transitions: \t"+Transitions.size());
//        constructor.writeProbFile(Transitions);
        NetConstructor.write_edges(edges, edgesPath.replace(".xml", ".interpolated.xml"));
        
                
    }
    
}

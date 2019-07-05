/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import AViterbi.Interpolate.Interpolation;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_distance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Vertex v1 = new Vertex(1, 3);
        Vertex v2 = new Vertex(6, 8);

        Vertex p = new Vertex(2, 6);
        Interpolation interpolation = new Interpolation(null,  null);

        System.out.println(interpolation.distance(v1, v2, p));
    }

}

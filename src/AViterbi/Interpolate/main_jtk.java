/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.Interpolate;

import edu.mines.jtk.interp.CubicInterpolator;

/**
 *
 * @author essam
 */
public class main_jtk {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int size = 3;

        float[] x = new float[size];
        x[2] = 2;
        x[1] = 1f;
        x[0] = 2.5f;

        float[] y = new float[size];
        y[2] = 5;
        y[1] = 1f;
        y[0] = 3.5f;

        CubicInterpolator ci = new CubicInterpolator(CubicInterpolator.Method.MONOTONIC, x, y);
        float iy = ci.interpolate(1.8f);

        System.out.println(iy);

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.Interpolate;

import org.apache.commons.math3.analysis.interpolation.HermiteInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

/**
 *
 * @author essam
 */
public class main_hermite_interpolation {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double nsample = 4;
//        double[] samples = new double[(int)nsample * 2];
        double[] samples = {2, 2, 3, 3, 6, 6, 7, 5};

        HermiteInterpolator hermite = new HermiteInterpolator();
//        hermite.addSamplePoint(nsample, samples);

        for (int i = 0; i < samples.length; i += 2) {
            // at x  value, provide both value and first derivative
            hermite.addSamplePoint(i / 2, new double[]{samples[i]}, new double[]{samples[i + 1]});
        }

        double ts[] = hermite.value(2);
        System.out.println("the second values: ");
        print(ts);

        PolynomialFunction[] funs = hermite.getPolynomials();
        for (int i = 0; i < funs.length; i++) {
            PolynomialFunction fun = funs[i];
            double[] fc = fun.getCoefficients();
            System.out.println("Coeffecients of function :"+i);
            print(fc);
        }
    }

    /**
     * Print array of double values ...
     * 
     * @param v 
     */
    private static void print(double[] v) {

        for (int i = 0; i < v.length; i++) {
            System.out.print(v[i]+",");
//            if (i < v.length - 1) {
//                System.err.print(",");
//            }
        }
        System.out.println("");

    }

}


import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class correlations {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double[] x = {0, 0.4019607843, 0.5980392157, 0.7941176471, 1, 1.2058823529, 1.4117647059, 1.5980392157, 1.8039215686, 2, 2.2058823529, 2.4019607843, 2.6078431373, 2.8039215686, 3};
        double[] y = {0.6728971963, 0.6479750779, 0.6479750779, 0.6479750779, 0.6604361371, 0.8224299065, 1.0093457944, 1.2834890966, 1.4953271028, 1.8068535826, 2.1806853583, 2.4797507788, 2.8411214953, 3.0778816199, 3.5140186916};
        double data[][] = {x, y};

        KendallsCorrelation kc = new KendallsCorrelation(data);
        RealMatrix kmc = kc.getCorrelationMatrix();
       
        System.out.println("KendallsCorrelation\t" + new KendallsCorrelation().correlation(x, y));
        System.out.println("PearsonsCorrelation\t" + new PearsonsCorrelation().correlation(x, y));
        System.out.println("SpearmansCorrelation\t" + new SpearmansCorrelation().correlation(x, y));

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author essam
 */
public class main_string {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String file_name = "SET2_P01.CSV.0_500_th-dist_1000_th-time_60.viterbi.day.17";

        String set_file_name = file_name.substring(0, file_name.indexOf("."));
        int day = Integer.parseInt(file_name.substring(file_name.lastIndexOf(".") + 1));

        System.out.println(day + "\t" + set_file_name);

        String dir = "/media/essam/Dell1/traffic/results_dec-2015/hr/SET2_P01.CSV.0_500_th-dist_1000_th-time_60.viterbi.day.8";
        String parent_dir = dir.substring(0, dir.lastIndexOf("/") + 1);
        System.out.println(parent_dir + day);

        String file = "SET2_P06.CSV.gz";
        String dataset_regex = "SET2_P[0-9]+.CSV";
        if (file.matches(dataset_regex)) {
            System.out.println("match");
        }

        file_name = "SET2_P01.CSV.0_500_th-dist_1000_th-time_60.density.day.7.xml";
        set_file_name = file_name.substring(0, file_name.indexOf("."));
        day = Integer.parseInt(file_name.substring(file_name.indexOf("day.")+4,file_name.lastIndexOf(".")));
        
        System.out.println(set_file_name + "\t"+day );
        
        file_name= "2013-01-14.xml";
        String daily_density_regex = "[0-9]+-[0-9]+-[0-9]+.xml";
        
        
        if (file_name.matches(daily_density_regex)) {
            String day_date = file_name.substring(0, file_name.lastIndexOf("."));
            System.out.println(day_date);
        }
    }

}



import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Essam
 */
public class Rename {

    public static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry.getPath());
//                System.out.println(fileEntry.getName());
            }
        }
        return files;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException {
//        String dir = "/home/essam/traffic/filtered_trips/without_training_3";
        String dir = args[0];
        String start_date = "2013-01-07";
        DateFormat sdformatter = new SimpleDateFormat("yyyy-mm-dd");

        Calendar scal = Calendar.getInstance();
        scal.setTime(sdformatter.parse(start_date));
        int month = 2;
        for (int i = 1; i <= 25; i++) {
            while (true) {
                int day = scal.get(Calendar.DAY_OF_MONTH);

                String vit_file = "SET2_P" + String.format("%02d", i) + ".CSV.0_500_th-dist_1000_th-time_60.viterbi.day." + day + ".xml";
                String oldfile = dir + "/" + vit_file;
                File file = new File(oldfile);
                if (file.exists() && !file.isDirectory()) {
                    /* rename */
                    System.out.println("File rename");

                    start_date = sdformatter.format(scal.getTime());
                    String newFile = oldfile.substring(0, oldfile.lastIndexOf("day")) + start_date + ".xml";
                    File file2 = new File(newFile);
                    if (file2.exists()) {
                        throw new java.io.IOException("file exists");
                    }

                    // Rename file (or directory)
                    boolean success = file.renameTo(file2);
                    if (day == scal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                        String tmp = "2013-" + String.format("%02d", month++) + "-01";
                        scal.setTime(sdformatter.parse(tmp));
//                        System.out.println("Change month");
//                        scal.set(Calendar.DAY_OF_MONTH, 1);
//                        scal.set(Calendar.MONTH, scal.get(Calendar.MONTH) + 1);
//                        System.out.println("month" + scal.get(Calendar.MONTH));
                    } else {
                        scal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } else {
                    break;
                }

            }
        }
//        int set = 1;
//        int day = scal.get(Calendar.DAY_OF_MONTH);
//
//        String vit_file = "SET2_P" + String.format("%02d", set) + ".CSV.0_500_th-dist_1000_th-time_60.viterbi.day." + day + ".xml";
//
//        scal.add(Calendar.DAY_OF_MONTH, 1);
//        start_date = sdformatter.format(scal.getTime());

//        ArrayList<String> files = listFilesForFolder(new File(dir));
//        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
//            String next = iterator.next();
//            File file = new File(next);
//            System.out.println(next);
//            // File (or directory) with new name
//            String newFile = next.replaceAll(":", "-");
//            File file2 = new File(newFile);
//            if (file2.exists()) {
//                throw new java.io.IOException("file exists");
//            }
//
//            // Rename file (or directory)
//            boolean success = file.renameTo(file2);
//        }
    }
}

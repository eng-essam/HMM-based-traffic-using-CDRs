/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import Observations.ObsTripsBuilder;
import utils.CDR;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class HPopulation {

    Hashtable<Integer, List<Tower>> users;
    ArrayList<Integer> t_days;
    List<CDR> cdrs;

    public HPopulation() {
        users = new Hashtable<>();
        t_days = new ArrayList<>();
        cdrs = new ArrayList<>();
    }

    /**
     * Check if the list of towers find_tower a specific tower id ..
     *
     * @param towers
     * @param twr_id
     * @return
     */
    private Tower find_tower(List<Tower> towers, int twr_id) {
        for (Iterator<Tower> iterator = towers.iterator(); iterator.hasNext();) {
            Tower next = iterator.next();
            if (next.id == twr_id) {
                return next;
            }
        }
        return null;
    }

    /**
     * Return number of days used in the study ...
     */
    public int get_total_days() {
        return t_days.size();
    }

    /**
     * Read CDRs from a number of a dataset files ...
     *
     * @param dataset_path
     * @return
     */
    public List<CDR> getObs(String dataset_path) {

//        List<CDR> cdrs = new ArrayList<>();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                this.cdrs.addAll(DataHandler.read_dataset(subsetPath));
            }
        }

        return this.cdrs;
    }

   
    /**
     * Read CDRs from a number of a dataset files ...
     *
     * @param dataset_path
     * @param start_usr_id
     * @param end_usr_id
     * @return
     */
    public List<CDR> getObs(String dataset_path, int start_usr_id, int end_usr_id) {

//        List<CDR> cdrs = new ArrayList<>();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                this.cdrs.addAll(read_dataset(subsetPath, start_usr_id, end_usr_id));
            }
        }

        return this.cdrs;
    }

    /**
     * Read CDRs from a number of a dataset files ...
     *
     * @param dataset_path
     * @param s_usr_id
     * @param e_usr_id
     * @return
     */
    public List<CDR> parallel_getObs(String dataset_path, final int s_usr_id, final int e_usr_id) {

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(threads);
        List<Future<List<CDR>>> futures = new ArrayList<Future<List<CDR>>>();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dataset_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                Callable<List<CDR>> callable = new Callable<List<CDR>>() {
                    @Override
					public List<CDR> call() throws Exception {
                        List<CDR> output = read_dataset(subsetPath, s_usr_id, e_usr_id);
                        // process your input here and compute the output
                        return output;
                    }
                };
                futures.add(service.submit(callable));
//                cdrs.addAll(read_dataset(subsetPath));
            }
        }

        service.shutdown();

        for (Future<List<CDR>> future : futures) {
            try {
                this.cdrs.addAll(future.get());
                System.out.println(" getObs() ===> CDR size: "+this.cdrs.size());
            } catch (InterruptedException ex) {
                Logger.getLogger(HPopulation.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(HPopulation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.cdrs;
    }

    /**
     * populate user data by segment data into chunks and run in parallel individual chunks ..
     * @param dataset_path
     * @param start_usr_id
     * @param end_usr_id
     * @param weekend_days_flag
     * @return 
     */
     public Hashtable<Integer, List<Tower>> parallel_populate(String dataset_path, int start_usr_id, int end_usr_id, boolean weekend_days_flag) {
        getObs(dataset_path);
        
         System.out.println("CDR size: "+this.cdrs.size());
        int threads = Runtime.getRuntime().availableProcessors();
        int usrs = end_usr_id - start_usr_id;
        final int chunk = (int) Math.ceil((double) usrs / (double) threads);

        ExecutorService service = Executors.newFixedThreadPool(threads);

        List<Future<UTowers>> futures = new ArrayList<Future<UTowers>>();
        for (int i = 0; i < threads; i++) {
            final int s_index = start_usr_id + i * chunk;

            Callable<UTowers> callable = new Callable<UTowers>() {
                @Override
				public UTowers call() throws Exception {
                    UTowers output = new UTowers();
                    // process your input here and compute the output
                    output.towers = populate(s_index, s_index + chunk, weekend_days_flag);

                    return output;
                }
            };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        for (Future<UTowers> future : futures) {
            UTowers ut;
            try {
                ut = future.get();
                users.putAll(ut.towers);
            } catch (InterruptedException ex) {
                Logger.getLogger(HPopulation.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(HPopulation.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        System.out.println("Finish population of towers ...");
        return users;
    }

    /**
     *
     * @param dataset_path
     * @param start_usr_id
     * @param end_usr_id
     * @param weekend_days_flag
     * @return
     */
    private Hashtable<Integer, List<Tower>> populate(int start_usr_id, int end_usr_id, boolean weekend_days_flag) {
//        List<CDR> cdrs = parallel_getObs(dataset_path);
        for (Iterator<CDR> iterator = this.cdrs.iterator(); iterator.hasNext();) {
            CDR record = iterator.next();
            if (record.id > start_usr_id && record.id < end_usr_id) {
                if (!weekend_days_flag && !DataHandler.is_week_day(record.date)) {
                    continue;
                }

                // calculate days used in the study ...
                Calendar cal = Calendar.getInstance();
                cal.setTime(record.date);
                int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

                if (!t_days.contains(dayOfYear)) {
                    t_days.add(dayOfYear);
                }

                List<Tower> towers;
                if (users.containsKey(record.id)) {
                    towers = users.get(record.id);
                    Tower T = find_tower(towers, record.twr_id);
                    if (T != null) {
                        T.add_occurance(record.date);
                    } else {
                        towers.add(new Tower(record.twr_id, record.date));
                    }
                    users.replace(record.id, towers);
                } else {
                    towers = new ArrayList<>();
                    Tower T = new Tower(record.twr_id, record.date);
                    towers.add(T);
                    users.put(record.id, towers);

                }
            }
        }

        return users;
    }

    

    /**
     *
     * @param dataset_path
     * @return
     */
    public Hashtable<Integer, List<Tower>> populate(String dataset_path, boolean weekend_days_flag) {
        List<CDR> cdrs = DataHandler.read_dataset(dataset_path);
        for (Iterator<CDR> iterator = cdrs.iterator(); iterator.hasNext();) {
            CDR record = iterator.next();

            if (!weekend_days_flag && !DataHandler.is_week_day(record.date)) {
                continue;
            }

            // calculate days used in the study ...
            Calendar cal = Calendar.getInstance();
            cal.setTime(record.date);
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

            if (!t_days.contains(dayOfYear)) {
                t_days.add(dayOfYear);
            }

            List<Tower> towers;
            if (users.containsKey(record.id)) {
                towers = users.get(record.id);
                Tower T = find_tower(towers, record.twr_id);
                if (T != null) {
                    T.add_occurance(record.date);
                } else {
                    towers.add(new Tower(record.twr_id, record.date));
                }
                users.replace(record.id, towers);
            } else {
                towers = new ArrayList<>();
                Tower T = new Tower(record.twr_id, record.date);
                towers.add(T);
                users.put(record.id, towers);

            }

        }

        return users;
    }

    /**
     * Read CDR data as it is; without any preprocessing of the data ..
     *
     * @param path
     * @param start_usr_id
     * @param end_usr_id
     * @return
     */
    public List<CDR> read_dataset(String path, int start_usr_id, int end_usr_id) {

        List<CDR> cdrs_subset = new ArrayList<>();

        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));

            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                CDR record = new CDR();
                String lineSplit[] = line.split(",");
                record.id = Integer.parseInt(lineSplit[0]);
                if (record.id >= start_usr_id && record.id <= end_usr_id) {

                    try {
                        record.date = parserSDF.parse(lineSplit[1]);

//                    Calendar cal = Calendar.getInstance();
//                    cal.setTime(record.date);
//                    int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
//
//                    if (!t_days.contains(dayOfYear)) {
//                        t_days.add(dayOfYear);
//                    }
                    } catch (ParseException ex) {
                        Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    record.twr_id = Integer.parseInt(lineSplit[2]);
                    cdrs_subset.add(record);
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cdrs_subset;
    }

    /**
     * Sort towers in descending order to be used in clustering ...
     *
     * @param users_data
     * @return
     */
    public Hashtable<Integer, List<Tower>> sort_towers(Hashtable<Integer, List<Tower>> users_data) {
        users_data.entrySet().stream().forEach((entrySet) -> {
            Integer usr_key = entrySet.getKey();
            List<Tower> twr_list = entrySet.getValue();
            Collections.sort(twr_list, (Tower t2, Tower t1) -> t1.days() - t2.days());
            users_data.replace(usr_key, twr_list);
        });
        return users_data;
    }
}

class UTowers {

    Hashtable<Integer, List<Tower>> towers;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author essam
 */
public class TCluster {

    /**
     * List of the contacted towers
     */
    int centroid;
    int weight;
    private int tower_days_sum;
    private int duration;
    private int work_hour_events;
    private int home_hour_events;
    private boolean consistant_data;
    private List<Integer> contacted_days;
    List<Tower> towers;

    private int days;
    private double tower_days_percent;
    private double days_percent;
    private double duration_percent;
    private double importance;

    private boolean home_loc = false;
    private boolean work_loc = false;

    public TCluster(int t_id) {

        this.centroid = t_id;
        consistant_data = false;
        this.towers = new ArrayList<>();
        Tower t = new Tower();
        t.set_id(t_id);
        this.towers.add(t);
    }

    public TCluster(Tower t) {
        this.centroid = t.id;
        consistant_data = false;
        this.towers = new ArrayList<>();
        this.towers.add(t);
    }

    public void add_tower(Tower t) {
        towers.add(t);
        /**
         * inverse the status of the data calculated ....
         */
        if (consistant_data) {
            consistant_data = !consistant_data;
        }
    }

    public void add_towers(List<Tower> ts) {
        towers.addAll(ts);
        /**
         * inverse the status of the data calculated ....
         */
        if (consistant_data) {
            consistant_data = !consistant_data;
        }
    }

    /**
     * Calculate observable factors of the cluster ..
     */
    private void cal_factors() {

        tower_days_sum = 0;
        duration = 0;
        work_hour_events = 0;
        home_hour_events = 0;
        this.contacted_days = new ArrayList<>();

        for (Iterator<Tower> iterator = towers.iterator(); iterator.hasNext();) {
            Tower t = iterator.next();

            // add contact days ...
            List<Integer> tc = t.contacted;
            for (Iterator<Integer> tc_iterator = tc.iterator(); tc_iterator.hasNext();) {
                int tc_i = tc_iterator.next();
                if (!contacted_days.contains(tc_i)) {
                    contacted_days.add(tc_i);
                }

            }

            // tower days ..
            tower_days_sum += t.days();
            home_hour_events += t.home_hour_event();
            work_hour_events += t.work_hour_event();

        }

        /**
         * Calculate the duration between the first and last contacted tower in
         * the cluster ....
         */
        Collections.sort(contacted_days);
        duration = Math.abs(contacted_days.get(0) - contacted_days.get(contacted_days.size() - 1));
    }

    public int get_centroid() {
        return centroid;
    }

    public String get_cluster_elements() {
        if (towers.isEmpty()) {
            return "";
        }
        String joined = Integer.toString(towers.get(0).id);
        for (int i = 1; i < towers.size(); i++) {
            Tower t = towers.get(i);
            joined += "," + t.id;

        }
        return joined;
    }

    public List<Integer> get_cluster_members() {

        List<Integer> elements = new ArrayList<>();
        for (Iterator<Tower> iterator = towers.iterator(); iterator.hasNext();) {
            Tower t = iterator.next();
            elements.add(t.id);
        }
        return elements;
    }

    /**
     *
     * @return the number of contact day, each day added once to the contact
     * list ...
     */
    public int get_days() {
        if (!consistant_data) {
            cal_factors();
            this.consistant_data = true;
        }
        if (contacted_days.isEmpty() || contacted_days == null) {
            return days;
        }
        return contacted_days.size();
    }

    public double get_days_percent() {
        return days_percent;
    }

    /**
     *
     * @return the number of days elapsed between the first contact with any
     * cell and the last contact ..
     */
    public int get_duration() {
        if (!consistant_data) {
            cal_factors();
            this.consistant_data = true;
        }
        return duration;

    }

    public double get_duration_percent() {
        return duration_percent;
    }

    /**
     *
     * @return the total home hour events for the cluster
     */
    public int get_home_hour_events() {
        if (!consistant_data) {
            cal_factors();
            this.consistant_data = true;
        }
        return home_hour_events;
    }

    public double get_importance() {
        return importance;
    }

    /**
     *
     * @return sum the number of contact days added by each tower ...
     */
    public int get_tower_days() {
        if (!consistant_data) {
            cal_factors();
            this.consistant_data = true;
        }
        return tower_days_sum;

    }

    public double get_tower_days_percent() {
        return tower_days_percent;
    }

    /**
     *
     * @return the total number of work hour events
     */
    public int get_work_hour_events() {
        if (!consistant_data) {
            cal_factors();
            this.consistant_data = true;
        }
        return work_hour_events;
    }

    public boolean is_home_loc() {
        return home_loc;
    }

    public boolean is_work_loc() {
        return work_loc;
    }

    public void set_cluster_elements(String elements) {
        String segs[] = elements.split(",");
        for (int i = 0; i < segs.length; i++) {
            int t_id = Integer.parseInt(segs[i]);
            Tower t = new Tower();
            t.set_id(t_id);
            towers.add(t);

        }
    }

    public void set_consistant_data(boolean consistant_data) {
        this.consistant_data = consistant_data;
    }

    public void set_days(int days) {
        contacted_days = new ArrayList<>();
        this.days = days;
    }

    public void set_days_percent(double days_percent) {
        this.days_percent = days_percent;
    }

    public void set_duration(int duration) {
        this.duration = duration;
    }

    public void set_duration_percent(double duration_percent) {
        this.duration_percent = duration_percent;
    }

    /**
     * Calculate the percentage of the current cluster between all clusters ...
     *
     * @param t_days
     * @param t_tower_days
     * @param t_duration
     */
    public void set_factors_percent(int t_days, int t_tower_days, int t_duration) {

        days_percent = get_days() / (double) t_days;
        tower_days_percent = get_tower_days() / (double) t_tower_days;
        duration_percent = get_duration() / (double) t_duration;

        importance = (days_percent + tower_days_percent + duration_percent) / 3.0;
    }

    public void set_home_hour_events(int home_hour_events) {
        this.home_hour_events = home_hour_events;
    }

    public void set_home_loc(boolean home_loc) {
        this.home_loc = home_loc;
    }

    public void set_importance(double importance) {
        this.importance = importance;
    }

    public void set_tower_days_percent(double tower_days_percent) {
        this.tower_days_percent = tower_days_percent;
    }

    public void set_tower_days_sum(int tower_days_sum) {
        this.tower_days_sum = tower_days_sum;
    }

    public void set_weight(int t_days) {
        this.weight = get_days();
    }

    public void set_work_hour_events(int work_hour_events) {
        this.work_hour_events = work_hour_events;
    }

    public void set_work_loc(boolean work_loc) {
        this.work_loc = work_loc;
    }

}

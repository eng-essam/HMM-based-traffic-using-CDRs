/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author essam
 */
public class Tower {

    /**
     * Contacted tower and list of days on which such tower has been contacted
     * ..
     */
    int id;
    private int night_events;
    private int day_light_events;
    List<Date> occurance;
    List<Integer> contacted;

    private List<Date> tmp_day_light_events;
    private List<Date> tmp_night_events;
    /**
     * Home hours constants ..
     */
    private final int START_NIGHT_HOURS = 19;
    private final int END_NIGHT_HOURS = 7;

    /**
     * Work hours constants ..
     */
    private final int START_WORK_HOURS = 13;
    private final int END_WORK_HOURS = 17;

    public Tower() {
        initialize();
    }

    public Tower(int id, Date d) {
        initialize();
        this.id = id;
        add_occurance_data(d);
    }

    /**
     * Reduce the effect of redundant events per day or night ..
     *
     * @param d
     */
    private void add_hw_events(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= START_WORK_HOURS && hourOfDay <= END_WORK_HOURS) {
            cal.set(Calendar.HOUR_OF_DAY, 14);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            d = cal.getTime();
            if (!tmp_day_light_events.contains(d)) {
                tmp_day_light_events.add(d);
                day_light_events = tmp_day_light_events.size();
            }
        } else if (hourOfDay >= START_NIGHT_HOURS || hourOfDay <= END_NIGHT_HOURS) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            d = cal.getTime();
            if (!tmp_night_events.contains(d)) {
                tmp_night_events.add(d);
                night_events = tmp_night_events.size();
            }
        }

    }

    public boolean add_occurance(Date d) {
        if (this.occurance == null) {
            return false;
        }
        add_occurance_data(d);
        return true;

    }

    /**
     *
     * @param Date of the day ...
     */
    private void add_occurance_data(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        this.occurance.add(d);
        if (!contacted.contains(dayOfYear)) {
            contacted.add(dayOfYear);
        }

        //increment night or day light events ..
        if (hourOfDay >= START_WORK_HOURS && hourOfDay <= END_WORK_HOURS) {
            day_light_events++;
        } else if (hourOfDay >= START_NIGHT_HOURS || hourOfDay <= END_NIGHT_HOURS) {
            night_events++;
        }

        /**
         * another way to increment the day/night event, but I'll use Isaacman
         * 2011 method count only ...
         */
//        add_hw_events(d);
    }

    public int days() {
        return contacted.size();
    }

    public int get_id() {
        return id;
    }

    public int home_hour_event() {
        return night_events;
    }

    /**
     * initialize tower parameters ..
     */
    private void initialize() {
        this.night_events = 0;
        this.day_light_events = 0;
        this.occurance = new ArrayList<>();
        this.contacted = new ArrayList<>();

        tmp_day_light_events = new ArrayList<>();
        tmp_night_events = new ArrayList<>();

    }

    public void set_id(int t_id) {
        id = t_id;
    }

    public int work_hour_event() {
        return day_light_events;
    }
}

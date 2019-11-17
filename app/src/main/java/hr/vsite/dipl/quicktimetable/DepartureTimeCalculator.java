package hr.vsite.dipl.quicktimetable;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.joda.time.chrono.StrictChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

/**
 * Created by Jure on 30.1.2017..
 * This class works as a time calculator that uses known tram station departure times and
 * line segment times(time offsets) to find departure time closest to current time;
 */

public class DepartureTimeCalculator {

    int closestHour;

    /**
     * Returns the closest hour to the current time.
     * @return a String representing the hour that will be used in SQL query
     */
    public String closestHour() {
        int hourNow = DateTime.now().getHourOfDay();
        closestHour = hourNow;
        // hours of day when daytime tram lines do not operate
        if (hourNow == 1 || hourNow == 2 || hourNow == 3) {
            // find next valid hour
            while (closestHour >= 1 && closestHour <= 3) {
                closestHour = closestHour + 1;
            }
        }
        else {
            closestHour = hourNow;
        }
        return formatHH(closestHour) + ":";
    }

    /**
     * Returns the closest hour to the current time increased by one. This is required
     * when departure times span between the end of the current hour and the next hour.
     * @return a String representing the hour that will be used in SQL query
     */
    public String closestHourPlusOne() {
        int nextHour;
        if(closestHour == 23) {
            nextHour = 0;
        }
        else {
            nextHour = closestHour + 1;
        }
        return formatHH(nextHour) + ":";
    }

    private String formatHH(int hour) {
        // (0)0 - (0)9
        if (hour >= 0 && hour <= 9) {
            return "0" + String.valueOf(hour);
        }
        // 10 - 23
        else {
            return String.valueOf(hour);
        }
    }

    /***
     * Returns a subset of departure times that are in the future.
     * @param potentialDepartures List of potential departures in a time frame.
     * @return List of departures that are in the future.
     */
    public ArrayList<LineDeparturePair> nextDepartures(ArrayList<LineDeparturePair> potentialDepartures) {
        ArrayList<LineDeparturePair> departures = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
        for (LineDeparturePair potentialDeparture : potentialDepartures) {
            LocalTime potentialTime = LocalTime.parse(potentialDeparture.getDepartureTime(), dateTimeFormatter);
            // if the departure time has not passed
            if (potentialTime.isAfter(DateTime.now().toLocalTime()) && departures.size() < QTTconstants.DEPARTURES_LIST_LIMIT) {
                // add line number and departure time to list of departures
                departures.add(
                        new LineDeparturePair(potentialDeparture.getLineNumber(), potentialTime.toString(dateTimeFormatter))
                );
            }
        }
        // in case no departures are found in current hour increase hour
        return departures;
    }
}

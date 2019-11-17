package hr.vsite.dipl.quicktimetable;

/**
 * Created by Jure on 4.11.2016..
 */

public class StationDepartureTime {
    private long id;
    private String time;
    private long stationId;
    private long lineNumber;

    public StationDepartureTime(long stationId, long lineNumber, String time) {
        this.stationId = stationId;
        this.lineNumber = lineNumber;
        this.time = time;
    }

    public long getStationId() {
        return stationId;
    }

    public String getTime() {
        return time;
    }

    public long getLineNumber() {
        return lineNumber;
    }
}

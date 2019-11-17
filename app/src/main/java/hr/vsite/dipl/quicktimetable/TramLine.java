package hr.vsite.dipl.quicktimetable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jure on 4.2.2016..
 */
public class TramLine {

    private long id;
    private long lineNumber;
    private long lineDirection;
    private String name;
    private List<Long> stationIds = new ArrayList<Long>();
    private String startStation;
    private String endStation;

    public TramLine(long id, long lineNumber, long lineDirection, String name, List<Long> stationIds, String startStation, String endStation) {
        this.id = id;
        this.lineNumber = lineNumber;
        this.lineDirection = lineDirection;
        this.name = name;
        this.stationIds = stationIds;
        this.startStation = startStation;
        this.endStation = endStation;
    }

    public TramLine(long id, long lineNumber, String name, List<Long> stationIds) {
        this.id = id;
        this.lineNumber = lineNumber;
        this.name = name;
        this.stationIds = stationIds;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public long getLineDirection() {
        return lineDirection;
    }

    /**
     * Retruns a list of station id's on the TramLine
     * @return List of station id's
     */
    public List<Long> getStationIds() {
        return stationIds;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getEndStation() {
        return endStation;
    }
}

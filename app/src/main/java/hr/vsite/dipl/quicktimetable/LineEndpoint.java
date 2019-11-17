package hr.vsite.dipl.quicktimetable;

import java.util.Objects;

/**
 * Created by Jure on 15.12.2016..
 * This class represents a single line endpoint defined by a start station, an end station
 * and the line number that identifies the tram line.
 */
public class LineEndpoint {
    private String lineNumber;
    private String startStation;
    private String endStation;

    public LineEndpoint(String lineNumber, String startStation, String endStation) {
        this.lineNumber = lineNumber;
        this.startStation = startStation;
        this.endStation = endStation;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEqual;
        if (o instanceof LineEndpoint) {
            LineEndpoint test = (LineEndpoint)o;
            if (this.lineNumber.equals(test.lineNumber) &&
                    this.startStation.equals(test.startStation) &&
                    this.endStation.equals(test.endStation)) {
                isEqual = true;
            }
            else isEqual = false;
        }
        else {
            isEqual = false;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + lineNumber.hashCode();
        hash = 31 * hash + startStation.hashCode();
        hash = 31 * hash + endStation.hashCode();

        return hash;
    }
}

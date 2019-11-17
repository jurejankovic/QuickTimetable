package hr.vsite.dipl.quicktimetable;

import java.util.List;

/**
 * Created by Jure on 7.11.2015..
 * Represents a tram network (graph) containing tram stations (vertices)
 * connected by tram line segments (edges).
 */
public class TramNetwork {

    private List<TramStation> tramStations;
    private List<TramLineSegment> tramLineSegments;

    public TramNetwork(List<TramStation> tramStations, List<TramLineSegment> tramLineSegments) {
        this.tramStations = tramStations;
        this.tramLineSegments = tramLineSegments;
    }

    public List<TramLineSegment> getTramLineSegments() {
        return tramLineSegments;
    }

    public List<TramStation> getTramStations() {
        return tramStations;
    }
}

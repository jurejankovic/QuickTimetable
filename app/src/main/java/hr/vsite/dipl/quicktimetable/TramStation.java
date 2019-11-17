package hr.vsite.dipl.quicktimetable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jure on 5.11.2015..
 * Represents a tram station as a node (vertex) on a tram network (graph).
 */
public class TramStation {

    private String name;
    private String shortName;
    private LatLng position;
    private long id;
    private long line;


    public void setId(long id) {
        this.id = id;
    }

/*    public  TramStation(long id, String name, LatLng position) {
        this.id = id;
        this.name = name;
        this.position = position;
//        this.line = line;
    }*/

    public TramStation(long id, String name, String shortName, LatLng position) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.position = position;
    }

    public String getShortName() {
        return shortName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public long getLine() {
        return line;
    }

    public void setLine(long line_id) {
        this.line = line_id;
    }
}

package hr.vsite.dipl.quicktimetable;

/** Pair of stations with the line they are located on.
 * Created by Jure on 12.11.2016..
 */
public class TramStationPair {
    private String stationA;
    private String stationB;
    private long lineId;

    public TramStationPair(String stationA, String stationB, long lineId) {
        this.stationA = stationA;
        this.stationB = stationB;
        this.lineId = lineId;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEqual;
        if (!(o instanceof TramStationPair)) {
            isEqual = false;
        }
        else {
            TramStationPair test = (TramStationPair) o;
            // check if stationA and stationB are the same in TramStationPair objects
            if ((test.stationA.equals(this.stationA) && test.stationB.equals(this.stationB)) && test.lineId == this.lineId) {
                isEqual = true;
            } else {
                isEqual = false;
            }
        }
        return isEqual;
    }
}

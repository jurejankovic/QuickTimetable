package hr.vsite.dipl.quicktimetable;

/**
 * Created by Jure on 5.11.2015..
 * Represents a tram line segment (edge) on a TramNetwork (graph)
 */
public class TramLineSegment {
    private long id;
    private long originId;
    private long destinationId;
    private int segmentTime;                 // time in seconds
    private long lineNumber;                // the line this segment belongs to
    private int direction;

    /**
     * Defines a segment on the tram line
     * @param segmentTime the time it takes the tram to get form origin to destination station
     * @param originId id of the origin station
     * @param destinationId id of the destination station
     */
    public TramLineSegment(int segmentTime, long originId, long destinationId) {
        this.segmentTime = segmentTime;
        this.originId = originId;
        this.destinationId = destinationId;
    }

    /**
     * Defines a segment on the tram line
     * @param segmentTime the time it takes the tram to get form origin to destination station
     * @param originId id of the origin station
     * @param destinationId id of the destination station
     * @param lineNumber number(name) of the line this segment is part of
     * @param direction
     */
    public TramLineSegment(long originId, long destinationId, int segmentTime, long lineNumber, int direction) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.segmentTime = segmentTime;
        this.lineNumber = lineNumber;
        this.direction = direction;
    }

    public int getSegmentTime() {
        return segmentTime;
    }

    public long getOriginId() {
        return originId;
    }

    public long getDestinationId() {
        return destinationId;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEqual;
        if (!(o instanceof TramLineSegment)) {
            isEqual = false;
        }
        else {
            TramLineSegment test = (TramLineSegment) o;
            // check if segments the same
            if (test.originId == this.originId && test.destinationId == this.destinationId &&
                    test.segmentTime == this.segmentTime && test.lineNumber == this.lineNumber &&
                    test.direction == this.direction) {
                isEqual = true;
            } else {
                isEqual = false;
            }
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = (int) (31 * hash + this.originId);
        hash = (int) (31 * hash + this.destinationId);
        hash = 31 * hash + this.segmentTime;
        hash = (int) (31 * hash + this.lineNumber);
        hash = 31 * hash + this.direction;

        return hash;
    }
}
